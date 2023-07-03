cmake_minimum_required(VERSION 3.4.1)

#设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/jniLibs/${ANDROID_ABI})

#.h文件目录
include_directories(
        ${CMAKE_SOURCE_DIR}/src/main/cpp #h文件目录
)

add_library( # Sets the name of the library.
        cgat-lib                     #c/cpp代码将要编译成为so库的名称，java代码加载库文件要用这个名称
        SHARED                          #动态库
        src/main/cpp/com_microtechmd_cgat_CGA.c
        src/main/cpp/abs.c
        src/main/cpp/AAC.c
        src/main/cpp/ADRR.c
        src/main/cpp/AUC.c
        src/main/cpp/combineVectorElements.c
        src/main/cpp/CONGA.c
        src/main/cpp/CV.c
        src/main/cpp/datools_emxAPI.c
        src/main/cpp/datools_emxutil.c
        src/main/cpp/datools_initialize.c
        src/main/cpp/datools_rtwutil.c
        src/main/cpp/datools_terminate.c
        src/main/cpp/DailyTrendMean.c
        src/main/cpp/DailyTrendPrctile.c
        src/main/cpp/datools_terminate.c
        src/main/cpp/diff.c
        src/main/cpp/exp.c
        src/main/cpp/GRADE.c
        src/main/cpp/HbA1c.c
        src/main/cpp/HBGD.c
        src/main/cpp/HBGI.c
        src/main/cpp/IQR.c
        src/main/cpp/JINDEX.c
        src/main/cpp/LAGE.c
        src/main/cpp/LBGD.c
        src/main/cpp/LBGI.c
        src/main/cpp/log.c
        src/main/cpp/log10.c
        src/main/cpp/MAG.c
        src/main/cpp/MAGE.c
        src/main/cpp/MAXBG.c
        src/main/cpp/MBG.c
        src/main/cpp/MINBG.c
        src/main/cpp/MODD.c
        src/main/cpp/MValue.c
        src/main/cpp/nanmean.c
        src/main/cpp/nanstd.c
        src/main/cpp/nansum.c
        src/main/cpp/neg2nan.c
        src/main/cpp/nullAssignment.c
        src/main/cpp/NUM.c
        src/main/cpp/Pentagon.c
        src/main/cpp/power.c
        src/main/cpp/prctile.c
        src/main/cpp/PT.c
        src/main/cpp/rdivide.c
        src/main/cpp/rt_nonfinite.c
        src/main/cpp/rtGetInf.c
        src/main/cpp/rtGetNaN.c
        src/main/cpp/SAFilter.c
        src/main/cpp/SDBG.c
        src/main/cpp/SelectByHour.c
        src/main/cpp/sign.c
        src/main/cpp/sort1.c
        src/main/cpp/sortIdx.c
        src/main/cpp/sqrt.c
        src/main/cpp/sum.c
        )

find_library( # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        cgat-lib
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

