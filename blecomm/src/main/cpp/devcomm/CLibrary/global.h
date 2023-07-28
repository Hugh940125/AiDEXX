#ifndef _GLOBAL_H_
#define _GLOBAL_H_

#include <stdio.h>
#include <sys/types.h>
#include "devcomm_config.h"


#ifdef ANDROID
#include "android/log.h"
#define LOG_E(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, __FUNCTION__, fmt, ##args)
#define LOG_I(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  __FUNCTION__, fmt, ##args)
#define LOG_D(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, __FUNCTION__, fmt, ##args)
#elif __APPLE__
#include <TargetConditionals.h>
#ifdef TARGET_OS_IPHONE
#include "MTBleCoreBridgeC.h"
#endif
#else
#define LOG_E(fmt, args...)   {printf("[ERROR] %s: ",__FILE__);printf(fmt,##args);printf("\n");fflush(stdout);}
#define LOG_I(fmt, args...)   {printf("[INFO] %s: ",__FILE__);printf(fmt,##args);printf("\n");fflush(stdout);}
#define LOG_D(fmt, args...)   {printf("[DEBUG] %s: ",__FILE__);printf(fmt,##args);printf("\n");fflush(stdout);}
#endif

#ifndef LOG_LEVEL
#define LOG_LEVEL	3
#endif

#if LOG_LEVEL <= 3
#define LOGE(fmt, args...)		LOG_E(fmt, ##args)
#else
#define LOGE(fmt, args...)
#endif
#if LOG_LEVEL <= 2
#define LOGI(fmt, args...)		LOG_I(fmt, ##args)
#else
#define LOGI(fmt, args...)
#endif
#if LOG_LEVEL <= 1
#define LOGD(fmt, args...)		LOG_D(fmt, ##args)
#else
#define LOGD(fmt, args...)
#endif


#ifdef __cplusplus
extern "C"
{
#endif

//Constant define
#ifndef NULL
	#define NULL			0
#endif

#define FLAG_MASK_1_BIT		0x01
#define FLAG_MASK_2_BIT		0x03
#define FLAG_MASK_3_BIT		0x07
#define FLAG_MASK_4_BIT		0x0F
#define FLAG_MASK_5_BIT		0x1F
#define FLAG_MASK_6_BIT		0x3F
#define FLAG_MASK_7_BIT		0x7F
#define FLAG_MASK_8_BIT		0xFF

#define FLAG_WRITE_FIELD(reg, field, mask, value)	((reg) = ((reg) & (~((mask) << (field)))) | \
													((value) << (field)))
#define FLAG_READ_FIELD(reg, field, mask)			(((reg) >> (field)) & (mask))
#define FLAG_SET_BIT(reg, field)						((reg) |= (1 << (field)))
#define FLAG_CLEAR_BIT(reg, field)					((reg) &= ~(1 << (field)))
#define FLAG_REVERSE_BIT(reg, field)					((reg) ^= (1 << (field)))
#define FLAG_GET_BIT(reg, field)						((reg) & (1 << (field)))


//Type definition

#define BROADCAST_LENGTH    20

#ifndef uint
#define	uint				uint32
#endif

#ifndef sint
#define sint				sint32
#endif

typedef char				int8;
typedef unsigned char		uint8;
typedef signed char			sint8;
typedef short int			int16;
typedef unsigned short int	uint16;
typedef signed short int	sint16;
typedef int					int32;
typedef unsigned int		uint32;
typedef signed int			sint32;
typedef long long			int64;
typedef unsigned long long	uint64;
typedef signed long	long	sint64;
typedef float				float32;
typedef double				float64;

typedef enum 
{
	FUNCTION_OK = 1,
	FUNCTION_FAIL = 0
} function_return;

#ifdef __cplusplus
}
#endif

#endif
