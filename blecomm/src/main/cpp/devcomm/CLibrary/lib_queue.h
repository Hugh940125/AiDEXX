/*
 * Module:	Circular queue library
 * Author:	Lvjianfeng
 * Date:	2011.9
 */

#ifndef _LIB_QUEUE_H_
#define _LIB_QUEUE_H_

#include "global.h"

#ifdef __cplusplus
extern "C"
{
#endif

//Constant define

#ifndef LIB_QUEUE_TEST_ENABLE
#define LIB_QUEUE_TEST_ENABLE 0
#endif

//Type definition

typedef enum
{
	LIB_QUEUE_PARAM_HEAD,
	LIB_QUEUE_PARAM_TAIL,
	LIB_QUEUE_PARAM_BUFFER_CLEAR,
	LIB_QUEUE_PARAM_BUFFER_SPACE,
	LIB_QUEUE_COUNT_PARAM
} lib_queue_param;

typedef struct
{
	uint ui_Head;
	uint ui_Tail;
	uint ui_Space;
	uint ui_Length;
	uint8 *u8p_Buffer;
} lib_queue_object;

//Function declaration

uint LibQueue_Initialize(
	lib_queue_object *tp_Queue,
	uint8 *u8p_Buffer,
	uint ui_Length);
uint LibQueue_SetConfig(
	lib_queue_object *tp_Queue,
	uint ui_Parameter,
	const void *vp_Value);
uint LibQueue_GetConfig(
	lib_queue_object *tp_Queue,
	uint ui_Parameter,
	void *vp_Value);
uint LibQueue_PushHead(
	lib_queue_object *tp_Queue,
	const uint8 *u8p_Data,
	uint *uip_Length);
uint LibQueue_PushTail(
	lib_queue_object *tp_Queue,
	const uint8 *u8p_Data,
	uint *uip_Length);
uint LibQueue_PopHead(
	lib_queue_object *tp_Queue,
	uint8 *u8p_Data,
	uint *uip_Length);
uint LibQueue_PopTail(
	lib_queue_object *tp_Queue,
	uint8 *u8p_Data,
	uint *uip_Length);
uint LibQueue_PeekHead(
	lib_queue_object *tp_Queue,
	uint8 *u8p_Data,
	uint *uip_Length);
uint LibQueue_PeekTail(
	lib_queue_object *tp_Queue,
	uint8 *u8p_Data,
	uint *uip_Length);

#if LIB_QUEUE_TEST_ENABLE == 1
void LibQueue_Test(void);
#endif

#ifdef __cplusplus
}
#endif

#endif
