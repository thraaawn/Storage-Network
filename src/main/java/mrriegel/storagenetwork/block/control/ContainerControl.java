package mrriegel.storagenetwork.block.control;

import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.gui.IStorageContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;

public class ContainerControl extends Container implements IStorageContainer {

  private TileControl tileRequest;
  protected InventoryPlayer playerInv;

  public ContainerControl(final TileControl tile, final InventoryPlayer playerInv) {

    this.setTileRequest(tile);
    this.playerInv = playerInv;

    //    bindPlayerInvo(playerInv);
    //    bindHotbar();
  }

  @Override
  public void slotChanged() {
    //parent is abstract
    //seems to not happen from -shiftclick- crafting 

    // UtilTileEntity.updateTile(getTileRequest().getWorld(), getTileRequest().getPos());
  }

  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    TileMaster tileMaster = this.getTileMaster();
    if (tileMaster == null) {
      return false;
    }

    return playerIn.getDistanceSq(getTileRequest().getPos().getX() + 0.5D, getTileRequest().getPos().getY() + 0.5D, getTileRequest().getPos().getZ() + 0.5D) <= 64.0D;
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

  @Override
  public InventoryCrafting getCraftMatrix() {
    return null;
  }
}
