/*
 * Module:	Device Communication Protocol
 * Author:	Lvjianfeng
 * Date:	2012.11
 */


#include "devcomm.h"


//Constant definition

#define CHECKSUM_BASE_CRC8					0x00
#define CHECKSUM_BASE_CRC16					0xFFFF

#define PACKET_CONTROL_OFFSET_ENDING		7
#define PACKET_CONTROL_OFFSET_MODE			6
#define PACKET_CONTROL_OFFSET_IDENTITY		0
#define PACKET_IDENTITY_MAX					FLAG_MASK_6_BIT
#define PACKET_IDENTITY_MASK				FLAG_MASK_6_BIT


//Type definition

typedef enum
{
	DEVCOMM_CONTROL_FLAG_VALID = 0,
	DEVCOMM_COUNT_CONTROL_FLAG
} devcomm_control_flag;

typedef enum
{
	DEVCOMM_LINK_FLAG_VALID = 0,
	DEVCOMM_LINK_FLAG_IDENTITY,
	DEVCOMM_LINK_FLAG_ACKNOWLEDGEMENT,
	DEVCOMM_COUNT_LINK_FLAG
} devcomm_link_flag;
	
typedef struct
{
	devcomm_int t_SourceAddress;
	devcomm_int t_TargetAddress;
	devcomm_int t_Length;
	devcomm_int t_Offset;
	uint8 u8_Control;
	uint8 u8_Checksum;
} devcomm_packet_head;
	
typedef struct
{
	devcomm_int t_SourcePort;
	devcomm_int t_TargetPort;
	uint16 u16_Acknowledgement;
	uint16 u16_Checksum;
} devcomm_segment_head;

typedef struct
{
	devcomm_int t_Flag;
	devcomm_int t_Address;
	devcomm_int t_PacketLength;
	devcomm_int t_SendPacketCount;
	devcomm_int t_ReceivePacketCount;
	devcomm_packet_head *tp_SendPacketHead;
	devcomm_packet_head *tp_ReceivePacketHead;
	devcomm_segment_head *tp_SendSegmentHead;
	devcomm_segment_head *tp_ReceiveSegmentHead;
	devcomm_encryption u8_Encryption;
	uint8 u8_SendIdentity;
	uint8 u8_ReceiveIdentity;
	uint8 u8_SendBuffer[DEVCOMM_LINK_BUFFER_SIZE];
	uint8 u8_ReceiveBuffer[DEVCOMM_LINK_BUFFER_SIZE];
	uint16 u16_RetryCounter;
	uint16 u16_TimeoutTimer;
	uint16 u16_Acknowledgement[DEVCOMM_RETRY_MAX + 1];
} devcomm_link;

typedef struct
{
	devcomm_int t_Flag;
	devcomm_int t_Error;
	devcomm_profile t_Profile;
	devcomm_callback t_Callback;
	devcomm_link t_Link[DEVCOMM_LINK_COUNT_MAX];
	uint8 u8_PacketBuffer[DEVCOMM_PACKET_LENGTH_MAX];
} devcomm_control;


//Private variable definition

static devcomm_control m_t_Control[DEVCOMM_DEVICE_COUNT_MAX] = {{0}};


//Private function declaration

static uint DevComm_CheckDevice
(
	devcomm_int t_Device
);
static uint DevComm_CheckPayloadLength
(
	devcomm_link *tp_Link,
	devcomm_int t_Length
);
static uint DevComm_SendPacket
(
	devcomm_int t_Device,
	devcomm_link *tp_Link
);
static uint DevComm_ReceivePacket
(
	devcomm_int t_Device,
	const uint8 *u8p_Data,
	devcomm_int t_Length
);
static uint DevComm_ParsePacket
(
	devcomm_int t_Device,
	devcomm_control *tp_Control,
	devcomm_link *tp_Link
);
static uint DevComm_DiscardPacket
(
	devcomm_int t_Device,
	devcomm_control *tp_Control,
	devcomm_int t_Length
);
static uint DevComm_ForwardPacket
(
	devcomm_int t_Device,
	devcomm_control *tp_Control,
	devcomm_int t_Length
);
static void DevComm_ReloadSendBuffer
(
	devcomm_link *tp_Link,
	devcomm_callback *tp_Callback,
	const uint8 *u8p_Data,
	devcomm_int t_Length
);
static void DevComm_UpdateAcknowledgement
(
	devcomm_link *tp_Link,
	devcomm_callback *tp_Callback,
	uint16 u16_Checksum
);
static void DevComm_UpdateTimeout
(
	devcomm_int t_Device,
	devcomm_control *tp_Control,
	devcomm_link *tp_Link,
	uint16 u16_TickTime
);
static void DevComm_Retry
(
	devcomm_int t_Device,
	devcomm_control *tp_Control,
	devcomm_link *tp_Link
);


//Public function definition

uint DevComm_Initialize( devcomm_int t_Device,
                         const devcomm_profile *tp_Profile,
                         const devcomm_callback *tp_Callback )
{
	//Check if device is valid or not
	if (t_Device >= DEVCOMM_DEVICE_COUNT_MAX)
		return FUNCTION_FAIL;

	devcomm_control *tp_Control = &m_t_Control[t_Device];

	//Check if control has already been initialized
	if (FLAG_GET_BIT(tp_Control->t_Flag, DEVCOMM_CONTROL_FLAG_VALID) != 0)
		return FUNCTION_FAIL;

	//Check if maximum packet length is valid or not
	if (tp_Profile->t_PacketLengthMax < sizeof(devcomm_packet_head) + sizeof(devcomm_segment_head))
		return FUNCTION_FAIL;

	//Check if maximum retry is valid or not
	if (tp_Profile->u16_Retry > DEVCOMM_RETRY_MAX)
		return FUNCTION_FAIL;

	tp_Callback->fp_Memcpy( (uint8 *)&tp_Control->t_Profile,
	                        (const uint8 *)tp_Profile,
	                        sizeof(devcomm_profile) );
	tp_Callback->fp_Memcpy( (uint8 *)&tp_Control->t_Callback,
	                        (const uint8 *)tp_Callback,
	                        sizeof(devcomm_callback) );

	FLAG_SET_BIT(tp_Control->t_Flag, DEVCOMM_CONTROL_FLAG_VALID);

	return FUNCTION_OK;
}


void DevComm_Tick( devcomm_int t_Device,
                   uint16 u16_TickTime )
{
	//Check if device is valid or not
	if (t_Device >= DEVCOMM_DEVICE_COUNT_MAX)
		return;

    devcomm_control *tp_Control = &m_t_Control[t_Device];
    devcomm_link *tp_Link = tp_Control->t_Link;

	//Search for valid links that need acknowledgement
    uint i;
	for (i = 0; i < DEVCOMM_LINK_COUNT_MAX; i++)
	{
		DevComm_UpdateTimeout(t_Device, tp_Control, tp_Link, u16_TickTime);
		tp_Link++;
	}
}


uint DevComm_Link( devcomm_int t_Device,
                   devcomm_int t_Address,
                   devcomm_int t_PacketLength )
{
    if (DevComm_CheckDevice(t_Device) != FUNCTION_OK)
        return FUNCTION_FAIL;

	if ( t_PacketLength > m_t_Control[t_Device].t_Profile.t_PacketLengthMax         ||
		 t_PacketLength < sizeof(devcomm_packet_head) + sizeof(devcomm_segment_head)  )
		return FUNCTION_FAIL;

    devcomm_int i;
    devcomm_link *tp_Link = m_t_Control[t_Device].t_Link;

	//Search for invalid link or valid link with the same address
	for (i = 0; i < DEVCOMM_LINK_COUNT_MAX; i++)
	{
		if ( ( (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_VALID) != 0) && (tp_Link->t_Address == t_Address) ) ||
			   (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_VALID) == 0)                                            )
			break;
		tp_Link++;
	}
	if (i == DEVCOMM_LINK_COUNT_MAX)
		return FUNCTION_FAIL;

	//Initialize link if it's invalid link
	if (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_VALID) == 0)
	{
		FLAG_SET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_VALID);
		tp_Link->t_Address = t_Address;
		tp_Link->t_SendPacketCount = 0;
		tp_Link->t_ReceivePacketCount = 0;
		tp_Link->tp_SendPacketHead = 0;
		tp_Link->tp_ReceivePacketHead = 0;
		tp_Link->tp_SendSegmentHead = 0;
		tp_Link->tp_ReceiveSegmentHead = 0;
		tp_Link->u8_SendIdentity = 0;
		tp_Link->u8_ReceiveIdentity = 0;
		tp_Link->u16_RetryCounter = 0;
		tp_Link->u16_TimeoutTimer = 0;
		tp_Link->u8_Encryption = DEVCOMM_ENCRYPTION_OFF;
	}
	tp_Link->t_PacketLength = t_PacketLength;

	return FUNCTION_OK;
}


uint DevComm_Unlink( devcomm_int t_Device,
                     devcomm_int t_Address )
{
	if (DevComm_CheckDevice(t_Device) != FUNCTION_OK)
		return FUNCTION_FAIL;

	devcomm_int i;
	devcomm_link *tp_Link = m_t_Control[t_Device].t_Link;

	//Search for valid link with the same address
	for (i = 0; i < DEVCOMM_LINK_COUNT_MAX; i++)
	{
		if ( (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_VALID) != 0) && (tp_Link->t_Address == t_Address) )
			break;
		tp_Link++;
	}
	if (i == DEVCOMM_LINK_COUNT_MAX)
		return FUNCTION_FAIL;

	tp_Link->t_Flag = 0;

	return FUNCTION_OK;
}


uint DevComm_SwitchEncryption( devcomm_int t_Device,
                               devcomm_int t_Address,
                               devcomm_encryption u8_Encryption )
{
    if (DevComm_CheckDevice(t_Device) != FUNCTION_OK)
        return FUNCTION_FAIL;

    devcomm_int i;
    devcomm_link *tp_Link = m_t_Control[t_Device].t_Link;

    //Search for valid link with the same address
    for (i = 0; i < DEVCOMM_LINK_COUNT_MAX; i++)
    {
        if ( (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_VALID) != 0) && (tp_Link->t_Address == t_Address) )
            break;
        tp_Link++;
    }
    if (i == DEVCOMM_LINK_COUNT_MAX)
        return FUNCTION_FAIL;

    tp_Link->u8_Encryption = u8_Encryption;
    return FUNCTION_OK;
}


uint DevComm_Send( devcomm_int t_Device,
                   devcomm_int t_Address,
                   devcomm_int t_SourcePort,
                   devcomm_int t_TargetPort,
                   uint8 *u8p_Data,
                   devcomm_int t_Length,
                   devcomm_int t_Mode )
{
	if (DevComm_CheckDevice(t_Device) != FUNCTION_OK)
		return FUNCTION_FAIL;

	devcomm_control *tp_Control = &m_t_Control[t_Device];
	devcomm_link *tp_Link = tp_Control->t_Link;
	devcomm_callback *tp_Callback = &tp_Control->t_Callback;

	//Search for valid link
	devcomm_int i;
	for (i = 0; i < DEVCOMM_LINK_COUNT_MAX; i++)
	{
		if ( (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_VALID) != 0) && (tp_Link->t_Address == t_Address) )
			break;
		tp_Link++;
	}
	if (i == DEVCOMM_LINK_COUNT_MAX)
		return FUNCTION_FAIL;

	//Check if the last sending is finished or not
	if (tp_Link->t_SendPacketCount > 0)
		return FUNCTION_FAIL;

	//Return if the link is not acknowledged
	if (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_ACKNOWLEDGEMENT) != 0)
		return FUNCTION_FAIL;

	//Check if length of payload for sending is valid or not
	if (DevComm_CheckPayloadLength(tp_Link, t_Length) != FUNCTION_OK)
		return FUNCTION_FAIL;

	if (t_Mode == DEVCOMM_MODE_ACKNOWLEDGEMENT)
		FLAG_SET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_ACKNOWLEDGEMENT);
	else
		FLAG_CLEAR_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_ACKNOWLEDGEMENT);

	tp_Link->u16_TimeoutTimer = 0;
	tp_Link->u16_RetryCounter = 0;
	tp_Link->tp_SendPacketHead = (devcomm_packet_head *)tp_Link->u8_SendBuffer;
	tp_Link->tp_SendSegmentHead = (devcomm_segment_head *)(tp_Link->u8_SendBuffer + sizeof(devcomm_packet_head));
	tp_Link->tp_SendPacketHead->t_SourceAddress = tp_Control->t_Profile.t_Address;
	tp_Link->tp_SendPacketHead->t_TargetAddress = t_Address;
	tp_Link->tp_SendSegmentHead->t_SourcePort = t_SourcePort;
	tp_Link->tp_SendSegmentHead->t_TargetPort = t_TargetPort;

	if (tp_Link->u8_Encryption == DEVCOMM_ENCRYPTION_ON || tp_Link->u8_Encryption == DEVCOMM_ENCRYPTION_UPDATE)
	{
	    if (tp_Callback->fp_Encrypt != 0)
	        tp_Callback->fp_Encrypt(u8p_Data, t_Length);
	}

	DevComm_ReloadSendBuffer( tp_Link,
	                          tp_Callback,
	                          u8p_Data,
	                          t_Length );
	DevComm_SendPacket(t_Device, tp_Link);

	return FUNCTION_OK;
}


uint DevComm_Receive( devcomm_int t_Device,
                      devcomm_int t_Address,
                      devcomm_int *tp_SourcePort,
                      devcomm_int *tp_TargetPort,
                      uint8 *u8p_Data,
                      devcomm_int *tp_Length,
                      devcomm_int *tp_Mode )
{
    //Check if device is valid or not
    if (DevComm_CheckDevice(t_Device) != FUNCTION_OK)
        return FUNCTION_FAIL;

	devcomm_control *tp_Control = &m_t_Control[t_Device];;
	devcomm_link *tp_Link = tp_Control->t_Link;;
	devcomm_callback *tp_Callback = &tp_Control->t_Callback;

	uint8 *u8p_Packet = u8p_Data;
	uint8 *u8p_Buffer;
	devcomm_int t_DataLength;
	devcomm_int t_PayloadLength;
	devcomm_packet_head *tp_PacketHead;

	//Search valid link for receiving
	devcomm_int i;
	for (i = 0; i < DEVCOMM_LINK_COUNT_MAX; i++)
	{
		if ( (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_VALID) != 0) && (tp_Link->t_Address == t_Address) )
			break;
		tp_Link++;
	}
	if (i >= DEVCOMM_LINK_COUNT_MAX)
		return FUNCTION_FAIL;

	//Check if there is any data can be received
	if (tp_Link->t_ReceivePacketCount == 0)
		return FUNCTION_FAIL;

	tp_Link->t_ReceivePacketCount--;

	tp_PacketHead = (devcomm_packet_head *)tp_Link->u8_ReceiveBuffer;
	t_DataLength = tp_PacketHead->t_Length - (sizeof(devcomm_packet_head) + sizeof(devcomm_segment_head));

	if (t_DataLength > *tp_Length)
		t_DataLength = *tp_Length;

	u8p_Buffer = tp_Link->u8_ReceiveBuffer + (sizeof(devcomm_packet_head) + sizeof(devcomm_segment_head));
	tp_Callback->fp_Memcpy(u8p_Packet, u8p_Buffer, t_DataLength);
	u8p_Buffer += tp_PacketHead->t_Length - sizeof(devcomm_segment_head);
	u8p_Packet += t_DataLength;
	t_PayloadLength = t_DataLength;

	if (FLAG_GET_BIT(tp_PacketHead->u8_Control, PACKET_CONTROL_OFFSET_MODE) == 0)
		*tp_Mode = DEVCOMM_MODE_ACKNOWLEDGEMENT;
	else
		*tp_Mode = DEVCOMM_MODE_NO_ACKNOWLEDGEMENT;

	*tp_Length -= t_DataLength;

	while ( (FLAG_GET_BIT(tp_PacketHead->u8_Control, PACKET_CONTROL_OFFSET_ENDING) == 0) && (*tp_Length > 0) )
	{
		tp_PacketHead = (devcomm_packet_head *)((uint8 *)tp_PacketHead + tp_PacketHead->t_Length);
		t_DataLength = tp_PacketHead->t_Length - sizeof(devcomm_packet_head);
		if (t_DataLength > *tp_Length)
			t_DataLength = *tp_Length;

		tp_Callback->fp_Memcpy(u8p_Packet, u8p_Buffer, t_DataLength);
		u8p_Buffer += tp_PacketHead->t_Length;
		u8p_Packet += t_DataLength;
		t_PayloadLength += t_DataLength;
		*tp_Length -= t_DataLength;
	}

	*tp_Length = t_PayloadLength;
	*tp_SourcePort = tp_Link->tp_ReceiveSegmentHead->t_SourcePort;
	*tp_TargetPort = tp_Link->tp_ReceiveSegmentHead->t_TargetPort;

	if (tp_Link->u8_Encryption == DEVCOMM_ENCRYPTION_READY)
	{
	    tp_Link->u8_Encryption = DEVCOMM_ENCRYPTION_ON;
	}
	if (tp_Link->u8_Encryption == DEVCOMM_ENCRYPTION_UPDATE)
    {
        tp_Link->u8_Encryption = DEVCOMM_ENCRYPTION_ON;
        if (tp_Callback->fp_EncryptionUpdate != 0)
            tp_Callback->fp_EncryptionUpdate();
    }
	if (tp_Link->u8_Encryption == DEVCOMM_ENCRYPTION_ON)
    {
        if (tp_Callback->fp_Decrypt != 0)
            tp_Callback->fp_Decrypt(u8p_Data, t_PayloadLength);
    }

	return FUNCTION_OK;
}


uint DevComm_Query( devcomm_int t_Device,
                    devcomm_int t_Address,
                    devcomm_int t_Info,
                    devcomm_int *tp_Value )
{
	if (DevComm_CheckDevice(t_Device) != FUNCTION_OK)
		return FUNCTION_FAIL;

    devcomm_int i;
    devcomm_link *tp_Link;

    switch (t_Info) {
    case DEVCOMM_INFO_STATE:
        *tp_Value = DEVCOMM_STATE_IDLE;
        tp_Link = m_t_Control[t_Device].t_Link;

        for (i = 0; i < DEVCOMM_LINK_COUNT_MAX; i++)
        {
            if ( (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_VALID) != 0) &&
                 (tp_Link->t_Address == t_Address)                              )
            {
                if ( (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_ACKNOWLEDGEMENT) != 0) ||
                     (tp_Link->t_SendPacketCount > 0)                                         )
                {
                    *tp_Value = DEVCOMM_STATE_BUSY;
                    break;
                }
            }
            tp_Link++;
        }
        break;

    case DEVCOMM_INFO_ERROR:
        *tp_Value = m_t_Control[t_Device].t_Error;
        break;

    default:
        return FUNCTION_FAIL;
    }

	return FUNCTION_OK;
}


uint DevComm_WriteDeviceDone( devcomm_int t_Device )
{
	if (DevComm_CheckDevice(t_Device) != FUNCTION_OK)
		return FUNCTION_FAIL;

	devcomm_link *tp_Link  = m_t_Control[t_Device].t_Link;

	//Search for valid link that has packet to send
	devcomm_int i;
	for (i = 0; i < DEVCOMM_LINK_COUNT_MAX; i++)
	{
		if ( (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_VALID) != 0) &&
			 (tp_Link->t_SendPacketCount > 0)                               )
			break;
		tp_Link++;
	}
	if (i >= DEVCOMM_LINK_COUNT_MAX)
		return FUNCTION_OK;

	devcomm_packet_head *tp_PacketHead = tp_Link->tp_SendPacketHead;

	tp_Link->t_SendPacketCount--;
	if (tp_Link->t_SendPacketCount > 0)
	{
		tp_Link->tp_SendPacketHead = (devcomm_packet_head *)((uint8 *)tp_PacketHead + (tp_PacketHead->t_Length));
		DevComm_SendPacket(t_Device, tp_Link);
	}
	else
	{
	    m_t_Control[t_Device].t_Callback.fp_HandleEvent( t_Device,
	                                                     tp_PacketHead->t_TargetAddress,
	                                                     tp_Link->tp_SendSegmentHead->t_TargetPort,
	                                                     tp_Link->tp_SendSegmentHead->t_SourcePort,
	                                                     DEVCOMM_EVENT_SEND_DONE );
	}

	return FUNCTION_OK;
}


uint DevComm_ReadDeviceDone( devcomm_int t_Device,
                             const uint8 *u8p_Data,
                             devcomm_int t_Length )
{
	if (DevComm_CheckDevice(t_Device) != FUNCTION_OK)
		return FUNCTION_FAIL;

	return DevComm_ReceivePacket(t_Device, u8p_Data, t_Length);
}


//Private function definition

static uint DevComm_CheckDevice( devcomm_int t_Device )
{
	//Check if device is valid or not
	if (t_Device >= DEVCOMM_DEVICE_COUNT_MAX)
		return FUNCTION_FAIL;

	//Check if control is valid or not
	if (FLAG_GET_BIT(m_t_Control[t_Device].t_Flag, DEVCOMM_CONTROL_FLAG_VALID) == 0)
		return FUNCTION_FAIL;

	return FUNCTION_OK;
}


static uint DevComm_CheckPayloadLength( devcomm_link *tp_Link,
                                        devcomm_int t_Length )
{
	devcomm_int t_PacketPayload = tp_Link->t_PacketLength - sizeof(devcomm_packet_head);
	if ( (t_Length > (devcomm_int)(~(0)) - (t_PacketPayload + sizeof(devcomm_segment_head) - 1)) )
		return FUNCTION_FAIL;

	//Check if length of payload data is out of range
	devcomm_int t_PacketCount = (sizeof(devcomm_segment_head) + t_Length + t_PacketPayload - 1) / t_PacketPayload;
	if ((t_PacketPayload + sizeof(devcomm_packet_head)) * t_PacketCount > DEVCOMM_LINK_BUFFER_SIZE)
		return FUNCTION_FAIL;

	return FUNCTION_OK;
}


static uint DevComm_SendPacket( devcomm_int t_Device,
                                devcomm_link *tp_Link )
{
	devcomm_packet_head *tp_PacketHead = tp_Link->tp_SendPacketHead;
	m_t_Control[t_Device].t_Callback.fp_WriteDevice( t_Device,
	                                                 (const uint8 *)tp_PacketHead,
	                                                 tp_PacketHead->t_Length );
	return FUNCTION_OK;
}


static uint DevComm_ReceivePacket( devcomm_int t_Device,
                                   const uint8 *u8p_Data,
                                   devcomm_int t_Length )
{
	devcomm_control *tp_Control = &m_t_Control[t_Device];
	devcomm_link *tp_Link = tp_Control->t_Link;;
	devcomm_callback *tp_Callback = &tp_Control->t_Callback;

	uint8 *u8p_Buffer;
	const devcomm_packet_head *tp_PacketHead;
	devcomm_packet_head *tp_ReceivePacketHead;

	if (t_Length < sizeof(devcomm_packet_head))
		return DevComm_DiscardPacket(t_Device, tp_Control, t_Length);

	tp_PacketHead = (const devcomm_packet_head *)u8p_Data;

	//Check if checksum of packet head is correct or not
	if ( tp_PacketHead->u8_Checksum != tp_Callback->fp_GetCRC8( (const uint8 *)tp_PacketHead,
	                                                            sizeof(devcomm_packet_head) - sizeof(tp_PacketHead->u8_Checksum),
	                                                            CHECKSUM_BASE_CRC8 ) )
		return DevComm_DiscardPacket(t_Device, tp_Control, t_Length);

	//Check if packet length is valid or not
	if (tp_PacketHead->t_Length > t_Length)
		return DevComm_DiscardPacket(t_Device, tp_Control, t_Length);

	t_Length = tp_PacketHead->t_Length;

	//If this device should not accept this packet, forward it to other devices
	if (tp_Control->t_Profile.t_Address != tp_PacketHead->t_TargetAddress)
		return DevComm_ForwardPacket(t_Device, tp_Control, t_Length);

	//Search for valid link that should handle this packet
	devcomm_int i;
	for (i = 0; i < DEVCOMM_LINK_COUNT_MAX; i++)
	{
		if ((FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_VALID) != 0) &&
			(tp_Link->t_Address == tp_PacketHead->t_SourceAddress))
			break;
		tp_Link++;
	}
	//Return if no link can handle this packet
	if (i >= DEVCOMM_LINK_COUNT_MAX)
		return DevComm_DiscardPacket(t_Device, tp_Control, t_Length);

	//Check if it's a new packet
	if (tp_PacketHead->t_Offset == 0)
	{
		//Check if length of packet is out of range
		if ( (tp_PacketHead->t_Length > DEVCOMM_LINK_BUFFER_SIZE) ||
			 (tp_PacketHead->t_Length < sizeof(devcomm_packet_head) + sizeof(devcomm_segment_head)) )
			return DevComm_DiscardPacket(t_Device, tp_Control, t_Length);

		if (tp_Link->t_ReceivePacketCount > 0)
			tp_Link->t_ReceivePacketCount--;

		u8p_Buffer = tp_Link->u8_ReceiveBuffer;
		tp_Link->tp_ReceiveSegmentHead = (devcomm_segment_head *)(u8p_Buffer + sizeof(devcomm_packet_head));
	}
	else
	{
		tp_ReceivePacketHead = tp_Link->tp_ReceivePacketHead;

		if (tp_ReceivePacketHead == 0)
			return DevComm_DiscardPacket(t_Device, tp_Control, t_Length);

		//Check if identity of the packet is matched or not
		if (FLAG_READ_FIELD(tp_ReceivePacketHead->u8_Control, PACKET_CONTROL_OFFSET_IDENTITY, PACKET_IDENTITY_MASK) !=
			FLAG_READ_FIELD(tp_PacketHead->u8_Control,        PACKET_CONTROL_OFFSET_IDENTITY, PACKET_IDENTITY_MASK)   )
			return DevComm_DiscardPacket(t_Device, tp_Control, t_Length);

		//Check if length of packet is out of range
		if ( tp_PacketHead->t_Length >
		     (tp_Link->u8_ReceiveBuffer + DEVCOMM_LINK_BUFFER_SIZE) - ((uint8 *)tp_ReceivePacketHead + tp_ReceivePacketHead->t_Length) )
			return DevComm_DiscardPacket(t_Device, tp_Control, t_Length);

		//Check if offset is match or not
		if ( tp_ReceivePacketHead->t_Offset + tp_ReceivePacketHead->t_Length - sizeof(devcomm_packet_head) !=
		     tp_PacketHead->t_Offset                                                                         )
			return DevComm_DiscardPacket(t_Device, tp_Control, t_Length);

		u8p_Buffer = (uint8 *)tp_ReceivePacketHead + tp_ReceivePacketHead->t_Length;
	}

	if (tp_Callback->fp_ReadDevice(t_Device, u8p_Buffer, &t_Length) != FUNCTION_OK)
		return FUNCTION_FAIL;

	tp_Link->tp_ReceivePacketHead = (devcomm_packet_head *)u8p_Buffer;

	//Check if it's the last fragment of the packet
	if (FLAG_GET_BIT(tp_Link->tp_ReceivePacketHead->u8_Control, PACKET_CONTROL_OFFSET_ENDING) != 0)
		return DevComm_ParsePacket(t_Device, tp_Control, tp_Link);

	return FUNCTION_OK;
}


static uint DevComm_ParsePacket( devcomm_int t_Device,
                                 devcomm_control *tp_Control,
                                 devcomm_link *tp_Link )
{
    devcomm_callback *tp_Callback = &tp_Control->t_Callback;
	uint8 *u8p_Buffer;
	uint16 u16_Checksum;
	uint16 u16_RetryCounter;
	devcomm_int t_PayloadLength;

	devcomm_packet_head *tp_PacketHead;
	devcomm_segment_head *tp_SegmentHead;

	tp_PacketHead = (devcomm_packet_head *)tp_Link->u8_ReceiveBuffer;
	tp_SegmentHead = tp_Link->tp_ReceiveSegmentHead;
	u8p_Buffer = tp_Link->u8_ReceiveBuffer + sizeof(devcomm_packet_head);

	u16_Checksum = tp_Callback->fp_GetCRC16( u8p_Buffer,
	                                         sizeof(devcomm_segment_head) - sizeof(tp_SegmentHead->u16_Checksum),
	                                         CHECKSUM_BASE_CRC16 );
	u8p_Buffer += sizeof(devcomm_segment_head);
	u16_Checksum = tp_Callback->fp_GetCRC16( u8p_Buffer,
	                                         tp_PacketHead->t_Length - (sizeof(devcomm_packet_head) + sizeof(devcomm_segment_head)),
	                                         u16_Checksum );
	u8p_Buffer += tp_PacketHead->t_Length - sizeof(devcomm_segment_head);
	t_PayloadLength = tp_PacketHead->t_Length - (sizeof(devcomm_packet_head) + sizeof(devcomm_segment_head));

	while (FLAG_GET_BIT(tp_PacketHead->u8_Control, PACKET_CONTROL_OFFSET_ENDING) == 0)
	{
		tp_PacketHead = (devcomm_packet_head *)((uint8 *)tp_PacketHead + tp_PacketHead->t_Length);
		u16_Checksum = tp_Callback->fp_GetCRC16( u8p_Buffer,
		                                         tp_PacketHead->t_Length - sizeof(devcomm_packet_head),
		                                         u16_Checksum );
		u8p_Buffer += tp_PacketHead->t_Length;
		t_PayloadLength += tp_PacketHead->t_Length - sizeof(devcomm_packet_head);
	}

	//Check if checksum of segment is matched or not
	if (tp_SegmentHead->u16_Checksum != u16_Checksum)
		return FUNCTION_FAIL;

	if (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_ACKNOWLEDGEMENT) != 0)
	{
		for (u16_RetryCounter = 0; u16_RetryCounter <= tp_Link->u16_RetryCounter; u16_RetryCounter++)
		{
			//Check if the packet being sent is acknowledged or not
			if (tp_SegmentHead->u16_Acknowledgement == tp_Link->u16_Acknowledgement[u16_RetryCounter])
			{
				FLAG_CLEAR_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_ACKNOWLEDGEMENT);
				tp_Callback->fp_HandleEvent( t_Device,
				                             tp_PacketHead->t_SourceAddress,
				                             tp_Link->tp_SendSegmentHead->t_TargetPort,
				                             tp_Link->tp_SendSegmentHead->t_SourcePort,
				                             DEVCOMM_EVENT_ACKNOWLEDGE );
				break;
			}
		}
	}

	if (t_PayloadLength > 0)
	{
		//Check if it's duplicate packet
		if ( (FLAG_READ_FIELD(tp_PacketHead->u8_Control, PACKET_CONTROL_OFFSET_IDENTITY, PACKET_IDENTITY_MASK) != tp_Link->u8_ReceiveIdentity) ||
			 (FLAG_READ_FIELD(tp_PacketHead->u8_Control, PACKET_CONTROL_OFFSET_IDENTITY, PACKET_IDENTITY_MASK) == 0)                             )
		{
			tp_Link->u8_ReceiveIdentity = FLAG_READ_FIELD(tp_PacketHead->u8_Control, PACKET_CONTROL_OFFSET_IDENTITY, PACKET_IDENTITY_MASK);
			
			if (tp_Link->u8_ReceiveIdentity == 0)
				tp_Link->u8_ReceiveIdentity = (uint8)PACKET_IDENTITY_MAX;

			tp_Link->t_ReceivePacketCount++;
			tp_Callback->fp_HandleEvent( t_Device,
			                             tp_PacketHead->t_SourceAddress,
			                             tp_SegmentHead->t_SourcePort,
			                             tp_SegmentHead->t_TargetPort,
			                             DEVCOMM_EVENT_RECEIVE_DONE );
		}
	}

	if (FLAG_GET_BIT(tp_PacketHead->u8_Control, PACKET_CONTROL_OFFSET_MODE) == 0)
	    DevComm_UpdateAcknowledgement(tp_Link, &tp_Control->t_Callback, u16_Checksum);

	return FUNCTION_OK;
}


static uint DevComm_DiscardPacket( devcomm_int t_Device,
                                   devcomm_control *tp_Control,
                                   devcomm_int t_Length )
{
	if (tp_Control->t_Error < (devcomm_int)(~0))
		tp_Control->t_Error++;

	devcomm_int t_PacketLength;
	if (t_Length > DEVCOMM_PACKET_LENGTH_MAX)
		t_PacketLength = DEVCOMM_PACKET_LENGTH_MAX;
	else
		t_PacketLength = t_Length;

	return tp_Control->t_Callback.fp_ReadDevice( t_Device,
	                                             tp_Control->u8_PacketBuffer,
	                                             &t_PacketLength );
}


static uint DevComm_ForwardPacket( devcomm_int t_Device,
                                   devcomm_control *tp_Control,
                                   devcomm_int t_Length )
{
	devcomm_int t_PacketLength;
	if (t_Length > DEVCOMM_PACKET_LENGTH_MAX)
		t_PacketLength = DEVCOMM_PACKET_LENGTH_MAX;
	else
		t_PacketLength = t_Length;

	if ( tp_Control->t_Callback.fp_ReadDevice( t_Device,
	                                           tp_Control->u8_PacketBuffer,
	                                           &t_PacketLength ) != FUNCTION_OK )
		return FUNCTION_FAIL;


	//Forward packet to all other devices except for the current device
	devcomm_int i;
	devcomm_control *tp_ForwardControl = m_t_Control;
	for (i = 0; i < DEVCOMM_DEVICE_COUNT_MAX; i++)
	{
		if ((i != t_Device) && (t_PacketLength <= tp_ForwardControl->t_Profile.t_PacketLengthMax))
		{
			tp_ForwardControl->t_Callback.fp_WriteDevice(i,
			                                             tp_Control->u8_PacketBuffer,
			                                             t_PacketLength);
		}
		tp_ForwardControl++;
	}

	return FUNCTION_OK;
}


static void DevComm_ReloadSendBuffer( devcomm_link *tp_Link,
                                      devcomm_callback *tp_Callback,
                                      const uint8 *u8p_Data,
                                      devcomm_int t_Length )
{
	uint8 *u8p_Buffer;
	devcomm_int t_DataLength;
	devcomm_int t_PacketPayload;
	devcomm_packet_head *tp_PacketHead;
	devcomm_segment_head *tp_SegmentHead;


	tp_PacketHead = tp_Link->tp_SendPacketHead;
	tp_SegmentHead = tp_Link->tp_SendSegmentHead;

	if (tp_Link->tp_ReceiveSegmentHead != 0)
		tp_SegmentHead->u16_Acknowledgement = tp_Link->tp_ReceiveSegmentHead->u16_Checksum;
	else
		tp_SegmentHead->u16_Acknowledgement = 0;

	tp_SegmentHead->u16_Checksum = tp_Callback->fp_GetCRC16( (const uint8 *)tp_SegmentHead,
	                                                         sizeof(devcomm_segment_head) - sizeof(tp_SegmentHead->u16_Checksum),
	                                                         CHECKSUM_BASE_CRC16 );
	tp_SegmentHead->u16_Checksum = tp_Callback->fp_GetCRC16( u8p_Data,
	                                                         t_Length,
	                                                         tp_SegmentHead->u16_Checksum );
	tp_PacketHead->u8_Control = FLAG_READ_FIELD(tp_PacketHead->u8_Control, PACKET_CONTROL_OFFSET_IDENTITY, PACKET_IDENTITY_MASK);

	if (tp_PacketHead->u8_Control < (uint8)PACKET_IDENTITY_MAX)
	{
		if (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_IDENTITY) != 0)
		{
			tp_PacketHead->u8_Control++;
		}
		else
		{
			tp_PacketHead->u8_Control = 0;
			FLAG_SET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_IDENTITY);
		}
	}
	else
	{
		tp_PacketHead->u8_Control = 1;
	}

	if (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_ACKNOWLEDGEMENT) != 0)
	{
	    uint16 u16_RetryCounter;
	    for (u16_RetryCounter = 0; u16_RetryCounter <= DEVCOMM_RETRY_MAX; u16_RetryCounter++)
	        tp_Link->u16_Acknowledgement[u16_RetryCounter] = tp_SegmentHead->u16_Checksum;
	}
	else
	{
		FLAG_SET_BIT(tp_PacketHead->u8_Control, PACKET_CONTROL_OFFSET_MODE);
	}

	tp_PacketHead->t_Offset = 0;
	t_PacketPayload = tp_Link->t_PacketLength - sizeof(devcomm_packet_head);

	if (t_Length + sizeof(devcomm_segment_head) > t_PacketPayload)
		t_DataLength = t_PacketPayload - sizeof(devcomm_segment_head);
	else
		t_DataLength = t_Length;

	tp_PacketHead->t_Length = sizeof(devcomm_packet_head) + sizeof(devcomm_segment_head) + t_DataLength;
	u8p_Buffer = (uint8 *)tp_SegmentHead + sizeof(devcomm_segment_head);
	tp_Callback->fp_Memcpy(u8p_Buffer, u8p_Data, t_DataLength);
	u8p_Buffer += t_DataLength;
	u8p_Data += t_DataLength;
	t_Length -= t_DataLength;
	t_DataLength += sizeof(devcomm_segment_head);
	tp_Link->t_SendPacketCount++;

	while (t_Length > 0)
	{
	    tp_PacketHead->u8_Checksum = tp_Callback->fp_GetCRC8( (const uint8 *)tp_PacketHead,
	                                                          sizeof(devcomm_packet_head) - sizeof(tp_PacketHead->u8_Checksum),
	                                                          CHECKSUM_BASE_CRC8 );
	    tp_Callback->fp_Memcpy( u8p_Buffer,
	                            (const uint8 *)tp_PacketHead,
	                            sizeof(devcomm_packet_head) );
		tp_PacketHead = (devcomm_packet_head *)u8p_Buffer;
		tp_PacketHead->t_Offset += t_DataLength;

		if (t_Length > t_PacketPayload)
			t_DataLength = t_PacketPayload;
		else
			t_DataLength = t_Length;

		tp_PacketHead->t_Length = sizeof(devcomm_packet_head) + t_DataLength;
		u8p_Buffer += sizeof(devcomm_packet_head);
		tp_Callback->fp_Memcpy(u8p_Buffer, u8p_Data, t_DataLength);
		u8p_Buffer += t_DataLength;
		u8p_Data += t_DataLength;
		t_Length -= t_DataLength;
		tp_Link->t_SendPacketCount++;
	}

	FLAG_SET_BIT(tp_PacketHead->u8_Control, PACKET_CONTROL_OFFSET_ENDING);
	tp_PacketHead->u8_Checksum = tp_Callback->fp_GetCRC8( (const uint8 *)tp_PacketHead,
	                                                      sizeof(devcomm_packet_head) - sizeof(tp_PacketHead->u8_Checksum),
	                                                      CHECKSUM_BASE_CRC8 );
}


static void DevComm_UpdateAcknowledgement( devcomm_link *tp_Link,
                                           devcomm_callback *tp_Callback,
                                           uint16 u16_Checksum )
{
	uint8 *u8p_Buffer;
	devcomm_packet_head *tp_PacketHead;
	devcomm_segment_head *tp_SegmentHead;


	tp_SegmentHead = tp_Link->tp_SendSegmentHead;

	if (tp_SegmentHead == 0)
		return;

	//Check if checksum of the segment needs to be recalculated or not
	if (tp_SegmentHead->u16_Acknowledgement != u16_Checksum)
	{
		tp_SegmentHead->u16_Acknowledgement = u16_Checksum;
		tp_PacketHead = (devcomm_packet_head *)tp_Link->u8_SendBuffer;
		u8p_Buffer = tp_Link->u8_SendBuffer + sizeof(devcomm_packet_head);
		u16_Checksum = tp_Callback->fp_GetCRC16(u8p_Buffer,
		                                        sizeof(devcomm_segment_head) - sizeof(tp_SegmentHead->u16_Checksum),
		                                        CHECKSUM_BASE_CRC16);
		u8p_Buffer += sizeof(devcomm_segment_head);
		u16_Checksum = tp_Callback->fp_GetCRC16(u8p_Buffer,
		                                        tp_PacketHead->t_Length - (sizeof(devcomm_packet_head) + sizeof(devcomm_segment_head)),
		                                        u16_Checksum);
		u8p_Buffer += tp_PacketHead->t_Length - sizeof(devcomm_segment_head);

		while (FLAG_GET_BIT(tp_PacketHead->u8_Control, 
			PACKET_CONTROL_OFFSET_ENDING) == 0)
		{
			tp_PacketHead = (devcomm_packet_head *)((uint8 *)tp_PacketHead + tp_PacketHead->t_Length);
			u16_Checksum = tp_Callback->fp_GetCRC16( u8p_Buffer,
			                                         tp_PacketHead->t_Length - sizeof(devcomm_packet_head),
			                                         u16_Checksum );
			u8p_Buffer += tp_PacketHead->t_Length;
		}

		tp_SegmentHead->u16_Checksum = u16_Checksum;
	}

	tp_Link->u16_Acknowledgement[tp_Link->u16_RetryCounter] = tp_SegmentHead->u16_Checksum;
}


static void DevComm_UpdateTimeout( devcomm_int t_Device,
                                   devcomm_control *tp_Control,
                                   devcomm_link *tp_Link,
                                   uint16 u16_TickTime )
{
	if ( (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_VALID) == 0)           ||
		 (FLAG_GET_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_ACKNOWLEDGEMENT) == 0)   )
		return;

	tp_Link->u16_TimeoutTimer += u16_TickTime;

	//Check if sending timeout is occurred
	if (tp_Link->u16_TimeoutTimer >= tp_Control->t_Profile.u16_Timeout)
	{
		tp_Link->u16_RetryCounter++;
		//Check if it has reached maximum retry times
		if (tp_Link->u16_RetryCounter > tp_Control->t_Profile.u16_Retry)
		{
		    FLAG_CLEAR_BIT(tp_Link->t_Flag, DEVCOMM_LINK_FLAG_ACKNOWLEDGEMENT);
		    tp_Control->t_Callback.fp_HandleEvent( t_Device,
		                                           tp_Link->tp_SendPacketHead->t_TargetAddress,
		                                           tp_Link->tp_SendSegmentHead->t_TargetPort,
		                                           tp_Link->tp_SendSegmentHead->t_SourcePort,
		                                           DEVCOMM_EVENT_TIMEOUT );
		}
		else
		{
			tp_Link->u16_TimeoutTimer = 0;
			DevComm_Retry(t_Device, tp_Control, tp_Link);
		}
	}
}


static void DevComm_Retry( devcomm_int t_Device,
                           devcomm_control *tp_Control,
                           devcomm_link *tp_Link )
{
	devcomm_packet_head *tp_PacketHead;

	if (tp_Link->t_SendPacketCount > 0)
		return;

	tp_Link->t_SendPacketCount++;
	tp_PacketHead = (devcomm_packet_head *)tp_Link->u8_SendBuffer;
	tp_Link->tp_SendPacketHead = tp_PacketHead;

	while (FLAG_GET_BIT(tp_PacketHead->u8_Control, PACKET_CONTROL_OFFSET_ENDING) == 0)
	{
		tp_Link->t_SendPacketCount++;
		tp_PacketHead = (devcomm_packet_head *)((uint8 *)tp_PacketHead + tp_PacketHead->t_Length);
	}

	DevComm_SendPacket(t_Device, tp_Link);
}
