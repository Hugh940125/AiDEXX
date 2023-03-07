/*
 * Module:	Communication manager task
 * Author:	Lvjianfeng
 * Date:	2011.9
 */

#ifndef _TASK_COMM_H_
#define _TASK_COMM_H_

#include "global.h"

#ifdef __cplusplus
extern "C"
{
#endif

//Constant define

//Type definition

typedef enum
{
	TASK_ADDRESS_MASTER = 0,
	TASK_ADDRESS_SLAVE,
	TASK_COUNT_ADDRESS
} task_comm_address;

typedef enum
{
	TASK_COMM_PARAM_BUSY = 0,
	TASK_COMM_PARAM_CALLBACK,
	TASK_COMM_PARAM_LINK,
	TASK_COMM_COUNT_PARAM
} task_comm_parameter;

typedef enum
{
	TASK_COMM_MODE_ACKNOWLEDGEMENT = 0,
	TASK_COMM_MODE_NO_ACKNOWLEDGEMENT,
	TASK_COMM_COUNT_MODE
} task_comm_mode;

typedef enum
{
	TASK_COMM_EVENT_SEND_DONE = 0,
	TASK_COMM_EVENT_ACKNOWLEDGE,
	TASK_COMM_EVENT_TIMEOUT,
	TASK_COMM_EVENT_RECEIVE_DONE,
	TASK_COMM_COUNT_EVENT
} task_comm_event;

typedef struct
{
	uint8 u8_Operation;
	uint8 u8_Parameter;
	uint8 *u8p_Data;
	uint8 u8_Length;
} task_comm_command;

typedef uint (*task_comm_callback_handle_event)(
	uint8 u8_Address,
	uint8 u8_SourcePort,
	uint8 u8_TargetPort,
	uint8 u8_Event);

typedef uint (*task_comm_callback_handle_command)(
	uint8 u8_Address,
	uint8 u8_SourcePort,
	uint8 u8_TargetPort,
	const task_comm_command *tp_Command,
	uint8 u8_Mode);

typedef uint (*task_comm_callback_write_device)(
	uint8 u8_Address,
	const uint8 *u8p_Data,
	uint8 u8_Length);

typedef struct
{
	task_comm_callback_handle_event fp_HandleEvent;
	task_comm_callback_handle_command fp_HandleCommand;
	task_comm_callback_write_device fp_WriteDevice;
} task_comm_callback;

//Function declaration

uint TaskComm_Initialize(void);
void TaskComm_Finalize(void);
uint TaskComm_SetConfig(
	uint ui_Address,
	uint ui_Parameter,
	const void *vp_Value);
uint TaskComm_GetConfig(
	uint ui_Address,
	uint ui_Parameter,
	void *vp_Value);
uint TaskComm_Send(
	uint8 u8_Address,
	uint8 u8_SourcePort,
	uint8 u8_TargetPort,
	const task_comm_command *tp_Command,
	uint8 u8_Mode);
void TaskComm_TurnOffEncryption(void);
void TaskComm_ReadyForEncryption(const uint8 *key);
void TaskComm_UpdateForEncryption(const uint8 *iv);

uint TaskComm_SetFrameOn(uint on);

#ifdef __cplusplus
}
#endif

#endif
