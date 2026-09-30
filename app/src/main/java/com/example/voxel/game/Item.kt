package com.example.voxel.game

import com.example.voxel.world.BlockType

/**
 * Items in the voxel world, including placable blocks, tools, weapons, and materials.
 */
enum class ItemCategory {
    BLOCK, TOOL, WEAPON, FOOD, MATERIAL
}

enum class Item(
    val id: String,
    val displayName: String,
    val category: ItemCategory,
    val maxStack: Int = 64,
    val blockType: BlockType? = null,
    val damage: Float = 1.0f,
    val miningSpeedMultiplier: Float = 1.0f,
    val harvestLevel: Int = 0, // 0 = hand, 1 = wood, 2 = stone, 3 = iron
    val foodPoints: Int = 0,
    val iconEmoji: String = "📦"
) {
    // Blocks
    GRASS_BLOCK("grass", "Grass Block", ItemCategory.BLOCK, blockType = BlockType.GRASS, iconEmoji = "🌱"),
    DIRT_BLOCK("dirt", "Dirt", ItemCategory.BLOCK, blockType = BlockType.DIRT, iconEmoji = "🟫"),
    STONE_BLOCK("stone", "Stone", ItemCategory.BLOCK, blockType = BlockType.STONE, iconEmoji = "🪨"),
    COBBLESTONE_BLOCK("cobblestone", "Cobblestone", ItemCategory.BLOCK, blockType = BlockType.COBBLESTONE, iconEmoji = "🧱"),
    WOOD_LOG_BLOCK("log", "Oak Log", ItemCategory.BLOCK, blockType = BlockType.WOOD_LOG, iconEmoji = "🪵"),
    WOOD_PLANKS_BLOCK("planks", "Oak Planks", ItemCategory.BLOCK, blockType = BlockType.WOOD_PLANKS, iconEmoji = "🪵"),
    LEAVES_BLOCK("leaves", "Leaves", ItemCategory.BLOCK, blockType = BlockType.LEAVES, iconEmoji = "🍃"),
    SAND_BLOCK("sand", "Sand", ItemCategory.BLOCK, blockType = BlockType.SAND, iconEmoji = "🏖️"),
    COAL_ORE_BLOCK("coal_ore", "Coal Ore", ItemCategory.BLOCK, blockType = BlockType.COAL_ORE, iconEmoji = "⬛"),
    IRON_ORE_BLOCK("iron_ore", "Iron Ore", ItemCategory.BLOCK, blockType = BlockType.IRON_ORE, iconEmoji = "🔘"),
    CRAFTING_TABLE_BLOCK("crafting_table", "Crafting Table", ItemCategory.BLOCK, blockType = BlockType.CRAFTING_TABLE, iconEmoji = "🛠️"),
    CHEST_BLOCK("chest", "Chest", ItemCategory.BLOCK, blockType = BlockType.CHEST, iconEmoji = "🧰"),
    TORCH_BLOCK("torch", "Torch", ItemCategory.BLOCK, blockType = BlockType.TORCH, iconEmoji = "🔥"),
    GLASS_BLOCK("glass", "Glass", ItemCategory.BLOCK, blockType = BlockType.GLASS, iconEmoji = "🪟"),
    BRICK_BLOCK("brick", "Bricks", ItemCategory.BLOCK, blockType = BlockType.BRICK, iconEmoji = "🧱"),

    // Materials
    STICK("stick", "Stick", ItemCategory.MATERIAL, maxStack = 64, iconEmoji = "🥢"),
    COAL("coal", "Coal", ItemCategory.MATERIAL, maxStack = 64, iconEmoji = "⚫"),
    IRON_INGOT("iron_ingot", "Iron Ingot", ItemCategory.MATERIAL, maxStack = 64, iconEmoji = "🪙"),
    WOOL("wool", "White Wool", ItemCategory.MATERIAL, maxStack = 64, iconEmoji = "🧶"),
    ROTTEN_FLESH("rotten_flesh", "Rotten Flesh", ItemCategory.MATERIAL, maxStack = 64, iconEmoji = "🥩"),

    // Tools
    WOODEN_PICKAXE("wood_pickaxe", "Wooden Pickaxe", ItemCategory.TOOL, maxStack = 1, miningSpeedMultiplier = 2.0f, harvestLevel = 1, damage = 2.0f, iconEmoji = "⛏️"),
    STONE_PICKAXE("stone_pickaxe", "Stone Pickaxe", ItemCategory.TOOL, maxStack = 1, miningSpeedMultiplier = 4.0f, harvestLevel = 2, damage = 3.0f, iconEmoji = "⛏️"),
    WOODEN_AXE("wood_axe", "Wooden Axe", ItemCategory.TOOL, maxStack = 1, miningSpeedMultiplier = 2.0f, harvestLevel = 1, damage = 3.0f, iconEmoji = "🪓"),
    STONE_AXE("stone_axe", "Stone Axe", ItemCategory.TOOL, maxStack = 1, miningSpeedMultiplier = 3.5f, harvestLevel = 2, damage = 4.0f, iconEmoji = "🪓"),
    WOODEN_SWORD("wood_sword", "Wooden Sword", ItemCategory.WEAPON, maxStack = 1, damage = 4.0f, iconEmoji = "🗡️"),
    STONE_SWORD("stone_sword", "Stone Sword", ItemCategory.WEAPON, maxStack = 1, damage = 6.0f, iconEmoji = "⚔️"),

    // Food
    APPLE("apple", "Apple", ItemCategory.FOOD, maxStack = 64, foodPoints = 4, iconEmoji = "🍎"),
    BREAD("bread", "Bread", ItemCategory.FOOD, maxStack = 64, foodPoints = 5, iconEmoji = "🍞"),
    RAW_BEEF("beef", "Raw Beef", ItemCategory.FOOD, maxStack = 64, foodPoints = 3, iconEmoji = "🥩"),
    RAW_PORKCHOP("porkchop", "Raw Porkchop", ItemCategory.FOOD, maxStack = 64, foodPoints = 3, iconEmoji = "🥓");

    companion object {
        private val BY_ID = values().associateBy { it.id }
        fun fromId(id: String): Item = BY_ID[id] ?: DIRT_BLOCK

        fun fromBlock(block: BlockType): Item {
            return when (block) {
                BlockType.GRASS -> DIRT_BLOCK
                BlockType.DIRT -> DIRT_BLOCK
                BlockType.STONE -> COBBLESTONE_BLOCK
                BlockType.COBBLESTONE -> COBBLESTONE_BLOCK
                BlockType.WOOD_LOG -> WOOD_LOG_BLOCK
                BlockType.WOOD_PLANKS -> WOOD_PLANKS_BLOCK
                BlockType.LEAVES -> APPLE
                BlockType.SAND -> SAND_BLOCK
                BlockType.COAL_ORE -> COAL
                BlockType.IRON_ORE -> IRON_ORE_BLOCK
                BlockType.CRAFTING_TABLE -> CRAFTING_TABLE_BLOCK
                BlockType.CHEST -> CHEST_BLOCK
                BlockType.TORCH -> TORCH_BLOCK
                BlockType.GLASS -> GLASS_BLOCK
                BlockType.BRICK -> BRICK_BLOCK
                else -> DIRT_BLOCK
            }
        }
    }
}

data class ItemStack(
    val item: Item,
    var count: Int
) {
    fun copyStack(newCount: Int = count): ItemStack = ItemStack(item, newCount)
}
