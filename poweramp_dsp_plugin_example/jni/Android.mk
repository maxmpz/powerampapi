LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_ARM_MODE := arm
LOCAL_MODULE := powerampdspexample

LOCAL_SRC_FILES := 

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_CFLAGS := -DHAVE_NEON=1 -Winline -save-temps -O3 -std=gnu99 -mfloat-abi=softfp -mfpu=neon  
    LOCAL_SRC_FILES := plugin.c
endif


LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
