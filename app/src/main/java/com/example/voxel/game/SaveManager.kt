package com.example.voxel.game

import android.content.Context
import com.example.voxel.world.BlockType
import com.example.voxel.world.World
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class SavedWorldData(
    val seed: Long,
    val gameTime: Float,
    val playerX: Float,
    val playerY: Float,
    val playerZ: Float,
    val playerYaw: Float,
    val playerPitch: Float,
    val playerHealth: Float,
    val inventory: List<ItemStack?>,
    val selectedHotbarIndex: Int,
    val modifiedBlocks: Map<Long, BlockType>,
    val chestInventories: Map<Long, Inventory>
)

object SaveManager {

    private const val SAVE_FILE_NAME = "voxel_world_save.json"

    fun saveWorld(
        context: Context,
        world: World,
        playerX: Float,
        playerY: Float,
        playerZ: Float,
        playerYaw: Float,
        playerPitch: Float,
        playerHealth: Float,
        inventory: Inventory
    ): Boolean {
        return try {
            val root = JSONObject()
            root.put("seed", world.seed)
            root.put("gameTime", world.gameTime)
            root.put("playerX", playerX.toDouble())
            root.put("playerY", playerY.toDouble())
            root.put("playerZ", playerZ.toDouble())
            root.put("playerYaw", playerYaw.toDouble())
            root.put("playerPitch", playerPitch.toDouble())
            root.put("playerHealth", playerHealth.toDouble())
            root.put("selectedHotbarIndex", inventory.selectedHotbarIndex)

            // Save inventory
            val invArray = JSONArray()
            for (i in 0 until inventory.totalSlots) {
                val stack = inventory.getSlot(i)
                if (stack != null) {
                    val obj = JSONObject()
                    obj.put("slot", i)
                    obj.put("itemId", stack.item.id)
                    obj.put("count", stack.count)
                    invArray.put(obj)
                }
            }
            root.put("inventory", invArray)

            // Save modified blocks
            val blocksArray = JSONArray()
            for (entry in world.modifiedBlocks) {
                val obj = JSONObject()
                obj.put("key", entry.key)
                obj.put("blockId", entry.value.id.toInt())
                blocksArray.put(obj)
            }
            root.put("modifiedBlocks", blocksArray)

            // Save chest inventories
            val chestsArray = JSONArray()
            for (entry in world.chestInventories) {
                val chestObj = JSONObject()
                chestObj.put("key", entry.key)
                val cItems = JSONArray()
                val cinv = entry.value
                for (ci in 0 until cinv.totalSlots) {
                    val cstack = cinv.getSlot(ci)
                    if (cstack != null) {
                        val itemObj = JSONObject()
                        itemObj.put("slot", ci)
                        itemObj.put("itemId", cstack.item.id)
                        itemObj.put("count", cstack.count)
                        cItems.put(itemObj)
                    }
                }
                chestObj.put("items", cItems)
                chestsArray.put(chestObj)
            }
            root.put("chests", chestsArray)

            // Write atomically to file
            val file = File(context.filesDir, SAVE_FILE_NAME)
            val tempFile = File(context.filesDir, "$SAVE_FILE_NAME.tmp")
            tempFile.writeText(root.toString())
            if (tempFile.renameTo(file)) {
                true
            } else {
                file.writeText(root.toString())
                tempFile.delete()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadWorld(context: Context): SavedWorldData? {
        return try {
            val file = File(context.filesDir, SAVE_FILE_NAME)
            if (!file.exists()) return null

            val jsonStr = file.readText()
            val root = JSONObject(jsonStr)

            val seed = root.optLong("seed", 133742L)
            val gameTime = root.optDouble("gameTime", 6000.0).toFloat()
            val playerX = root.optDouble("playerX", 8.0).toFloat()
            val playerY = root.optDouble("playerY", 22.0).toFloat()
            val playerZ = root.optDouble("playerZ", 8.0).toFloat()
            val playerYaw = root.optDouble("playerYaw", 135.0).toFloat()
            val playerPitch = root.optDouble("playerPitch", -10.0).toFloat()
            val playerHealth = root.optDouble("playerHealth", 20.0).toFloat()
            val selectedHotbarIndex = root.optInt("selectedHotbarIndex", 0)

            val invList = MutableList<ItemStack?>(36) { null }
            val invArray = root.optJSONArray("inventory")
            if (invArray != null) {
                for (i in 0 until invArray.length()) {
                    val obj = invArray.getJSONObject(i)
                    val slot = obj.getInt("slot")
                    val itemId = obj.getString("itemId")
                    val count = obj.getInt("count")
                    if (slot in 0 until 36) {
                        invList[slot] = ItemStack(Item.fromId(itemId), count)
                    }
                }
            }

            val modBlocks = mutableMapOf<Long, BlockType>()
            val blocksArray = root.optJSONArray("modifiedBlocks")
            if (blocksArray != null) {
                for (i in 0 until blocksArray.length()) {
                    val obj = blocksArray.getJSONObject(i)
                    val key = obj.getLong("key")
                    val blockId = obj.getInt("blockId").toByte()
                    modBlocks[key] = BlockType.fromId(blockId)
                }
            }

            val chests = mutableMapOf<Long, Inventory>()
            val chestsArray = root.optJSONArray("chests")
            if (chestsArray != null) {
                for (i in 0 until chestsArray.length()) {
                    val obj = chestsArray.getJSONObject(i)
                    val key = obj.getLong("key")
                    val chestInv = Inventory(27)
                    val cItems = obj.optJSONArray("items")
                    if (cItems != null) {
                        for (ci in 0 until cItems.length()) {
                            val itemObj = cItems.getJSONObject(ci)
                            val slot = itemObj.getInt("slot")
                            val itemId = itemObj.getString("itemId")
                            val count = itemObj.getInt("count")
                            chestInv.setSlot(slot, ItemStack(Item.fromId(itemId), count))
                        }
                    }
                    chests[key] = chestInv
                }
            }

            SavedWorldData(
                seed = seed,
                gameTime = gameTime,
                playerX = playerX,
                playerY = playerY,
                playerZ = playerZ,
                playerYaw = playerYaw,
                playerPitch = playerPitch,
                playerHealth = playerHealth,
                inventory = invList,
                selectedHotbarIndex = selectedHotbarIndex,
                modifiedBlocks = modBlocks,
                chestInventories = chests
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun hasSave(context: Context): Boolean {
        return File(context.filesDir, SAVE_FILE_NAME).exists()
    }

    fun deleteSave(context: Context) {
        val file = File(context.filesDir, SAVE_FILE_NAME)
        if (file.exists()) file.delete()
    }
}
