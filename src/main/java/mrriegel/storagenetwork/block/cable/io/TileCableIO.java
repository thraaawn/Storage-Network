package mrriegel.storagenetwork.block.cable.io;

import mrriegel.storagenetwork.api.data.EnumStorageDirection;
import mrriegel.storagenetwork.block.cable.TileCableWithFacing;
import mrriegel.storagenetwork.capabilities.CapabilityConnectableAutoIO;
import mrriegel.storagenetwork.capabilities.StorageNetworkCapabilities;
import mrriegel.storagenetwork.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class TileCableIO extends TileCableWithFacing {
  protected CapabilityConnectableAutoIO ioStorage;

  public TileCableIO() {
    this.ioStorage = new CapabilityConnectableAutoIO(this, EnumStorageDirection.BOTH);
  }

  public TileCableIO(EnumStorageDirection storageDirection) {
    this.ioStorage = new CapabilityConnectableAutoIO(this, storageDirection);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);

    this.ioStorage.deserializeNBT(compound.getCompoundTag("ioStorage"));
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    NBTTagCompound result = super.writeToNBT(compound);
    result.setTag("ioStorage", this.ioStorage.serializeNBT());
    return result;
  }

  @Override
  public void setDirection(@Nullable EnumFacing direction) {
    super.setDirection(direction);
    this.ioStorage.setInventoryFace(direction);
  }

  @Override
  public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
    if(capability == StorageNetworkCapabilities.CONNECTABLE_AUTO_IO) {
      return true;
    }

    return super.hasCapability(capability, facing);
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
    if(capability == StorageNetworkCapabilities.CONNECTABLE_AUTO_IO) {
      // If this tile entity is being loaded in a chunk from nbt it is using the argument-less constructor.
      // This causes the tile entity to have its EnumStorageDirection set to BOTH, see the constructor above.
      // With this little patch we are restoring the proper direction.
      // TODO: This should get cleaned up by further splitting TileCableIO into Export and Import tiles.
      Block blockType = this.world.getBlockState(pos).getBlock();
      if(blockType == ModBlocks.exKabel) {
        this.ioStorage.direction = EnumStorageDirection.OUT;
      } else if(blockType == ModBlocks.imKabel) {
        this.ioStorage.direction = EnumStorageDirection.IN;
      }

      return (T) ioStorage;
    }

    return super.getCapability(capability, facing);
  }
}
