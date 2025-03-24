package com.gitlab.mudlej.MjPdfReader.manager.storage

import android.app.Activity
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.util.computeHash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.SortedMap
import kotlin.system.measureTimeMillis

class StorageManager {

    private var filesMap = sortedMapOf<String, File>()

    suspend fun scanPdfFilesWithHash(activity: Activity): SortedMap<String, File> {
        return withContext(Dispatchers.IO) {
            val time = measureTimeMillis {
                readAllFiles()
                    .filter { file -> file.extension == PDF_EXTENSION }
                    .forEach { file ->
                        val hash = computeHash(activity, PDF(uri = file.toUri()))
                        filesMap[hash] = file
                    }
            }
            Log.d(TAG, "scanPdfFilesWithHash: timeElapsed: ${time / 1000F}s")
            printFilesMap()
            return@withContext filesMap
        }
    }

    private fun readAllFiles(): FileTreeWalk {
        return File(ROOT_DIR).walk()
            .onEnter { file ->                        // before entering this dir check if
                !file.isHidden                             // it is not hidden
                && file != ANDROID_DIR                     // it is not Android directory
                && file != DATA_DIR                        // it is not data directory
                && !File(file, ".nomedia").exists()   // there is no .nomedia file inside
            }
    }

    private fun printFilesMap() {
        Log.d(
            TAG,
            "scanPdfFilesWithHash pairs: ${filesMap
                .map { " \"${it.key}\" to \"${it.value}\"" }
                .joinToString(", \n")
            }"
        )
    }

    companion object {

        private val TAG = StorageManager::class.java.simpleName

        val ROOT_DIR = Environment.getExternalStorageDirectory().absolutePath
        private val ANDROID_DIR = File("$ROOT_DIR/Android")
        private val DATA_DIR = File("$ROOT_DIR/data")

        const val PDF_EXTENSION = "pdf"
    }
}