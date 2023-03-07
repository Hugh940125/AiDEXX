#ifndef ARRAY
#define ARRAY

template<class T>
int arrayLen(T &array) {
    return sizeof(array) / sizeof(array[0]);
}

#endif // ARRAY

