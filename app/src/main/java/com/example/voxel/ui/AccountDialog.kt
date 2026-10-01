package com.example.voxel.ui

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.voxel.auth.PlayerSkin
import com.example.voxel.game.GameViewModel

@Composable
fun AccountDialog(
    viewModel: GameViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val theme = settings.uiTheme
    val fontScale = settings.fontScale

    var selectedTab by remember { mutableIntStateOf(0) }
    var refreshAccountTrigger by remember { mutableIntStateOf(0) }

    val accountManager = viewModel.accountManager
    val currentAccount = accountManager.currentAccount

    var usernameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(18.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xF0161B22)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎮", fontSize = (26 * fontScale).sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Central Player Account",
                                color = Color.White,
                                fontSize = (18 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Profile, Skins & Cloud Stats Database",
                                color = Color(0xFF90A4AE),
                                fontSize = (11 * fontScale).sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_account_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Tabs: Profile vs Auth
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF212730),
                    contentColor = theme.accentColor
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("👤 Player Profile & Stats", fontSize = (13 * fontScale).sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("🔐 Cloud Authentication", fontSize = (13 * fontScale).sp, fontWeight = FontWeight.Bold) }
                    )
                }

                if (selectedTab == 0) {
                    // Profile & Stats
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF212730)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(theme.primaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentAccount?.skin?.headEmoji ?: "🧔",
                                        fontSize = (28 * fontScale).sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = currentAccount?.username ?: "Guest Miner",
                                        color = Color.White,
                                        fontSize = (17 * fontScale).sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Level ${currentAccount?.level ?: 1} Adventurer • Active Player",
                                        color = theme.accentColor,
                                        fontSize = (12 * fontScale).sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = currentAccount?.email ?: "miner@voxelcraft.io",
                                        color = Color(0xFF90A4AE),
                                        fontSize = (11 * fontScale).sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Stats Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatCard("⛏️ Mined", "${currentAccount?.blocksMined ?: 0}", fontScale)
                                StatCard("🧱 Placed", "${currentAccount?.blocksPlaced ?: 0}", fontScale)
                                StatCard("⚔️ Mobs Defeated", "${currentAccount?.mobsDefeated ?: 0}", fontScale)
                                StatCard("💎 Highscore", "${(currentAccount?.level ?: 1) * 250}", fontScale)
                            }
                        }
                    }

                    // Skin Selector
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF212730)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Select Player Skin (Rendered in 3D):",
                                color = Color.White,
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(PlayerSkin.values()) { skin ->
                                    val isSelected = (currentAccount?.skin == skin)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) theme.primaryColor.copy(alpha = 0.5f) else Color(0xFF263238))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF455A64),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                accountManager.updateSkin(skin)
                                                refreshAccountTrigger++
                                                Toast.makeText(context, "Equipped Skin: ${skin.displayName}", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(10.dp)
                                            .testTag("skin_${skin.id}")
                                    ) {
                                        Text(skin.headEmoji, fontSize = (28 * fontScale).sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = skin.displayName.split(" ")[0],
                                            color = Color.White,
                                            fontSize = (11 * fontScale).sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Login / Registration Form
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF212730)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isRegisterMode) "Create New Player Account" else "Sign In to Central Database",
                                color = Color.White,
                                fontSize = (15 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = usernameInput,
                                onValueChange = { usernameInput = it },
                                label = { Text("Username") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (isRegisterMode) {
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Email Address") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    val res = if (isRegisterMode) {
                                        accountManager.register(usernameInput, emailInput, passwordInput)
                                    } else {
                                        accountManager.login(usernameInput, passwordInput)
                                    }
                                    res.onSuccess {
                                        Toast.makeText(context, "Welcome, ${it.username}!", Toast.LENGTH_SHORT).show()
                                        selectedTab = 0
                                    }.onFailure {
                                        Toast.makeText(context, it.message ?: "Authentication failed", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isRegisterMode) "Register Account" else "Log In", fontSize = (14 * fontScale).sp)
                            }

                            Button(
                                onClick = { isRegisterMode = !isRegisterMode },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (isRegisterMode) "Already have an account? Sign In" else "Need an account? Register",
                                    fontSize = (12 * fontScale).sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, fontScale: Float) {
    Surface(
        color = Color(0xFF263238),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = Color(0xFFFFD54F), fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color(0xFFCFD8DC), fontSize = (10 * fontScale).sp)
        }
    }
}
