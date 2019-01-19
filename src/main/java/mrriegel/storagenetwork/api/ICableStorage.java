package mrriegel.storagenetwork.api;

import mrriegel.storagenetwork.data.EnumFilterDirection;
import net.minecraft.item.ItemStack;

public interface ICableStorage extends ICable {

  boolean canTransfer(ItemStack stack, EnumFilterDirection way);

  /**
   * Is this inventory currently being used for storage.
   * 
   * By default this should return true,
   * 
   * meaning the network can use it at any time for item transactions.
   * 
   * @return
   */
  boolean isStorageEnabled();


}
