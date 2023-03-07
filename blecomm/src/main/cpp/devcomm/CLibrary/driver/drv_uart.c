/*
 * Module:	UART driver
 * Author:	Lvjianfeng
 * Date:	2014.8
 */


#include <stdio.h>

#include "../lib_frame.h"

#include "drv_uart.h"


//Constant definition

#define UART_BUFFER_SIZE			256

#define FRAME_HEADER				'-'
#define FRAME_ESCAPE				'/'


//Type definition

typedef enum
{
	DRV_UART_FLAG_FRAME = 0,
	DRV_UART_COUNT_FLAG
} drv_uart_flag;

typedef struct
{
	uint ui_Flag;
	uint ui_ReadLength;
	uint8 u8_BufferWrite[UART_BUFFER_SIZE];
	uint8 u8_BufferRead[UART_BUFFER_SIZE];
	uint8 u8_BufferLog[UART_BUFFER_SIZE];
	drv_uart_callback t_Callback;
	lib_frame_object t_Frame;
} drv_uart_control;


//Private variable definition

static drv_uart_control m_t_UARTControl[DRV_UART_DEVICE_ID_COUNT] = {{0}};


//Private function declaration


//Public function definition

uint DrvUART_Initialize
(
	uint ui_DeviceID
)
{
	m_t_UARTControl[ui_DeviceID].t_Frame.u8_Header = FRAME_HEADER;
	m_t_UARTControl[ui_DeviceID].t_Frame.u8_Escape = FRAME_ESCAPE;

	return FUNCTION_OK;
}


void DrvUART_Finalize
(
	uint ui_DeviceID
)
{
}


uint DrvUART_SetConfig
(
	uint ui_DeviceID,
	uint ui_Parameter,
	const void *vp_Value
)
{
	drv_uart_control *tp_Control;


	tp_Control = &m_t_UARTControl[ui_DeviceID];

	switch (ui_Parameter)
	{
		case DRV_UART_PARAM_CALLBACK:
			tp_Control->t_Callback = *((const drv_uart_callback *)vp_Value);
			break;

		case DRV_UART_PARAM_FRAME:

			if (*((const uint *)vp_Value) != 0)
			{
				FLAG_SET_BIT(tp_Control->ui_Flag, DRV_UART_FLAG_FRAME);
			}
			else
			{
				FLAG_CLEAR_BIT(tp_Control->ui_Flag, DRV_UART_FLAG_FRAME);
			}

			LibFrame_Initialize(&tp_Control->t_Frame);
			break;

		default:
			break;
	}

	return FUNCTION_OK;
}


uint DrvUART_GetConfig
(
	uint ui_DeviceID,
	uint ui_Parameter,
	void *vp_Value
)
{
//	uint ui_Value;


	switch (ui_Parameter)
	{
		case DRV_UART_PARAM_BUSY:
			*((uint *)vp_Value) = 0;
			break;

		case DRV_UART_PARAM_FRAME:

			if (FLAG_GET_BIT(m_t_UARTControl[ui_DeviceID].ui_Flag,
				DRV_UART_FLAG_FRAME) != 0)
			{
				*((uint *)vp_Value) = 1;
			}
			else
			{
				*((uint *)vp_Value) = 0;
			}

			break;

		default:
			return FUNCTION_FAIL;
	}

	return FUNCTION_OK;
}


uint DrvUART_Write
(
	uint ui_DeviceID,
	const uint8 *u8p_Data,
	uint ui_Length
)
{
	uint ui_Log;
	uint ui_Index;
	lib_frame_int t_Length;
	drv_uart_control *tp_Control;
	char *u8p_BufferLog;


	LOGD("Write frame, length: %d", ui_Length);

	if (ui_Length <= 0)
	{
		return FUNCTION_FAIL;
	}

	ui_Log = 0;
	tp_Control = &m_t_UARTControl[ui_DeviceID];
	u8p_BufferLog = (char *)tp_Control->u8_BufferLog;

	for (ui_Index = 0; ui_Index < ui_Length; ui_Index++)
	{
		ui_Log = snprintf(u8p_BufferLog, (char *)tp_Control->u8_BufferLog +
			sizeof(tp_Control->u8_BufferLog) - u8p_BufferLog, "%02X ",
			u8p_Data[ui_Index]);
		u8p_BufferLog += ui_Log;
	}

	LOGD("Write frame, data: %s", tp_Control->u8_BufferLog);

	t_Length = (lib_frame_int)ui_Length;

	//Check if length of data is out of range
	if (FLAG_GET_BIT(tp_Control->ui_Flag, DRV_UART_FLAG_FRAME) != 0)
	{
		if (ui_Length > (UART_BUFFER_SIZE / 2) - LIB_FRAME_HEADER_LENGTH)
		{
			return FUNCTION_FAIL;
		}

		LibFrame_Pack(&tp_Control->t_Frame, u8p_Data,
			tp_Control->u8_BufferWrite, &t_Length);
	}
	else
	{
		if (tp_Control->t_Callback.fp_Memcpy != 0)
		{
			tp_Control->t_Callback.fp_Memcpy(tp_Control->u8_BufferWrite,
				u8p_Data, ui_Length);
		}
	}

	if (tp_Control->t_Callback.fp_Write != 0)
	{
		if (tp_Control->t_Callback.fp_Write(ui_DeviceID,
			tp_Control->u8_BufferWrite, (uint8)t_Length) != FUNCTION_OK)
		{
			LOGE("Write serial port fail");

			return FUNCTION_FAIL;
		}
	}

	LOGD("Write frame end");

	return FUNCTION_OK;
}


uint DrvUART_Read
(
	uint ui_DeviceID,
	uint8 *u8p_Data,
	uint *uip_Length
)
{
	drv_uart_control *tp_Control;


	LOGD("Read frame, length: %d", *uip_Length);

	tp_Control = &m_t_UARTControl[ui_DeviceID];

	if ((*uip_Length <= 0) || (tp_Control->ui_ReadLength == 0))
	{
		return FUNCTION_FAIL;
	}

	if (*uip_Length > tp_Control->ui_ReadLength)
	{
		*uip_Length = tp_Control->ui_ReadLength;
	}

	if (tp_Control->t_Callback.fp_Memcpy != 0)
	{
		tp_Control->t_Callback.fp_Memcpy(u8p_Data,
			tp_Control->u8_BufferRead, *uip_Length);
	}

	tp_Control->ui_ReadLength = 0;

	return FUNCTION_OK;
}


uint DrvUART_Receive
(
	uint ui_DeviceID,
	const uint8 *u8p_Data,
	uint ui_Length
)
{
	uint ui_Log;
	uint ui_Index;
	uint8 *u8p_BufferRead;
	lib_frame_int t_Length;
	drv_uart_control *tp_Control;
	char *u8p_BufferLog;


	tp_Control = &m_t_UARTControl[ui_DeviceID];

	if ((ui_Length <= 0) || (ui_Length > UART_BUFFER_SIZE) ||
		(tp_Control->ui_ReadLength > 0))
	{
		return FUNCTION_FAIL;
	}

	u8p_BufferRead = tp_Control->u8_BufferRead;

	while (ui_Length > 0)
	{
		if (FLAG_GET_BIT(tp_Control->ui_Flag, DRV_UART_FLAG_FRAME) != 0)
		{
			tp_Control->ui_ReadLength = 0;

			while ((ui_Length > 0) && (tp_Control->ui_ReadLength == 0))
			{
				t_Length = 1;
				tp_Control->ui_ReadLength =
					(uint) LibFrame_Unpack(&tp_Control->t_Frame, u8p_Data,
						u8p_BufferRead, &t_Length);
				ui_Length--;
				u8p_Data++;

				if (tp_Control->t_Frame.t_Length >= UART_BUFFER_SIZE)
				{
					LOGD("Data received overflow");
					LibFrame_Initialize(&tp_Control->t_Frame);
					ui_Length = 0;
					break;
				}
			}

			if (tp_Control->ui_ReadLength > 0)
			{
				LibFrame_Initialize(&tp_Control->t_Frame);
			}
		}
		else
		{
			if (tp_Control->t_Callback.fp_Memcpy != 0)
			{
				tp_Control->ui_ReadLength = ui_Length;
				tp_Control->t_Callback.fp_Memcpy(u8p_BufferRead, u8p_Data,
					ui_Length);
				ui_Length = 0;
			}
		}

		if (tp_Control->ui_ReadLength > 0)
		{
			LOGD("Receive frame, length: %d", tp_Control->ui_ReadLength);

			ui_Log = 0;
			u8p_BufferLog = (char *) tp_Control->u8_BufferLog;

			for (ui_Index = 0; ui_Index < tp_Control->ui_ReadLength; ui_Index++)
			{
				ui_Log = snprintf(u8p_BufferLog,
					(char *) tp_Control->u8_BufferLog +
						sizeof(tp_Control->u8_BufferLog) - u8p_BufferLog,
					"%02X ", tp_Control->u8_BufferRead[ui_Index]);
				u8p_BufferLog += ui_Log;
			}

			LOGD("Receive frame, data: %s", tp_Control->u8_BufferLog);

			if (tp_Control->t_Callback.fp_ReadDone != 0)
			{
				tp_Control->t_Callback.fp_ReadDone(ui_DeviceID,
					tp_Control->u8_BufferRead, tp_Control->ui_ReadLength);
			}

			LOGD("Receive frame end");
		}
	}

	return FUNCTION_OK;
}


//Private function definition
