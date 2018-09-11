package mrriegel.storagenetwork.gui;

import mrriegel.storagenetwork.block.cable.ContainerCable;
import mrriegel.storagenetwork.block.cable.GuiCable;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.block.request.ContainerRequest;
import mrriegel.storagenetwork.block.request.GuiRequest;
import mrriegel.storagenetwork.block.request.TileRequest;
import mrriegel.storagenetwork.gui.fb.ContainerFastRemote;
import mrriegel.storagenetwork.gui.fb.ContainerFastRequest;
import mrriegel.storagenetwork.gui.fb.GuiFastRemote;
import mrriegel.storagenetwork.gui.fb.GuiFastRequest;
import mrriegel.storagenetwork.item.remote.ContainerRemote;
import mrriegel.storagenetwork.item.remote.GuiRemote;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

  public static final int CABLE = 0;
  public static final int REQUEST = 3;
  public static final int REMOTE = 4;
  public static final boolean FB_LOADED = Loader.isModLoaded("fastbench");

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    BlockPos pos = new BlockPos(x, y, z);
    UtilTileEntity.updateTile(world, pos);
    if (ID == CABLE) {
      return new ContainerCable((TileCable) world.getTileEntity(pos), player.inventory);
    }
    if (ID == REQUEST) {
      return FB_LOADED ? new ContainerFastRequest((TileRequest) world.getTileEntity(pos), player, world, pos) : new ContainerRequest((TileRequest) world.getTileEntity(pos), player.inventory);
    }
    if (ID == REMOTE) {
      return FB_LOADED ? new ContainerFastRemote(player, world, pos) : new ContainerRemote(player.inventory);
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
      return FB_LOADED ? new GuiFastRequest(player, world, pos) : new GuiRequest(new ContainerRequest((TileRequest) world.getTileEntity(pos), player.inventory));
    }
    if (ID == REMOTE) {
      return FB_LOADED ? new GuiFastRemote(player, world) : new GuiRemote(new ContainerRemote(player.inventory));
    }
    return null;
  }
}
