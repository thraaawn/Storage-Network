package mrriegel.storagenetwork.block.request;

import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.gui.ContainerNetworkBase;
import mrriegel.storagenetwork.gui.InventoryCraftingNetwork;
import mrriegel.storagenetwork.network.StackRefreshClientMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ContainerRequest extends ContainerNetworkBase {

  private TileRequest tileRequest;

  public ContainerRequest(final TileRequest tile, final InventoryPlayer playerInv) {
    matrix = new InventoryCraftingNetwork(this, tile.matrix);
    this.setTileRequest(tile);
    this.playerInv = playerInv;
    result = new InventoryCraftResult();

    TileMaster tileMaster = this.getTileMaster();
    if (tileMaster != null) {
      SlotCraftingNetwork slotCraftOutput = new SlotCraftingNetwork(playerInv.player, matrix, result, 0, 101, 128);
      slotCraftOutput.setTileMaster(tileMaster);
      this.addSlotToContainer(slotCraftOutput);
    }

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
      List<ItemStack> list = tileMaster.getStacks();
      PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<>()), (EntityPlayerMP) playerIn);
    }

    return playerIn.getDistanceSq(getTileRequest().getPos().getX() + 0.5D, getTileRequest().getPos().getY() + 0.5D, getTileRequest().getPos().getZ() + 0.5D) <= 64.0D;
  }

  @Override
  public boolean canMergeSlot(ItemStack stack, Slot slot) {
    return slot.inventory != this.result && super.canMergeSlot(stack, slot);
  }

  @Override
  public TileMaster getTileMaster() {
    if(getTileRequest() == null || getTileRequest().getMaster() == null) {
      return null;
    }

    return getTileRequest().getMaster().getTileEntity(TileMaster.class);
  }

  public TileRequest getTileRequest() {
    return tileRequest;
  }

  public void setTileRequest(TileRequest tileRequest) {
    this.tileRequest = tileRequest;
  }

  @Override
  public boolean isRequest() {
    return true;
  }
}
