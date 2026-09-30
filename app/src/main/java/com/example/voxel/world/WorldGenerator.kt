package com.example.voxel.world

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Deterministic procedural terrain and structure generator.
 */
class WorldGenerator(val seed: Long = 133742L) {

    val seaLevel = 14
    private val random = Random(seed)

    // Fast 2D value noise for terrain height
    fun getTerrainHeight(worldX: Int, worldZ: Int): Int {
        // Flatten village area around (16..36, 16..36)
        if (worldX in 12..38 && worldZ in 12..38) {
            return 17
        }

        val scale1 = 0.02
        val scale2 = 0.05
        val scale3 = 0.1

        val n1 = pseudoNoise2D(worldX * scale1, worldZ * scale1, seed)
        val n2 = pseudoNoise2D(worldX * scale2, worldZ * scale2, seed + 101)
        val n3 = pseudoNoise2D(worldX * scale3, worldZ * scale3, seed + 202)

        val combined = n1 * 0.65 + n2 * 0.25 + n3 * 0.10 // 0.0 .. 1.0
        val height = 12 + (combined * 22).toInt() // 12 .. 34
        return height.coerceIn(4, Chunk.SIZE_Y - 8)
    }

    private fun pseudoNoise2D(x: Double, z: Double, s: Long): Double {
        val xi = Math.floor(x).toInt()
        val zi = Math.floor(z).toInt()
        val xf = x - xi
        val zf = z - zi

        // Smoothstep
        val u = xf * xf * (3.0 - 2.0 * xf)
        val v = zf * zf * (3.0 - 2.0 * zf)

        val n00 = hash2D(xi, zi, s)
        val n10 = hash2D(xi + 1, zi, s)
        val n01 = hash2D(xi, zi + 1, s)
        val n11 = hash2D(xi + 1, zi + 1, s)

        val x0 = n00 * (1.0 - u) + n10 * u
        val x1 = n01 * (1.0 - u) + n11 * u
        return x0 * (1.0 - v) + x1 * v
    }

    private fun hash2D(x: Int, z: Int, s: Long): Double {
        var h = (x * 374761393L + z * 668265263L + s * 3266489917L).toLong()
        h = (h xor (h shr 13)) * 1274126177L
        return ((h xor (h shr 16)) and 0x7fffffffL).toDouble() / 0x7fffffffL.toDouble()
    }

    private fun isCave(x: Int, y: Int, z: Int): Boolean {
        if (y < 4 || y > 24) return false
        // Avoid cave beneath village
        if (x in 12..38 && z in 12..38) return false

        // Starter cave near (worldX ~ 6, worldZ ~ -10, y ~ 16..20)
        val distToStarterCave = sqrt(((x - 8) * (x - 8) + (z - (-8)) * (z - (-8))).toDouble())
        if (distToStarterCave < 4.0 && y in 14..20) {
            return true
        }

        val n = pseudoNoise2D(x * 0.12, z * 0.12, seed + y * 13)
        return n > 0.82
    }

    fun generateChunkData(chunk: Chunk) {
        val startX = chunk.chunkX * Chunk.SIZE_X
        val startZ = chunk.chunkZ * Chunk.SIZE_Z

        for (lx in 0 until Chunk.SIZE_X) {
            val wx = startX + lx
            for (lz in 0 until Chunk.SIZE_Z) {
                val wz = startZ + lz
                val height = getTerrainHeight(wx, wz)

                // Fill bedrock at y = 0
                chunk.setBlock(lx, 0, lz, BlockType.BEDROCK)

                for (y in 1 until Chunk.SIZE_Y) {
                    if (y < height - 3) {
                        // Underground stone and ores
                        if (isCave(wx, y, wz)) {
                            chunk.setBlock(lx, y, lz, BlockType.AIR)
                        } else {
                            val oreHash = hash2D(wx + y * 7, wz + y * 13, seed + 555)
                            if (oreHash > 0.94) {
                                chunk.setBlock(lx, y, lz, BlockType.IRON_ORE)
                            } else if (oreHash > 0.88) {
                                chunk.setBlock(lx, y, lz, BlockType.COAL_ORE)
                            } else {
                                chunk.setBlock(lx, y, lz, BlockType.STONE)
                            }
                        }
                    } else if (y < height) {
                        // Subsurface dirt
                        if (isCave(wx, y, wz)) {
                            chunk.setBlock(lx, y, lz, BlockType.AIR)
                        } else {
                            chunk.setBlock(lx, y, lz, BlockType.DIRT)
                        }
                    } else if (y == height) {
                        // Surface block
                        if (isCave(wx, y, wz) && height < 20) {
                            chunk.setBlock(lx, y, lz, BlockType.AIR)
                        } else if (height <= seaLevel + 1) {
                            chunk.setBlock(lx, y, lz, BlockType.SAND)
                        } else {
                            chunk.setBlock(lx, y, lz, BlockType.GRASS)
                        }
                    } else if (y <= seaLevel) {
                        // Water
                        chunk.setBlock(lx, y, lz, BlockType.WATER)
                    } else {
                        chunk.setBlock(lx, y, lz, BlockType.AIR)
                    }
                }

                // Place trees organically
                if (height > seaLevel + 1 && height < Chunk.SIZE_Y - 10) {
                    val treeChance = hash2D(wx, wz, seed + 999)
                    // No trees directly inside village center
                    val inVillage = wx in 14..36 && wz in 14..36
                    if (!inVillage && treeChance > 0.965 && lx in 2..13 && lz in 2..13) {
                        placeTree(chunk, lx, height + 1, lz)
                    }
                }
            }
        }

        // If this chunk contains the Village (around chunk [1, 1] i.e. 16..31, 16..31)
        if (chunk.chunkX == 1 && chunk.chunkZ == 1) {
            buildVillage(chunk)
        }

        chunk.isDirty = true
        chunk.isGenerated = true
    }

    private fun placeTree(chunk: Chunk, lx: Int, startY: Int, lz: Int) {
        val trunkHeight = 4
        // Trunk
        for (y in 0 until trunkHeight) {
            val py = startY + y
            if (py < Chunk.SIZE_Y) {
                chunk.setBlock(lx, py, lz, BlockType.WOOD_LOG)
            }
        }
        // Leaves canopy
        val leafBase = startY + trunkHeight - 2
        for (dy in 0..2) {
            val radius = if (dy == 2) 1 else 2
            val py = leafBase + dy
            if (py >= Chunk.SIZE_Y) continue
            for (dx in -radius..radius) {
                for (dz in -radius..radius) {
                    if (dx == 0 && dz == 0 && dy < 2) continue // trunk
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius && dy == 2) continue // corners
                    val tx = lx + dx
                    val tz = lz + dz
                    if (tx in 0 until Chunk.SIZE_X && tz in 0 until Chunk.SIZE_Z) {
                        if (chunk.getBlock(tx, py, tz) == BlockType.AIR) {
                            chunk.setBlock(tx, py, tz, BlockType.LEAVES)
                        }
                    }
                }
            }
        }
    }

    private fun buildVillage(chunk: Chunk) {
        // Base ground level inside chunk [1, 1] is y = 17
        val gy = 17

        // 1. Central Cobblestone Well at local (lx = 7..9, lz = 7..9)
        for (x in 7..9) {
            for (z in 7..9) {
                chunk.setBlock(x, gy, z, BlockType.COBBLESTONE)
                if (x == 8 && z == 8) {
                    chunk.setBlock(x, gy + 1, z, BlockType.WATER)
                    chunk.setBlock(x, gy, z, BlockType.WATER)
                } else {
                    chunk.setBlock(x, gy + 1, z, BlockType.COBBLESTONE)
                }
            }
        }
        // Well pillars and roof
        chunk.setBlock(7, gy + 2, 7, BlockType.WOOD_PLANKS)
        chunk.setBlock(9, gy + 2, 7, BlockType.WOOD_PLANKS)
        chunk.setBlock(7, gy + 2, 9, BlockType.WOOD_PLANKS)
        chunk.setBlock(9, gy + 2, 9, BlockType.WOOD_PLANKS)
        for (x in 7..9) {
            for (z in 7..9) {
                chunk.setBlock(x, gy + 3, z, BlockType.WOOD_PLANKS)
            }
        }
        chunk.setBlock(8, gy + 2, 8, BlockType.TORCH)

        // 2. Pathways connecting houses
        for (z in 0..15) {
            chunk.setBlock(8, gy, z, BlockType.COBBLESTONE)
        }
        for (x in 0..15) {
            chunk.setBlock(x, gy, 8, BlockType.COBBLESTONE)
        }

        // 3. House 1: North-West House (lx: 1..5, lz: 1..5)
        buildSmallHouse(chunk, 1, gy, 1, 5, 5, withCraftingTable = true)

        // 4. House 2: North-East House (lx: 10..14, lz: 1..5)
        buildSmallHouse(chunk, 10, gy, 1, 5, 5, withChest = true)

        // 5. House 3: South-West Blacksmith / Hall (lx: 1..6, lz: 10..15)
        buildBlacksmith(chunk, 1, gy, 10)

        // 6. House 4: South-East Villager Cottage (lx: 10..14, lz: 10..14)
        buildSmallHouse(chunk, 10, gy, 10, 5, 5, withCraftingTable = false)
    }

    private fun buildSmallHouse(chunk: Chunk, startX: Int, baseY: Int, startZ: Int, width: Int, length: Int, withCraftingTable: Boolean = false, withChest: Boolean = false) {
        val wallHeight = 3
        for (x in 0 until width) {
            for (z in 0 until length) {
                val gx = startX + x
                val gz = startZ + z
                if (gx >= Chunk.SIZE_X || gz >= Chunk.SIZE_Z) continue

                // Foundation
                chunk.setBlock(gx, baseY, gz, BlockType.COBBLESTONE)

                // Walls & interior
                val isEdge = (x == 0 || x == width - 1 || z == 0 || z == length - 1)
                for (y in 1..wallHeight) {
                    val gy = baseY + y
                    if (isEdge) {
                        // Corner pillars are wood logs, walls are wood planks
                        if ((x == 0 || x == width - 1) && (z == 0 || z == length - 1)) {
                            chunk.setBlock(gx, gy, gz, BlockType.WOOD_LOG)
                        } else if (y == 2 && ((x == width / 2 && (z == 0 || z == length - 1)) || (z == length / 2 && (x == 0 || x == width - 1)))) {
                            // Window
                            chunk.setBlock(gx, gy, gz, BlockType.GLASS)
                        } else {
                            chunk.setBlock(gx, gy, gz, BlockType.WOOD_PLANKS)
                        }
                    } else {
                        chunk.setBlock(gx, gy, gz, BlockType.AIR)
                    }
                }

                // Roof
                chunk.setBlock(gx, baseY + wallHeight + 1, gz, BlockType.WOOD_PLANKS)
            }
        }

        // Door entrance (clear 2 blocks on one side)
        val doorX = startX + width / 2
        val doorZ = startZ + length - 1
        if (doorX < Chunk.SIZE_X && doorZ < Chunk.SIZE_Z) {
            chunk.setBlock(doorX, baseY + 1, doorZ, BlockType.AIR)
            chunk.setBlock(doorX, baseY + 2, doorZ, BlockType.AIR)
        }

        // Interior torch
        val insideX = startX + 1
        val insideZ = startZ + 1
        if (insideX < Chunk.SIZE_X && insideZ < Chunk.SIZE_Z) {
            chunk.setBlock(insideX, baseY + 2, insideZ, BlockType.TORCH)
            if (withCraftingTable) {
                chunk.setBlock(insideX, baseY + 1, insideZ, BlockType.CRAFTING_TABLE)
            } else if (withChest) {
                chunk.setBlock(insideX, baseY + 1, insideZ, BlockType.CHEST)
            }
        }
    }

    private fun buildBlacksmith(chunk: Chunk, startX: Int, baseY: Int, startZ: Int) {
        val width = 5
        val length = 5
        for (x in 0 until width) {
            for (z in 0 until length) {
                val gx = startX + x
                val gz = startZ + z
                if (gx >= Chunk.SIZE_X || gz >= Chunk.SIZE_Z) continue

                // Cobblestone foundation
                chunk.setBlock(gx, baseY, gz, BlockType.COBBLESTONE)

                // Open-air smithy with stone pillars and furnace
                val isCorner = (x == 0 || x == width - 1) && (z == 0 || z == length - 1)
                for (y in 1..3) {
                    if (isCorner) {
                        chunk.setBlock(gx, baseY + y, gz, BlockType.COBBLESTONE)
                    } else {
                        chunk.setBlock(gx, baseY + y, gz, BlockType.AIR)
                    }
                }
                // Stone roof
                chunk.setBlock(gx, baseY + 4, gz, BlockType.STONE)
            }
        }
        // Blacksmith items: Chest and Crafting table
        chunk.setBlock(startX + 1, baseY + 1, startZ + 1, BlockType.CHEST)
        chunk.setBlock(startX + 1, baseY + 1, startZ + 2, BlockType.CRAFTING_TABLE)
        chunk.setBlock(startX + 2, baseY + 2, startZ + 1, BlockType.TORCH)
    }
}
