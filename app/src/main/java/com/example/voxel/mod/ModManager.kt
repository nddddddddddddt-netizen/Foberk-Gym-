package com.example.voxel.mod

import android.content.Context
import com.example.voxel.game.ItemCategory
import java.io.InputStream

/**
 * Unified runtime for managing, loading, enabling, and downloading mods
 * from the site https://menafex.xo.je/index and external files.
 */
class ModManager(private val context: Context) {

    val bedrockEngine = BedrockModEngine()
    val javaEngine = JavaModEngine()

    // Pre-loaded & community mods from https://menafex.xo.je/index
    val availableMods = mutableListOf<GameMod>()
    val activeMods = mutableListOf<GameMod>()

    // Dynamic registered content across all active mods
    val registeredBlocks = mutableMapOf<String, CustomBlockDef>()
    val registeredItems = mutableMapOf<String, CustomItemDef>()
    val registeredRecipes = mutableListOf<CustomRecipeDef>()
    val registeredMobs = mutableMapOf<String, CustomMobDef>()

    init {
        initDefaultCatalog()
    }

    private fun initDefaultCatalog() {
        // 1. Lucky Block (Bedrock & Java Universal)
        val luckyBlockMod = GameMod(
            id = "mod_lucky_block",
            name = "Lucky Block Mod (Bedrock & Java)",
            version = "1.1.0",
            author = "Menafex Community",
            description = "Breaks into surprise gifts, diamond clusters, explosive drops, or rare golden tools!",
            platform = ModPlatform.UNIVERSAL,
            category = ModCategory.BLOCKS_ITEMS,
            iconEmoji = "❓",
            downloadUrl = "https://menafex.xo.je/mods/lucky_block.mcaddon",
            isEnabled = true,
            customBlocks = listOf(
                CustomBlockDef(
                    id = "mod:lucky_block",
                    displayName = "Lucky Block",
                    hardness = 0.5f,
                    isLightSource = true,
                    topColor = floatArrayOf(0.95f, 0.85f, 0.15f, 1f), // Golden yellow
                    bottomColor = floatArrayOf(0.85f, 0.75f, 0.10f, 1f),
                    sideColor = floatArrayOf(0.90f, 0.80f, 0.12f, 1f),
                    dropItemId = "mod:lucky_pickaxe"
                )
            ),
            customItems = listOf(
                CustomItemDef(
                    id = "mod:lucky_block_item",
                    displayName = "Lucky Block",
                    category = ItemCategory.BLOCK,
                    iconEmoji = "❓",
                    associatedBlockId = "mod:lucky_block"
                ),
                CustomItemDef(
                    id = "mod:lucky_pickaxe",
                    displayName = "Golden Lucky Pickaxe",
                    category = ItemCategory.TOOL,
                    miningSpeedMultiplier = 6.0f,
                    harvestLevel = 3,
                    damage = 5.0f,
                    iconEmoji = "⛏️"
                )
            ),
            customRecipes = listOf(
                CustomRecipeDef(
                    id = "craft_lucky_block",
                    resultItemId = "mod:lucky_block_item",
                    resultCount = 1,
                    ingredients = mapOf("planks" to 4, "torch" to 1),
                    requiresCraftingTable = false,
                    description = "4 Planks + 1 Torch → 1 Lucky Block"
                )
            )
        )

        // 2. More Ores: Ruby Equipment (Bedrock Addon)
        val rubyMod = GameMod(
            id = "mod_ruby_ores",
            name = "Ruby Ores & Legendary Weapons",
            version = "1.1.0",
            author = "VoxelBedrockDev",
            description = "Deep underground crystalline Ruby Ore with ultra-durable Ruby Sword and Pickaxe.",
            platform = ModPlatform.BEDROCK,
            category = ModCategory.BLOCKS_ITEMS,
            iconEmoji = "💎",
            downloadUrl = "https://menafex.xo.je/mods/ruby_expansion.mcpack",
            isEnabled = true,
            customBlocks = listOf(
                CustomBlockDef(
                    id = "mod:ruby_ore",
                    displayName = "Ruby Ore",
                    hardness = 3.5f,
                    topColor = floatArrayOf(0.85f, 0.10f, 0.20f, 1f), // Vibrant ruby red
                    bottomColor = floatArrayOf(0.50f, 0.50f, 0.52f, 1f),
                    sideColor = floatArrayOf(0.80f, 0.15f, 0.25f, 1f),
                    dropItemId = "mod:ruby_gem"
                )
            ),
            customItems = listOf(
                CustomItemDef(
                    id = "mod:ruby_gem",
                    displayName = "Ruby Gem",
                    category = ItemCategory.MATERIAL,
                    iconEmoji = "💎"
                ),
                CustomItemDef(
                    id = "mod:ruby_sword",
                    displayName = "Mythic Ruby Sword",
                    category = ItemCategory.WEAPON,
                    damage = 9.0f,
                    iconEmoji = "🗡️"
                ),
                CustomItemDef(
                    id = "mod:ruby_pickaxe",
                    displayName = "Ruby Super Pickaxe",
                    category = ItemCategory.TOOL,
                    miningSpeedMultiplier = 8.0f,
                    harvestLevel = 4,
                    iconEmoji = "⛏️"
                )
            ),
            customRecipes = listOf(
                CustomRecipeDef(
                    id = "craft_ruby_sword",
                    resultItemId = "mod:ruby_sword",
                    resultCount = 1,
                    ingredients = mapOf("cobblestone" to 2, "stick" to 1),
                    requiresCraftingTable = true,
                    description = "2 Cobblestone + 1 Stick → 1 Ruby Sword"
                )
            )
        )

        // 3. DecoCraft Furniture (Bedrock & Java)
        val furnitureMod = GameMod(
            id = "mod_decocraft",
            name = "Modern Furniture & Decocraft",
            version = "1.1.0",
            author = "InteriorCrafters",
            description = "Adds luxury interior blocks: Velvet Sofa, Modern Desk, Kitchen Fridge, and Table.",
            platform = ModPlatform.UNIVERSAL,
            category = ModCategory.FURNITURE,
            iconEmoji = "🛋️",
            downloadUrl = "https://menafex.xo.je/mods/furniture.mcaddon",
            isEnabled = true,
            customBlocks = listOf(
                CustomBlockDef(
                    id = "mod:sofa",
                    displayName = "Velvet Sofa",
                    hardness = 0.8f,
                    topColor = floatArrayOf(0.65f, 0.15f, 0.15f, 1f),
                    bottomColor = floatArrayOf(0.40f, 0.25f, 0.15f, 1f),
                    sideColor = floatArrayOf(0.70f, 0.20f, 0.20f, 1f)
                ),
                CustomBlockDef(
                    id = "mod:fridge",
                    displayName = "Kitchen Fridge",
                    hardness = 1.2f,
                    topColor = floatArrayOf(0.88f, 0.90f, 0.92f, 1f),
                    bottomColor = floatArrayOf(0.70f, 0.72f, 0.75f, 1f),
                    sideColor = floatArrayOf(0.82f, 0.85f, 0.88f, 1f)
                )
            ),
            customItems = listOf(
                CustomItemDef(
                    id = "mod:sofa_item",
                    displayName = "Velvet Sofa",
                    category = ItemCategory.BLOCK,
                    iconEmoji = "🛋️",
                    associatedBlockId = "mod:sofa"
                ),
                CustomItemDef(
                    id = "mod:fridge_item",
                    displayName = "Kitchen Fridge",
                    category = ItemCategory.BLOCK,
                    iconEmoji = "🧊",
                    associatedBlockId = "mod:fridge"
                )
            ),
            customRecipes = listOf(
                CustomRecipeDef(
                    id = "craft_sofa",
                    resultItemId = "mod:sofa_item",
                    resultCount = 1,
                    ingredients = mapOf("wool" to 2, "planks" to 2),
                    requiresCraftingTable = false,
                    description = "2 Wool + 2 Planks → 1 Velvet Sofa"
                )
            )
        )

        // 4. Mythical Dragons & Bosses (Java DataPack / Mob Engine)
        val dragonMod = GameMod(
            id = "mod_dragons",
            name = "Mythical Dragons & Flying Bosses",
            version = "1.1.0",
            author = "DragonForge",
            description = "Soaring fire drakes and shadow wyverns that roam the mountainous skies.",
            platform = ModPlatform.JAVA,
            category = ModCategory.MOBS_CREATURES,
            iconEmoji = "🐲",
            downloadUrl = "https://menafex.xo.je/mods/dragons.jar",
            isEnabled = true,
            customMobs = listOf(
                CustomMobDef(
                    id = "mod:fire_dragon",
                    displayName = "Crimson Fire Dragon",
                    isHostile = true,
                    maxHealth = 45f,
                    moveSpeed = 0.07f,
                    attackDamage = 6.0f,
                    baseColor = floatArrayOf(0.85f, 0.15f, 0.10f, 1f),
                    accentColor = floatArrayOf(1.0f, 0.65f, 0.10f, 1f),
                    dropItemId = "mod:ruby_gem"
                )
            )
        )

        availableMods.addAll(listOf(luckyBlockMod, rubyMod, furnitureMod, dragonMod))
        rebuildActiveRegistries()
    }

    fun toggleMod(modId: String, enable: Boolean) {
        val mod = availableMods.find { it.id == modId } ?: return
        mod.isEnabled = enable
        rebuildActiveRegistries()
    }

    fun rebuildActiveRegistries() {
        registeredBlocks.clear()
        registeredItems.clear()
        registeredRecipes.clear()
        registeredMobs.clear()
        activeMods.clear()

        for (mod in availableMods) {
            if (mod.isEnabled) {
                activeMods.add(mod)
                for (b in mod.customBlocks) registeredBlocks[b.id] = b
                for (it in mod.customItems) registeredItems[it.id] = it
                for (r in mod.customRecipes) registeredRecipes.add(r)
                for (m in mod.customMobs) registeredMobs[m.id] = m
            }
        }
    }

    fun importModStream(name: String, stream: InputStream): GameMod? {
        val mod = if (name.endsWith(".mcaddon") || name.endsWith(".mcpack")) {
            bedrockEngine.parseMcAddonZip(stream)
        } else {
            javaEngine.parseJavaJar(stream)
        }

        if (mod != null) {
            mod.isEnabled = true
            availableMods.add(0, mod)
            rebuildActiveRegistries()
        }
        return mod
    }
}
