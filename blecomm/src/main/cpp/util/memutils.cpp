#include "memutils.h"

void MemUtils::copy(char *dst, const char *src, uint32 size) {
    while (size-- > 0)
        *dst++ = *src++;
}
