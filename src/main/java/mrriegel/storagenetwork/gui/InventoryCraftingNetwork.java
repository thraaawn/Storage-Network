package mrriegel.storagenetwork.gui;

import java.util.List;
import java.util.Map;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class InventoryCraftingNetwork extends InventoryCrafting {

  /** stupid thing is private with no getter so overwrite */
  private final NonNullList<ItemStack> stackList;
  private final Container eventHandler;
  public boolean skipEvents;

  public InventoryCraftingNetwork(Container eventHandlerIn, int width, int height) {
    super(eventHandlerIn, width, height);
    eventHandler = eventHandlerIn;
    this.stackList = NonNullList.<ItemStack> withSize(3 * 3, ItemStack.EMPTY);
  }

  public InventoryCraftingNetwork(Container eventHandlerIn, Map<Integer, ItemStack> matrix) {
    this(eventHandlerIn, 3, 3);
    skipEvents = true;
    for (int i = 0; i < 9; i++) {
      if (matrix.get(i) != null && matrix.get(i).isEmpty() == false)
        this.setInventorySlotContents(i, matrix.get(i));
    }
    skipEvents = false;
  }

  public InventoryCraftingNetwork(Container eventHandlerIn, List<ItemStack> matrix) {
    this(eventHandlerIn, 3, 3);
    skipEvents = true;
    for (int i = 0; i < 9; i++) {
      if (matrix.get(i) != null && matrix.get(i).isEmpty() == false)
        this.setInventorySlotContents(i, matrix.get(i));
    }
    this.skipEvents = false;
  }

  @Override
  public void setInventorySlotContents(int index, ItemStack stack) {
    this.stackList.set(index, stack);
    if (skipEvents == false) {
      this.eventHandler.onCraftMatrixChanged(this);
    }
  }

  @Override
  public int getSizeInventory() {
    return this.stackList.size();
  }

  @Override
  public boolean isEmpty() {
    for (ItemStack itemstack : this.stackList) {
      if (!itemstack.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ItemStack getStackInSlot(int index) {
    return index >= this.getSizeInventory() ? ItemStack.EMPTY : (ItemStack) this.stackList.get(index);
  }

  @Override
  public ItemStack removeStackFromSlot(int index) {
    return ItemStackHelper.getAndRemove(this.stackList, index);
  }

  @Override
  public ItemStack decrStackSize(int index, int count) {
    ItemStack itemstack = ItemStackHelper.getAndSplit(this.stackList, index, count);
    if (!itemstack.isEmpty()) {
      this.eventHandler.onCraftMatrixChanged(this);
    }
    return itemstack;
  }

  @Override
  public void clear() {
    this.stackList.clear();
  }

  @Override
  public void fillStackedContents(RecipeItemHelper helper) {
    for (ItemStack itemstack : this.stackList) {
      helper.accountStack(itemstack);
    }
  }
}
