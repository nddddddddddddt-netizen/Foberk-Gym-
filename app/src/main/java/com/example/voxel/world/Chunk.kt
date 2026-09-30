package com.example.voxel.world

/**
 * A 16x48x16 vertical chunk of voxels.
 */
class Chunk(val chunkX: Int, val chunkZ: Int) {

    companion object {
        const val SIZE_X = 16
        const val SIZE_Y = 48
        const val SIZE_Z = 16
        const val TOTAL_BLOCKS = SIZE_X * SIZE_Y * SIZE_Z

        fun getIndex(x: Int, y: Int, z: Int): Int {
            return (y * SIZE_Z + z) * SIZE_X + x
        }
    }

    private val blocks = ByteArray(TOTAL_BLOCKS)
    var isDirty: Boolean = true
    var isGenerated: Boolean = false

    fun getBlock(x: Int, y: Int, z: Int): BlockType {
        if (x !in 0 until SIZE_X || y !in 0 until SIZE_Y || z !in 0 until SIZE_Z) {
            return BlockType.AIR
        }
        return BlockType.fromId(blocks[getIndex(x, y, z)])
    }

    fun setBlock(x: Int, y: Int, z: Int, type: BlockType) {
        if (x !in 0 until SIZE_X || y !in 0 until SIZE_Y || z !in 0 until SIZE_Z) return
        val idx = getIndex(x, y, z)
        if (blocks[idx] != type.id) {
            blocks[idx] = type.id
            isDirty = true
        }
    }

    fun getBlockRaw(index: Int): Byte = blocks[index]
    fun setBlockRaw(index: Int, id: Byte) {
        blocks[index] = id
    }
}
