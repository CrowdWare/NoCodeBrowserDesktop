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

package at.crowdware.nocodebrowser.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodebrowser.utils.ContentLoader
import at.crowdware.nocodebrowser.utils.Padding
import at.crowdware.nocodebrowser.utils.Page
import at.crowdware.nocodebrowser.utils.UIElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Bitmap
import java.io.File
import java.io.FileNotFoundException
import java.nio.ByteBuffer


@Composable
fun LoadPage(
    name: String,
    contentLoader: ContentLoader,
) {
    var page: Page? by remember { mutableStateOf(Page(color="#FFFFFF", backgroundColor = "#000000", padding = Padding(0,0,0,0), scrollable = "false", elements = mutableListOf(), title = "Todo"))}
    var isLoading by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    //val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        page = withContext(Dispatchers.IO) {
            contentLoader.loadPage(name)
        }
        isLoading = false
    }
    if (isLoading) {
        Box (modifier = Modifier.fillMaxSize()){
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        if (page != null) {
            val padding = page!!.padding
            val bgColor = page!!.backgroundColor
            //navhostBackground.value = hexToColor(bgColor, MaterialTheme.colorScheme.background)
            var modifier = Modifier as Modifier
            if (page!!.scrollable == "true") {
                modifier = modifier.verticalScroll(scrollState)
            }
            var showSettingsDialog by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                showSettingsDialog = true
                            }
                        )
                    }
            ) {

                Column(
                    modifier = modifier
                        .padding(
                            start = padding.left.dp,
                            top = padding.top.dp,
                            bottom = padding.bottom.dp,
                            end = padding.right.dp
                        )
                        .fillMaxSize()
                        .background(
                            color = hexToColor(
                                bgColor,
                                MaterialTheme.colors.background
                            )
                        )
                ) {
                    RenderPage(page!!, contentLoader)
                }
            }
        }
    }
}

@Composable
fun hexToColor(hex: String, default: Color): Color {
    var value: Color
    if(!hex.startsWith("#")) {
        when(hex) {
            "primary" -> {value = MaterialTheme.colors.primary }
            "onPrimary" -> {value =MaterialTheme.colors.onPrimary }
            //"primaryContainer" -> {value = MaterialTheme.colors.primaryContainer }
            //"onPrimaryContainer" -> {value = MaterialTheme.colors.onPrimaryContainer }
            "surface" -> {value = MaterialTheme.colors.surface  }
            "onSurface" -> {value = MaterialTheme.colors.onSurface }
            "secondary" -> {value = MaterialTheme.colors.secondary }
            "onSecondary" -> {value = MaterialTheme.colors.onSecondary }
            //"secondaryContainer" -> {value = MaterialTheme.colors.secondaryContainer }
            //"onSecondaryContainer" -> {value = MaterialTheme.colors.onSecondaryContainer }
            //"tertiary" -> {value = MaterialTheme.colors.tertiary }
            //"onTertiary" -> {value =MaterialTheme.colors.onTertiary }
            //"tertiaryContainer" -> {value = MaterialTheme.colors.tertiaryContainer }
            //"onTertiaryContainer" -> {value = MaterialTheme.colors.onTertiaryContainer }
            //"outline" -> {value = MaterialTheme.colors.outline  }
            //"outlineVariant" -> {value = MaterialTheme.colors.outlineVariant }
            //"onErrorContainer" -> {value = MaterialTheme.colors.onErrorContainer }
            "onError" -> {value = MaterialTheme.colors.onError }
            //"inverseSurface" -> {value = MaterialTheme.colors.inverseSurface  }
            //"inversePrimary" -> {value = MaterialTheme.colors.inversePrimary }
            //"inverseOnSurface" -> {value = MaterialTheme.colors.inverseOnSurface }
            "background" -> {value = MaterialTheme.colors.background }
            "onBackground" -> {value = MaterialTheme.colors.onBackground }
            "error" -> {value = MaterialTheme.colors.error }
            //"scrim" -> {value = MaterialTheme.colors.scrim }
            else -> {value = default}
        }
        return value
    }

    val color = hex.trimStart('#')
    return when (color.length) {
        6 -> {
            // Hex without alpha (e.g., "RRGGBB")
            val r = color.substring(0, 2).toIntOrNull(16) ?: return Color.Black
            val g = color.substring(2, 4).toIntOrNull(16) ?: return Color.Black
            val b = color.substring(4, 6).toIntOrNull(16) ?: return Color.Black
            Color(r, g, b)
        }
        8 -> {
            // Hex with alpha (e.g., "AARRGGBB")
            val a = color.substring(0, 2).toIntOrNull(16) ?: return Color.Black
            val r = color.substring(2, 4).toIntOrNull(16) ?: return Color.Black
            val g = color.substring(4, 6).toIntOrNull(16) ?: return Color.Black
            val b = color.substring(6, 8).toIntOrNull(16) ?: return Color.Black
            Color(r, g, b, a)
        }
        else -> default
    }
}

@Composable
fun RenderPage(
    page: Page,
    contentLoader: ContentLoader
) {
    for (element in page.elements) {
        RenderElement(element, contentLoader)
    }
}

@Composable
fun RowScope.RenderElement(element: UIElement, contentLoader: ContentLoader) {
    when(element) {
        is UIElement.ColumnElement -> {
            renderColumn(element, contentLoader)
        }
        is UIElement.RowElement -> {
            renderRow(element, contentLoader)
        }
        is UIElement.TextElement -> {
            renderText(element, contentLoader)
        }
        is UIElement.MarkdownElement -> {
            renderMarkdown(element, contentLoader)
        }
        is UIElement.ButtonElement -> {
            renderButton(
                modifier = if(element.weight > 0) Modifier.weight(element.weight.toFloat()) else Modifier,
                element = element,
                contentLoader = contentLoader
            )
        }
        is UIElement.ImageElement -> {
            dynamicImageFromAssets(
                modifier = if(element.weight > 0) Modifier.weight(element.weight.toFloat()) else Modifier,
                filename = element.src,
                scale = element.scale,
                link = element.link,
                contentLoader = contentLoader
            )
        }
        is UIElement.VideoElement -> {
            dynamicVideofromAssets(modifier = if(element.weight > 0) Modifier.weight(element.weight.toFloat()) else Modifier, filename = element.src, contentLoader=contentLoader)
        }
        is UIElement.SoundElement -> {
            dynamicSoundfromAssets(element.src, contentLoader)
        }
        is UIElement.YoutubeElement -> {
            dynamicYoutube(modifier = if(element.weight > 0){Modifier.weight(element.weight.toFloat())} else {Modifier}, videoId = element.id)
        }
        is UIElement.SceneElement -> {
            dynamicScene(
                modifier = if(element.weight > 0){Modifier.weight(element.weight.toFloat())} else {Modifier},
                element,
                contentLoader
            )
        }
        is UIElement.SpacerElement -> {
            var mod = Modifier as Modifier
            if (element.amount > 0)
                mod = mod.then(Modifier.width(element.amount.dp))
            if (element.weight > 0)
                mod = mod.then(Modifier.weight(element.weight.toFloat()))
            Spacer(modifier = mod)
        }
        else -> {}
    }
}

@Composable
fun ColumnScope.RenderElement(element: UIElement, contentLoader: ContentLoader) {
    when (element) {
        is UIElement.ColumnElement -> {
            renderColumn(element, contentLoader)
        }
        is UIElement.RowElement -> {
            renderRow(element, contentLoader)
        }
        is UIElement.TextElement -> {
            renderText(element, contentLoader)
        }
        is UIElement.MarkdownElement -> {
            renderMarkdown(element, contentLoader)
        }
        is UIElement.ButtonElement -> {
            renderButton(modifier= Modifier, element = element, contentLoader = contentLoader)
        }
        is UIElement.ImageElement -> {
            dynamicImageFromAssets(
                modifier = if(element.weight > 0){Modifier.weight(element.weight.toFloat())} else {Modifier},
                filename = element.src,
                scale =element.scale,
                link = element.link,
                contentLoader = contentLoader
            )
        }
        is UIElement.VideoElement -> {
            dynamicVideofromAssets(modifier = if(element.weight > 0){Modifier.weight(element.weight.toFloat())} else {Modifier}, filename = element.src, contentLoader= contentLoader)
        }
        is UIElement.SoundElement -> {
            dynamicSoundfromAssets(element.src, contentLoader)
        }
        is UIElement.YoutubeElement -> {
            dynamicYoutube(modifier = if(element.weight > 0){Modifier.weight(element.weight.toFloat())} else {Modifier}, videoId = element.id)
        }
        is UIElement.SceneElement -> {
            dynamicScene(
                modifier = if(element.weight > 0){Modifier.weight(element.weight.toFloat())} else {Modifier},
                element,
                contentLoader
            )
        }
        is UIElement.SpacerElement -> {
            var mod = Modifier as Modifier
            if (element.amount > 0)
                mod = mod.then(Modifier.height(element.amount.dp))
            if (element.weight > 0)
                mod = mod.then(Modifier.weight(element.weight.toFloat()))
            Spacer(modifier = mod)
        }
        else -> {}
    }
}

@Composable
fun renderColumn(element: UIElement.ColumnElement, contentLoader: ContentLoader) {
    Column (modifier = Modifier.padding(
        top = element.padding.top.dp,
        bottom = element.padding.bottom.dp,
        start = element.padding.left.dp,
        end = element.padding.right.dp
    )) {
        for (ele in element.uiElements) {
            RenderElement(ele, contentLoader)
        }
    }
}

@Composable
fun renderRow(element: UIElement.RowElement, contentLoader: ContentLoader) {
    Row (horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(
        top = element.padding.top.dp,
        bottom = element.padding.bottom.dp,
        start = element.padding.left.dp,
        end = element.padding.right.dp
    )) {
        for (ele in element.uiElements) {
            RenderElement( ele, contentLoader)
        }
    }
}

@Composable
fun renderText(element: UIElement.TextElement, contentLoader: ContentLoader) {
    Text(
        text = element.text.trim(),
        style = TextStyle(
            color = hexToColor(element.color, MaterialTheme.colors.onBackground),
            fontSize = element.fontSize,
            fontWeight = element.fontWeight,
            textAlign = element.textAlign
        )
    )
}

@Composable
fun renderMarkdown(element: UIElement.MarkdownElement, contentLoader: ContentLoader) {
    var cacheName by remember { mutableStateOf("") }
    var text = ""

    if (element.part.isNotEmpty()) {
        LaunchedEffect(element.part) {
            cacheName = withContext(Dispatchers.IO) {
                contentLoader.loadAsset(element.part, "parts")
            }
        }
        if (cacheName.isNotEmpty()) {
                text = loadTextAssetFromCache(cacheName, contentLoader).toString()
                val parsedMarkdown = parseMarkdown(text)
                Text(
                    text = parsedMarkdown,
                    style = TextStyle(
                        color = hexToColor(element.color, MaterialTheme.colors.onBackground),
                        fontSize = element.fontSize,
                        fontWeight = element.fontWeight,
                        textAlign = element.textAlign
                    )
                )

        }
    } else {
        text = element.text.trim()
        val parsedMarkdown = parseMarkdown(text)
        Text(
            text = parsedMarkdown,
            style = TextStyle(
                color = hexToColor(element.color, MaterialTheme.colors.onBackground),
                fontSize = element.fontSize,
                fontWeight = element.fontWeight,
                textAlign = element.textAlign
            )
        )
    }
}

@Composable
fun renderButton(
    modifier: Modifier = Modifier,
    element: UIElement.ButtonElement,
    contentLoader: ContentLoader
) {
    var colors = buttonColors()

    if(element.color.isNotEmpty() && element.backgroundColor.isNotEmpty())
        colors = buttonColors(
            backgroundColor = hexToColor(element.backgroundColor, Color.Unspecified),
            contentColor = hexToColor(element.color, Color.Unspecified))
    else if(element.color.isNotEmpty())
        colors = buttonColors(contentColor = hexToColor(element.color, Color.Unspecified))
    else if(element.backgroundColor.isNotEmpty())
        colors = buttonColors(
            backgroundColor = hexToColor(element.backgroundColor, Color.Unspecified))

    Button(
        modifier = modifier
            .fillMaxWidth()
            .then(if(element.width > 0)Modifier.width(element.width.dp)else Modifier)
            .then(if(element.height > 0)Modifier.height(element.height.dp)else Modifier),
        colors = colors,
        onClick =  { handleButtonClick(element.link) }) {
        Text(text = element.label)
    }
}

@Composable
fun RenderElement(
    element: UIElement,
    contentLoader: ContentLoader
) {
    when (element) {
        is UIElement.ColumnElement -> {
            renderColumn( element, contentLoader)
        }
        is UIElement.RowElement -> {
            renderRow( element, contentLoader)
        }
        is UIElement.TextElement -> {
            renderText(element, contentLoader)
        }
        is UIElement.MarkdownElement -> {
           renderMarkdown(element, contentLoader)
        }
        is UIElement.ButtonElement -> {
            renderButton(modifier = Modifier, element = element, contentLoader)
        }
        is UIElement.ImageElement -> {
            dynamicImageFromAssets(modifier = Modifier, filename = element.src, scale = element.scale, link= element.link, contentLoader)
        }
        is UIElement.VideoElement -> {
            dynamicVideofromAssets(modifier= Modifier, element.src, contentLoader=contentLoader)
        }
        is UIElement.SoundElement -> {
            dynamicSoundfromAssets(element.src, contentLoader)
        }
        is UIElement.YoutubeElement -> {
            dynamicYoutube(modifier = Modifier, videoId = element.id)
        }
        is UIElement.SceneElement -> {
            dynamicScene(modifier = Modifier, element = element, contentLoader)
        }
        else -> {}
    }
}

fun handleButtonClick(
    link: String
) {
    when {
        link.startsWith("page:") -> {
            val pageId = link.removePrefix("page:")
            //navController.navigate(pageId)
        }
        link.startsWith("web:") -> {
            val url = link.removePrefix("web:")
            //mainActivity.openWebPage(url)
        }
        link.startsWith("animation:") -> {
            val aniType = link.removePrefix("animation:")
            //mainActivity.sendToAnimation(aniType)
        }
        else -> {
            println("Unknown link type: $link")
        }
    }
}

@Composable
fun dynamicImageFromAssets(
    modifier: Modifier = Modifier,
    filename: String,
    scale: String,
    link: String,
    contentLoader: ContentLoader
) {
    var cacheName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        cacheName = withContext(Dispatchers.IO) {
            contentLoader.loadAsset(filename, "images")
        }
    }
    if (cacheName.isNotEmpty()) {
        val bitmap = loadImageFromCache( cacheName, contentLoader)
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = when(scale) {
                    "crop" -> ContentScale.Crop
                    "fit" -> ContentScale.Fit
                    "inside" -> ContentScale.Inside
                    "fillwidth" -> ContentScale.FillWidth
                    "fillbounds" -> ContentScale.FillBounds
                    "fillheight" -> ContentScale.FillHeight
                    "none" -> ContentScale.None
                    else -> ContentScale.Fit
                },
                modifier = modifier.fillMaxWidth()
            )
      } else {
          Text(text = "Image [$filename] not found")
        }
    } else {
        Text(text = "Image [$filename] not loaded")
    }
}

@Composable
fun dynamicSoundfromAssets(filename: String, contentLoader: ContentLoader) {
    /*
    var cacheName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        cacheName = withContext(Dispatchers.IO) {
            contentLoader.loadAsset(filename ,"sounds")
        }
    }
    if (cacheName.isNotEmpty()) {
        val exoPlayer = remember { ExoPlayer.Builder().build() }
        val mediaItem = remember(filename) {
            if (filename.startsWith("http")) {
                MediaItem.fromUri(Uri.parse(cacheName))
            } else {
                val file = File(mainActivity.filesDir, cacheName)
                val uri = Uri.fromFile(file)
                MediaItem.fromUri(uri)
            }
        }
        LaunchedEffect(mediaItem) {
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
        exoPlayer.playWhenReady = true

        DisposableEffect(Unit) {
            onDispose {
                exoPlayer.release()
            }
        }
    }
     */
}

@Composable
fun dynamicVideofromAssets(modifier: Modifier = Modifier, filename: String, height: Int = 200, contentLoader: ContentLoader) {
    /*
    var cacheName by remember { mutableStateOf("") }
    if(filename.startsWith("http")) {
        cacheName = filename
    } else {
        LaunchedEffect(Unit) {
            cacheName = withContext(Dispatchers.IO) {
                contentLoader.loadAsset(filename, "videos")
            }
        }
    }
    if (cacheName.isNotEmpty()) {
        val exoPlayer = remember { ExoPlayer.Builder(mainActivity).build() }
        val mediaItem = remember(filename) {
            if (filename.startsWith("http")) {
                MediaItem.fromUri(Uri.parse(cacheName))
            } else {
                val file = File(mainActivity.filesDir, cacheName)
                val uri = Uri.fromFile(file)
                MediaItem.fromUri(uri)
            }
        }
        LaunchedEffect(mediaItem) {
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
        exoPlayer.playWhenReady = true

        DisposableEffect(Unit) {
            onDispose {
                exoPlayer.release()
            }
        }
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                }
            },
            modifier = modifier.fillMaxWidth().height(height.dp)
        )
    }

     */
}

@Composable
fun dynamicYoutube(modifier: Modifier = Modifier, videoId: String, height: Int = 200) {
    /*


    val ctx = LocalContext.current

    AndroidView(
        modifier = modifier.height(height.dp),
        factory = {
            var view = YouTubePlayerView(it)
            val fragment = view.addYouTubePlayerListener(
                object : AbstractYouTubePlayerListener() {
                    override fun onReady(
                        youTubePlayer:
                        YouTubePlayer
                    ) {
                        super.onReady(youTubePlayer)
                        youTubePlayer.loadVideo(videoId, 0f)
                    }
                }
            )
            view
        }
    )

     */
}

fun loadImageFromCache( filename: String, contentLoader: ContentLoader): Bitmap? {
    /*
    return try {
        val file = File(contentLoader.cacheDir, filename)
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

     */
    return null
}

@Composable
fun dynamicScene(modifier: Modifier = Modifier, element: UIElement.SceneElement, contentLoader: ContentLoader) {
    /*
    val context = LocalContext.current
    val mainActivity = context as MainActivity
    var modelCacheName by remember { mutableStateOf("") }
    var iblCacheName by remember { mutableStateOf("") }
    var skyboxCacheName by remember { mutableStateOf("") }
    val surfaceView = remember { SurfaceView(context) }
    val modelViewer = remember(surfaceView) {
        ModelViewer(surfaceView)
    }

    LaunchedEffect(element.model) {
        modelCacheName = withContext(Dispatchers.IO) {
            mainActivity.contentLoader.loadAsset(element.model, "models")
        }
    }
    LaunchedEffect(element.ibl) {
        iblCacheName = withContext(Dispatchers.IO) {
            mainActivity.contentLoader.loadAsset(element.ibl, "models")
        }
    }
    LaunchedEffect(element.skybox) {
        skyboxCacheName = withContext(Dispatchers.IO) {
            mainActivity.contentLoader.loadAsset(element.skybox, "models")
        }
    }
    if (modelCacheName.isNotEmpty() && iblCacheName.isNotEmpty() && skyboxCacheName.isNotEmpty()) {
        try {
            val modelBuffer = loadAssetFromCache(modelCacheName, context)
            if (element.model.endsWith(".gltf")) {
                modelViewer.loadModelGltf(modelBuffer) { uri ->
                    val assetCacheName = runBlocking(Dispatchers.IO) {
                        if (uri.endsWith(".bin")) {
                            mainActivity.contentLoader.loadAsset(uri, "models")
                        } else {
                            mainActivity.contentLoader.loadAsset(uri, "textures")
                        }
                    }
                    loadAssetFromCache(assetCacheName, context)
                }
            } else {
                modelViewer.loadModelGlb(modelBuffer)
            }
            modelViewer.transformToUnitCube()

            val iblBuffer = loadAssetFromCache(iblCacheName, context)
            KTX1Loader.createIndirectLight(modelViewer.engine, iblBuffer).apply {
                intensity = 50_000f
                modelViewer.scene.indirectLight = this
            }

            val skyboxBuffer = loadAssetFromCache(skyboxCacheName, context)
            KTX1Loader.createSkybox(modelViewer.engine, skyboxBuffer).apply {
                modelViewer.scene.skybox = this
            }
            surfaceView.setOnTouchListener(modelViewer)
        } catch (e: Exception) {
            println("Error on load in dynamic scene: ${e.message}")
            e.printStackTrace()
        }
    }

    AndroidView(
        factory = { surfaceView },
        modifier = modifier
            .fillMaxWidth()
            .then(if (element.width > 0) Modifier.width(element.width.dp) else Modifier)
            .then(if (element.height > 0) Modifier.height(element.height.dp) else Modifier)
    )
    val choreographer = remember { Choreographer.getInstance() }
    val startTime = remember { System.nanoTime() }
    val frameCallback = remember {
        object : Choreographer.FrameCallback {
            override fun doFrame(currentTime: Long) {
                try {
                    // Berechne die vergangene Zeit in Sekunden
                    val seconds = (currentTime - startTime).toDouble() / 1_000_000_000
                    choreographer.postFrameCallback(this)

                    // Handle animation and rendering logic
                    modelViewer.animator?.apply {
                        if (animationCount > 0) {
                            applyAnimation(0, seconds.toFloat())
                        }
                        updateBoneMatrices()
                    }
                    modelViewer.render(currentTime)
                } catch(e: Exception) {
                    println("Error in doFrame: ${e.message}")
                }
            }
        }
    }

    DisposableEffect(Unit) {
        choreographer.postFrameCallback(frameCallback)
        onDispose {
            choreographer.removeFrameCallback(frameCallback)
        }
    }

     */
}

fun loadAssetFromCache(assetName: String, contentLoader: ContentLoader): ByteBuffer {
    return try {
        val asset = File(contentLoader.cacheDir, assetName)
        if (asset.exists()) {
            val input = asset.inputStream()
            val bytes = ByteArray(input.available())
            input.read(bytes)
            ByteBuffer.wrap(bytes)
        } else {
            throw FileNotFoundException("Asset not found in cache: $assetName")
        }
    } catch (e: Exception) {
        println("Error loading cached asset [$assetName]: ${e.message}")
        ByteBuffer.allocate(0)
    }
}

fun loadTextAssetFromCache(assetName: String, contentLoader: ContentLoader): String {
    return try {
        val asset = File(contentLoader.cacheDir, assetName)
        if (asset.exists()) {
            asset.readText()
        } else {
            throw FileNotFoundException("Asset not found in cache: $assetName")
        }
    } catch (e: Exception) {
        println("Error loading cached asset [$assetName]: ${e.message}")
        ""
    }
}


fun parseMarkdown(markdown: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val lines = markdown.split("\n")

    for (i in lines.indices) {
        val line = lines[i]
        var j = 0
        while (j < line.length) {
            when {
                line.startsWith("###### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("###### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("##### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("##### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("#### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("#### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("## ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("## ").trim())
                    }
                    j = line.length
                }
                line.startsWith("# ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("# ").trim())
                    }
                    j = line.length
                }
                line.startsWith("***", j) -> {
                    val endIndex = line.indexOf("***", j + 3)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                            append(line.substring(j + 3, endIndex).trim())
                        }
                        j = endIndex + 3
                    } else {
                        builder.append("***")
                        j += 3
                    }
                }
                line.startsWith("**", j) -> {
                    val endIndex = line.indexOf("**", j + 2)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(line.substring(j + 2, endIndex).trim())
                        }
                        j = endIndex + 2
                    } else {
                        builder.append("**")
                        j += 2
                    }
                }
                line.startsWith("*", j) -> {
                    val endIndex = line.indexOf("*", j + 1)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(line.substring(j + 1, endIndex).trim())
                        }
                        j = endIndex + 1
                    } else {
                        builder.append("*")
                        j += 1
                    }
                }
                line.startsWith("~~", j) -> {
                    val endIndex = line.indexOf("~~", j + 2)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(line.substring(j + 2, endIndex).trim())
                        }
                        j = endIndex + 2
                    } else {
                        builder.append("~~")
                        j += 2
                    }
                }
                line.startsWith("(c)", j) || line.startsWith("(C)", j) -> {
                    builder.append("©")
                    j += 3
                }
                line.startsWith("(r)", j) || line.startsWith("(R)", j) -> {
                    builder.append("®")
                    j += 3
                }
                line.startsWith("(tm)", j) || line.startsWith("(TM)", j) -> {
                    builder.append("™")
                    j += 4
                }
                else -> {
                    builder.append(line[j])
                    j++
                }
            }
        }

        if (i < lines.size - 1) {
            builder.append("\n")
        }
    }

    return builder.toAnnotatedString()
}