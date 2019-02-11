package mrriegel.storagenetwork.api.data;

import javax.annotation.Nonnull;
import mrriegel.storagenetwork.api.IStorageNetworkHelpers;
import net.minecraft.item.ItemStack;

/**
 * Do not implement this interface yourself, but get instances from the {@link IStorageNetworkHelpers} you get passed on your Plugin initialization.
 *
 * You also should usually not need to call any of the given methods, since you are usually just passing instances of this to other api methods.
 */
public interface IItemStackMatcher {

  /**
   * Returns the stack stored in the matcher.
   *
   * @return
   */
  ItemStack getStack();

  /**
   * Matches the given rules of the master against the given stack and returns whether it matches.
   *
   * @param stack
   * @return
   */
  boolean match(@Nonnull ItemStack stack);
}
