/*
 * Module:	UART driver
 * Author:	Lvjianfeng
 * Date:	2014.8
 */

#ifndef _DRV_UART_H_
#define _DRV_UART_H_

#include "../global.h"

#ifdef __cplusplus
extern "C"
{
#endif

//Constant define

#define DRV_UART_DEVICE_ID_COUNT	1


//Type definition

typedef enum
{
	DRV_UART_PARAM_BUSY = 0,
	DRV_UART_PARAM_CALLBACK,
	DRV_UART_PARAM_FRAME,
	DRV_UART_COUNT_PARAM
} drv_uart_param;

typedef void (*drv_uart_callback_write_done)
(
	uint ui_DeviceID
);

typedef void (*drv_uart_callback_read_done)
(
	uint ui_DeviceID,
	const uint8 *u8p_Data,
	uint ui_Length
);

typedef uint (*drv_uart_callback_write)
(
	uint ui_DeviceID,
	const uint8 *u8p_Data,
	uint8 u8_Length
);

typedef void (*drv_uart_callback_memcpy)
(
	uint8 *u8p_Target,
	const uint8 *u8p_Source,
	uint ui_Lenght
);

typedef struct
{
	drv_uart_callback_write_done fp_WriteDone;
	drv_uart_callback_read_done fp_ReadDone;
	drv_uart_callback_write fp_Write;
	drv_uart_callback_memcpy fp_Memcpy;
} drv_uart_callback;


//Function declaration

uint DrvUART_Initialize
(
	uint ui_DeviceID
);
void DrvUART_Finalize
(
	uint ui_DeviceID
);
uint DrvUART_SetConfig
(
	uint ui_DeviceID,
	uint ui_Parameter,
	const void *vp_Value
);
uint DrvUART_GetConfig
(
	uint ui_DeviceID,
	uint ui_Parameter,
	void *vp_Value
);
uint DrvUART_Write
(
	uint ui_DeviceID,
	const uint8 *u8p_Data,
	uint ui_Length
);
uint DrvUART_Read
(
	uint ui_DeviceID,
	uint8 *u8p_Data,
	uint *uip_Length
);
uint DrvUART_Receive
(
	uint ui_DeviceID,
	const uint8 *u8p_Data,
	uint ui_Length
);

#if DRV_UART_TEST_ENABLE == 1
void DrvUART_Test(void);
#endif

#ifdef __cplusplus
}
#endif

#endif
