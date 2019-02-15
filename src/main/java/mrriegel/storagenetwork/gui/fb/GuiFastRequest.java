package mrriegel.storagenetwork.gui.fb;

import mrriegel.storagenetwork.block.request.TileRequest;
import mrriegel.storagenetwork.data.EnumSortType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiFastRequest extends GuiFastNetworkCrafter {

  private TileRequest tile;

  public GuiFastRequest(EntityPlayer player, World world, BlockPos pos) {
    super(player, world, pos);
    tile = (TileRequest) world.getTileEntity(pos);
    this.inventorySlots = new ContainerFastRequest.Client(tile, player, world, pos);
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
