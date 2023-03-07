/*
 * Module:	Hardware driver
 * Author:	Lvjianfeng
 * Date:	2011.8
 */


#include "drv.h"


//Constant definition


//Private variable definition


//Private function declaration


//Public function definition

uint Drv_Initialize(void)
{
	uint i;


	for (i = 0; i < DRV_UART_DEVICE_ID_COUNT; i++)
	{
		if (DrvUART_Initialize(i) != FUNCTION_OK)
		{
			return FUNCTION_FAIL;
		}
	}

	return FUNCTION_OK;
}


void Drv_Finalize(void)
{
	uint i;


	for (i = 0; i < DRV_UART_DEVICE_ID_COUNT; i++)
	{
		DrvUART_Finalize(i);
	}
}


void Drv_Memcpy
(
	uint8 *u8p_Target,
	const uint8 *u8p_Source,
	uint ui_Length
)
{
	while (ui_Length > 0)
	{
		*u8p_Target++ = *u8p_Source++;
		ui_Length--;
	}
}


void Drv_Memset
(
	uint8 *u8p_Data,
	uint8 u8_Value,
	uint ui_Length
)
{
	while (ui_Length > 0)
	{
		*u8p_Data++ = u8_Value;
		ui_Length--;
	}
}

//Private function definition
