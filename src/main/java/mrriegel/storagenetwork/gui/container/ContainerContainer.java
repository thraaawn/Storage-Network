package mrriegel.storagenetwork.gui.container;

import mrriegel.storagenetwork.init.ModItems;
import mrriegel.storagenetwork.tile.TileContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class ContainerContainer extends Container {
	InventoryPlayer playerInv;
	public TileContainer tile;

	public ContainerContainer(TileContainer tile, InventoryPlayer playerInv) {
    System.out.println("new ContainerContainer");
		this.playerInv = playerInv;
		this.tile = tile;
		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(tile, i, 8 + i * 18, 26) {
				@Override
				public boolean isItemValid(ItemStack stack) {
					return stack != null &&!stack.isEmpty() && stack.isItemEqual(new ItemStack(ModItems.template)) && stack.getTagCompound() != null && stack.getTagCompound().getTag("res") != null;
				}

				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					if (getStack() != null && !getStack().isEmpty()){
						getStack().getTagCompound().setLong("machine", ((TileEntity) inventory).getPos().toLong());
					}
				}
			});
		}
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 - 39 + 10 + i * 18));
			}
		}
		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142 - 39 + 10));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn.getDistanceSq(tile.getPos().getX() + 0.5D, tile.getPos().getY() + 0.5D, tile.getPos().getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
    System.out.println("transferStackInSlot ctr plain");
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(slotIndex);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (!itemstack.isItemEqual(new ItemStack(ModItems.template)) || itemstack.getTagCompound().getTag("res") == null)
				return ItemStack.EMPTY;
			if (slotIndex <= 8) {
				if (!this.mergeItemStack(itemstack1, 8, 8 + 37, true))
					return ItemStack.EMPTY;
				slot.onSlotChange(itemstack1, itemstack);
			} else {
				if (!this.mergeItemStack(itemstack1, 0, 9, false))
					return ItemStack.EMPTY;
			}
			if (itemstack1.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}
			slot.onTake(player, itemstack1);
		}

		return itemstack;
	}
}
