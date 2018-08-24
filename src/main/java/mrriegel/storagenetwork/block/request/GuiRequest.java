package mrriegel.storagenetwork.block.request;

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
  protected boolean inField(int mouseX, int mouseY) {
    return mouseX > (guiLeft + 7) && mouseX < (guiLeft + xSize - 7) && mouseY > (guiTop + 7) && mouseY < (guiTop + 90);
  }

  @Override
  protected boolean inSearchbar(int mouseX, int mouseY) {
    return isPointInRegion(81, 96, 85, fontRenderer.FONT_HEIGHT, mouseX, mouseY);
  }

  @Override
  protected boolean inX(int mouseX, int mouseY) {
    return isPointInRegion(63, 110, 7, 7, mouseX, mouseY);
  }

  @Override
  protected boolean isScreenValid() {
    return true;
  }
}
