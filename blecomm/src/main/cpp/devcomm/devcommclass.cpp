#include "devcommclass.h"
#include "../constant/globalconstants.h"
#include "../util/ctimer.h"
#include "../util/encryptor/aesencryptor.h"

 extern "C" {
 #include "CLibrary/task_comm.h"
 #include "CLibrary/driver/drv.h"
 }

 static uint HandleCommand( uint8 /*u8_Address*/, uint8 u8_SourcePort, uint8 /*u8_TargetPort*/, const task_comm_command *tp_Command, uint8 /*u8_Mode*/ ) {
     return DevComm::sCallback->onHandleCommand( u8_SourcePort, tp_Command->u8_Operation, tp_Command->u8_Parameter, tp_Command->u8p_Data, tp_Command->u8_Length);
 }

 static uint HandleEvent( uint8 /*u8_Address*/, uint8 /*u8_SourcePort*/, uint8 /*u8_TargetPort*/, uint8 u8_Event ) {
     return DevComm::sCallback->onHandleEvent(u8_Event);
 }

 static uint WriteDevice( uint8 /*u8_Address*/, const uint8 *u8p_Data, uint8 u8_Length ) {
     return DevComm::sCallback->writeDevice(u8p_Data, u8_Length);
 }


 DevComm::Callback *DevComm::sCallback = 0;
 DevComm *DevComm::instance[DEV_COMM_TYPE_COUNT] = {
     new DevComm1(),
     new DevComm2()
 };
 uint DevComm::instanceNum = 0;

 void DevComm::select(int instance) {
     if (instance >= DEV_COMM_TYPE_COUNT)
         instance = 0;
     instanceNum = instance;
 }

 DevComm *DevComm::getInstance() {
     return instance[instanceNum];
 }


 DevComm1::DevComm1() {
     task_comm_callback t_Callback;

     Drv_Initialize();

     t_Callback.fp_HandleEvent = HandleEvent;
     t_Callback.fp_HandleCommand = HandleCommand;
     t_Callback.fp_WriteDevice = WriteDevice;
     TaskComm_SetConfig(0, TASK_COMM_PARAM_CALLBACK, (const void *) &t_Callback);

     TaskComm_Initialize();
 }

 DevComm1::~DevComm1() {
     TaskComm_Finalize();
     Drv_Finalize();
 }

 int DevComm1::setPacketLength(uint8 packetLength) {
     return TaskComm_SetConfig(0, TASK_COMM_PARAM_LINK, (const void *) &packetLength);
 }

 int DevComm1::send(int targetPort, int mode, int operation, int parameter, uint8 *data, uint16 length) {
     task_comm_command t_Command;
     t_Command.u8_Operation = operation;
     t_Command.u8_Parameter = parameter;
     t_Command.u8p_Data = data;
     t_Command.u8_Length = length;
     return TaskComm_Send( 0, 1, targetPort, &t_Command, mode );
 }

 int DevComm1::receive(const uint8 *data, uint16 length) {
     return DrvUART_Receive(0, data, length);
 }

 void DevComm1::turnOffEncryption() {
     return TaskComm_TurnOffEncryption();
 }

 void DevComm1::readyForEncryption(const uint8 *data, uint16 length) {
     return TaskComm_ReadyForEncryption(data);
 }

 void DevComm1::updateEncryption(const uint8 *data, uint16 length) {
     return TaskComm_UpdateForEncryption(data);
 }

 int DevComm1::setFrameOn(bool on) {
     return TaskComm_SetFrameOn(on);
 }

 bool DevComm1::isBusy() {
     return false;
 }

#if ENABLE_CRC16_CCITT
#include "CLibrary/lib_checksum.h"
#include "../util/byteutils.h"
#endif

DevComm2::DevComm2()  {
    sendTimeout = 400;
    retryCount = 3;
    retryTimer = new CTimer();
    encryptor = new AesEncryptor(AesEncryptor::CFB, AesEncryptor::AES128);
}

DevComm2::~DevComm2() {
    delete retryTimer;
    delete encryptor;
}

int DevComm2::setPacketLength(uint8 packetLength) {
    return FUNCTION_OK;
}

int DevComm2::send(int targetPort, int mode, int operation, int parameter, uint8 *data, uint16 length) {
    if (currentOp != 0xFF)
        return FUNCTION_FAIL;
    currentOp = operation;

    buffer[0] = operation;
    for (uint16 i = 0; i < length; i++)
        buffer[i+1] = data[i];

#if ENABLE_CRC16_CCITT
    uint16 crc16 = LibChecksum_GetChecksum16Bit_CCITT(buffer, length + 1);
    LittleEndianByteUtils::unsignedShortToBytes(crc16, buffer + length + 1);
    uint16 sendLength = length + 3;
#else
    uint16 sendLength = length + 1;
#endif

#if ENABLE_ENCRYPTION
    encryptor->encrypt(buffer, sendLength);
#endif

    if (!DevComm::sCallback->writeDevice(buffer, sendLength))
        return FUNCTION_FAIL;
    
    bool ok;
    ok = retryTimer->AsyncLoop(sendTimeout, [this](uint16 len) {
        LOGI("retryTimer execute");
        if (retryTimer->m_nCount >= retryCount) {
            retryTimer->CancelLoop();
            DevComm::sCallback->onHandleEvent(Global::EVENT_TIMEOUT);
            currentOp = 0xFF;
        } else if (currentOp != 0xFF) {
            DevComm::sCallback->writeDevice(buffer, len);
        }
    }, sendLength);

    return FUNCTION_OK;
}

int DevComm2::receive(const uint8 *data, uint16 length) {
#if ENABLE_ENCRYPTION
    for (uint16 i = 0; i < length; i++)
        buffer[i] = data[i];
    encryptor->decrypt(buffer, length);
    const uint8 *receiveData = buffer;
#else
    const uint8 *receiveData = data;
#endif

#if ENABLE_CRC16_CCITT
    if (length < 3)
        return FUNCTION_FAIL;
    uint16 crc16 = LittleEndianByteUtils::byteToUnsignedShort(receiveData + length - 2);
    if (crc16 != LibChecksum_GetChecksum16Bit_CCITT(receiveData, length - 2))
        return FUNCTION_FAIL;
    uint16 receiveLength = length - 3;
#else
    uint16 receiveLength = length - 1;
#endif

    uint8 operation = receiveData[0];

    if (operation != currentOp)
        return FUNCTION_FAIL;

    // if (receiveData[1] == 2)
    //     return FUNCTION_FAIL;

    retryTimer->Cancel();
    currentOp = 0xFF;
    return sCallback->onHandleCommand( 0xFF, operation, 0xFF, receiveData + 1, receiveLength);
}

void DevComm2::turnOffEncryption() {
}

void DevComm2::readyForEncryption(const uint8 *data, uint16 length) {
}

void DevComm2::updateEncryption(const uint8 *data, uint16 length) {
}

int DevComm2::setFrameOn(bool on) {
    return FUNCTION_OK;
}

bool DevComm2::isBusy() {
    return currentOp != 0xFF;
}
