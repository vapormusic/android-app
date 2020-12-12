package app.shosetsu.android.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater.from
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.view.isVisible
import app.shosetsu.android.view.widget.TriStateButton.State.*
import com.github.doomsdayrs.apps.shosetsu.R
import com.github.doomsdayrs.apps.shosetsu.databinding.TriStateButtonBinding

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
 * shosetsu
 * 23 / 11 / 2020
 */
class TriStateButton @JvmOverloads constructor(
		context: Context,
		attrs: AttributeSet? = null,
		defStyleAttr: Int = 0
) : FrameLayout(
		context,
		attrs,
		defStyleAttr
) {
	private val checkedRes: Int
	private val uncheckedRes: Int
	private val ignoredRes: Int


	/**
	 * Prevents this button from going into an ignored state
	 */
	var skipIgnored = false

	var state: State = IGNORED
		set(value) {
			field = value
			onStateChangeListener.forEach { listener ->
				listener(value)
			}
			setDrawable()
		}

	private val onClickListeners = ArrayList<(View) -> Unit>()
	private val onStateChangeListener = ArrayList<(State) -> Unit>()

	private val binding: TriStateButtonBinding by lazy {
		TriStateButtonBinding.inflate(
				from(context), this, true
		)
	}

	init {
		context.theme.obtainStyledAttributes(attrs, R.styleable.TriStateButton, defStyleAttr, 0).apply {
			try {
				checkedRes = getResourceIdOrThrow(R.styleable.TriStateButton_button_unchecked)
				uncheckedRes = getResourceIdOrThrow(R.styleable.TriStateButton_button_checked)
				ignoredRes = getResourceId(R.styleable.TriStateButton_button_ignored, 0)
				state = State.fromKey(getResourceId(R.styleable.TriStateButton_state, 0))

				binding.textView.text = getString(R.styleable.TriStateButton_android_text)
						?: ""
			} finally {
				recycle()
			}
		}
		binding.press.setOnClickListener { view: View ->
			// Add onclick listener to pass through to onClick
			onClickListeners.forEach { onClick: (View) -> Unit ->
				onClick(view)
			}

			// Cycle state afterwards
			cycleState()
		}

	}

	fun addOnClickListener(onClick: (View) -> Unit) {
		onClickListeners.add(onClick)
	}

	fun removeOnClickListener(onClick: (View) -> Unit) {
		onClickListeners.remove(onClick)
	}

	fun clearOnClickListeners() {
		onClickListeners.clear()
	}

	fun addOnStateChangeListener(onClick: (State) -> Unit) {
		onStateChangeListener.add(onClick)
	}

	fun removeOnStateChangeListener(onClick: (State) -> Unit) {
		onStateChangeListener.remove(onClick)
	}

	fun clearOnStateChangeListener() {
		onStateChangeListener.clear()
	}

	/**
	 * Cycles through the states of the tristate button
	 */
	fun cycleState() {
		state = when (state) {
			IGNORED -> CHECKED
			CHECKED -> UNCHECKED
			UNCHECKED -> if (skipIgnored) CHECKED else IGNORED
		}
	}

	private fun setDrawable() {
		if (state == IGNORED) {
			if (ignoredRes != 0) {
				binding.imageView.setImageResource(ignoredRes)
			} else binding.imageView.visibility = INVISIBLE
			return
		}
		// If checked / unchecked, set visibility to visible
		binding.imageView.isVisible = true
		binding.imageView.setImageResource(if (state == CHECKED) checkedRes else uncheckedRes)
	}

	enum class State(val key: Int) {
		IGNORED(0), CHECKED(1), UNCHECKED(2);

		companion object {
			fun fromKey(key: Int) = values().find { it.key == key }!!
		}
	}
}