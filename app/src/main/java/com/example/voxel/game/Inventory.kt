package com.example.voxel.game

/**
 * Manages player inventory slots (9 hotbar + 27 main = 36 slots)
 * and chest container interactions.
 */
class Inventory(val totalSlots: Int = 36) {

    val slots: Array<ItemStack?> = Array(totalSlots) { null }
    var selectedHotbarIndex: Int = 0

    val currentHeldItem: ItemStack?
        get() = slots[selectedHotbarIndex]

    fun getSlot(index: Int): ItemStack? {
        if (index in 0 until totalSlots) return slots[index]
        return null
    }

    fun setSlot(index: Int, stack: ItemStack?) {
        if (index in 0 until totalSlots) {
            slots[index] = stack
        }
    }

    /**
     * Adds an item stack into the inventory.
     * Returns true if fully added, false if partially/not added.
     */
    fun addItem(item: Item, amount: Int): Boolean {
        var remaining = amount
        // 1. Stack into existing slots
        for (i in 0 until totalSlots) {
            val current = slots[i]
            if (current != null && current.item == item && current.count < item.maxStack) {
                val canTake = item.maxStack - current.count
                val take = minOf(canTake, remaining)
                current.count += take
                remaining -= take
                if (remaining <= 0) return true
            }
        }
        // 2. Put into empty slots
        for (i in 0 until totalSlots) {
            if (slots[i] == null) {
                val take = minOf(item.maxStack, remaining)
                slots[i] = ItemStack(item, take)
                remaining -= take
                if (remaining <= 0) return true
            }
        }
        return remaining == 0
    }

    /**
     * Deducts item count from inventory.
     */
    fun removeItem(item: Item, amount: Int): Boolean {
        var needed = amount
        for (i in 0 until totalSlots) {
            val current = slots[i]
            if (current != null && current.item == item) {
                if (current.count > needed) {
                    current.count -= needed
                    needed = 0
                    break
                } else {
                    needed -= current.count
                    slots[i] = null
                }
            }
        }
        return needed == 0
    }

    /**
     * Decrements the currently held item by 1 (e.g. after placing block).
     */
    fun consumeHeldItem(): Boolean {
        val current = slots[selectedHotbarIndex] ?: return false
        current.count--
        if (current.count <= 0) {
            slots[selectedHotbarIndex] = null
        }
        return true
    }

    fun swapSlots(from: Int, to: Int) {
        if (from !in 0 until totalSlots || to !in 0 until totalSlots) return
        val temp = slots[from]
        slots[from] = slots[to]
        slots[to] = temp
    }

    fun splitStack(from: Int, to: Int) {
        if (from !in 0 until totalSlots || to !in 0 until totalSlots) return
        val source = slots[from] ?: return
        val dest = slots[to]
        if (dest == null) {
            val half = (source.count + 1) / 2
            val remaining = source.count - half
            slots[from] = if (remaining > 0) ItemStack(source.item, remaining) else null
            slots[to] = ItemStack(source.item, half)
        } else if (dest.item == source.item && dest.count < dest.item.maxStack) {
            val half = (source.count + 1) / 2
            val space = dest.item.maxStack - dest.count
            val move = minOf(half, space)
            dest.count += move
            source.count -= move
            if (source.count <= 0) slots[from] = null
        }
    }

    fun toList(): List<ItemStack?> = slots.toList()

    fun clear() {
        for (i in 0 until totalSlots) {
            slots[i] = null
        }
    }
}
