#ifndef JNIGLOBAL_H
#define JNIGLOBAL_H

#include <jni.h>

extern JavaVM *m_jvm;
extern JNIEnv *m_env;

extern jmethodID listAdd;

extern jfieldID fieldBleAdapterPtr;
extern jfieldID fieldBleControllerPtr;
extern jfieldID fieldMessageCallbackPtr;

bool AttachCurrentThread();
void clearException();
void DetachCurrentThread(bool attached);

jobject newList(JNIEnv *env);

#endif //JNIGLOBAL_H
