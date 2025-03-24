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

package com.gitlab.mudlej.MjPdfReader.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.print.PrintManager
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.github.barteksc.pdfviewer.PDFView.Configurator
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.scroll.ScrollHandle
import com.github.barteksc.pdfviewer.util.Constants
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.gitlab.mudlej.MjPdfReader.Launcher
import com.gitlab.mudlej.MjPdfReader.Launchers
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.*
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityMainBinding
import com.gitlab.mudlej.MjPdfReader.databinding.PasswordDialogBinding
import com.gitlab.mudlej.MjPdfReader.enums.AdditionalOptions
import com.gitlab.mudlej.MjPdfReader.enums.FileType
import com.gitlab.mudlej.MjPdfReader.manager.database.DatabaseManager
import com.gitlab.mudlej.MjPdfReader.manager.database.DatabaseManagerImpl
import com.gitlab.mudlej.MjPdfReader.manager.fullscreen.FullScreenOptionsManager
import com.gitlab.mudlej.MjPdfReader.manager.fullscreen.FullScreenOptionsManagerImpl
import com.gitlab.mudlej.MjPdfReader.manager.permission.PermissionManager
import com.gitlab.mudlej.MjPdfReader.manager.print.PdfDocumentAdapter
import com.gitlab.mudlej.MjPdfReader.repository.AppDatabase
import com.gitlab.mudlej.MjPdfReader.repository.PdfRecord
import com.gitlab.mudlej.MjPdfReader.ui.*
import com.gitlab.mudlej.MjPdfReader.ui.about.AboutActivity
import com.gitlab.mudlej.MjPdfReader.ui.bookmark.BookmarksActivity
import com.gitlab.mudlej.MjPdfReader.ui.home.HomeActivity
import com.gitlab.mudlej.MjPdfReader.ui.link.LinksActivity
import com.gitlab.mudlej.MjPdfReader.ui.search.SearchActivity
import com.gitlab.mudlej.MjPdfReader.ui.settings.SettingsActivity
import com.gitlab.mudlej.MjPdfReader.ui.text_mode.TextModeActivity
import com.gitlab.mudlej.MjPdfReader.util.*
import com.gitlab.mudlej.MjPdfReader.util.FileUtil.fileFromUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shockwave.pdfium.PdfPasswordException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.time.LocalDateTime
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.sign
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private val shouldStopExtracting: MutableMap<Int, Boolean> = mutableMapOf()
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    private var doubleBackToExitPressedOnce = false
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private lateinit var fullScreenOptionsManager: FullScreenOptionsManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var databaseManager: DatabaseManager
    private lateinit var pref: Preferences
    private val pdf = PDF()

    private lateinit var actionBarMenu: Menu

    private val launchers = Launchers(
        Launcher(this, pdf).pdfPicker(),
        Launcher(this, pdf).saveToDownloadPermission(::saveDownloadedFileAfterPermissionRequest),
        Launcher(this, pdf).readFileErrorPermission(::restartAppIfGranted),
        Launcher(this, pdf).settings(::displayFromUri)
    )

    private lateinit var appTitle: TextView
    private lateinit var appTitlePageNumber: TextView
    private lateinit var showSearchBar: () -> Unit
    private var brightness: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "-----------onCreate: ${pdf.name} ")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCustomActionBar()

        // To avoid FileUriExposedException, (https://stackoverflow.com/questions/38200282/)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())

        // init
        pref = Preferences(PreferenceManager.getDefaultSharedPreferences(this))
        fullScreenOptionsManager = FullScreenOptionsManagerImpl(
            binding, pdf, pref.getHideDelay().toLong()
        )
        databaseManager = DatabaseManagerImpl(AppDatabase.getInstance(applicationContext))
        permissionManager = PermissionManager(this)
        brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 2

        Constants.THUMBNAIL_RATIO = pref.getThumbnailRation()
        Constants.PART_SIZE = pref.getPartSize()

        // Show Intro Activity and Features Dialog on the first install
        if (pref.getFirstInstall()) {
            onFirstInstall()
            finish()
            return
        }

        // navigate to settings to get permission to manage storage
        //permissionManager.checkStoragePermission { }

        // Create PDF by restoring it in case of an activity restart OR ...
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }
        else {
            pdf.uri = intent.data
            if (pdf.uri == null) {
                pickFile()
                //goToHomePage()
            }
        }

        displayFromUri(pdf.uri, true)
        setButtonsFunctionalities()
        showAppFeaturesDialogOnFirstRun()
        overrideOnBackButtonPressed()
    }

    private fun goToHomePage() {
        Intent(this, HomeActivity::class.java).also {
            startActivity(it)
        }
        finish()
    }

    fun initPdf(pdf: PDF, uri: Uri) {
        pdf.uri = uri
        lifecycleScope.launch {
            pdf.fileHash = computeHash(this@MainActivity, pdf)
        }
    }

    private fun setCustomActionBar() {
        val actionBar = supportActionBar
        // Disable the default and enable the custom
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayShowCustomEnabled(true)

        val customView: View = layoutInflater.inflate(R.layout.actionbar_title, null)
        appTitlePageNumber = customView.findViewById(R.id.actionbarPageNumber)
        appTitle = customView.findViewById(R.id.actionbarTitle)

        fun titleClickListener() {
            val title = pdf.getTitle()
            if (title.isNotBlank()) {
                //Toast.makeText(this, title, Toast.LENGTH_LONG).show()
                Snackbar.make(binding.root, title, Snackbar.LENGTH_LONG).show()
            }
        }
        appTitle.setOnClickListener { titleClickListener() }
        appTitlePageNumber.setOnClickListener { titleClickListener() }

        // Apply the custom view
        actionBar?.customView = customView
    }

    private fun onFirstInstall() {
        // To avoid com.github.paolorotolo.appintro.AppIntroBaseFragment.onCreateView
        // android.content.res.Resources$NotFoundException
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            startActivity(Intent(this, MainIntroActivity::class.java))
        }
        pref.setFirstInstall(false)
        pref.setShowFeaturesDialog(true)
    }

    private fun pickFile() {
        try {
            launchers.pdfPicker.launch(arrayOf(PDF.FILE_TYPE))
        }
        catch (e: ActivityNotFoundException) {
            // alert user that file manager not working
            //Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_LONG).show()
            Snackbar.make(binding.root, R.string.toast_pick_file_error, Snackbar.LENGTH_LONG).show()
        }
    }

    fun displayFromUri(uri: Uri?, savePassword: Boolean = false) {
        if (uri == null) {
            return
        }

        pdf.name = getFileName(this, uri)
        updateAppTitle()
        pdf.resetLength()

        setTaskDescription(ActivityManager.TaskDescription(pdf.name))
        val scheme = uri.scheme
        if (scheme != null && scheme.contains("http")) {
            downloadOrShowDownloadedFile(uri)
        } // temporary solution for files opened via nextcloud
        else if (scheme != null && scheme.contains("org.nextcloud.documents")){
            downloadOrShowDownloadedFile(uri)
        }
        else {
            initPdfViewAndLoad(binding.pdfView.fromUri(pdf.uri), savePassword = savePassword)
        }
    }

    private fun updateAppTitle() {
        appTitle.text = pdf.getTitleWithPageNumber()
    }

    private fun initPdfViewAndLoad(viewConfigurator: Configurator, savePassword: Boolean = false) {
        // attempt to find a saved location for the pdf else assign zero
        if (pdf.pageNumber == 0) {
            lifecycleScope.launch {
                val hash = computeHash(this@MainActivity, pdf)
                if (hash == null) {
                    showFailedToComputeHashError()
                    return@launch
                }
                val pageNumber = databaseManager.findPageNumber(hash)

                pdf.fileHash = hash
                pdf.pageNumber = pageNumber
                withContext(Dispatchers.Main) {
                    initPdfViewAndLoad(viewConfigurator, pageNumber, savePassword)
                }
            }
        }
        else initPdfViewAndLoad(viewConfigurator, pdf.pageNumber, savePassword)
    }

    private fun initPdfViewAndLoad(viewConfigurator: Configurator, pageNumber: Int, savePassword: Boolean) {
        val pdfView = binding.pdfView
        pdfView.useBestQuality(pref.getHighQuality())
        pdfView.minZoom = Preferences.minZoomDefault
        pdfView.midZoom = Preferences.midZoomDefault
        pdfView.maxZoom = pref.getMaxZoom()
        pdfView.zoomTo(pdf.zoom)
        val spacing = if (pref.getSpaceBetweenPages()) Preferences.spacingDefault else 0

        viewConfigurator   // creates a PDFView.Configurator
            .defaultPage(pageNumber)
            .onPageChange { page: Int, pageCount: Int -> setCurrentPage(page, pageCount) }
            .enableAnnotationRendering(Preferences.annotationRenderingDefault)
            .enableAntialiasing(pref.getAntiAliasing())
            .onTap { fullScreenOptionsManager.showAllTemporarilyOrHide(); true }
            .onLongPress { copyPageText(false) }
            .scrollHandle(createScrollHandle())
            .spacing(spacing)
            .onError { exception: Throwable -> handleFileOpeningError(exception) }
            .onPageError { page: Int, error: Throwable -> reportLoadPageError(page, error) }
            .pageFitPolicy(FitPolicy.WIDTH)
            .password(pdf.password)
            .swipeHorizontal(pref.getHorizontalScroll())
            .zoomDisabled(false)
            .autoSpacing(pref.getHorizontalScroll())
            .pageSnap(pref.getPageSnap())
            .pageFling(pref.getPageFling())
            .nightMode(pref.getPdfDarkTheme())
            .onLoad {
                configureTheme()
                createPdfRecord(savePassword, pdf)
                checkAutoFullScreen()
                checkAlwaysHorizontal()
                configureButtonsLabels()
                if (pdf.uri != null) {
                    setUpSecondBar()
                }
            }
            .load()

        // Show the page scroll handler for a while when the pdf is loaded then hide it.
        pdfView.performTap()
    }

    private fun showBarButtonsThatNeedFile() {
        val barButtonsThatNeedFile = listOf(
            R.id.fullscreenOption,
            R.id.copyPageTextOption,
            R.id.bookmarksListOption,
            R.id.linksListOption,
            R.id.goToPageOption,
            R.id.shareFileOption,
            R.id.printFileOption,
            R.id.searchOption,
            R.id.toggleSecondBarOption
        )
        barButtonsThatNeedFile.forEach { actionBarMenu.findItem(it)?.isVisible = true }

        actionBarMenu.findItem(R.id.reloadOption)?.isVisible = pref.getEnableReloadButton()
    }

    private fun checkAutoFullScreen() {
        if (pref.getAutoFullScreen() && !pdf.isFullScreenToggled) {
            toggleFullscreen()
        }
    }

    private fun checkAlwaysHorizontal() {
        if (pref.getAlwaysHorizontal() && pdf.isPortrait) {
            rotateScreen()
        }
        if (!pref.getAlwaysHorizontal() && !pdf.isPortrait) {
            rotateScreen()
        }
    }

    private fun createPdfRecord(savePassword: Boolean, pdf: PDF) {
        val password = if (savePassword) pdf.password else null
        lifecycleScope.launch {
            if (databaseManager.hasRecord(this@MainActivity.pdf.fileHash as String)) {
                // cannot use elvis operator ?: with a suspend function, it won't wait
                if (pdf.fileHash == null) {
                    pdf.fileHash = computeHash(this@MainActivity, this@MainActivity.pdf)
                }
                val fileHash = pdf.fileHash
                if (fileHash == null) {
                    Log.e(TAG, "createPdfRecord: Failed to compute fileHash while creating PdfRecord")
                    return@launch
                }
                databaseManager.setLastOpened(fileHash, LocalDateTime.now())
                if (password != null) {
                    databaseManager.setPassword(fileHash, password)
                }
            }
            else {
                if (pdf.fileHash == null) {
                    pdf.fileHash = computeHash(this@MainActivity, pdf)
                }
                val fileHash = pdf.fileHash
                if (fileHash == null) {
                    showFailedToComputeHashError()
                    return@launch
                }
                val record = PdfRecord.from(fileHash, this@MainActivity.pdf, password)
                databaseManager.saveRecordInBackground(record)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createScrollHandle(): ScrollHandle {
        // hiding the handle if the pdf.length is 1 will happen when pdf.length is set in setPdfLength()
        val handle = DefaultScrollHandle(this)
        handle.setOnTouchListener(fullScreenOptionsManager.getOnTouchListener())
        handle.setOnClickListener { goToPage() }
        return handle
    }

    private fun copyPageText(bypass: Boolean) {
        val pageNumber = pdf.pageNumber
        if (shouldStopExtracting.getOrElse(pageNumber) { false }) {
            return
        }

        var pageText = ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                pageText = binding.pdfView.getPageText(pageNumber)
            }
            catch (e: Throwable) {
                Log.e("PDFium", "extractPageText($pageNumber): error while extracting text", e)
                showFailedExtractTextSnackbar(pageNumber)
            }

            withContext(Dispatchers.Main) {
                if (pageText.isEmpty() || pageText.isBlank()) {
                    showNoTextInPageMessage()
                }
                else {
                    showCopyPageTextDialog(this@MainActivity, binding, pageNumber, pageText, pref, bypass)
                }
            }
        }
    }

    private fun showFailedExtractTextSnackbar(pageNumber: Int) {
        Snackbar.make(binding.root, "Failed to extract text of this file.", Snackbar.LENGTH_SHORT)
            .setAction("Stop this message") { shouldStopExtracting[pageNumber] = true }
            .show()
    }

    private var showNoTextInPage = true
    private fun showNoTextInPageMessage() {
        if (showNoTextInPage) {
            Snackbar.make(binding.root, "Couldn't find text in this page.", Snackbar.LENGTH_LONG).show()
            showNoTextInPage = false
        }
    }

    private fun setUpSecondBar() {
        val buttons: MutableList<ImageView> = mutableListOf()

        // padding values
        val paddingHorDp = 16
        val paddingVerDp = 8
        val density = resources.displayMetrics.density
        val paddingHorizontal = (paddingHorDp * density).toInt()    // convert to pixels
        val paddingVertical = (paddingVerDp * density).toInt()      // convert to pixels

        val toggleTheme = ImageView(this)
        toggleTheme.setImageResource(R.drawable.ic_toggle_theme)
        toggleTheme.setOnClickListener { switchPdfTheme() }
        buttons.add(toggleTheme)

        val openFile = ImageView(this)
        openFile.setImageResource(R.drawable.ic_folder)
        openFile.setOnClickListener { pickFile() }
        buttons.add(openFile)

        val copyPageText = ImageView(this)
        copyPageText.setImageResource(R.drawable.ic_copy)
        copyPageText.setOnClickListener { copyPageText(bypass = true) }
        buttons.add(copyPageText)

        val bookmarks = ImageView(this)
        bookmarks.setImageResource(R.drawable.ic_book_bookmark)
        bookmarks.setOnClickListener { showBookmarks() }
        buttons.add(bookmarks)

        val shareFile = ImageView(this)
        shareFile.setImageResource(R.drawable.ic_share)
        shareFile.setOnClickListener { shareFile(pdf.uri, FileType.PDF) }
        buttons.add(shareFile)

        val search = ImageView(this)
        search.setImageResource(R.drawable.search_icon)
        search.setOnClickListener { showSearchBar() }
        buttons.add(search)

        val goToPage = ImageView(this)
        goToPage.setImageResource(R.drawable.ic_shortcut)
        goToPage.setOnClickListener { goToPage() }
        buttons.add(goToPage)

        for (button in buttons) {
            button.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
            binding.secondBarLayout.addView(button)
        }

        // show it or hide it based on preferences
        if (pref.getSecondBarEnabled() && !pdf.isFullScreenToggled) {
            binding.secondBarLayout.visibility = View.VISIBLE
        }
        else {
            binding.secondBarLayout.visibility = View.GONE
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setButtonsFunctionalities() {
        exitFullScreenListener(binding)
        setAutoScrollButtons(binding)
        setBrightnessSeekbarListener(binding)
        binding.apply {
            rotateScreenButton.setOnClickListener { rotateScreen() }
            brightnessButton.setOnClickListener { setBrightnessButtonListeners(binding) }
            autoScrollButton.setOnClickListener { autoScrollButtonListener(binding) }
            screenshotButton.setOnClickListener { takeScreenshot() }
            toggleHorizontalSwipeButton.setOnClickListener { horizontalSwipeButtonListener(binding) }
            toggleZoomLockButton.setOnClickListener { zoomLockButtonListener(binding) }
            toggleLabelButton.setOnClickListener { toggleLabelButtonListener() }
            pickFileButton.setOnClickListener { pickFile() }
        }
    }

    private fun configureButtonsLabels() {
        if (pref.getHideButtonsLabels()) {
            fullScreenOptionsManager.toggleLabelVisibility(this@MainActivity, ::drawableOf, ::getString)
        }
    }

    private fun toggleZoomDisabled(binding: ActivityMainBinding) {
        binding.pdfView.isZoomDisabled = !binding.pdfView.isZoomDisabled
    }

    private fun toggleLabelButtonListener() {
        fullScreenOptionsManager.toggleLabelVisibility(this@MainActivity, ::drawableOf, ::getString)
        pref.setHideButtonsLabels(!pref.getHideButtonsLabels())
    }

    private fun rotateScreen() {
        requestedOrientation = if (pdf.isPortrait) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        // We need to toggle the start constraint of the brightness layout between startToStart=parent and startToEnd=buttonsList
        // There is no other way to do it AFAIK for complex reasons:
        // We want to make the SeekBar at the bottom span the full width of the screen unless it would overlap with the list on the left.
        // The SeekBar should start where the list ends if the list is long enough (such as in landscape mode), otherwise it should
        // start from parent. And we can't make the layout of the buttons list pass touch interactions with clickable=false and focusable=false
        // because it's a ScrollView
        toggleViewStartConstraint(binding.brightnessLayout, binding.fullScreenButtonsLayout.id)

        pdf.togglePortrait()
    }

    private fun zoomLockButtonListener(binding: ActivityMainBinding) {
        binding.apply {
            if (pdfView.isZoomDisabled) {
                enableZooming(binding)
            }
            else {
                disableZooming(binding)
            }
        }
    }

    private fun horizontalSwipeButtonListener(binding: ActivityMainBinding) {
        binding.apply {
            if (pdfView.isHorizontalSwipeDisabled) {
                enableHorizontalSwiping(binding)
            }
            else {
                disableHorizontalSwiping(binding)
            }
        }
    }

    private fun enableZooming(binding: ActivityMainBinding) {
        binding.toggleZoomLockButton.icon = drawableOf(R.drawable.ic_zoom_out)
        binding.pdfView.isZoomDisabled = false
    }

    private fun disableZooming(binding: ActivityMainBinding) {
        binding.toggleZoomLockButton.icon = drawableOf(R.drawable.ic_lock)
        binding.pdfView.isZoomDisabled = true
    }

    private fun enableHorizontalSwiping(binding: ActivityMainBinding) {
        binding.toggleHorizontalSwipeButton.icon = drawableOf(R.drawable.ic_allow_horizontal_swipe)
        binding.pdfView.isHorizontalSwipeDisabled = false
    }

    private fun disableHorizontalSwiping(binding: ActivityMainBinding) {
        binding.toggleHorizontalSwipeButton.icon = drawableOf(R.drawable.ic_horizontal_swipe_locked)
        binding.pdfView.isHorizontalSwipeDisabled = true
    }

    private fun setAutoScrollButtons(binding: ActivityMainBinding) {
        val delay = 1L
        val scrollUnit = Preferences.AUTO_SCROLL_UNIT
        var scrollBy = -scrollUnit * pref.getScrollSpeed()

        binding.autoScrollSpeedText.text = simplifySpeed(scrollBy).toString()

        binding.incScrollSpeedButton.setOnClickListener {
            scrollBy = changeScrollingSpeed(scrollBy, scrollUnit, isIncreasing = true)
            saveScrollSpeed(scrollBy)
        }
        binding.decScrollSpeedButton.setOnClickListener {
            if (scrollBy.absoluteValue > scrollUnit) {
                scrollBy = changeScrollingSpeed(scrollBy, scrollUnit, isIncreasing = false)
                saveScrollSpeed(scrollBy)
            }
        }

        // check this out: https://stackoverflow.com/questions/7938516/continuously-increase-integer-value-as-the-button-is-pressed
        val handler = Handler(mainLooper)
        lateinit var runnable: Runnable
        val HANDLER_DELAY = 100L

        fun createUpdatingSpeedRunnable(isIncreasing: Boolean): Boolean {
            runnable = Runnable {
                if (!binding.incScrollSpeedButton.isPressed && !binding.decScrollSpeedButton.isPressed) {
                    return@Runnable
                }

                scrollBy = changeScrollingSpeed(scrollBy, scrollUnit, isIncreasing)
                handler.postDelayed(runnable, HANDLER_DELAY)
            }
            handler.postDelayed(runnable, HANDLER_DELAY)
            return true
        }

        binding.incScrollSpeedButton.setOnLongClickListener { createUpdatingSpeedRunnable(isIncreasing = true) }
        binding.decScrollSpeedButton.setOnLongClickListener { createUpdatingSpeedRunnable(isIncreasing = false) }

        binding.reverseScrollDirectionButton.setOnClickListener { scrollBy = -scrollBy }

        binding.toggleAutoScrollButton.setOnClickListener {
            pdf.isAutoScrolling = !pdf.isAutoScrolling

            if (!pdf.isAutoScrolling) {
                stopAutoScrolling(binding)
                return@setOnClickListener
            }
            else {
                binding.toggleAutoScrollButton.setIconResource(R.drawable.ic_pause)
            }

            fun startAutoScrolling() {
                autoScrollHandler.postDelayed({
                    if (pref.getHorizontalScroll()) {
                        binding.pdfView.moveRelativeTo(scrollBy.toFloat(), 0F)
                    }
                    else {
                        binding.pdfView.moveRelativeTo(0F, scrollBy.toFloat())
                    }
                    binding.pdfView.loadPages()

                    if (pdf.isAutoScrolling || pdf.pageNumber < pdf.length) {
                        startAutoScrolling()
                    }
                }, delay)
            }
            startAutoScrolling()
        }
    }

    private fun saveScrollSpeed(scrollBy: Double) {
        pref.setScrollSpeed(simplifySpeed(scrollBy))
    }

    private fun stopAutoScrolling(binding: ActivityMainBinding) {
        binding.toggleAutoScrollButton.setIconResource(R.drawable.ic_play_arrow)
        autoScrollHandler.removeCallbacksAndMessages(null)
        pdf.isAutoScrolling = false
    }

    private fun autoScrollButtonListener(binding: ActivityMainBinding) {
        if (binding.autoScrollLayout.isVisible) hideAutoScroll(binding) else showAutoScroll(binding)
    }

    private fun hideAutoScroll(binding: ActivityMainBinding) {
        binding.autoScrollLayout.visibility = View.GONE
        binding.autoScrollSpeedText.visibility = View.GONE
        pdf.isAutoScrollClicked = false
    }

    private fun showAutoScroll(binding: ActivityMainBinding) {
        binding.autoScrollLayout.visibility = View.VISIBLE
        binding.autoScrollSpeedText.visibility = View.VISIBLE
        pdf.isAutoScrollClicked = true
    }

    private fun setBrightnessButtonListeners(binding: ActivityMainBinding) {
        if (binding.brightnessLayout.isVisible) hideBrightnessControl(binding) else showBrightnessControl(binding)
    }

    private fun hideBrightnessControl(binding: ActivityMainBinding) {
        binding.brightnessLayout.visibility = View.GONE
        pdf.isBrightnessClicked = false
    }

    private fun showBrightnessControl(binding: ActivityMainBinding) {
        binding.brightnessLayout.visibility = View.VISIBLE
        pdf.isBrightnessClicked = true
    }

    private fun setBrightnessSeekbarListener(binding: ActivityMainBinding) {
        // init the seekbar
        val brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        binding.brightnessSeekBar.progress = brightness
        binding.brightnessPercentage.text = "$brightness%"
        binding.brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (seekBar == null) return
                // Don't override system's brightness if the user didn't manually asked for it
                if (fromUser) updateBrightness(progress)
            }
        })

    }

    private fun updateBrightness(brightness: Int) {
        binding.brightnessPercentage.text = "$brightness%"
        window.attributes.screenBrightness = brightness.toFloat() / 100
        window.attributes = window.attributes // apply it
    }

    private fun exitFullScreenListener(binding: ActivityMainBinding) {
        binding.exitFullScreenButton.setOnClickListener {
            if (!pref.getAlwaysHorizontal()) {
                unlockScreenOrientation()
            }
            toggleFullscreen()
            stopAutoScrolling(binding)
            enableZooming(binding)
            hideBrightnessControl(binding)
            hideAutoScroll(binding)
            enableHorizontalSwiping(binding)

            // A try to give the brightness control back to the system but this won't work
            // updateBrightness(brightness)
        }
    }

    private fun unlockScreenOrientation() {
        // set orientation to unspecified so that the screen rotation will be unlocked
        // this is because PORTRAIT / LANDSCAPE modes will lock the app in them
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun simplifySpeed(scrollBy: Double): Int {
        return (scrollBy.absoluteValue * (1 / Preferences.AUTO_SCROLL_UNIT)).toInt()
    }

    private fun changeScrollingSpeed(scrollBy: Double, interval: Double, isIncreasing: Boolean): Double {
        val newSpeed = if (isIncreasing) {
            (scrollBy.absoluteValue + interval) * scrollBy.sign
        }
        else {
            if (scrollBy.absoluteValue > interval) {
                (scrollBy.absoluteValue - interval) * scrollBy.sign
            }
            else {
                scrollBy
            }
        }

        binding.autoScrollSpeedText.text = simplifySpeed(newSpeed).toString()
        return newSpeed
    }

    public override fun onResume() {
        Log.i(TAG, "-----------onResume: ${pdf.name} ")
        super.onResume()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (pref.getScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        if (::actionBarMenu.isInitialized) {
            showBarButtonsThatNeedFile()
        }

        // check if there is a pdf at first
        // if (pdf.uri == null) return

        if (pdf.uri != null) {
            binding.pickFileButton.visibility = View.GONE
        }
        else {
            binding.pickFileButton.visibility = View.VISIBLE
        }

        // restore the full screen mode if was toggled On
        restoreFullScreenIfNeeded()

        // Prompt the user to restore the previous zoom if there is one saved other than the default
        // pdfZoom != binding.pdfView.getZoom())   // doesn't work for some peculiar reason
        if (pdf.zoom != 1f) {
            Snackbar.make(
                findViewById(R.id.mainLayout),
                getString(R.string.ask_restore_zoom), Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.restore)) {
                    binding.pdfView.zoomWithAnimation(pdf.zoom)
                }
                .show()
        }
    }

    private fun restoreFullScreenIfNeeded() {
        if (pdf.isFullScreenToggled) {
            pdf.isFullScreenToggled = false
            toggleFullscreen()
        }
    }

    private fun shareFile(uri: Uri?, type: FileType) {
        if (uri == null) {
            checkHasFile()  // only to show the message
            return
        }
        val sharingIntent: Intent =
            if (uri.scheme != null && uri.scheme!!.startsWith("http")) {
                plainTextShareIntent(getString(R.string.share_file), pdf.uri.toString())
            }
            else if (type == FileType.PDF) {
                fileShareIntent(getString(R.string.share_file), pdf.name, uri)
            }
            else if (type == FileType.IMAGE) {
                imageShareIntent(getString(R.string.share_file), pdf.name, uri)
            }
            else {
                return
            }

        try {
            startActivity(sharingIntent)
        }
        catch (e: Throwable) {
            //Toast.makeText(this, "Error sharing the file. (${e.message})", Toast.LENGTH_LONG).show()
            Snackbar.make(binding.root, "Error sharing the file. (${e.message})", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun configureTheme() {
        ColorUtil.colorize(this, window, supportActionBar)
        val color = SurfaceColors.SURFACE_2.getColor(this)
        binding.secondBarLayout.setBackgroundColor(color)

        val pdfView = binding.pdfView

        // set background color behind pages
        if (!pref.getPdfDarkTheme()) {
            pdfView.setBackgroundColor(Preferences.pdfDarkBackgroundColor)
        }
        else {
            pdfView.setBackgroundColor(Preferences.pdfLightBackgroundColor)
        }

        if (pref.getAppFollowSystemTheme()) {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
        else {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        if (pref.getPdfFollowSystemTheme()) {
            applyPdfTheme()
        }
    }

    private fun applyPdfTheme() {
        when (applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> setPdfTheme(true)
            Configuration.UI_MODE_NIGHT_NO -> setPdfTheme(false)
            Configuration.UI_MODE_NIGHT_UNDEFINED -> setPdfTheme(false)
        }
    }

    private fun reportLoadPageError(page: Int, error: Throwable) {
        val message = resources.getString(R.string.cannot_load_page) + page + " " + error
        //Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        Log.e(TAG, message)
    }

    private fun handleFileOpeningError(exception: Throwable) {
        val fileHash = pdf.fileHash
        if (exception is PdfPasswordException && fileHash != null) {
            if (pdf.password != null) {
                //Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show()
                Snackbar.make(binding.root, R.string.wrong_password, Snackbar.LENGTH_SHORT).show()
                pdf.password = null         // prevent the toast if the user rotates the screen
            }

            lifecycleScope.launch {
                pdf.password = databaseManager.findPdfPassword(fileHash)
                withContext(Dispatchers.Main) {
                    if (pdf.password != null) {
                        displayFromUri(pdf.uri)
                    }
                    else {
                        askForPdfPassword()
                    }
                }
            }
        }
        else if (couldNotOpenFileDueToMissingPermission(exception)) {
            launchers.readFileErrorPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        else {
            //Toast.makeText(this, R.string.file_opening_error, Toast.LENGTH_LONG).show()
            Snackbar.make(binding.root, R.string.file_opening_error, Snackbar.LENGTH_LONG).show()
            Log.e(TAG, getString(R.string.file_opening_error), exception)
        }
    }

    private fun couldNotOpenFileDueToMissingPermission(e: Throwable): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) return false
        val exceptionMessage = e.message
        return e is FileNotFoundException && exceptionMessage != null
            && exceptionMessage.contains(getString(R.string.permission_denied))
    }

    private fun restartAppIfGranted(isPermissionGranted: Boolean) {
        if (isPermissionGranted) {
            // This is a quick and dirty way to make the system restart the current activity *and the current app process*.
            // This is needed because on Android 6 storage permission grants do not take effect until
            // the app process is restarted.
            exitProcess(0)
        }
        else {
            //Toast.makeText(this, R.string.file_opening_error, Toast.LENGTH_LONG).show()
            Snackbar.make(binding.root, R.string.file_opening_error, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun toggleFullscreen() {
        fun showUi() {
            supportActionBar?.show()
            if (pref.getSecondBarEnabled()) {
                binding.secondBarLayout.visibility = View.VISIBLE
            }
            binding.pdfView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }

        fun hideUi() {
            supportActionBar?.hide()
            binding.secondBarLayout.visibility = View.GONE
            binding.pdfView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            )
        }

        if (!pdf.isFullScreenToggled) {
            hideUi()
            pdf.isFullScreenToggled = true
            fullScreenOptionsManager.hideAll()

            // show how to exit Full Screen dialog
            if (pref.getShowFeaturesDialog()) {
                showHowToExitFullscreenDialog(this, pref)
            }
        }
        else {
            showUi()
            pdf.isFullScreenToggled = false
            fullScreenOptionsManager.showAllTemporarilyOrHide()
        }
    }

    private fun downloadOrShowDownloadedFile(uri: Uri) {
        if (PdfBytesHolder.pdfByte == null) {
            PdfBytesHolder.pdfByte = lastCustomNonConfigurationInstance as ByteArray?
        }
        if (PdfBytesHolder.pdfByte != null) {
            initPdfViewAndLoad(binding.pdfView.fromBytes(PdfBytesHolder.pdfByte))
        }
        else {
            // we will get the pdf asynchronously with the DownloadPDFFile object
            binding.progressBar.visibility = View.VISIBLE
            val downloadPDFFile = DownloadPDFFile(this, binding)
            downloadPDFFile.execute(uri.toString())
        }
    }

    override fun onRetainCustomNonConfigurationInstance(): Any? {
        return PdfBytesHolder.pdfByte
    }

    fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    fun saveToFileAndDisplay(pdfFileContent: ByteArray?) {
        Log.d(TAG, "saveToFileAndDisplay pdfFileContent is set to: $pdfFileContent: ")
        PdfBytesHolder.pdfByte = pdfFileContent
        saveToDownloadFolderIfAllowed(pdfFileContent)
        initPdfViewAndLoad(binding.pdfView.fromBytes(pdfFileContent))
    }

    private fun saveToDownloadFolderIfAllowed(fileContent: ByteArray?) {
        if (canWriteToDownloadFolder(this)) {
            trySaveToDownloads(fileContent, false)
        }
        else {
            launchers.saveToDownloadPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun trySaveToDownloads(fileContent: ByteArray?, showSuccessMessage: Boolean) {
        try {
            val downloadDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            writeBytesToFile(downloadDirectory, pdf.name, fileContent)
            if (showSuccessMessage) {
                //Toast.makeText(this, R.string.saved_to_download, Toast.LENGTH_SHORT).show()
                Snackbar.make(binding.root, R.string.saved_to_download, Snackbar.LENGTH_SHORT).show()
            }
        }
        catch (e: IOException) {
            Log.e(TAG, getString(R.string.save_to_download_failed), e)
            //Toast.makeText(this, R.string.save_to_download_failed, Toast.LENGTH_SHORT).show()
            Snackbar.make(binding.root, R.string.save_to_download_failed, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun saveDownloadedFileAfterPermissionRequest(isPermissionGranted: Boolean) {
        if (isPermissionGranted) {
            trySaveToDownloads(PdfBytesHolder.pdfByte, true)
        }
        else {
            //Toast.makeText(this, R.string.save_to_download_failed, Toast.LENGTH_SHORT).show()
            Snackbar.make(binding.root, R.string.save_to_download_failed, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun navToAppSettings() {
        launchers.settings.launch(Intent(this, SettingsActivity::class.java))
    }

    private fun setCurrentPage(pageNumber: Int, pageCount: Int) {
        pdf.pageNumber = pageNumber
        setPdfLength(pageCount)
        updateAppTitle()

        lifecycleScope.launch {
            // cannot use elvis operator ?: with a suspend function, it won't wait
            if (pdf.fileHash == null) {
                pdf.fileHash = computeHash(this@MainActivity, pdf)
            }
            val hash = pdf.fileHash
            if (hash != null) {  // Ensure hash is not null
                databaseManager.setPageNumber(hash, pageNumber)  // Set the page number in the database
            }
            else {
                showFailedToComputeHashError()
            }
        }
    }

    private fun showFailedToComputeHashError() {
        val message = "Can't hash the file! Last visited page won't be remembered in this session."
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        Log.e(TAG, "showFailedToComputeHashError: $message", RuntimeException())
    }

    private fun setPdfLength(pageCount: Int) {
        pdf.initPdfLength(pageCount)
        if (pageCount == 1) {
            fullScreenOptionsManager.permanentlyHidePageHandle()
        }
    }

    private fun printFile() {
        if (checkHasFile()) {
            val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
            try {
                printManager.print(
                    pdf.name,
                    PdfDocumentAdapter(this, pdf.uri), null
                )
            }
            catch (e: Throwable) {
                //Toast.makeText(this, "Failed to print. Error message: ${e.message}", Toast.LENGTH_LONG).show()
                Snackbar.make(binding.root, "Failed to print. Error message: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun askForPdfPassword() {
        val dialogBinding = PasswordDialogBinding.inflate(layoutInflater)
        showAskForPasswordDialog(this, pdf, dialogBinding, ::displayFromUri)
    }

    private fun showAppFeaturesDialogOnFirstRun() {
        if (pref.getShowFeaturesDialog()) {
            Handler(mainLooper).postDelayed({ showAppFeaturesDialog(this) }, 500)
            pref.setShowFeaturesDialog(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.actionBarMenu = menu
        menu.showOptionalIcons()
        showBarButtonsThatNeedFile()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reloadOption -> recreate()
            R.id.fullscreenOption -> toggleFullscreen()
            R.id.switchThemeOption -> switchPdfTheme()
            R.id.openFileOption -> pickFile()
            R.id.copyPageTextOption -> copyPageText(true)
            R.id.bookmarksListOption -> showBookmarks()
            R.id.goToPageOption -> goToPage()
            R.id.linksListOption -> showLinks()
            R.id.shareFileOption -> shareFile(pdf.uri, FileType.PDF)
            R.id.printFileOption -> printFile()
            //R.id.searchOption -> searchFileClicked()
            R.id.toggleSecondBarOption -> toggleSecondBar()
            R.id.additionalOptionsOption -> showAdditionalOptions()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // set search functionality
        val searchView = menu.findItem(R.id.searchOption).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                fun startSearchActivity() {
                    pdf.lastQuery = query
                    Intent(this@MainActivity, SearchActivity::class.java).also { searchIntent ->
                        searchIntent.putExtra(PDF.filePathKey, pdf.uri.toString())
                        searchIntent.putExtra(PDF.passwordKey, pdf.password)
                        searchIntent.putExtra(PDF.searchQueryKey, query.trim())
                        startActivityForResult(searchIntent, PDF.startSearchActivity)
                        supportActionBar?.collapseActionView()  // close it after searching
                    }
                }

                if (query.isBlank() || query.length < 3) {
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle(getString(R.string.too_short_query))
                        .setMessage(getString(R.string.too_short_query_message).format(query))
                        .setNeutralButton(getString(R.string.proceed_anyway)) { _, _ ->
                            startSearchActivity()
                        }
                        .setPositiveButton(getText(R.string.ok)) { badQueryDialog, _ ->
                            badQueryDialog.dismiss()
                        }
                        .show()
                }
                else {
                    startSearchActivity()
                }
                return false
            }

            override fun onQueryTextChange(query: String) = false
        })

        // create a lambda to trigger the search
        showSearchBar = { menu.performIdentifierAction(R.id.searchOption, 0) }

        return super.onPrepareOptionsMenu(menu)
    }

    private fun toggleSecondBar() {
        binding.apply {
            if (secondBarLayout.visibility == View.VISIBLE) {
                secondBarLayout.visibility = View.GONE
                pref.setSecondBarEnabled(false)
            }
            else {
                secondBarLayout.visibility = View.VISIBLE
                pref.setSecondBarEnabled(true)
            }
        }
    }

    private fun showLinks() {
        Intent(this@MainActivity, LinksActivity::class.java).also { linksIntent ->
            linksIntent.putExtra(PDF.filePathKey, pdf.uri.toString())
            linksIntent.putExtra(PDF.passwordKey, pdf.password)
            startActivityForResult(linksIntent, PDF.startLinksActivity)
        }
    }

    private fun showBookmarks() {
        Intent(this@MainActivity, BookmarksActivity::class.java).also { bookmarkIntent ->
            bookmarkIntent.putExtra(PDF.filePathKey, pdf.uri.toString())
            bookmarkIntent.putExtra(PDF.passwordKey, pdf.password)
            startActivityForResult(bookmarkIntent, PDF.startBookmarksActivity)
        }
    }

    private fun goToPage() {
        fun goToPage(pageIndex: Int) {
            binding.pdfView.jumpTo(pageIndex)
        }
        showGoToPageDialog(this, binding.root, pdf.pageNumber, pdf.length, ::goToPage)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (pref.getTurnPageByVolumeButtons()) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> binding.pdfView.jumpTo(++pdf.pageNumber)
                KeyEvent.KEYCODE_VOLUME_UP -> binding.pdfView.jumpTo(--pdf.pageNumber)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun navToTextMode() {
        if (!checkHasFile()) {
            return
        }

        Intent(this, TextModeActivity::class.java).also {
            it.putExtra(PDF.filePathKey, pdf.uri.toString())
            it.putExtra(PDF.passwordKey, pdf.password)
            startActivityForResult(it, PDF.startTextActivity)
        }
    }

    private fun showAdditionalOptions() {

        data class Item(val title: String, val icon: Int)

        val settingsMap = mapOf(
            AdditionalOptions.APP_SETTINGS to Item(getString(R.string.app_settings), R.drawable.ic_settings),
            AdditionalOptions.TEXT_MODE to Item(getString(R.string.text_mode), R.drawable.ic_text),
            AdditionalOptions.METADATA to Item(getString(R.string.file_metadata), R.drawable.meta_info),
            AdditionalOptions.ADVANCED_CONFIG to Item(
                getString(R.string.advanced_config),
                R.drawable.ic_display_settings
            ),
            AdditionalOptions.ABOUT to Item(getString(R.string.action_about), R.drawable.info_icon),
        )

        // create a dialog for additional options and set their functionalities

        // Custom Adapter for the dialog so we can use icons for the items
        val items = settingsMap.values.toTypedArray()
        val adapter: ListAdapter = object : ArrayAdapter<Item>(
            this,
            android.R.layout.select_dialog_item,
            android.R.id.text1,
            items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                // Use super class to create the View
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById(android.R.id.text1) as TextView

                textView.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0)
                textView.text = items[position].title

                val padding = (10 * resources.displayMetrics.density + 0.5f).toInt()
                textView.compoundDrawablePadding = padding
                return view
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.settings))
            .setAdapter(adapter) { dialog, item ->
                when (item) {
                    AdditionalOptions.APP_SETTINGS.ordinal -> {
                        navToAppSettings()
                    }

                    AdditionalOptions.TEXT_MODE.ordinal -> {
                        navToTextMode()
                    }

                    AdditionalOptions.METADATA.ordinal -> {
                        if (checkHasFile()) {
                            val uri = pdf.uri
                            var file: File? = null
                            if (uri != null) {
                                try {
                                    file = fileFromUri(this@MainActivity, uri, pdf.name)
                                }
                                catch (throwable: Throwable) {
                                    Log.e(TAG, "showAdditionalOptions: Failed to createFileFromUri", throwable)
                                }
                            }
                            showMetaDialog(this, binding.pdfView.documentMeta, file)
                        }
                    }

                    AdditionalOptions.ADVANCED_CONFIG.ordinal -> {
                        showPartSizeDialog(this, pref)
                    }

                    AdditionalOptions.ABOUT.ordinal -> {
                        startActivity(navIntent(this, AboutActivity::class.java))
                    }
                }
                dialog.dismiss()
            }.show()
    }

    private fun checkHasFile(): Boolean {
        if (!pdf.hasFile()) {
            Snackbar.make(
                binding.root, getString(R.string.no_pdf_in_app),
                Snackbar.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    private fun switchPdfTheme() {
        if (pref.getPdfFollowSystemTheme()) {
            Snackbar.make(
                binding.root,
                "PDF theme is set to follow system's theme. Disable it in the Settings first.",
                Snackbar.LENGTH_LONG
            ).show()
        }
        else if (checkHasFile()) {
            setPdfTheme(!pref.getPdfDarkTheme())
        }
    }

    private fun setPdfTheme(darkTheme: Boolean) {
        if (pref.getPdfDarkTheme() != darkTheme) {
            pref.setPdfDarkTheme(darkTheme)
            recreate()
        }
    }

    private fun screenShot(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun takeScreenshot() {
        val now = DateFormat.format("yyyy_MM_dd-hh_mm_ss", Date())
        try {
            val fileName = "${pdf.name.removeSuffix(".pdf")} - ${now}.jpg"
            val imageFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)

            fullScreenOptionsManager.showAllTemporarilyOrHide()
            val bitmap = screenShot(binding.pdfView)

            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, PDF.SCREENSHOT_IMAGE_QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()

            val uri = saveImage(bitmap, fileName)
            Snackbar.make(binding.root, getString(R.string.screenshot_saved), Snackbar.LENGTH_SHORT).also {
                it.setAction(getString(R.string.share)) { shareFile(uri, FileType.IMAGE) }
                it.show()
            }
        }
        catch (e: Throwable) {
            // Several error may come out with file handling or DOM
            //Toast.makeText(this, getString(R.string.failed_save_screenshot), Toast.LENGTH_LONG).show()
            Snackbar.make(binding.root, getString(R.string.failed_save_screenshot), Snackbar.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun saveImage(bitmap: Bitmap, fileName: String): Uri? {
        val (fileOutputStream: OutputStream?, imageUri: Uri?) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/*")

            // e.g.     ~/Pictures/app_name/screenshot1.jpg
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/${getString(R.string.mj_app_name)}/"
            )

            val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            Pair(imageUri?.let { contentResolver.openOutputStream(it) }, imageUri)
        }
        else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
            val image = File(imagesDir, fileName)
            Pair(FileOutputStream(image), image.toUri())
        }
        if (fileOutputStream != null) {
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                PDF.SCREENSHOT_IMAGE_QUALITY,
                fileOutputStream
            )
        }
        fileOutputStream?.close()
        return imageUri
    }

    private fun drawableOf(id: Int): Drawable? {
        return AppCompatResources.getDrawable(this, id)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(PDF.uriKey, pdf.uri)
        outState.putString(PDF.fileHashKey, pdf.fileHash)
        outState.putInt(PDF.pageNumberKey, pdf.pageNumber)
        outState.putString(PDF.passwordKey, pdf.password)
        outState.putBoolean(PDF.isFullScreenToggledKey, pdf.isFullScreenToggled)
        outState.putFloat(PDF.zoomKey, binding.pdfView.zoom)
        outState.putBoolean(PDF.isExtractingTextFinishedKey, pdf.isExtractingTextFinished)
        super.onSaveInstanceState(outState)
    }

    private fun restoreInstanceState(savedState: Bundle) {
        pdf.uri = savedState.getParcelable(PDF.uriKey)
        pdf.fileHash = savedState.getString(PDF.fileHashKey)
        pdf.pageNumber = savedState.getInt(PDF.pageNumberKey)
        pdf.password = savedState.getString(PDF.passwordKey)
        pdf.isFullScreenToggled = savedState.getBoolean(PDF.isFullScreenToggledKey)
        pdf.zoom = savedState.getFloat(PDF.zoomKey)
        pdf.isExtractingTextFinished = savedState.getBoolean(PDF.isExtractingTextFinishedKey)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        binding.progressBar.visibility = View.GONE
        when (requestCode) {
            PDF.startBookmarksActivity -> {
                if (resultCode == PDF.BOOKMARK_RESULT_OK) {
                    val pageIndex = intent?.getIntExtra(PDF.chosenBookmarkKey, pdf.pageNumber) ?: return
                    binding.pdfView.jumpTo(pageIndex)
                }
            }
            PDF.startLinksActivity -> {
                if (resultCode == PDF.LINK_RESULT_OK) {
                    val pageNumber = intent?.getIntExtra(PDF.linkResultKey, pdf.pageNumber) ?: return
                    val pageIndex = pageNumber - 1
                    binding.pdfView.jumpTo(pageIndex)
                }
            }
            PDF.startSearchActivity -> {
                if (resultCode == PDF.SEARCH_RESULT_OK) {
                    val searchResultJson = intent?.getStringExtra(PDF.searchResultKey) ?: return
                    val searchResultType = object : TypeToken<SearchResult>() {}.type
                    val searchResult = Gson().fromJson<SearchResult>(searchResultJson, searchResultType)

                    // highlight the result text
                    val textBound = binding.pdfView.createHighlightText(
                        searchResult.pageNumber,
                        searchResult.originalIndex,
                        searchResult.inputEnd - searchResult.inputStart,
                        true
                    )

                    if (textBound.isEmpty()) {
                        Snackbar.make(binding.root, "Failed to highlight search result", Snackbar.LENGTH_SHORT).show()
                    }
                    // I disabled this because I couldn't get the zooming in to work properly in all cases, it is ~80% of the time correct.
                    // else if (textBound.size == 1) {
                    //     binding.pdfView.zoomWithAnimation(textBound[0].toRectF(), 3f, searchResult.pageNumber)
                    //     binding.pdfView.reloadPages()    // to show the highlighting
                    //}
                    else {
                        // because the user may not see the highlight if it was zoomed in before searching
                        binding.pdfView.resetZoomWithAnimation()
                        binding.pdfView.reloadPages()   // to show the highlighting
                    }

                    // show a snackbar with a button that will remove the highlight (it wills still be cached for a bit)
                    val snackbar = Snackbar.make(binding.root, getString(R.string.results), Snackbar.LENGTH_INDEFINITE)
                    snackbar.setAction(getString(R.string.done)) {
                        binding.pdfView.clearSearchResultsHighlight(searchResult.pageNumber)
                        snackbar.dismiss()
                    }
                    val snackBarView = snackbar.view
                    val textView = snackBarView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
                    textView.setTextColor(ContextCompat.getColor(this,R.color.search))
                    textView.setOnClickListener {
                        //binding.pdfView.resetZoomWithAnimation()
                        binding.pdfView.clearSearchResultsHighlight(searchResult.pageNumber)
                        //Handler(Looper.getMainLooper()).postDelayed({
                        Intent(this@MainActivity, SearchActivity::class.java).also { searchIntent ->
                            searchIntent.putExtra(PDF.filePathKey, pdf.uri.toString())
                            searchIntent.putExtra(PDF.passwordKey, pdf.password)
                            pdf.lastQuery?.let { searchIntent.putExtra(PDF.searchQueryKey, it.trim()) }
                            searchIntent.putExtra(PDF.resultPositionInListKey, searchResult.searchResultIndexInList)
                            startActivityForResult(searchIntent, PDF.startSearchActivity)
                            snackbar.dismiss()
                        }
                        //}, 400) // same as zoom-out animation duration (not a good way to do it, I know)
                    }
                    snackbar.show()

                    binding.pdfView.jumpUsingPageNumber(searchResult.pageNumber)
                }
            }
        }
    }


    private fun overrideOnBackButtonPressed() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("BackPress", "onBackPressed called: doubleBackToExitPressedOnce = $doubleBackToExitPressedOnce")
                if (!pref.getDoubleTapToExitEnabled() || doubleBackToExitPressedOnce) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                } else {
                    Snackbar.make(binding.root, getString(R.string.press_back_again), Snackbar.LENGTH_LONG).show()
                    doubleBackToExitPressedOnce = true

                    CoroutineScope(Dispatchers.Main).launch {
                        delay(2500)
                        Log.d("BackPress", "Coroutine executing: resetting doubleBackToExitPressedOnce")
                        doubleBackToExitPressedOnce = false
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

}


/*
    * pdf.pageNumber && pdf.length:
        will be set by PDFView::onPageChange() -> setCurrentPage()

    * pdf.password:
        will be set by PDFView::onError() -> handleFileOpeningError() -> askForPdfPassword()
 */