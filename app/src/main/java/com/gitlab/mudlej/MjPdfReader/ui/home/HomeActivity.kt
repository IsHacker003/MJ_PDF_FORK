package com.gitlab.mudlej.MjPdfReader.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gitlab.mudlej.MjPdfReader.R
import com.gitlab.mudlej.MjPdfReader.data.Preferences
import com.gitlab.mudlej.MjPdfReader.databinding.ActivityHomeBinding
import com.gitlab.mudlej.MjPdfReader.databinding.RecordAboutDialogBinding
import com.gitlab.mudlej.MjPdfReader.enums.ListFilter
import com.gitlab.mudlej.MjPdfReader.enums.ReadingStatus
import com.gitlab.mudlej.MjPdfReader.manager.database.DatabaseManager
import com.gitlab.mudlej.MjPdfReader.manager.database.DatabaseManagerImpl
import com.gitlab.mudlej.MjPdfReader.manager.permission.PermissionManager
import com.gitlab.mudlej.MjPdfReader.manager.storage.StorageManager
import com.gitlab.mudlej.MjPdfReader.repository.AppDatabase
import com.gitlab.mudlej.MjPdfReader.repository.PdfRecord
import com.gitlab.mudlej.MjPdfReader.ui.main.MainActivity
import com.gitlab.mudlej.MjPdfReader.util.ColorUtil
import com.gitlab.mudlej.MjPdfReader.util.FileUtil
import com.gitlab.mudlej.MjPdfReader.util.StringUtil.formatEnumToTitle
import com.gitlab.mudlej.MjPdfReader.util.StringUtil.formatTitleToEnum
import com.gitlab.mudlej.MjPdfReader.util.divideToPercent
import com.gitlab.mudlej.MjPdfReader.util.showOptionalIcons
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.shockwave.pdfium.PdfiumCore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class HomeActivity : AppCompatActivity(), RecordFunctions {

    private lateinit var permissionManager: PermissionManager
    private lateinit var databaseManager: DatabaseManager
    private lateinit var binding: ActivityHomeBinding
    private lateinit var listFilter: ListFilter
    private lateinit var pdfiumCore: PdfiumCore
    private lateinit var pref: Preferences

    private val recordAdapter = RecordAdapter(this, this)
    private var records: List<PdfRecord> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init
        databaseManager = DatabaseManagerImpl(AppDatabase.getInstance(applicationContext))
        pref = Preferences(PreferenceManager.getDefaultSharedPreferences(this))
        permissionManager = PermissionManager(this)
        listFilter = pref.getListFilter()
        pdfiumCore = PdfiumCore(this)

        initUi()
        initRecordList()
    }

    private fun loadAllFiles() {
        val storageManager = StorageManager()
        lifecycleScope.launch {
            val filesMap = storageManager.scanPdfFilesWithHash(this@HomeActivity)

            // back to the UI
            withContext(Dispatchers.Main) {
                recordAdapter.submitList(filesMap.map { PdfRecord.from(this@HomeActivity, it) })
                postSetFilter(getString(R.string.all_files))
            }
        }
    }

    private fun hideProgressBar() {
        binding.swipeRecordList.isRefreshing = false
    }

    private fun showProgressBar() {
        binding.swipeRecordList.isRefreshing = true
    }

    override fun onResume() {
        super.onResume()
        // scanning the storage every time is very expensive
        if (listFilter != ListFilter.ALL) {
            initRecordList()
        }
    }

    private fun initUi() {
        ColorUtil.colorize(this, window, supportActionBar)
        title = getString(R.string.recently_opened)
        recordAdapter.submitList(records)
        addRecordSwipeFunctionality()

        binding.recordRecyclerView.apply {
            adapter = recordAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }

        binding.swipeRecordList.setOnRefreshListener {
            initRecordList()
        }
    }

    private fun filterRecordsList(listFilter: ListFilter, filter: (PdfRecord) -> Boolean) {
        val title = listFilter.name.formatEnumToTitle()
        CoroutineScope(Dispatchers.IO).launch {
            records = findAllRecords()
                .filter(filter)
                .sortedByDescending { record -> record.lastOpened }

            Log.d("HomeActivity", "onCreate: PDFs: $records")
            recordAdapter.submitList(records)
            withContext(Dispatchers.Main) {
                postSetFilter(title)
            }
        }
    }

    private fun postSetFilter(newTitle: String) {
        title = newTitle
        hideProgressBar()
    }

    private fun initRecordList() {
        showProgressBar()
        when (listFilter) {
            ListFilter.ALL       -> loadAllFiles()
            ListFilter.FAVORITE  -> filterRecordsList(ListFilter.FAVORITE)  { it.favorite }
            ListFilter.TO_READ   -> filterRecordsList(ListFilter.TO_READ)   { it.reading == ReadingStatus.TO_READ }
            ListFilter.READING   -> filterRecordsList(ListFilter.READING)   { it.reading == ReadingStatus.READING }
            ListFilter.ON_HOLD   -> filterRecordsList(ListFilter.ON_HOLD)   { it.reading == ReadingStatus.ON_HOLD }
            ListFilter.COMPLETED -> filterRecordsList(ListFilter.COMPLETED) { it.reading == ReadingStatus.COMPLETED }
            ListFilter.ABANDONED -> filterRecordsList(ListFilter.ABANDONED) { it.reading == ReadingStatus.ABANDONED }
            ListFilter.RECENT    -> filterRecordsList(ListFilter.RECENT)    { true }
        }
    }

    private suspend fun findAllRecords(): List<PdfRecord> {
        return withContext(Dispatchers.IO) {
            databaseManager.findAllRecords()
                .filter { it.fileName.isNotEmpty() }
        }
    }

    private fun addRecordSwipeFunctionality() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val record = recordAdapter.currentList[viewHolder.adapterPosition] ?: return
                onPdfRecordSwiped(record)
            }
        }).attachToRecyclerView(binding.recordRecyclerView)
    }

    private fun onPdfRecordSwiped(record: PdfRecord) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_dialog_title))
            .setMessage(getString(R.string.delete_dialog_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                lifecycleScope.launchWhenStarted {
                    databaseManager.removeRecord(record)
                    withContext(Dispatchers.Main) {
                        initRecordList()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                recordAdapter.notifyItemChanged(recordAdapter.currentList.indexOf(record))
            }
            .show()

    }

    private fun showListFilterDialog() {
        val filters = getListFilterItems()
        val currentFilterIndex = filters.indexOf(listFilter.name.formatEnumToTitle())

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.filter_list_dialog_title))
            .setSingleChoiceItems(filters, currentFilterIndex) { dialog, index ->
                listFilter = ListFilter.valueOf(filters[index].formatTitleToEnum())
                recordAdapter.submitList(listOf())  // clear
                pref.setListFilter(listFilter)
                initRecordList()
                dialog.dismiss()
            }
            .show()
    }

    private fun getListFilterItems(): Array<String> {
        return ListFilter.entries.map { it.name.formatEnumToTitle() }.toTypedArray()
    }

    private fun getReadingItems(): Array<String> {
        return ReadingStatus.entries.map { it.name.formatEnumToTitle() }.toTypedArray()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        menu.showOptionalIcons()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.searchOptionInHome -> Snackbar.make(binding.root, "Search", Snackbar.LENGTH_SHORT).show()
            R.id.openFileOptionInHome -> permissionManager.launchPicker()
            R.id.listFilterOptionInHome -> showListFilterDialog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // set search functionality
        val searchView = menu.findItem(R.id.searchOptionInHome).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = false

            override fun onQueryTextChange(query: String): Boolean {
                val filteredList = records.filter { it.fileName.contains(query, true) }
                recordAdapter.submitList(filteredList)
                return false
            }
        })
        searchView.setOnCloseListener {
            recordAdapter.submitList(records)
            true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCardClicked(record: PdfRecord) {
        Intent(this, MainActivity::class.java).also { mainIntent ->
            mainIntent.data = record.uri
            startActivity(mainIntent)
        }
    }

    override fun onAboutClicked(record: PdfRecord) {
        //val aboutView = layoutInflater.inflate(R.layout.record_about_dialog, null) as RecordAboutDialogBinding
        val aboutView = RecordAboutDialogBinding.inflate(layoutInflater, null, false)
        val pdfData = FileUtil.getPdfData(pdfiumCore, record.uri, reduceSize = false)

        aboutView.apply {
            title.text = record.fileName
            lastOpened.text = getLastOpened(record)

            if (pdfData != null) {
                pdfData.cover?.let { image.setImageBitmap(it) }

                length.text = pdfData.length.toString()
                progressBar.max = pdfData.length

                percentage.text = getString(R.string.record_percentage_template)
                    .format(getPageNumber(record).divideToPercent(pdfData.length))
            }

            progress.text = getPageNumber(record).toString()
            progressBar.progress = getPageNumber(record)
            setFavorite(aboutView, record.favorite)

            favorite.setOnClickListener {
                val newFavorite = !record.favorite
                lifecycleScope.launchWhenStarted {
                    databaseManager.setFavorite(record.hash, newFavorite)
                    withContext(Dispatchers.Main) {
                        setFavorite(aboutView, newFavorite)
                        record.favorite = newFavorite
                    }
                }
                initRecordList()
            }

            val statusAdapter =  ArrayAdapter(
                this@HomeActivity,
                R.layout.support_simple_spinner_dropdown_item,
                getReadingItems()
            )

            statusSpinner.adapter = statusAdapter
            statusSpinner.setSelection(statusAdapter.getPosition(record.reading.name.formatEnumToTitle()))
            statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    lifecycleScope.launchWhenStarted {
                        val choice = statusAdapter.getItem(position)?.formatTitleToEnum() ?: return@launchWhenStarted
                        databaseManager.setReading(record.hash, ReadingStatus.valueOf(choice))
                        initRecordList()
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.about_record))
            .setView(aboutView.root)
            .show()
    }

    private fun getPageNumber(record: PdfRecord): Int {
        if (record.pageNumber == 0) {
            return 0    // this is a special case to indicate there is no progress
        }
        return record.pageNumber + 1
    }

    private fun setFavorite(aboutView: RecordAboutDialogBinding, newFavorite: Boolean) {
        if (newFavorite) {
            aboutView.favoriteIcon.setImageResource(R.drawable.ic_favorite_active)
            aboutView.favoriteLabel.setTextColor(
                ContextCompat.getColor(this@HomeActivity, R.color.favorite)
            )
        }
        else {
            aboutView.favoriteIcon.setImageResource(R.drawable.ic_favorite_inactive)
//            aboutView.favoriteLabel.setTextColor(
//                ContextCompat.getColor(this@HomeActivity, R.color.onDarkBackground)
//            )
        }
    }

    private fun getLastOpened(record: PdfRecord) =
        if (record.lastOpened == LocalDateTime.parse(PdfRecord.UNSET_DATE)) {
            "Never"
        }
        else {
            record.lastOpened.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
        }

}