import os
import shutil

from build_dependencies.common import download_file, extract_tar_file, log, delete_if_exists, delete_file_if_exists, \
    get_lib_path
from build_dependencies.values import Arch, PDFIUM_URLS, FILE_NAMES, LIB_EXTENSION, Lib, TGZ_EXTENSION, BUILD, \
    ARCH_NAMES

DOWNLOADED_LIB_PATH = "./lib/libpdfium.so"


def fetch_prebuilt_pdfium():
    for arch in [Arch.x86, Arch.x86_64, Arch.arm64, Arch.armeabi]:
        log(f"Fetching PDFium for {ARCH_NAMES[arch]}.")
        os.makedirs(BUILD, exist_ok=True)
        os.chdir(BUILD)

        filename = FILE_NAMES[Lib.pdfium] + TGZ_EXTENSION
        download_file(PDFIUM_URLS[arch], filename)
        extract_tar_file(filename)

        lib_filename = FILE_NAMES[Lib.pdfium] + LIB_EXTENSION
        lib_path = get_lib_path(arch, lib_filename)

        delete_file_if_exists(lib_path)
        log("Cleared any old lib.")

        shutil.move(DOWNLOADED_LIB_PATH, lib_path)
        log("Moved the new lib to the target dir.")

        os.chdir("../")
        delete_if_exists(f"{BUILD}")
        log(f"Finished fetching PDFium for {ARCH_NAMES[arch]}.")
        log(f"------------------------------------")


if __name__ == "__main__":
    fetch_prebuilt_pdfium()