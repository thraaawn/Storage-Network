package mrriegel.storagenetwork.api.capability;

import java.util.Collections;
import java.util.List;
import mrriegel.storagenetwork.api.data.DimPos;
import mrriegel.storagenetwork.api.data.EnumStorageDirection;
import mrriegel.storagenetwork.api.data.IItemStackMatcher;
import mrriegel.storagenetwork.api.network.INetworkMaster;
import net.minecraft.item.ItemStack;

/**
 * Only expose this capability if you want your cable/block to auto-export and import blocks controlled by the networks master. You could quite as well just expose {@link IConnectable} and do the
 * exporting/importing in your own update() method.
 *
 * If you indeed want to add another exporting/importing cable in the style of the integrated ones, this might be for you. In all other cases, this is probably not what you want.
 */
public interface IConnectableItemAutoIO {

  /**
   * Return either IN or OUT here, but not BOTH. If you return BOTH expect weird things to happen.
   *
   * View the EnumStorageDirection from the networks perspective, i.e.: - OUT means you are storing items in your storage based on the given auto export list. - IN means you are extracting items from
   * your storage in regular intervals.
   *
   * @return
   */
  EnumStorageDirection ioDirection();

  /**
   * This is called on your capability every time the network tries to insert a stack into your storage.
   *
   * If your ioDirection is set to OUT, this should never get called, unless another malicious mod is doing it.
   *
   * @param stack
   *          The stack being inserted into your storage
   * @param simulate
   *          Whether or not this is just a simulation
   * @return The remainder of the stack if not all of it fit into your storage
   */
  ItemStack insertStack(ItemStack stack, boolean simulate);

  /**
   * This is called whenever its your storages turn to import another item. Use the first used slot for this if you have a slot based inventory. If its not a simulation actually remove the stack from
   * your storage!
   *
   * Apply your own transfer rate here!
   *
   * If your ioDirection is set to IN, this should never get called, unless another malicious mod is doing it.
   *
   * @param size
   *          The size of the stack that should be extracted
   * @param simulate
   *          Whether or not this is just a simulation
   * @return The stack that has been requested, if you have it
   */
  ItemStack extractNextStack(int size, boolean simulate);

  /**
   * Storages with a higher priority (== lower number) are processed first. You probably want to add a way to configure the priority of your storage.
   *
   * @return Return the priority here
   */
  int getPriority();

  /**
   * Get transfer rate from 0-64. This is literally the amount of items that can be transferred per operation.
   *
   * @return max stacksize to transfer per operation
   */
  int getTransferRate();

  /**
   * Called every tick to see if an operation should be processed now, i.e. this can be used to add cooldown times or disable operations via redstone signal.
   *
   * @param connectablePos
   *          The position of your block, including the world
   * @param master
   *          The network master. Use this to e.g. query amount of items.
   *
   * @return Whether or not this IConnectableLink should be processed this tick.
   */
  boolean runNow(DimPos connectablePos, INetworkMaster master);

  /**
   * If this block is used with an ioDirection of OUT and has its getSupportedTransferDirection set to OUT, then this list will be consolidated by the master and available items in the network
   * matching the {@link IItemStackMatcher}s in the list will be exported via the canTransfer() and transfer() methods above.
   *
   * In other words: - Only implement this if you are making a master-controlled export cable (you shouldnt)
   *
   * @return
   */
  default List<IItemStackMatcher> getAutoExportList() {
    return Collections.emptyList();
  }
}
