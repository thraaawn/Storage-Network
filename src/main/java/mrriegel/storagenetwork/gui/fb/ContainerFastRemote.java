package mrriegel.storagenetwork.gui.fb;

import java.util.ArrayList;

import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.item.remote.ItemRemote;
import mrriegel.storagenetwork.network.StackRefreshClientMessage;
import mrriegel.storagenetwork.registry.ModItems;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.NBTHelper;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerFastRemote extends ContainerFastNetworkCrafter {

	private ItemStack remoteItemStack;

	public ContainerFastRemote(EntityPlayer player, World world, BlockPos pos) {
		super(player, world, pos);
		remoteItemStack = player.inventory.getCurrentItem();
		this.inventorySlots.clear();
		this.inventoryItemStacks.clear();
		for (int i = 0; i < 9; i++) {
			if (i != 8) this.craftMatrix.stackList.set(i, NBTHelper.getItemStack(remoteItemStack, "c" + i));
			else this.craftMatrix.setInventorySlotContents(i, NBTHelper.getItemStack(remoteItemStack, "c" + i));
		}

		SlotCraftingNetwork slotCraftOutput = new SlotCraftingNetwork(player, craftMatrix, craftResult, 0, 101, 128);
		slotCraftOutput.setTileMaster(this.getTileMaster());
		this.addSlotToContainer(slotCraftOutput);
		bindGrid();
		bindPlayerInvo(player.inventory);
		bindHotbar(player);
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		TileMaster tileMaster = this.getTileMaster();
		if (tileMaster == null) { return false; }
		if (!playerIn.world.isRemote && (forceSync || playerIn.world.getTotalWorldTime() % 40 == 0)) {
			forceSync = false;
			PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(tileMaster.getStacks(), new ArrayList<StackWrapper>()), (EntityPlayerMP) playerIn);
		}
		return playerIn.inventory.getCurrentItem() != null && playerIn.inventory.getCurrentItem().getItem() == ModItems.remote;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		for (int i = 0; i < 9; i++) {
			NBTHelper.setItemStack(remoteItemStack, "c" + i, craftMatrix.getStackInSlot(i));
		}
	}

	@Override
	public TileMaster getTileMaster() {
		return ItemRemote.getTile(remoteItemStack);
	}

	@Override
	public void bindHotbar(EntityPlayer player) {
		for (int i = 0; i < 9; ++i) {
			if (i == player.inventory.currentItem) this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 232) {

				@Override
				public boolean isItemValid(ItemStack stack) {
					return false;
				}

				@Override
				public boolean canTakeStack(EntityPlayer playerIn) {
					return false;
				}
			});
			else this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 232));
		}
	}

	@Override
	public boolean isRequest() {
		return false;
	}
}