# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("aug12_2")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(distribution_DIR ${CMAKE_SOURCE_DIR}/../../../../distribution)

add_library(nfiq2api STATIC IMPORTED)
set_target_properties(nfiq2api PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/nfiq2api/lib/${ANDROID_ABI}/libNfiq2Api.so)

set(distribution_DIR ${CMAKE_SOURCE_DIR}/../../../../distribution)

add_library(imgproc STATIC IMPORTED)
set_target_properties(imgproc PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/nfiq2api/lib/${ANDROID_ABI}/libopencv_imgproc.a)

add_library(frfxll STATIC IMPORTED)
set_target_properties(frfxll PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/nfiq2api/lib/${ANDROID_ABI}/libFRFXLL.so)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

add_library( # Sets the name of the library.
        aug12_2

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries(aug12_2
        android)
target_link_libraries( # Specifies the target library.
        aug12_2

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
target_include_directories(aug12_2 PRIVATE
        ${distribution_DIR}/nfiq2api/include
        )

target_link_libraries(aug12_2
        nfiq2api
        imgproc
        frfxll
        )
