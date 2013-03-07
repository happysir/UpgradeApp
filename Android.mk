LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

#LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-renderscript-files-under, src)

LOCAL_PACKAGE_NAME := SystemUpdate
#LOCAL_CERTIFICATE := shared
LOCAL_CERTIFICATE := platform

LOCAL_DEX_PREOPT := false

LOCAL_JAVA_LIBRARIES := android.policy

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))