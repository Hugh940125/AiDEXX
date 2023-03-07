#ifndef MEMUTILS_H
#define MEMUTILS_H

#include "../devcomm/CLibrary/global.h"

class MemUtils {
public:
    static void copy(char *const dst, const char *src, uint32 size);
};


#endif //MEMUTILS_H
