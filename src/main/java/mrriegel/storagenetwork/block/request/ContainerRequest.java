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
import net.minecraft.inventory.InventoryCrafting;
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
    int index = 0;
    //3x3 crafting grid
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        this.addSlotToContainer(new Slot(matrix, index++, 8 + j * 18, 110 + i * 18));
      }
    }
    //player inventory
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
      }
    }
    //player hotbar
    for (int i = 0; i < 9; ++i) {
      this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 232));
    }
    this.onCraftMatrixChanged(this.matrix);
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
  public void onContainerClosed(EntityPlayer playerIn) {
    slotChanged();
    super.onContainerClosed(playerIn);
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
      if (slotIndex == 0) {
        craftShift(playerIn, (TileMaster) this.tile.getWorld().getTileEntity(this.tile.getMaster()));
        return ItemStack.EMPTY;
      }
      if (slotIndex <= 9) {
        if (!this.mergeItemStack(itemstack1, 10, 10 + 36, true)) {
          return ItemStack.EMPTY;
        }
        slot.onSlotChange(itemstack1, itemstack);
      }
      else {
        TileMaster tile = (TileMaster) this.tile.getWorld().getTileEntity(this.tile.getMaster());
        if (tile != null) {
          int rest = tile.insertStack(itemstack1, null, false);
          ItemStack stack = rest == 0 ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(itemstack1, rest);
          slot.putStack(stack);
          detectAndSendChanges();
          List<StackWrapper> list = tile.getStacks();
          PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, tile.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
          if (stack.isEmpty()) {
            return ItemStack.EMPTY;
          }
          slot.onTake(playerIn, itemstack1);
          return ItemStack.EMPTY;
        }
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
    if (tile == null || tile.getMaster() == null || !(tile.getWorld().getTileEntity(tile.getMaster()) instanceof TileMaster))
      return false;
    TileMaster t = (TileMaster) tile.getWorld().getTileEntity(tile.getMaster());
    if (!tile.getWorld().isRemote && tile.getWorld().getTotalWorldTime() % 40 == 0) {
      List<StackWrapper> list = t.getStacks();
      PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, t.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
    }
    return playerIn.getDistanceSq(tile.getPos().getX() + 0.5D, tile.getPos().getY() + 0.5D, tile.getPos().getZ() + 0.5D) <= 64.0D;
  }

  @Override
  public boolean canMergeSlot(ItemStack stack, Slot slot) {
    return slot.inventory != this.result && super.canMergeSlot(stack, slot);
  }

  @Override
  public InventoryCrafting getCraftMatrix() {
    return this.matrix;
  }

  @Override
  public TileMaster getTileMaster() {
    return (TileMaster) tile.getWorld().getTileEntity(tile.getMaster());
  }
}
