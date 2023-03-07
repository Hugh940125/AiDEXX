#ifndef MESSAGE_DIGEST_H_
#define MESSAGE_DIGEST_H_
 
class MD5
{
public:
    static void digest(const unsigned char* data, int len, unsigned char * md5Out);
};

#endif /* MESSAGE_DIGEST_H_ */