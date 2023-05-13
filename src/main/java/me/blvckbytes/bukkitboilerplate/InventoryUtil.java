/*
 * MIT License
 *
 * Copyright (c) 2023 BlvckBytes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.blvckbytes.bukkitboilerplate;

import me.blvckbytes.utilitytypes.Tuple;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class InventoryUtil {

  //=========================================================================//
  //                                   API                                   //
  //=========================================================================//

  public static final int[] EMPTY_SLOT_MASK = new int[0];

  /**
   * Gives a player as much as possible of the provided item stack and
   * drops the remaining items at their location.
   * @param target Target player
   * @param stack Items to hand out
   * @return Number of dropped items
   */
  public int giveItemsOrDrop(Player target, ItemStack stack) {
    // Add as much as possible into the inventory
    Tuple<Integer, @Nullable Runnable> result = prepareAddingToInventory(fromBukkit(target.getInventory()), stack, EMPTY_SLOT_MASK, false);

    if (result.b != null)
      result.b.run();

    int remaining = result.a;
    int dropped = remaining;

    // Done, everything fit
    if (remaining == 0)
      return 0;

    // Could not get the world, all further iterations make no sense
    World w = target.getLocation().getWorld();
    if (w == null)
      return 0;

    // Drop all remaining items
    int stackSize = stack.getMaxStackSize();
    while (remaining > 0) {
      ItemStack items = stack.clone();
      items.setAmount(Math.min(remaining, stackSize));
      w.dropItem(target.getEyeLocation(), items);
      remaining -= items.getAmount();
    }

    return dropped;
  }

  /**
   * Convert a bukkit inventory to the internally used abstraction
   * @param inv Bukkit inventory
   * @return Abstraction wrapper
   */
  public IInventory<Inventory> fromBukkit(Inventory inv) {
    return new IInventory<Inventory>() {

      @Override
      public @Nullable ItemStack get(int slot) {
        try {
          ItemStack ret = inv.getItem(slot);

          if (ret == null || ret.getAmount() == 0)
            return null;

          return ret;
        } catch (Exception e) {
          return null;
        }
      }

      @Override
      public void set(int slot, @Nullable ItemStack item) {
        try {
          inv.setItem(slot, item);
        } catch (Exception ignored) {}
      }

      @Override
      public int getSize() {
        return inv.getStorageContents().length;
      }

      @Override
      public Inventory getHandle() {
        return inv;
      }
    };
  }

  /**
   * Convert a plain array to the internally used abstraction
   * @param array ItemStack array
   * @return Abstraction wrapper
   */
  public IInventory<ItemStack[]> fromArray(ItemStack[] array) {
    return new IInventory<ItemStack[]>() {

      @Override
      public @Nullable ItemStack get(int slot) {
        try {
          ItemStack ret = array[slot];

          if (ret.getAmount() == 0)
            return null;

          return ret;
        } catch (Exception e) {
          return null;
        }
      }

      @Override
      public void set(int slot, @Nullable ItemStack item) {
        try {
          array[slot] = item;
        } catch (Exception ignored) {}
      }

      @Override
      public int getSize() {
        return array.length;
      }

      @Override
      public ItemStack[] getHandle() {
        return array;
      }
    };
  }

  /**
   * Take and return the first matching ItemStack from an inventory
   * @param target Target inventory
   * @param predicate Matching predicate (returns how many of those items to remove, return all to null out, 0 to skip the item)
   * @param slotMask Optional (positive) mask of slots (empty means ignored)
   * @return First matching item which has been removed or empty if no item matched
   */
  public Optional<ItemStack> takeFirstMatching(IInventory<?> target, Function<ItemStack, Integer> predicate, int[] slotMask) {
    // Iterate all slots
    int[] slots = slotMask.length == 0 ? IntStream.range(0, target.getSize()).toArray() : slotMask;
    for (int i : slots) {
      ItemStack stack = target.get(i);

      // Empty slot
      if (stack == null)
        continue;

      // Item doesn't match
      int numRemove = predicate.apply(stack);
      if (numRemove == 0)
        continue;

      // Take all and null the slot
      if (stack.getAmount() <= numRemove) {
        target.set(i, null);
        return Optional.of(stack);
      }

      // Subtract requested amount
      stack.setAmount(stack.getAmount() - numRemove);
      ItemStack ret = new ItemStack(stack);
      ret.setAmount(numRemove);
      return Optional.of(ret);
    }

    return Optional.empty();
  }

  /**
   * Prepares adding an ItemStack to an inventory and responds with the number of
   * items that didn't fit as well as an executable to dispatch this action
   * @param target Target inventory
   * @param item Item to add
   * @param slotMask Optional (positive) mask of slots (empty means ignored)
   * @param allOrNothing Whether all items need to fit, if true, it's either all or none
   * @return Tuple of the number of items that didn't fit and the optional runnable (only returned on
   *         success), that will dispatch adding the items
   */
  public Tuple<Integer, @Nullable Runnable> prepareAddingToInventory(IInventory<?> target, ItemStack item, int[] slotMask, boolean allOrNothing) {
    // This number will be decremented as space is found along the way
    int remaining = item.getAmount();
    int stackSize = item.getType().getMaxStackSize();

    // At first, only store planned partitions using the format <Slot, Amount>
    // and execute them all at once at the end, to have a transaction-like behavior
    Map<Integer, Integer> partitions = new HashMap<>();
    List<Integer> vacantSlots = new ArrayList<>();

    // Iterate all slots
    int[] slots = slotMask.length == 0 ? IntStream.range(0, target.getSize()).toArray() : slotMask;
    for (int slot : slots) {
      ItemStack stack = target.get(slot);

      // Done, no more items remaining
      if (remaining < 0)
        break;

      // Completely vacant slot
      if (stack == null || stack.getType() == Material.AIR) {
        vacantSlots.add(slot);
        continue;
      }

      // Incompatible stacks, ignore
      if (!stack.isSimilar(item))
        continue;

      // Compatible stack but no more room left
      int usable = Math.max(0, stackSize - stack.getAmount());
      if (usable == 0)
        continue;

      // Add the last few remaining items, done
      if (usable >= remaining) {
        partitions.put(slot, stack.getAmount() + remaining);
        remaining = 0;
        break;
      }

      // Set to a full stack and subtract the delta from remaining
      partitions.put(slot, stackSize);
      remaining -= usable;
    }

    // If there are still items remaining, start using vacant slots
    if (remaining > 0 && vacantSlots.size() > 0) {
      for (int vacantSlot : vacantSlots) {
        if (remaining <= 0)
          break;

        // Set as many items as possible or as many as remain
        int amount = Math.min(remaining, stackSize);
        partitions.put(vacantSlot, amount);
        remaining -= amount;
      }
    }

    // Requested all or nothing, didn't fit completely
    if (allOrNothing && remaining > 0)
      return new Tuple<>(item.getAmount(), null);

    return new Tuple<>(remaining, () -> {
      // Apply partitions to inventory
      for (Map.Entry<Integer, Integer> partition : partitions.entrySet()) {
        int slot = partition.getKey();
        int amount = partition.getValue();
        ItemStack currentItem = target.get(slot);

        // Slot empty, create new item
        if (currentItem == null) {
          currentItem = item.clone();
          currentItem.setAmount(amount);
          target.set(slot, currentItem);
          continue;
        }

        // Update existing slot
        currentItem.setAmount(amount);
      }
    });
  }
}
