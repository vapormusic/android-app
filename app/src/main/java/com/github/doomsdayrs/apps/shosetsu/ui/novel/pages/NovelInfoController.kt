package com.github.doomsdayrs.apps.shosetsu.ui.novel.pages

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import app.shosetsu.lib.Novel
import com.github.doomsdayrs.apps.shosetsu.R
import com.github.doomsdayrs.apps.shosetsu.R.id
import com.github.doomsdayrs.apps.shosetsu.common.consts.BundleKeys.BUNDLE_NOVEL_ID
import com.github.doomsdayrs.apps.shosetsu.common.dto.HResult
import com.github.doomsdayrs.apps.shosetsu.common.ext.*
import com.github.doomsdayrs.apps.shosetsu.ui.migration.MigrationController
import com.github.doomsdayrs.apps.shosetsu.ui.novel.NovelController
import com.github.doomsdayrs.apps.shosetsu.view.base.FABView
import com.github.doomsdayrs.apps.shosetsu.view.base.ViewedController
import com.github.doomsdayrs.apps.shosetsu.view.uimodels.NovelUI
import com.github.doomsdayrs.apps.shosetsu.viewmodel.base.INovelInfoViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

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
 */

/**
 * Shosetsu
 * 9 / June / 2019
 *
 * The page you see when you select a novel
 */
class NovelInfoController(
		private val bundle: Bundle
) : ViewedController(bundle), FABView {
	override val layoutRes: Int = R.layout.novel_main

	val viewModel: INovelInfoViewModel by viewModel()

	private var novelUI: NovelUI? = null
	private var formatterName: String = ""

	init {
		setHasOptionsMenu(true)
	}

	// UI items
	@Attach(id.novel_add)
	var novelAdd: FloatingActionButton? = null

	@Attach(id.novel_title)
	var novelTitle: TextView? = null

	@Attach(id.novel_author)
	var novelAuthor: TextView? = null

	@Attach(id.novel_description)
	var novelDescription: TextView? = null

	@Attach(id.novel_publish)
	var novelPublish: TextView? = null

	@Attach(id.novel_artists)
	var novelArtists: TextView? = null

	@Attach(id.novel_genres)
	var novelGenres: ChipGroup? = null

	@Attach(id.novel_formatter)
	var novelFormatter: TextView? = null

	@Attach(id.novel_image)
	var novelImage: ImageView? = null

	@Attach(id.novel_image_background)
	var novelImageBackground: ImageView? = null

	override fun onOptionsItemSelected(item: MenuItem): Boolean = novelUI?.let {
		when (item.itemId) {
			id.source_migrate -> {
				parentController?.router?.pushController(MigrationController(bundleOf(Pair(
						MigrationController.TARGETS_BUNDLE_KEY,
						arrayOf(novelUI!!.id!!).toIntArray()
				))).withFadeTransaction())
				true
			}
			id.webview -> {
				activity?.openInWebView(it.novelURL)
				true
			}
			id.browser -> {
				activity?.openInBrowser(it.novelURL)
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	} ?: false

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.toolbar_novel, menu)
		menu.findItem(id.source_migrate).isVisible = novelUI?.bookmarked ?: false
	}

	override fun onViewCreated(view: View) {
		novelAdd?.hide()

		if (novelUI?.bookmarked == true)
			novelAdd?.setImageResource(R.drawable.ic_baseline_check_circle_24)

		novelAdd?.setOnClickListener {
			novelAdd?.setImageResource(
					if (novelUI?.bookmarked == false)
						R.drawable.ic_baseline_check_circle_24
					else R.drawable.ic_add_circle_outline_24dp
			)
			viewModel.toggleBookmark(novelUI!!)
		}
		viewModel.setNovelID(bundle.getInt(BUNDLE_NOVEL_ID))
		setObserver()
		setFormatterName()
		setNovelData()
	}

	private fun setObserver() {
		viewModel.liveData.observe(this, Observer {
			when (it) {
				is HResult.Success -> {
					novelUI = it.data
					activity?.invalidateOptionsMenu()
					// If the data is not present, loads it
					if (!novelUI!!.loaded) (parentController as NovelController).refresh()
					else setNovelData()
				}
				is HResult.Error -> {
				}
				is HResult.Empty -> {
				}
				is HResult.Loading -> {
				}
			}
		})
		viewModel.formatterName.observe(this, Observer {
			when (it) {
				is HResult.Success -> {
					formatterName = it.data
					launchUI {
						setFormatterName()
					}
				}
				is HResult.Error -> {
					launchUI {
						setFormatterName("Error on loading")
					}
				}
				is HResult.Empty -> {
					launchUI {
						setFormatterName("UNKNOWN")
					}
				}
				is HResult.Loading -> {
					launchUI {
						setFormatterName("Loading")
					}
				}
			}

		})
	}

	/**
	 * Sets the data of this page
	 */
	private fun setNovelData() {
		novelUI?.let { novelUI ->
			// Handle title
			activity?.setActivityTitle(novelUI.title)
			novelTitle?.text = novelUI.title

			// Handle authors
			if (novelUI.authors.isNotEmpty())
				novelAuthor?.text = novelUI.authors.contentToString()

			// Handle description
			novelDescription?.text = novelUI.description

			// Handle artists
			if (novelUI.artists.isNotEmpty())
				novelArtists?.text = novelUI.artists.contentToString()

			// Handles the status of the novel
			when (novelUI.status) {
				Novel.Status.PAUSED -> novelPublish?.setText(R.string.paused)
				Novel.Status.COMPLETED -> novelPublish?.setText(R.string.completed)
				Novel.Status.PUBLISHING -> novelPublish?.setText(R.string.publishing)
				else -> novelPublish?.setText(R.string.unknown)
			}

			// Inserts the chips for genres
			for (string in novelUI.genres) {
				val chip = Chip(novelGenres!!.context)
				chip.text = string
				novelGenres?.addView(chip)
			}

			// Loads the image
			if (novelUI.imageURL.isNotEmpty()) {
				Picasso.get().load(novelUI.imageURL).into(novelImage, object : Callback {
					override fun onSuccess() {
						Picasso.get().load(novelUI.imageURL).into(novelImageBackground)
					}

					override fun onError(e: Exception?) {
					}
				})
			}

			// Show the option to add the novel
			novelAdd?.show()
		}
	}

	private fun setFormatterName(text: String = formatterName) {
		novelFormatter?.text = text
	}

	override fun hideFAB() {
		novelUI?.let {
			novelAdd?.hide()
		}
	}

	override fun showFAB() {
		novelUI?.let {
			novelAdd?.show()
		}
	}
}