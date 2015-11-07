#include <jni.h>
#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <limits.h>

#include <android/log.h>
#include <arm_neon.h>

#define  LOG_TAG    "plugin.c"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)

int registerNativeMethods(JNIEnv* env, const char* className, JNINativeMethod* gMethods, int numMethods) {
    jclass clazz = (*env)->FindClass(env, className);
    if (clazz == NULL) return JNI_FALSE;
    if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0) return JNI_FALSE;
    return JNI_TRUE;
}

static JNINativeMethod native_methods[] = { /* name, signature, funcPtr */
//	{"native_empty_test_via_register", "(I)I", (void*)native_empty_test_via_register},
//	{"native_static_empty_test_via_register", "!(I)I", (void*)native_static_empty_test_via_register},
};

__attribute__ ((visibility("default"))) jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = JNI_ERR;

    if((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("GetEnv failed!");
        goto end;
    }

//    if(!registerNativeMethods(env, "com/maxmpz/neontests/NeonActivity", native_methods, sizeof(native_methods) / sizeof(native_methods[0]))) {
//    	LOGE("registerNativeMethods failed");
//    	goto end;
//    }

    LOGE("LOADED!");

    result = JNI_VERSION_1_6;
end:
    return result;
}

__attribute__ ((visibility("default"))) void JNI_OnUnload(JavaVM* vm, void* reserved) {
}


