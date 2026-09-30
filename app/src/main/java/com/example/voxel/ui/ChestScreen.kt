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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.voxel.game.Inventory

@Composable
fun ChestScreen(
    viewModel: GameViewModel,
    chestInventory: Inventory,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val fontScale = settings.fontScale
    var refreshTrigger by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = { viewModel.closeModals() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xF0181F26)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🧰 Storage Chest (Tap item to transfer)",
                        color = Color(0xFFFFD54F),
                        fontSize = (17 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { viewModel.closeModals() },
                        modifier = Modifier.testTag("close_chest_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Chest Section (27 slots)
                Column {
                    Text("Chest Contents (27 Slots):", color = Color(0xFFB0BEC5), fontSize = (12 * fontScale).sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    for (row in 0 until 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (col in 0 until 9) {
                                val idx = row * 9 + col
                                val stack = chestInventory.getSlot(idx)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF263238))
                                        .border(1.dp, Color(0xFF455A64), RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (stack != null) {
                                                // Transfer to player inventory
                                                if (viewModel.inventory.addItem(stack.item, stack.count)) {
                                                    chestInventory.setSlot(idx, null)
                                                    viewModel.soundSystem.playBlockPlace()
                                                    viewModel.updateHotbarState()
                                                    refreshTrigger++
                                                }
                                            }
                                        }
                                        .testTag("chest_slot_$idx"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (stack != null) {
                                        Text(stack.item.iconEmoji, fontSize = (16 * fontScale).sp)
                                        if (stack.count > 1) {
                                            Text(
                                                text = "${stack.count}",
                                                color = Color.White,
                                                fontSize = (10 * fontScale).sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Player Inventory Section (36 slots)
                Column {
                    Text("Your Inventory (Tap to store in chest):", color = Color(0xFF81C784), fontSize = (12 * fontScale).sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    for (row in 0 until 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (col in 0 until 9) {
                                val idx = 9 + row * 9 + col
                                val stack = viewModel.inventory.getSlot(idx)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E252D))
                                        .border(1.dp, Color(0xFF37474F), RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (stack != null) {
                                                if (chestInventory.addItem(stack.item, stack.count)) {
                                                    viewModel.inventory.setSlot(idx, null)
                                                    viewModel.soundSystem.playBlockPlace()
                                                    viewModel.updateHotbarState()
                                                    refreshTrigger++
                                                }
                                            }
                                        }
                                        .testTag("player_inv_to_chest_$idx"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (stack != null) {
                                        Text(stack.item.iconEmoji, fontSize = (16 * fontScale).sp)
                                        if (stack.count > 1) {
                                            Text(
                                                text = "${stack.count}",
                                                color = Color.White,
                                                fontSize = (10 * fontScale).sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
                                            )
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
