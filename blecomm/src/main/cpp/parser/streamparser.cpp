#include "streamparser.h"

#include "../util/inputbytestream.h"

StreamParser::StreamParser(const char *bytes, uint16 length, InputByteStream::Endian endian) {
    ibs = new InputByteStream((uint8*)bytes, length, endian);
}

StreamParser::~StreamParser() {
    delete ibs;
    ibs = NULL;
}
