/*
 * Module:	Circular queue library
 * Author:	Lvjianfeng
 * Date:	2011.9
 */


#include "lib_queue.h"


//Constant definition


//Type definition


//Private variable definition


//Private function declaration

static void LibQueue_Memcpy
(
	uint8 *u8p_Dst,
	const uint8 *u8p_Src,
	uint ui_Length
);


//Public function definition

uint LibQueue_Initialize
(
	lib_queue_object *tp_Queue,
	uint8 *u8p_Buffer,
	uint ui_Length
)
{
	tp_Queue->ui_Head = 0;
	tp_Queue->ui_Tail = 0;
	tp_Queue->ui_Space = ui_Length;
	tp_Queue->ui_Length = ui_Length;
	tp_Queue->u8p_Buffer = u8p_Buffer;

	return FUNCTION_OK;
}


uint LibQueue_SetConfig
(
	lib_queue_object *tp_Queue,
	uint ui_Parameter,
	const void *vp_Value
)
{
	switch (ui_Parameter)
	{
		case LIB_QUEUE_PARAM_BUFFER_CLEAR:
			tp_Queue->ui_Head = 0;
			tp_Queue->ui_Tail = 0;
			tp_Queue->ui_Space = tp_Queue->ui_Length;
			break;

		default:
			break;
	}

	return FUNCTION_OK;
}


uint LibQueue_GetConfig
(
	lib_queue_object *tp_Queue,
	uint ui_Parameter,
	void *vp_Value
)
{
	switch (ui_Parameter)
	{
		case LIB_QUEUE_PARAM_HEAD:
			(*(uint *)vp_Value) = tp_Queue->ui_Head;
			break;

		case LIB_QUEUE_PARAM_TAIL:
			(*(uint *)vp_Value) = tp_Queue->ui_Tail;
			break;

		case LIB_QUEUE_PARAM_BUFFER_SPACE:
			(*(uint *)vp_Value) = tp_Queue->ui_Space;
			break;

		default:
			break;
	}

	return FUNCTION_OK;
}


uint LibQueue_PushHead
(
	lib_queue_object *tp_Queue,
	const uint8 *u8p_Data,
	uint *uip_Length
)
{
	uint ui_BufferLength;
	uint ui_BufferHead;
	uint ui_PartialLength;
	uint ui_PushLength;


	//Check if length of data is out of range 
	if (*uip_Length == 0)
	{
		return FUNCTION_FAIL;
	}

	ui_BufferLength = tp_Queue->ui_Length;
	ui_PushLength = tp_Queue->ui_Space;

	//Check if data to be push exceeds queue buffer or not
	if (ui_PushLength < *uip_Length)
	{
		*uip_Length = ui_PushLength;

		//Check if queue buffer is empty or not
		if (ui_PushLength == 0)
		{
			return FUNCTION_FAIL;
		}
	}
	else
	{
		ui_PushLength = *uip_Length;
	}

	ui_BufferHead = tp_Queue->ui_Head;

	if (ui_BufferHead < ui_PushLength)
	{
		ui_PartialLength = ui_PushLength - ui_BufferHead;

		if (u8p_Data != (uint8 *)NULL)
		{
			LibQueue_Memcpy(&tp_Queue->u8p_Buffer[ui_BufferLength - ui_PartialLength],
				&u8p_Data[0], ui_PartialLength);
			LibQueue_Memcpy(&tp_Queue->u8p_Buffer[0], &u8p_Data[ui_PartialLength],
				ui_BufferHead);
		}

		ui_BufferHead = ui_BufferLength - ui_PartialLength;
	}
	else
	{
		if (u8p_Data != (uint8 *)NULL)
		{
			LibQueue_Memcpy(&tp_Queue->u8p_Buffer[ui_BufferHead - ui_PushLength], 
				&u8p_Data[0], ui_PushLength);
		}

		ui_BufferHead -= ui_PushLength;
	}

	tp_Queue->ui_Head = ui_BufferHead;
	tp_Queue->ui_Space -= ui_PushLength;

	return FUNCTION_OK;
}


uint LibQueue_PushTail
(
	lib_queue_object *tp_Queue,
	const uint8 *u8p_Data,
	uint *uip_Length
)
{
	uint ui_BufferLength;
	uint ui_BufferTail;
	uint ui_PartialLength;
	uint ui_PushLength;


	//Check if length of data is out of range
	if (*uip_Length == 0)
	{
		return FUNCTION_FAIL;
	}

	ui_BufferLength = tp_Queue->ui_Length;
	ui_PushLength = tp_Queue->ui_Space;

	//Check if data to be pushed exceeds buffer space or not
	if (ui_PushLength < *uip_Length)
	{
		*uip_Length = ui_PushLength;

		//Check if queue buffer is full or not
		if (ui_PushLength == 0)
		{
			return FUNCTION_FAIL;
		}
	}
	else
	{
		ui_PushLength = *uip_Length;
	}

	ui_BufferTail = tp_Queue->ui_Tail;

	if ((ui_BufferTail + ui_PushLength) > ui_BufferLength)
	{
		ui_PartialLength = ui_BufferLength - ui_BufferTail;

		if (u8p_Data != (const uint8 *)NULL)
		{
			LibQueue_Memcpy(&tp_Queue->u8p_Buffer[ui_BufferTail], &u8p_Data[0], 
				ui_PartialLength);
			LibQueue_Memcpy(&tp_Queue->u8p_Buffer[0], &u8p_Data[ui_PartialLength], 
				ui_PushLength - ui_PartialLength);
		}

		ui_BufferTail = ui_PushLength - ui_PartialLength;
	}
	else
	{
		if (u8p_Data != (const uint8 *)NULL)
		{
			LibQueue_Memcpy(&tp_Queue->u8p_Buffer[ui_BufferTail], &u8p_Data[0], 
				ui_PushLength);
		}

		ui_BufferTail +=ui_PushLength;
		
		if (ui_BufferTail == ui_BufferLength)
		{
			ui_BufferTail = 0;
		}
	}

	tp_Queue->ui_Tail = ui_BufferTail;
	tp_Queue->ui_Space -= ui_PushLength;

	return FUNCTION_OK;
}


uint LibQueue_PopHead
(
	lib_queue_object *tp_Queue,
	uint8 *u8p_Data,
	uint *uip_Length
)
{
	uint ui_BufferLength;
	uint ui_BufferHead;
	uint ui_PartialLength;
	uint ui_PushLength;
	uint ui_PopLength;


	//Check if length of data is out of range 
	if (*uip_Length == 0)
	{
		return FUNCTION_FAIL;
	}

	ui_BufferLength = tp_Queue->ui_Length;
	ui_PushLength = ui_BufferLength - tp_Queue->ui_Space;

	//Check if data to be popped exceeds queue buffer or not
	if (ui_PushLength < *uip_Length)
	{
		*uip_Length = ui_PushLength;

		//Check if queue buffer is empty or not
		if (ui_PushLength == 0)
		{
			return FUNCTION_FAIL;
		}

		ui_PopLength = ui_PushLength;
	}
	else
	{
		ui_PopLength = *uip_Length;
	}

	ui_BufferHead = tp_Queue->ui_Head;

	if ((ui_BufferHead + ui_PopLength) > ui_BufferLength)
	{
		ui_PartialLength = ui_BufferLength - ui_BufferHead;

		if (u8p_Data != (uint8 *)NULL)
		{
			LibQueue_Memcpy(&u8p_Data[0], &tp_Queue->u8p_Buffer[ui_BufferHead], 
				ui_PartialLength);
			LibQueue_Memcpy(&u8p_Data[ui_PartialLength], &tp_Queue->u8p_Buffer[0], 
				ui_PopLength - ui_PartialLength);
		}

		ui_BufferHead = ui_PopLength - ui_PartialLength;
	}
	else
	{
		if (u8p_Data != (uint8 *)NULL)
		{
			LibQueue_Memcpy(&u8p_Data[0], &tp_Queue->u8p_Buffer[ui_BufferHead], 
				ui_PopLength);
		}

		ui_BufferHead += ui_PopLength;

		if (ui_BufferHead == ui_BufferLength)
		{
			ui_BufferHead = 0;
		}
	}

	tp_Queue->ui_Head = ui_BufferHead;
	tp_Queue->ui_Space += ui_PopLength;

	return FUNCTION_OK;
}


uint LibQueue_PopTail
(
	lib_queue_object *tp_Queue,
	uint8 *u8p_Data,
	uint *uip_Length
)
{
	uint ui_BufferLength;
	uint ui_BufferTail;
	uint ui_PartialLength;
	uint ui_PushLength;
	uint ui_PopLength;


	//Check if length of data is out of range 
	if (*uip_Length == 0)
	{
		return FUNCTION_FAIL;
	}

	ui_BufferLength = tp_Queue->ui_Length;
	ui_PushLength = ui_BufferLength - tp_Queue->ui_Space;

	//Check if data to be popped exceeds queue buffer or not
	if (ui_PushLength < *uip_Length)
	{
		*uip_Length = ui_PushLength;

		//Check if queue buffer is empty or not
		if (ui_PushLength == 0)
		{
			return FUNCTION_FAIL;
		}

		ui_PopLength = ui_PushLength;
	}
	else
	{
		ui_PopLength = *uip_Length;
	}

	ui_BufferTail = tp_Queue->ui_Tail;

	if (ui_BufferTail < ui_PopLength)
	{
		ui_PartialLength = ui_PopLength - ui_BufferTail;

		if (u8p_Data != (uint8 *)NULL)
		{
			LibQueue_Memcpy(&u8p_Data[0], 
				&tp_Queue->u8p_Buffer[ui_BufferLength - ui_PartialLength], 
				ui_PartialLength);
			LibQueue_Memcpy(&u8p_Data[ui_PartialLength], &tp_Queue->u8p_Buffer[0], 
				ui_BufferTail);
		}

		ui_BufferTail = ui_BufferLength - ui_PartialLength;
	}
	else
	{
		if (u8p_Data != (uint8 *)NULL)
		{
			LibQueue_Memcpy(&u8p_Data[0], 
				&tp_Queue->u8p_Buffer[ui_BufferTail - ui_PopLength], 
				ui_PopLength);
		}

		ui_BufferTail -= ui_PopLength;
	}

	tp_Queue->ui_Tail = ui_BufferTail;
	tp_Queue->ui_Space += ui_PopLength;

	return FUNCTION_OK;
}


uint LibQueue_PeekHead
(
	lib_queue_object *tp_Queue,
	uint8 *u8p_Data,
	uint *uip_Length
)
{
	uint ui_BufferLength;
	uint ui_BufferHead;
	uint ui_PartialLength;
	uint ui_PushLength;
	uint ui_PopLength;


	//Check if length of data is out of range 
	if (*uip_Length == 0)
	{
		return FUNCTION_FAIL;
	}

	ui_BufferLength = tp_Queue->ui_Length;
	ui_PushLength = ui_BufferLength - tp_Queue->ui_Space;

	//Check if data to be popped exceeds queue buffer or not
	if (ui_PushLength < *uip_Length)
	{
		*uip_Length = ui_PushLength;

		//Check if queue buffer is empty or not
		if (ui_PushLength == 0)
		{
			return FUNCTION_FAIL;
		}

		ui_PopLength = ui_PushLength;
	}
	else
	{
		ui_PopLength = *uip_Length;
	}

	ui_BufferHead = tp_Queue->ui_Head;

	if ((ui_BufferHead + ui_PopLength) > ui_BufferLength)
	{
		ui_PartialLength = ui_BufferLength - ui_BufferHead;

		if (u8p_Data != (uint8 *)NULL)
		{
			LibQueue_Memcpy(&u8p_Data[0], &tp_Queue->u8p_Buffer[ui_BufferHead], 
				ui_PartialLength);
			LibQueue_Memcpy(&u8p_Data[ui_PartialLength], &tp_Queue->u8p_Buffer[0], 
				ui_PopLength - ui_PartialLength);
		}
	}
	else
	{
		if (u8p_Data != (uint8 *)NULL)
		{
			LibQueue_Memcpy(&u8p_Data[0], &tp_Queue->u8p_Buffer[ui_BufferHead], 
				ui_PopLength);
		}
	}

	return FUNCTION_OK;
}


uint LibQueue_PeekTail
(
	lib_queue_object *tp_Queue,
	uint8 *u8p_Data,
	uint *uip_Length
)
{
	uint ui_BufferLength;
	uint ui_BufferTail;
	uint ui_PartialLength;
	uint ui_PushLength;
	uint ui_PopLength;


	//Check if length of data is out of range 
	if (*uip_Length == 0)
	{
		return FUNCTION_FAIL;
	}

	ui_BufferLength = tp_Queue->ui_Length;
	ui_PushLength = ui_BufferLength - tp_Queue->ui_Space;

	//Check if data to be popped exceeds queue buffer or not
	if (ui_PushLength < *uip_Length)
	{
		*uip_Length = ui_PushLength;

		//Check if queue buffer is empty or not
		if (ui_PushLength == 0)
		{
			return FUNCTION_FAIL;
		}

		ui_PopLength = ui_PushLength;
	}
	else
	{
		ui_PopLength = *uip_Length;
	}

	ui_BufferTail = tp_Queue->ui_Tail;

	if (ui_BufferTail < ui_PopLength)
	{
		ui_PartialLength = ui_PopLength - ui_BufferTail;

		if (u8p_Data != (uint8 *)NULL)
		{
			LibQueue_Memcpy(&u8p_Data[0], 
				&tp_Queue->u8p_Buffer[ui_BufferLength - ui_PartialLength], 
				ui_PartialLength);
			LibQueue_Memcpy(&u8p_Data[ui_PartialLength], &tp_Queue->u8p_Buffer[0], 
				ui_BufferTail);
		}
	}
	else
	{
		if (u8p_Data != (uint8 *)NULL)
		{
			LibQueue_Memcpy(&u8p_Data[0], 
				&tp_Queue->u8p_Buffer[ui_BufferTail - ui_PopLength], 
				ui_PopLength);
		}
	}

	return FUNCTION_OK;
}


#if LIB_QUEUE_TEST_ENABLE == 1

void LibQueue_Test(void)
{
	lib_queue_object t_Queue;
	uint8 u8_Buffer[15];
	uint8 u8_PushData[] = {'1', '2', '3'};
	uint8 u8_PushData2[] = {'a', 'b', 'c'};
	uint8 u8_PopData[6];
	uint ui_Length;


	LibQueue_Initialize(&t_Queue, u8_Buffer, sizeof(u8_Buffer));

	ui_Length = sizeof(u8_PushData);
	LibQueue_PushTail(&t_Queue, u8_PushData, &ui_Length);
	ui_Length = sizeof(u8_PushData2);
	LibQueue_PushTail(&t_Queue, u8_PushData2, &ui_Length);
	ui_Length = sizeof(u8_PushData);
	LibQueue_PushTail(&t_Queue, u8_PushData, &ui_Length);
	ui_Length = sizeof(u8_PushData2);
	LibQueue_PushTail(&t_Queue, u8_PushData2, &ui_Length);

	ui_Length = sizeof(u8_PopData);
	LibQueue_PopHead(&t_Queue, u8_PopData, &ui_Length);
	ui_Length = sizeof(u8_PushData);
	LibQueue_PushTail(&t_Queue, u8_PushData, &ui_Length);
	ui_Length = sizeof(u8_PopData);
	LibQueue_PopHead(&t_Queue, u8_PopData, &ui_Length);

	ui_Length = sizeof(u8_PushData2);
	LibQueue_PushHead(&t_Queue, u8_PushData2, &ui_Length);
	ui_Length = sizeof(u8_PushData);
	LibQueue_PushHead(&t_Queue, u8_PushData, &ui_Length);
	ui_Length = sizeof(u8_PushData2);
	LibQueue_PushHead(&t_Queue, u8_PushData2, &ui_Length);
	ui_Length = sizeof(u8_PushData);
	LibQueue_PushHead(&t_Queue, u8_PushData, &ui_Length);

	ui_Length = sizeof(u8_PopData);
	LibQueue_PopTail(&t_Queue, u8_PopData, &ui_Length);
	ui_Length = sizeof(u8_PushData2);
	LibQueue_PushHead(&t_Queue, u8_PushData2, &ui_Length);
	ui_Length = sizeof(u8_PopData);
	LibQueue_PopTail(&t_Queue, u8_PopData, &ui_Length);

	ui_Length = sizeof(u8_PushData);
	LibQueue_PushTail(&t_Queue, u8_PushData, &ui_Length);
	ui_Length = sizeof(u8_PushData2);
	LibQueue_PushHead(&t_Queue, u8_PushData2, &ui_Length);
	ui_Length = sizeof(u8_PushData);
	LibQueue_PushTail(&t_Queue, u8_PushData, &ui_Length);
	
	ui_Length = sizeof(u8_PopData);
	LibQueue_PeekTail(&t_Queue, u8_PopData, &ui_Length);
	
	ui_Length = sizeof(u8_PopData);
	LibQueue_PeekHead(&t_Queue, u8_PopData, &ui_Length);
}

#endif


//Private function definition

static void LibQueue_Memcpy
(
	uint8 *u8p_Dst,
	const uint8 *u8p_Src,
	uint ui_Length
)
{
	while (ui_Length > 0)
	{
		*u8p_Dst++ = *u8p_Src++;
		ui_Length--;
	}
}
