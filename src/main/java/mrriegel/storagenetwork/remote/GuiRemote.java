package mrriegel.storagenetwork.remote;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.gui.GuiContainerStorageInventory;
import mrriegel.storagenetwork.helper.NBTHelper;
import mrriegel.storagenetwork.request.TileRequest.EnumSortType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class GuiRemote extends GuiContainerStorageInventory {

  public GuiRemote(ContainerRemote inventorySlotsIn) {
    super(inventorySlotsIn);
    texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/request.png");
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
    ItemStack remote = getItemRemote();
    if (remote != null)
      return NBTHelper.getBoolean(remote, "down");
    return false;
  }

  @Override
  public void setDownwards(boolean d) {
    ItemStack remote = getItemRemote();
    if (remote != null)
      NBTHelper.setBoolean(remote, "down", d);
  }

  @Override
  public EnumSortType getSort() {
    ItemStack remote = getItemRemote();
    if (remote != null)
      return EnumSortType.valueOf(NBTHelper.getString(remote, "sort"));
    return null;
  }

  /**
   * 
   * @return @Nullable ItemStack
   */
  public ItemStack getItemRemote() {
    ItemStack remote = mc.player.inventory.getCurrentItem();
    if (remote.getItem() instanceof ItemRemote == false) {
      return null;
    }
    return remote;
  }

  @Override
  public void setSort(EnumSortType s) {
    ItemStack remote = getItemRemote();
    if (remote != null)
      NBTHelper.setString(remote, "sort", s.toString());
  }

  @Override
  public BlockPos getPos() {
    return BlockPos.ORIGIN;
  }

  @Override
  protected int getDim() {
    ItemStack remote = getItemRemote();
    if (remote != null)
      return NBTHelper.getInteger(remote, "dim");
    return 0;
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
    return this.getItemRemote() != null;
  }
}
