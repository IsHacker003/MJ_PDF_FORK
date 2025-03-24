import os

from build_dependencies.common import log
from build_dependencies.pdfium import fetch_prebuilt_pdfium
from build_dependencies.libpng import build_libpng_libs
from build_dependencies.freetype2 import build_freetype_libs
from build_dependencies.shared_cpp_lib import copy_shared_cpp_libs
from build_dependencies.native_code import build_native_code
from build_dependencies.values import LIB_DIR_PATH

log("Start " + __file__)
log("INSTALL_PATH: " + LIB_DIR_PATH)

os.chdir("build_dependencies")
fetch_prebuilt_pdfium()
build_libpng_libs()
build_freetype_libs()
copy_shared_cpp_libs()
build_native_code()