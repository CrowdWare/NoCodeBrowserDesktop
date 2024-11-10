/****************************************************************************
 * Copyright (C) 2024 CrowdWare
 *
 * This file is part of NoCodeBrowser.
 *
 *  NoCodeBrowser is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeBrowser is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeBrowser.  If not, see <http://www.gnu.org/licenses/>.
 *
 ****************************************************************************/

package at.crowdware.nocodebrowser.utils

import at.crowdware.nocodebrowser.viewmodel.GlobalProjectState
import at.crowdware.nocodebrowser.viewmodel.ProjectState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class Link(val titel: String, val url: String)

class ContentLoader {
    private lateinit var okHttpClient: OkHttpClient
    var app: App? = null
    lateinit var appUrl: String
    private var appLoaded = false
    val links: MutableList<Link> = mutableListOf()
    lateinit var cacheDir: File
    lateinit var projectState: ProjectState

    // Initialize the OkHttp client and setup cache directory
    fun init() {
        projectState = GlobalProjectState.projectState!!
        cacheDir = File(System.getProperty("user.home"), ".noCodeBrowser")

        println("cacheDit: $cacheDir")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        okHttpClient = OkHttpClient.Builder().build()

        val directory = File(cacheDir, "ContentCache")
        if (!directory.exists()) {
            directory.mkdir()
        }

        // load link list
        val file = File(cacheDir, "links.txt")
        if(file.exists()) {
            val list = file.readLines()
            for (line in list) {
                val values = line.split("|")
                links.add(Link(values[0], values[1]))
            }
        }
    }

    fun addLink(title: String, url: String) {
        val file = File(cacheDir, "links.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.appendText("$title|$url\n")
    }

    fun removeLink(title: String, url: String) {
        links.removeIf { it.titel == title && it.url == url }
        val file = File(cacheDir, "links.txt")
        file.delete()
        file.createNewFile()
        for(link in links) {
            file.writeText("${link.titel}|${link.url}\n")
        }
    }

    suspend fun loadAsset(name: String, subdir: String): String {
        var fileContent: ByteArray? = null
        val url = "$appUrl/$subdir/$name"
        if(app == null) {
            return ""
        }
        val result = app!!.deployment.files.find { it.path == "$name" }
        if (result == null) {
            return ""
        }
        val fileName = ("ContentCache/" + appUrl.substringAfter("://") + "/$subdir/").replace(".", "_").replace(":", "_") + "$name"
        val file = File(cacheDir, fileName)
        var ret = true
        if (file.exists()) {
            val lastModifiedMillis = file.lastModified()
            val lastModifiedDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModifiedMillis), ZoneId.systemDefault())
            if (result.time.isAfter(lastModifiedDateTime)) {
                // web server version is newer
                ret = loadAndCacheAsset(url, fileName, result.time)
            }
        } else {
            ret = loadAndCacheAsset(url, fileName, result.time)
        }
        return if (ret)
            fileName
        else
            ""
    }

    suspend fun switchApp(url: String) {
        if(url != appUrl) {
            val app = loadApp(url+ "/app.sml")
            if(app != null) {
                projectState.setNewApp(app)
            }
        }
    }

    suspend fun loadPage(name: String): Page? {
        var lang = "-" + LocaleManager.getLanguage()

        if (appUrl != ProjectState.url.substringBefore("/app.sml"))
            lang = "" // only one language for books, for now
        var fileContent = ""
        val url = "$appUrl/pages$lang/$name.sml"
        println("url: $url")
        if (app == null)
            return null
        for (fil in app!!.deployment.files) {
            println("file: ${fil.path}")
        }
        val result = app!!.deployment.files.find { it.path == "$name.sml"}
        if (result == null) {
            println("page not in ds")
            return null
        }
        val fileName = ("ContentCache/" + appUrl.substringAfter("://") + "/pages$lang/$name").replace(".", "_").replace(":", "_") + ".sml"
        val file = File(cacheDir, fileName)
        println("file: $cacheDir $fileName ${file.exists()}")
        fileContent = if (file.exists()) {
            val lastModifiedMillis = file.lastModified()
            val lastModifiedDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModifiedMillis), ZoneId.systemDefault())
            if (result.time.isAfter(lastModifiedDateTime)) {
                // web server version is newer
                loadAndCacheSml(url, fileName, result.time) ?: ""
            } else {
                // use cache version instead
                file.readText()
            }
        } else {
            loadAndCacheSml(url, fileName, result.time) ?: ""
        }
        var page = parsePage(fileContent)
        if (page.first == null) {
            page = parsePage("Page { Column { padding: \"16\" Text { color: \"#FF0000\" fontSize: 18 text:\"An error occurred loading the home page.\"}}}")
        }
        return page.first
    }

    private suspend fun loadAndCacheSml(url: String, fileName: String, time: LocalDateTime): String? {
        val sml = withContext(Dispatchers.IO) {
            downloadSml(url)
        }
        if (sml != null) {
            val cache = File(cacheDir, fileName)
            // Make sure the parent directories exist
            val parentDir = cache.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }
            cache.writeText(sml)
            val millis = time
                .atZone(ZoneId.systemDefault()) // Convert to ZonedDateTime in the system's default time zone
                .toInstant() // Convert to Instant (which represents a moment in time)
                .toEpochMilli()
            cache.setLastModified(millis)
        }
        return sml
    }

    // Suspend function to load the app asynchronously
    suspend fun loadApp(url: String): App? = withContext(Dispatchers.IO) {
        var fileContent = ""

        appUrl = url.substringBefore("/app.sml")

        val fileName = "ContentCache/" + appUrl.substringAfter("://").replace(".", "_").replace(":", "_") + "/app.sml"
        val file = File(cacheDir, fileName)

        // Make sure the parent directories exist
        val parentDir = file.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        // Check if pre cached file exists
        if (file.exists()) {
            fileContent = file.readText()
        }

        if (!fileContent.contains("at.crowdware.nocodebrowser") || fileContent.isEmpty()) {
            // Download content from the URL
            var appContent = downloadSml(url)
            if (appContent != null) {
                if (fileContent != appContent) {
                    // Write new content to the cache if it has changed, in case web server is offline
                    file.writeText(appContent)
                }
            } else {
                // Use cached content if available
                if (fileContent.isNotEmpty()) {
                    appContent = fileContent
                }
            }

            // Parse the app content
            if (appContent != null && appContent.isNotEmpty()) {
                val result = parseApp(appContent)
                app = result.first
                appLoaded = true
            }
        } else {
            // use pre cached version
            val result = parseApp(fileContent)
            app = result.first
            appLoaded = true
        }
        projectState.app = app
        return@withContext app
    }

    // Suspend function to download content asynchronously
    suspend fun downloadSml(url: String): String? = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        return@withContext try {
            // Use OkHttp's enqueue for asynchronous call
            val response = okHttpClient.newCall(request).await()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    suspend fun downloadAsset(url: String): ByteArray? = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        return@withContext try {
            val response = okHttpClient.newCall(request).await()
            if (response.isSuccessful) {
                // Read the response body as a ByteArray
                response.body?.byteStream()?.use { inputStream ->
                    inputStream.readBytes()
                }
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Extension function to convert OkHttp Call into a suspending function
    private suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isCancelled) return
                continuation.resumeWith(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (continuation.isCancelled) return
                continuation.resumeWith(Result.success(response))
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
                // Handle cancellation exception
            }
        }
    }

    suspend fun loadAndCacheAsset(url: String, fileName: String, time: LocalDateTime): Boolean {
        val bytes = withContext(Dispatchers.IO) {
            downloadAsset(url)
        }
        if (bytes != null) {
            val cache = File(cacheDir, fileName)
            val parentDir = cache.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }
            cache.writeBytes(bytes)
            val millis = time
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            cache.setLastModified(millis)
        }
        return true
    }
}