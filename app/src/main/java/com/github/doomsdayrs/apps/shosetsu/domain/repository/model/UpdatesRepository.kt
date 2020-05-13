package com.github.doomsdayrs.apps.shosetsu.domain.repository.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.github.doomsdayrs.apps.shosetsu.common.dto.HResult
import com.github.doomsdayrs.apps.shosetsu.datasource.local.base.ILocalUpdatesDataSource
import com.github.doomsdayrs.apps.shosetsu.domain.model.local.UpdateEntity
import com.github.doomsdayrs.apps.shosetsu.domain.repository.base.IUpdatesRepository

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
 * ====================================================================
 */

/**
 * shosetsu
 * 24 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
class UpdatesRepository(
		val iLocalUpdatesDataSource: ILocalUpdatesDataSource
) : IUpdatesRepository {
	override fun addUpdate(updateEntity: UpdateEntity) {
		TODO("Not yet implemented")
	}

	override fun getUpdates(): LiveData<HResult<List<UpdateEntity>>> {
		TODO("Not yet implemented")
	}

	override suspend fun getUpdateDays(): LiveData<HResult<List<Long>>> =
			liveData {
				emitSource(iLocalUpdatesDataSource.getUpdateDays())
			}
}