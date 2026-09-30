package com.example.voxel.game

data class RecipeIngredient(
    val item: Item,
    val count: Int
)

data class CraftingRecipe(
    val id: String,
    val result: ItemStack,
    val ingredients: List<RecipeIngredient>,
    val requiresCraftingTable: Boolean = false,
    val description: String = ""
)

object CraftingRecipes {

    val ALL_RECIPES = listOf(
        // Logs -> 4 Planks
        CraftingRecipe(
            id = "log_to_planks",
            result = ItemStack(Item.WOOD_PLANKS_BLOCK, 4),
            ingredients = listOf(RecipeIngredient(Item.WOOD_LOG_BLOCK, 1)),
            requiresCraftingTable = false,
            description = "1 Oak Log → 4 Oak Planks"
        ),
        // 2 Planks -> 4 Sticks
        CraftingRecipe(
            id = "planks_to_sticks",
            result = ItemStack(Item.STICK, 4),
            ingredients = listOf(RecipeIngredient(Item.WOOD_PLANKS_BLOCK, 2)),
            requiresCraftingTable = false,
            description = "2 Oak Planks → 4 Sticks"
        ),
        // 4 Planks -> Crafting Table
        CraftingRecipe(
            id = "crafting_table",
            result = ItemStack(Item.CRAFTING_TABLE_BLOCK, 1),
            ingredients = listOf(RecipeIngredient(Item.WOOD_PLANKS_BLOCK, 4)),
            requiresCraftingTable = false,
            description = "4 Oak Planks → Crafting Table"
        ),
        // 1 Coal + 1 Stick -> 4 Torches
        CraftingRecipe(
            id = "torches",
            result = ItemStack(Item.TORCH_BLOCK, 4),
            ingredients = listOf(
                RecipeIngredient(Item.COAL, 1),
                RecipeIngredient(Item.STICK, 1)
            ),
            requiresCraftingTable = false,
            description = "1 Coal + 1 Stick → 4 Torches"
        ),
        // 8 Planks -> Chest
        CraftingRecipe(
            id = "chest",
            result = ItemStack(Item.CHEST_BLOCK, 1),
            ingredients = listOf(RecipeIngredient(Item.WOOD_PLANKS_BLOCK, 8)),
            requiresCraftingTable = true,
            description = "8 Oak Planks → Storage Chest"
        ),
        // 3 Planks + 2 Sticks -> Wooden Pickaxe
        CraftingRecipe(
            id = "wood_pickaxe",
            result = ItemStack(Item.WOODEN_PICKAXE, 1),
            ingredients = listOf(
                RecipeIngredient(Item.WOOD_PLANKS_BLOCK, 3),
                RecipeIngredient(Item.STICK, 2)
            ),
            requiresCraftingTable = true,
            description = "3 Planks + 2 Sticks → Wooden Pickaxe"
        ),
        // 3 Cobblestone + 2 Sticks -> Stone Pickaxe
        CraftingRecipe(
            id = "stone_pickaxe",
            result = ItemStack(Item.STONE_PICKAXE, 1),
            ingredients = listOf(
                RecipeIngredient(Item.COBBLESTONE_BLOCK, 3),
                RecipeIngredient(Item.STICK, 2)
            ),
            requiresCraftingTable = true,
            description = "3 Cobblestone + 2 Sticks → Stone Pickaxe"
        ),
        // 3 Planks + 2 Sticks -> Wooden Axe
        CraftingRecipe(
            id = "wood_axe",
            result = ItemStack(Item.WOODEN_AXE, 1),
            ingredients = listOf(
                RecipeIngredient(Item.WOOD_PLANKS_BLOCK, 3),
                RecipeIngredient(Item.STICK, 2)
            ),
            requiresCraftingTable = true,
            description = "3 Planks + 2 Sticks → Wooden Axe"
        ),
        // 3 Cobblestone + 2 Sticks -> Stone Axe
        CraftingRecipe(
            id = "stone_axe",
            result = ItemStack(Item.STONE_AXE, 1),
            ingredients = listOf(
                RecipeIngredient(Item.COBBLESTONE_BLOCK, 3),
                RecipeIngredient(Item.STICK, 2)
            ),
            requiresCraftingTable = true,
            description = "3 Cobblestone + 2 Sticks → Stone Axe"
        ),
        // 2 Planks + 1 Stick -> Wooden Sword
        CraftingRecipe(
            id = "wood_sword",
            result = ItemStack(Item.WOODEN_SWORD, 1),
            ingredients = listOf(
                RecipeIngredient(Item.WOOD_PLANKS_BLOCK, 2),
                RecipeIngredient(Item.STICK, 1)
            ),
            requiresCraftingTable = false,
            description = "2 Planks + 1 Stick → Wooden Sword"
        ),
        // 2 Cobblestone + 1 Stick -> Stone Sword
        CraftingRecipe(
            id = "stone_sword",
            result = ItemStack(Item.STONE_SWORD, 1),
            ingredients = listOf(
                RecipeIngredient(Item.COBBLESTONE_BLOCK, 2),
                RecipeIngredient(Item.STICK, 1)
            ),
            requiresCraftingTable = true,
            description = "2 Cobblestone + 1 Stick → Stone Sword"
        )
    )

    fun canCraft(recipe: CraftingRecipe, inventory: List<ItemStack?>, atCraftingTable: Boolean): Boolean {
        if (recipe.requiresCraftingTable && !atCraftingTable) return false
        val counts = mutableMapOf<Item, Int>()
        for (stack in inventory) {
            if (stack != null) {
                counts[stack.item] = (counts[stack.item] ?: 0) + stack.count
            }
        }
        for (ing in recipe.ingredients) {
            if ((counts[ing.item] ?: 0) < ing.count) {
                return false
            }
        }
        return true
    }
}
