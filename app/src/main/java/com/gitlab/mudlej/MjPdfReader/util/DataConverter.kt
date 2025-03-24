package com.gitlab.mudlej.MjPdfReader.util

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDateTime

class DataConverter {

    private val gson = Gson()

    @TypeConverter fun fromUri(uri: Uri?): String? = uri?.toString()
    @TypeConverter fun toUri(json: String?): Uri? = Uri.parse(json)

    @TypeConverter fun fromFile(file: File?): String? = gson.toJson(file)
    @TypeConverter fun toFile(json: String?): File? = gson.fromJson(json, object : TypeToken<File>() {}.type)

    @TypeConverter fun fromFileList(file: List<File>?): String? = gson.toJson(file)
    @TypeConverter fun toFileList(json: String?): List<File>? = gson.fromJson(json, object : TypeToken<List<File>>() {}.type)

    @TypeConverter fun fromBitmap(obj: Bitmap?): String? = gson.toJson(obj)
    @TypeConverter fun toBitmap(json: String?): Bitmap? = gson.fromJson(json, object : TypeToken<Bitmap>() {}.type)

    @TypeConverter fun fromLocalDateTime(obj: LocalDateTime?): String = obj.toString()
    @TypeConverter fun toLocalDateTime(json: String?): LocalDateTime? {
        return LocalDateTime.parse(json)
        //return gson.fromJson(json, object : TypeToken<LocalDateTime>() {}.type)
    }
}