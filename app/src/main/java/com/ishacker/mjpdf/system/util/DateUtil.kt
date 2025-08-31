package com.ishacker.mjpdf.system.util

import android.util.Log
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun convertDateString(input: String): String? {
    try {
        Log.d("TAG", "convertDateString: input=$input ")
        // Step 1: Parse the string to extract date, time, and timezone information
        val dateTimePart = input.substring(2, 16)  // "20230320004150"
        val zonePart = input.substring(16)         // "+00'0'"

        // Step 2: Define the original format (assuming the time is UTC)
        val originalFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        // Step 3: Parse the datetime part
        val dateTime = LocalDateTime.parse(dateTimePart, originalFormatter)

        // Step 4: Handle the timezone part to create a ZonedDateTime
        val offsetHours = zonePart.substring(0, 3).toInt() // "+00"
        val zonedDateTime = dateTime.atOffset(ZoneOffset.ofHours(offsetHours)).toZonedDateTime()

        //val outputFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm '(UTC'x')'")
        val outputFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        return zonedDateTime.format(outputFormatter)
    }
    catch (throwable: Throwable) {
        Log.e("DateUtil", "convertDateString: Failed!", throwable)
        return null
    }
}