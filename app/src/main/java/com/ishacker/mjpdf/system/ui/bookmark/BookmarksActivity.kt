package com.ishacker.mjpdf.system.ui.bookmark

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ishacker.mjpdf.system.R
import com.ishacker.mjpdf.system.data.Bookmark
import com.ishacker.mjpdf.system.data.PDF
import com.ishacker.mjpdf.system.databinding.ActivityBookmarksBinding
import com.ishacker.mjpdf.system.manager.extractor.PdfExtractor
import com.ishacker.mjpdf.system.util.ColorUtil
import com.ishacker.mjpdf.system.util.createPdfExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookmarksActivity : AppCompatActivity(), BookmarkFunctions {
    private lateinit var binding: ActivityBookmarksBinding
    private lateinit var pdfExtractor: PdfExtractor
    private val bookmarkAdapter = BookmarkAdapter(this, this)
    private var bookmarks: List<Bookmark> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showProgressBar()
        lifecycleScope.launch {
            initPdfExtractor()
            if (::pdfExtractor.isInitialized) {
                initActionBar()
                initBookmarks()
                initUi()
            } else {
                finish()
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun initPdfExtractor() {
        val pdfPath = intent.getStringExtra(PDF.filePathKey)
        val pdfPassword = intent.getStringExtra(PDF.passwordKey)
        try {
            pdfExtractor = createPdfExtractor(this, Uri.parse(pdfPath), pdfPassword)
        }
        catch (throwable: Throwable) {
            Toast.makeText(
                this,
                "Failed to read bookmarks! (file move or deleted?)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun initBookmarks() {
        CoroutineScope(Dispatchers.Default).launch {
            bookmarks = pdfExtractor.getAllBookmarks()
            bookmarkAdapter.submitList(bookmarks)

            // back to the UI
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                postGettingBookmarks()
            }
        }
    }

    private fun postGettingBookmarks() {
        if (bookmarks.isNotEmpty()) {
            binding.message.visibility = View.GONE
        }
        else {
            binding.message.text = getString(R.string.no_table_of_contents);
        }
    }

    private fun initActionBar() {
        // add back button to the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = "Searching..."
    }

    private fun initUi() {
        ColorUtil.colorize(this, window, supportActionBar)
        title = getString(R.string.table_of_contents)
        bookmarkAdapter.submitList(bookmarks)
        binding.bookmarksRecyclerView.apply {
            adapter = bookmarkAdapter
            layoutManager = LinearLayoutManager(this@BookmarksActivity)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBookmarkClicked(bookmark: Bookmark) {
        val resultIntent = Intent()
        resultIntent.putExtra(PDF.chosenBookmarkKey, bookmark.pageIdx.toInt())
        setResult(PDF.BOOKMARK_RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        const val TAG = "BookmarksActivity"
    }

}