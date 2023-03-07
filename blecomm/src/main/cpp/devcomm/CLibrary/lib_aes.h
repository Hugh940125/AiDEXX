#ifndef _LIB_AES_H_
#define _LIB_AES_H_

#include <stdint.h>

#ifdef __cplusplus
extern "C"
{
#endif

#define AES128 1
//#define AES192 1
//#define AES256 1

#if defined(AES256) && (AES256 == 1)
    #define KEYLEN 32
#elif defined(AES192) && (AES192 == 1)
    #define KEYLEN 24
#else
    #define KEYLEN 16   // Key length in bytes
#endif

void LIB_AES_CFB_encrypt(uint8_t* buffer, uint8_t length);
void LIB_AES_CFB_decrypt(uint8_t* buffer, uint8_t length);

#ifdef __cplusplus
}
#endif

#endif //_LIB_AES_H_
