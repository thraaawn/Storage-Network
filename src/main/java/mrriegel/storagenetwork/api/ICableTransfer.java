package mrriegel.storagenetwork.api;

import mrriegel.storagenetwork.data.EnumFilterDirection;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public interface ICableTransfer extends IHasNetworkPriority {

  /**
   * Location of connected tile entity
   * 
   * @return
   */
  BlockPos getConnectedInventory();

  /**
   * Connected Inventory
   * 
   * @return
   */
  IItemHandler getInventory();

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
