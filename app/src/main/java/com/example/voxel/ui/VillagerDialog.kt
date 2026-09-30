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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.voxel.entity.Mob
import com.example.voxel.game.GameViewModel
import com.example.voxel.game.Item

data class VillagerTrade(
    val id: String,
    val giveItem: Item,
    val giveCount: Int,
    val receiveItem: Item,
    val receiveCount: Int
)

@Composable
fun VillagerDialog(
    viewModel: GameViewModel,
    villager: Mob,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val theme = settings.uiTheme
    val fontScale = settings.fontScale

    val trades = listOf(
        VillagerTrade("t1", Item.WOOD_LOG_BLOCK, 4, Item.BREAD, 2),
        VillagerTrade("t2", Item.IRON_ORE_BLOCK, 2, Item.STONE_PICKAXE, 1),
        VillagerTrade("t3", Item.WOOL, 1, Item.TORCH_BLOCK, 4),
        VillagerTrade("t4", Item.COBBLESTONE_BLOCK, 6, Item.APPLE, 2)
    )

    Dialog(
        onDismissRequest = { viewModel.closeModals() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xF0181F26)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👨‍🌾", fontSize = (28 * fontScale).sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Village Merchant",
                                color = Color.White,
                                fontSize = (18 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Trade resources with the villager",
                                color = Color(0xFF90A4AE),
                                fontSize = (12 * fontScale).sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.closeModals() },
                        modifier = Modifier.testTag("close_villager_trade_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(trades) { trade ->
                        // Check if player has required trade item
                        val playerHas = viewModel.inventory.toList().filterNotNull()
                            .filter { it.item == trade.giveItem }
                            .sumOf { it.count }
                        val canAfford = playerHas >= trade.giveCount

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (canAfford) Color(0xFF233028) else Color(0xFF262C34)
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
                                // Trade visual (Give -> Receive)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF37474F)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(trade.giveItem.iconEmoji, fontSize = (18 * fontScale).sp)
                                        Text(
                                            "${trade.giveCount}",
                                            color = Color.White,
                                            fontSize = (11 * fontScale).sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("➡️", fontSize = (16 * fontScale).sp)
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF37474F)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(trade.receiveItem.iconEmoji, fontSize = (18 * fontScale).sp)
                                        Text(
                                            "${trade.receiveCount}",
                                            color = Color.White,
                                            fontSize = (11 * fontScale).sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            "${trade.giveCount}x ${trade.giveItem.displayName} for ${trade.receiveCount}x ${trade.receiveItem.displayName}",
                                            color = Color.White,
                                            fontSize = (13 * fontScale).sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "You have: $playerHas",
                                            color = if (canAfford) Color(0xFF81C784) else Color(0xFFE57373),
                                            fontSize = (11 * fontScale).sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (canAfford) {
                                            viewModel.inventory.removeItem(trade.giveItem, trade.giveCount)
                                            viewModel.inventory.addItem(trade.receiveItem, trade.receiveCount)
                                            viewModel.soundSystem.playCraftSuccess()
                                            viewModel.updateHotbarState()
                                        }
                                    },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = theme.primaryColor,
                                        disabledContainerColor = Color(0xFF37474F)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("trade_button_${trade.id}")
                                ) {
                                    Text("Trade", fontSize = (12 * fontScale).sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
