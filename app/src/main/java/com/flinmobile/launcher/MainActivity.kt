package com.flinmobile.launcher

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

private val Bg = Color(0xFF090909)
private val Panel = Color(0xFF171717)
private val Orange = Color(0xFFFF6B00)
private val OrangeLight = Color(0xFFFFA04D)
private val Muted = Color(0xFF929292)

class MainActivity : ComponentActivity() {
    private val http = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val cacheUrl = "https://github.com/dozapsixoza1/silver-eureka-/releases/download/latest/files_full.zip"
    private val clientUrl = "https://github.com/dozapsixoza1/silver-eureka-/releases/download/latest/flinrp_full.apk"
    private val serverIp = "195.18.27.226"
    private val serverPort = 3146
    private lateinit var downloadDir: File
    private lateinit var gameDir: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 9, 9)
        window.navigationBarColor = android.graphics.Color.rgb(9, 9, 9)
        downloadDir = File(getExternalFilesDir(null), "downloads").apply { mkdirs() }
        gameDir = File(getExternalFilesDir(null), "game").apply { mkdirs() }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Orange, background = Bg, surface = Panel,
                onBackground = Color.White, onSurface = Color.White
            )) {
                LauncherScreen(
                    savedNickname = loadNickname(),
                    ready = checkIfReady(),
                    onNicknameChanged = ::saveNickname,
                    onPlay = ::launchGame,
                    onDownload = ::beginDownload
                )
            }
        }
    }

    @Composable
    private fun LauncherScreen(
        savedNickname: String,
        ready: Boolean,
        onNicknameChanged: (String) -> Unit,
        onPlay: (String) -> Unit,
        onDownload: ((Float, String) -> Unit, () -> Unit, (String) -> Unit) -> Unit
    ) {
        val context = LocalContext.current
        var nickname by remember { mutableStateOf(savedNickname) }
        var installedCache by remember { mutableStateOf(ready) }
        var downloading by remember { mutableStateOf(false) }
        var progress by remember { mutableFloatStateOf(0f) }
        var progressText by remember { mutableStateOf("") }

        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF291205), Bg, Color(0xFF050505)))
            )
        ) {
            Box(
                Modifier.size(250.dp).offset(x = 115.dp, y = 20.dp)
                    .clip(CircleShape).background(Orange.copy(alpha = 0.08f))
                    .align(Alignment.TopEnd)
            )
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = 21.dp).padding(top = 18.dp, bottom = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(OrangeLight, Color(0xFFB83B00))),
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("F", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text("FLIN MOBILE", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                        Text("ROLE PLAY", color = Orange, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.5.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Surface(color = Color(0xFF1C281C), shape = RoundedCornerShape(50)) {
                        Row(Modifier.padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF80D780)))
                            Spacer(Modifier.width(7.dp))
                            Text("MOBILE", color = Color(0xFF9CE39B), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }

                Spacer(Modifier.height(31.dp))
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(25.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF48200C), Color(0xFF21130D), Color(0xFF171313))))
                        .border(1.dp, Orange.copy(alpha = 0.27f), RoundedCornerShape(25.dp))
                        .padding(vertical = 28.dp, horizontal = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ТВОЯ ИГРА. ТВОЯ ИСТОРИЯ.", color = OrangeLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("FLIN", color = Color.White, fontSize = 49.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp, lineHeight = 50.sp)
                    Text("MOBILE", color = Orange, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 7.sp)
                    Spacer(Modifier.height(13.dp))
                    Text("Погрузись в мир Role Play", color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp, textAlign = TextAlign.Center)
                }

                Spacer(Modifier.height(17.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoCard(Modifier.weight(1f), "РЕЖИМ", "ROLE PLAY")
                    InfoCard(Modifier.weight(1f), "СЕРВЕР", "FLIN #1")
                }

                Spacer(Modifier.height(11.dp))
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Panel)
                        .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
                        .padding(17.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("АДРЕС СЕРВЕРА", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                        Spacer(Modifier.weight(1f))
                        Text("FLIN MOBILE #1", color = OrangeLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("$serverIp:$serverPort", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text("Подключение через игровой клиент", color = Muted, fontSize = 11.sp)
                }

                Spacer(Modifier.height(21.dp))
                Column(Modifier.fillMaxWidth()) {
                    Text("ИГРОВОЙ НИКНЕЙМ", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color(0xFF202020))
                            .border(1.dp, Orange.copy(alpha = 0.23f), RoundedCornerShape(15.dp))
                            .padding(horizontal = 15.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✦", color = Orange, fontSize = 17.sp)
                        Spacer(Modifier.width(10.dp))
                        BasicTextField(
                            value = nickname,
                            onValueChange = { nickname = it; onNicknameChanged(it) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                            textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                            decorationBox = { inner ->
                                Box {
                                    if (nickname.isBlank()) Text("Например, Player_Name", color = Color(0xFF777777), fontSize = 14.sp)
                                    inner()
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        if (nickname.isBlank()) {
                            Toast.makeText(context, "Сначала введи игровой никнейм", Toast.LENGTH_SHORT).show()
                        } else if (!installedCache) {
                            downloading = true
                            progressText = "Подготовка загрузки..."
                            onDownload(
                                { value, message -> progress = value.coerceIn(0f, 1f); progressText = message },
                                { downloading = false; installedCache = true; progress = 1f; progressText = "Кэш установлен" },
                                { error -> downloading = false; progressText = "Ошибка загрузки"; Toast.makeText(context, error, Toast.LENGTH_LONG).show() }
                            )
                        } else {
                            onPlay(nickname.trim())
                        }
                    },
                    enabled = !downloading,
                    modifier = Modifier.fillMaxWidth().height(59.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.White, disabledContainerColor = Color(0xFF663300))
                ) {
                    Text(
                        when { downloading -> "ЗАГРУЗКА..."; installedCache -> "ИГРАТЬ  ▶"; else -> "УСТАНОВИТЬ ИГРУ" },
                        fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp
                    )
                }

                if (downloading || progress > 0f) {
                    Spacer(Modifier.height(14.dp))
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Panel).padding(15.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(progressText, color = Color.White.copy(alpha = 0.86f), fontSize = 12.sp)
                            Spacer(Modifier.weight(1f))
                            Text("${(progress * 100).toInt()}%", color = OrangeLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(9.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                            color = Orange, trackColor = Color(0xFF393939)
                        )
                    }
                }

                Spacer(Modifier.height(21.dp))
                Text("FLIN MOBILE  •  ROLE PLAY", color = Color(0xFF5F5F5F), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }
    }

    @Composable
    private fun InfoCard(modifier: Modifier, label: String, value: String) {
        Column(
            modifier.clip(RoundedCornerShape(15.dp)).background(Panel)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(15.dp))
                .padding(horizontal = 13.dp, vertical = 13.dp)
        ) {
            Text(label, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }

    private fun loadNickname(): String =
        getSharedPreferences("flin_prefs", MODE_PRIVATE).getString("nickname", "") ?: ""

    private fun saveNickname(value: String) {
        getSharedPreferences("flin_prefs", MODE_PRIVATE).edit().putString("nickname", value.trim()).apply()
    }

    private fun checkIfReady(): Boolean = File(gameDir, "installed.marker").exists()

    private fun beginDownload(
        onProgress: (Float, String) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    onProgressMain(onProgress, 0.01f, "Скачивание игрового кэша...")
                    val cacheFile = File(downloadDir, "files_full.zip")
                    downloadFile(cacheUrl, cacheFile) { value ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            onProgress(value * 0.70f, "Скачивание кэша... ${(value * 100).toInt()}%")
                        }
                    }
                    onProgressMain(onProgress, 0.72f, "Распаковка игровых файлов...")
                    unzipSafely(cacheFile, gameDir)
                    cacheFile.delete()
                    File(gameDir, "installed.marker").writeText("ok")

                    onProgressMain(onProgress, 0.85f, "Скачивание игрового клиента...")
                    val apkFile = File(downloadDir, "flinrp_full.apk")
                    downloadFile(clientUrl, apkFile) { value ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            onProgress(0.85f + value * 0.10f, "Загрузка клиента... ${(value * 100).toInt()}%")
                        }
                    }
                    val archiveInfo = packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
                        ?: throw IllegalStateException("Скачанный APK повреждён или не является приложением")
                    getSharedPreferences("flin_prefs", MODE_PRIVATE).edit()
                        .putString("client_package", archiveInfo.packageName).apply()
                    withContext(Dispatchers.Main) {
                        onProgress(0.96f, "Открываем установщик клиента...")
                        installApk(apkFile)
                        onProgress(1f, "Кэш готов. Заверши установку клиента.")
                        onComplete()
                    }
                }
            } catch (error: Exception) {
                withContext(Dispatchers.Main) { onError(error.message ?: "Неизвестная ошибка") }
            }
        }
    }

    private suspend fun onProgressMain(callback: (Float, String) -> Unit, value: Float, message: String) {
        withContext(Dispatchers.Main) { callback(value, message) }
    }

    private fun downloadFile(url: String, destination: File, onProgress: (Float) -> Unit) {
        val partial = File(destination.parentFile, destination.name + ".part")
        val request = Request.Builder().url(url).build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Ошибка загрузки: HTTP ${response.code}")
            val body = response.body ?: throw Exception("Сервер вернул пустой файл")
            val total = body.contentLength()
            var downloaded = 0L
            body.byteStream().use { input ->
                FileOutputStream(partial).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val count = input.read(buffer)
                        if (count == -1) break
                        output.write(buffer, 0, count)
                        downloaded += count
                        if (total > 0L) onProgress(downloaded.toFloat() / total.toFloat())
                    }
                    output.fd.sync()
                }
            }
        }
        if (destination.exists()) destination.delete()
        if (!partial.renameTo(destination)) {
            partial.copyTo(destination, overwrite = true)
            partial.delete()
        }
    }

    private fun unzipSafely(zipFile: File, targetDir: File) {
        val root = targetDir.canonicalFile
        ZipInputStream(zipFile.inputStream().buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val output = File(root, entry.name).canonicalFile
                if (output != root && !output.path.startsWith(root.path + File.separator)) {
                    throw SecurityException("Недопустимый путь в архиве")
                }
                if (entry.isDirectory) output.mkdirs()
                else {
                    output.parentFile?.mkdirs()
                    FileOutputStream(output).use { stream -> zip.copyTo(stream) }
                }
                zip.closeEntry()
            }
        }
    }

    private fun installApk(apkFile: File) {
        if (!apkFile.exists() || apkFile.length() == 0L) throw IllegalStateException("Файл игрового клиента не найден")
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, "$packageName.provider", apkFile)
        } else Uri.fromFile(apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    private fun launchGame(nickname: String) {
        saveNickname(nickname)
        val prefs = getSharedPreferences("flin_prefs", MODE_PRIVATE)
        val apkFile = File(downloadDir, "flinrp_full.apk")
        val pkg = prefs.getString("client_package", null)
            ?: if (apkFile.exists()) packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)?.packageName else null

        if (pkg.isNullOrBlank()) {
            Toast.makeText(this, "Сначала нажми «Установить игру», чтобы лаунчер скачал клиент.", Toast.LENGTH_LONG).show()
            return
        }

        val installed = try {
            packageManager.getApplicationInfo(pkg, 0)
            true
        } catch (_: Exception) {
            false
        }

        if (!installed) {
            if (apkFile.exists() && apkFile.length() > 0L) {
                Toast.makeText(this, "Подтверди установку игрового клиента Android.", Toast.LENGTH_LONG).show()
                installApk(apkFile)
            } else {
                Toast.makeText(this, "Файл клиента не найден. Повтори загрузку игры.", Toast.LENGTH_LONG).show()
            }
            return
        }

        val intent = packageManager.getLaunchIntentForPackage(pkg)
        if (intent == null) {
            Toast.makeText(this, "Не удалось открыть игровой клиент.", Toast.LENGTH_LONG).show()
            return
        }
        intent.putExtra("nickname", nickname)
        intent.putExtra("name", nickname)
        intent.putExtra("server", "$serverIp:$serverPort")
        intent.putExtra("ip", serverIp)
        intent.putExtra("port", serverPort)
        startActivity(intent)
    }
}
