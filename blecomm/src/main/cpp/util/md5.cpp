#include "md5.h"
#include <string.h>

typedef unsigned char BYTE;
typedef unsigned int UINT;
typedef UINT MD5_SUB_ARRAY[16];
typedef UINT MD5_TRANSORM_FUNC(UINT, UINT, UINT);
typedef struct
{
   UINT abcd[4];
   MD5_SUB_ARRAY sub_array;
} MD5_TRANSFORM_PARAM;

const static UINT MD5_TRANSFORM_MATRIX[4][16][3] = {
    {
        {0, 7, 1},
        {1, 12, 2},
        {2, 17, 3},
        {3, 22, 4},
        {4, 7, 5},
        {5, 12, 6},
        {6, 17, 7},
        {7, 22, 8},
        {8, 7, 9},
        {9, 12, 10},
        {10, 17, 11},
        {11, 22, 12},
        {12, 7, 13},
        {13, 12, 14},
        {14, 17, 15},
        {15, 22, 16},
    },

    {
        {1, 5, 17},
        {6, 9, 18},
        {11, 14, 19},
        {0, 20, 20},
        {5, 5, 21},
        {10, 9, 22},
        {15, 14, 23},
        {4, 20, 24},
        {9, 5, 25},
        {14, 9, 26},
        {3, 14, 27},
        {8, 20, 28},
        {13, 5, 29},
        {2, 9, 30},
        {7, 14, 31},
        {12, 20, 32},
    },

    {
        {5, 4, 33},
        {8, 11, 34},
        {11, 16, 35},
        {14, 23, 36},
        {1, 4, 37},
        {4, 11, 38},
        {7, 16, 39},
        {10, 23, 40},
        {13, 4, 41},
        {0, 11, 42},
        {3, 16, 43},
        {6, 23, 44},
        {9, 4, 45},
        {12, 11, 46},
        {15, 16, 47},
        {2, 23, 48},
    },

    {
        {0, 6, 49},
        {7, 10, 50},
        {14, 15, 51},
        {5, 21, 52},
        {12, 6, 53},
        {3, 10, 54},
        {10, 15, 55},
        {1, 21, 56},
        {8, 6, 57},
        {15, 10, 58},
        {6, 15, 59},
        {13, 21, 60},
        {4, 6, 61},
        {11, 10, 62},
        {2, 15, 63},
        {9, 21, 64},
    },
};

const static UINT MD5_TRANSFORM_ARRAY[65] = {
    0, 0xd76aa478, 0xe8c7b756, 0x242070db, 0xc1bdceee, 0xf57c0faf,
    0x4787c62a, 0xa8304613, 0xfd469501, 0x698098d8, 0x8b44f7af, 0xffff5bb1,
    0x895cd7be, 0x6b901122, 0xfd987193, 0xa679438e, 0x49b40821, 0xf61e2562,
    0xc040b340, 0x265e5a51, 0xe9b6c7aa, 0xd62f105d, 0x2441453, 0xd8a1e681,
    0xe7d3fbc8, 0x21e1cde6, 0xc33707d6, 0xf4d50d87, 0x455a14ed, 0xa9e3e905,
    0xfcefa3f8, 0x676f02d9, 0x8d2a4c8a, 0xfffa3942, 0x8771f681, 0x6d9d6122,
    0xfde5380c, 0xa4beea44, 0x4bdecfa9, 0xf6bb4b60, 0xbebfbc70, 0x289b7ec6,
    0xeaa127fa, 0xd4ef3085, 0x4881d05, 0xd9d4d039, 0xe6db99e5, 0x1fa27cf8,
    0xc4ac5665, 0xf4292244, 0x432aff97, 0xab9423a7, 0xfc93a039, 0x655b59c3,
    0x8f0ccc92, 0xffeff47d, 0x85845dd1, 0x6fa87e4f, 0xfe2ce6e0, 0xa3014314,
    0x4e0811a1, 0xf7537e82, 0xbd3af235, 0x2ad7d2bb, 0xeb86d391};

static inline UINT F(UINT x, UINT y, UINT z) { return ((x & y) | ((~x) & z)); }
static inline UINT G(UINT x, UINT y, UINT z) { return ((x & z) | (y & (~z))); }
static inline UINT H(UINT x, UINT y, UINT z) { return (x ^ y ^ z); }
static inline UINT I(UINT x, UINT y, UINT z) { return (y ^ (x | (~z))); }

static void MD5_transform(MD5_TRANSFORM_PARAM *param, int ring, MD5_TRANSORM_FUNC func)
{
   UINT a, b, c, d, s, k, i;
   UINT *abcd = param->abcd;
   UINT *X;
   const UINT *T;
   int index;
   X = param->sub_array;
   T = MD5_TRANSFORM_ARRAY;

   for (index = 0; index < 16; index++)
   {
      a = abcd[(3 * index + 0) & 3];
      b = abcd[(3 * index + 1) & 3];
      c = abcd[(3 * index + 2) & 3];
      d = abcd[(3 * index + 3) & 3];

      k = MD5_TRANSFORM_MATRIX[ring][index][0];
      s = MD5_TRANSFORM_MATRIX[ring][index][1];
      i = MD5_TRANSFORM_MATRIX[ring][index][2];

      a = a + func(b, c, d) + X[k] + T[i];
      a = (a << s) | (a >> (32 - s));
      a = a + b;

      abcd[(3 * index + 0) & 3] = a;
   }
}

void MD5::digest(const unsigned char *data, int len, unsigned char *md5Out)
{
   int x, new_len;
   MD5_TRANSFORM_PARAM param;
   UINT ABCD[4] = {0x67452301L, 0xefcdab89L, 0x98badcfeL, 0x10325476L};

   new_len = (((len + 8) >> 6) + 1) << 6;
   unsigned char *ts = (unsigned char *)param.sub_array;
   for (x = 0; x < new_len; x += 64)
   {
      memcpy(param.abcd, ABCD, 16);
      if (x + 64 <= len)
      {
         memcpy(ts, &data[x], 64);
      }
      else
      {
         memcpy(ts, &data[x], len - x);
         if (new_len - len > 9)
         {
            ts[len - x] = 0x80;
         }
         memset(&ts[len - x + 1], 0, new_len - len - 9);
         *(UINT *)(ts + 56) = len * 8;
         memset(ts + 60, 0, 4);
      }
      MD5_transform(&param, 0, F);
      MD5_transform(&param, 1, G);
      MD5_transform(&param, 2, H);
      MD5_transform(&param, 3, I);

      ABCD[0] += param.abcd[0];
      ABCD[1] += param.abcd[1];
      ABCD[2] += param.abcd[2];
      ABCD[3] += param.abcd[3];
   }
   memcpy(md5Out, ABCD, 16);
}
