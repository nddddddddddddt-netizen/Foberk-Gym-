package com.example.voxel.engine.gl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.voxel.entity.Mob
import com.example.voxel.entity.MobType
import com.example.voxel.entity.Player
import com.example.voxel.game.Item
import com.example.voxel.world.BlockType
import com.example.voxel.world.Chunk
import com.example.voxel.world.RaycastResult
import com.example.voxel.world.World
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.ConcurrentHashMap
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class VoxelRenderer(
    val world: World,
    val player: Player
) : GLSurfaceView.Renderer {

    var renderDistance: Int = 3
    var targetedBlock: RaycastResult? = null
    var mobsList: List<Mob> = emptyList()

    private val shader = VoxelShader()
    private val chunkMeshes = ConcurrentHashMap<Long, ChunkMesh>()

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    // Sun & Moon geometry buffers
    private var celestialBuffer: FloatBuffer? = null
    private var boxBuffer: FloatBuffer? = null
    private var wireBoxBuffer: FloatBuffer? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        shader.init()
        initUnitBoxBuffers()
        initCelestialBuffers()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat().coerceAtLeast(1f)
        Matrix.perspectiveM(projectionMatrix, 0, 70f, aspect, 0.1f, 150f)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Calculate sky & sun parameters from world time
        // 0=dawn, 6000=noon, 12000=sunset, 18000=midnight
        val time = world.gameTime % 24000f
        val sunAngle = (time / 24000f) * 2f * Math.PI.toFloat() - (Math.PI.toFloat() / 2f)
        val sunDirX = cos(sunAngle)
        val sunDirY = sin(sunAngle)
        val sunDirZ = 0.2f

        // Sky colors
        val skyR: Float
        val skyG: Float
        val skyB: Float
        val sunR: Float
        val sunG: Float
        val sunB: Float
        val ambR: Float
        val ambG: Float
        val ambB: Float

        if (time in 2000f..10000f) {
            // Daytime
            skyR = 0.45f; skyG = 0.65f; skyB = 0.95f
            sunR = 1.0f; sunG = 0.98f; sunB = 0.92f
            ambR = 0.40f; ambG = 0.40f; ambB = 0.45f
        } else if (time in 10000f..13000f) {
            // Sunset
            val p = (time - 10000f) / 3000f
            skyR = 0.45f * (1f - p) + 0.95f * p
            skyG = 0.65f * (1f - p) + 0.45f * p
            skyB = 0.95f * (1f - p) + 0.25f * p
            sunR = 1.0f; sunG = 0.60f; sunB = 0.30f
            ambR = 0.35f; ambG = 0.30f; ambB = 0.30f
        } else if (time in 13000f..21000f) {
            // Night
            skyR = 0.05f; skyG = 0.07f; skyB = 0.14f
            sunR = 0.25f; sunG = 0.30f; sunB = 0.45f // Moonlight
            ambR = 0.12f; ambG = 0.14f; ambB = 0.20f
        } else {
            // Dawn
            val p = (time - 21000f) / 5000f
            skyR = 0.05f * (1f - p) + 0.90f * p
            skyG = 0.07f * (1f - p) + 0.55f * p
            skyB = 0.14f * (1f - p) + 0.50f * p
            sunR = 1.0f; sunG = 0.70f; sunB = 0.40f
            ambR = 0.30f; ambG = 0.30f; ambB = 0.35f
        }

        GLES20.glClearColor(skyR, skyG, skyB, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Build First-Person View Matrix
        val eyeX = player.getEyeX()
        val eyeY = player.getEyeY()
        val eyeZ = player.getEyeZ()

        val look = player.getLookVector()
        val targetX = eyeX + look[0]
        val targetY = eyeY + look[1]
        val targetZ = eyeZ + look[2]

        Matrix.setLookAtM(
            viewMatrix, 0,
            eyeX, eyeY, eyeZ,
            targetX, targetY, targetZ,
            0f, 1f, 0f
        )

        // Draw Sun and Moon in sky
        drawCelestialBodies(sunAngle, eyeX, eyeY, eyeZ)

        // Render Voxel World Chunks
        GLES20.glUseProgram(shader.programId)

        // Set global lighting uniforms
        GLES20.glUniform3f(shader.sunDirHandle, sunDirX, sunDirY, sunDirZ)
        GLES20.glUniform3f(shader.sunColorHandle, sunR, sunG, sunB)
        GLES20.glUniform3f(shader.ambientLightHandle, ambR, ambG, ambB)
        GLES20.glUniform3f(shader.fogColorHandle, skyR, skyG, skyB)
        val fogEnd = (renderDistance * 16f) * 1.15f
        val fogStart = fogEnd * 0.65f
        GLES20.glUniform1f(shader.fogStartHandle, fogStart)
        GLES20.glUniform1f(shader.fogEndHandle, fogEnd)

        // World Identity Model Matrix
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        GLES20.glUniformMatrix4fv(shader.mvMatrixHandle, 1, false, mvMatrix, 0)
        GLES20.glUniformMatrix4fv(shader.mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // Render each chunk
        for (entry in world.chunks) {
            val chunk = entry.value
            val key = entry.key
            var mesh = chunkMeshes[key]
            if (mesh == null) {
                mesh = ChunkMesh(chunk.chunkX, chunk.chunkZ)
                chunkMeshes[key] = mesh
            }

            if (chunk.isDirty || !mesh.isBuilt) {
                mesh.build(world, chunk)
            }

            mesh.render(shader)
        }

        // Render 3D Mobs
        renderMobs()

        // Render Targeted Block Outline Wireframe
        renderTargetOutline()

        // Render First-Person Held Item / Hand in view
        renderFirstPersonHeldItem()
    }

    private fun drawCelestialBodies(sunAngle: Float, eyeX: Float, eyeY: Float, eyeZ: Float) {
        val buf = celestialBuffer ?: return
        GLES20.glUseProgram(shader.wireProgramId)

        // Sun position
        val dist = 80f
        val sunX = eyeX + cos(sunAngle) * dist
        val sunY = eyeY + sin(sunAngle) * dist
        val sunZ = eyeZ

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, sunX, sunY, sunZ)
        Matrix.scaleM(modelMatrix, 0, 10f, 10f, 10f)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        GLES20.glUniformMatrix4fv(shader.wireMvpHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform4f(shader.wireColorHandle, 1f, 0.95f, 0.4f, 1f) // Sun yellow

        buf.position(0)
        GLES20.glVertexAttribPointer(shader.wirePosHandle, 3, GLES20.GL_FLOAT, false, 12, buf)
        GLES20.glEnableVertexAttribArray(shader.wirePosHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)

        // Moon opposite sun
        val moonX = eyeX - cos(sunAngle) * dist
        val moonY = eyeY - sin(sunAngle) * dist
        val moonZ = eyeZ

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, moonX, moonY, moonZ)
        Matrix.scaleM(modelMatrix, 0, 8f, 8f, 8f)
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        GLES20.glUniformMatrix4fv(shader.wireMvpHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform4f(shader.wireColorHandle, 0.9f, 0.95f, 1f, 1f) // Moon white
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

    private fun renderMobs() {
        val buf = boxBuffer ?: return
        GLES20.glUseProgram(shader.wireProgramId)

        buf.position(0)
        GLES20.glVertexAttribPointer(shader.wirePosHandle, 3, GLES20.GL_FLOAT, false, 12, buf)
        GLES20.glEnableVertexAttribArray(shader.wirePosHandle)

        for (mob in mobsList) {
            val isHurt = mob.hurtTimer > 0f
            val baseCol = if (isHurt) floatArrayOf(0.9f, 0.2f, 0.2f, 1f) else mob.type.baseColor
            val accentCol = if (isHurt) floatArrayOf(0.9f, 0.2f, 0.2f, 1f) else mob.type.accentColor

            val swing = sin(mob.walkAnim.toDouble()).toFloat() * 25f

            // Body
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, mob.x, mob.y + 0.6f, mob.z)
            Matrix.rotateM(modelMatrix, 0, -mob.yaw, 0f, 1f, 0f)

            if (mob.type == MobType.ZOMBIE || mob.type == MobType.VILLAGER) {
                // Humanoid body
                Matrix.scaleM(modelMatrix, 0, 0.5f, 0.7f, 0.3f)
            } else {
                // Quadruped body
                Matrix.scaleM(modelMatrix, 0, 0.7f, 0.6f, 1.0f)
            }
            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
            GLES20.glUniformMatrix4fv(shader.wireMvpHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniform4f(shader.wireColorHandle, baseCol[0], baseCol[1], baseCol[2], baseCol[3])
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)

            // Head
            Matrix.setIdentityM(modelMatrix, 0)
            val headY = if (mob.type == MobType.ZOMBIE || mob.type == MobType.VILLAGER) mob.y + 1.25f else mob.y + 0.95f
            val headZ = if (mob.type == MobType.ZOMBIE || mob.type == MobType.VILLAGER) 0f else 0.5f
            Matrix.translateM(modelMatrix, 0, mob.x, headY, mob.z)
            Matrix.rotateM(modelMatrix, 0, -mob.yaw, 0f, 1f, 0f)
            Matrix.translateM(modelMatrix, 0, 0f, 0f, headZ)
            Matrix.scaleM(modelMatrix, 0, 0.45f, 0.45f, 0.45f)

            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
            GLES20.glUniformMatrix4fv(shader.wireMvpHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniform4f(shader.wireColorHandle, accentCol[0], accentCol[1], accentCol[2], accentCol[3])
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)

            // Legs with animated swinging
            if (mob.type == MobType.ZOMBIE || mob.type == MobType.VILLAGER) {
                // 2 Legs
                for (side in listOf(-0.15f, 0.15f)) {
                    val legSwing = if (side < 0) swing else -swing
                    Matrix.setIdentityM(modelMatrix, 0)
                    Matrix.translateM(modelMatrix, 0, mob.x, mob.y + 0.35f, mob.z)
                    Matrix.rotateM(modelMatrix, 0, -mob.yaw, 0f, 1f, 0f)
                    Matrix.translateM(modelMatrix, 0, side, 0f, 0f)
                    Matrix.rotateM(modelMatrix, 0, legSwing, 1f, 0f, 0f)
                    Matrix.scaleM(modelMatrix, 0, 0.2f, 0.55f, 0.2f)

                    Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                    Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
                    GLES20.glUniformMatrix4fv(shader.wireMvpHandle, 1, false, mvpMatrix, 0)
                    GLES20.glUniform4f(shader.wireColorHandle, baseCol[0] * 0.8f, baseCol[1] * 0.8f, baseCol[2] * 0.8f, 1f)
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
                }
            } else {
                // 4 Legs for animals
                for (dx in listOf(-0.25f, 0.25f)) {
                    for (dz in listOf(-0.35f, 0.35f)) {
                        val legSwing = if ((dx > 0 && dz > 0) || (dx < 0 && dz < 0)) swing else -swing
                        Matrix.setIdentityM(modelMatrix, 0)
                        Matrix.translateM(modelMatrix, 0, mob.x, mob.y + 0.25f, mob.z)
                        Matrix.rotateM(modelMatrix, 0, -mob.yaw, 0f, 1f, 0f)
                        Matrix.translateM(modelMatrix, 0, dx, 0f, dz)
                        Matrix.rotateM(modelMatrix, 0, legSwing, 1f, 0f, 0f)
                        Matrix.scaleM(modelMatrix, 0, 0.18f, 0.45f, 0.18f)

                        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)
                        GLES20.glUniformMatrix4fv(shader.wireMvpHandle, 1, false, mvpMatrix, 0)
                        GLES20.glUniform4f(shader.wireColorHandle, baseCol[0] * 0.8f, baseCol[1] * 0.8f, baseCol[2] * 0.8f, 1f)
                        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
                    }
                }
            }
        }
    }

    private fun renderTargetOutline() {
        val target = targetedBlock ?: return
        val buf = wireBoxBuffer ?: return

        GLES20.glUseProgram(shader.wireProgramId)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, target.hitBlockX.toFloat() - 0.002f, target.hitBlockY.toFloat() - 0.002f, target.hitBlockZ.toFloat() - 0.002f)
        Matrix.scaleM(modelMatrix, 0, 1.004f, 1.004f, 1.004f)

        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        GLES20.glUniformMatrix4fv(shader.wireMvpHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform4f(shader.wireColorHandle, 0f, 0f, 0f, 0.85f) // Crisp black outline

        buf.position(0)
        GLES20.glVertexAttribPointer(shader.wirePosHandle, 3, GLES20.GL_FLOAT, false, 12, buf)
        GLES20.glEnableVertexAttribArray(shader.wirePosHandle)

        GLES20.glLineWidth(3f)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 24)
    }

    private fun renderFirstPersonHeldItem() {
        val buf = boxBuffer ?: return
        // Render held block or tool in bottom right view
        GLES20.glUseProgram(shader.wireProgramId)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT) // Draw on top of world

        Matrix.setIdentityM(modelMatrix, 0)
        val eyeX = player.getEyeX()
        val eyeY = player.getEyeY()
        val eyeZ = player.getEyeZ()

        val radYaw = Math.toRadians(player.yaw.toDouble())
        val radPitch = Math.toRadians(player.pitch.toDouble())

        val forwardX = -sin(radYaw).toFloat() * cos(radPitch).toFloat()
        val forwardY = sin(radPitch).toFloat()
        val forwardZ = cos(radYaw).toFloat() * cos(radPitch).toFloat()

        val rightX = cos(radYaw).toFloat()
        val rightZ = sin(radYaw).toFloat()

        // Swing animation angle
        val swing = sin(player.handSwingProgress * Math.PI).toFloat() * 30f

        val itemX = eyeX + forwardX * 0.7f + rightX * 0.35f
        val itemY = eyeY + forwardY * 0.7f - 0.28f + (swing * 0.005f)
        val itemZ = eyeZ + forwardZ * 0.7f + rightZ * 0.35f

        Matrix.translateM(modelMatrix, 0, itemX, itemY, itemZ)
        Matrix.rotateM(modelMatrix, 0, -player.yaw + 35f, 0f, 1f, 0f)
        Matrix.rotateM(modelMatrix, 0, player.pitch - swing, 1f, 0f, 0f)
        Matrix.scaleM(modelMatrix, 0, 0.18f, 0.18f, 0.18f)

        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0)

        GLES20.glUniformMatrix4fv(shader.wireMvpHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform4f(shader.wireColorHandle, 0.55f, 0.40f, 0.25f, 1f) // Held wood/block

        buf.position(0)
        GLES20.glVertexAttribPointer(shader.wirePosHandle, 3, GLES20.GL_FLOAT, false, 12, buf)
        GLES20.glEnableVertexAttribArray(shader.wirePosHandle)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

    private fun initUnitBoxBuffers() {
        // Centered cube (-0.5 .. 0.5)
        val v = floatArrayOf(
            // Top (+Y)
            -0.5f, 0.5f, -0.5f,  0.5f, 0.5f, -0.5f,  0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,  0.5f, 0.5f, 0.5f,  -0.5f, 0.5f, 0.5f,
            // Bottom (-Y)
            -0.5f, -0.5f, 0.5f,  0.5f, -0.5f, 0.5f,  0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,  0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,
            // Front (+Z)
            -0.5f, 0.5f, 0.5f,   0.5f, 0.5f, 0.5f,   0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,   0.5f, -0.5f, 0.5f,  -0.5f, -0.5f, 0.5f,
            // Back (-Z)
            0.5f, 0.5f, -0.5f,  -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,  -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f,
            // Right (+X)
            0.5f, 0.5f, 0.5f,    0.5f, 0.5f, -0.5f,  0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,    0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f,
            // Left (-X)
            -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f,  -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f
        )
        val bb = ByteBuffer.allocateDirect(v.size * 4)
        bb.order(ByteOrder.nativeOrder())
        boxBuffer = bb.asFloatBuffer().apply { put(v); position(0) }

        // Wire box lines (0..1 box)
        val lines = floatArrayOf(
            0f, 0f, 0f,  1f, 0f, 0f,
            1f, 0f, 0f,  1f, 0f, 1f,
            1f, 0f, 1f,  0f, 0f, 1f,
            0f, 0f, 1f,  0f, 0f, 0f,

            0f, 1f, 0f,  1f, 1f, 0f,
            1f, 1f, 0f,  1f, 1f, 1f,
            1f, 1f, 1f,  0f, 1f, 1f,
            0f, 1f, 1f,  0f, 1f, 0f,

            0f, 0f, 0f,  0f, 1f, 0f,
            1f, 0f, 0f,  1f, 1f, 0f,
            1f, 0f, 1f,  1f, 1f, 1f,
            0f, 0f, 1f,  0f, 1f, 1f
        )
        val wbb = ByteBuffer.allocateDirect(lines.size * 4)
        wbb.order(ByteOrder.nativeOrder())
        wireBoxBuffer = wbb.asFloatBuffer().apply { put(lines); position(0) }
    }

    private fun initCelestialBuffers() {
        celestialBuffer = boxBuffer
    }
}
