package com.github.doomsdayrs.apps.shosetsu.ui.settings.subFragments

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import com.github.doomsdayrs.apps.shosetsu.common.Settings
import com.github.doomsdayrs.apps.shosetsu.backend.shoDir
import com.github.doomsdayrs.apps.shosetsu.common.consts.ActivityRequestCodes.REQUEST_CODE_DIRECTORY
import com.github.doomsdayrs.apps.shosetsu.common.ext.context
import com.github.doomsdayrs.apps.shosetsu.common.ext.toast
import com.github.doomsdayrs.apps.shosetsu.common.ext.viewModel
import com.github.doomsdayrs.apps.shosetsu.ui.settings.SettingsSubController
import com.github.doomsdayrs.apps.shosetsu.viewmodel.base.ISettingsDownloadViewModel

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
 * 13 / 07 / 2019
 */
class DownloadSettings : SettingsSubController() {
	val viewModel: ISettingsDownloadViewModel by viewModel()
	override val settings by lazy { viewModel.settings }

	override fun onViewCreated(view: View) {
		settings[0].setTextOnClickListener { performFileSearch() }
		super.onViewCreated(view)
	}

	private fun setDownloadDirectory(dir: String) {
		Settings.downloadDirectory = dir
		shoDir = dir
		recyclerView?.post { adapter?.notifyItemChanged(0) }
	}

	private fun performFileSearch() {
		context?.toast("Please make sure this is on the main storage, " +
				"SD card storage is not functional yet", duration = Toast.LENGTH_LONG)
		val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
		i.addCategory(Intent.CATEGORY_DEFAULT)
		activity?.startActivityForResult(Intent.createChooser(i, "Choose directory"), 42)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == REQUEST_CODE_DIRECTORY && resultCode == Activity.RESULT_OK) {
			if (data != null) {
				val path = data.data?.path
				Log.i("Selected Folder", "Uri: $path")
				if (path != null)
					setDownloadDirectory(path.substring(path.indexOf(":") + 1))
				else context?.toast("Path is null")
			}
		}
	}
}