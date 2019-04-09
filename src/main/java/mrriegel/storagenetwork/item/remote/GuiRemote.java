package mrriegel.storagenetwork.item.remote;

import javax.annotation.Nullable;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.data.EnumSortType;
import mrriegel.storagenetwork.gui.GuiContainerStorageInventory;
import mrriegel.storagenetwork.util.NBTHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class GuiRemote extends GuiContainerStorageInventory {

  ContainerRemote container;
  public GuiRemote(ContainerRemote container) {
    super(container);
    this.container = container;
    isSimple = container.getItemRemote().getMetadata() == RemoteType.SIMPLE.ordinal();
    if (isSimple) {
      //set different texture for simple 
      texture = new ResourceLocation(StorageNetwork.MODID, "textures/gui/request_full.png");
      this.setSort(EnumSortType.NAME);
      this.setDownwards(false);
    }
  }

  @Override
  public boolean getDownwards() {
    ItemStack remote = container.getItemRemote();
    if (remote.isEmpty() == false)
      return NBTHelper.getBoolean(remote, "down");
    return false;
  }

  @Override
  public void setDownwards(boolean d) {
    ItemStack remote = container.getItemRemote();
    if (remote.isEmpty() == false)
      NBTHelper.setBoolean(remote, "down", d);
  }

  @Override
  public @Nullable EnumSortType getSort() {
    ItemStack remote = container.getItemRemote();
    if (remote.isEmpty() == false)
      return EnumSortType.valueOf(NBTHelper.getString(remote, "sort"));
    return null;
  }

  //  public @Nonnull ItemStack getItemRemote() {
  //    //    ItemStack remote = mc.player.inventory.getCurrentItem();
  //    //    if (remote.getItem() instanceof ItemRemote == false) { 
  //      return ItemStack.EMPTY;
  //    //    }
  //    //    return remote;
  //  }

  @Override
  public void setSort(EnumSortType s) {
    ItemStack remote = container.getItemRemote();
    if (remote.isEmpty() == false)
      NBTHelper.setString(remote, "sort", s.toString());
  }

  @Override
  public BlockPos getPos() {
    return BlockPos.ORIGIN;
  }

  @Override
  protected int getDim() {
    ItemStack remote = container.getItemRemote();
    if (remote.isEmpty() == false)
      return NBTHelper.getInteger(remote, "dim");
    return 0;
  }

  @Override
  protected boolean isScreenValid() {
    return container.getItemRemote().isEmpty() == false;
  }
}
