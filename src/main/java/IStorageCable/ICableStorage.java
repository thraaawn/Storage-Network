package IStorageCable;

import mrriegel.storagenetwork.data.EnumFilterDirection;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public interface ICableStorage extends IHasNetworkPriority {

  BlockPos getConnectedInventory();

  IItemHandler getInventory();

  EnumFacing getInventoryFace();

  EnumFilterDirection getTransferDirection();

  boolean canTransfer(ItemStack stack, EnumFilterDirection way);

  /**
   * position of this tile entity
   * 
   * @return
   */
  BlockPos getPos();
}
