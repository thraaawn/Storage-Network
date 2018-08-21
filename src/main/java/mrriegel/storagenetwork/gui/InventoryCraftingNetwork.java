package mrriegel.storagenetwork.gui;

import java.util.List;
import java.util.Map;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

public class InventoryCraftingNetwork extends InventoryCrafting {

  public InventoryCraftingNetwork(Container eventHandlerIn, Map<Integer, ItemStack> matrix) {
    super(eventHandlerIn, 3, 3);
    for (int i = 0; i < 9; i++) {
      if (matrix.get(i) != null && matrix.get(i).isEmpty() == false)
        this.setInventorySlotContents(i, matrix.get(i));
    }
  }

  public InventoryCraftingNetwork(Container eventHandlerIn, List<ItemStack> matrix) {
    super(eventHandlerIn, 3, 3);
    for (int i = 0; i < 9; i++) {
      if (matrix.get(i) != null && matrix.get(i).isEmpty() == false)
        this.setInventorySlotContents(i, matrix.get(i));
    }
  }

  @Override
  public void setInventorySlotContents(int index, ItemStack stack) {
    super.setInventorySlotContents(index, stack);
  }
}
