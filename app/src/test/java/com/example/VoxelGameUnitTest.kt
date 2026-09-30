package com.example

import com.example.voxel.game.CraftingRecipes
import com.example.voxel.game.Inventory
import com.example.voxel.game.Item
import com.example.voxel.game.ItemStack
import com.example.voxel.world.BlockType
import com.example.voxel.world.World
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoxelGameUnitTest {

    @Test
    fun testWorldGenerationAndChunkAccess() {
        val world = World(133742L)
        val chunk = world.getChunk(0, 0, createIfMissing = true)
        assertNotNull("Chunk (0,0) should be generated", chunk)
        assertTrue("Chunk should be marked as generated", chunk!!.isGenerated)

        // Bedrock at y = 0
        val bedrock = world.getBlock(0, 0, 0)
        assertEquals(BlockType.BEDROCK, bedrock)

        // Air far above terrain
        val sky = world.getBlock(0, 47, 0)
        assertEquals(BlockType.AIR, sky)
    }

    @Test
    fun testBlockModificationsPersistence() {
        val world = World(133742L)
        // Modify a block
        world.setBlock(10, 20, 10, BlockType.BRICK)
        assertEquals(BlockType.BRICK, world.getBlock(10, 20, 10))

        // Modify to AIR (mining)
        world.setBlock(10, 20, 10, BlockType.AIR)
        assertEquals(BlockType.AIR, world.getBlock(10, 20, 10))
    }

    @Test
    fun testInventoryAddingAndStacking() {
        val inv = Inventory(36)

        // Add 10 planks
        val added = inv.addItem(Item.WOOD_PLANKS_BLOCK, 10)
        assertTrue(added)
        assertEquals(10, inv.getSlot(0)?.count)
        assertEquals(Item.WOOD_PLANKS_BLOCK, inv.getSlot(0)?.item)

        // Add 5 more planks -> should stack into same slot
        inv.addItem(Item.WOOD_PLANKS_BLOCK, 5)
        assertEquals(15, inv.getSlot(0)?.count)

        // Consume held item
        inv.selectedHotbarIndex = 0
        inv.consumeHeldItem()
        assertEquals(14, inv.getSlot(0)?.count)
    }

    @Test
    fun testCraftingRecipes() {
        // Logs -> Planks recipe
        val logRecipe = CraftingRecipes.ALL_RECIPES.first { it.id == "log_to_planks" }
        assertFalse(
            "Cannot craft without logs",
            CraftingRecipes.canCraft(logRecipe, emptyList(), atCraftingTable = false)
        )

        val invWithLog = listOf(ItemStack(Item.WOOD_LOG_BLOCK, 1))
        assertTrue(
            "Can craft planks with 1 log",
            CraftingRecipes.canCraft(logRecipe, invWithLog, atCraftingTable = false)
        )

        // Crafting table recipe: Wooden Pickaxe requires crafting table
        val pickRecipe = CraftingRecipes.ALL_RECIPES.first { it.id == "wood_pickaxe" }
        val pickMaterials = listOf(
            ItemStack(Item.WOOD_PLANKS_BLOCK, 3),
            ItemStack(Item.STICK, 2)
        )
        assertFalse(
            "Wooden pickaxe cannot be crafted in pocket",
            CraftingRecipes.canCraft(pickRecipe, pickMaterials, atCraftingTable = false)
        )
        assertTrue(
            "Wooden pickaxe can be crafted at crafting table",
            CraftingRecipes.canCraft(pickRecipe, pickMaterials, atCraftingTable = true)
        )
    }

    @Test
    fun testRaycastDetection() {
        val world = World(133742L)
        // Place target block in air layer where surrounding is AIR
        world.setBlock(5, 40, 5, BlockType.STONE)

        // Raycast from (5.5, 40.5, 1.0) looking forward along +Z towards (5, 40, 5)
        val hit = world.raycast(
            originX = 5.5f, originY = 40.5f, originZ = 1.0f,
            dirX = 0f, dirY = 0f, dirZ = 1f,
            maxDistance = 6f
        )
        assertNotNull("Raycast should hit the stone block", hit)
        assertEquals(5, hit!!.hitBlockX)
        assertEquals(40, hit.hitBlockY)
        assertEquals(5, hit.hitBlockZ)
        assertEquals(BlockType.STONE, hit.blockType)
    }
}
