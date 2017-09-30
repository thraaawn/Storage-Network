package mrriegel.storagenetwork;
import mrriegel.storagenetwork.helper.Util;
import mrriegel.storagenetwork.master.TileMaster;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractBlockConnectable extends BlockContainer {
  public AbstractBlockConnectable(Material materialIn) {
    super(materialIn);
  }
  @Override
  public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
    try {
      //be more careful eh?  java.lang.IllegalArgumentException: Cannot get property PropertyInteger{name=meta, clazz=class java.lang.Integer, values=[0, 1, 2, 3, 4, 5, 6, 7, 8, 
      if (!blockIn.hasTileEntity(state) && blockIn != Blocks.AIR) {
        return;
      }
      if (worldIn.getTileEntity(pos) instanceof IConnectable) {
        IConnectable conUnitNeedsMaster = (IConnectable) worldIn.getTileEntity(pos);
        //the new thing needs to find the master of the network. so either my neighbor knows who the master is, 
        //or my neighbor IS the master
        for (BlockPos p : Util.getSides(pos)) {
          if (worldIn.getTileEntity(p) instanceof IConnectable) {
            IConnectable conUnit = (IConnectable) worldIn.getTileEntity(p);
            if (conUnit.getMaster() != null) {
              conUnitNeedsMaster.setMaster((conUnit).getMaster());
            }
          }
          else if (worldIn.getTileEntity(p) instanceof TileMaster) {
            conUnitNeedsMaster.setMaster(p);
          }
        }
      }
      setConnections(worldIn, pos, state, true);
    }
    catch (Exception e) {
      System.out.println("StorageNetwork: exception thrown while updating neighbours:");
      e.printStackTrace();
    }
  }
  public void setConnections(World worldIn, BlockPos pos, IBlockState state, boolean refresh) {
    IConnectable tile = (IConnectable) worldIn.getTileEntity(pos);
    if (tile.getMaster() == null) {
      for (BlockPos p : Util.getSides(pos)) {
        if (worldIn.getTileEntity(p) instanceof TileMaster) {
          tile.setMaster(p);
          break;
        }
      }
    }
    if (tile.getMaster() != null) {
      TileEntity mas = worldIn.getTileEntity(tile.getMaster());
      tile.setMaster(null);
      worldIn.markChunkDirty(((TileEntity) tile).getPos(), ((TileEntity) tile));
      try {
        setAllMastersNull(worldIn, pos);
      }
      catch (Error e) {
        e.printStackTrace();
        if (mas instanceof TileMaster)
          for (BlockPos p : ((TileMaster) mas).connectables)
          if (worldIn.getChunkFromBlockCoords(p).isLoaded() && worldIn.getTileEntity(p) instanceof IConnectable) {
          ((IConnectable) worldIn.getTileEntity(p)).setMaster(null);
          worldIn.markChunkDirty(p, worldIn.getTileEntity(p));
          }
      }
      if (refresh && mas instanceof TileMaster) {
        ((TileMaster) mas).refreshNetwork();
      }
    }
    worldIn.markChunkDirty(((TileEntity) tile).getPos(), ((TileEntity) tile));
  }
  private void setAllMastersNull(World world, BlockPos pos) {
    ((IConnectable) world.getTileEntity(pos)).setMaster(null);
    for (BlockPos bl : Util.getSides(pos)) {
      if (world.getChunkFromBlockCoords(bl).isLoaded() && world.getTileEntity(bl) instanceof IConnectable && ((IConnectable) world.getTileEntity(bl)).getMaster() != null) {
        ((IConnectable) world.getTileEntity(bl)).setMaster(null);
        world.markChunkDirty(bl, world.getTileEntity(bl));
        setAllMastersNull(world, bl);
      }
    }
  }
}
