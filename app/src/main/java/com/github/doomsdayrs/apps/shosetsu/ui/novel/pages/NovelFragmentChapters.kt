package com.github.doomsdayrs.apps.shosetsu.ui.novel.pages

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.doomsdayrs.api.shosetsu.services.core.objects.NovelChapter
import com.github.doomsdayrs.apps.shosetsu.R
import com.github.doomsdayrs.apps.shosetsu.backend.DownloadManager
import com.github.doomsdayrs.apps.shosetsu.backend.Utilities.openChapter
import com.github.doomsdayrs.apps.shosetsu.backend.async.ChapterLoader
import com.github.doomsdayrs.apps.shosetsu.backend.async.ChapterLoader.ChapterLoaderAction
import com.github.doomsdayrs.apps.shosetsu.backend.database.Database
import com.github.doomsdayrs.apps.shosetsu.backend.database.Database.DatabaseChapter.*
import com.github.doomsdayrs.apps.shosetsu.backend.database.Database.DatabaseIdentification
import com.github.doomsdayrs.apps.shosetsu.backend.database.Database.DatabaseUpdates.addToUpdates
import com.github.doomsdayrs.apps.shosetsu.ui.novel.NovelFragment
import com.github.doomsdayrs.apps.shosetsu.ui.novel.adapters.ChaptersAdapter
import com.github.doomsdayrs.apps.shosetsu.variables.DownloadItem
import com.github.doomsdayrs.apps.shosetsu.variables.enums.Status
import kotlinx.android.synthetic.main.fragment_novel_chapters.*

/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 * ====================================================================
 */
/**
 * Shosetsu
 * 9 / June / 2019
 *
 * @author github.com/doomsdayrs
 *
 *
 * Displays the chapters the novel contains
 * TODO Check filesystem if the chapter is saved, even if not in DB.
 *
 */
class NovelFragmentChapters : Fragment() {
    val chaptersLoadedAction: ChapterLoaderAction

    init {
        chaptersLoadedAction = object : ChapterLoaderAction {
            override fun onPreExecute() {
                fragment_novel_chapters_refresh.isRefreshing = true
                if (novelFragment?.formatter?.isIncrementingChapterList!!)
                    page_count.visibility = VISIBLE
            }

            override fun onPostExecute(result: Boolean, finalChapters: ArrayList<NovelChapter>) {
                fragment_novel_chapters_refresh.isRefreshing = false
                if (novelFragment?.formatter?.isIncrementingChapterList!!)
                    page_count.visibility = GONE

                if (result) {
                    novelFragment?.novelChapters = finalChapters
                    adapter?.notifyDataSetChanged()
                }
            }

            override fun onJustBeforePost(finalChapters: ArrayList<NovelChapter>) {
                val s = getString(R.string.processing_data)
                page_count?.post { page_count.text = s }
                for ((count: Int, novelChapter: NovelChapter) in finalChapters.withIndex()) {
                    val sc = s + ": $count/${finalChapters.size}"
                    page_count?.post { page_count.text = sc }
                    if (novelFragment != null && novelFragment!!.novelID != -1) {
                        if (isNotInChapters(novelChapter.link)) {
                            Log.i("ChapterLoader", "Adding ${novelChapter.link}")
                            addToChapters(novelFragment!!.novelID, novelChapter)
                            if (!novelFragment!!.new)
                                addToUpdates(novelFragment!!.novelID, novelChapter.link, System.currentTimeMillis())
                        } else {
                            updateChapter(novelChapter)
                        }
                    } else Log.e("ChapterLoader", "Invalid novelID")
                }
            }

            override fun onIncrementingProgress(page: Int, max: Int) {
                val s = "Page: $page/$max"
                page_count?.post { page_count.text = s }
            }

            override fun errorReceived(errorString: String) {
                Log.e("ChapterLoader", errorString)
                activity?.runOnUiThread { Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private var currentMaxPage = 1
    var selectedChapters = ArrayList<NovelChapter>()
    var adapter: ChaptersAdapter? = ChaptersAdapter(this)
    var novelFragment: NovelFragment? = null


    var menu: Menu? = null

    operator fun contains(novelChapter: NovelChapter): Boolean {
        for (n in selectedChapters) if (n.link.equals(novelChapter.link, ignoreCase = true)) return true
        return false
    }

    private fun findMinPosition(): Int {
        var min: Int = novelFragment!!.novelChapters.size
        for (x in novelFragment!!.novelChapters.indices) if (contains(novelFragment!!.novelChapters[x])) if (x < min) min = x
        return min
    }

    private fun findMaxPosition(): Int {
        var max = -1
        for (x in novelFragment!!.novelChapters.indices.reversed()) if (contains(novelFragment!!.novelChapters[x])) if (x > max) max = x
        return max
    }



    override fun onDestroy() {
        super.onDestroy()
        reversed = false
        Log.d("NFChapters", "Destroy")
    }

    /**
     * Save data of view before destroyed
     *
     * @param outState output save
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("maxPage", currentMaxPage)
        outState.putSerializable("selChapter", selectedChapters)
    }

    /**
     * Creates view
     *
     * @param inflater           inflater to retrieve objects
     * @param container          container of this fragment
     * @param savedInstanceState save
     * @return View
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (savedInstanceState != null) {
            //TODO Remove novelChapter as a valid data stream
            selectedChapters = (savedInstanceState.getSerializable("selChapter") as ArrayList<NovelChapter>?)!!
            currentMaxPage = savedInstanceState.getInt("maxPage")
        }
        novelFragment = parentFragment as NovelFragment?
        novelFragment!!.novelFragmentChapters = this
        Log.d("NovelFragmentChapters", "Creating")
        return inflater.inflate(R.layout.fragment_novel_chapters, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resume.visibility = GONE
        if (novelFragment != null)
            fragment_novel_chapters_refresh.setOnRefreshListener {
                if (novelFragment != null && novelFragment!!.novelURL.isNotEmpty())
                    ChapterLoader(chaptersLoadedAction, novelFragment!!.formatter, novelFragment!!.novelURL).execute()
            }
        if (savedInstanceState != null) {
            currentMaxPage = savedInstanceState.getInt("maxPage")
        }
        setChapters()
        onResume()
        resume.setOnClickListener {
            val i = novelFragment!!.lastRead()
            if (i != -1 && i != -2) {
                if (activity != null) openChapter(activity!!, novelFragment!!.novelChapters[i], novelFragment!!.novelID, novelFragment!!.formatter.formatterID)
            } else Toast.makeText(context, "No chapters! How did you even press this!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Sets the novel chapters down
     */
    fun setChapters() {
        fragment_novel_chapters_recycler!!.post {
            fragment_novel_chapters_recycler!!.setHasFixedSize(false)
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
            if (novelFragment != null && !Database.DatabaseNovels.isNotInNovels(novelFragment!!.novelID)) {
                novelFragment!!.novelChapters = getChapters(novelFragment!!.novelID)
                if (novelFragment!!.novelChapters.isNotEmpty()) resume!!.visibility = VISIBLE
            }
            adapter = ChaptersAdapter(this)
            adapter!!.setHasStableIds(true)
            fragment_novel_chapters_recycler!!.layoutManager = layoutManager
            fragment_novel_chapters_recycler!!.adapter = adapter
        }
    }

    val inflater: MenuInflater?
        get() = MenuInflater(context)

    fun updateAdapter(): Boolean {
        return fragment_novel_chapters_recycler!!.post { if (adapter != null) adapter!!.notifyDataSetChanged() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.chapter_select_all -> {
                for (novelChapter in novelFragment!!.novelChapters) if (!contains(novelChapter)) selectedChapters.add(novelChapter)
                updateAdapter()
                return true
            }
            R.id.chapter_download_selected -> {
                for (novelChapter in selectedChapters) {
                    val chapterID = DatabaseIdentification.getChapterIDFromChapterURL(novelChapter.link)
                    if (!Database.DatabaseChapter.isSaved(chapterID)) {
                        val downloadItem = DownloadItem(novelFragment!!.formatter, novelFragment!!.novelPage.title, novelChapter.title, chapterID)
                        DownloadManager.addToDownload(activity, downloadItem)
                    }
                }
                updateAdapter()
                return true
            }
            R.id.chapter_delete_selected -> {
                for (novelChapter in selectedChapters) {
                    val chapterID = DatabaseIdentification.getChapterIDFromChapterURL(novelChapter.link)
                    if (Database.DatabaseChapter.isSaved(chapterID)) DownloadManager.delete(context, DownloadItem(novelFragment!!.formatter, novelFragment!!.novelPage.title, novelChapter.title, chapterID))
                }
                updateAdapter()
                return true
            }
            R.id.chapter_deselect_all -> {
                selectedChapters = ArrayList()
                updateAdapter()
                if (inflater != null) activity?.invalidateOptionsMenu()
                return true
            }
            R.id.chapter_mark_read -> {
                for (novelChapter in selectedChapters) {
                    val chapterID = DatabaseIdentification.getChapterIDFromChapterURL(novelChapter.link)
                    if (Database.DatabaseChapter.getStatus(chapterID).a != 2) Database.DatabaseChapter.setChapterStatus(chapterID, Status.READ)
                }
                updateAdapter()
                return true
            }
            R.id.chapter_mark_unread -> {
                for (novelChapter in selectedChapters) {
                    val chapterID = DatabaseIdentification.getChapterIDFromChapterURL(novelChapter.link)
                    if (Database.DatabaseChapter.getStatus(chapterID).a != 0) Database.DatabaseChapter.setChapterStatus(chapterID, Status.UNREAD)
                }
                updateAdapter()
                return true
            }
            R.id.chapter_mark_reading -> {
                for (novelChapter in selectedChapters) {
                    val chapterID = DatabaseIdentification.getChapterIDFromChapterURL(novelChapter.link)
                    if (Database.DatabaseChapter.getStatus(chapterID).a != 0) Database.DatabaseChapter.setChapterStatus(chapterID, Status.READING)
                }
                updateAdapter()
                return true
            }
            R.id.chapter_select_between -> {
                val min = findMinPosition()
                val max = findMaxPosition()
                var x = min
                while (x < max) {
                    if (!contains(novelFragment!!.novelChapters[x])) selectedChapters.add(novelFragment!!.novelChapters[x])
                    x++
                }
                updateAdapter()
                return true
            }
            R.id.chapter_filter -> {
                novelFragment!!.novelChapters = novelFragment!!.novelChapters.reversed()
                reversed = !reversed
                return updateAdapter()
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        if (adapter != null) adapter!!.notifyDataSetChanged()
    }

    /**
     * Creates the option menu (on the top toolbar)
     *
     * @param menu     Menu reference to fill
     * @param inflater Object to inflate the menu
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        menu.clear()
        if (selectedChapters.size <= 0) inflater.inflate(R.menu.toolbar_chapters, menu) else inflater.inflate(R.menu.toolbar_chapters_selected, menu)
    }

    var reversed = false

    /**
     * Constructor
     */
    init {
        setHasOptionsMenu(true)
    }
}