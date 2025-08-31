package com.ishacker.mjpdf.system.ui.link

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ishacker.mjpdf.system.R
import com.ishacker.mjpdf.system.data.Link
import com.ishacker.mjpdf.system.data.PDF
import com.ishacker.mjpdf.system.databinding.ActivityLinkBinding
import com.ishacker.mjpdf.system.manager.extractor.PdfExtractor
import com.ishacker.mjpdf.system.util.ColorUtil
import com.ishacker.mjpdf.system.util.configureSearchIcon
import com.ishacker.mjpdf.system.util.copyToClipboard
import com.ishacker.mjpdf.system.util.createPdfExtractor
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LinksActivity : AppCompatActivity(), LinkFunctions {
    private lateinit var binding: ActivityLinkBinding
    private lateinit var pdfExtractor: PdfExtractor
    private val linkAdapter = LinkAdapter(this, this)
    private var links: List<Link> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showProgressBar()

        lifecycleScope.launch {
            initPdfExtractor()
            if (::pdfExtractor.isInitialized) {
                initActionBar()
                initLinks()
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
                "Failed to read links! (file move or deleted?)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun initLinks() {
        CoroutineScope(Dispatchers.Default).launch {
            links = pdfExtractor.getAllLinks()
            linkAdapter.submitList(links)

            // back to the UI
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                postGettingLinks()
            }
        }
    }

    private fun postGettingLinks() {
        if (links.isNotEmpty()) {
            binding.message.visibility = View.GONE
        }
        else {
            binding.message.text = getString(R.string.no_links_put_in_pdf)
        }

        // set up the title in the App Bar
        title = "${"%,d".format(links.size)} ${getString(R.string.links_in_document)}"

        // show too many results message
        if (links.size > PDF.TOO_MANY_RESULTS) {
            Snackbar.make(binding.root,getString(R.string.too_many_results_may_be_slow), Snackbar.LENGTH_INDEFINITE).also {
                it.setAction(getText(R.string.ok)) { }
                it.show()
            }
        }
    }

    private fun initActionBar() {
        // add back button to the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.loading)
    }

    private fun initUi() {
        ColorUtil.colorize(this, window, supportActionBar)
        title = getString(R.string.links_activity_title)
        linkAdapter.submitList(links)
        linkAdapter.progressBar = binding.progressBar
        binding.linkRecyclerView.apply {
            adapter = linkAdapter
            layoutManager = LinearLayoutManager(this@LinksActivity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        configureSearchIcon(menu, links.isNotEmpty())
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // set search functionality
        val searchView = menu.findItem(R.id.search_in_search_activity).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = false

            override fun onQueryTextChange(query: String): Boolean {
                linkAdapter.nestedQuery = query
                binding.progressBar.visibility = View.VISIBLE
                val filteredList = links.filter {
                    it.url.contains(query, true)  || it.text.contains(query, true)
                }
                linkAdapter.submitList(filteredList)
                linkAdapter.notifyDataSetChanged() // because the comparator doesn't see the difference in text style

                Snackbar.make(
                    binding.root,
                    getString(R.string.number_of_filtered_results).format(filteredList.size),
                    Snackbar.LENGTH_SHORT
                ).show()
                return false
            }
        })
        searchView.setOnCloseListener {
            linkAdapter.submitList(links)
            true
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onLinkClicked(link: Link) {
        Intent(Intent.ACTION_VIEW).also {
            it.data = (Uri.parse(link.url))
            try {
                startActivity(it)
            } catch (throwable: Throwable) {
                Snackbar.make(binding.root, getString(R.string.no_app_to_open_link), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onPageNumberClicked(link: Link) {
        Intent().also { resultIntent ->
            resultIntent.putExtra(PDF.linkResultKey, link.pageNumber)
            setResult(PDF.LINK_RESULT_OK, resultIntent)
        }
        finish()
    }

    override fun onCopyLinkClicked(link: Link) {
        val copyLabel = "Link URL copy"
        copyToClipboard(this, copyLabel, link.url)

        // show message to user before closing
        //Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
        Snackbar.make(binding.root, getString(R.string.copied_to_clipboard), Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "LinksActivity"
    }

}