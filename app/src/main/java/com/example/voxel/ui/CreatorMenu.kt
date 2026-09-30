package com.example.voxel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.voxel.entity.MobType
import com.example.voxel.game.GameViewModel

@Composable
fun CreatorMenu(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val theme = settings.uiTheme
    val fontScale = settings.fontScale
    val gameTime by viewModel.gameTime.collectAsState()
    val isHudVisible by viewModel.isHudVisible.collectAsState()

    Dialog(
        onDismissRequest = { viewModel.closeModals() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xF0181E24)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎬", fontSize = (24 * fontScale).sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Creator & Recording Controls",
                            color = Color(0xFFFFD54F),
                            fontSize = (18 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.closeModals() },
                        modifier = Modifier.testTag("close_creator_menu")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // 1. Time of Day Scrubber
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Time of Day (Sun / Night Scrubber): ${(gameTime / 1000).toInt()}k ticks",
                            color = Color.White,
                            fontSize = (13 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = gameTime,
                            onValueChange = { viewModel.setTimeOfDay(it) },
                            valueRange = 0f..24000f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFD54F),
                                activeTrackColor = Color(0xFFFFB300)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { viewModel.setTimeOfDay(0f) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                            ) { Text("Dawn (0k)", fontSize = (10 * fontScale).sp) }
                            Button(
                                onClick = { viewModel.setTimeOfDay(6000f) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                            ) { Text("Noon (6k)", fontSize = (10 * fontScale).sp) }
                            Button(
                                onClick = { viewModel.setTimeOfDay(12000f) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                            ) { Text("Sunset (12k)", fontSize = (10 * fontScale).sp) }
                            Button(
                                onClick = { viewModel.setTimeOfDay(18000f) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                            ) { Text("Midnight (18k)", fontSize = (10 * fontScale).sp) }
                        }
                    }
                }

                // 2. Creator Toggles: Flight, Invulnerability, Hide HUD
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Cinematic & Flight Toggles", color = Color.White, fontSize = (13 * fontScale).sp, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Free Camera / Flying Mode", color = Color(0xFFECEFF1), fontSize = (12 * fontScale).sp)
                            Switch(
                                checked = viewModel.player.isFlying,
                                onCheckedChange = { viewModel.toggleFlight() },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Invulnerability (God Mode)", color = Color(0xFFECEFF1), fontSize = (12 * fontScale).sp)
                            Switch(
                                checked = viewModel.player.isInvulnerable,
                                onCheckedChange = { viewModel.toggleGodMode() },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("HUD Visibility (For recording clean gameplay)", color = Color(0xFFECEFF1), fontSize = (12 * fontScale).sp)
                            Switch(
                                checked = isHudVisible,
                                onCheckedChange = { viewModel.toggleHud() },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.accentColor)
                            )
                        }
                    }
                }

                // 3. Quick Give Resources
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Quick Resource Grant", color = Color.White, fontSize = (13 * fontScale).sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.giveCreatorKit() },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                modifier = Modifier.weight(1f).testTag("give_creator_kit_button")
                            ) {
                                Text("📦 Builder Starter Kit", fontSize = (11 * fontScale).sp)
                            }
                        }
                    }
                }

                // 4. Mob Spawner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Spawn Mobs (In front of player)", color = Color.White, fontSize = (13 * fontScale).sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.spawnMobAtPlayer(MobType.SHEEP) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                                modifier = Modifier.weight(1f)
                            ) { Text("🐑 Sheep", fontSize = (10 * fontScale).sp) }
                            Button(
                                onClick = { viewModel.spawnMobAtPlayer(MobType.COW) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                                modifier = Modifier.weight(1f)
                            ) { Text("🐄 Cow", fontSize = (10 * fontScale).sp) }
                            Button(
                                onClick = { viewModel.spawnMobAtPlayer(MobType.PIG) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                                modifier = Modifier.weight(1f)
                            ) { Text("🐖 Pig", fontSize = (10 * fontScale).sp) }
                            Button(
                                onClick = { viewModel.spawnMobAtPlayer(MobType.VILLAGER) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                                modifier = Modifier.weight(1f)
                            ) { Text("👨‍🌾 Villager", fontSize = (10 * fontScale).sp) }
                            Button(
                                onClick = { viewModel.spawnMobAtPlayer(MobType.ZOMBIE) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                                modifier = Modifier.weight(1f)
                            ) { Text("🧟 Zombie", fontSize = (10 * fontScale).sp) }
                        }
                    }
                }

                // 5. Teleport Buttons
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Locations Teleport", color = Color.White, fontSize = (13 * fontScale).sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.teleportTo(8f, 22f, 8f) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                                modifier = Modifier.weight(1f)
                            ) { Text("📍 Scenic Spawn", fontSize = (11 * fontScale).sp) }
                            Button(
                                onClick = { viewModel.teleportTo(22f, 19f, 22f) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                                modifier = Modifier.weight(1f)
                            ) { Text("🏡 Village Center", fontSize = (11 * fontScale).sp) }
                            Button(
                                onClick = { viewModel.teleportTo(8f, 16f, -8f) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                                modifier = Modifier.weight(1f)
                            ) { Text("🕳️ Starter Cave", fontSize = (11 * fontScale).sp) }
                        }
                    }
                }
            }
        }
    }
}
