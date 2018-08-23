package mrriegel.storagenetwork.block;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Base class for Cable and Request Table
 * 
 */
public abstract class AbstractBlockConnectable extends BlockContainer {

  public AbstractBlockConnectable(Material materialIn) {
    super(materialIn);
  }

  @Override
  public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
    try {
      TileEntity tile = worldIn.getTileEntity(pos);
      if (tile != null && tile instanceof IConnectable) {
        IConnectable conUnitNeedsMaster = (IConnectable) tile;
        //the new thing needs to find the master of the network. so either my neighbor knows who the master is, 
        //or my neighbor IS the master
        TileEntity tileLoop = null;
        for (BlockPos p : UtilTileEntity.getSides(pos)) {
          tileLoop = worldIn.getTileEntity(p);
          if (tileLoop instanceof IConnectable) {
            IConnectable conUnit = (IConnectable) tileLoop;
            if (conUnit.getMaster() != null) {
              conUnitNeedsMaster.setMaster((conUnit).getMaster());
            }
          }
          else if (tileLoop instanceof TileMaster) {
            conUnitNeedsMaster.setMaster(p);
          }
        }
      }
      setConnections(worldIn, pos, state, true);
    }
    catch (Exception e) {
      StorageNetwork.instance.logger.error("StorageNetwork: exception thrown while updating neighbours:", e);
    }
  }

  public void setConnections(World worldIn, BlockPos pos, IBlockState state, boolean refresh) {
    TileEntity myselfTile = worldIn.getTileEntity(pos);
    if (myselfTile == null || myselfTile instanceof IConnectable == false) {
      return;
    }
    IConnectable myselfConnect = (IConnectable) myselfTile;
    if (myselfConnect.getMaster() == null) {
      for (BlockPos p : UtilTileEntity.getSides(pos)) {
        if (worldIn.getTileEntity(p) instanceof TileMaster) {
          myselfConnect.setMaster(p);
          break;
        }
      }
    }
    if (myselfConnect.getMaster() != null) {
      TileEntity tileMaster = worldIn.getTileEntity(myselfConnect.getMaster());
      myselfConnect.setMaster(null);
      worldIn.markChunkDirty(myselfTile.getPos(), myselfTile);
      try {
        setAllMastersNull(worldIn, pos, myselfConnect);
      }
      catch (Error e) {
        e.printStackTrace();
        if (tileMaster instanceof TileMaster) {
          ///seems like i can delete this superhack but im not sure, it never executes
          for (BlockPos p : ((TileMaster) tileMaster).getConnectables())
            if (worldIn.getChunkFromBlockCoords(p).isLoaded() && worldIn.getTileEntity(p) instanceof IConnectable) {
              ((IConnectable) worldIn.getTileEntity(p)).setMaster(null);
              worldIn.markChunkDirty(p, worldIn.getTileEntity(p));
            }
        }
      }
      if (refresh && tileMaster instanceof TileMaster) {
        ((TileMaster) tileMaster).refreshNetwork();
      }
    }
    worldIn.markChunkDirty(myselfTile.getPos(), myselfTile);
  }

  private void setAllMastersNull(World world, BlockPos pos, IConnectable myself) {
    // myself = worldIn.getTileEntity(pos);
    myself.setMaster(null);
    for (BlockPos posBeside : UtilTileEntity.getSides(pos)) {
      if (!world.getChunkFromBlockCoords(posBeside).isLoaded()) {
        continue;
      }
      TileEntity nhbr = world.getTileEntity(posBeside);
      if (nhbr instanceof IConnectable) {//
        IConnectable nbrConn = (IConnectable) nhbr;
        if (nbrConn.getMaster() != null) {
          nbrConn.setMaster(null);
          world.markChunkDirty(posBeside, world.getTileEntity(posBeside));
          setAllMastersNull(world, posBeside, myself);
        }
      }
    }
  }
}
