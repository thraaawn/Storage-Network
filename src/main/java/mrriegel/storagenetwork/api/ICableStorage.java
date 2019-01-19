package mrriegel.storagenetwork.api;

import mrriegel.storagenetwork.data.EnumFilterDirection;
import net.minecraft.item.ItemStack;

public interface ICableStorage extends ICable {

  boolean canTransfer(ItemStack stack, EnumFilterDirection way);


}
