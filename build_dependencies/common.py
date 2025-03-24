import os
import shutil
import subprocess
import tarfile

import requests

from build_dependencies.values import LIB_DIR_PATH, ARCH_NAMES, DEFAULT_TOOLCHAIN, ANDROID_TOOLCHAIN_FILENAME, \
    get_toolchain_path, FILE_NAMES, Lib

alternative_cpp_path = ""


# ------------------------------------------------------------
def run_cmd(cmd):
    command = " ".join(cmd)
    log("Run: " + command)
    result = subprocess.call(command, shell=True)
    if result != 0:
        error(command + "  result=" + str(result))


def error(msg):
    log("Error !!! " + msg)
    os.sys.exit(0)


def log(msg):
    print("* " + msg)


def delete_if_exists(path):
    if os.path.exists(path):
        shutil.rmtree(path)


def delete_file_if_exists(path):
    if os.path.exists(path):
        os.remove(path)


def download_file(url, filename=None, show_done_message=False):
    if not filename:
        filename = url.split('/')[-1] + ".tar.xz"

    print(f"* Downloading {filename}")
    with requests.get(url, stream=True) as request:
        request.raise_for_status()

        with open(filename, 'wb') as file:
            for chunk in request.iter_content(chunk_size=1024):
                file.write(chunk)

    if show_done_message:
        print(f"* Finished downloading {filename}")
    return filename


def extract_tar_file(filename, path=".", show_done_message=False):
    log(f"Extracting {filename}")
    with tarfile.open(filename) as file:
        file.extractall(path)

    if show_done_message:
        log(f"Finished extracting {filename}")


def move_file(filename, target):
    os.makedirs(target, exist_ok=True)
    shutil.move(filename, f"{target}/{filename}")


def get_lib_path(arch, lib_filename, levels_up=2):
    return f"{'../' * levels_up}{LIB_DIR_PATH}/{ARCH_NAMES[arch]}/{lib_filename}"


def get_toolchain():
    path = os.path.join(DEFAULT_TOOLCHAIN, ANDROID_TOOLCHAIN_FILENAME)
    if os.path.exists(path):
        return path

    log(f"Searching for {ANDROID_TOOLCHAIN_FILENAME}")
    toolchain_path = get_toolchain_path(find_ndk_path())
    return os.path.join(toolchain_path, ANDROID_TOOLCHAIN_FILENAME)


def find_ndk_path():
    try:
        return os.environ["ANDROID_NDK"]
    except KeyError:
        error(f"ANDROID_NDK env variable is empty. Thus, can't locate {ANDROID_TOOLCHAIN_FILENAME}\n"
              f'Hint: Try running: find / -name "*android.toolchain.cmake*" 2>/dev/null\n'
              f'      Then run ANDROID_NDK=SOME_PATH/android-ndk-SOMETHING/android-ndk-SOME_VERSION python build_dependencies.py')
        exit(1)


def get_shared_cpp_libs_path():
    global alternative_cpp_path

    path = os.path.join(find_ndk_path(), "toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/lib")
    if os.path.exists(path):
        return path

    if alternative_cpp_path != "":
        return alternative_cpp_path

    log(f"Couldn't find {FILE_NAMES[Lib.cpp_shared]} libs at {path}")
    log("Hint: you can try yo find the path  using 'find / -name libc++_shared.so 2>/dev/null'")
    alternative_cpp_path = input("Enter the path manually: ")
    if os.path.exists(alternative_cpp_path):
        return alternative_cpp_path

    error(f"Couldn't find the path you entered: {alternative_cpp_path}")
    exit(1)
