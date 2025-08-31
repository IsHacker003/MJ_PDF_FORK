/*
 *   MJ PDF
 *   Copyright (C) 2023 Mudlej
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *  --------------------------
 *  This code was previously licensed under
 *
 *  MIT License
 *
 *  Copyright (c) 2018 Gokul Swaminathan
 *  Copyright (c) 2023 Mudlej
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.ishacker.mjpdf.system.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.ishacker.mjpdf.system.R
import com.ishacker.mjpdf.system.data.Preferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        setUpSwitches()
    }

    private fun setUpSwitches() {
        setVisualSection()
        setBehaviorSection()
        setTextSection()
        setExperimentalSection()
    }

    private fun setVisualSection() {
        val qualitySwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.quality)
            setDefaultValue(Preferences.highQualityDefault)
            key = Preferences.highQualityKey
            isIconSpaceReserved = false
        }
        val aliasSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.anti_aliasing)
            setDefaultValue(Preferences.antiAliasingDefault)
            key = Preferences.antiAliasingKey
            isIconSpaceReserved = false
        }
        val screenOnSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.keep_screen_on)
            setDefaultValue(Preferences.screenOnDefault)
            key = Preferences.screenOnKey
            isIconSpaceReserved = false
        }
        val spaceBetweenPages = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.space_between_pages)
            setDefaultValue(Preferences.spaceBetweenPagesDefault)
            key = Preferences.spaceBetweenPagesKey
            isIconSpaceReserved = false
        }

        val section: PreferenceCategory? = findPreference("visualSection")
        section?.apply {
            isIconSpaceReserved = false
            addPreference(qualitySwitch)
            addPreference(aliasSwitch)
            addPreference(screenOnSwitch)
            addPreference(spaceBetweenPages)
        }
    }

    private fun setBehaviorSection() {
        val doubleTapToExitSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.double_tap_to_exit)
            setDefaultValue(Preferences.doubleTapToExitEnabledDefault)
            key = Preferences.doubleTapToExitEnabledKey
            isIconSpaceReserved = false
        }
        val horizontalScrollSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.horizontal_scrolling_mode)
            setDefaultValue(Preferences.horizontalScrollDefault)
            key = Preferences.horizontalScrollKey
            isIconSpaceReserved = false
        }
        val autoFullScreenSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.auto_full_screen)
            setDefaultValue(Preferences.autoFullScreenDefault)
            key = Preferences.autoFullScreenKey
            summary = getString(R.string.auto_full_screen_summary)
            isIconSpaceReserved = false
        }
        val autoFullScreenHorizontalSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.always_horizontal)
            setDefaultValue(Preferences.alwaysHorizontalDefault)
            key = Preferences.alwaysHorizontalKey
            summary = getString(R.string.always_horizontal_summary)
            isIconSpaceReserved = false
        }
        val pageSnapSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.snap)
            setDefaultValue(Preferences.pageSnapDefault)
            key = Preferences.pageSnapKey
            summary = getString(R.string.snap_summary)
            isIconSpaceReserved = false
        }
        val pageFlingSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.fling)
            setDefaultValue(Preferences.pageFlingDefault)
            key = Preferences.pageFlingKey
            summary = getString(R.string.fling_summary)
            isIconSpaceReserved = false
        }
        val turnPageByVolumeButtonsSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.turn_page_by_volume_buttons_title)
            setDefaultValue(Preferences.turnPageByVolumeButtonsDefault)
            key = Preferences.turnPageByVolumeButtonsKey
            summary = getString(R.string.turn_page_by_volume_buttons_summary)
            isIconSpaceReserved = false
        }

        val section: PreferenceCategory? = findPreference("behaviorSection")
        section?.apply {
            isIconSpaceReserved = false
            addPreference(doubleTapToExitSwitch)
            addPreference(horizontalScrollSwitch)
            addPreference(autoFullScreenSwitch)
            addPreference(autoFullScreenHorizontalSwitch)
            addPreference(pageSnapSwitch)
            addPreference(pageFlingSwitch)
            addPreference(turnPageByVolumeButtonsSwitch)
        }
    }
    private fun setTextSection() {
        val showCopyTextDialogSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.show_copy_dialog_title)
            setDefaultValue(Preferences.copyTextDialogDefault)
            key = Preferences.copyTextDialogKey
            summary = getString(R.string.show_copy_dialog_summary)
            isIconSpaceReserved = false
        }

        val section: PreferenceCategory? = findPreference("textSection")
        section?.apply {
            isIconSpaceReserved = false
            addPreference(showCopyTextDialogSwitch)
        }
    }

    private fun setExperimentalSection() {
        val appDarkThemeSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.dark_theme_for_app)
            setDefaultValue(Preferences.appFollowSystemThemeDefault)
            key = Preferences.appFollowSystemThemeKey
            summary = getString(R.string.app_dark_theme_summary)
            isIconSpaceReserved = false

            // set a caution dialog to show for this option
            setOnPreferenceClickListener {
                // don't show the dialog when turning it off
                if (!isChecked) {
                    setDefaultNightMode(MODE_NIGHT_NO)
                    return@setOnPreferenceClickListener true
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.caution))
                    .setMessage(getString(R.string.app_dark_dialog_message))
                    .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                        dialog.dismiss()
                        setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                    .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                        isChecked = false
                    }
                    .create().show()
                return@setOnPreferenceClickListener true
            }
        }
        val pdfDarkThemeSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.dark_theme_for_pdf)
            setDefaultValue(Preferences.pdfFollowSystemThemeDefault)
            key = Preferences.pdfFollowSystemThemeKey
            summary = getString(R.string.pdf_dark_theme_summary)
            isIconSpaceReserved = false
        }

        val enableReloadButtonSwitch = SwitchPreferenceCompat(requireContext()).apply {
            title = getString(R.string.enable_reload_button)
            setDefaultValue(Preferences.enableReloadButtonDefault)
            key = Preferences.enableReloadButtonKey
            summary = getString(R.string.enable_reload_summary)
            isIconSpaceReserved = false
        }

        val section: PreferenceCategory? = findPreference("experimentalSection")
        section?.apply {
            isIconSpaceReserved = false
            addPreference(appDarkThemeSwitch)
            addPreference(pdfDarkThemeSwitch)
            addPreference(enableReloadButtonSwitch)
        }
    }
}