import os
from pathlib import Path

from build_dependencies.common import run_cmd, find_ndk_path
from build_dependencies.values import LIB_DIR_PATH


def build_native_code():
    JNI_PATH = Path(f"../{LIB_DIR_PATH}").resolve().parents[0]
    os.chdir(JNI_PATH)
    run_cmd([f"{find_ndk_path()}/ndk-build"])


if __name__ == "__main__":
    build_native_code()