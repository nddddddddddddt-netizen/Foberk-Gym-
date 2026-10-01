package com.example.voxel.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.voxel.game.GameViewModel
import com.example.voxel.mod.GameMod
import com.example.voxel.mod.ModPlatform
import com.example.voxel.mod.TextureConverter
import com.example.voxel.mod.TexturePack
import com.example.voxel.mod.WorldMapImporter

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ModBrowserScreen(
    viewModel: GameViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val theme = settings.uiTheme
    val fontScale = settings.fontScale

    var selectedTab by remember { mutableIntStateOf(0) }
    var refreshModsTrigger by remember { mutableIntStateOf(0) }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var webLoading by remember { mutableStateOf(false) }
    var webError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(14.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xF014181F)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧩", fontSize = (24 * fontScale).sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Menafex Mod Engine & Browser",
                                color = Color(0xFFFFD54F),
                                fontSize = (18 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bedrock Addons & Java Mods Runtime • v1.1",
                                color = Color(0xFFB0BEC5),
                                fontSize = (11 * fontScale).sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_mod_browser_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF212730),
                    contentColor = theme.accentColor
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("🌐 Website Browser", fontSize = (13 * fontScale).sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("⚡ 1-Click Mods (${viewModel.modManager.activeMods.size} Active)", fontSize = (13 * fontScale).sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("🗺️ Maps & Worlds", fontSize = (13 * fontScale).sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("🎨 Textures & Shaders", fontSize = (13 * fontScale).sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (selectedTab) {
                    0 -> {
                        // ================= 1. EMBEDDED WEBVIEW BROWSER =================
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Browser Toolbar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (webViewInstance?.canGoBack() == true) webViewInstance?.goBack()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                                    }
                                    IconButton(
                                        onClick = { webViewInstance?.reload() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "https://menafex.xo.je/index",
                                        color = Color(0xFF81C784),
                                        fontSize = (12 * fontScale).sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Button(
                                    onClick = {
                                        // Switch to 1-Click tab
                                        selectedTab = 1
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("📥 Install Site Mods", fontSize = (11 * fontScale).sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))) {
                                AndroidView(
                                    factory = { ctx ->
                                        WebView(ctx).apply {
                                            val webSettings = this.settings
                                            webSettings.javaScriptEnabled = true
                                            webSettings.domStorageEnabled = true
                                            webSettings.loadWithOverviewMode = true
                                            webSettings.useWideViewPort = true
                                            webSettings.setSupportZoom(true)
                                            webSettings.builtInZoomControls = true
                                            webSettings.displayZoomControls = false

                                            webViewClient = object : WebViewClient() {
                                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                                    webLoading = true
                                                    webError = null
                                                }

                                                override fun onPageFinished(view: WebView?, url: String?) {
                                                    webLoading = false
                                                }

                                                override fun onReceivedError(
                                                    view: WebView?,
                                                    request: WebResourceRequest?,
                                                    error: WebResourceError?
                                                ) {
                                                    webLoading = false
                                                    webError = "Could not reach network host: menafex.xo.je (Offline preview active)"
                                                }
                                            }

                                            loadUrl("https://menafex.xo.je/index")
                                            webViewInstance = this
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )

                                if (webLoading) {
                                    Surface(
                                        color = Color(0xAA000000),
                                        modifier = Modifier.align(Alignment.Center),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("🌐 Loading menafex.xo.je...", color = Color.White, modifier = Modifier.padding(12.dp))
                                    }
                                }

                                if (webError != null) {
                                    Surface(
                                        color = Color(0xDD212121),
                                        modifier = Modifier.align(Alignment.Center).padding(20.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("🌐 Site Catalog Ready", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("The native Bedrock and Java mods from menafex.xo.je are pre-loaded in the '1-Click Mods' tab.", color = Color(0xFFCFD8DC), fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = { selectedTab = 1 },
                                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor)
                                            ) {
                                                Text("Open 1-Click Mods Catalog", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // ================= 2. 1-CLICK MODS MARKETPLACE =================
                        val modsList = viewModel.modManager.availableMods
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(modsList) { mod ->
                                ModCard(
                                    mod = mod,
                                    fontScale = fontScale,
                                    theme = theme,
                                    onToggle = { enabled ->
                                        viewModel.toggleMod(mod.id, enabled)
                                        refreshModsTrigger++
                                        Toast.makeText(
                                            context,
                                            if (enabled) "${mod.name} Activated in World!" else "${mod.name} Deactivated",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }
                    2 -> {
                        // ================= 3. MAPS & ADVENTURE WORLDS =================
                        val maps = WorldMapImporter.BUNDLED_MAPS
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(maps) { map ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF202731)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(map.iconEmoji, fontSize = (28 * fontScale).sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(map.name, color = Color.White, fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = Color(0xFF37474F),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = map.platform.name,
                                                            color = Color(0xFF81C784),
                                                            fontSize = (9 * fontScale).sp,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Text(map.description, color = Color(0xFFB0BEC5), fontSize = (11 * fontScale).sp)
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.loadAdventureMap(map)
                                                Toast.makeText(context, "Loaded Map: ${map.name}", Toast.LENGTH_SHORT).show()
                                                onClose()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Play Map", fontSize = (12 * fontScale).sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // ================= 4. TEXTURE PACKS & SHADERS =================
                        val packs = TexturePack.values()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(packs) { pack ->
                                val isActive = (pack == TextureConverter.activePack)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) Color(0xFF263A2E) else Color(0xFF202731)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(pack.iconEmoji, fontSize = (28 * fontScale).sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(pack.displayName, color = Color.White, fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = Color(0xFF37474F),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(pack.resolution, color = Color(0xFFFFD54F), fontSize = (9 * fontScale).sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                    }
                                                }
                                                Text(pack.description, color = Color(0xFFB0BEC5), fontSize = (11 * fontScale).sp)
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                TextureConverter.activePack = pack
                                                viewModel.applyTexturePack()
                                                Toast.makeText(context, "Applied ${pack.displayName}", Toast.LENGTH_SHORT).show()
                                                refreshModsTrigger++
                                            },
                                            enabled = !isActive,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = theme.primaryColor,
                                                disabledContainerColor = Color(0xFF37474F)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(if (isActive) "Active" else "Apply", fontSize = (12 * fontScale).sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModCard(
    mod: GameMod,
    fontScale: Float,
    theme: com.example.voxel.game.UiTheme,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (mod.isEnabled) Color(0xFF1E2F26) else Color(0xFF212731)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2C3540)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(mod.iconEmoji, fontSize = (24 * fontScale).sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mod.name,
                            color = Color.White,
                            fontSize = (14 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = when (mod.platform) {
                                ModPlatform.BEDROCK -> Color(0xFF1565C0)
                                ModPlatform.JAVA -> Color(0xFFE65100)
                                else -> Color(0xFF2E7D32)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = mod.platform.name,
                                color = Color.White,
                                fontSize = (9 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = mod.description,
                        color = Color(0xFF90A4AE),
                        fontSize = (11 * fontScale).sp
                    )

                    val details = listOfNotNull(
                        if (mod.customBlocks.isNotEmpty()) "${mod.customBlocks.size} Blocks" else null,
                        if (mod.customItems.isNotEmpty()) "${mod.customItems.size} Items" else null,
                        if (mod.customMobs.isNotEmpty()) "${mod.customMobs.size} Mobs" else null
                    ).joinToString(" • ")

                    if (details.isNotEmpty()) {
                        Text(
                            text = "Adds: $details",
                            color = Color(0xFFFFD54F),
                            fontSize = (10 * fontScale).sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = { onToggle(!mod.isEnabled) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mod.isEnabled) Color(0xFFC62828) else theme.primaryColor
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("toggle_mod_${mod.id}")
            ) {
                Text(
                    text = if (mod.isEnabled) "Deactivate" else "⚡ 1-Click Install",
                    fontSize = (12 * fontScale).sp
                )
            }
        }
    }
}
