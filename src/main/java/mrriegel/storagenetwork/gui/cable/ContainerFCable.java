package mrriegel.storagenetwork.gui.cable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import mrriegel.storagenetwork.helper.StackWrapper;
import mrriegel.storagenetwork.helper.Util;
import mrriegel.storagenetwork.init.ModItems;
import mrriegel.storagenetwork.tile.AbstractFilterTile;
import mrriegel.storagenetwork.tile.TileKabel;
import mrriegel.storagenetwork.tile.TileKabel.Kind;
import mrriegel.storagenetwork.tile.TileMaster;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class ContainerFCable extends Container {
	InventoryPlayer playerInv;
	public AbstractFilterTile tile;
	private Map<Integer, StackWrapper> filter;
	IInventory upgrades;

	public ContainerFCable(AbstractFilterTile tile, InventoryPlayer playerInv) {
		this.playerInv = playerInv;
		this.tile = tile;
		filter = new HashMap<Integer, StackWrapper>(tile.getFilter());
		upgrades = new InventoryBasic("upgrades", false, 4) {
			@Override
			public int getInventoryStackLimit() {
				return 4;
			}
		};
		if (tile instanceof TileKabel && ((TileKabel) tile).isUpgradeable()) {
			for (int i = 0; i < ((TileKabel) tile).getUpgrades().size(); i++) {
				upgrades.setInventorySlotContents(i, ((TileKabel) tile).getUpgrades().get(i));
			}
			for (int ii = 0; ii < 4; ii++) {
				this.addSlotToContainer(new Slot(upgrades, ii, 98 + ii * 18, 6) {
					@Override
					public boolean isItemValid(ItemStack stack) {
						return stack.getItem() == ModItems.upgrade && ((getStack() != null && getStack().getItemDamage() == stack.getItemDamage()) || !in(stack.getItemDamage()));
					}

					@Override
					public void onSlotChanged() {
						slotChanged();
						super.onSlotChanged();
					}

					private boolean in(int meta) {
						for (int i = 0; i < upgrades.getSizeInventory(); i++) {
							if (upgrades.getStackInSlot(i) != null && upgrades.getStackInSlot(i).getItemDamage() == meta)
								return true;
						}
						return false;
					}

				});
			}
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
		return true;
//		return tile != null && tile.getMaster() != null && tile.getWorld().getTileEntity(tile.getMaster()) instanceof TileMaster;
	}

	public void slotChanged() {
		tile.setFilter(filter);
		if (tile instanceof TileKabel) {
			((TileKabel) tile).setUpgrades(Arrays.<ItemStack> asList(null, null, null, null));
			for (int i = 0; i < upgrades.getSizeInventory(); i++)
				((TileKabel) tile).getUpgrades().set(i, upgrades.getStackInSlot(i));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		Slot slot = this.inventorySlots.get(slotIndex);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			if (itemstack1 == null || Util.getFluid(itemstack1) == null)
				return null;
			for (int i = 0; i < 9; i++) {
				if (!(tile instanceof TileKabel) || ((TileKabel) tile).getKind() == Kind.fstorageKabel)
					i = 4;
				if (filter.get(i) == null && !in(new StackWrapper(itemstack1, 1))) {
					filter.put(i, new StackWrapper(itemstack1.copy(), 1));
					slotChanged();
					break;
				}
				if (!(tile instanceof TileKabel) || ((TileKabel) tile).getKind() == Kind.fstorageKabel)
					break;
			}
		}
		return null;
	}

	boolean in(StackWrapper stack) {
		for (int i = 0; i < 9; i++) {
			if (filter.get(i) != null && sameFluid(filter.get(i).getStack(), stack.getStack()))
				return true;
		}
		return false;
	}

	private boolean sameFluid(ItemStack s1, ItemStack s2) {
		FluidStack f1 = Util.getFluid(s1);
		FluidStack f2 = Util.getFluid(s2);
		// System.out.println(f1.getFluid() +" "+ f2.getFluid());
		return f1.getFluid() == f2.getFluid();
	}

	public Map<Integer, StackWrapper> getFilter() {
		return filter;
	}

	public void setFilter(Map<Integer, StackWrapper> filter) {
		this.filter = filter;
	}

}