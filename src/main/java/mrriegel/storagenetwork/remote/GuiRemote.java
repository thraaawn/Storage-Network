package mrriegel.storagenetwork.remote;
import mrriegel.storagenetwork.RigelNetworkGuiRequest;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.helper.NBTHelper;
import mrriegel.storagenetwork.request.TileRequest.EnumSortType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class GuiRemote extends RigelNetworkGuiRequest {
  public GuiRemote(ContainerRemote inventorySlotsIn) {
    super(inventorySlotsIn);
    texture = new ResourceLocation(StorageNetwork.MODID + ":textures/gui/request.png");
  }
  @Override
  public void initGui() {
    super.initGui();
    String savedSearch = NBTHelper.getString(getItemRemote(), NBT_SEARCH);
    if (savedSearch != null) {
      searchBar.setText(savedSearch);
    }
  }
  @Override
  public void updateScreen() {
    super.updateScreen();
    if (searchBar != null && searchBar.getText() != null) {
      NBTHelper.setString(getItemRemote(), NBT_SEARCH, searchBar.getText());
    }
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
    return NBTHelper.getBoolean(getItemRemote(), "down");
  }
  @Override
  public void setDownwards(boolean d) {
    NBTHelper.setBoolean(getItemRemote(), "down", d);
  }
  @Override
  public EnumSortType getSort() {
    return EnumSortType.valueOf(NBTHelper.getString(getItemRemote(), "sort"));
  }
  public ItemStack getItemRemote() {
    return mc.player.inventory.getCurrentItem();
  }
  @Override
  public void setSort(EnumSortType s) {
    NBTHelper.setString(getItemRemote(), "sort", s.toString());
  }
  @Override
  public BlockPos getPos() {
    return BlockPos.ORIGIN;
  }
  @Override
  protected int getDim() {
    return NBTHelper.getInteger(getItemRemote(), "dim");
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
}
