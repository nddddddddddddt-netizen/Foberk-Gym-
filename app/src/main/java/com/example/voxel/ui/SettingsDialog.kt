package com.example.voxel.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.voxel.game.GameViewModel
import com.example.voxel.game.UiTheme

@Composable
fun SettingsDialog(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val currentSettings by viewModel.settings.collectAsState()
    val theme = currentSettings.uiTheme
    val fontScale = currentSettings.fontScale

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
                    Text(
                        text = "⚙️ Game & Accessibility Settings",
                        color = Color.White,
                        fontSize = (18 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { viewModel.closeModals() },
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // 1. Theme Color Selection
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Interface Color Theme: ${theme.displayName}",
                            color = Color.White,
                            fontSize = (14 * fontScale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (t in UiTheme.values()) {
                                val isSelected = (t == theme)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.updateSettings(currentSettings.copy(uiTheme = t))
                                        }
                                        .padding(6.dp)
                                        .testTag("theme_${t.name.lowercase()}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(t.primaryColor)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = t.displayName.split(" ")[0],
                                        color = if (isSelected) Color.White else Color(0xFFB0BEC5),
                                        fontSize = (11 * fontScale).sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Dynamic Font Size Ratio Slider
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Font Size Ratio (Dynamic Typography Scale)",
                                color = Color.White,
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(fontScale * 100).toInt()}%",
                                color = theme.accentColor,
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = fontScale,
                            onValueChange = {
                                viewModel.updateSettings(currentSettings.copy(fontScale = it))
                            },
                            valueRange = 0.8f..1.4f,
                            steps = 5,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.accentColor,
                                activeTrackColor = theme.primaryColor
                            )
                        )
                    }
                }

                // 3. Render Distance (Chunks radius)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Render Distance (Streaming Chunks)",
                                color = Color.White,
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${currentSettings.renderDistance} Chunks",
                                color = theme.accentColor,
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = currentSettings.renderDistance.toFloat(),
                            onValueChange = {
                                viewModel.updateSettings(currentSettings.copy(renderDistance = it.toInt()))
                            },
                            valueRange = 2f..5f,
                            steps = 2,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.accentColor,
                                activeTrackColor = theme.primaryColor
                            )
                        )
                    }
                }

                // 4. Sound FX Volume
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sound Effects Volume",
                                color = Color.White,
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(currentSettings.soundVolume * 100).toInt()}%",
                                color = theme.accentColor,
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = currentSettings.soundVolume,
                            onValueChange = {
                                viewModel.updateSettings(currentSettings.copy(soundVolume = it))
                            },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.accentColor,
                                activeTrackColor = theme.primaryColor
                            )
                        )
                    }
                }

                // 5. Touch Sensitivity
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Touch Look Sensitivity",
                                color = Color.White,
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("%.1fx", currentSettings.touchSensitivity),
                                color = theme.accentColor,
                                fontSize = (13 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = currentSettings.touchSensitivity,
                            onValueChange = {
                                viewModel.updateSettings(currentSettings.copy(touchSensitivity = it))
                            },
                            valueRange = 0.5f..2.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.accentColor,
                                activeTrackColor = theme.primaryColor
                            )
                        )
                    }
                }
            }
        }
    }
}
