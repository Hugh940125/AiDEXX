/*
 * Module:	Checksum library
 * Author:	Lvjianfeng
 * Date:	2011.9
 */

#ifndef _LIB_CHECKSUM_H_
#define _LIB_CHECKSUM_H_

#include "global.h"

#ifdef __cplusplus
extern "C"
{
#endif

//Constant definition

#ifndef LIB_CHECKSUM_16_BIT_COMPACT
#define LIB_CHECKSUM_16_BIT_COMPACT		0
#endif


//Type definition


//Function declaration

uint8 LibChecksum_GetChecksum8Bit
(
	const uint8 *u8p_Data,
	uint16 u16_Length
);
uint8 LibChecksum_GetChecksumPartial8Bit
(
	const uint8 *u8p_Data,
	uint16 u16_Length,
	uint8 u8_ChecksumBase
);
uint16 LibChecksum_GetChecksum16Bit_CCITT
(
	const uint8 *u8p_Data,
	uint16 u16_Length
);
uint16 LibChecksum_GetChecksum16Bit
(
	const uint8 *u8p_Data,
	uint16 u16_Length
);
uint16 LibChecksum_GetChecksumPartial16Bit
(
	const uint8 *u8p_Data,
	uint16 u16_Length,
	uint16 u16_ChecksumBase
);

#ifdef __cplusplus
}
#endif

#endif
