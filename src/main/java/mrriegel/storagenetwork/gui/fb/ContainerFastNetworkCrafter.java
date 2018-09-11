package mrriegel.storagenetwork.gui.fb;

import java.util.ArrayList;
import java.util.List;

import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.gui.IStorageContainer;
import mrriegel.storagenetwork.network.StackRefreshClientMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.data.FilterItem;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import shadows.fastbench.gui.ContainerFastBench;
import shadows.fastbench.gui.SlotCraftingSucks;

public abstract class ContainerFastNetworkCrafter extends ContainerFastBench implements IStorageContainer {

	protected boolean forceSync = true;
	protected final World world;
	protected final EntityPlayer player;

	public ContainerFastNetworkCrafter(EntityPlayer player, World world, BlockPos pos) {
		super(player, world, pos);
		this.world = world;
		this.player = player;
	}

	public abstract TileMaster getTileMaster();

	public abstract void bindHotbar(EntityPlayer player);

	protected void bindPlayerInvo(final InventoryPlayer playerInv) {
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
			}
		}
	}

	protected void bindGrid() {
		int index = 0;
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				this.addSlotToContainer(new Slot(this.craftMatrix, index++, 8 + j * 18, 110 + i * 18));
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		if (world.isRemote) return ItemStack.EMPTY;
		ItemStack slotCopy = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			slotCopy = slotStack.copy();
			TileMaster tileMaster = this.getTileMaster();
			if (index == 0) {
				return super.transferStackInSlot(player, index);
			} else if (tileMaster != null) {
				int rest = tileMaster.insertStack(slotStack, null, false);
				ItemStack stack = rest == 0 ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(slotStack, rest);
				slot.putStack(stack);
				detectAndSendChanges();
				List<StackWrapper> list = tileMaster.getStacks();
				PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<>()), (EntityPlayerMP) player);
				if (stack.isEmpty()) return ItemStack.EMPTY;
				slot.onTake(player, slotStack);
				return ItemStack.EMPTY;
			}
			slot.onSlotChanged();
			if (slotStack.getCount() == slotCopy.getCount()) return ItemStack.EMPTY;
			slot.onTake(player, slotStack);
		}
		return super.transferStackInSlot(player, index);
	}

	@Override
	public InventoryCrafting getCraftMatrix() {
		return craftMatrix;
	}

	@Override
	public void slotChanged() {
	}

	protected final class SlotCraftingNetwork extends SlotCraftingSucks {

		protected TileMaster tileMaster;
		ItemStack[] lastItems;
		IRecipe lastLastRecipe;

		public SlotCraftingNetwork(EntityPlayer player, InventoryCrafting matrix, InventoryCraftResult result, int index, int x, int y) {
			super(ContainerFastNetworkCrafter.this, player, matrix, result, index, x, y);
		}

		@Override
		public void onCrafting(ItemStack stack) {
			super.onCrafting(stack);
		}

		@Override
		public ItemStack onTake(EntityPlayer player, ItemStack stack) {
			if (!world.isRemote) {
				if (ContainerFastNetworkCrafter.this.lastRecipe != lastLastRecipe) {
					lastLastRecipe = ContainerFastNetworkCrafter.this.lastRecipe;
					lastItems = new ItemStack[9];
					for (int i = 0; i < 9; i++) {
						lastItems[i] = this.craftMatrix.getStackInSlot(i).copy();
					}
				}

				ItemStack take = super.onTake(player, stack);

				for (int i = 0; i < 9; i++) {
					if (craftMatrix.getStackInSlot(i).isEmpty() && getTileMaster() != null) {
						ItemStack cached = lastItems[i];
						if (!cached.isEmpty()) this.craftMatrix.stackList.set(i, getTileMaster().request(new FilterItem(cached, true, false, false), 1, false));
					}
				}

				detectAndSendChanges();
				ContainerFastNetworkCrafter.this.onCraftMatrixChanged(ContainerFastNetworkCrafter.this.craftMatrix);
				ContainerFastNetworkCrafter.this.forceSync = true;
				return take;
			} else return super.onTake(player, stack);
		}

		public TileMaster getTileMaster() {
			return tileMaster;
		}

		public void setTileMaster(TileMaster tileMaster) {
			this.tileMaster = tileMaster;
		}
	}
}
