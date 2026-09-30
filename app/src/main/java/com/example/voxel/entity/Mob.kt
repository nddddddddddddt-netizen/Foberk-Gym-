package com.example.voxel.entity

import com.example.voxel.world.BlockType
import com.example.voxel.world.World
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class MobType(
    val displayName: String,
    val isHostile: Boolean,
    val maxHealth: Float,
    val moveSpeed: Float,
    val baseColor: FloatArray,
    val accentColor: FloatArray
) {
    SHEEP(
        "Sheep",
        false,
        8f,
        0.04f,
        floatArrayOf(0.92f, 0.92f, 0.92f, 1f), // White wool
        floatArrayOf(0.85f, 0.70f, 0.65f, 1f)  // Pinkish face
    ),
    COW(
        "Cow",
        false,
        10f,
        0.04f,
        floatArrayOf(0.40f, 0.28f, 0.18f, 1f), // Brown hide
        floatArrayOf(0.90f, 0.90f, 0.90f, 1f)  // White patches
    ),
    PIG(
        "Pig",
        false,
        10f,
        0.045f,
        floatArrayOf(0.95f, 0.65f, 0.68f, 1f), // Pink body
        floatArrayOf(0.88f, 0.50f, 0.55f, 1f)  // Snout
    ),
    VILLAGER(
        "Villager",
        false,
        20f,
        0.05f,
        floatArrayOf(0.45f, 0.28f, 0.18f, 1f), // Brown robes
        floatArrayOf(0.82f, 0.62f, 0.48f, 1f)  // Skin tone
    ),
    ZOMBIE(
        "Zombie",
        true,
        20f,
        0.055f,
        floatArrayOf(0.25f, 0.50f, 0.25f, 1f), // Green rotting skin
        floatArrayOf(0.18f, 0.45f, 0.55f, 1f)  // Cyan shirt
    )
}

enum class MobState {
    IDLE, WANDERING, FLEEING, CHASING, ATTACKING
}

class Mob(
    val id: String = "mob_" + Random.nextInt(100000),
    val type: MobType,
    var x: Float,
    var y: Float,
    var z: Float
) {
    var yaw: Float = Random.nextFloat() * 360f
    var pitch: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var vz: Float = 0f
    var onGround: Boolean = false

    var health: Float = type.maxHealth
    var hurtTimer: Float = 0f
    var state: MobState = MobState.IDLE
    var stateTimer: Float = 0f

    var walkAnim: Float = 0f
    var attackCooldown: Float = 0f
    var soundTimer: Float = Random.nextFloat() * 15f + 5f

    fun update(world: World, playerX: Float, playerY: Float, playerZ: Float, allMobs: List<Mob>, dt: Float) {
        stateTimer -= dt
        if (hurtTimer > 0f) hurtTimer -= dt
        if (attackCooldown > 0f) attackCooldown -= dt
        if (soundTimer > 0f) soundTimer -= dt

        val distToPlayer = sqrt((playerX - x) * (playerX - x) + (playerZ - z) * (playerZ - z))

        // AI decision making
        when (type) {
            MobType.ZOMBIE -> {
                // Find closest target (player or villager)
                var targetX = playerX
                var targetZ = playerZ
                var minDist = distToPlayer

                for (mob in allMobs) {
                    if (mob.type == MobType.VILLAGER && mob.health > 0) {
                        val d = sqrt((mob.x - x) * (mob.x - x) + (mob.z - z) * (mob.z - z))
                        if (d < minDist) {
                            minDist = d
                            targetX = mob.x
                            targetZ = mob.z
                        }
                    }
                }

                if (minDist < 16f) {
                    state = MobState.CHASING
                    val angle = atan2((targetZ - z).toDouble(), (targetX - x).toDouble()).toFloat()
                    yaw = Math.toDegrees(angle.toDouble()).toFloat() - 90f
                    val speed = type.moveSpeed
                    vx = cos(angle.toDouble()).toFloat() * speed
                    vz = sin(angle.toDouble()).toFloat() * speed

                    // Melee attack range
                    if (minDist < 1.4f && attackCooldown <= 0f) {
                        state = MobState.ATTACKING
                        attackCooldown = 1.0f
                    }
                } else {
                    wanderAI()
                }
            }
            MobType.VILLAGER -> {
                // Look for nearby zombies to flee from
                var closestZombie: Mob? = null
                var closestZDist = 12f
                for (mob in allMobs) {
                    if (mob.type == MobType.ZOMBIE && mob.health > 0) {
                        val d = sqrt((mob.x - x) * (mob.x - x) + (mob.z - z) * (mob.z - z))
                        if (d < closestZDist) {
                            closestZDist = d
                            closestZombie = mob
                        }
                    }
                }

                if (closestZombie != null) {
                    state = MobState.FLEEING
                    val angle = atan2((z - closestZombie.z).toDouble(), (x - closestZombie.x).toDouble()).toFloat()
                    yaw = Math.toDegrees(angle.toDouble()).toFloat() - 90f
                    val speed = type.moveSpeed * 1.3f
                    vx = cos(angle.toDouble()).toFloat() * speed
                    vz = sin(angle.toDouble()).toFloat() * speed
                } else {
                    wanderAI()
                }
            }
            else -> {
                // Peaceful animals
                wanderAI()
            }
        }

        // Apply physics
        vy -= 0.025f // Gravity
        if (vy < -0.4f) vy = -0.4f

        // Check ground and step up
        val groundY = getGroundY(world, x + vx, y, z + vz)
        if (groundY != null) {
            val stepDiff = groundY - y
            if (stepDiff in 0f..1.1f) {
                // Step up smoothly
                y = groundY
                vy = 0f
                onGround = true
            } else if (stepDiff < 0f && y + vy <= groundY) {
                y = groundY
                vy = 0f
                onGround = true
            } else {
                y += vy
                onGround = false
            }
        } else {
            y += vy
            onGround = false
        }

        x += vx
        z += vz

        val horizontalSpeed = sqrt(vx * vx + vz * vz)
        if (horizontalSpeed > 0.005f) {
            walkAnim += horizontalSpeed * 8f
        }
    }

    private fun wanderAI() {
        if (stateTimer <= 0f) {
            if (Random.nextFloat() < 0.6f) {
                state = MobState.WANDERING
                val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                yaw = Math.toDegrees(angle.toDouble()).toFloat()
                vx = cos(angle.toDouble()).toFloat() * type.moveSpeed * 0.5f
                vz = sin(angle.toDouble()).toFloat() * type.moveSpeed * 0.5f
                stateTimer = Random.nextFloat() * 3f + 1f
            } else {
                state = MobState.IDLE
                vx = 0f
                vz = 0f
                stateTimer = Random.nextFloat() * 2f + 1f
            }
        }
    }

    private fun getGroundY(world: World, targetX: Float, currentY: Float, targetZ: Float): Float? {
        val ix = kotlin.math.floor(targetX.toDouble()).toInt()
        val iz = kotlin.math.floor(targetZ.toDouble()).toInt()
        val currentIY = kotlin.math.floor(currentY.toDouble()).toInt()

        for (checkY in (currentIY + 2) downTo (currentIY - 4)) {
            val block = world.getBlock(ix, checkY, iz)
            if (block.isSolid) {
                return (checkY + 1).toFloat()
            }
        }
        return null
    }

    fun takeDamage(damage: Float): Boolean {
        health -= damage
        hurtTimer = 0.3f
        vy = 0.15f // Knockback pop
        return health <= 0f
    }
}
