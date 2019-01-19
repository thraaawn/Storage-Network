package mrriegel.storagenetwork.api;

import mrriegel.storagenetwork.data.EnumFilterDirection;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public interface ICableStorage extends ICable {



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
