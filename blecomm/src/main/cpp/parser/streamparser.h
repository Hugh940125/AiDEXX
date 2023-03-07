#ifndef STREAMPARSER_H
#define STREAMPARSER_H

#include "../devcomm/CLibrary/global.h"
#include "../util/inputbytestream.h"

class InputByteStream;
class StreamParser
{
protected:
    InputByteStream *ibs;

    StreamParser(const char *bytes, uint16 length, InputByteStream::Endian endian);
    ~StreamParser();
};

#endif // STREAMPARSER_H
