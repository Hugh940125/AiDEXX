/*
 * Module:	Communication manager task
 * Author:	Lvjianfeng
 * Date:	2011.9
 */

#include <pthread.h>

#ifdef __linux__
#include <sys/select.h>
#elif __WIN32
#include <windows.h>
#elif __APPLE__
#include <sys/select.h>
#include <sys/time.h>
#endif

#include "driver/drv.h"
#include "devcomm.h"
#include "lib_checksum.h"
#include "lib_queue.h"

#include "task_comm.h"
#include "aes.h"

//Constant definition

#define TASK_COMM_BUFFER_SIZE DEVCOMM_LINK_BUFFER_SIZE
#define TASK_COMM_PACKET_LENGTH_MAX 128
#define TASK_COMM_PACKET_LENGTH_MASTER 20
#define TASK_COMM_PACKET_LENGTH_SLAVE 32
#define TASK_COMM_SEND_RETRY 3
#define TASK_COMM_SEND_TIMEOUT 800

#define TASK_COMM_THREAD_CREATE_DELAY 100
#define TASK_COMM_WRITE_DELAY 20
// #define TASK_COMM_TRHEAD_LOCK

//Type definition
typedef enum
{
	TASK_COMM_DEVICE_SERIAL = 0,
	TASK_COMM_COUNT_DEVICE
} task_comm_device;

typedef enum
{
	TASK_COMM_STATE_INITIAL = 0,
	TASK_COMM_STATE_SENDING,
	TASK_COMM_COUNT_STATE
} task_comm_state;

typedef enum
{
	TASK_COMM_COMMAND_OFFSET_OPERATION = 0,
	TASK_COMM_COMMAND_OFFSET_PARAMETER,
	TASK_COMM_COUNT_COMMAND_OFFSET
} task_comm_command_offset;

typedef struct
{
	uint8 u8_SourcePort;
	uint8 u8_TargetPort;
	uint8 u8_Mode;
	uint8 u8_Operation;
	uint8 u8_Parameter;
	uint8 u8_DataLength;
} task_comm_message_head;

//Private variable definition

static uint m_ui_State[DRV_UART_DEVICE_ID_COUNT] = {0};
static uint m_ui_Address[DRV_UART_DEVICE_ID_COUNT] = {0};
static uint8 m_u8_BufferQueue[DRV_UART_DEVICE_ID_COUNT][TASK_COMM_BUFFER_SIZE] = {{0}};
static uint8 m_u8_BufferCommand[DRV_UART_DEVICE_ID_COUNT][TASK_COMM_BUFFER_SIZE] = {{0}};
static lib_queue_object m_t_QueueSend[DRV_UART_DEVICE_ID_COUNT] = {{0}};
static pthread_mutex_t m_t_MutexCond[DRV_UART_DEVICE_ID_COUNT] = {PTHREAD_MUTEX_INITIALIZER};
static pthread_mutex_t m_t_MutexEvent[DRV_UART_DEVICE_ID_COUNT] = {PTHREAD_MUTEX_INITIALIZER};
static pthread_cond_t m_t_Cond[DRV_UART_DEVICE_ID_COUNT] = {PTHREAD_COND_INITIALIZER};
static task_comm_callback m_t_Callback = {0};

//Private function declaration

static uint TaskComm_InitializeDevice(
    void);
static uint TaskComm_InitializeComm(
	void);
static uint TaskComm_GetDevice(
	devcomm_int t_Address,
	devcomm_int *tp_Device);
static void TaskComm_Memcpy(
	uint8 *u8p_Target,
	const uint8 *u8p_Source,
	devcomm_int t_Length);
static uint8 TaskComm_GetCRC8(
	const uint8 *u8p_Data,
	devcomm_int t_Length,
	uint8 u8_Base);
static uint16 TaskComm_GetCRC16(
	const uint8 *u8p_Data,
	devcomm_int t_Length,
	uint16 u16_Base);
static uint TaskComm_HandleCommEvent(
	devcomm_int t_Device,
	devcomm_int t_Address,
	devcomm_int t_SourcePort,
	devcomm_int t_TargetPort,
	devcomm_int t_Event);
static uint TaskComm_WriteDevice(
	devcomm_int t_Device,
	const uint8 *u8p_Data,
	devcomm_int t_Length);
static uint TaskComm_ReadDevice(
	devcomm_int t_Device,
	uint8 *u8p_Data,
	devcomm_int *tp_Length);
static void TaskComm_WriteDone(
	uint ui_DeviceID);
static void TaskComm_ReadDone(
	uint ui_DeviceID,
	const uint8 *u8p_Data,
	uint ui_Length);
static uint TaskComm_Write(
	uint ui_DeviceID,
	const uint8 *u8p_Data,
	uint8 u8_Length);
static void TaskComm_Delay(
	uint16 u16_Time);
static void *TaskComm_WriteThread(
	void *arg);
static void TaskComm_Encrypt(
	uint8 *u8p_Data,
	devcomm_int t_Length);
static void TaskComm_Decrypt(
	uint8 *u8p_Data,
	devcomm_int t_Length);
//Public function definition

uint TaskComm_Initialize(void)
{
	uint i;
	pthread_t t_ThreadID;

	LOGD("Initialization begin");

	if (TaskComm_InitializeDevice() != FUNCTION_OK)
	{
		LOGD("Initialization fail 11111");
		return FUNCTION_FAIL;
	}

	if (TaskComm_InitializeComm() != FUNCTION_OK)
	{
		LOGD("Initialization fail 22222");
		return FUNCTION_FAIL;
	}

	for (i = 0; i < DRV_UART_DEVICE_ID_COUNT; i++)
	{
		if (LibQueue_Initialize(&m_t_QueueSend[i], m_u8_BufferQueue[i], sizeof(m_u8_BufferQueue[i])) != FUNCTION_OK)
		{
			LOGD("Initialization fail 3333");
			return FUNCTION_FAIL;
		}

		FLAG_SET_BIT(m_ui_State[i], TASK_COMM_STATE_INITIAL);
		m_ui_Address[i] = i;

		if (pthread_create(&t_ThreadID, NULL, TaskComm_WriteThread, (void *)&m_ui_Address[i]) != 0)
		{
			LOGD("Initialization fail 4444");
			return FUNCTION_FAIL;
		}
	}

	TaskComm_Delay(TASK_COMM_THREAD_CREATE_DELAY);

	LOGD("Initialization end");

	return FUNCTION_OK;
}

void TaskComm_Finalize(void)
{
	uint i;

	for (i = 0; i < DRV_UART_DEVICE_ID_COUNT; i++)
	{
		FLAG_CLEAR_BIT(m_ui_State[i], TASK_COMM_STATE_INITIAL);
	}
}

uint TaskComm_SetConfig(
	uint ui_Address,
	uint ui_Parameter,
	const void *vp_Value)
{
	devcomm_int t_Device;

	switch (ui_Parameter)
	{
	case TASK_COMM_PARAM_CALLBACK:
		Drv_Memcpy((uint8 *)&m_t_Callback, (const uint8 *)vp_Value, sizeof(task_comm_callback));
		break;

	case TASK_COMM_PARAM_LINK:

		if (TaskComm_GetDevice((devcomm_int)ui_Address, &t_Device) != FUNCTION_OK)
		{
			return FUNCTION_FAIL;
		}

        if (*((const uint8 *)vp_Value) == 0)
		{
			return DevComm_Unlink(t_Device, TASK_ADDRESS_MASTER);
		}
		else
		{
            return DevComm_Link(t_Device, TASK_ADDRESS_MASTER, *((const uint8 *)vp_Value));
		}

		break;

	default:
		return FUNCTION_FAIL;
	}

	return FUNCTION_OK;
}

uint TaskComm_GetConfig(
	uint ui_Address,
	uint ui_Parameter,
	void *vp_Value)
{
	devcomm_int t_Value;
	devcomm_int t_Device;

	switch (ui_Parameter)
	{
	case TASK_COMM_PARAM_BUSY:

		if (TaskComm_GetDevice((devcomm_int)ui_Address, &t_Device) != FUNCTION_OK)
		{
			return FUNCTION_FAIL;
		}

		if (LibQueue_GetConfig(&m_t_QueueSend[t_Device], LIB_QUEUE_PARAM_BUFFER_SPACE, vp_Value) != FUNCTION_OK)
		{
			return FUNCTION_FAIL;
		}

		if (*((uint *)vp_Value) < sizeof(m_u8_BufferQueue[t_Device]))
		{
			*((uint *)vp_Value) = 1;
			return FUNCTION_OK;
		}

		if (DevComm_Query(t_Device, TASK_ADDRESS_MASTER, DEVCOMM_INFO_STATE, &t_Value) != FUNCTION_OK)
		{
			return FUNCTION_FAIL;
		}

		if (t_Value == DEVCOMM_STATE_BUSY)
		{
			*((uint *)vp_Value) = 1;
		}
		else
		{
			*((uint *)vp_Value) = 0;
		}

		break;

	default:
		break;
	}

	return FUNCTION_OK;
}

uint TaskComm_Send(
	uint8 u8_Address,
	uint8 u8_SourcePort,
	uint8 u8_TargetPort,
	const task_comm_command *tp_Command,
	uint8 u8_Mode)
{
	uint ui_Value;
	devcomm_int t_Device;
	task_comm_message_head t_MessageHead;

    LOGD("Send command begin");
    LOGD("Address: %d, Source Port: %d, Target Port: %d", u8_Address, u8_SourcePort, u8_TargetPort);
    LOGD("Operation: %d, Parameter: %d, Length: %d", tp_Command->u8_Operation, tp_Command->u8_Parameter, tp_Command->u8_Length);

	if (u8_Address >= TASK_COUNT_ADDRESS)
	{
		LOGD("1111");
		return FUNCTION_FAIL;
	}

	if (TaskComm_GetDevice((devcomm_int)u8_Address, &t_Device) != FUNCTION_OK)
	{
		LOGD("22222");
		return FUNCTION_FAIL;
	}

	if (tp_Command == (const task_comm_command *)0)
	{
		LOGD("333333");
		return FUNCTION_FAIL;
	}

	if (LibQueue_GetConfig(&m_t_QueueSend[t_Device], LIB_QUEUE_PARAM_BUFFER_SPACE, (void *)&ui_Value) != FUNCTION_OK)
	{
		LOGD("44444");
		return FUNCTION_FAIL;
	}

	if (ui_Value < tp_Command->u8_Length + sizeof(t_MessageHead))
	{
		LOGD("55555");
		return FUNCTION_FAIL;
	}

	t_MessageHead.u8_SourcePort = u8_SourcePort;
	t_MessageHead.u8_TargetPort = u8_TargetPort;
	t_MessageHead.u8_Mode = u8_Mode;
	t_MessageHead.u8_Operation = tp_Command->u8_Operation;
	t_MessageHead.u8_Parameter = tp_Command->u8_Parameter;
	t_MessageHead.u8_DataLength = tp_Command->u8_Length;
	ui_Value = sizeof(t_MessageHead);

	pthread_mutex_lock(&m_t_MutexCond[t_Device]);

	if (LibQueue_PushTail(&m_t_QueueSend[t_Device], (const uint8 *)&t_MessageHead, &ui_Value) != FUNCTION_OK)
	{
		LOGD("6666");
		pthread_mutex_unlock(&m_t_MutexCond[t_Device]);
		return FUNCTION_FAIL;
	}

	ui_Value = (uint)tp_Command->u8_Length;

	if (ui_Value > 0)
	{
		if (LibQueue_PushTail(&m_t_QueueSend[t_Device], (const uint8 *)tp_Command->u8p_Data, &ui_Value) != FUNCTION_OK)
		{
			LOGD("7777");
			pthread_mutex_unlock(&m_t_MutexCond[t_Device]);
			return FUNCTION_FAIL;
		}
	}

	FLAG_SET_BIT(m_ui_State[t_Device], TASK_COMM_STATE_SENDING);
	pthread_cond_signal(&m_t_Cond[t_Device]);
	pthread_mutex_unlock(&m_t_MutexCond[t_Device]);

	LOGD("Send command end");

	return FUNCTION_OK;
}

void TaskComm_TurnOffEncryption(void)
{
	DevComm_SwitchEncryption(TASK_COMM_DEVICE_SERIAL, TASK_ADDRESS_MASTER, DEVCOMM_ENCRYPTION_OFF);
}

void TaskComm_ReadyForEncryption(const uint8 *key)
{
	AES_SetKey((const uint8_t *)key, 0);
	DevComm_SwitchEncryption(TASK_COMM_DEVICE_SERIAL, TASK_ADDRESS_MASTER, DEVCOMM_ENCRYPTION_READY);
}

void TaskComm_UpdateForEncryption(const uint8 *iv)
{
    AES_SetKey(0, (const uint8_t *)iv);
}

uint TaskComm_SetFrameOn(uint on)
{
    uint i;    
    for (i = 0; i < DRV_UART_DEVICE_ID_COUNT; i++)
    {
        if (DrvUART_SetConfig(i, DRV_UART_PARAM_FRAME, (const void *)&on) != FUNCTION_OK)
        {
            return FUNCTION_FAIL;
        }
    }
    
    return FUNCTION_OK;
}

//Private function definition

static uint TaskComm_InitializeDevice(void)
{
    uint i;
    drv_uart_callback t_Callback;

    t_Callback.fp_WriteDone = (drv_uart_callback_write_done)0;
    t_Callback.fp_ReadDone = TaskComm_ReadDone;
    t_Callback.fp_Write = TaskComm_Write;
    t_Callback.fp_Memcpy = Drv_Memcpy;

    for (i = 0; i < DRV_UART_DEVICE_ID_COUNT; i++)
    {
        if (DrvUART_SetConfig(i, DRV_UART_PARAM_CALLBACK, (const void *)&t_Callback) != FUNCTION_OK)
        {
            return FUNCTION_FAIL;
        }
    }

    return FUNCTION_OK;
}

static uint TaskComm_InitializeComm(void)
{
	devcomm_int i;
	devcomm_profile t_Profile;
	devcomm_callback t_Callback;

	t_Profile.t_Address = TASK_ADDRESS_MASTER;
	t_Profile.t_PacketLengthMax = TASK_COMM_PACKET_LENGTH_MAX;
	t_Profile.u16_Retry = TASK_COMM_SEND_RETRY;
	t_Profile.u16_Timeout = TASK_COMM_SEND_TIMEOUT;
	t_Callback.fp_HandleEvent = TaskComm_HandleCommEvent;
	t_Callback.fp_WriteDevice = TaskComm_WriteDevice;
	t_Callback.fp_ReadDevice = TaskComm_ReadDevice;
	t_Callback.fp_Memcpy = TaskComm_Memcpy;
	t_Callback.fp_GetCRC8 = TaskComm_GetCRC8;
	t_Callback.fp_GetCRC16 = TaskComm_GetCRC16;
    t_Callback.fp_EncryptionUpdate = (devcomm_callback_encryption_update)0;
	t_Callback.fp_Encrypt = TaskComm_Encrypt;
	t_Callback.fp_Decrypt = TaskComm_Decrypt;
	t_Callback.fp_EnterCritical = (devcomm_callback_enter_critical)0;
	t_Callback.fp_ExitCritical = (devcomm_callback_exit_critical)0;

	for (i = 0; i < DRV_UART_DEVICE_ID_COUNT; i++)
	{
		if (DevComm_Initialize(i, &t_Profile, &t_Callback) != FUNCTION_OK)
		{
			return FUNCTION_FAIL;
		}

		if (DevComm_Link(i, TASK_ADDRESS_MASTER, TASK_COMM_PACKET_LENGTH_MASTER) != FUNCTION_OK)
		{
			return FUNCTION_FAIL;
		}
	}

	return FUNCTION_OK;
}

static uint TaskComm_GetDevice(
	devcomm_int t_Address,
	devcomm_int *tp_Device)
{
	devcomm_int i;

	for (i = 0; i < DRV_UART_DEVICE_ID_COUNT; i++)
	{
		if (m_ui_Address[i] == (uint)t_Address)
		{
			*tp_Device = i;

			return FUNCTION_OK;
		}
	}

	return FUNCTION_FAIL;
}

static void TaskComm_Memcpy(
	uint8 *u8p_Target,
	const uint8 *u8p_Source,
	devcomm_int t_Length)
{
	Drv_Memcpy(u8p_Target, u8p_Source, (uint)t_Length);
}

static uint8 TaskComm_GetCRC8(
	const uint8 *u8p_Data,
	devcomm_int t_Length,
	uint8 u8_Base)
{
	return LibChecksum_GetChecksumPartial8Bit(u8p_Data, (uint16)t_Length,
											  u8_Base);
}

static uint16 TaskComm_GetCRC16(
	const uint8 *u8p_Data,
	devcomm_int t_Length,
	uint16 u16_Base)
{
	return LibChecksum_GetChecksumPartial16Bit(u8p_Data, (uint16)t_Length,
											   u16_Base);
}

static uint TaskComm_HandleCommEvent(
	devcomm_int t_Device,
	devcomm_int t_Address,
	devcomm_int t_SourcePort,
	devcomm_int t_TargetPort,
	devcomm_int t_Event)
{
	uint ui_Return;
	devcomm_int t_Length;
	devcomm_int t_Mode;
	task_comm_command t_Command;

	if (t_Address != TASK_ADDRESS_MASTER)
	{
		return FUNCTION_FAIL;
	}

	t_Address = (devcomm_int)m_ui_Address[t_Device];

	LOGD("Handle event %d: Address(%d) PortS(%d) PortT(%d)", t_Event, t_Address, t_SourcePort, t_TargetPort);

	ui_Return = FUNCTION_FAIL;

	do
	{
		if (t_Event == TASK_COMM_EVENT_RECEIVE_DONE)
		{
			t_Length = TASK_COMM_BUFFER_SIZE;

			if (DevComm_Receive(
					t_Device,
					TASK_ADDRESS_MASTER,
					&t_SourcePort,
					&t_TargetPort,
					m_u8_BufferCommand[t_Device],
					&t_Length,
					&t_Mode) != FUNCTION_OK)
			{
				break;
			}

			if (t_Length < TASK_COMM_COUNT_COMMAND_OFFSET)
			{
				break;
			}

			t_Command.u8_Operation = m_u8_BufferCommand[t_Device][TASK_COMM_COMMAND_OFFSET_OPERATION];
			t_Command.u8_Parameter = m_u8_BufferCommand[t_Device][TASK_COMM_COMMAND_OFFSET_PARAMETER];
			t_Command.u8_Length = (uint8)t_Length - TASK_COMM_COUNT_COMMAND_OFFSET;
			t_Command.u8p_Data = m_u8_BufferCommand[t_Device] + TASK_COMM_COUNT_COMMAND_OFFSET;

			LOGD("Call command handler");

			ui_Return = m_t_Callback.fp_HandleCommand(
				(uint8)t_Address,
				(uint8)t_SourcePort,
				(uint8)t_TargetPort,
				&t_Command,
				(uint8)t_Mode);
		}
		else
		{
			LOGD("Call event handler");

			ui_Return = m_t_Callback.fp_HandleEvent(
				(uint8)t_Address,
				(uint8)t_SourcePort,
				(uint8)t_TargetPort,
				(uint8)t_Event);
		}
	} while (0);

	return ui_Return;
}

static uint TaskComm_WriteDevice(
	devcomm_int t_Device,
	const uint8 *u8p_Data,
	devcomm_int t_Length)
{
	uint ui_Return;

	ui_Return = DrvUART_Write((uint)t_Device, u8p_Data, (uint)t_Length);

	return ui_Return;
}

static uint TaskComm_ReadDevice(
	devcomm_int t_Device,
	uint8 *u8p_Data,
	devcomm_int *tp_Length)
{
	uint ui_Length;
	uint ui_Return;

	ui_Length = (uint)(*tp_Length);
	ui_Return = DrvUART_Read((uint)t_Device, u8p_Data, &ui_Length);
	*tp_Length = (devcomm_int)ui_Length;

	return ui_Return;
}

static void TaskComm_WriteDone(
	uint ui_DeviceID)
{
	LOGD("Write device done: %d", ui_DeviceID);

	if (DevComm_WriteDeviceDone((devcomm_int)ui_DeviceID) != FUNCTION_OK)
	{
		LOGD("Write done fail: %d", ui_DeviceID);
	}
}

static void TaskComm_ReadDone(
	uint ui_DeviceID,
	const uint8 *u8p_Data,
	uint ui_Length)
{
#ifdef TASK_COMM_TRHEAD_LOCK
	pthread_mutex_lock(&m_t_MutexEvent[ui_DeviceID]);
#endif

	LOGD("Read device done: %d, length: %d", ui_DeviceID, ui_Length);

	if (DevComm_ReadDeviceDone((devcomm_int)ui_DeviceID, u8p_Data, (devcomm_int)ui_Length) != FUNCTION_OK)
	{
		LOGD("Read done fail: %d, length: %d", ui_DeviceID, ui_Length);
	}

#ifdef TASK_COMM_TRHEAD_LOCK
	pthread_mutex_unlock(&m_t_MutexEvent[ui_DeviceID]);
#endif
}

static uint TaskComm_Write(
	uint ui_DeviceID,
	const uint8 *u8p_Data,
	uint8 u8_Length)
{
	return m_t_Callback.fp_WriteDevice(m_ui_Address[ui_DeviceID], u8p_Data, u8_Length);
}

static void TaskComm_Delay(
	uint16 u16_Time)
{
	LOGD("Delay begin");

#ifndef WIN32
	struct timeval t_Time;

	if (u16_Time > 0)
	{
		t_Time.tv_sec = (time_t)u16_Time / 1000;
		t_Time.tv_usec = (suseconds_t)((time_t)u16_Time - (1000 * t_Time.tv_sec)) * 1000;
		select(0, NULL, NULL, NULL, &t_Time);
	}
#else
	Sleep(u16_Time);
#endif

	LOGD("Delay end");
}

static devcomm_int TaskComm_SendCommand(
	devcomm_int t_Device)
{
	uint ui_Value;
	task_comm_message_head t_MessageHead;

	ui_Value = sizeof(t_MessageHead);

	pthread_mutex_lock(&m_t_MutexCond[t_Device]);

	if (LibQueue_PopHead(&m_t_QueueSend[t_Device], (uint8 *)&t_MessageHead, &ui_Value) != FUNCTION_OK)
	{
		pthread_mutex_unlock(&m_t_MutexCond[t_Device]);
		return DEVCOMM_STATE_IDLE;
	}

	pthread_mutex_unlock(&m_t_MutexCond[t_Device]);

	if (ui_Value != sizeof(t_MessageHead))
	{
		return DEVCOMM_STATE_IDLE;
	}

	ui_Value = (uint)t_MessageHead.u8_DataLength;

	if (ui_Value > 0)
	{
		pthread_mutex_lock(&m_t_MutexCond[t_Device]);

		if (LibQueue_PopHead(
				&m_t_QueueSend[t_Device],
				m_u8_BufferCommand[t_Device] + TASK_COMM_COUNT_COMMAND_OFFSET,
				&ui_Value) != FUNCTION_OK)
		{
			pthread_mutex_unlock(&m_t_MutexCond[t_Device]);
			return DEVCOMM_STATE_IDLE;
		}

		pthread_mutex_unlock(&m_t_MutexCond[t_Device]);

		if (ui_Value != (uint)t_MessageHead.u8_DataLength)
		{
			return DEVCOMM_STATE_IDLE;
		}
	}

	m_u8_BufferCommand[t_Device][TASK_COMM_COMMAND_OFFSET_OPERATION] = t_MessageHead.u8_Operation;
	m_u8_BufferCommand[t_Device][TASK_COMM_COMMAND_OFFSET_PARAMETER] = t_MessageHead.u8_Parameter;

	if (DevComm_Send(
			t_Device,
			TASK_ADDRESS_MASTER,
			(devcomm_int)t_MessageHead.u8_SourcePort,
			(devcomm_int)t_MessageHead.u8_TargetPort,
			(uint8 *)m_u8_BufferCommand[t_Device],
			(devcomm_int)(t_MessageHead.u8_DataLength + TASK_COMM_COUNT_COMMAND_OFFSET),
			(devcomm_int)t_MessageHead.u8_Mode) != FUNCTION_OK)
	{
		return DEVCOMM_STATE_IDLE;
	}

	return DEVCOMM_STATE_BUSY;
}

static void *TaskComm_WriteThread(void *arg)
{
	devcomm_int t_State;
	devcomm_int t_Device;
	devcomm_int t_Address;

	t_Address = (devcomm_int) * ((uint *)arg);

	if (TaskComm_GetDevice(t_Address, &t_Device) != FUNCTION_OK)
	{
		return (void *)0;
	}

	while (FLAG_GET_BIT(m_ui_State[t_Device], TASK_COMM_STATE_INITIAL) != 0)
	{
		pthread_mutex_lock(&m_t_MutexCond[t_Device]);

		if (FLAG_GET_BIT(m_ui_State[t_Device], TASK_COMM_STATE_SENDING) == 0)
		{
			pthread_cond_wait(&m_t_Cond[t_Device], &m_t_MutexCond[t_Device]);
		}

		FLAG_CLEAR_BIT(m_ui_State[t_Device], TASK_COMM_STATE_SENDING);
		pthread_mutex_unlock(&m_t_MutexCond[t_Device]);

		LOGD("Enter write thread");

		do
		{
// #ifdef TASK_COMM_TRHEAD_LOCK			
			pthread_mutex_lock(&m_t_MutexEvent[t_Device]);
// #endif

			if (DevComm_Query(t_Device, TASK_ADDRESS_MASTER, DEVCOMM_INFO_STATE, &t_State) != FUNCTION_OK)
			{
// #ifdef TASK_COMM_TRHEAD_LOCK
				pthread_mutex_unlock(&m_t_MutexEvent[t_Device]);
// #endif
				return (void *)0;
			}

			if (t_State == DEVCOMM_STATE_BUSY)
			{
				TaskComm_WriteDone((uint)t_Device);
			}
			else
			{
				t_State = TaskComm_SendCommand(t_Device);
			}

			if (t_State == DEVCOMM_STATE_BUSY)
			{
				DevComm_Tick(t_Device, TASK_COMM_WRITE_DELAY);
			}

// #ifdef TASK_COMM_TRHEAD_LOCK
			pthread_mutex_unlock(&m_t_MutexEvent[t_Device]);
// #endif

			if (t_State == DEVCOMM_STATE_BUSY)
			{
				TaskComm_Delay(TASK_COMM_WRITE_DELAY);
			}
		} while (t_State == DEVCOMM_STATE_BUSY);

		LOGD("Exit write thread");
	}

	return (void *)0;
}
static void TaskComm_Encrypt(uint8 *u8p_Data, devcomm_int t_Length)
{
	AES_CFB_encrypt((uint8_t *)u8p_Data, t_Length);
}

static void TaskComm_Decrypt(uint8 *u8p_Data, devcomm_int t_Length)
{
	AES_CFB_decrypt((uint8_t *)u8p_Data, t_Length);
}
