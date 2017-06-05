package mrriegel.storagenetwork.remote;
import mrriegel.storagenetwork.RigelNetworkGuiRequest;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.helper.NBTHelper;
import mrriegel.storagenetwork.request.TileRequest.EnumSortType;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class GuiRemote extends RigelNetworkGuiRequest {
  public GuiRemote(Container inventorySlotsIn) {
    super(inventorySlotsIn);
    texture = new ResourceLocation(StorageNetwork.MODID + ":textures/gui/request.png");
  }

  @Override
  public int getLines() {
    return 4;
  }
  @Override
  public int getColumns() {
    return 9;
  }
  @Override
  public boolean getDownwards() {
    return NBTHelper.getBoolean(mc.player.inventory.getCurrentItem(), "down");
  }
  @Override
  public void setDownwards(boolean d) {
    NBTHelper.setBoolean(mc.player.inventory.getCurrentItem(), "down", d);
  }
  @Override
  public EnumSortType getSort() {
    return EnumSortType.valueOf(NBTHelper.getString(mc.player.inventory.getCurrentItem(), "sort"));
  }
  @Override
  public void setSort(EnumSortType s) {
    NBTHelper.setString(mc.player.inventory.getCurrentItem(), "sort", s.toString());
  }
  @Override
  public BlockPos getPos() {
    return BlockPos.ORIGIN;
  }

  @Override
  protected int getDim() {
    return NBTHelper.getInteger(mc.player.inventory.getCurrentItem(), "dim");
  }
  @Override
  protected boolean inField(int mouseX, int mouseY) {
    return mouseX > (guiLeft + 7) && mouseX < (guiLeft + xSize - 7) && mouseY > (guiTop + 7) && mouseY < (guiTop + 90);
    }
  @Override
  protected boolean inSearchbar(int mouseX, int mouseY) {

    return isPointInRegion(81, 96, 85, fontRendererObj.FONT_HEIGHT, mouseX, mouseY); }
  @Override
  protected boolean inX(int mouseX, int mouseY) {
    return false;
    //return isPointInRegion(63, 110, 7, 7, mouseX, mouseY);
  }
}
