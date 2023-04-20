cmake_minimum_required(VERSION 3.4.1)

#设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/jniLibs/${ANDROID_ABI})
set(CMAKE_OSX_ARCHITECTURES "arm-v7a")
#.h文件目录
include_directories(
        ${CMAKE_SOURCE_DIR}/src/main/cpp/jni
)

add_library( # Sets the name of the library.
        blecomm-lib                     #c/cpp代码将要编译成为so库的名称，java代码加载库文件要用这个名称
        SHARED                          #动态库
        src/main/cpp/jni/jni_global.cpp
        src/main/cpp/jni/com_microtechmd_blecomm_BleAdapter.cpp
        src/main/cpp/jni/com_microtechmd_blecomm_controller_BleController.cpp
        src/main/cpp/jni/com_microtechmd_blecomm_controller_BgmController.cpp
        src/main/cpp/jni/com_microtechmd_blecomm_controller_CgmController.cpp
        src/main/cpp/jni/com_microtechmd_blecomm_controller_AidexXController.cpp
        src/main/cpp/jni/com_microtechmd_blecomm_controller_PumpController.cpp
        src/main/cpp/jni/com_microtechmd_blecomm_parser_CgmParser.cpp
        src/main/cpp/jni/com_microtechmd_blecomm_parser_PumpParser.cpp
        src/main/cpp/jni/com_microtechmd_blecomm_parser_BgmParser.cpp
        src/main/cpp/jni/com_microtechmd_blecomm_parser_AidexXParser.cpp
        src/main/cpp/controller/bgm/bgmcontroller.cpp
        src/main/cpp/controller/cgm/cgmcontroller.cpp
        src/main/cpp/controller/cgm/aidexxcontroller.cpp
        src/main/cpp/controller/pump/pumpcontroller.cpp
        src/main/cpp/controller/blecontroller.cpp
        src/main/cpp/devcomm/devcommclass.cpp
        src/main/cpp/parser/cgm/cgmbroadcastparser.cpp
        src/main/cpp/parser/cgm/cgmdefaultparamparser.cpp
        src/main/cpp/parser/cgm/cgmdevicecheckparser.cpp
        src/main/cpp/parser/cgm/cgmdeviceparser.cpp
        src/main/cpp/parser/cgm/cgmhistoriesparser.cpp
        src/main/cpp/parser/cgm/cgmhistoryparser.cpp
        src/main/cpp/parser/pump/pumphistoryparser.cpp
        src/main/cpp/parser/pump/pumpbroadcastparser.cpp
        src/main/cpp/parser/pump/pumpbolusprofileparser.cpp
        src/main/cpp/parser/pump/pumpbasalprofileparser.cpp
        src/main/cpp/util/memutils.cpp
        src/main/cpp/util/byteutils.cpp
        src/main/cpp/util/ctimer.cpp
        src/main/cpp/util/inputbytestream.cpp
        src/main/cpp/ble.cpp
        src/main/cpp/util/md5.cpp
        src/main/cpp/util/encryptor/aesencryptor.cpp
        src/main/cpp/devcomm/CLibrary/lib_aes.c
        src/main/cpp/devcomm/CLibrary/driver/drv.c
        src/main/cpp/devcomm/CLibrary/driver/drv_uart.c
        src/main/cpp/devcomm/CLibrary/aes.c
        src/main/cpp/devcomm/CLibrary/devcomm.c
        src/main/cpp/devcomm/CLibrary/lib_checksum.c
        src/main/cpp/devcomm/CLibrary/lib_frame.c
        src/main/cpp/devcomm/CLibrary/lib_queue.c
        src/main/cpp/devcomm/CLibrary/task_comm.c
        src/main/cpp/parser/pump/pumpdeviceparser.cpp
        src/main/cpp/parser/streamparser.cpp
        src/main/cpp/parser/numberparser.cpp
        src/main/cpp/parser/cgm/aidexxbroadcastparser.cpp
        src/main/cpp/parser/cgm/aidexxcalibrationsparser.cpp
        src/main/cpp/parser/cgm/aidexxdatetimeparser.cpp
        src/main/cpp/parser/cgm/aidexxdefaultparamsparser.cpp
        src/main/cpp/parser/cgm/aidexxfullbroadcastparser.cpp
        src/main/cpp/parser/cgm/aidexxscanresponseparser.cpp
        src/main/cpp/parser/cgm/aidexxhistoriesparser.cpp
        src/main/cpp/parser/bgm/bgmdeviceparser.cpp
        src/main/cpp/parser/bgm/bgmhistoryparser.cpp
        src/main/cpp/parser/bgm/bgmstateparser.cpp
        src/main/cpp/parser/pump/pumpbasalprofileparser.cpp
        src/main/cpp/parser/pump/pumpbolusprofileparser.cpp

        )

find_library( # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        blecomm-lib
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

