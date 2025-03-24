import os

LIB_EXTENSION = ".so"
TGZ_EXTENSION = ".tgz"
BUILD = "build"
LIBPNG_BUILD = "libpng_build"
FREETYPE_BUILD = "freetype_build"
BUILD_TYPE = "Release"  # Release or Debug
LIB_DIR_PATH = "PdfiumAndroid/src/main/jni/lib"
CMAKE_DIR_PATH = f"{os.getcwd()}/build_dependencies/cmake"


def get_toolchain_path(ndk_path):
    return os.path.join(ndk_path, "build/cmake")


# Toolchain path
NDK_PATH = "/opt/android-ndk/"
DEFAULT_TOOLCHAIN = get_toolchain_path(NDK_PATH)
ANDROID_TOOLCHAIN_FILENAME = "android.toolchain.cmake"
ANDROID_PLATFORM = "19"


class Arch:
    x86_64 = 0
    x86 = 1
    arm64 = 2
    armeabi = 3


class Lib:
    pdfium = 0
    freetype2 = 1
    libpng = 2
    cpp_shared = 3


FILE_NAMES = {
    Lib.pdfium: "libmodpdfium",
    Lib.freetype2: "libmodft2",
    Lib.libpng: "libmodpng",
    Lib.cpp_shared: "libc++_shared",
}

ARCH_NAMES = {
    Arch.x86: "x86",
    Arch.x86_64: "x86_64",
    Arch.arm64: "arm64-v8a",
    Arch.armeabi: "armeabi-v7a",
}

PDFIUM_URLS = {
    # Arch.x86: "https://github.com/bblanchon/pdfium-binaries/releases/latest/download/pdfium-android-x86.tgz",
    # Arch.x86_64: "https://github.com/bblanchon/pdfium-binaries/releases/latest/download/pdfium-android-x64.tgz",
    # Arch.arm64: "https://github.com/bblanchon/pdfium-binaries/releases/latest/download/pdfium-android-arm64.tgz",
    # Arch.armeabi: "https://github.com/bblanchon/pdfium-binaries/releases/latest/download/pdfium-android-arm.tgz",
    Arch.x86: "https://github.com/bblanchon/pdfium-binaries/releases/download/chromium%2F5445/pdfium-android-x86.tgz",
    Arch.x86_64: "https://github.com/bblanchon/pdfium-binaries/releases/download/chromium%2F5445/pdfium-android-x64.tgz",
    Arch.arm64: "https://github.com/bblanchon/pdfium-binaries/releases/download/chromium%2F5445/pdfium-android-arm64.tgz",
    Arch.armeabi: "https://github.com/bblanchon/pdfium-binaries/releases/download/chromium%2F5445/pdfium-android-arm.tgz",
}

PDFIUM_PATHS = {
    Arch.x86: "../../../../bblanchon/pdfium-android-x86.tgz",
    Arch.x86_64: "../../../../bblanchon/pdfium-android-x64.tgz",
    Arch.arm64: "../../../../bblanchon/pdfium-android-arm64.tgz",
    Arch.armeabi: "../../../../bblanchon/pdfium-android-arm.tgz",
}

LIB_CPP_DIR_NAMES = {
    Arch.x86: "i686-linux-android",
    Arch.x86_64: "x86_64-linux-android",
    Arch.arm64: "aarch64-linux-android",
    Arch.armeabi: "arm-linux-androideabi",
}

LIBPNG_URL = "https://sourceforge.net/projects/libpng/files/latest/download"
FREETYPE_URL = "https://sourceforge.net/projects/freetype/files/latest/download"

# LIBPNG_URL = "https://sourceforge.net/projects/libpng/files/libpng16/1.6.37/libpng-1.6.37.tar.xz/download"
# FREETYPE_URL_TEMPLATE = "https://gitlab.freedesktop.org/freetype/freetype/-/archive/VER-{VERSION}/freetype-VER-{VERSION}.tar"
# FREETYPE_URL = "https://gitlab.freedesktop.org/freetype/freetype/-/archive/master/freetype-master.tar.gz"

