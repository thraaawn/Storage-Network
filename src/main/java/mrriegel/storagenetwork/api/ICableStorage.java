package mrriegel.storagenetwork.api;

import mrriegel.storagenetwork.data.EnumFilterDirection;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface ICableStorage extends ICable {



  EnumFacing getInventoryFace();

  EnumFilterDirection getTransferDirection();

  boolean canTransfer(ItemStack stack, EnumFilterDirection way);


}
