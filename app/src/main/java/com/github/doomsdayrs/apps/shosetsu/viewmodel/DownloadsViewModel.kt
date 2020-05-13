package com.github.doomsdayrs.apps.shosetsu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.github.doomsdayrs.apps.shosetsu.common.Settings
import com.github.doomsdayrs.apps.shosetsu.common.dto.HResult
import com.github.doomsdayrs.apps.shosetsu.common.dto.loading
import com.github.doomsdayrs.apps.shosetsu.domain.usecases.GetDownloadsUseCase
import com.github.doomsdayrs.apps.shosetsu.view.uimodels.DownloadUI
import com.github.doomsdayrs.apps.shosetsu.viewmodel.base.IDownloadsViewModel
import kotlinx.coroutines.Dispatchers

/*
 * This file is part of shosetsu.
 *
 * shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * shosetsu
 * 24 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
class DownloadsViewModel(
		val getDownloadsUseCase: GetDownloadsUseCase
) : IDownloadsViewModel() {

	override val liveData: LiveData<HResult<List<DownloadUI>>> by lazy {
		liveData(viewModelScope.coroutineContext + Dispatchers.Default) {
			emit(loading())
			emitSource(getDownloadsUseCase())
		}
	}

	override fun togglePause(): Boolean {
		Settings.isDownloadPaused = !Settings.isDownloadPaused
		return Settings.isDownloadPaused
	}
}