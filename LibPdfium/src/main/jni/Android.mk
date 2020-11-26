LOCAL_PATH := $(call my-dir)

#Prebuilt libraries
include $(CLEAR_VARS)
LOCAL_MODULE := LibPdfium
ARCH_PATH = $(TARGET_ARCH_ABI)
LOCAL_SRC_FILES := $(LOCAL_PATH)/lib/$(ARCH_PATH)/libpdfium.so
include $(PREBUILT_SHARED_LIBRARY)

#Main JNI library
include $(CLEAR_VARS)
LOCAL_MODULE := pdfium-lib

LOCAL_CFLAGS += -DHAVE_PTHREADS
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_SHARED_LIBRARIES += LibPdfium
LOCAL_LDLIBS += -llog -landroid -ljnigraphics

LOCAL_SRC_FILES :=  $(LOCAL_PATH)/src/mainJNILib.cpp $(LOCAL_PATH)/src/BufferedBlockWriter.cpp

include $(BUILD_SHARED_LIBRARY)
