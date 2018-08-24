package mrriegel.storagenetwork.item.remote;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mrriegel.storagenetwork.block.request.EnumSortType;
import mrriegel.storagenetwork.gui.GuiContainerStorageInventory;
import mrriegel.storagenetwork.util.NBTHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class GuiRemote extends GuiContainerStorageInventory {

  public GuiRemote(ContainerRemote inventorySlotsIn) {
    super(inventorySlotsIn);
  }

  @Override
  public boolean getDownwards() {
    ItemStack remote = getItemRemote();
    if (remote.isEmpty() == false)
      return NBTHelper.getBoolean(remote, "down");
    return false;
  }

  @Override
  public void setDownwards(boolean d) {
    ItemStack remote = getItemRemote();
    if (remote.isEmpty() == false)
      NBTHelper.setBoolean(remote, "down", d);
  }

  @Override
  public @Nullable EnumSortType getSort() {
    ItemStack remote = getItemRemote();
    if (remote.isEmpty() == false)
      return EnumSortType.valueOf(NBTHelper.getString(remote, "sort"));
    return null;
  }

  public @Nonnull ItemStack getItemRemote() {
    ItemStack remote = mc.player.inventory.getCurrentItem();
    if (remote.getItem() instanceof ItemRemote == false) {
      return ItemStack.EMPTY;
    }
    return remote;
  }

  @Override
  public void setSort(EnumSortType s) {
    ItemStack remote = getItemRemote();
    if (remote.isEmpty() == false)
      NBTHelper.setString(remote, "sort", s.toString());
  }

  @Override
  public BlockPos getPos() {
    return BlockPos.ORIGIN;
  }

  @Override
  protected int getDim() {
    ItemStack remote = getItemRemote();
    if (remote.isEmpty() == false)
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
    return this.getItemRemote().isEmpty() == false;
  }
}
