package com.ishacker.mjpdf.system.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.ishacker.mjpdf.system.data.PdfData
import com.shockwave.pdfium.PdfiumCore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object FileUtil {

    fun storeBitmap(context: Context, bitmap: Bitmap): File {
        val fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
        val extension = ".png"  // TODO: Extract file extension
        val file = File(context.filesDir, fileName + extension)
        context.contentResolver.openOutputStream(file.toUri())?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)    // 100 is full quality
        }
        return file
    }

    // This is a problematic function
    fun getSizeInMb(file: File): Double {
        return file.length().toDouble() / (1024 * 1024);
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }


    /**
     * Return PdfData which has the length of the PDF and an image of a page number, the cover if not specified
     */
    fun getPdfData(pdfium: PdfiumCore, uri: Uri, pageNumber: Int = 0, reduceSize: Boolean = true): PdfData? {
        try {
            val fd = ParcelFileDescriptor.open(uri.toFile(), ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfDocument = pdfium.newDocument(fd)
            pdfium.openPage(pdfDocument, pageNumber)
            val length = pdfium.getPageCount(pdfDocument)

            val width = pdfium.getPageWidthPoint(pdfDocument, pageNumber)
            val height = pdfium.getPageHeightPoint(pdfDocument, pageNumber)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            pdfium.renderPageBitmap(pdfDocument, bitmap, pageNumber, 0, 0, width, height)
            pdfium.closeDocument(pdfDocument)

            val cover = if (reduceSize) {
                getResizedBitmap(bitmap, 800)
            } else {
                bitmap
            }
            return PdfData(cover, length)
        }
        catch(throwable: Throwable) {
            Log.e("FileUtils", "getPdfData: Error while trying to get the data about PDF", throwable)
            return null
        }
    }

    /*
    * content:// URIs point to data managed by a Content Provider.
    * This data may be stored in different forms (such as files, assets, SQLite databases)
    *  and isn't accessible directly as a file path but through the ContentResolver system.
    *
    * This code snippet reads data from the provided content:// URI
    * and writes it to a temporary file in your application's cache directory.
    * */
    @Throws(IOException::class)
    fun fileFromUri(context: Context, uri: Uri, fileName: String = "temp-file.pdf"): File? {
        val contentResolver = context.contentResolver

        // Open an InputStream from the URI using ContentResolver
        val inputStream = contentResolver.openInputStream(uri)

        inputStream?.let {
            val file = File(context.cacheDir, fileName )
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
            return file
        }
        return null
    }
}