package mrriegel.storagenetwork.block.control;

import java.util.ArrayList;
import java.util.List;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.gui.ContainerNetworkBase;
import mrriegel.storagenetwork.gui.InventoryCraftingNetwork;
import mrriegel.storagenetwork.network.StackRefreshClientMessage;
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

public class ContainerControl extends ContainerNetworkBase {

  private TileControl tileRequest;

  public ContainerControl(final TileControl tile, final InventoryPlayer playerInv) {
    matrix = new InventoryCraftingNetwork(this, tile.matrix);
    this.setTileRequest(tile);
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
    for (int i = 0; i < 9; i++) {
      getTileRequest().matrix.put(i, matrix.getStackInSlot(i));
    }
    UtilTileEntity.updateTile(getTileRequest().getWorld(), getTileRequest().getPos());
  }

  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    TileMaster tileMaster = this.getTileMaster();
    if (tileMaster == null) {
      return false;
    }
    if (!getTileRequest().getWorld().isRemote && getTileRequest().getWorld().getTotalWorldTime() % 40 == 0) {
      List<StackWrapper> list = tileMaster.getStacks();
      PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<StackWrapper>()), (EntityPlayerMP) playerIn);
    }
    return playerIn.getDistanceSq(getTileRequest().getPos().getX() + 0.5D, getTileRequest().getPos().getY() + 0.5D, getTileRequest().getPos().getZ() + 0.5D) <= 64.0D;
  }

  @Override
  public boolean canMergeSlot(ItemStack stack, Slot slot) {
    return slot.inventory != this.result && super.canMergeSlot(stack, slot);
  }

  @Override
  public TileMaster getTileMaster() {
    return (TileMaster) getTileRequest().getWorld().getTileEntity(getTileRequest().getMaster());
  }

  public TileControl getTileRequest() {
    return tileRequest;
  }

  public void setTileRequest(TileControl tileRequest) {
    this.tileRequest = tileRequest;
  }

  @Override
  public boolean isRequest() {
    return true;
  }
}
