package com.example.voxel.auth

enum class PlayerSkin(
    val id: String,
    val displayName: String,
    val description: String,
    val headEmoji: String,
    val skinColor: FloatArray,
    val shirtColor: FloatArray
) {
    STEVE(
        "steve",
        "Steve the Miner",
        "Classic iconic blue shirt and jeans",
        "🧔",
        floatArrayOf(0.75f, 0.55f, 0.40f, 1f),
        floatArrayOf(0.15f, 0.55f, 0.65f, 1f)
    ),
    ALEX(
        "alex",
        "Alex the Explorer",
        "Green tunic and sleek adventurer boots",
        "👱‍♀️",
        floatArrayOf(0.85f, 0.65f, 0.50f, 1f),
        floatArrayOf(0.35f, 0.58f, 0.28f, 1f)
    ),
    KNIGHT(
        "knight",
        "Iron Knight",
        "Polished steel armor with crimson cape",
        "🛡️",
        floatArrayOf(0.65f, 0.65f, 0.70f, 1f),
        floatArrayOf(0.75f, 0.15f, 0.15f, 1f)
    ),
    NINJA(
        "ninja",
        "Shadow Ninja",
        "Midnight stealth gi with dark mask",
        "🥷",
        floatArrayOf(0.20f, 0.20f, 0.22f, 1f),
        floatArrayOf(0.10f, 0.10f, 0.12f, 1f)
    ),
    WIZARD(
        "wizard",
        "Arcane Wizard",
        "Deep mystical purple robes and gold trim",
        "🧙‍♂️",
        floatArrayOf(0.80f, 0.65f, 0.55f, 1f),
        floatArrayOf(0.45f, 0.15f, 0.65f, 1f)
    ),
    CYBER(
        "cyber",
        "Cyber Miner 2077",
        "Glowing neon circuit visor and cybernetic gear",
        "🤖",
        floatArrayOf(0.30f, 0.85f, 0.90f, 1f),
        floatArrayOf(0.10f, 0.20f, 0.35f, 1f)
    )
}

data class PlayerAccount(
    val id: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    var skin: PlayerSkin = PlayerSkin.STEVE,
    var level: Int = 1,
    var blocksMined: Int = 0,
    var blocksPlaced: Int = 0,
    var mobsDefeated: Int = 0,
    var diamondsCollected: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    var lastLoginAt: Long = System.currentTimeMillis()
)
