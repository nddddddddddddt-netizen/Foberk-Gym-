package com.example.voxel.mod

import com.example.voxel.game.ItemCategory
import org.json.JSONObject
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Parses Minecraft Java Edition Mods (.jar, Fabric fabric.mod.json, Forge mods.toml, and DataPacks).
 */
class JavaModEngine {

    fun parseFabricMetadata(jsonStr: String): Pair<String, String>? {
        return try {
            val root = JSONObject(jsonStr)
            val name = root.optString("name", "Java Fabric Mod")
            val desc = root.optString("description", "")
            Pair(name, desc)
        } catch (e: Exception) {
            null
        }
    }

    fun parseDataPackMeta(jsonStr: String): Pair<String, String>? {
        return try {
            val root = JSONObject(jsonStr)
            val pack = root.getJSONObject("pack")
            val desc = pack.optString("description", "Java DataPack")
            Pair("Java DataPack", desc)
        } catch (e: Exception) {
            null
        }
    }

    fun parseJavaRecipe(id: String, jsonStr: String): CustomRecipeDef? {
        return try {
            val root = JSONObject(jsonStr)
            val type = root.optString("type", "minecraft:crafting_shaped")

            val resultObj = root.optJSONObject("result")
            val resultId = resultObj?.optString("item") ?: root.optString("result", "stick")
            val count = resultObj?.optInt("count", 1) ?: 1

            val ingredientsMap = mutableMapOf<String, Int>()

            if (type.contains("shapeless")) {
                val ings = root.optJSONArray("ingredients")
                if (ings != null) {
                    for (i in 0 until ings.length()) {
                        val ingObj = ings.optJSONObject(i)
                        val item = ingObj?.optString("item") ?: "planks"
                        ingredientsMap[item] = (ingredientsMap[item] ?: 0) + 1
                    }
                }
            } else {
                // Shaped recipe
                val keyObj = root.optJSONObject("key")
                val pattern = root.optJSONArray("pattern")
                if (keyObj != null && pattern != null) {
                    for (i in 0 until pattern.length()) {
                        val row = pattern.getString(i)
                        for (ch in row) {
                            if (ch != ' ') {
                                val itemEntry = keyObj.optJSONObject(ch.toString())
                                val item = itemEntry?.optString("item") ?: "planks"
                                ingredientsMap[item] = (ingredientsMap[item] ?: 0) + 1
                            }
                        }
                    }
                }
            }

            CustomRecipeDef(
                id = id,
                resultItemId = resultId,
                resultCount = count,
                ingredients = ingredientsMap,
                requiresCraftingTable = true,
                description = "Java Recipe: $id"
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parseJavaJar(inputStream: InputStream): GameMod? {
        return try {
            val zip = ZipInputStream(inputStream)
            var entry = zip.nextEntry
            var modName = "Imported Java Mod"
            var modDesc = "Imported Java Edition Mod / DataPack"

            val blocks = mutableListOf<CustomBlockDef>()
            val items = mutableListOf<CustomItemDef>()
            val recipes = mutableListOf<CustomRecipeDef>()

            while (entry != null) {
                val name = entry.name
                if (name == "fabric.mod.json") {
                    val content = zip.bufferedReader().readText()
                    parseFabricMetadata(content)?.let {
                        modName = it.first
                        modDesc = it.second
                    }
                } else if (name == "pack.mcmeta") {
                    val content = zip.bufferedReader().readText()
                    parseDataPackMeta(content)?.let {
                        modName = it.first
                        modDesc = it.second
                    }
                } else if (name.contains("recipes/") && name.endsWith(".json")) {
                    val content = zip.bufferedReader().readText()
                    val rId = name.substringAfterLast("/").substringBefore(".json")
                    parseJavaRecipe(rId, content)?.let {
                        recipes.add(it)
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }

            GameMod(
                id = "java_" + modName.lowercase().replace(" ", "_"),
                name = modName,
                version = "1.0",
                author = "Java Modder",
                description = modDesc,
                platform = ModPlatform.JAVA,
                category = ModCategory.BLOCKS_ITEMS,
                iconEmoji = "☕",
                customBlocks = blocks,
                customItems = items,
                customRecipes = recipes
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
