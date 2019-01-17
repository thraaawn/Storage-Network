package mrriegel.storagenetwork.block;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.config.ConfigHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Base class for Cable, Control, Request
 *
 */
public class TileConnectable extends TileEntity implements IConnectable {

  protected BlockPos posMaster;

  @Override
  public NBTTagCompound getUpdateTag() {
    return writeToNBT(new NBTTagCompound());
  }

  @SuppressWarnings("serial")
  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    posMaster = new Gson().fromJson(compound.getString("master"), new TypeToken<BlockPos>() {}.getType());
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);
    compound.setString("master", new Gson().toJson(posMaster));
    return compound;
  }

  @Override
  public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
    return oldState.getBlock() != newSate.getBlock();
  }

  @Override
  public BlockPos getMaster() {
    return posMaster;
  }

  @Override
  public void setMaster(BlockPos master) {
    this.posMaster = master;
  }

  @Override
  public SPacketUpdateTileEntity getUpdatePacket() {
    NBTTagCompound syncData = new NBTTagCompound();
    this.writeToNBT(syncData);
    return new SPacketUpdateTileEntity(this.pos, 1, syncData);
  }

  @Override
  public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    readFromNBT(pkt.getNbtCompound());
  }

  @Override
  public void onChunkUnload() {
    if (ConfigHandler.reloadNetworkWhenUnloadChunk && posMaster != null) {
      try {
        TileEntity maybeMaster = world.getTileEntity(posMaster);
        if (maybeMaster instanceof TileMaster) {
          ((TileMaster) maybeMaster).refreshNetwork();
        }
      }
      catch (Exception e) {
        StorageNetwork.instance.logger.error("Error on chunk unload ", e);
      }
    }
  }
}
