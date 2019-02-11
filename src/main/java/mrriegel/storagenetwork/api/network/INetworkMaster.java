package mrriegel.storagenetwork.api.network;

import java.util.List;
import mrriegel.storagenetwork.api.data.IItemStackMatcher;
import net.minecraft.item.ItemStack;

/**
 * Do not implement this yourself. It is implemented by the TileMaster entity that controls the network.
 *
 * You can use this interface to query what stacks are in the network and to insert/extract items from it.
 */
public interface INetworkMaster {

  /**
   * Get a list of all stacks stored in the network. Note that this list can get quite large if networks get large and have lots of items stored. It might also get expensive to calculate in the first
   * place.
   *
   * Use it sparingly and cache results if you can. If you are looking for a specify item you probably want to use the getAmount method!
   *
   * @return A list containing all stacks in the network.
   */
  List<ItemStack> getStacks();

  /**
   * Insert an itemstack into the network. This tries to insert the stack into any of the other linked inventories. It tries to insert the same stack into the same storage again before trying another
   * storage. Returns the number of things moved out of the stack.
   *
   * @param stack
   *          The stack you want to insert into the network
   * @param simulate
   *          Whether this is just a simulation.
   * @return count of remaining leftover, not count moved
   */
  int insertStack(ItemStack stack, boolean simulate);

  /**
   * Request an itemstack from the network. You can use also use this to query the size of a stack by only simulating and passing in a very large size, a convenience default method for this is
   * included as well: getAmount.
   *
   * @param fil
   * @param size
   * @param simulate
   * @return
   */
  ItemStack request(IItemStackMatcher fil, final int size, boolean simulate);

  /**
   * Clear import history, i.e. make the master forget which inventories are preferred for specific stacks because it put them there earlier. This is necessary mostly when changing priorities of
   * inventories, but there might be other cases where it is necessary.
   */
  void clearCache();

  /**
   * Convenience wrapper around request to get the total amount of items of a specific kind in the network.
   *
   * @param matcher
   *          The rules used to search through the network
   * @return
   */
  default ItemStack getAmount(IItemStackMatcher matcher) {
    return request(matcher, Integer.MAX_VALUE, true);
  }
}
