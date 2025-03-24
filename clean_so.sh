# -exec rm {} + This option tells find to execute the rm command on the found files. 
# The {} is a placeholder for the file names found by find. 
# The + at the end causes find to try to pass all found files to rm in a single call.
find PdfiumAndroid/src/main/ -type f -name "*.so" -exec rm {} +
