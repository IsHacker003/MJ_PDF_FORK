import glob
import os
import shutil

from build_dependencies.common import download_file, extract_tar_file, log, delete_if_exists, \
    delete_file_if_exists, get_lib_path, run_cmd, get_toolchain
from build_dependencies.values import Arch, FILE_NAMES, LIB_EXTENSION, Lib, ARCH_NAMES, LIBPNG_URL, LIBPNG_BUILD, BUILD, \
    ANDROID_PLATFORM, BUILD_TYPE

BUILT_LIB_NAME = "libpng16.so"


def build_libpng_libs():
    log(f"Downloading libpng source code.")
    os.makedirs(LIBPNG_BUILD, exist_ok=True)
    os.chdir(LIBPNG_BUILD)
    LIBPNG_ROOT_PATH = os.getcwd()

    temp_file_name = "libpng.tar.gz"
    download_file(LIBPNG_URL, temp_file_name)
    extract_tar_file(temp_file_name)
    delete_file_if_exists(temp_file_name)

    # get file name (different for each version)
    files = glob.glob("./libpng-*")
    os.chdir(files[0])  # there should be only one file

    for arch in [Arch.x86_64, Arch.arm64, Arch.x86, Arch.armeabi]:
        log(f"Building libpng for {ARCH_NAMES[arch]}.")

        # create build folder
        delete_if_exists(BUILD)
        os.makedirs(BUILD, exist_ok=True)
        os.chdir(BUILD)

        build_libpng(arch, LIBPNG_ROOT_PATH)
        log(f"Finished building {ARCH_NAMES[arch]}")

        lib_filename = FILE_NAMES[Lib.libpng] + LIB_EXTENSION
        lib_path = get_lib_path(arch, lib_filename, levels_up=3)

        os.chdir("../")
        delete_file_if_exists(lib_path)
        log("Deleted any old lib.")

        downloaded_lib_path = f"{BUILD}/{BUILT_LIB_NAME}"
        shutil.move(downloaded_lib_path, lib_path)
        log("Moved the new lib to the target dir.")

        log(f"Finished libpng for {ARCH_NAMES[arch]}.")
        log(f"------------------------------------")

    os.chdir("../../")


def build_libpng(arch, LIBPNG_DIR):
    INSTALL_PREFIX = os.path.abspath(os.path.join(LIBPNG_DIR, "lib", ARCH_NAMES[arch]))
    # cmake generator
    cmd = ["cmake"]
    cmd += ["-DCMAKE_TOOLCHAIN_FILE=" + get_toolchain()]
    cmd += ["-DANDROID_ABI=" + ARCH_NAMES[arch]]
    cmd += ["-DANDROID_PLATFORM=" + ANDROID_PLATFORM]
    cmd += ["-DCMAKE_INSTALL_PREFIX=" + INSTALL_PREFIX]
    cmd += ["-DPNG_TESTS=OFF -DHAVE_LD_VERSION_SCRIPT=OFF"]

    if BUILD_TYPE == "Debug":
        cmd += ["-DPNG_DEBUG=ON"]
    cmd += [".."]
    run_cmd(cmd)

    # cmake build
    cmd = ["cmake --build ."]
    cmd += ["--config " + BUILD_TYPE]
    run_cmd(cmd)

    # cmake install
    cmd = ["cmake --install ."]
    cmd += ["--config " + BUILD_TYPE]
    run_cmd(cmd)



if __name__ == "__main__":
    build_libpng_libs()
