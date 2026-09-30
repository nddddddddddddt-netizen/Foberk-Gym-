package com.example.voxel.ui

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.example.voxel.engine.gl.VoxelRenderer
import com.example.voxel.game.GameScreen
import com.example.voxel.game.GameViewModel

@SuppressLint("ClickableViewAccessibility")
@Composable
fun VoxelGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.screen.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val activeChest by viewModel.activeChest.collectAsState()
    val activeVillager by viewModel.activeVillager.collectAsState()
    val targetBlock by viewModel.targetedBlock.collectAsState()

    var glViewRef = remember { mutableStateOf<GLSurfaceView?>(null) }
    val renderer = remember {
        VoxelRenderer(
            world = viewModel.world,
            player = viewModel.player
        )
    }

    // Keep renderer updated with current settings, target, and mobs
    LaunchedEffect(settings.renderDistance) {
        renderer.renderDistance = settings.renderDistance
    }
    LaunchedEffect(targetBlock) {
        renderer.targetedBlock = targetBlock
    }
    LaunchedEffect(viewModel.mobs.size) {
        renderer.mobsList = viewModel.mobs.toList()
    }

    // Handle Android system back button
    BackHandler {
        if (currentScreen != GameScreen.PLAYING) {
            viewModel.closeModals()
        } else {
            viewModel.openScreen(GameScreen.PAUSE)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        // 1. OpenGL ES 2.0 3D Viewport
        AndroidView(
            factory = { context ->
                GLSurfaceView(context).apply {
                    setEGLContextClientVersion(2)
                    setRenderer(renderer)
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                    glViewRef.value = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Damage Flash Overlay
        if (viewModel.player.hurtTimer > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = (viewModel.player.hurtTimer * 1.5f).coerceIn(0f, 0.45f)))
            )
        }

        // 3. In-Game Touch HUD & Controls
        TouchControls(viewModel = viewModel)

        // 4. Modals and Dialogs
        when (currentScreen) {
            GameScreen.INVENTORY -> {
                InventoryScreen(viewModel = viewModel, isCraftingTable = false)
            }
            GameScreen.CRAFTING_TABLE -> {
                InventoryScreen(viewModel = viewModel, isCraftingTable = true)
            }
            GameScreen.CHEST -> {
                activeChest?.let { chest ->
                    ChestScreen(viewModel = viewModel, chestInventory = chest)
                }
            }
            GameScreen.VILLAGER_TRADE -> {
                activeVillager?.let { villager ->
                    VillagerDialog(viewModel = viewModel, villager = villager)
                }
            }
            GameScreen.PAUSE -> {
                PauseMenu(viewModel = viewModel)
            }
            GameScreen.SETTINGS -> {
                SettingsDialog(viewModel = viewModel)
            }
            GameScreen.CREATOR_TOOLS -> {
                CreatorMenu(viewModel = viewModel)
            }
            GameScreen.DEATH -> {
                DeathScreen(viewModel = viewModel)
            }
            GameScreen.PLAYING -> {
                // HUD is rendered
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            glViewRef.value?.onPause()
        }
    }
}
