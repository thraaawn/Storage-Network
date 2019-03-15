package mrriegel.storagenetwork.block;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.api.capability.IConnectable;
import mrriegel.storagenetwork.api.data.DimPos;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.capabilities.StorageNetworkCapabilities;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Base class for Control, Cable and Request Table
 *
 */
public abstract class AbstractBlockConnectable extends BaseBlock {

  public AbstractBlockConnectable(Material materialIn, String registryName) {
    super(materialIn, registryName);
  }

  @Override
  public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
    try {
      TileEntity tile = worldIn.getTileEntity(pos);
      if (tile != null && tile.hasCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null)) {
        IConnectable conUnitNeedsMaster = tile.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null);
        //the new thing needs to find the master of the network. so either my neighbor knows who the master is,
        //or my neighbor IS the master
        TileEntity tileLoop = null;
        for (BlockPos p : UtilTileEntity.getSides(pos)) {
          tileLoop = worldIn.getTileEntity(p);
          if (tileLoop != null && tileLoop.hasCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null)) {
            IConnectable conUnit = tileLoop.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null);
            if (conUnit.getMasterPos() != null) {
              conUnitNeedsMaster.setMasterPos(conUnit.getMasterPos());
            }
          }
          else if (tileLoop instanceof TileMaster) {
            conUnitNeedsMaster.setMasterPos(new DimPos(worldIn, p));
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
    if (myselfTile == null || !myselfTile.hasCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null)) {
      return;
    }
    IConnectable myselfConnect = myselfTile.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null);
    if (myselfConnect.getMasterPos() == null) {
      for (BlockPos p : UtilTileEntity.getSides(pos)) {
        if (worldIn.getTileEntity(p) instanceof TileMaster) {
          myselfConnect.setMasterPos(new DimPos(worldIn, p));
          break;
        }
      }
    }

    if(worldIn.isRemote) {
      return;
    }

    if (myselfConnect.getMasterPos() != null) {
      TileMaster tileMaster = StorageNetwork.helpers.getTileMasterForConnectable(myselfConnect);
      myselfConnect.setMasterPos(null);
      worldIn.markChunkDirty(myselfTile.getPos(), myselfTile);
      try {
        setAllMastersNull(worldIn, pos, myselfConnect);
      }
      catch (Error e) {
        e.printStackTrace();
        if (tileMaster != null) {
          ///seems like i can delete this superhack but im not sure, it never executes
          for (DimPos p : tileMaster.getConnectablePositions()) {
            TileEntity tileCurrent = p.getTileEntity(TileEntity.class);
            if (p.isLoaded() && tileCurrent != null && tileCurrent.hasCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null)) {
              tileCurrent.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null).setMasterPos(null);
              p.getWorld().markChunkDirty(p.getBlockPos(), tileCurrent);
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
    myself.setMasterPos(null);
    for (BlockPos posBeside : UtilTileEntity.getSides(pos)) {
      if (!world.getChunkFromBlockCoords(posBeside).isLoaded()) {
        continue;
      }
      TileEntity nhbr = world.getTileEntity(posBeside);
      if (nhbr != null && nhbr.hasCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null)) {//
        IConnectable nbrConn = nhbr.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null);
        if (nbrConn.getMasterPos() != null) {
          nbrConn.setMasterPos(null);
          world.markChunkDirty(posBeside, world.getTileEntity(posBeside));
          setAllMastersNull(world, posBeside, myself);
        }
      }
    }
  }
}
