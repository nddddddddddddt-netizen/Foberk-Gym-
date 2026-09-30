package com.example.voxel.entity

import com.example.voxel.world.BlockType
import com.example.voxel.world.World
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

class Player(
    var x: Float = 8f,
    var y: Float = 22f,
    var z: Float = 8f
) {
    var yaw: Float = 135f   // Facing towards village at chunk (1,1)
    var pitch: Float = -10f // Slight downward angle overlooking scenery

    var vx: Float = 0f
    var vy: Float = 0f
    var vz: Float = 0f

    val width: Float = 0.6f
    val height: Float = 1.8f
    val eyeHeight: Float = 1.62f

    var onGround: Boolean = false
    var inWater: Boolean = false
    var isFlying: Boolean = false
    var isInvulnerable: Boolean = false

    var health: Float = 20f
    val maxHealth: Float = 20f
    var hurtTimer: Float = 0f
    var isDead: Boolean = false

    var fallDistance: Float = 0f

    // Mining state
    var miningBlockX: Int = 0
    var miningBlockY: Int = 0
    var miningBlockZ: Int = 0
    var miningProgress: Float = 0f
    var isMining: Boolean = false

    // Hand swing animation (0f..1f)
    var handSwingProgress: Float = 0f

    fun getEyeX(): Float = x
    fun getEyeY(): Float = y + eyeHeight
    fun getEyeZ(): Float = z

    fun getLookVector(): FloatArray {
        val radYaw = Math.toRadians(yaw.toDouble())
        val radPitch = Math.toRadians(pitch.toDouble())
        val xDir = -sin(radYaw) * cos(radPitch)
        val yDir = sin(radPitch)
        val zDir = cos(radYaw) * cos(radPitch)
        return floatArrayOf(xDir.toFloat(), yDir.toFloat(), zDir.toFloat())
    }

    fun update(
        world: World,
        moveForward: Float, // -1.0 .. 1.0
        moveStrafe: Float,  // -1.0 .. 1.0
        jumpPressed: Boolean,
        dt: Float,
        onHurt: () -> Unit = {}
    ) {
        if (isDead) return

        if (hurtTimer > 0f) hurtTimer -= dt
        if (handSwingProgress > 0f) {
            handSwingProgress -= dt * 4f
            if (handSwingProgress < 0f) handSwingProgress = 0f
        }

        // Check if player is currently in water
        val eyeBlock = world.getBlock(floor(x).toInt(), floor(y + 0.5f).toInt(), floor(z).toInt())
        inWater = (eyeBlock == BlockType.WATER)

        // Calculate movement directions from yaw
        val radYaw = Math.toRadians(yaw.toDouble())
        val forwardX = -sin(radYaw).toFloat()
        val forwardZ = cos(radYaw).toFloat()
        val strafeX = cos(radYaw).toFloat()
        val strafeZ = sin(radYaw).toFloat()

        val speed = if (isFlying) 0.25f else if (inWater) 0.045f else 0.085f

        val targetVx = (forwardX * moveForward + strafeX * moveStrafe) * speed
        val targetVz = (forwardZ * moveForward + strafeZ * moveStrafe) * speed

        // Smooth acceleration
        vx += (targetVx - vx) * 0.4f
        vz += (targetVz - vz) * 0.4f

        // Vertical movement / Gravity
        if (isFlying) {
            if (jumpPressed) {
                vy = 0.15f
            } else {
                vy *= 0.8f
            }
            fallDistance = 0f
        } else if (inWater) {
            if (jumpPressed) {
                vy = 0.08f
            } else {
                vy -= 0.008f // Gentle sinking
                if (vy < -0.1f) vy = -0.1f
            }
            fallDistance = 0f
        } else {
            // Normal gravity
            vy -= 0.032f
            if (vy < -0.6f) vy = -0.6f

            if (jumpPressed && onGround) {
                vy = 0.24f
                onGround = false
            }

            if (vy < 0f) {
                fallDistance += -vy
            }
        }

        // Move with AABB collision detection against world blocks
        moveWithCollision(world, vx, vy, vz, onHurt)
    }

    private fun moveWithCollision(world: World, dx: Float, dy: Float, dz: Float, onHurt: () -> Unit) {
        val prevY = y

        // Move along Y
        y += dy
        if (checkCollision(world)) {
            if (dy < 0f) {
                // Landed on ground
                y = floor(y) + 1.0f
                onGround = true
                if (fallDistance > 3.5f && !isFlying && !isInvulnerable) {
                    val damage = (fallDistance - 3.5f) * 2f
                    takeDamage(damage)
                    onHurt()
                }
                fallDistance = 0f
            } else if (dy > 0f) {
                // Hit ceiling
                y = floor(y + height) - height
            }
            vy = 0f
        } else {
            if (dy < -0.05f) {
                onGround = false
            }
        }

        // Move along X with auto-stepping
        x += dx
        if (checkCollision(world)) {
            // Attempt auto-step up 0.5 block
            if (onGround) {
                y += 0.5f
                if (!checkCollision(world)) {
                    // Step up succeeded!
                } else {
                    y -= 0.5f
                    x -= dx
                    vx = 0f
                }
            } else {
                x -= dx
                vx = 0f
            }
        }

        // Move along Z with auto-stepping
        z += dz
        if (checkCollision(world)) {
            if (onGround) {
                y += 0.5f
                if (!checkCollision(world)) {
                    // Step up succeeded!
                } else {
                    y -= 0.5f
                    z -= dz
                    vz = 0f
                }
            } else {
                z -= dz
                vz = 0f
            }
        }

        // Prevent falling into void below bedrock
        if (y < -5f) {
            takeDamage(999f)
            onHurt()
        }
    }

    private fun checkCollision(world: World): Boolean {
        val halfW = width / 2f
        val minX = floor(x - halfW).toInt()
        val maxX = floor(x + halfW).toInt()
        val minY = floor(y).toInt()
        val maxY = floor(y + height).toInt()
        val minZ = floor(z - halfW).toInt()
        val maxZ = floor(z + halfW).toInt()

        for (bx in minX..maxX) {
            for (by in minY..maxY) {
                for (bz in minZ..maxZ) {
                    val block = world.getBlock(bx, by, bz)
                    if (block.isSolid) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun takeDamage(damage: Float) {
        if (isInvulnerable || isDead) return
        health -= damage
        hurtTimer = 0.4f
        if (health <= 0f) {
            health = 0f
            isDead = true
        }
    }

    fun respawn(spawnX: Float = 8f, spawnY: Float = 22f, spawnZ: Float = 8f) {
        x = spawnX
        y = spawnY
        z = spawnZ
        vx = 0f
        vy = 0f
        vz = 0f
        health = maxHealth
        isDead = false
        fallDistance = 0f
        hurtTimer = 0f
    }
}
