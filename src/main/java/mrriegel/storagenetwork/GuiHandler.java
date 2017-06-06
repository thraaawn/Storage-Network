package mrriegel.storagenetwork;
import mrriegel.storagenetwork.cable.ContainerCable;
import mrriegel.storagenetwork.cable.GuiCable;
import mrriegel.storagenetwork.helper.Util;
import mrriegel.storagenetwork.remote.ContainerRemote;
import mrriegel.storagenetwork.remote.GuiRemote;
import mrriegel.storagenetwork.request.ContainerRequest;
import mrriegel.storagenetwork.request.GuiRequest;
import mrriegel.storagenetwork.request.TileRequest;
import mrriegel.storagenetwork.tile.AbstractFilterTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
  public static final int CABLE = 0;
  public static final int REQUEST = 3;
  public static final int REMOTE = 4;
  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    Util.updateTile(world, new BlockPos(x, y, z));
    if (ID == CABLE) { return new ContainerCable((AbstractFilterTile) world.getTileEntity(new BlockPos(x, y, z)), player.inventory); }
    if (ID == REQUEST) { return new ContainerRequest((TileRequest) world.getTileEntity(new BlockPos(x, y, z)), player.inventory); }
    if (ID == REMOTE) { return new ContainerRemote(player.inventory); }
    return null;
  }
  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    if (ID == CABLE) {
      AbstractFilterTile tile = (AbstractFilterTile) world.getTileEntity(new BlockPos(x, y, z));
      return new GuiCable(new ContainerCable(tile, player.inventory));
    }
    if (ID == REQUEST) { return new GuiRequest(new ContainerRequest((TileRequest) world.getTileEntity(new BlockPos(x, y, z)), player.inventory)); }
    if (ID == REMOTE) { return new GuiRemote(new ContainerRemote(player.inventory)); }
    return null;
  }
}
