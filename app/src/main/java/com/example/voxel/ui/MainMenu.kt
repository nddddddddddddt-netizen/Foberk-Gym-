package com.example.voxel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voxel.game.GameScreen
import com.example.voxel.game.GameViewModel
import com.example.voxel.game.SaveManager
import com.example.voxel.world.World

@Composable
fun MainMenu(
    viewModel: GameViewModel,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val theme = settings.uiTheme
    val fontScale = settings.fontScale

    val hasSave = remember { SaveManager.hasSave(context) }
    var seedText by remember { mutableStateOf("133742") }
    var showSeedInput by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D1B2A),
                        Color(0xFF1B263B),
                        Color(0xFF415A77)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Title & Branding
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = Color(0x66000000),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎮", fontSize = (20 * fontScale).sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3D FIRST-PERSON SURVIVAL",
                            color = Color(0xFFFFD54F),
                            fontSize = (12 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "VOXEL CRAFT",
                    color = Color.White,
                    fontSize = (38 * fontScale).sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Infinite Procedural World • Mining & Building • Villages & Mobs",
                    color = Color(0xFFB0BEC5),
                    fontSize = (13 * fontScale).sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Feature Highlights Card
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(containerColor = Color(0x77000000)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("✨ Features included:", color = Color.White, fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                        Text("• Seamless Chunk Streaming & Persistent Block Edits", color = Color(0xFFCFD8DC), fontSize = (11 * fontScale).sp)
                        Text("• Animals (Sheep, Cow, Pig), Living Villagers, Night Zombies", color = Color(0xFFCFD8DC), fontSize = (11 * fontScale).sp)
                        Text("• Full Crafting Table & Storage Chest Containers", color = Color(0xFFCFD8DC), fontSize = (11 * fontScale).sp)
                        Text("• Creator Suite with Time Scrubber, Mob Spawner & Free Flight", color = Color(0xFFCFD8DC), fontSize = (11 * fontScale).sp)
                    }
                }
            }

            // Right Action Menu
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xCC111820)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF37474F))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (showSeedInput) {
                        Text("Enter World Seed:", color = Color.White, fontSize = (13 * fontScale).sp)
                        OutlinedTextField(
                            value = seedText,
                            onValueChange = { seedText = it.filter { c -> c.isDigit() || c == '-' } },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = theme.accentColor
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("seed_input")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showSeedInput = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel", fontSize = (12 * fontScale).sp) }

                            Button(
                                onClick = {
                                    val parsedSeed = seedText.toLongOrNull() ?: 133742L
                                    viewModel.world = World(parsedSeed)
                                    viewModel.resetWorld()
                                    onStartGame()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                modifier = Modifier.weight(1f).testTag("start_with_seed_button")
                            ) { Text("Create", fontSize = (12 * fontScale).sp) }
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.resetWorld()
                                onStartGame()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("play_game_button")
                        ) {
                            Text("▶️ Play New World", fontSize = (15 * fontScale).sp, fontWeight = FontWeight.Bold)
                        }

                        if (hasSave) {
                            Button(
                                onClick = {
                                    viewModel.loadGame()
                                    onStartGame()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("continue_saved_button")
                            ) {
                                Text("💾 Continue Saved World", fontSize = (13 * fontScale).sp)
                            }
                        }

                        Button(
                            onClick = { showSeedInput = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("custom_seed_button")
                        ) {
                            Text("🌱 Custom World Seed", fontSize = (13 * fontScale).sp)
                        }

                        Button(
                            onClick = { viewModel.openScreen(GameScreen.SETTINGS) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("main_settings_button")
                        ) {
                            Text("⚙️ Settings & Theme", fontSize = (13 * fontScale).sp)
                        }
                    }
                }
            }
        }

        // Render Settings Dialog if requested from main menu
        val currentScreen by viewModel.screen.collectAsState()
        if (currentScreen == GameScreen.SETTINGS) {
            SettingsDialog(viewModel = viewModel)
        }
    }
}
