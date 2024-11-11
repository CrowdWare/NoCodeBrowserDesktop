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
import at.crowdware.nocodebrowser.utils.App

expect fun createProjectState(): ProjectState

abstract class ProjectState {

    var isAboutDialogOpen by  mutableStateOf(false)

    var app: App? by mutableStateOf(null)
    var pageName by mutableStateOf("home")

    companion object {
        const val url = "https://crowdware.github.io/NoCodeBrowserDesktop/app.sml"
    }

    fun setNewApp(ap: App) {
        app = ap
    }

    fun navigate(page: String) {
        println("nav: $page")
        pageName = page
    }
}

object GlobalProjectState {
    var projectState: ProjectState? = null
}

