package com.example.voxel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voxel.game.GameScreen
import com.example.voxel.game.GameViewModel
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun TouchControls(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val health by viewModel.health.collectAsState()
    val hotbar by viewModel.hotbarSlots.collectAsState()
    val selectedIndex by viewModel.selectedHotbarIndex.collectAsState()
    val targetBlock by viewModel.targetedBlock.collectAsState()
    val miningProgress by viewModel.miningProgress.collectAsState()
    val isHudVisible by viewModel.isHudVisible.collectAsState()
    val gameTime by viewModel.gameTime.collectAsState()
    val settings by viewModel.settings.collectAsState()

    if (!isHudVisible) return

    val theme = settings.uiTheme
    val fontScale = settings.fontScale

    Box(modifier = modifier.fillMaxSize()) {

        // ================= TOP BAR =================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Creator Tools, Mod Browser & Time icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.openScreen(GameScreen.CREATOR_TOOLS) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x99000000), RoundedCornerShape(8.dp))
                        .testTag("creator_tools_button")
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Creator Tools", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { viewModel.openScreen(GameScreen.MOD_BROWSER) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x991565C0), RoundedCornerShape(8.dp))
                        .testTag("hud_mod_browser_button")
                ) {
                    Text("🧩", fontSize = (18 * fontScale).sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Time of day badge
                val timeName = when {
                    gameTime in 2000f..10000f -> "☀️ Day"
                    gameTime in 10000f..13000f -> "🌅 Sunset"
                    gameTime in 13000f..21000f -> "🌙 Night"
                    else -> "🌄 Dawn"
                }
                Surface(
                    color = Color(0x99000000),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = timeName,
                        color = Color.White,
                        fontSize = (13 * fontScale).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Center: Health Hearts
            Row(
                modifier = Modifier
                    .background(Color(0x99000000), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val hearts = (health / 2f).coerceIn(0f, 10f)
                for (i in 0 until 10) {
                    val heartChar = when {
                        i < hearts.toInt() -> "❤️"
                        i == hearts.toInt() && (hearts - hearts.toInt()) >= 0.5f -> "💔"
                        else -> "🖤"
                    }
                    Text(text = heartChar, fontSize = (14 * fontScale).sp)
                }
            }

            // Right: Pause & Inventory buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.openScreen(GameScreen.INVENTORY) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(theme.primaryColor.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                        .testTag("inventory_button")
                ) {
                    Text("🎒", fontSize = (20 * fontScale).sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.openScreen(GameScreen.PAUSE) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x99000000), RoundedCornerShape(8.dp))
                        .testTag("pause_button")
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Pause", tint = Color.White)
                }
            }
        }

        // ================= CENTER CROSSHAIR & TARGET INFO =================
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White.copy(alpha = 0.8f), fontSize = 28.sp, fontWeight = FontWeight.Bold)

            // Block targeted tooltip & mining progress
            if (targetBlock != null) {
                Column(
                    modifier = Modifier.offset(y = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = Color(0xAA000000),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = targetBlock!!.blockType.displayName,
                            color = Color.White,
                            fontSize = (12 * fontScale).sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (miningProgress > 0f) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { miningProgress },
                            modifier = Modifier
                                .width(80.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFF9800),
                            trackColor = Color(0x55000000)
                        )
                    }
                }
            }
        }

        // ================= TOUCH INPUT AREAS =================
        // Right-side Touch Look Drag (controls yaw and pitch)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 220.dp, end = 120.dp, top = 60.dp, bottom = 90.dp)
                .pointerInput(settings.touchSensitivity) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val sens = 0.22f * settings.touchSensitivity
                        viewModel.player.yaw = (viewModel.player.yaw + dragAmount.x * sens) % 360f
                        viewModel.player.pitch = (viewModel.player.pitch - dragAmount.y * sens).coerceIn(-89f, 89f)
                    }
                }
        )

        // Left Virtual Joystick
        VirtualJoystick(
            onMove = { x, y ->
                viewModel.joystickX = x
                viewModel.joystickY = y
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 24.dp)
        )

        // Right Action Buttons (Jump, Mine, Place)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Place / Interact Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(theme.primaryColor.copy(alpha = 0.9f))
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .clickable { viewModel.interactOrAttack() }
                    .testTag("interact_button"),
                contentAlignment = Alignment.Center
            ) {
                Text("🛠️", fontSize = 22.sp)
            }

            // Mine / Attack Button (Press & Hold)
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(Color(0xCCB71C1C))
                    .border(2.dp, Color(0xFFFF8A80), CircleShape)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val down = awaitPointerEvent()
                                if (down.changes.any { it.pressed }) {
                                    viewModel.isMineButtonHeld = true
                                } else {
                                    viewModel.isMineButtonHeld = false
                                }
                            }
                        }
                    }
                    .testTag("mine_button"),
                contentAlignment = Alignment.Center
            ) {
                Text("⛏️", fontSize = 24.sp)
            }

            // Jump Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xAA333333))
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                viewModel.isJumpPressed = event.changes.any { it.pressed }
                            }
                        }
                    }
                    .testTag("jump_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Jump", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }

        // ================= BOTTOM 9-SLOT HOTBAR =================
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .background(Color(0xBB111111), RoundedCornerShape(10.dp))
                .border(2.dp, Color(0x88444444), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0 until 9) {
                val stack = hotbar.getOrNull(i)
                val isSelected = (i == selectedIndex)

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) theme.accentColor.copy(alpha = 0.4f) else Color(0x66222222))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFFFFD54F) else Color(0x66777777),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { viewModel.selectHotbarSlot(i) }
                        .testTag("hotbar_slot_$i"),
                    contentAlignment = Alignment.Center
                ) {
                    if (stack != null) {
                        Text(
                            text = stack.item.iconEmoji,
                            fontSize = (18 * fontScale).sp
                        )
                        if (stack.count > 1) {
                            Text(
                                text = "${stack.count}",
                                color = Color.White,
                                fontSize = (11 * fontScale).sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 2.dp, bottom = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VirtualJoystick(
    onMove: (x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = 55f

    Box(
        modifier = modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(Color(0x66000000))
            .border(2.dp, Color(0x66FFFFFF), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(55.dp.toPx(), 55.dp.toPx())
                        val diff = offset - center
                        val dist = diff.getDistance()
                        val clamped = if (dist > maxRadius) diff * (maxRadius / dist) else diff
                        thumbOffset = clamped
                        onMove(clamped.x / maxRadius, -clamped.y / maxRadius)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = thumbOffset + dragAmount
                        val dist = newOffset.getDistance()
                        val clamped = if (dist > maxRadius) newOffset * (maxRadius / dist) else newOffset
                        thumbOffset = clamped
                        onMove(clamped.x / maxRadius, -clamped.y / maxRadius)
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.x.roundToInt(), thumbOffset.y.roundToInt()) }
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xAAFFFFFF))
                .border(2.dp, Color.White, CircleShape)
        )
    }
}
