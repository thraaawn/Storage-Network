package mrriegel.storagenetwork.block.cable.link;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.TileCableWithFacing;
import mrriegel.storagenetwork.capabilities.CapabilityConnectableLink;
import mrriegel.storagenetwork.capabilities.StorageNetworkCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class TileCableLink extends TileCableWithFacing {
  protected CapabilityConnectableLink itemStorage;

  public TileCableLink() {
    this.itemStorage = new CapabilityConnectableLink(this);
    this.itemStorage.filters.setMatchOreDict(false);
    this.itemStorage.filters.setMatchMeta(true);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);

    this.itemStorage.deserializeNBT(compound.getCompoundTag("itemStorage"));
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    NBTTagCompound result = super.writeToNBT(compound);
    result.setTag("itemStorage", itemStorage.serializeNBT());
    return result;
  }

  @Override
  public void setDirection(@Nullable EnumFacing direction) {
    super.setDirection(direction);
    this.itemStorage.setInventoryFace(direction);
  }

  @Override
  public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
    if(capability == StorageNetworkCapabilities.CONNECTABLE_ITEM_STORAGE_CAPABILITY) {
      return true;
    }

    return super.hasCapability(capability, facing);
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
    if(capability == StorageNetworkCapabilities.CONNECTABLE_ITEM_STORAGE_CAPABILITY) {
      return (T) itemStorage;
    }

    return super.getCapability(capability, facing);
  }

}
