package com.example.voxel.world

/**
 * Definition of all block types in the Voxel game.
 */
enum class BlockType(
    val id: Byte,
    val displayName: String,
    val isSolid: Boolean,
    val isTransparent: Boolean,
    val isLightSource: Boolean = false,
    val hardness: Float = 1.0f,
    // RGBA colors (0f..1f): [top, bottom, side]
    val topColor: FloatArray = floatArrayOf(0.5f, 0.5f, 0.5f, 1f),
    val bottomColor: FloatArray = floatArrayOf(0.4f, 0.4f, 0.4f, 1f),
    val sideColor: FloatArray = floatArrayOf(0.45f, 0.45f, 0.45f, 1f)
) {
    AIR(
        id = 0,
        displayName = "Air",
        isSolid = false,
        isTransparent = true,
        hardness = 0f
    ),
    GRASS(
        id = 1,
        displayName = "Grass Block",
        isSolid = true,
        isTransparent = false,
        hardness = 0.6f,
        topColor = floatArrayOf(0.38f, 0.68f, 0.28f, 1f),       // Vibrant lush green
        bottomColor = floatArrayOf(0.48f, 0.33f, 0.22f, 1f),    // Rich soil brown
        sideColor = floatArrayOf(0.44f, 0.45f, 0.25f, 1f)      // Grass fringe on dirt
    ),
    DIRT(
        id = 2,
        displayName = "Dirt",
        isSolid = true,
        isTransparent = false,
        hardness = 0.5f,
        topColor = floatArrayOf(0.48f, 0.33f, 0.22f, 1f),
        bottomColor = floatArrayOf(0.48f, 0.33f, 0.22f, 1f),
        sideColor = floatArrayOf(0.48f, 0.33f, 0.22f, 1f)
    ),
    STONE(
        id = 3,
        displayName = "Stone",
        isSolid = true,
        isTransparent = false,
        hardness = 1.5f,
        topColor = floatArrayOf(0.52f, 0.52f, 0.54f, 1f),
        bottomColor = floatArrayOf(0.48f, 0.48f, 0.50f, 1f),
        sideColor = floatArrayOf(0.50f, 0.50f, 0.52f, 1f)
    ),
    COBBLESTONE(
        id = 4,
        displayName = "Cobblestone",
        isSolid = true,
        isTransparent = false,
        hardness = 2.0f,
        topColor = floatArrayOf(0.42f, 0.42f, 0.44f, 1f),
        bottomColor = floatArrayOf(0.38f, 0.38f, 0.40f, 1f),
        sideColor = floatArrayOf(0.40f, 0.40f, 0.42f, 1f)
    ),
    WOOD_LOG(
        id = 5,
        displayName = "Oak Log",
        isSolid = true,
        isTransparent = false,
        hardness = 1.2f,
        topColor = floatArrayOf(0.70f, 0.58f, 0.40f, 1f),       // Tree rings
        bottomColor = floatArrayOf(0.70f, 0.58f, 0.40f, 1f),
        sideColor = floatArrayOf(0.42f, 0.28f, 0.16f, 1f)       // Bark
    ),
    WOOD_PLANKS(
        id = 6,
        displayName = "Oak Planks",
        isSolid = true,
        isTransparent = false,
        hardness = 1.0f,
        topColor = floatArrayOf(0.68f, 0.52f, 0.33f, 1f),
        bottomColor = floatArrayOf(0.64f, 0.48f, 0.30f, 1f),
        sideColor = floatArrayOf(0.66f, 0.50f, 0.32f, 1f)
    ),
    LEAVES(
        id = 7,
        displayName = "Oak Leaves",
        isSolid = true,
        isTransparent = true,
        hardness = 0.2f,
        topColor = floatArrayOf(0.24f, 0.56f, 0.18f, 0.95f),
        bottomColor = floatArrayOf(0.20f, 0.50f, 0.15f, 0.95f),
        sideColor = floatArrayOf(0.22f, 0.53f, 0.17f, 0.95f)
    ),
    SAND(
        id = 8,
        displayName = "Sand",
        isSolid = true,
        isTransparent = false,
        hardness = 0.5f,
        topColor = floatArrayOf(0.86f, 0.82f, 0.60f, 1f),
        bottomColor = floatArrayOf(0.82f, 0.78f, 0.56f, 1f),
        sideColor = floatArrayOf(0.84f, 0.80f, 0.58f, 1f)
    ),
    WATER(
        id = 9,
        displayName = "Water",
        isSolid = false,
        isTransparent = true,
        hardness = 100f,
        topColor = floatArrayOf(0.18f, 0.42f, 0.85f, 0.65f),
        bottomColor = floatArrayOf(0.15f, 0.38f, 0.80f, 0.65f),
        sideColor = floatArrayOf(0.17f, 0.40f, 0.82f, 0.65f)
    ),
    COAL_ORE(
        id = 10,
        displayName = "Coal Ore",
        isSolid = true,
        isTransparent = false,
        hardness = 2.5f,
        topColor = floatArrayOf(0.35f, 0.35f, 0.36f, 1f),
        bottomColor = floatArrayOf(0.32f, 0.32f, 0.34f, 1f),
        sideColor = floatArrayOf(0.30f, 0.30f, 0.32f, 1f)
    ),
    IRON_ORE(
        id = 11,
        displayName = "Iron Ore",
        isSolid = true,
        isTransparent = false,
        hardness = 3.0f,
        topColor = floatArrayOf(0.55f, 0.50f, 0.46f, 1f),
        bottomColor = floatArrayOf(0.52f, 0.47f, 0.43f, 1f),
        sideColor = floatArrayOf(0.58f, 0.48f, 0.42f, 1f)
    ),
    CRAFTING_TABLE(
        id = 12,
        displayName = "Crafting Table",
        isSolid = true,
        isTransparent = false,
        hardness = 1.2f,
        topColor = floatArrayOf(0.72f, 0.55f, 0.35f, 1f),
        bottomColor = floatArrayOf(0.60f, 0.45f, 0.28f, 1f),
        sideColor = floatArrayOf(0.58f, 0.40f, 0.25f, 1f)
    ),
    CHEST(
        id = 13,
        displayName = "Chest",
        isSolid = true,
        isTransparent = false,
        hardness = 1.5f,
        topColor = floatArrayOf(0.65f, 0.48f, 0.25f, 1f),
        bottomColor = floatArrayOf(0.58f, 0.40f, 0.20f, 1f),
        sideColor = floatArrayOf(0.62f, 0.44f, 0.22f, 1f)
    ),
    TORCH(
        id = 14,
        displayName = "Torch",
        isSolid = false,
        isTransparent = true,
        isLightSource = true,
        hardness = 0.1f,
        topColor = floatArrayOf(1.0f, 0.85f, 0.30f, 1f),       // Bright flame
        bottomColor = floatArrayOf(0.50f, 0.35f, 0.20f, 1f),
        sideColor = floatArrayOf(0.95f, 0.70f, 0.25f, 1f)
    ),
    GLASS(
        id = 15,
        displayName = "Glass",
        isSolid = true,
        isTransparent = true,
        hardness = 0.3f,
        topColor = floatArrayOf(0.85f, 0.95f, 1.0f, 0.4f),
        bottomColor = floatArrayOf(0.85f, 0.95f, 1.0f, 0.4f),
        sideColor = floatArrayOf(0.85f, 0.95f, 1.0f, 0.4f)
    ),
    BRICK(
        id = 16,
        displayName = "Bricks",
        isSolid = true,
        isTransparent = false,
        hardness = 2.0f,
        topColor = floatArrayOf(0.70f, 0.35f, 0.25f, 1f),
        bottomColor = floatArrayOf(0.65f, 0.30f, 0.20f, 1f),
        sideColor = floatArrayOf(0.68f, 0.32f, 0.22f, 1f)
    ),
    BEDROCK(
        id = 17,
        displayName = "Bedrock",
        isSolid = true,
        isTransparent = false,
        hardness = 9999f,
        topColor = floatArrayOf(0.18f, 0.18f, 0.18f, 1f),
        bottomColor = floatArrayOf(0.12f, 0.12f, 0.12f, 1f),
        sideColor = floatArrayOf(0.15f, 0.15f, 0.15f, 1f)
    ),
    LUCKY_BLOCK(
        id = 18,
        displayName = "Lucky Block",
        isSolid = true,
        isTransparent = false,
        isLightSource = true,
        hardness = 0.5f,
        topColor = floatArrayOf(0.96f, 0.82f, 0.12f, 1f),
        bottomColor = floatArrayOf(0.85f, 0.70f, 0.08f, 1f),
        sideColor = floatArrayOf(0.92f, 0.78f, 0.10f, 1f)
    ),
    RUBY_ORE(
        id = 19,
        displayName = "Ruby Ore",
        isSolid = true,
        isTransparent = false,
        hardness = 3.5f,
        topColor = floatArrayOf(0.85f, 0.12f, 0.22f, 1f),
        bottomColor = floatArrayOf(0.48f, 0.48f, 0.50f, 1f),
        sideColor = floatArrayOf(0.80f, 0.15f, 0.25f, 1f)
    ),
    SOFA(
        id = 20,
        displayName = "Velvet Sofa",
        isSolid = true,
        isTransparent = false,
        hardness = 0.8f,
        topColor = floatArrayOf(0.68f, 0.18f, 0.18f, 1f),
        bottomColor = floatArrayOf(0.42f, 0.25f, 0.15f, 1f),
        sideColor = floatArrayOf(0.72f, 0.20f, 0.20f, 1f)
    ),
    FRIDGE(
        id = 21,
        displayName = "Kitchen Fridge",
        isSolid = true,
        isTransparent = false,
        hardness = 1.5f,
        topColor = floatArrayOf(0.88f, 0.90f, 0.92f, 1f),
        bottomColor = floatArrayOf(0.70f, 0.72f, 0.75f, 1f),
        sideColor = floatArrayOf(0.82f, 0.85f, 0.88f, 1f)
    );

    companion object {
        private val ID_MAP = values().associateBy { it.id }
        fun fromId(id: Byte): BlockType = ID_MAP[id] ?: AIR
    }
}
