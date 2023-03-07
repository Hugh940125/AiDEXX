#ifndef AES_ENCRYPTOR_H
#define AES_ENCRYPTOR_H

#include "../../devcomm/CLibrary/global.h"

class AesEncryptor
{
public:
    enum BlockSize {AES128, AES192, AES256};
    enum Mode {CFB};

    explicit AesEncryptor(Mode mode, BlockSize block);
    ~AesEncryptor();
    void setKey(const uint8 *key);
    void setIv(const uint8 *iv);
    void encrypt(uint8 *data, uint8 length);
    void decrypt(uint8 *data, uint8 length);

private:
    Mode mode;
    BlockSize blockSize;

    uint8 *Key;
    uint8 *Iv;
    uint8 *RoundKey;

    int Nk;
    int KEYLEN;
    int Nr;
    int keyExpSize;

    typedef uint8 state_t[4][4];
    state_t* state;

    void memCpy(uint8 *dest, const uint8 *src, uint32 length);
    void memSet(uint8 *dest, uint8 u8_Value, uint32 length);

    static uint8 getSBoxValue(uint8 num);
    static uint8 xtime(uint8 x);

    void KeyExpansion();
    void AddRoundKey(uint8 round);
    void SubBytes();
    void ShiftRows();
    void MixColumns();
    void Cipher();

    void CFB_encrypt(uint8* buffer, uint8 length);
    void CFB_decrypt(uint8* buffer, uint8 length);
};




#endif // AES_ENCRYPTOR_H
