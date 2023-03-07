/*
 * Module:	Frame library
 * Author:	Lvjianfeng
 * Date:	2012.12
 */

#ifndef _LIB_FRAME_H_
#define _LIB_FRAME_H_


#include "global.h"

#ifdef __cplusplus
extern "C"
{
#endif

//Constant definition

#ifndef LIB_FRAME_HEADER_LENGTH
#define LIB_FRAME_HEADER_LENGTH		2
#endif


//Type definition

#ifndef lib_frame_int
#define lib_frame_int				uint
#endif

typedef enum
{
	LIB_FRAME_PARAM_STATE,
	LIB_FRAME_PARAM_LENGTH,
	LIB_FRAME_COUNT_PARAM
} lib_frame_param;

typedef enum
{
	LIB_FRAME_STATE_IDLE = 0,
	LIB_FRAME_STATE_HEADER,
	LIB_FRAME_STATE_DATA,
	LIB_FRAME_STATE_ESCAPE,
	LIB_FRAME_COUNT_STATE
} lib_frame_state;

typedef struct
{
	lib_frame_int t_State;
	lib_frame_int t_Length;
	lib_frame_int t_HeaderCount;
	lib_frame_int t_Reserved;
	uint8 u8_Header;
	uint8 u8_Escape;
} lib_frame_object;


//Function declaration

void LibFrame_Initialize
(
	lib_frame_object *tp_Frame
);
uint LibFrame_SetConfig
(
	lib_frame_object *tp_Frame,
	uint ui_Parameter,
	const void *vp_Value
);
uint LibFrame_GetConfig
(
	lib_frame_object *tp_Frame,
	uint ui_Parameter,
	void *vp_Value
);
void LibFrame_Pack
(
	lib_frame_object *tp_Frame,
	const uint8 *u8p_Source,
	uint8 *u8p_Target,
	lib_frame_int *tp_Length
);
lib_frame_int LibFrame_Unpack
(
	lib_frame_object *tp_Frame,
	const uint8 *u8p_Source,
	uint8 *u8p_Target,
	lib_frame_int *tp_Length
);

#ifdef __cplusplus
}
#endif

#endif
