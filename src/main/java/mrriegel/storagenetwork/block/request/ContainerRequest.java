package mrriegel.storagenetwork.block.request;

import java.util.List;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.gui.ContainerNetworkBase;
import mrriegel.storagenetwork.gui.InventoryCraftingNetwork;
import mrriegel.storagenetwork.network.StacksMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.UtilTileEntity;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class ContainerRequest extends ContainerNetworkBase {

  public TileRequest tile;

  public ContainerRequest(final TileRequest tile, final InventoryPlayer playerInv) {
    matrix = new InventoryCraftingNetwork(this, tile.matrix);
    this.tile = tile;
    this.playerInv = playerInv;
    result = new InventoryCraftResult();
    SlotCraftingNetwork slotCraftOutput = new SlotCraftingNetwork(playerInv.player, matrix, result, 0, 101, 128);
    slotCraftOutput.setTileMaster((TileMaster) tile.getWorld().getTileEntity(tile.getMaster()));
    this.addSlotToContainer(slotCraftOutput);
    bindGrid();
    bindPlayerInvo(playerInv);
    bindHotbar();
    this.onCraftMatrixChanged(this.matrix);
  }

  @Override
  public void bindHotbar() {
    //player hotbar
    for (int i = 0; i < 9; ++i) {
      this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 232));
    }
  }

  @Override
  public void onCraftMatrixChanged(IInventory inventoryIn) {
    if (this.recipeLocked) {
      //StorageNetwork.log("recipe locked so onCraftMatrixChanged cancelled");
      return;
    }
    findMatchingRecipe(matrix);
  }

  @Override
  public void slotChanged() {
    //parent is abstract
    //seems to not happen from -shiftclick- crafting
    StorageNetwork.log("[cr] start .slotChanged");
    for (int i = 0; i < 9; i++) {
      tile.matrix.put(i, matrix.getStackInSlot(i));
    }
    StorageNetwork.benchmark("[cr] slotChanged before updateTile");
    UtilTileEntity.updateTile(tile.getWorld(), tile.getPos());
    StorageNetwork.benchmark("[cr] slotChanged End updateTile");
  }

  @Override
  public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex) {
    if (playerIn.world.isRemote) {
      return ItemStack.EMPTY;
    }
    StorageNetwork.log("transfer " + slotIndex);
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(slotIndex);
    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();
      TileMaster tileMaster = this.getTileMaster();
      if (slotIndex == 0) {
        craftShift(playerIn, tileMaster);
        return ItemStack.EMPTY;
      }
      //      else if (slotIndex <= 9) {
      //        if (!this.mergeItemStack(itemstack1, 10, 10 + 36, true)) {
      //          return ItemStack.EMPTY;
      //        }
      //        slot.onSlotChange(itemstack1, itemstack);
      //      }
      else if (tileMaster != null) {
        int rest = tileMaster.insertStack(itemstack1, null, false);
        ItemStack stack = rest == 0 ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(itemstack1, rest);
        slot.putStack(stack);
        detectAndSendChanges();
        List<StackWrapper> list = tileMaster.getStacks();
        PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, tileMaster.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
        if (stack.isEmpty()) {
          return ItemStack.EMPTY;
        }
        slot.onTake(playerIn, itemstack1);
        return ItemStack.EMPTY;
      }
      if (itemstack1.getCount() == 0) {
        slot.putStack(ItemStack.EMPTY);
      }
      else {
        slot.onSlotChanged();
      }
      if (itemstack1.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }
      slot.onTake(playerIn, itemstack1);
    }
    return itemstack;
  }

  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    TileMaster tileMaster = this.getTileMaster();
    if (tileMaster == null) {
      return false;
    }
    if (!tile.getWorld().isRemote && tile.getWorld().getTotalWorldTime() % 40 == 0) {
      List<StackWrapper> list = tileMaster.getStacks();
      PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, tileMaster.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
    }
    return playerIn.getDistanceSq(tile.getPos().getX() + 0.5D, tile.getPos().getY() + 0.5D, tile.getPos().getZ() + 0.5D) <= 64.0D;
  }

  @Override
  public boolean canMergeSlot(ItemStack stack, Slot slot) {
    return slot.inventory != this.result && super.canMergeSlot(stack, slot);
  }

  @Override
  public TileMaster getTileMaster() {
    return (TileMaster) tile.getWorld().getTileEntity(tile.getMaster());
  }
}
