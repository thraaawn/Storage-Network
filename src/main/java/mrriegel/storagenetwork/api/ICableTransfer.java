package mrriegel.storagenetwork.api;

import mrriegel.storagenetwork.data.EnumFilterDirection;
import net.minecraft.item.ItemStack;

public interface ICableTransfer extends ICable {

  /**
   * Called every tick to see if an operation should be processed now
   * 
   * @return
   */
  boolean runNow();

  /**
   * get transfer rate from 0-64
   * 
   * @return
   */
  int getTransferRate();

  /**
   * Can a transfer be done in this direction with this stack.
   * 
   * @param stack
   * @param way
   * @return
   */
  boolean canTransfer(ItemStack stack, EnumFilterDirection way);
}
