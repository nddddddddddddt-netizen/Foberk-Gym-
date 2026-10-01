package com.example.voxel.mod

import com.example.voxel.game.ItemCategory
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Parses Minecraft Bedrock Edition Addons (.mcaddon, .mcpack, manifest.json, behavior & resource JSONs).
 */
class BedrockModEngine {

    fun parseManifest(jsonStr: String): Pair<String, String>? {
        return try {
            val root = JSONObject(jsonStr)
            val header = root.getJSONObject("header")
            val name = header.optString("name", "Bedrock Addon")
            val desc = header.optString("description", "")
            Pair(name, desc)
        } catch (e: Exception) {
            null
        }
    }

    fun parseBedrockBlock(id: String, jsonStr: String): CustomBlockDef? {
        return try {
            val root = JSONObject(jsonStr)
            val blockObj = root.optJSONObject("minecraft:block") ?: return null
            val desc = blockObj.optJSONObject("description")
            val identifier = desc?.optString("identifier") ?: id

            val components = blockObj.optJSONObject("components")
            val hardness = components?.optDouble("minecraft:destructible_by_mining", 1.5)?.toFloat() ?: 1.5f
            val lightEmission = components?.optInt("minecraft:light_emission", 0) ?: 0
            val isLight = lightEmission > 0

            // Generate representative color based on block name / identifier
            val colors = generateColorFromIdentifier(identifier)

            CustomBlockDef(
                id = identifier,
                displayName = identifier.substringAfter(":").replace("_", " ").capitalize(),
                hardness = hardness,
                isSolid = true,
                isLightSource = isLight,
                topColor = colors[0],
                bottomColor = colors[1],
                sideColor = colors[2],
                dropItemId = identifier
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseBedrockItem(id: String, jsonStr: String): CustomItemDef? {
        return try {
            val root = JSONObject(jsonStr)
            val itemObj = root.optJSONObject("minecraft:item") ?: return null
            val desc = itemObj.optJSONObject("description")
            val identifier = desc?.optString("identifier") ?: id

            val components = itemObj.optJSONObject("components")
            val maxStack = components?.optInt("minecraft:max_stack_size", 64) ?: 64

            var damage = 1.0f
            var speed = 1.0f
            var category = ItemCategory.TOOL
            var emoji = "✨"

            if (identifier.contains("sword")) {
                category = ItemCategory.WEAPON
                damage = 7.0f
                emoji = "⚔️"
            } else if (identifier.contains("pickaxe")) {
                speed = 5.0f
                emoji = "⛏️"
            } else if (identifier.contains("axe")) {
                damage = 5.0f
                speed = 4.0f
                emoji = "🪓"
            } else if (identifier.contains("gem") || identifier.contains("ingot") || identifier.contains("ore")) {
                category = ItemCategory.MATERIAL
                emoji = "💎"
            }

            CustomItemDef(
                id = identifier,
                displayName = identifier.substringAfter(":").replace("_", " ").capitalize(),
                category = category,
                maxStack = maxStack,
                damage = damage,
                miningSpeedMultiplier = speed,
                harvestLevel = 3,
                iconEmoji = emoji
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseBedrockEntity(id: String, jsonStr: String): CustomMobDef? {
        return try {
            val root = JSONObject(jsonStr)
            val entityObj = root.optJSONObject("minecraft:entity") ?: return null
            val desc = entityObj.optJSONObject("description")
            val identifier = desc?.optString("identifier") ?: id

            val components = entityObj.optJSONObject("components")
            val healthObj = components?.optJSONObject("minecraft:health")
            val maxHealth = healthObj?.optDouble("max", 20.0)?.toFloat() ?: 20f

            val movementObj = components?.optJSONObject("minecraft:movement")
            val speed = movementObj?.optDouble("value", 0.05)?.toFloat() ?: 0.05f

            val attackObj = components?.optJSONObject("minecraft:attack")
            val damage = attackObj?.optDouble("damage", 4.0)?.toFloat() ?: 3.0f

            val isHostile = identifier.contains("zombie") || identifier.contains("monster") || identifier.contains("dragon") || identifier.contains("boss")

            val baseCol = if (isHostile) floatArrayOf(0.7f, 0.15f, 0.15f, 1f) else floatArrayOf(0.2f, 0.6f, 0.8f, 1f)
            val accCol = if (isHostile) floatArrayOf(0.9f, 0.3f, 0.1f, 1f) else floatArrayOf(0.9f, 0.9f, 0.3f, 1f)

            CustomMobDef(
                id = identifier,
                displayName = identifier.substringAfter(":").replace("_", " ").capitalize(),
                isHostile = isHostile,
                maxHealth = maxHealth,
                moveSpeed = speed.coerceIn(0.03f, 0.09f),
                attackDamage = damage,
                baseColor = baseCol,
                accentColor = accCol
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseMcAddonZip(inputStream: InputStream): GameMod? {
        return try {
            val zip = ZipInputStream(inputStream)
            var entry = zip.nextEntry
            var modName = "Imported Bedrock Addon"
            var modDesc = "Imported Bedrock Addon from .mcaddon"

            val blocks = mutableListOf<CustomBlockDef>()
            val items = mutableListOf<CustomItemDef>()
            val mobs = mutableListOf<CustomMobDef>()

            while (entry != null) {
                val name = entry.name
                if (name.endsWith("manifest.json")) {
                    val content = zip.bufferedReader().readText()
                    val pair = parseManifest(content)
                    if (pair != null) {
                        modName = pair.first
                        modDesc = pair.second
                    }
                } else if (name.startsWith("blocks/") && name.endsWith(".json")) {
                    val content = zip.bufferedReader().readText()
                    parseBedrockBlock(name.substringAfterLast("/").substringBefore(".json"), content)?.let {
                        blocks.add(it)
                    }
                } else if (name.startsWith("items/") && name.endsWith(".json")) {
                    val content = zip.bufferedReader().readText()
                    parseBedrockItem(name.substringAfterLast("/").substringBefore(".json"), content)?.let {
                        items.add(it)
                    }
                } else if (name.startsWith("entities/") && name.endsWith(".json")) {
                    val content = zip.bufferedReader().readText()
                    parseBedrockEntity(name.substringAfterLast("/").substringBefore(".json"), content)?.let {
                        mobs.add(it)
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }

            GameMod(
                id = "bedrock_" + modName.lowercase().replace(" ", "_"),
                name = modName,
                version = "1.0",
                author = "Bedrock Community",
                description = modDesc,
                platform = ModPlatform.BEDROCK,
                category = ModCategory.BLOCKS_ITEMS,
                iconEmoji = "📦",
                customBlocks = blocks,
                customItems = items,
                customMobs = mobs
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun generateColorFromIdentifier(id: String): Array<FloatArray> {
        val hash = Math.abs(id.hashCode())
        val r = ((hash and 0xFF0000) shr 16) / 255f
        val g = ((hash and 0x00FF00) shr 8) / 255f
        val b = (hash and 0x0000FF) / 255f

        val top = floatArrayOf((r * 0.9f).coerceIn(0.1f, 1f), (g * 0.9f).coerceIn(0.1f, 1f), (b * 0.9f).coerceIn(0.1f, 1f), 1f)
        val bot = floatArrayOf((r * 0.7f).coerceIn(0.1f, 1f), (g * 0.7f).coerceIn(0.1f, 1f), (b * 0.7f).coerceIn(0.1f, 1f), 1f)
        val side = floatArrayOf((r * 0.8f).coerceIn(0.1f, 1f), (g * 0.8f).coerceIn(0.1f, 1f), (b * 0.8f).coerceIn(0.1f, 1f), 1f)
        return arrayOf(top, bot, side)
    }

    private fun String.capitalize(): String {
        return this.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
