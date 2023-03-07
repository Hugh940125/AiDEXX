/*
 * Module:	Frame library
 * Author:	Lvjianfeng
 * Date:	2012.12
 */


#include "lib_frame.h"


//Constant definition


//Type definition


//Private variable definition


//Public function definition

void LibFrame_Initialize
(
	lib_frame_object *tp_Frame
)
{
	tp_Frame->t_State = LIB_FRAME_STATE_IDLE;
	tp_Frame->t_Length = 0;
	tp_Frame->t_HeaderCount = 0;
}


uint LibFrame_SetConfig
(
	lib_frame_object *tp_Frame,
	uint ui_Parameter,
	const void *vp_Value
)
{
	return FUNCTION_OK;
}


uint LibFrame_GetConfig
(
	lib_frame_object *tp_Frame,
	uint ui_Parameter,
	void *vp_Value
)
{
	switch (ui_Parameter)
	{
		case LIB_FRAME_PARAM_STATE:
			(*(lib_frame_int *)vp_Value) = tp_Frame->t_State;
			break;

		case LIB_FRAME_PARAM_LENGTH:
			(*(lib_frame_int *)vp_Value) = tp_Frame->t_Length;
			break;

		default:
			break;
	}

	return FUNCTION_OK;
}


void LibFrame_Pack
(
	lib_frame_object *tp_Frame,
	const uint8 *u8p_Source,
	uint8 *u8p_Target,
	lib_frame_int *tp_Length
)
{
	lib_frame_int i;
	lib_frame_int t_Length;


	//Assemble head of the frame
	for (i = 0; i < LIB_FRAME_HEADER_LENGTH; i++)
	{
		*u8p_Target = tp_Frame->u8_Header;
		u8p_Target++;
	}	

	t_Length = LIB_FRAME_HEADER_LENGTH;

	//Assemble payload of the frame
	for (i = 0; i < *tp_Length; i++)
	{
        if ((*u8p_Source == tp_Frame->u8_Header) ||
            (*u8p_Source == tp_Frame->u8_Escape))
        {
			*u8p_Target = tp_Frame->u8_Escape;
			u8p_Target++;
			t_Length++;
		}
		
		*u8p_Target = *u8p_Source;
		u8p_Source++;
		u8p_Target++;
		t_Length++;
	}

	//Assemble tail of the frame
	for (i = 0; i < LIB_FRAME_HEADER_LENGTH; i++)
	{
		*u8p_Target = tp_Frame->u8_Header;
		u8p_Target++;
	}	

	t_Length += LIB_FRAME_HEADER_LENGTH;

	*tp_Length = t_Length;
}


lib_frame_int LibFrame_Unpack
(
	lib_frame_object *tp_Frame,
	const uint8 *u8p_Source,
	uint8 *u8p_Target,
	lib_frame_int *tp_Length
)
{
	if ((tp_Frame->t_State == LIB_FRAME_STATE_DATA) ||
		(tp_Frame->t_State == LIB_FRAME_STATE_ESCAPE))
	{
		u8p_Target += tp_Frame->t_Length;
	}
	else
	{
		tp_Frame->t_Length = 0;
	}

	while (*tp_Length > 0)
	{
		(*tp_Length)--;

		switch (tp_Frame->t_State)
		{
			case LIB_FRAME_STATE_IDLE:

				if (*u8p_Source == tp_Frame->u8_Header)
				{
					tp_Frame->t_HeaderCount++;
					tp_Frame->t_State = LIB_FRAME_STATE_HEADER;
				}
				
				break;

			case LIB_FRAME_STATE_HEADER:
				
				if (*u8p_Source == tp_Frame->u8_Header)
				{
					tp_Frame->t_HeaderCount++;
				}
				else
				{
					//Check if new frame is found
					if (tp_Frame->t_HeaderCount >= LIB_FRAME_HEADER_LENGTH)
					{
						tp_Frame->t_HeaderCount = 0;

						if (*u8p_Source == tp_Frame->u8_Escape)
						{
							tp_Frame->t_State = LIB_FRAME_STATE_ESCAPE;
						}
						else
						{
							*u8p_Target = *u8p_Source;
							u8p_Target++;
							tp_Frame->t_Length++;
							tp_Frame->t_State = LIB_FRAME_STATE_DATA;
						}
					}
					else
					{
						tp_Frame->t_HeaderCount = 0;
					}
				}

				break;

			case LIB_FRAME_STATE_DATA:

				//Check if it is end of frame
				if (*u8p_Source == tp_Frame->u8_Header)
				{
					tp_Frame->t_HeaderCount++;

					if (tp_Frame->t_HeaderCount >= LIB_FRAME_HEADER_LENGTH)
					{
						tp_Frame->t_HeaderCount = 0;
						tp_Frame->t_State = LIB_FRAME_STATE_IDLE;
						return tp_Frame->t_Length;
					}
				}
				else 
				{
					tp_Frame->t_HeaderCount = 0;

					if (*u8p_Source == tp_Frame->u8_Escape)
					{
						tp_Frame->t_State = LIB_FRAME_STATE_ESCAPE;
					}
					else
					{
						*u8p_Target = *u8p_Source;
						u8p_Target++;
						tp_Frame->t_Length++;
					}
				}

				break;

			case LIB_FRAME_STATE_ESCAPE:
				*u8p_Target = *u8p_Source;
				u8p_Target++;
				tp_Frame->t_Length++;
				tp_Frame->t_State = LIB_FRAME_STATE_DATA;
				break;

			default:
				break;
		}

		u8p_Source++;
	}

	return 0;
}


//Private function declaration
