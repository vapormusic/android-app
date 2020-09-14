package app.shosetsu.android.viewmodel.model

import androidx.lifecycle.LiveData
import app.shosetsu.android.common.dto.HResult
import app.shosetsu.android.common.enums.ReadingStatus
import app.shosetsu.android.domain.usecases.load.LoadUpdatesUseCase
import app.shosetsu.android.view.uimodels.model.UpdateUI
import app.shosetsu.android.viewmodel.abstracted.IUpdatesViewModel

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
 * 29 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
class UpdatesViewModel(
		private val getUpdatesUseCase: LoadUpdatesUseCase,
) : IUpdatesViewModel() {
	override val liveData: LiveData<HResult<List<UpdateUI>>> by lazy {
		getUpdatesUseCase()
	}

	override suspend fun updateChapter(updateUI: UpdateUI, readingStatus: ReadingStatus) {
	}
}