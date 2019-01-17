package mrriegel.storagenetwork.block.request;

import mrriegel.storagenetwork.data.EnumSortType;
import mrriegel.storagenetwork.gui.GuiContainerStorageInventory;
import net.minecraft.util.math.BlockPos;

public class GuiRequest extends GuiContainerStorageInventory {

  private TileRequest tile;

  public GuiRequest(ContainerRequest inventorySlotsIn) {
    super(inventorySlotsIn);
    tile = inventorySlotsIn.getTileRequest();
  }

  @Override
  public void initGui() {
    super.initGui();
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
  }

  @Override
  public boolean getDownwards() {
    return tile.isDownwards();
  }

  @Override
  public void setDownwards(boolean d) {
    tile.setDownwards(d);
  }

  @Override
  public EnumSortType getSort() {
    return tile.getSort();
  }

  @Override
  public void setSort(EnumSortType s) {
    tile.setSort(s);
  }

  @Override
  public BlockPos getPos() {
    return tile.getPos();
  }

  @Override
  protected int getDim() {
    return tile.getWorld().provider.getDimension();
  }

  @Override
  protected boolean isScreenValid() {
    return true;
  }
}
