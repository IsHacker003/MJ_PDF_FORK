package com.ishacker.mjpdf.system.manager.permission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ishacker.mjpdf.system.BuildConfig
import com.ishacker.mjpdf.system.ui.main.MainActivity


class PermissionManager(private val activity: AppCompatActivity) {

    private lateinit var storageGrantedFunc: () -> Unit

    // -------------- Manage Storage
    fun checkStoragePermission(func: () -> Unit): Boolean {
        storageGrantedFunc = func

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
                requestPermissionLauncher.launch(intent)
            }
            else {
                storageGrantedFunc()
            }
        }
//        else {
//            AlertDialog.Builder(activity)
//                .setCancelable(false)
//                .setTitle("Not Supported")
//                .setMessage("This app doesn't support anything below SDK 30 (Android 10) yet.")
//                .show()
//        }
        return false;
    }

    val requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setTitle("Really?")
                    .setMessage("For real? How can I work right now?!")
                    .setPositiveButton("Ask Again") { _, _ -> checkStoragePermission(storageGrantedFunc) }
                    //.setNegativeButton("I'm stupid") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
            else {
                storageGrantedFunc()
            }
        }
    }

    // -------------- File Picker

    fun launchPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        pdfPicker.launch(intent)
    }

    private val pdfPicker = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val pdfUri = result?.data?.data ?: return@registerForActivityResult

            Intent(activity, MainActivity::class.java).also { intent ->
                //intent.putExtra("pdfUri", pdfUri.toString())
                intent.data = pdfUri
                activity.startActivity(intent)
            }
        }
    }
}