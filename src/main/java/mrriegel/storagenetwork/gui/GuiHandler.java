package mrriegel.storagenetwork.gui;

import mrriegel.storagenetwork.block.cable.ContainerCable;
import mrriegel.storagenetwork.block.cable.GuiCable;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.block.request.ContainerRequest;
import mrriegel.storagenetwork.block.request.GuiRequest;
import mrriegel.storagenetwork.block.request.TileRequest;
import mrriegel.storagenetwork.item.remote.ContainerRemote;
import mrriegel.storagenetwork.item.remote.GuiRemote;
import mrriegel.storagenetwork.util.UtilTileEntity;
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
    BlockPos pos = new BlockPos(x, y, z);
    UtilTileEntity.updateTile(world, pos);
    if (ID == CABLE) {
      return new ContainerCable((TileCable) world.getTileEntity(pos), player.inventory);
    }
    if (ID == REQUEST) {
      return new ContainerRequest((TileRequest) world.getTileEntity(pos), player.inventory);
    }
    if (ID == REMOTE) {
      return new ContainerRemote(player.inventory);
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    BlockPos pos = new BlockPos(x, y, z);
    if (ID == CABLE) {
      TileCable tile = (TileCable) world.getTileEntity(pos);
      return new GuiCable(new ContainerCable(tile, player.inventory));
    }
    if (ID == REQUEST) {
      return new GuiRequest(new ContainerRequest((TileRequest) world.getTileEntity(pos), player.inventory));
    }
    if (ID == REMOTE) {
      return new GuiRemote(new ContainerRemote(player.inventory));
    }
    return null;
  }
}
