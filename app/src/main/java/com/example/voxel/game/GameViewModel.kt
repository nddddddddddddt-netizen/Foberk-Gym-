package com.example.voxel.game

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.voxel.auth.AccountManager
import com.example.voxel.entity.Mob
import com.example.voxel.entity.MobState
import com.example.voxel.entity.MobType
import com.example.voxel.entity.Player
import com.example.voxel.mod.AdventureMap
import com.example.voxel.mod.ModManager
import com.example.voxel.mod.WorldMapImporter
import com.example.voxel.world.BlockType
import com.example.voxel.world.RaycastResult
import com.example.voxel.world.World
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class GameScreen {
    PLAYING,
    INVENTORY,
    CRAFTING_TABLE,
    CHEST,
    VILLAGER_TRADE,
    PAUSE,
    SETTINGS,
    CREATOR_TOOLS,
    DEATH,
    MOD_BROWSER,
    ACCOUNT
}

enum class UiTheme(val displayName: String, val primaryColor: Color, val accentColor: Color) {
    EMERALD("Emerald Grass", Color(0xFF2E7D32), Color(0xFF81C784)),
    DIAMOND("Diamond Sky", Color(0xFF00838F), Color(0xFF4DD0E1)),
    NETHER("Nether Crimson", Color(0xFFC62828), Color(0xFFFF8A80)),
    MIDNIGHT("Midnight Obsidian", Color(0xFF263238), Color(0xFF90A4AE))
}

data class GameSettings(
    val uiTheme: UiTheme = UiTheme.EMERALD,
    val fontScale: Float = 1.0f,
    val renderDistance: Int = 3,
    val soundVolume: Float = 0.8f,
    val touchSensitivity: Float = 1.0f
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val soundSystem = SoundSystem()
    private val vibrator = application.getSystemService(Vibrator::class.java)

    val modManager = ModManager(application)
    val accountManager = AccountManager(application)

    var world = World(133742L)
    var player = Player(8f, 22f, 8f)
    val inventory = Inventory(36)
    val mobs = mutableListOf<Mob>()

    // State flows for Compose UI
    private val _screen = MutableStateFlow(GameScreen.PLAYING)
    val screen: StateFlow<GameScreen> = _screen.asStateFlow()

    private val _health = MutableStateFlow(20f)
    val health: StateFlow<Float> = _health.asStateFlow()

    private val _hotbarSlots = MutableStateFlow<List<ItemStack?>>(List(9) { null })
    val hotbarSlots: StateFlow<List<ItemStack?>> = _hotbarSlots.asStateFlow()

    private val _selectedHotbarIndex = MutableStateFlow(0)
    val selectedHotbarIndex: StateFlow<Int> = _selectedHotbarIndex.asStateFlow()

    private val _targetedBlock = MutableStateFlow<RaycastResult?>(null)
    val targetedBlock: StateFlow<RaycastResult?> = _targetedBlock.asStateFlow()

    private val _miningProgress = MutableStateFlow(0f)
    val miningProgress: StateFlow<Float> = _miningProgress.asStateFlow()

    private val _isHudVisible = MutableStateFlow(true)
    val isHudVisible: StateFlow<Boolean> = _isHudVisible.asStateFlow()

    private val _settings = MutableStateFlow(GameSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private val _activeChest = MutableStateFlow<Inventory?>(null)
    val activeChest: StateFlow<Inventory?> = _activeChest.asStateFlow()

    private val _activeVillager = MutableStateFlow<Mob?>(null)
    val activeVillager: StateFlow<Mob?> = _activeVillager.asStateFlow()

    private val _gameTime = MutableStateFlow(6000f)
    val gameTime: StateFlow<Float> = _gameTime.asStateFlow()

    // Input state from touch controls
    var joystickX: Float = 0f
    var joystickY: Float = 0f
    var isJumpPressed: Boolean = false
    var isMineButtonHeld: Boolean = false

    private var gameLoopJob: Job? = null

    init {
        initStartingInventory()
        initInitialMobs()
        startGameLoop()
    }

    private fun initStartingInventory() {
        inventory.addItem(Item.WOODEN_PICKAXE, 1)
        inventory.addItem(Item.WOODEN_AXE, 1)
        inventory.addItem(Item.WOOD_PLANKS_BLOCK, 16)
        inventory.addItem(Item.TORCH_BLOCK, 8)
        inventory.addItem(Item.BREAD, 4)
        // Pre-package mod items from active Bedrock & Java mods
        inventory.addItem(Item.LUCKY_BLOCK_ITEM, 4)
        inventory.addItem(Item.RUBY_SWORD, 1)
        updateHotbarState()
    }

    private fun initInitialMobs() {
        mobs.clear()
        // Spawn friendly animals near spawn
        mobs.add(Mob(type = MobType.SHEEP, x = 6f, y = 20f, z = 12f))
        mobs.add(Mob(type = MobType.COW, x = 12f, y = 20f, z = 6f))
        mobs.add(Mob(type = MobType.PIG, x = 10f, y = 20f, z = 14f))

        // Spawn villagers in village around (20..26, 17, 20..26)
        mobs.add(Mob(type = MobType.VILLAGER, x = 20f, y = 18f, z = 20f))
        mobs.add(Mob(type = MobType.VILLAGER, x = 24f, y = 18f, z = 22f))
        mobs.add(Mob(type = MobType.VILLAGER, x = 22f, y = 18f, z = 25f))
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTime = System.currentTimeMillis()
            var footstepCounter = 0f

            while (isActive) {
                val now = System.currentTimeMillis()
                val dt = ((now - lastTime) / 1000f).coerceIn(0.001f, 0.1f)
                lastTime = now

                if (_screen.value == GameScreen.PLAYING) {
                    // 1. Advance Day/Night time (24000 ticks = 1 cycle, ~12 minutes full cycle)
                    world.gameTime = (world.gameTime + dt * 35f) % 24000f
                    _gameTime.value = world.gameTime

                    // 2. Update player physics
                    player.update(
                        world = world,
                        moveForward = joystickY,
                        moveStrafe = joystickX,
                        jumpPressed = isJumpPressed,
                        dt = dt,
                        onHurt = {
                            soundSystem.playPlayerHurt()
                            vibrate(100)
                        }
                    )
                    _health.value = player.health

                    if (player.isDead && _screen.value != GameScreen.DEATH) {
                        _screen.value = GameScreen.DEATH
                    }

                    // Footstep sound
                    val speed = sqrt(player.vx * player.vx + player.vz * player.vz)
                    if (player.onGround && speed > 0.03f) {
                        footstepCounter += dt
                        if (footstepCounter > 0.38f) {
                            soundSystem.playFootstep()
                            footstepCounter = 0f
                        }
                    }

                    // 3. Chunk streaming around player
                    world.updateStreaming(player.x, player.z, _settings.value.renderDistance)

                    // 4. Raycast for block targeting
                    val look = player.getLookVector()
                    val hit = world.raycast(player.getEyeX(), player.getEyeY(), player.getEyeZ(), look[0], look[1], look[2], 5.0f)
                    _targetedBlock.value = hit

                    // 5. Handle mining progress
                    if (isMineButtonHeld && hit != null) {
                        handleMiningTick(hit, dt)
                    } else {
                        if (player.miningProgress > 0f) {
                            player.miningProgress = 0f
                            _miningProgress.value = 0f
                        }
                    }

                    // 6. Update Mobs AI
                    val iterator = mobs.iterator()
                    while (iterator.hasNext()) {
                        val mob = iterator.next()
                        if (mob.health <= 0f) {
                            iterator.remove()
                            continue
                        }
                        mob.update(world, player.x, player.y, player.z, mobs, dt)

                        // Zombie attacks player if within melee distance
                        if (mob.type == MobType.ZOMBIE && mob.state == MobState.ATTACKING) {
                            val dist = sqrt((player.x - mob.x) * (player.x - mob.x) + (player.z - mob.z) * (player.z - mob.z))
                            if (dist < 1.6f) {
                                player.takeDamage(3.0f)
                                soundSystem.playPlayerHurt()
                                vibrate(120)
                                mob.state = MobState.IDLE
                            }
                        }

                        // Periodic mob ambient sounds
                        if (mob.soundTimer <= 0f) {
                            when (mob.type) {
                                MobType.ZOMBIE -> soundSystem.playZombieGroan()
                                MobType.COW -> soundSystem.playAnimalSound("cow")
                                MobType.SHEEP -> soundSystem.playAnimalSound("sheep")
                                MobType.PIG -> soundSystem.playAnimalSound("pig")
                                else -> {}
                            }
                            mob.soundTimer = Random.nextFloat() * 15f + 8f
                        }
                    }

                    // 7. Night-time zombie spawning
                    if (world.gameTime in 13000f..21000f && mobs.size < 12 && Random.nextFloat() < 0.005f) {
                        spawnNightZombie()
                    }
                }

                delay(16) // ~60 FPS update rate
            }
        }
    }

    private fun handleMiningTick(hit: RaycastResult, dt: Float) {
        player.handSwingProgress = 1.0f
        player.miningBlockX = hit.hitBlockX
        player.miningBlockY = hit.hitBlockY
        player.miningBlockZ = hit.hitBlockZ

        val heldItem = inventory.currentHeldItem
        val speedMultiplier = heldItem?.item?.miningSpeedMultiplier ?: 1.0f
        val hardness = hit.blockType.hardness.coerceAtLeast(0.1f)

        player.miningProgress += (dt * speedMultiplier / hardness) * 1.5f
        _miningProgress.value = player.miningProgress.coerceIn(0f, 1f)

        if (player.miningProgress >= 1.0f) {
            // Block broken!
            breakTargetBlock(hit)
            player.miningProgress = 0f
            _miningProgress.value = 0f
        }
    }

    fun breakTargetBlock(hit: RaycastResult) {
        val type = hit.blockType
        if (type == BlockType.BEDROCK) return

        accountManager.recordBlockMined()
        world.setBlock(hit.hitBlockX, hit.hitBlockY, hit.hitBlockZ, BlockType.AIR)
        soundSystem.playBlockBreak()
        vibrate(80)

        // Drop item into inventory (including Lucky Block surprise drop!)
        if (type == BlockType.LUCKY_BLOCK) {
            val r = Random.nextFloat()
            when {
                r < 0.35f -> inventory.addItem(Item.RUBY_GEM, 3)
                r < 0.65f -> inventory.addItem(Item.LUCKY_PICKAXE, 1)
                r < 0.85f -> inventory.addItem(Item.BREAD, 6)
                else -> {
                    inventory.addItem(Item.IRON_INGOT, 4)
                    mobs.add(Mob(type = MobType.PIG, x = hit.hitBlockX.toFloat(), y = hit.hitBlockY.toFloat() + 1f, z = hit.hitBlockZ.toFloat()))
                }
            }
        } else {
            val droppedItem = Item.fromBlock(type)
            inventory.addItem(droppedItem, 1)
        }
        updateHotbarState()
    }

    fun placeHeldBlock() {
        val hit = _targetedBlock.value ?: return
        val held = inventory.currentHeldItem ?: return
        val blockType = held.item.blockType ?: return

        // Destination block position
        val placeX = hit.hitBlockX + hit.normalX
        val placeY = hit.hitBlockY + hit.normalY
        val placeZ = hit.hitBlockZ + hit.normalZ

        // Check if destination intersects player AABB
        val playerMinX = floor(player.x - player.width / 2f).toInt()
        val playerMaxX = floor(player.x + player.width / 2f).toInt()
        val playerMinY = floor(player.y).toInt()
        val playerMaxY = floor(player.y + player.height).toInt()
        val playerMinZ = floor(player.z - player.width / 2f).toInt()
        val playerMaxZ = floor(player.z + player.width / 2f).toInt()

        if (placeX in playerMinX..playerMaxX &&
            placeY in playerMinY..playerMaxY &&
            placeZ in playerMinZ..playerMaxZ &&
            blockType.isSolid
        ) {
            return // Cannot place inside player
        }

        accountManager.recordBlockPlaced()
        world.setBlock(placeX, placeY, placeZ, blockType)
        soundSystem.playBlockPlace()
        inventory.consumeHeldItem()
        updateHotbarState()
        player.handSwingProgress = 1.0f
    }

    fun interactOrAttack() {
        player.handSwingProgress = 1.0f

        // Check if looking at a mob first
        val look = player.getLookVector()
        val eyeX = player.getEyeX()
        val eyeY = player.getEyeY()
        val eyeZ = player.getEyeZ()

        var hitMob: Mob? = null
        var minMobDist = 3.5f

        for (mob in mobs) {
            val dx = mob.x - eyeX
            val dy = mob.y + 0.6f - eyeY
            val dz = mob.z - eyeZ
            val dist = sqrt(dx * dx + dy * dy + dz * dz)
            if (dist < minMobDist) {
                // Check view alignment
                val dot = (dx * look[0] + dy * look[1] + dz * look[2]) / dist
                if (dot > 0.85f) {
                    hitMob = mob
                    minMobDist = dist
                }
            }
        }

        if (hitMob != null) {
            if (hitMob.type == MobType.VILLAGER) {
                // Open trading dialog
                _activeVillager.value = hitMob
                _screen.value = GameScreen.VILLAGER_TRADE
                return
            } else {
                // Attack mob!
                val held = inventory.currentHeldItem
                val dmg = held?.item?.damage ?: 1.0f
                val isDead = hitMob.takeDamage(dmg)
                soundSystem.playPlayerHurt()
                vibrate(100)

                if (isDead) {
                    accountManager.recordMobDefeated()
                    when (hitMob.type) {
                        MobType.COW -> inventory.addItem(Item.RAW_BEEF, 2)
                        MobType.PIG -> inventory.addItem(Item.RAW_PORKCHOP, 2)
                        MobType.SHEEP -> {
                            inventory.addItem(Item.WOOL, 1)
                            inventory.addItem(Item.RAW_BEEF, 1)
                        }
                        MobType.ZOMBIE -> {
                            inventory.addItem(Item.ROTTEN_FLESH, 2)
                            if (Random.nextFloat() < 0.25f) inventory.addItem(Item.IRON_INGOT, 1)
                        }
                        MobType.FIRE_DRAGON -> {
                            inventory.addItem(Item.RUBY_GEM, 4)
                            inventory.addItem(Item.RUBY_SWORD, 1)
                        }
                        else -> {}
                    }
                    updateHotbarState()
                }
                return
            }
        }

        // If not mob, check targeted block
        val hit = _targetedBlock.value ?: return
        when (hit.blockType) {
            BlockType.CRAFTING_TABLE -> {
                _screen.value = GameScreen.CRAFTING_TABLE
            }
            BlockType.CHEST -> {
                val chest = world.getChestInventory(hit.hitBlockX, hit.hitBlockY, hit.hitBlockZ)
                _activeChest.value = chest
                _screen.value = GameScreen.CHEST
            }
            else -> {
                // Fallback to place block
                placeHeldBlock()
            }
        }
    }

    private fun spawnNightZombie() {
        val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
        val dist = Random.nextFloat() * 10f + 14f
        val zx = player.x + cos(angle.toDouble()).toFloat() * dist
        val zz = player.z + sin(angle.toDouble()).toFloat() * dist
        val zy = world.generator.getTerrainHeight(floor(zx).toInt(), floor(zz).toInt()) + 1f
        mobs.add(Mob(type = MobType.ZOMBIE, x = zx, y = zy, z = zz))
        soundSystem.playZombieGroan()
    }

    fun selectHotbarSlot(index: Int) {
        if (index in 0..8) {
            inventory.selectedHotbarIndex = index
            _selectedHotbarIndex.value = index
        }
    }

    fun updateHotbarState() {
        _hotbarSlots.value = (0..8).map { inventory.getSlot(it)?.copyStack() }
    }

    fun openScreen(s: GameScreen) {
        _screen.value = s
    }

    fun closeModals() {
        _screen.value = GameScreen.PLAYING
        _activeChest.value = null
        _activeVillager.value = null
        updateHotbarState()
    }

    fun toggleHud() {
        _isHudVisible.value = !_isHudVisible.value
    }

    fun updateSettings(newSettings: GameSettings) {
        _settings.value = newSettings
        soundSystem.volume = newSettings.soundVolume
    }

    fun respawnPlayer() {
        player.respawn(8f, 22f, 8f)
        _health.value = player.health
        _screen.value = GameScreen.PLAYING
    }

    fun craftItem(recipe: CraftingRecipe, atCraftingTable: Boolean): Boolean {
        if (!CraftingRecipes.canCraft(recipe, inventory.toList(), atCraftingTable)) return false

        // Deduct ingredients
        for (ing in recipe.ingredients) {
            inventory.removeItem(ing.item, ing.count)
        }
        // Add result
        inventory.addItem(recipe.result.item, recipe.result.count)
        soundSystem.playCraftSuccess()
        updateHotbarState()
        return true
    }

    // Creator / Debug tools
    fun setTimeOfDay(time: Float) {
        world.gameTime = time
        _gameTime.value = time
    }

    fun giveCreatorKit() {
        inventory.addItem(Item.WOOD_LOG_BLOCK, 32)
        inventory.addItem(Item.COBBLESTONE_BLOCK, 32)
        inventory.addItem(Item.STONE_PICKAXE, 1)
        inventory.addItem(Item.STONE_SWORD, 1)
        inventory.addItem(Item.TORCH_BLOCK, 32)
        inventory.addItem(Item.CHEST_BLOCK, 2)
        inventory.addItem(Item.CRAFTING_TABLE_BLOCK, 1)
        inventory.addItem(Item.BREAD, 16)
        updateHotbarState()
    }

    fun toggleGodMode() {
        player.isInvulnerable = !player.isInvulnerable
        if (player.isInvulnerable) player.health = 20f
    }

    fun toggleFlight() {
        player.isFlying = !player.isFlying
    }

    fun spawnMobAtPlayer(type: MobType) {
        val look = player.getLookVector()
        val sx = player.x + look[0] * 3f
        val sz = player.z + look[2] * 3f
        val sy = player.y
        mobs.add(Mob(type = type, x = sx, y = sy, z = sz))
    }

    fun teleportTo(x: Float, y: Float, z: Float) {
        player.x = x
        player.y = y
        player.z = z
        player.vx = 0f
        player.vy = 0f
        player.vz = 0f
    }

    fun saveGame(): Boolean {
        return SaveManager.saveWorld(
            context = getApplication(),
            world = world,
            playerX = player.x,
            playerY = player.y,
            playerZ = player.z,
            playerYaw = player.yaw,
            playerPitch = player.pitch,
            playerHealth = player.health,
            inventory = inventory
        )
    }

    fun loadGame(): Boolean {
        val data = SaveManager.loadWorld(getApplication()) ?: return false
        world = World(data.seed)
        world.gameTime = data.gameTime
        world.modifiedBlocks.putAll(data.modifiedBlocks)
        world.chestInventories.putAll(data.chestInventories)

        player.respawn(data.playerX, data.playerY, data.playerZ)
        player.yaw = data.playerYaw
        player.pitch = data.playerPitch
        player.health = data.playerHealth

        inventory.clear()
        for (i in 0 until minOf(data.inventory.size, inventory.totalSlots)) {
            inventory.setSlot(i, data.inventory[i])
        }
        inventory.selectedHotbarIndex = data.selectedHotbarIndex
        _selectedHotbarIndex.value = data.selectedHotbarIndex
        updateHotbarState()
        _health.value = player.health
        _gameTime.value = world.gameTime
        return true
    }

    fun resetWorld() {
        SaveManager.deleteSave(getApplication())
        world = World(133742L)
        player.respawn(8f, 22f, 8f)
        inventory.clear()
        initStartingInventory()
        initInitialMobs()
        _health.value = 20f
        _screen.value = GameScreen.PLAYING
    }

    fun toggleMod(modId: String, enable: Boolean) {
        modManager.toggleMod(modId, enable)
        // Mark chunks dirty so custom blocks re-render
        world.chunks.values.forEach { it.isDirty = true }
    }

    fun applyTexturePack() {
        // Mark all chunks dirty to re-mesh with the new texture palette
        world.chunks.values.forEach { it.isDirty = true }
    }

    fun loadAdventureMap(map: AdventureMap) {
        world = World(map.seed)
        WorldMapImporter.applyMapStructures(map, world)
        player.respawn(map.spawnX, map.spawnY, map.spawnZ)
        _health.value = player.health
        _gameTime.value = world.gameTime
        updateHotbarState()
        _screen.value = GameScreen.PLAYING
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
