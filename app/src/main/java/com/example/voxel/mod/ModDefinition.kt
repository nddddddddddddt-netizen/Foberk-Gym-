package com.example.voxel.mod

import com.example.voxel.game.ItemCategory

enum class ModPlatform {
    BEDROCK,
    JAVA,
    UNIVERSAL
}

enum class ModCategory {
    BLOCKS_ITEMS,
    MOBS_CREATURES,
    WORLD_GENERATION,
    SHADERS_TEXTURES,
    FURNITURE,
    MAGIC_TOOLS
}

data class CustomBlockDef(
    val id: String,
    val displayName: String,
    val hardness: Float = 1.5f,
    val isSolid: Boolean = true,
    val isLightSource: Boolean = false,
    val topColor: FloatArray = floatArrayOf(0.8f, 0.2f, 0.2f, 1f),
    val bottomColor: FloatArray = floatArrayOf(0.7f, 0.1f, 0.1f, 1f),
    val sideColor: FloatArray = floatArrayOf(0.75f, 0.15f, 0.15f, 1f),
    val dropItemId: String? = null
)

data class CustomItemDef(
    val id: String,
    val displayName: String,
    val category: ItemCategory = ItemCategory.TOOL,
    val maxStack: Int = 64,
    val damage: Float = 1.0f,
    val miningSpeedMultiplier: Float = 1.0f,
    val harvestLevel: Int = 1,
    val iconEmoji: String = "✨",
    val associatedBlockId: String? = null
)

data class CustomRecipeDef(
    val id: String,
    val resultItemId: String,
    val resultCount: Int,
    val ingredients: Map<String, Int>, // Item ID -> Count
    val requiresCraftingTable: Boolean = false,
    val description: String = ""
)

data class CustomMobDef(
    val id: String,
    val displayName: String,
    val isHostile: Boolean,
    val maxHealth: Float,
    val moveSpeed: Float,
    val attackDamage: Float,
    val baseColor: FloatArray,
    val accentColor: FloatArray,
    val dropItemId: String? = null
)

data class GameMod(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val platform: ModPlatform,
    val category: ModCategory,
    val iconEmoji: String = "🧩",
    val downloadUrl: String = "",
    val websiteUrl: String = "https://menafex.xo.je/index",
    var isEnabled: Boolean = false,
    val customBlocks: List<CustomBlockDef> = emptyList(),
    val customItems: List<CustomItemDef> = emptyList(),
    val customRecipes: List<CustomRecipeDef> = emptyList(),
    val customMobs: List<CustomMobDef> = emptyList()
)
