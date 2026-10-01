package com.example.voxel.mod

import com.example.voxel.world.BlockType

enum class TexturePack(
    val id: String,
    val displayName: String,
    val resolution: String,
    val description: String,
    val iconEmoji: String
) {
    VANILLA_CLASSIC("vanilla", "Vanilla Classic", "16x16", "The iconic traditional voxel texture palette", "🌱"),
    FAITHFUL_HD("faithful", "Faithful HD", "32x32", "High-definition enhanced color saturation and detail", "💎"),
    FANTASY_MEDIEVAL("fantasy", "Fantasy Medieval", "32x32", "Warm rustic hues, enchanted forests, and castle stones", "🏰"),
    CYBER_VOXEL("cyber", "Cyber Voxel", "64x64", "Sci-fi neon vibrancy, deep slate ores, and radiant water", "⚡")
}

object TextureConverter {

    var activePack: TexturePack = TexturePack.VANILLA_CLASSIC

    fun getBlockColors(blockType: BlockType, pack: TexturePack = activePack): Array<FloatArray> {
        val baseTop = blockType.topColor.clone()
        val baseBot = blockType.bottomColor.clone()
        val baseSide = blockType.sideColor.clone()

        return when (pack) {
            TexturePack.VANILLA_CLASSIC -> arrayOf(baseTop, baseBot, baseSide)
            TexturePack.FAITHFUL_HD -> {
                // Boost saturation and contrast
                arrayOf(
                    adjustColors(baseTop, 1.15f, 1.1f),
                    adjustColors(baseBot, 1.15f, 1.05f),
                    adjustColors(baseSide, 1.15f, 1.1f)
                )
            }
            TexturePack.FANTASY_MEDIEVAL -> {
                // Warm, golden-brownish medieval tint
                arrayOf(
                    tintColors(baseTop, 1.1f, 0.95f, 0.85f),
                    tintColors(baseBot, 1.05f, 0.90f, 0.80f),
                    tintColors(baseSide, 1.08f, 0.92f, 0.82f)
                )
            }
            TexturePack.CYBER_VOXEL -> {
                // Vibrant neon highlights
                arrayOf(
                    tintColors(baseTop, 0.85f, 1.25f, 1.2f),
                    tintColors(baseBot, 0.75f, 0.85f, 1.15f),
                    tintColors(baseSide, 0.80f, 1.15f, 1.25f)
                )
            }
        }
    }

    private fun adjustColors(c: FloatArray, sat: Float, brightness: Float): FloatArray {
        return floatArrayOf(
            (c[0] * brightness * sat).coerceIn(0f, 1f),
            (c[1] * brightness * sat).coerceIn(0f, 1f),
            (c[2] * brightness * sat).coerceIn(0f, 1f),
            c[3]
        )
    }

    private fun tintColors(c: FloatArray, tr: Float, tg: Float, tb: Float): FloatArray {
        return floatArrayOf(
            (c[0] * tr).coerceIn(0f, 1f),
            (c[1] * tg).coerceIn(0f, 1f),
            (c[2] * tb).coerceIn(0f, 1f),
            c[3]
        )
    }
}
