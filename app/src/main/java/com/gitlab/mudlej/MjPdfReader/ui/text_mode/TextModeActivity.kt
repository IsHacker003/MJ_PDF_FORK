package com.gitlab.mudlej.MjPdfReader.ui.text_mode

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.PDF
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityTextModeBinding
import com.gitlab.mudlej.MjPdfReader.manager.extractor.PdfExtractor
import com.gitlab.mudlej.MjPdfReader.ui.showGoToPageDialog
import com.gitlab.mudlej.MjPdfReader.util.ColorUtil
import com.gitlab.mudlej.MjPdfReader.util.createPdfExtractor
import com.gitlab.mudlej.MjPdfReader.util.getFileName
import com.gitlab.mudlej.MjPdfReader.util.indexesOf
import com.gitlab.mudlej.MjPdfReader.util.newColorPicker
import com.gitlab.mudlej.MjPdfReader.util.showOptionalIcons
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import top.defaults.colorpicker.ColorPickerPopup.ColorPickerObserver


class TextModeActivity  : AppCompatActivity() {
    private lateinit var binding: ActivityTextModeBinding
    private lateinit var pdfExtractor: PdfExtractor
    private lateinit var prefManager: SharedPreferences
    private lateinit var pdfUri: Uri
    private var pdfPassword: String? = null

    private var textSize = DEFAULT_FONT_SIZE
    private var textColor = DEFAULT_TEXT_COLOR
    private var backgroundColor = DEFAULT_BACKGROUND_COLOR
    private var buttonColor = DEFAULT_BUTTON_COLOR

    private var pdfLength = -1
    private var pageNumber = DEFAULT_PAGE_NUMBER
    private var pageText = ""
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextModeBinding.inflate(layoutInflater)
        prefManager = PreferenceManager.getDefaultSharedPreferences(this)
        setContentView(binding.root)

        initPdfProperties()

        lifecycleScope.launch {
            initPdfExtractor()
            if (::pdfExtractor.isInitialized) {
                loadPref()
                initActionBar()
                initData()
                initUi()
            } else {
                finish()
            }
        }
    }

    private fun initPdfProperties() {
        val pdfPath = intent.getStringExtra(PDF.filePathKey)
        if (pdfPath.isNullOrEmpty()) {
            badFileExit()
            return
        }
        pdfUri = Uri.parse(pdfPath)
        pdfPassword = intent.getStringExtra(PDF.passwordKey)
    }

    private fun initPdfExtractor() {
        try {
            pdfExtractor = createPdfExtractor(this, pdfUri, pdfPassword)
        }
        catch (throwable: Throwable) {
            Toast.makeText(
                this@TextModeActivity,
                "Failed to read text (file move or deleted?)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun initActionBar() {
        val pdfTitle = getFileName(this, pdfUri).removeSuffix(".pdf")
        val actionBar = supportActionBar
        // Disable the default and enable the custom
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayShowCustomEnabled(true)

        val customView: View = layoutInflater.inflate(R.layout.actionbar_title, null)
        val appTitle = customView.findViewById<TextView>(R.id.actionbarTitle)
        appTitle.text = pdfTitle
        appTitle.typeface = Typeface.SERIF
        appTitle.setOnClickListener {
            if (pdfTitle.isNotBlank()) {
                //Toast.makeText(this, pdfTitle, Toast.LENGTH_LONG).show()
                Snackbar.make(binding.root, pdfTitle, Snackbar.LENGTH_LONG).show()
            }
        }

        actionBar?.customView = customView
    }

    private fun updatePageText() {
        pageText = pdfExtractor.getPageText(pageNumber)
        binding.pageTextView.text = pageText
        binding.pageTextView.textSize = textSize

        if (searchQuery.isNotEmpty()) {
            showQueryResultsInPage()
        }
    }

    private fun initData() {
        updatePageText()
        pdfLength = pdfExtractor.getPageCount()
        if (pdfLength == -1) {
            badFileExit()
        }
    }

    private fun badFileExit() {
        //Toast.makeText(this, getString(R.string.failed_to_extract_text), Toast.LENGTH_LONG).show()
        Snackbar.make(binding.root, getString(R.string.failed_to_extract_text), Snackbar.LENGTH_LONG).show()
        finish()
    }

    private fun initUi() {
        ColorUtil.colorize(this, window, supportActionBar)
        binding.apply {
            nextButton.setOnClickListener { nextPage() }
            prevButton.setOnClickListener { prevPage() }
            pageCounter.setOnClickListener {
                val pageIndex = pageNumber - 1
                showGoToPageDialog(this@TextModeActivity, binding.root, pageIndex, pdfLength, ::goToPage)
            }
        }
        updatePageCounter()
    }

    private fun updatePageCounter() {
        binding.pageCounter.text = getString(R.string.page_counter_label).format(pageNumber, pdfLength)
    }

    private fun goToPage(pageIndex: Int) {
        if (pageIndex in 0 until pdfLength) {
            pageNumber = pageIndex + 1
            postUpdatePageNumber()
        }
        else {
            Snackbar.make(
                binding.root,
                getString(R.string.page_out_of_bound).format(pageIndex + 1),
                Snackbar.LENGTH_LONG
            ).show()
        }

    }

    private fun nextPage() {
        if (pageNumber < pdfLength) {
            ++pageNumber
            postUpdatePageNumber()
        }
    }

    private fun prevPage() {
        if (pageNumber > 1) {
            --pageNumber
            postUpdatePageNumber()
        }
    }

    private fun postUpdatePageNumber() {
        savePageNumber()
        updatePageText()
        updatePageCounter()
        scrollToTop()
    }

    private fun scrollToTop() {
        // scroll to top of the Text ScrollView layout
        binding.pageTextScrollView.fullScroll(ScrollView.FOCUS_UP)
    }

    private fun increaseTextSize() {
        if (textSize < MAX_FONT_SIZE) {
            binding.pageTextView.textSize = ++textSize
            saveTextSize()
        }
    }

    private fun decreaseTextSize() {
        if (textSize > MIN_FONT_SIZE) {
            binding.pageTextView.textSize = --textSize
            saveTextSize()
        }
    }

    private fun savePageNumber() {
        prefManager.edit().putInt(pdfUri.toString(), pageNumber).apply()
    }

    private fun saveTextSize() {
        prefManager.edit().putFloat(FONT_SIZE_KEY, textSize).apply()
    }

    private fun saveTextColor() {
        prefManager.edit().putInt(TEXT_COLOR_KEY, textColor).apply()
    }

    private fun saveBackgroundColor() {
        prefManager.edit().putInt(BACKGROUND_COLOR_KEY, backgroundColor).apply()
    }

    private fun saveButtonColor() {
        prefManager.edit().putInt(BUTTON_COLOR_KEY, buttonColor).apply()
    }

    private fun loadPref() {
        // load values
        pageNumber = prefManager.getInt(pdfUri.toString(), DEFAULT_PAGE_NUMBER)
        textColor = prefManager.getInt(TEXT_COLOR_KEY, DEFAULT_TEXT_COLOR)
        backgroundColor = prefManager.getInt(BACKGROUND_COLOR_KEY, DEFAULT_BACKGROUND_COLOR)
        buttonColor = prefManager.getInt(BUTTON_COLOR_KEY, DEFAULT_BACKGROUND_COLOR)
        textSize = prefManager.getFloat(FONT_SIZE_KEY, DEFAULT_FONT_SIZE)

        updateValues()
    }

    private fun resetValuesToDefault() {
        textColor = DEFAULT_TEXT_COLOR
        backgroundColor = DEFAULT_BACKGROUND_COLOR
        textSize = DEFAULT_FONT_SIZE
        buttonColor = DEFAULT_BUTTON_COLOR
        saveTextSize()
        saveTextColor()
        saveBackgroundColor()
        saveButtonColor()
        updateValues()
    }

    private fun setTextColor() {
        newColorPicker(this)
            .show(binding.pageTextView, object : ColorPickerObserver() {
                override fun onColorPicked(color: Int) {
                    textColor = color
                    updateTextColor()
                    saveTextColor()
                }
            })
    }

    private fun setBackgroundColor() {
        newColorPicker(this)
            .show(binding.textLayout, object : ColorPickerObserver() {
                override fun onColorPicked(color: Int) {
                    backgroundColor = color
                    updateBackgroundColor()
                    saveBackgroundColor()
                }
            })
    }

    private fun updateTextColor() {
        binding.pageTextView.setTextColor(textColor)
        binding.pageCounter.setTextColor(textColor)
    }

    private fun updateBackgroundColor() {
        binding.textLayout.setBackgroundColor(backgroundColor)
        binding.buttonsLayout.setBackgroundColor(backgroundColor)
    }

    private fun updateValues() {
        updateTextColor()
        updateBackgroundColor()
        updatePageText()
    }

    private fun applyDraculaTheme() {
        textColor = draculaForegroundColor
        saveTextColor()
        backgroundColor = draculaBackgroundColor
        saveBackgroundColor()
        buttonColor = draculaButtonColor
        saveButtonColor()
        updateValues()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.text_mode_menu, menu)
        menu.showOptionalIcons()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.increase_text_size -> increaseTextSize()
            R.id.decrease_text_size -> decreaseTextSize()
            R.id.text_color -> setTextColor()
            R.id.background_color -> setBackgroundColor()
            R.id.dracula_theme -> applyDraculaTheme()
            R.id.reset_to_Default -> resetValuesToDefault()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // set search functionality
        val searchView = menu.findItem(R.id.search_in_text_mode).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = false

            override fun onQueryTextChange(query: String): Boolean {
                searchQuery = query
                showQueryResultsInPage()
                return false
            }
        })
        return super.onPrepareOptionsMenu(menu)
    }

    private fun showQueryResultsInPage() {
        val indexes = pageText.indexesOf(searchQuery, ignoreCase = true)
        val stylizedText = stylizeText(searchQuery, indexes)
        binding.pageTextView.setText(stylizedText, TextView.BufferType.SPANNABLE)

//        if (indexes.size != pageText.length) {
//            Snackbar.make(
//                binding.root,
//                getString(R.string.number_of_results).format(indexes.size),
//                Snackbar.LENGTH_SHORT
//            ).show()
//        }
    }

    private fun stylizeText(query: String, indexes: List<Int>): Spannable {
        val color = "#ff335d"   // should be extracted
        val spannable = SpannableString(pageText)

        if (query.isEmpty() || query.isBlank()) {
            return spannable
        }
        else {
            spannable.removeSpan(StyleSpan(Typeface.BOLD))
            spannable.removeSpan(UnderlineSpan())
        }

        for (index in indexes) {
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor(color)),
                index,
                index + query.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                UnderlineSpan(),
                index,
                index + query.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(PAGE_NUMBER_KEY, pageNumber)
        outState.putInt(PDF_LENGTH_KEY, pdfLength)
        outState.putFloat(FONT_SIZE_KEY, textSize)
        outState.putInt(TEXT_COLOR_KEY, textColor)
        outState.putInt(BACKGROUND_COLOR_KEY, backgroundColor)
        outState.putParcelable(URI_KEY, pdfUri)
        // add font color
        super.onSaveInstanceState(outState)
    }

    private fun restoreInstanceState(savedState: Bundle) {
        pageNumber = savedState.getInt(PAGE_NUMBER_KEY)
        pdfLength = savedState.getInt(PDF_LENGTH_KEY)
        textSize = savedState.getFloat(FONT_SIZE_KEY)
        pdfUri = savedState.getParcelable(URI_KEY) ?: return
    }

    companion object {
        private const val TAG = "TextModeActivity"

        // default values
        private const val MIN_FONT_SIZE = 3f
        private const val MAX_FONT_SIZE = 150f
        private const val DEFAULT_FONT_SIZE = 16f
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_BACKGROUND_COLOR = Color.WHITE
        private const val DEFAULT_BUTTON_COLOR = 2503224    // should be extracted
        private const val DEFAULT_PAGE_NUMBER = 1

        // keys
        private const val URI_KEY = "URI_KEY"
        private const val PAGE_NUMBER_KEY = "PAGE_NUMBER_KEY"
        private const val PDF_LENGTH_KEY = "PDF_LENGTH_KEY"
        private const val FONT_SIZE_KEY = "FONT_SIZE_KEY"
        private const val TEXT_COLOR_KEY = "TEXT_COLOR_KEY"
        private const val BACKGROUND_COLOR_KEY = "BACKGROUND_COLOR_KEY"
        private const val BUTTON_COLOR_KEY = "BUTTON_COLOR_KEY"


        // dracula theme
        val draculaBackgroundColor = Color.parseColor("#282a36")
        val draculaForegroundColor = Color.parseColor("#f8f8f2")
        val draculaButtonColor = Color.parseColor("#44475a")
    }
}