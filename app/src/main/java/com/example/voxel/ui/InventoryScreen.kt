package com.example.voxel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.voxel.game.CraftingRecipe
import com.example.voxel.game.CraftingRecipes
import com.example.voxel.game.GameScreen
import com.example.voxel.game.GameViewModel
import com.example.voxel.game.ItemStack

@Composable
fun InventoryScreen(
    viewModel: GameViewModel,
    isCraftingTable: Boolean = false,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val theme = settings.uiTheme
    val fontScale = settings.fontScale

    var selectedTab by remember { mutableIntStateOf(if (isCraftingTable) 1 else 0) }
    var selectedSlotIndex by remember { mutableStateOf<Int?>(null) }
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
            color = Color(0xF01A1E24)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCraftingTable) "🛠️ Crafting Table (3x3)" else "🎒 Inventory & Pocket Crafting",
                        color = Color.White,
                        fontSize = (18 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { viewModel.closeModals() },
                        modifier = Modifier.testTag("close_inventory_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Tabs: Inventory vs Crafting Recipes
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF263238),
                    contentColor = theme.accentColor
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Inventory", fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                if (isCraftingTable) "Crafting (Table)" else "Crafting (Pocket)",
                                fontSize = (14 * fontScale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    // ================= INVENTORY TAB =================
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left: 36 slots grid (27 main + 9 hotbar)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Storage (27 slots):", color = Color(0xFFAAAAAA), fontSize = (12 * fontScale).sp)
                            // 3 rows of 9 slots = 27 main inventory
                            for (row in 0 until 3) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (col in 0 until 9) {
                                        val slotIdx = 9 + row * 9 + col
                                        SlotView(
                                            slotIndex = slotIdx,
                                            stack = viewModel.inventory.getSlot(slotIdx),
                                            isSelected = selectedSlotIndex == slotIdx,
                                            fontScale = fontScale,
                                            onClick = {
                                                handleSlotClick(viewModel, slotIdx, selectedSlotIndex) {
                                                    selectedSlotIndex = it
                                                    refreshTrigger++
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Hotbar (9 slots):", color = theme.accentColor, fontSize = (12 * fontScale).sp, fontWeight = FontWeight.Bold)
                            // 1 row of 9 hotbar slots (0..8)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (col in 0 until 9) {
                                    val slotIdx = col
                                    SlotView(
                                        slotIndex = slotIdx,
                                        stack = viewModel.inventory.getSlot(slotIdx),
                                        isSelected = selectedSlotIndex == slotIdx,
                                        fontScale = fontScale,
                                        isHotbar = true,
                                        onClick = {
                                            handleSlotClick(viewModel, slotIdx, selectedSlotIndex) {
                                                selectedSlotIndex = it
                                                refreshTrigger++
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Right: Slot Action panel
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .fillMaxHeight(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E252D))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val currentStack = selectedSlotIndex?.let { viewModel.inventory.getSlot(it) }

                                Text(
                                    text = currentStack?.item?.displayName ?: "No Slot Selected",
                                    color = Color.White,
                                    fontSize = (14 * fontScale).sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (currentStack != null) {
                                    Text(
                                        text = currentStack.item.iconEmoji,
                                        fontSize = (36 * fontScale).sp
                                    )
                                    Text(
                                        text = "Amount: ${currentStack.count} / ${currentStack.item.maxStack}",
                                        color = Color(0xFFB0BEC5),
                                        fontSize = (12 * fontScale).sp
                                    )
                                    Text(
                                        text = "Category: ${currentStack.item.category}",
                                        color = Color(0xFF90A4AE),
                                        fontSize = (11 * fontScale).sp
                                    )

                                    Button(
                                        onClick = {
                                            selectedSlotIndex?.let { fromIdx ->
                                                // Find first empty or partial slot to split
                                                for (toIdx in 0 until 36) {
                                                    if (toIdx != fromIdx) {
                                                        viewModel.inventory.splitStack(fromIdx, toIdx)
                                                        viewModel.updateHotbarState()
                                                        refreshTrigger++
                                                        break
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                        modifier = Modifier.fillMaxWidth().testTag("split_stack_button")
                                    ) {
                                        Text("Split Half", fontSize = (12 * fontScale).sp)
                                    }
                                } else {
                                    Text(
                                        text = "Tap a slot to select, then tap another slot to swap or move items.",
                                        color = Color(0xFF78909C),
                                        fontSize = (11 * fontScale).sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // ================= CRAFTING RECIPES TAB =================
                    val allRecipes = CraftingRecipes.ALL_RECIPES
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allRecipes) { recipe ->
                            val canCraft = CraftingRecipes.canCraft(recipe, viewModel.inventory.toList(), isCraftingTable)
                            CraftingRecipeCard(
                                recipe = recipe,
                                canCraft = canCraft,
                                fontScale = fontScale,
                                theme = theme,
                                onCraft = {
                                    viewModel.craftItem(recipe, isCraftingTable)
                                    refreshTrigger++
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun handleSlotClick(
    viewModel: GameViewModel,
    clickedIdx: Int,
    selectedIdx: Int?,
    onSelect: (Int?) -> Unit
) {
    if (selectedIdx == null) {
        if (viewModel.inventory.getSlot(clickedIdx) != null) {
            onSelect(clickedIdx)
        }
    } else if (selectedIdx == clickedIdx) {
        onSelect(null) // Deselect
    } else {
        // Swap or Merge
        viewModel.inventory.swapSlots(selectedIdx, clickedIdx)
        viewModel.updateHotbarState()
        onSelect(null)
    }
}

@Composable
fun SlotView(
    slotIndex: Int,
    stack: ItemStack?,
    isSelected: Boolean,
    fontScale: Float,
    isHotbar: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFF558B2F) else if (isHotbar) Color(0xFF263238) else Color(0xFF1E252D))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF455A64),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .testTag("inv_slot_$slotIndex"),
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
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 2.dp, bottom = 1.dp)
                )
            }
        }
    }
}

@Composable
fun CraftingRecipeCard(
    recipe: CraftingRecipe,
    canCraft: Boolean,
    fontScale: Float,
    theme: com.example.voxel.game.UiTheme,
    onCraft: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (canCraft) Color(0xFF1E2A22) else Color(0xFF21252B)
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
            // Output & Description
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2C3440))
                        .border(1.5.dp, Color(0xFF546E7A), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(recipe.result.item.iconEmoji, fontSize = (22 * fontScale).sp)
                    if (recipe.result.count > 1) {
                        Text(
                            text = "${recipe.result.count}",
                            color = Color.White,
                            fontSize = (11 * fontScale).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = recipe.result.item.displayName,
                        color = Color.White,
                        fontSize = (15 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = recipe.description,
                        color = Color(0xFF90A4AE),
                        fontSize = (12 * fontScale).sp
                    )
                    if (recipe.requiresCraftingTable) {
                        Text(
                            text = "Requires Crafting Table",
                            color = Color(0xFFFFB74D),
                            fontSize = (11 * fontScale).sp
                        )
                    }
                }
            }

            // Craft Action Button
            Button(
                onClick = onCraft,
                enabled = canCraft,
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.primaryColor,
                    disabledContainerColor = Color(0xFF37474F)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("craft_btn_${recipe.id}")
            ) {
                Text(
                    text = if (canCraft) "Craft" else "Missing items",
                    fontSize = (12 * fontScale).sp,
                    color = if (canCraft) Color.White else Color(0xFF78909C)
                )
            }
        }
    }
}
