package com.example.voxel.world

import com.example.voxel.game.Inventory
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor
import kotlin.math.sqrt

data class RaycastResult(
    val hitBlockX: Int,
    val hitBlockY: Int,
    val hitBlockZ: Int,
    val normalX: Int,
    val normalY: Int,
    val normalZ: Int,
    val blockType: BlockType,
    val distance: Float
)

/**
 * Manages active chunks, chunk streaming, voxel modifications, and raycasting.
 */
class World(val seed: Long = 133742L) {

    val generator = WorldGenerator(seed)
    val chunks = ConcurrentHashMap<Long, Chunk>()
    val modifiedBlocks = ConcurrentHashMap<Long, BlockType>()
    val chestInventories = ConcurrentHashMap<Long, Inventory>()

    // Game time: 0..24000 (0=dawn, 6000=noon, 12000=sunset, 18000=midnight)
    var gameTime: Float = 6000f

    companion object {
        fun chunkKey(cx: Int, cz: Int): Long {
            return (cx.toLong() shl 32) or (cz.toLong() and 0xFFFFFFFFL)
        }

        fun blockKey(x: Int, y: Int, z: Int): Long {
            // 24 bits for x, 16 bits for y, 24 bits for z
            return ((x.toLong() and 0xFFFFFFL) shl 40) or
                    ((y.toLong() and 0xFFFFL) shl 24) or
                    (z.toLong() and 0xFFFFFFL)
        }
    }

    fun getChunk(chunkX: Int, chunkZ: Int, createIfMissing: Boolean = true): Chunk? {
        val key = chunkKey(chunkX, chunkZ)
        var chunk = chunks[key]
        if (chunk == null && createIfMissing) {
            chunk = Chunk(chunkX, chunkZ)
            generator.generateChunkData(chunk)

            // Re-apply any persistent modified blocks in this chunk
            val startX = chunkX * Chunk.SIZE_X
            val startZ = chunkZ * Chunk.SIZE_Z
            for (lx in 0 until Chunk.SIZE_X) {
                for (lz in 0 until Chunk.SIZE_Z) {
                    val wx = startX + lx
                    val wz = startZ + lz
                    for (y in 0 until Chunk.SIZE_Y) {
                        val bKey = blockKey(wx, y, wz)
                        val mod = modifiedBlocks[bKey]
                        if (mod != null) {
                            chunk.setBlock(lx, y, lz, mod)
                        }
                    }
                }
            }

            chunks[key] = chunk
        }
        return chunk
    }

    fun getBlock(x: Int, y: Int, z: Int): BlockType {
        if (y < 0 || y >= Chunk.SIZE_Y) return BlockType.AIR

        // Check if modified first for instant accuracy
        val bKey = blockKey(x, y, z)
        val mod = modifiedBlocks[bKey]
        if (mod != null) return mod

        val cx = floor(x.toDouble() / Chunk.SIZE_X).toInt()
        val cz = floor(z.toDouble() / Chunk.SIZE_Z).toInt()
        val chunk = getChunk(cx, cz, createIfMissing = true) ?: return BlockType.AIR

        val lx = ((x % Chunk.SIZE_X) + Chunk.SIZE_X) % Chunk.SIZE_X
        val lz = ((z % Chunk.SIZE_Z) + Chunk.SIZE_Z) % Chunk.SIZE_Z
        return chunk.getBlock(lx, y, lz)
    }

    fun setBlock(x: Int, y: Int, z: Int, type: BlockType): Boolean {
        if (y < 0 || y >= Chunk.SIZE_Y) return false

        val bKey = blockKey(x, y, z)
        modifiedBlocks[bKey] = type

        val cx = floor(x.toDouble() / Chunk.SIZE_X).toInt()
        val cz = floor(z.toDouble() / Chunk.SIZE_Z).toInt()
        val chunk = getChunk(cx, cz, createIfMissing = true) ?: return false

        val lx = ((x % Chunk.SIZE_X) + Chunk.SIZE_X) % Chunk.SIZE_X
        val lz = ((z % Chunk.SIZE_Z) + Chunk.SIZE_Z) % Chunk.SIZE_Z
        chunk.setBlock(lx, y, lz, type)

        // Mark neighboring chunk dirty if on chunk border
        if (lx == 0) getChunk(cx - 1, cz, false)?.isDirty = true
        if (lx == Chunk.SIZE_X - 1) getChunk(cx + 1, cz, false)?.isDirty = true
        if (lz == 0) getChunk(cx, cz - 1, false)?.isDirty = true
        if (lz == Chunk.SIZE_Z - 1) getChunk(cx, cz + 1, false)?.isDirty = true

        return true
    }

    fun getChestInventory(x: Int, y: Int, z: Int): Inventory {
        val key = blockKey(x, y, z)
        return chestInventories.getOrPut(key) {
            Inventory(27)
        }
    }

    /**
     * Updates chunk streaming around player.
     * Keeps chunks within renderDistance loaded and generated.
     */
    fun updateStreaming(playerX: Float, playerZ: Float, renderDistance: Int = 3) {
        val centerChunkX = floor(playerX.toDouble() / Chunk.SIZE_X).toInt()
        val centerChunkZ = floor(playerZ.toDouble() / Chunk.SIZE_Z).toInt()

        for (dx in -renderDistance..renderDistance) {
            for (dz in -renderDistance..renderDistance) {
                if (dx * dx + dz * dz <= renderDistance * renderDistance + 1) {
                    getChunk(centerChunkX + dx, centerChunkZ + dz, createIfMissing = true)
                }
            }
        }

        // Unload distant chunks to preserve mobile memory
        val unloadThreshold = renderDistance + 2
        val iterator = chunks.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val chunk = entry.value
            val distSq = (chunk.chunkX - centerChunkX) * (chunk.chunkX - centerChunkX) +
                    (chunk.chunkZ - centerChunkZ) * (chunk.chunkZ - centerChunkZ)
            if (distSq > unloadThreshold * unloadThreshold) {
                iterator.remove()
            }
        }
    }

    /**
     * Fast 3D DDA voxel raycasting.
     */
    fun raycast(
        originX: Float, originY: Float, originZ: Float,
        dirX: Float, dirY: Float, dirZ: Float,
        maxDistance: Float = 5.0f
    ): RaycastResult? {
        val len = sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ)
        if (len < 0.0001f) return null
        val dx = dirX / len
        val dy = dirY / len
        val dz = dirZ / len

        var currentX = floor(originX.toDouble()).toInt()
        var currentY = floor(originY.toDouble()).toInt()
        var currentZ = floor(originZ.toDouble()).toInt()

        val stepX = if (dx > 0) 1 else if (dx < 0) -1 else 0
        val stepY = if (dy > 0) 1 else if (dy < 0) -1 else 0
        val stepZ = if (dz > 0) 1 else if (dz < 0) -1 else 0

        val tDeltaX = if (stepX != 0) Math.abs(1.0f / dx) else Float.MAX_VALUE
        val tDeltaY = if (stepY != 0) Math.abs(1.0f / dy) else Float.MAX_VALUE
        val tDeltaZ = if (stepZ != 0) Math.abs(1.0f / dz) else Float.MAX_VALUE

        var tMaxX = if (stepX > 0) (currentX + 1.0f - originX) * tDeltaX else (originX - currentX) * tDeltaX
        var tMaxY = if (stepY > 0) (currentY + 1.0f - originY) * tDeltaY else (originY - currentY) * tDeltaY
        var tMaxZ = if (stepZ > 0) (currentZ + 1.0f - originZ) * tDeltaZ else (originZ - currentZ) * tDeltaZ

        var normalX = 0
        var normalY = 0
        var normalZ = 0
        var dist = 0.0f

        while (dist <= maxDistance) {
            val block = getBlock(currentX, currentY, currentZ)
            if (block.isSolid || block == BlockType.WATER) {
                return RaycastResult(
                    hitBlockX = currentX,
                    hitBlockY = currentY,
                    hitBlockZ = currentZ,
                    normalX = normalX,
                    normalY = normalY,
                    normalZ = normalZ,
                    blockType = block,
                    distance = dist
                )
            }

            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    dist = tMaxX
                    tMaxX += tDeltaX
                    currentX += stepX
                    normalX = -stepX
                    normalY = 0
                    normalZ = 0
                } else {
                    dist = tMaxZ
                    tMaxZ += tDeltaZ
                    currentZ += stepZ
                    normalX = 0
                    normalY = 0
                    normalZ = -stepZ
                }
            } else {
                if (tMaxY < tMaxZ) {
                    dist = tMaxY
                    tMaxY += tDeltaY
                    currentY += stepY
                    normalX = 0
                    normalY = -stepY
                    normalZ = 0
                } else {
                    dist = tMaxZ
                    tMaxZ += tDeltaZ
                    currentZ += stepZ
                    normalX = 0
                    normalY = 0
                    normalZ = -stepZ
                }
            }
        }
        return null
    }
}
