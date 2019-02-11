package mrriegel.storagenetwork.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mrriegel.storagenetwork.api.capability.IConnectable;
import mrriegel.storagenetwork.api.data.IItemStackMatcher;
import mrriegel.storagenetwork.api.network.INetworkMaster;
import net.minecraft.item.ItemStack;

/**
 * Do not implement this yourself. You can get access to an instance of this class by implementing the {@link IStorageNetworkPlugin} interface.
 */
public interface IStorageNetworkHelpers {

  /**
   * Get the {@link INetworkMaster} for the given connectable. Use this if you e.g. want to store items in the network.
   *
   * @param connectable
   *          The IConnectable capability instance
   * @return
   */
  @Nullable
  INetworkMaster getTileMasterForConnectable(@Nonnull IConnectable connectable);

  /**
   * Get an itemstack matcher for the given itemstack and rules.
   *
   * @param stack
   *          The stack you want to match against
   * @param ore
   *          Care about the ore dictionary
   * @param nbt
   *          Care about whether or not the meta data is equal
   * @param meta
   *          Does the meta data have to be equal
   * @return
   */
  IItemStackMatcher createItemStackMatcher(ItemStack stack, boolean ore, boolean nbt, boolean meta);
}
