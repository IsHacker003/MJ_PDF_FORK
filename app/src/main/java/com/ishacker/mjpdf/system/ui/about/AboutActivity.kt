/*
 *   MJ PDF
 *   Copyright (C) 2023 Mudlej
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *  --------------------------
 *  This code was previously licensed under
 *
 *  MIT License
 *
 *  Copyright (c) 2018 Gokul Swaminathan
 *  Copyright (c) 2023 Mudlej
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.ishacker.mjpdf.system.ui.about

import android.R
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.franmontiel.attributionpresenter.AttributionPresenter
import com.franmontiel.attributionpresenter.entities.Attribution
import com.franmontiel.attributionpresenter.entities.License
import com.ishacker.mjpdf.system.BuildConfig
import com.ishacker.mjpdf.system.databinding.ActivityAboutBinding
import com.ishacker.mjpdf.system.ui.showAppFeaturesDialog
import com.ishacker.mjpdf.system.util.ColorUtil
import com.ishacker.mjpdf.system.util.emailIntent
import com.ishacker.mjpdf.system.util.getAppVersion
import com.ishacker.mjpdf.system.util.linkIntent
import com.ishacker.mjpdf.system.util.navIntent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    
    private val APP_VERSION_RELEASE = "Version " + getAppVersion()
    private val APP_VERSION_DEBUG = "Version " + getAppVersion() + "-debug"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        initUi()
    }

    private fun initUi() {
        setVersionText()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ColorUtil.colorize(this, window, supportActionBar)
    }

    private fun setVersionText() {
        // check if app is debug
        if (BuildConfig.DEBUG) {
            binding.versionTextView.text = APP_VERSION_DEBUG
        } else {   //if app is release
            binding.versionTextView.text = APP_VERSION_RELEASE
        }
    }

    fun showLog(v: View?) {
        showAppFeaturesDialog(this)
    }

    fun showPrivacy(v: View?) {
        PrivacyInfoDialog().show(supportFragmentManager, "privacy_dialog")
    }

    fun showLicense(v: View?) {
        startActivity(
            linkIntent("https://gitlab.com/mudlej_android/mj_pdf_reader/-/blob/main/LICENSE")
        )
    }

    fun showLibraries(v: View?) {
        val attributionPresenter = AttributionPresenter.Builder(this)
            .addAttributions(
                Attribution.Builder("AttributionPresenter")
                    .addCopyrightNotice("Copyright 2017 Francisco José Montiel Navarro")
                    .addLicense(License.APACHE)
                    .setWebsite("https://github.com/franmontiel/AttributionPresenter")
                    .build()
            )
            .addAttributions(
                Attribution.Builder("MJ PDF's fork of Android PdfViewer")
                    .addCopyrightNotice("Forked by Mudlej")
                    .addLicense(License.APACHE)
                    .setWebsite("https://gitlab.com/mudlej_android/mj_pdf_reader/-/tree/main/AndroidPdfViewer")
                    .build()
            )
            .addAttributions(
                Attribution.Builder("MJ PDF's fork of PdfiumAndroid")
                    .addCopyrightNotice("Forked by Mudlej")
                    .addLicense(License.APACHE)
                    .setWebsite("https://gitlab.com/mudlej_android/mj_pdf_reader/-/tree/main/AndroidPdfViewer")
                    .build()
            )
            .addAttributions(
                Attribution.Builder("AppIntro")
                    .addCopyrightNotice("Copyright 2018 Paolo Rotolo")
                    .addLicense(License.APACHE)
                    .setWebsite("https://github.com/paolorotolo/AppIntro")
                    .build()
            )
            .addAttributions(
                Attribution.Builder("Android Open Source Project")
                    .addCopyrightNotice("Copyright 2016 The Android Open Source Project")
                    .addLicense(License.APACHE)
                    .setWebsite("http://developer.android.com/tools/support-library/index.html")
                    .build()
            )
            .addAttributions(
                Attribution.Builder("Android Support Libraries")
                    .addCopyrightNotice("Copyright 2016 The Android Open Source Project")
                    .addLicense(License.APACHE)
                    .setWebsite("http://developer.android.com/tools/support-library/index.html")
                    .build()
            )
            .addAttributions(
                Attribution.Builder("Material Design Icons")
                    .addCopyrightNotice("Copyright 2014, Austin Andrews")
                    .addLicense(
                        "SIL Open Font",
                        "https://github.com/Templarian/MaterialDesign/blob/master/LICENSE"
                    )
                    .setWebsite("https://materialdesignicons.com/")
                    .build()
            )
            .addAttributions(
                Attribution.Builder(" ColorPicker")
                    .addCopyrightNotice("Copyright 2018 Hong Duan")
                    .addLicense(License.APACHE)
                    .setWebsite("https://materialdesignicons.com/")
                    .build()
            )
            .build()

        //show license dialogue
        attributionPresenter.showDialog("Open Source Libraries")
    }

    fun emailDev(v: View?) {
        val email = "mudlej@proton.me"
        try {
            startActivity(emailIntent(
                email,
                getString(com.ishacker.mjpdf.system.R.string.mj_app_name),
                APP_VERSION_RELEASE
            ))
        } catch (e: ActivityNotFoundException) {
            //Toast.makeText(this, email, Toast.LENGTH_SHORT).show()
            Snackbar.make(binding.root, email, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun navToGit(v: View?) {
        startActivity(linkIntent("https://gitlab.com/mudlej"))
    }

    fun navToSourceCode(v: View?) {
        startActivity(linkIntent("https://github.com/IsHacker003/MJ_PDF_FORK"))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    class PrivacyInfoDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = MaterialAlertDialogBuilder(requireContext())
            return builder.setTitle(com.ishacker.mjpdf.system.R.string.privacy)
                .setMessage(com.ishacker.mjpdf.system.R.string.privacy_info)
                .setPositiveButton(com.ishacker.mjpdf.system.R.string.ok) { dialog, _ -> dialog.dismiss() }
                .setIcon(com.ishacker.mjpdf.system.R.drawable.privacy_icon)
                .create()
        }
    }
}