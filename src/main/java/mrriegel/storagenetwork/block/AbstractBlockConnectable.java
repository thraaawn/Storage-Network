package mrriegel.storagenetwork.block;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.data.CapabilityConnectable;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Base class for Control, Cable and Request Table
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
      if (tile != null && tile.hasCapability(CapabilityConnectable.CONNECTABLE_CAPABILITY, null)) {
        IConnectable conUnitNeedsMaster = tile.getCapability(CapabilityConnectable.CONNECTABLE_CAPABILITY, null);
        //the new thing needs to find the master of the network. so either my neighbor knows who the master is, 
        //or my neighbor IS the master
        TileEntity tileLoop = null;
        for (BlockPos p : UtilTileEntity.getSides(pos)) {
          tileLoop = worldIn.getTileEntity(p);
          if (tileLoop != null && tileLoop.hasCapability(CapabilityConnectable.CONNECTABLE_CAPABILITY, null)) {
            IConnectable conUnit = tileLoop.getCapability(CapabilityConnectable.CONNECTABLE_CAPABILITY, null);
            if (conUnit.getMaster() != null) {
              conUnitNeedsMaster.setMaster(conUnit.getMaster());
              conUnitNeedsMaster.setMasterDimension(conUnit.getMasterDimension());
            }
          }
          else if (tileLoop instanceof TileMaster) {
            conUnitNeedsMaster.setMaster(p);
            conUnitNeedsMaster.setMasterDimension(worldIn.provider.getDimension());
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
    if (myselfTile == null || !myselfTile.hasCapability(CapabilityConnectable.CONNECTABLE_CAPABILITY, null)) {
      return;
    }
    IConnectable myselfConnect = myselfTile.getCapability(CapabilityConnectable.CONNECTABLE_CAPABILITY, null);
    if (myselfConnect.getMaster() == null) {
      for (BlockPos p : UtilTileEntity.getSides(pos)) {
        if (worldIn.getTileEntity(p) instanceof TileMaster) {
          myselfConnect.setMaster(p);
          myselfConnect.setMasterDimension(worldIn.provider.getDimension());
          break;
        }
      }
    }

    if(worldIn.isRemote) {
      return;
    }

    if (myselfConnect.getMaster() != null) {
      TileMaster tileMaster = CapabilityConnectable.getTileMasterForConnectable(myselfConnect);
      myselfConnect.setMaster(null);
      worldIn.markChunkDirty(myselfTile.getPos(), myselfTile);
      try {
        setAllMastersNull(worldIn, pos, myselfConnect);
      }
      catch (Error e) {
        e.printStackTrace();
        if (tileMaster != null) {
          ///seems like i can delete this superhack but im not sure, it never executes
          for (BlockPos p : tileMaster.getConnectables()) {
            TileEntity tileCurrent = worldIn.getTileEntity(p);
            if (worldIn.getChunkFromBlockCoords(p).isLoaded() && tileCurrent.hasCapability(CapabilityConnectable.CONNECTABLE_CAPABILITY, null)) {
              tileCurrent.getCapability(CapabilityConnectable.CONNECTABLE_CAPABILITY, null).setMaster(null);
              worldIn.markChunkDirty(p, tileCurrent);
            }
          }
        }
      }
      if (refresh && tileMaster != null) {
        tileMaster.refreshNetwork();
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
      if (nhbr != null && nhbr.hasCapability(CapabilityConnectable.CONNECTABLE_CAPABILITY, null)) {//
        IConnectable nbrConn = nhbr.getCapability(CapabilityConnectable.CONNECTABLE_CAPABILITY, null);
        if (nbrConn.getMaster() != null) {
          nbrConn.setMaster(null);
          world.markChunkDirty(posBeside, world.getTileEntity(posBeside));
          setAllMastersNull(world, posBeside, myself);
        }
      }
    }
  }
}
