cmake_minimum_required(VERSION 3.22)
project(lorie)

set(can_use_assembler TRUE)
enable_language(ASM)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_BUILD_TYPE Release)

include(TestBigEndian)
include(CheckIncludeFile)
include(CheckSourceCompiles)
include(CheckCCompilerFlag)
include(CheckSymbolExists)
include(CheckFunctionExists)
include(CheckTypeSize)

find_package(Python3 QUIET)
# find_package(BISON REQUIRED) # Exigência exclusiva do X11 legado desativada

function(target_apply_patch patch_target d p)
    execute_process(COMMAND "bash" "-c" "patch -p1 -d \"${d}\" -i \"${p}\" -R --dry-run > /dev/null 2>&1 || patch -p1 -N -d \"${d}\" -i \"${p}\" -r -")
endfunction()

set(test_compile_options
        "-O3"
        "-Wall"
        "-Wpointer-arith"
        "-Wmissing-declarations"
        "-Wformat=2"
        "-Winvalid-pch"
        "-Wstrict-prototypes"
        "-Wmissing-prototypes"
        "-Wnested-externs"
        "-Wbad-function-cast"
        "-Wold-style-definition"
        "-Wunused"
        "-Wuninitialized"
        "-Wshadow"
        "-Wmissing-noreturn"
        "-Wmissing-format-attribute"
        "-Wredundant-decls"
        "-Wlogical-op"
        "-Werror=implicit"
        "-Werror=nonnull"
        "-Werror=init-self"
        "-Werror=main"
        "-Werror=missing-braces"
        "-Werror=sequence-point"
        "-Werror=return-type"
        "-Werror=trigraphs"
        "-Werror=array-bounds"
        "-Werror=write-strings"
        "-Werror=address"
        "-Werror=int-to-pointer-cast"
        "-Werror=pointer-to-int-cast"
        "-fno-strict-aliasing"
        "-Wno-unused-but-set-variable"
        "-Wno-unused-parameter"
        "-Wno-unused-variable"
        "-Wno-unused-function"
        "-Wno-missing-prototypes"
        "-Wno-macro-redefined"
        "-Wno-uninitialized"
        "-Wno-shadow")

set(CMAKE_REQUIRED_QUIET_OLD ${CMAKE_REQUIRED_QUIET})
set(CMAKE_REQUIRED_QUIET 1)
set(common_compile_options)
set(I 0)
foreach (option ${test_compile_options})
    check_c_compiler_flag(${option} COMPILER_TEST_${I})
    if (COMPILER_TEST_${I})
        set(common_compile_options ${common_compile_options} ${option})
    endif()
    MATH(EXPR I "${I}+1")
endforeach ()
set(CMAKE_REQUIRED_QUIET ${CMAKE_REQUIRED_QUIET_OLD})

# O servidor X11 legado (lorie/xserver) foi desativado em favor do gerenciador nativo do Android
# foreach(project xorgproto fontenc md pixman tirpc xshmfence Xdmcp Xau Xfont2 xkbcomp xserver)
#     include(recipes/${project}.cmake)
# endforeach()

add_library(windroid SHARED
        micewine/shell_loader.c
        micewine/controller.c
        fexcore/fex_wrapper.cpp
        bridge/native_surface_bridge.c
        bridge/native_ipc_server.c)

target_include_directories(windroid PRIVATE bridge micewine)
target_link_libraries(windroid PUBLIC android log)
