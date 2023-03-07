#ifndef BGMSTATEPARSER_H
#define BGMSTATEPARSER_H

#include "../../devcomm/CLibrary/global.h"

#include <vector>

using namespace std;

class BgmStateParser
{
public:
    static const int MIN_BYTES_LENGTH = 1;
    
    BgmStateParser(const char *bytes, uint16 length);
    int32 getValue();

private:
    vector<uint8> bytes;
};

#endif // BGMSTATEPARSER_H
