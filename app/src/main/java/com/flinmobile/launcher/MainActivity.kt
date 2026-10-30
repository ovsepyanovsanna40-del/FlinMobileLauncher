package com.flinmobile.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class LaunchableApp(val name: String, val packageName: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 10, 18)
        window.navigationBarColor = android.graphics.Color.rgb(8, 10, 18)
        setContent {
            FlinLauncherScreen(
                apps = remember { mutableStateOf(loadApps()) }.value,
                onLaunch = { packageName ->
                    packageManager.getLaunchIntentForPackage(packageName)?.let { startActivity(it) }
                },
                onRefresh = { loadApps() }
            )
        }
    }

    private fun loadApps(): List<LaunchableApp> {
        @Suppress("DEPRECATION")
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .mapNotNull { info ->
                val intent = packageManager.getLaunchIntentForPackage(info.packageName)
                if (intent == null || info.packageName == packageName) null
                else LaunchableApp(packageManager.getApplicationLabel(info).toString(), info.packageName)
            }
            .sortedBy { it.name.lowercase() }
    }
}

private val Ink = Color(0xFF080A12)
private val Panel = Color(0xFF141827)
private val Muted = Color(0xFF929BB1)
private val Accent = Color(0xFF8B7CFF)
private val Mint = Color(0xFF55E6C1)

@Composable
private fun FlinLauncherScreen(
    apps: List<LaunchableApp>,
    onLaunch: (String) -> Unit,
    onRefresh: () -> List<LaunchableApp>
) {
    var query by remember { mutableStateOf("") }
    var currentApps by remember { mutableStateOf(apps) }
    val filtered = currentApps.filter { it.name.contains(query, ignoreCase = true) }
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Ink,
            surface = Panel,
            primary = Accent,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF171A2B), Ink, Ink)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Brush.linearGradient(listOf(Accent, Color(0xFF5144B8)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("F", fontSize = 27.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("FLIN", fontSize = 13.sp, color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                        Text("MOBILE", fontSize = 19.sp, color = Color.White, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Surface(color = Color(0xFF1B2630), shape = CircleShape) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(Mint))
                            Spacer(Modifier.width(7.dp))
                            Text("READY", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
                Text("ТВОЁ ПРОСТРАНСТВО", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(5.dp))
                Text("Всё под рукой.", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(7.dp))
                Text("Запускай любимые приложения в один тап.", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(22.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(17.dp))
                        .background(Color(0xFF1A1E2D))
                        .padding(horizontal = 15.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⌕", color = Accent, fontSize = 26.sp)
                    Spacer(Modifier.width(9.dp))
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Поиск приложений", color = Muted, fontSize = 14.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Accent
                        )
                    )
                    Text(
                        "↻",
                        color = Muted,
                        fontSize = 22.sp,
                        modifier = Modifier.clickable { currentApps = onRefresh() }.padding(5.dp)
                    )
                }

                Spacer(Modifier.height(23.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ПРИЛОЖЕНИЯ", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp)
                    Spacer(Modifier.weight(1f))
                    Text("${filtered.size} доступно", color = Muted, fontSize = 12.sp)
                }
                Spacer(Modifier.height(12.dp))

                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⌕", color = Accent, fontSize = 42.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Ничего не найдено", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("Попробуй другое название", color = Muted, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        items(filtered, key = { it.packageName }) { app ->
                            AppTile(app = app, onClick = { onLaunch(app.packageName) })
                        }
                    }
                }

                Spacer(Modifier.height(5.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FLIN MOBILE", color = Color(0xFF596176), fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(4.dp).clip(CircleShape).background(Accent))
                    Spacer(Modifier.width(8.dp))
                    Text("SIMPLE. FAST. YOURS.", color = Color(0xFF596176), fontSize = 10.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
private fun AppTile(app: LaunchableApp, onClick: () -> Unit) {
    val swatches = listOf(Color(0xFF7768E8), Color(0xFF287F89), Color(0xFFB66B91), Color(0xFFB27C43), Color(0xFF527DBB))
    val tint = swatches[(app.packageName.hashCode().ushr(1)) % swatches.size]
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Panel)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(43.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(app.name.take(1).uppercase(), color = tint, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Text("ОТКРЫТЬ  ↗", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
