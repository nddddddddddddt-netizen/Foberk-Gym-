package com.example.voxel.mod

import com.example.voxel.world.BlockType
import com.example.voxel.world.Chunk
import com.example.voxel.world.World

data class AdventureMap(
    val id: String,
    val name: String,
    val platform: ModPlatform,
    val description: String,
    val seed: Long,
    val spawnX: Float,
    val spawnY: Float,
    val spawnZ: Float,
    val iconEmoji: String
)

object WorldMapImporter {

    val BUNDLED_MAPS = listOf(
        AdventureMap(
            id = "map_medieval_castle",
            name = "Medieval Fortress & Village",
            platform = ModPlatform.BEDROCK,
            description = "Sprawling fortified village with cobblestone battlements, grand hall, and watchtowers.",
            seed = 987654L,
            spawnX = 18f,
            spawnY = 20f,
            spawnZ = 18f,
            iconEmoji = "🏰"
        ),
        AdventureMap(
            id = "map_skyblock",
            name = "SkyBlock Challenge",
            platform = ModPlatform.JAVA,
            description = "Floating 4x4 dirt island surrounded by the endless sky. Survive with a chest and single tree!",
            seed = 555111L,
            spawnX = 8f,
            spawnY = 32f,
            spawnZ = 8f,
            iconEmoji = "☁️"
        ),
        AdventureMap(
            id = "map_survival_island",
            name = "Survival Island 1.1",
            platform = ModPlatform.UNIVERSAL,
            description = "Isolated sandy island surrounded by deep blue ocean with hidden sunken shipwrecks.",
            seed = 202611L,
            spawnX = 12f,
            spawnY = 18f,
            spawnZ = 12f,
            iconEmoji = "🏝️"
        ),
        AdventureMap(
            id = "map_deep_mines",
            name = "Mega Mines & Dungeons",
            platform = ModPlatform.JAVA,
            description = "Expansive underground cavern system packed with exposed minerals, ores, and torches.",
            seed = 777333L,
            spawnX = 8f,
            spawnY = 16f,
            spawnZ = -8f,
            iconEmoji = "⛏️"
        )
    )

    fun applyMapStructures(map: AdventureMap, world: World) {
        if (map.id == "map_skyblock") {
            // Build floating island
            for (x in 6..10) {
                for (z in 6..10) {
                    world.setBlock(x, 30, z, BlockType.DIRT)
                    world.setBlock(x, 31, z, BlockType.GRASS)
                }
            }
            world.setBlock(8, 30, 8, BlockType.BEDROCK)
            world.setBlock(9, 32, 8, BlockType.CHEST)
            // Island tree
            for (y in 32..35) world.setBlock(7, y, 7, BlockType.WOOD_LOG)
            for (dx in -1..1) {
                for (dz in -1..1) {
                    world.setBlock(7 + dx, 36, 7 + dz, BlockType.LEAVES)
                }
            }
        } else if (map.id == "map_medieval_castle") {
            // Build castle battlements around village center
            val by = 18
            for (i in 10..30) {
                world.setBlock(10, by, i, BlockType.COBBLESTONE)
                world.setBlock(30, by, i, BlockType.COBBLESTONE)
                world.setBlock(i, by, 10, BlockType.COBBLESTONE)
                world.setBlock(i, by, 30, BlockType.COBBLESTONE)

                // Battlements tooth pattern
                if (i % 2 == 0) {
                    world.setBlock(10, by + 1, i, BlockType.COBBLESTONE)
                    world.setBlock(30, by + 1, i, BlockType.COBBLESTONE)
                    world.setBlock(i, by + 1, 10, BlockType.COBBLESTONE)
                    world.setBlock(i, by + 1, 30, BlockType.COBBLESTONE)
                }
            }
            // Corner towers
            for (corner in listOf(Pair(10, 10), Pair(10, 30), Pair(30, 10), Pair(30, 30))) {
                for (ty in by..by + 5) {
                    world.setBlock(corner.first, ty, corner.second, BlockType.BRICK)
                }
                world.setBlock(corner.first, by + 6, corner.second, BlockType.TORCH)
            }
        }
    }
}
