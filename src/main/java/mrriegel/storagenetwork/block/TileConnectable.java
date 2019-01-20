package mrriegel.storagenetwork.block;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.data.CapabilityConnectable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * Base class for Cable, Control, Request
 *
 */
public class TileConnectable extends TileEntity {

  protected CapabilityConnectable connectable;

  public TileConnectable() {
    connectable = new CapabilityConnectable();
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    if(compound.hasKey("connectable")) {
      connectable.deserializeNBT(compound.getCompoundTag("connectable"));
  }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    compound.setTag("connectable", connectable.serializeNBT());
    return super.writeToNBT(compound);
  }

  @Override
  public NBTTagCompound getUpdateTag() {
    return writeToNBT(new NBTTagCompound());
  }

  @Override
  public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
    return oldState.getBlock() != newSate.getBlock();
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
    if (ConfigHandler.reloadNetworkWhenUnloadChunk && connectable != null && connectable.getMaster() != null) {
      try {
        TileEntity maybeMaster = world.getTileEntity(connectable.getMaster());
        if (maybeMaster instanceof TileMaster) {
          ((TileMaster) maybeMaster).refreshNetwork();
        }
      }
      catch (Exception e) {
        StorageNetwork.instance.logger.error("Error on chunk unload ", e);
      }
    }
  }

  @Override
  public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
    if(capability == CapabilityConnectable.CONNECTABLE_CAPABILITY) {
      return true;
    }

    return super.hasCapability(capability, facing);
  }

  @Nullable
  @Override
  public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
    if(capability == CapabilityConnectable.CONNECTABLE_CAPABILITY) {
      return (T) connectable;
    }

    return super.getCapability(capability, facing);
  }

  public BlockPos getMaster() {
    return connectable.getMaster();
  }
}
