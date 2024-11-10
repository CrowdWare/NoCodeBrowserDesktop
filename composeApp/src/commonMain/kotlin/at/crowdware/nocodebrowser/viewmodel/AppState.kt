/*
 * Copyright (C) 2024 CrowdWare
 *
 * This file is part of NoCodeBrowser.
 *
 *  NoCodeDesigner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeBrowser is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeBrowser.  If not, see <http://www.gnu.org/licenses/>.
 */


package at.crowdware.nocodebrowser.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

@Serializable
data class State(
    val windowHeight: Int,
    val windowWidth: Int,
    val windowX: Int,
    val windowY: Int,
    val theme: String
)

class AppState {
    var windowWidth by mutableStateOf(0)
    var windowHeight by mutableStateOf(0)
    var windowX by mutableStateOf(0)
    var windowY by mutableStateOf(0)
    var theme by mutableStateOf("")
}

fun createAppState(): AppState {
    return AppState()
}

object GlobalAppState {
    var appState: AppState? = null
}