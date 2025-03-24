import shutil

from build_dependencies.common import log, get_lib_path, delete_file_if_exists, get_shared_cpp_libs_path
from build_dependencies.values import Arch, ARCH_NAMES, FILE_NAMES, LIB_EXTENSION, Lib, \
    LIB_CPP_DIR_NAMES

LIB_FILENAME = FILE_NAMES[Lib.cpp_shared] + LIB_EXTENSION


def copy_shared_cpp_libs():
    for arch in [Arch.x86_64, Arch.arm64, Arch.x86, Arch.armeabi]:
        log(f"Copy {LIB_FILENAME} for {ARCH_NAMES[arch]}.")

        target_lib_path = get_lib_path(arch, LIB_FILENAME, levels_up=1)

        delete_file_if_exists(target_lib_path)
        log("Cleared any old lib.")

        source_lib_path = f"{get_shared_cpp_libs_path()}/{LIB_CPP_DIR_NAMES[arch]}/{LIB_FILENAME}"
        log(f"Copying from:{source_lib_path}")

        shutil.copy(source_lib_path, target_lib_path)
        log("Copied successfully any old lib.")


if __name__ == "__main__":
    copy_shared_cpp_libs()
