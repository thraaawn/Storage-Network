package mrriegel.storagenetwork.gui;

import mrriegel.storagenetwork.block.cable.ContainerCable;
import mrriegel.storagenetwork.block.cable.GuiCable;
import mrriegel.storagenetwork.block.cable.GuiCableProcessing;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.block.control.ContainerControl;
import mrriegel.storagenetwork.block.control.GuiControl;
import mrriegel.storagenetwork.block.control.TileControl;
import mrriegel.storagenetwork.block.request.ContainerRequest;
import mrriegel.storagenetwork.block.request.GuiRequest;
import mrriegel.storagenetwork.block.request.TileRequest;
import mrriegel.storagenetwork.gui.fb.ContainerFastRemote;
import mrriegel.storagenetwork.gui.fb.ContainerFastRequest;
import mrriegel.storagenetwork.gui.fb.GuiFastRemote;
import mrriegel.storagenetwork.gui.fb.GuiFastRequest;
import mrriegel.storagenetwork.item.remote.ContainerRemote;
import mrriegel.storagenetwork.item.remote.GuiRemote;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandler implements IGuiHandler {

  public static final int CABLE = 0;// EnumCableType.CONNECT.ordinal();
  public static final int REQUEST = 3;
  public static final int REMOTE = 4;
  public static final int CONTROLLER = 5;
  public static final boolean FB_LOADED = Loader.isModLoaded("fastbench");

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    BlockPos pos = new BlockPos(x, y, z);
    UtilTileEntity.updateTile(world, pos);
    if (ID == CABLE) {
      return new ContainerCable((TileCable) world.getTileEntity(pos), player.inventory);
    }
    if (ID == CONTROLLER) {
      return new ContainerControl((TileControl) world.getTileEntity(pos), player.inventory);
    }
    if (ID == REQUEST) {
      if (FB_LOADED) {
        return new ContainerFastRequest((TileRequest) world.getTileEntity(pos), player, world, pos);
      }
      else {
        return new ContainerRequest((TileRequest) world.getTileEntity(pos), player.inventory);
      }
    }
    if (ID == REMOTE) {
      if (FB_LOADED) {
        return new ContainerFastRemote(player, world, EnumHand.values()[x]);
      }
      else {
        return new ContainerRemote(player.inventory);
      }
    }
    return null;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    BlockPos pos = new BlockPos(x, y, z);
    if (ID == CABLE) {
      TileCable tile = (TileCable) world.getTileEntity(pos);
      if (tile.getBlockType() == ModBlocks.processKabel)
        return new GuiCableProcessing(new ContainerCable(tile, player.inventory));
      return new GuiCable(new ContainerCable(tile, player.inventory));
    }
    if (ID == CONTROLLER) {
      return new GuiControl(new ContainerControl((TileControl) world.getTileEntity(pos), player.inventory));
    }
    if (ID == REQUEST) {
      if (FB_LOADED) {
        return new GuiFastRequest(player, world, pos);
      }
      else {
        return new GuiRequest(new ContainerRequest((TileRequest) world.getTileEntity(pos), player.inventory));
      }
    }
    if (ID == REMOTE) {
      if (FB_LOADED) {
        return new GuiFastRemote(player, world, EnumHand.values()[x]);
      }
      else {
        return new GuiRemote(new ContainerRemote(player.inventory));
      }
    }
    return null;
  }
}
