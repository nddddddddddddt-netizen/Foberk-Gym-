package com.example.voxel.engine.gl

import android.opengl.GLES20
import com.example.voxel.mod.TextureConverter
import com.example.voxel.world.BlockType
import com.example.voxel.world.Chunk
import com.example.voxel.world.World
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Builds and renders the OpenGL ES mesh for a single chunk with hidden face culling and ambient occlusion.
 */
class ChunkMesh(val chunkX: Int, val chunkZ: Int) {

    companion object {
        const val FLOATS_PER_VERTEX = 11 // pos(3) + normal(3) + color(4) + ao(1)
        const val STRIDE = FLOATS_PER_VERTEX * 4
    }

    private var vertexBuffer: FloatBuffer? = null
    var vertexCount: Int = 0
    var isBuilt: Boolean = false

    fun build(world: World, chunk: Chunk) {
        val startX = chunkX * Chunk.SIZE_X
        val startZ = chunkZ * Chunk.SIZE_Z

        // Temporary dynamic float list
        var capacity = 1024 * FLOATS_PER_VERTEX
        var data = FloatArray(capacity)
        var offset = 0

        fun ensureCapacity(neededFloats: Int) {
            if (offset + neededFloats >= capacity) {
                capacity = maxOf(capacity * 2, offset + neededFloats + 2048)
                val newArr = FloatArray(capacity)
                System.arraycopy(data, 0, newArr, 0, offset)
                data = newArr
            }
        }

        fun putVertex(x: Float, y: Float, z: Float, nx: Float, ny: Float, nz: Float, r: Float, g: Float, b: Float, a: Float, ao: Float) {
            data[offset++] = x
            data[offset++] = y
            data[offset++] = z
            data[offset++] = nx
            data[offset++] = ny
            data[offset++] = nz
            data[offset++] = r
            data[offset++] = g
            data[offset++] = b
            data[offset++] = a
            data[offset++] = ao
        }

        fun putQuad(
            v1x: Float, v1y: Float, v1z: Float,
            v2x: Float, v2y: Float, v2z: Float,
            v3x: Float, v3y: Float, v3z: Float,
            v4x: Float, v4y: Float, v4z: Float,
            nx: Float, ny: Float, nz: Float,
            color: FloatArray,
            ao1: Float, ao2: Float, ao3: Float, ao4: Float
        ) {
            ensureCapacity(6 * FLOATS_PER_VERTEX)
            val r = color[0]
            val g = color[1]
            val b = color[2]
            val a = color[3]

            // Triangle 1: 1 -> 2 -> 3
            putVertex(v1x, v1y, v1z, nx, ny, nz, r, g, b, a, ao1)
            putVertex(v2x, v2y, v2z, nx, ny, nz, r, g, b, a, ao2)
            putVertex(v3x, v3y, v3z, nx, ny, nz, r, g, b, a, ao3)

            // Triangle 2: 1 -> 3 -> 4
            putVertex(v1x, v1y, v1z, nx, ny, nz, r, g, b, a, ao1)
            putVertex(v3x, v3y, v3z, nx, ny, nz, r, g, b, a, ao3)
            putVertex(v4x, v4y, v4z, nx, ny, nz, r, g, b, a, ao4)
        }

        fun calculateAO(side1: Boolean, side2: Boolean, corner: Boolean): Float {
            if (side1 && side2) return 0.5f
            var count = 0
            if (side1) count++
            if (side2) count++
            if (corner) count++
            return when (count) {
                0 -> 1.0f
                1 -> 0.85f
                2 -> 0.70f
                else -> 0.55f
            }
        }

        for (lx in 0 until Chunk.SIZE_X) {
            val wx = startX + lx
            for (lz in 0 until Chunk.SIZE_Z) {
                val wz = startZ + lz
                for (y in 0 until Chunk.SIZE_Y) {
                    val block = chunk.getBlock(lx, y, lz)
                    if (block == BlockType.AIR) continue
                    val colors = TextureConverter.getBlockColors(block)

                    val fx = wx.toFloat()
                    val fy = y.toFloat()
                    val fz = wz.toFloat()

                    // Top Face (+Y)
                    val topBlock = if (y + 1 < Chunk.SIZE_Y) chunk.getBlock(lx, y + 1, lz) else world.getBlock(wx, y + 1, wz)
                    if (topBlock.isTransparent && (block != topBlock || block != BlockType.WATER)) {
                        val s1 = world.getBlock(wx - 1, y + 1, wz).isSolid
                        val s2 = world.getBlock(wx + 1, y + 1, wz).isSolid
                        val s3 = world.getBlock(wx, y + 1, wz - 1).isSolid
                        val s4 = world.getBlock(wx, y + 1, wz + 1).isSolid
                        val c1 = world.getBlock(wx - 1, y + 1, wz - 1).isSolid
                        val c2 = world.getBlock(wx + 1, y + 1, wz - 1).isSolid
                        val c3 = world.getBlock(wx + 1, y + 1, wz + 1).isSolid
                        val c4 = world.getBlock(wx - 1, y + 1, wz + 1).isSolid

                        val ao1 = calculateAO(s1, s3, c1)
                        val ao2 = calculateAO(s2, s3, c2)
                        val ao3 = calculateAO(s2, s4, c3)
                        val ao4 = calculateAO(s1, s4, c4)

                        putQuad(
                            fx, fy + 1f, fz,
                            fx + 1f, fy + 1f, fz,
                            fx + 1f, fy + 1f, fz + 1f,
                            fx, fy + 1f, fz + 1f,
                            0f, 1f, 0f,
                            colors[0],
                            ao1, ao2, ao3, ao4
                        )
                    }

                    // Bottom Face (-Y)
                    val bottomBlock = if (y - 1 >= 0) chunk.getBlock(lx, y - 1, lz) else BlockType.BEDROCK
                    if (bottomBlock.isTransparent && (block != bottomBlock || block != BlockType.WATER)) {
                        putQuad(
                            fx, fy, fz + 1f,
                            fx + 1f, fy, fz + 1f,
                            fx + 1f, fy, fz,
                            fx, fy, fz,
                            0f, -1f, 0f,
                            colors[1],
                            0.7f, 0.7f, 0.7f, 0.7f
                        )
                    }

                    // North Face (+Z)
                    val northBlock = if (lz + 1 < Chunk.SIZE_Z) chunk.getBlock(lx, y, lz + 1) else world.getBlock(wx, y, wz + 1)
                    if (northBlock.isTransparent && (block != northBlock || block != BlockType.WATER)) {
                        val s1 = world.getBlock(wx - 1, y, wz + 1).isSolid
                        val s2 = world.getBlock(wx + 1, y, wz + 1).isSolid
                        val sTop = world.getBlock(wx, y + 1, wz + 1).isSolid
                        val sBot = world.getBlock(wx, y - 1, wz + 1).isSolid
                        val aoTopLeft = calculateAO(s1, sTop, world.getBlock(wx - 1, y + 1, wz + 1).isSolid)
                        val aoTopRight = calculateAO(s2, sTop, world.getBlock(wx + 1, y + 1, wz + 1).isSolid)
                        val aoBotRight = calculateAO(s2, sBot, world.getBlock(wx + 1, y - 1, wz + 1).isSolid)
                        val aoBotLeft = calculateAO(s1, sBot, world.getBlock(wx - 1, y - 1, wz + 1).isSolid)

                        putQuad(
                            fx, fy + 1f, fz + 1f,
                            fx + 1f, fy + 1f, fz + 1f,
                            fx + 1f, fy, fz + 1f,
                            fx, fy, fz + 1f,
                            0f, 0f, 1f,
                            colors[2],
                            aoTopLeft, aoTopRight, aoBotRight, aoBotLeft
                        )
                    }

                    // South Face (-Z)
                    val southBlock = if (lz - 1 >= 0) chunk.getBlock(lx, y, lz - 1) else world.getBlock(wx, y, wz - 1)
                    if (southBlock.isTransparent && (block != southBlock || block != BlockType.WATER)) {
                        val s1 = world.getBlock(wx + 1, y, wz - 1).isSolid
                        val s2 = world.getBlock(wx - 1, y, wz - 1).isSolid
                        val sTop = world.getBlock(wx, y + 1, wz - 1).isSolid
                        val sBot = world.getBlock(wx, y - 1, wz - 1).isSolid
                        val aoTopLeft = calculateAO(s1, sTop, world.getBlock(wx + 1, y + 1, wz - 1).isSolid)
                        val aoTopRight = calculateAO(s2, sTop, world.getBlock(wx - 1, y + 1, wz - 1).isSolid)
                        val aoBotRight = calculateAO(s2, sBot, world.getBlock(wx - 1, y - 1, wz - 1).isSolid)
                        val aoBotLeft = calculateAO(s1, sBot, world.getBlock(wx + 1, y - 1, wz - 1).isSolid)

                        putQuad(
                            fx + 1f, fy + 1f, fz,
                            fx, fy + 1f, fz,
                            fx, fy, fz,
                            fx + 1f, fy, fz,
                            0f, 0f, -1f,
                            colors[2],
                            aoTopLeft, aoTopRight, aoBotRight, aoBotLeft
                        )
                    }

                    // East Face (+X)
                    val eastBlock = if (lx + 1 < Chunk.SIZE_X) chunk.getBlock(lx + 1, y, lz) else world.getBlock(wx + 1, y, wz)
                    if (eastBlock.isTransparent && (block != eastBlock || block != BlockType.WATER)) {
                        val s1 = world.getBlock(wx + 1, y, wz + 1).isSolid
                        val s2 = world.getBlock(wx + 1, y, wz - 1).isSolid
                        val sTop = world.getBlock(wx + 1, y + 1, wz).isSolid
                        val sBot = world.getBlock(wx + 1, y - 1, wz).isSolid
                        val aoTopLeft = calculateAO(s1, sTop, world.getBlock(wx + 1, y + 1, wz + 1).isSolid)
                        val aoTopRight = calculateAO(s2, sTop, world.getBlock(wx + 1, y + 1, wz - 1).isSolid)
                        val aoBotRight = calculateAO(s2, sBot, world.getBlock(wx + 1, y - 1, wz - 1).isSolid)
                        val aoBotLeft = calculateAO(s1, sBot, world.getBlock(wx + 1, y - 1, wz + 1).isSolid)

                        putQuad(
                            fx + 1f, fy + 1f, fz + 1f,
                            fx + 1f, fy + 1f, fz,
                            fx + 1f, fy, fz,
                            fx + 1f, fy, fz + 1f,
                            1f, 0f, 0f,
                            colors[2],
                            aoTopLeft, aoTopRight, aoBotRight, aoBotLeft
                        )
                    }

                    // West Face (-X)
                    val westBlock = if (lx - 1 >= 0) chunk.getBlock(lx - 1, y, lz) else world.getBlock(wx - 1, y, wz)
                    if (westBlock.isTransparent && (block != westBlock || block != BlockType.WATER)) {
                        val s1 = world.getBlock(wx - 1, y, wz - 1).isSolid
                        val s2 = world.getBlock(wx - 1, y, wz + 1).isSolid
                        val sTop = world.getBlock(wx - 1, y + 1, wz).isSolid
                        val sBot = world.getBlock(wx - 1, y - 1, wz).isSolid
                        val aoTopLeft = calculateAO(s1, sTop, world.getBlock(wx - 1, y + 1, wz - 1).isSolid)
                        val aoTopRight = calculateAO(s2, sTop, world.getBlock(wx - 1, y + 1, wz + 1).isSolid)
                        val aoBotRight = calculateAO(s2, sBot, world.getBlock(wx - 1, y - 1, wz + 1).isSolid)
                        val aoBotLeft = calculateAO(s1, sBot, world.getBlock(wx - 1, y - 1, wz - 1).isSolid)

                        putQuad(
                            fx, fy + 1f, fz,
                            fx, fy + 1f, fz + 1f,
                            fx, fy, fz + 1f,
                            fx, fy, fz,
                            -1f, 0f, 0f,
                            colors[2],
                            aoTopLeft, aoTopRight, aoBotRight, aoBotLeft
                        )
                    }
                }
            }
        }

        vertexCount = offset / FLOATS_PER_VERTEX
        if (vertexCount > 0) {
            val bb = ByteBuffer.allocateDirect(offset * 4)
            bb.order(ByteOrder.nativeOrder())
            val fb = bb.asFloatBuffer()
            fb.put(data, 0, offset)
            fb.position(0)
            vertexBuffer = fb
        } else {
            vertexBuffer = null
        }

        chunk.isDirty = false
        isBuilt = true
    }

    fun render(shader: VoxelShader) {
        val buffer = vertexBuffer ?: return
        if (vertexCount <= 0) return

        buffer.position(0)
        GLES20.glVertexAttribPointer(shader.positionHandle, 3, GLES20.GL_FLOAT, false, STRIDE, buffer)
        GLES20.glEnableVertexAttribArray(shader.positionHandle)

        buffer.position(3)
        GLES20.glVertexAttribPointer(shader.normalHandle, 3, GLES20.GL_FLOAT, false, STRIDE, buffer)
        GLES20.glEnableVertexAttribArray(shader.normalHandle)

        buffer.position(6)
        GLES20.glVertexAttribPointer(shader.colorHandle, 4, GLES20.GL_FLOAT, false, STRIDE, buffer)
        GLES20.glEnableVertexAttribArray(shader.colorHandle)

        buffer.position(10)
        GLES20.glVertexAttribPointer(shader.aoHandle, 1, GLES20.GL_FLOAT, false, STRIDE, buffer)
        GLES20.glEnableVertexAttribArray(shader.aoHandle)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
    }
}
