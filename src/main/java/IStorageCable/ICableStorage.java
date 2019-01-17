package IStorageCable;

import mrriegel.storagenetwork.data.EnumFilterDirection;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public interface ICableStorage {

  int getPriority();

  BlockPos getConnectedInventory();

  IItemHandler getInventory();

  EnumFacing getInventoryFace();

  EnumFilterDirection getTransferDirection();

  /**
   * position of this tile entity
   * 
   * @return
   */
  BlockPos getPos();
}
