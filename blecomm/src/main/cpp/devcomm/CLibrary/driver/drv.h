/*
 * Module:	Hardware driver
 * Author:	Lvjianfeng
 * Date:	2011.8
 */

#ifndef _DRV_H_
#define _DRV_H_

#include "../global.h"
#include "drv_uart.h"

#ifdef __cplusplus
extern "C"
{
#endif

//Constant definition


//Type definition


//Function declaration

uint Drv_Initialize(void);
void Drv_Finalize(void);
void Drv_Memcpy
(
	uint8 *u8p_Target,
	const uint8 *u8p_Source,
	uint ui_Length
);
void Drv_Memset
(
	uint8 *u8p_Data,
	uint8 u8_Value,
	uint ui_Length
);

#ifdef __cplusplus
}
#endif

#endif
