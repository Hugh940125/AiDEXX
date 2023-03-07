/*
 * Module:	Device Communication Protocol
 * Author:	Lvjianfeng
 * Date:	2012.11
 */

#ifndef _DEVCOMM_H_
#define _DEVCOMM_H_

#ifdef __cplusplus
extern "C"
{
#endif

#include "global.h"


//Constant definition

#ifndef DEVCOMM_DEVICE_COUNT_MAX
#define DEVCOMM_DEVICE_COUNT_MAX		1
#endif

#ifndef DEVCOMM_LINK_COUNT_MAX
#define DEVCOMM_LINK_COUNT_MAX			1
#endif

#ifndef DEVCOMM_LINK_BUFFER_SIZE
#define DEVCOMM_LINK_BUFFER_SIZE		254
#endif

#ifndef DEVCOMM_PACKET_LENGTH_MAX
#define DEVCOMM_PACKET_LENGTH_MAX		16
#endif

#ifndef DEVCOMM_RETRY_MAX
#define DEVCOMM_RETRY_MAX               4
#endif


//Type definition

#ifndef DEVCOMM_INT
#define DEVCOMM_INT 					uint8
#endif
typedef DEVCOMM_INT devcomm_int;

typedef uint (*devcomm_callback_handle_event)
(
	devcomm_int t_Device,
	devcomm_int t_Address,
	devcomm_int t_SourcePort,
	devcomm_int t_TargetPort,
	devcomm_int t_Event
);

typedef uint (*devcomm_callback_write_device)
(
	devcomm_int t_Device,
	const uint8 *u8p_Data,
	devcomm_int t_Length
);

typedef uint (*devcomm_callback_read_device)
(
	devcomm_int t_Device,
	uint8 *u8p_Data,
	devcomm_int *tp_Length
);

typedef void (*devcomm_callback_memcpy)
(
	uint8 *u8p_Target,
	const uint8 *u8p_Source,
	devcomm_int t_Length
);

typedef uint8 (*devcomm_callback_get_crc8)
(
	const uint8 *u8p_Data,
	devcomm_int t_Length,
	uint8 u8_Base
);

typedef uint16 (*devcomm_callback_get_crc16)
(
	const uint8 *u8p_Data,
	devcomm_int t_Length,
	uint16 u16_Base
);

typedef void (*devcomm_callback_encryption_update)(void);
typedef void (*devcomm_callback_encrypt)
(
    uint8 *u8p_Data,
    devcomm_int t_Length
);

typedef void (*devcomm_callback_decrypt)
(
    uint8 *u8p_Data,
    devcomm_int t_Length
);

typedef void (*devcomm_callback_enter_critical)(void);

typedef void (*devcomm_callback_exit_critical)(void);

typedef enum
{
	DEVCOMM_INFO_STATE = 0,
	DEVCOMM_INFO_ERROR,
	DEVCOMM_COUNT_INFO
} devcomm_info;

typedef enum
{
	DEVCOMM_STATE_IDLE = 0,
	DEVCOMM_STATE_BUSY,
	DEVCOMM_COUNT_STATE
} devcomm_state;

typedef enum
{
	DEVCOMM_MODE_ACKNOWLEDGEMENT = 0,
	DEVCOMM_MODE_NO_ACKNOWLEDGEMENT,
	DEVCOMM_COUNT_MODE
} devcomm_mode;

typedef enum
{
	DEVCOMM_EVENT_SEND_DONE = 0,
	DEVCOMM_EVENT_ACKNOWLEDGE,
	DEVCOMM_EVENT_TIMEOUT,
	DEVCOMM_EVENT_RECEIVE_DONE,
	DEVCOMM_COUNT_EVENT
} devcomm_event;

typedef enum
{
    DEVCOMM_ENCRYPTION_OFF = 0,
    DEVCOMM_ENCRYPTION_ON,
    DEVCOMM_ENCRYPTION_READY,
    DEVCOMM_ENCRYPTION_UPDATE,
    DEVCOMM_COUNT_ENCRYPTION
} devcomm_encryption;

typedef struct
{
	devcomm_int t_Address;
	devcomm_int t_PacketLengthMax;
	uint16 u16_Retry;
	uint16 u16_Timeout;
} devcomm_profile;

typedef struct
{
	devcomm_callback_handle_event fp_HandleEvent;
	devcomm_callback_write_device fp_WriteDevice;
	devcomm_callback_read_device fp_ReadDevice;
	devcomm_callback_memcpy fp_Memcpy;
	devcomm_callback_get_crc8 fp_GetCRC8;
	devcomm_callback_get_crc16 fp_GetCRC16;
	devcomm_callback_encryption_update fp_EncryptionUpdate;
	devcomm_callback_encrypt fp_Encrypt;
	devcomm_callback_decrypt fp_Decrypt;
	devcomm_callback_enter_critical fp_EnterCritical;
	devcomm_callback_exit_critical fp_ExitCritical;
} devcomm_callback;


//Function declaration

uint DevComm_Initialize
(
	devcomm_int t_Device,
	const devcomm_profile *tp_Profile,
	const devcomm_callback *tp_Callback
);
void DevComm_Tick
(
	devcomm_int t_Device,
	uint16 u16_TickTime
);
uint DevComm_Link
(
	devcomm_int t_Device,
	devcomm_int t_Address,
	devcomm_int t_PacketLength
);
uint DevComm_Unlink
(
	devcomm_int t_Device,
	devcomm_int t_Address
);
uint DevComm_SwitchEncryption
(
    devcomm_int t_Device,
    devcomm_int t_Address,
    devcomm_encryption u8_Encryption
);
uint DevComm_Send
(
	devcomm_int t_Device,
	devcomm_int t_Address,
	devcomm_int t_SourcePort,
	devcomm_int t_TargetPort,
	uint8 *u8p_Data,
	devcomm_int t_Length,
	devcomm_int t_Mode
);
uint DevComm_Receive
(
	devcomm_int t_Device,
	devcomm_int t_Address,
	devcomm_int *tp_SourcePort,
	devcomm_int *tp_TargetPort,
	uint8 *u8p_Data,
	devcomm_int *tp_Length,
	devcomm_int *tp_Mode
);
uint DevComm_Query
(
	devcomm_int t_Device,
	devcomm_int t_Address,
	devcomm_int t_Info,
	devcomm_int *tp_Value
);
uint DevComm_WriteDeviceDone
(
	devcomm_int t_Device
);
uint DevComm_ReadDeviceDone
(
	devcomm_int t_Device,
	const uint8 *u8p_Data,
	devcomm_int t_Length
);

#ifdef __cplusplus
}
#endif

#endif
