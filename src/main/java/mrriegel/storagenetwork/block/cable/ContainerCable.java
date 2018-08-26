package mrriegel.storagenetwork.block.cable;

import mrriegel.storagenetwork.block.AbstractFilterTile;
import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.registry.ModItems;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCable extends Container {

  public static final int UPGRADE_COUNT = 4;
  private TileCable tile;
  private IInventory upgrades;

  public ContainerCable(TileCable tile, InventoryPlayer playerInv) {
    this.setTile(tile);
    upgrades = tile;
    if (tile.isUpgradeable()) {
      for (int ii = 0; ii < UPGRADE_COUNT; ii++) {
        this.addSlotToContainer(new Slot(upgrades, ii, 98 + ii * 18, 6) {

          @Override
          public boolean isItemValid(ItemStack stack) {
            return stack.getItem() == ModItems.upgrade;
          }
        });
      }
    }
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 55 + 34 + i * 18));
      }
    }
    for (int i = 0; i < 9; ++i) {
      this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 113 + 34));
    }
  }

  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    return playerIn.getDistanceSq(getTile().getPos().getX() + 0.5D, getTile().getPos().getY() + 0.5D, getTile().getPos().getZ() + 0.5D) <= 64.0D;
  }

  @Override
  public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
    Slot slot = this.inventorySlots.get(slotIndex);
    //in range [4,39] means its coming FROM inventory
    // [0,3] is the filter list
    if (slot != null && slot.getHasStack()) {
      ItemStack stackInSlot = slot.getStack();
      if (stackInSlot.getItem() instanceof ItemUpgrade) {
        if (4 <= slotIndex && slotIndex <= 39) {
          //FROM inventory to upgrade slots
          if (!this.mergeItemStack(stackInSlot, 0, 4, true)) {
            return ItemStack.EMPTY;
          }
        }
        else if (0 <= slotIndex && slotIndex <= 3) {
          //FROM upgrade slots TO inventory
          if (!this.mergeItemStack(stackInSlot, 4, 40, true)) {
            return ItemStack.EMPTY;
          }
        }
      }
    }
    return ItemStack.EMPTY;
  }

  public boolean isInFilter(StackWrapper stack) {
    for (int i = 0; i < AbstractFilterTile.FILTER_SIZE; i++) {
      if (getTile().getFilter().get(i) != null && getTile().getFilter().get(i).getStack().isItemEqual(stack.getStack())) {
        return true;
      }
    }
    return false;
  }

  public TileCable getTile() {
    return tile;
  }

  public void setTile(TileCable tile) {
    this.tile = tile;
  }
}
