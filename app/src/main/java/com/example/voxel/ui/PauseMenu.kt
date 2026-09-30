package com.example.voxel.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.voxel.game.GameScreen
import com.example.voxel.game.GameViewModel

@Composable
fun PauseMenu(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val theme = settings.uiTheme
    val fontScale = settings.fontScale

    Dialog(
        onDismissRequest = { viewModel.closeModals() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .width(320.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xF01A1E24)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "⏸️ Game Paused",
                    color = Color.White,
                    fontSize = (20 * fontScale).sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { viewModel.closeModals() },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                    modifier = Modifier.fillMaxWidth().testTag("resume_game_button")
                ) {
                    Text("▶️ Resume Game", fontSize = (14 * fontScale).sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val saved = viewModel.saveGame()
                        Toast.makeText(context, if (saved) "World & Progress Saved!" else "Failed to save", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.fillMaxWidth().testTag("save_game_button")
                ) {
                    Text("💾 Save World Progress", fontSize = (14 * fontScale).sp)
                }

                Button(
                    onClick = { viewModel.openScreen(GameScreen.SETTINGS) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                    modifier = Modifier.fillMaxWidth().testTag("open_settings_button")
                ) {
                    Text("⚙️ Settings & UI Theme", fontSize = (14 * fontScale).sp)
                }

                Button(
                    onClick = { viewModel.openScreen(GameScreen.CREATOR_TOOLS) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                    modifier = Modifier.fillMaxWidth().testTag("open_creator_tools_button")
                ) {
                    Text("🎬 Creator & Recording Tools", fontSize = (14 * fontScale).sp)
                }

                Button(
                    onClick = {
                        viewModel.resetWorld()
                        Toast.makeText(context, "World Reset to Beginning", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                    modifier = Modifier.fillMaxWidth().testTag("reset_world_button")
                ) {
                    Text("🔄 Reset World", fontSize = (13 * fontScale).sp)
                }
            }
        }
    }
}
