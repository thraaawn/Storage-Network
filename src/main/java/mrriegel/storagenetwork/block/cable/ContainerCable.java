package mrriegel.storagenetwork.block.cable;

import mrriegel.storagenetwork.item.ItemUpgrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCable extends Container {
  public TileCable tile;
  protected int sq = 18;

  public ContainerCable(TileCable tile, InventoryPlayer playerInv) {
    this.tile = tile;

    //player inventory
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * sq, 55 + 34 + i * sq));
      }
    }
    //player hotbar
    for (int i = 0; i < 9; ++i) {
      this.addSlotToContainer(new Slot(playerInv, i, 8 + i * sq, 113 + 34));
    }
  }

  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    return playerIn.getDistanceSq(tile.getPos().getX() + 0.5D, tile.getPos().getY() + 0.5D, tile.getPos().getZ() + 0.5D) <= 64.0D;
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
}
