package mrriegel.storagenetwork.gui;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface IStorageInventory {

  void setStacks(List<ItemStack> stacks);

  void setCraftableStacks(List<ItemStack> stacks);
}
