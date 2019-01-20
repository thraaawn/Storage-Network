package mrriegel.storagenetwork.block.cable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.api.data.DimPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class TileCableWithFacing extends TileCable {
  @Nullable
  EnumFacing direction = null;

  public boolean hasDirection() {
    return direction != null;
  }

  public EnumFacing getDirection() {
    return direction;
  }

  public BlockPos getFacingPosition() {
    return this.getPos().offset(direction);
  }

  public void setDirection(@Nullable EnumFacing direction) {
    this.direction = direction;
  }

  protected boolean isValidLinkNeighbor(EnumFacing facing) {
    if(facing == null) {
      return false;
    }

    TileEntity neighbor = world.getTileEntity(pos.offset(facing));
    if(neighbor != null && neighbor.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())) {
      return true;
    }

    return false;
  }

  public void findNewDirection() {
    if(isValidLinkNeighbor(direction)) {
      return;
    }

    for(EnumFacing facing : EnumFacing.values()) {
      if(isValidLinkNeighbor(facing)) {
        setDirection(facing);
        return;
      }
    }

    setDirection(null);
  }


  public void rotateTo(EnumFacing direction) {
    this.direction = direction;
    this.markDirty();
  }

  public void rotate() {
    this.rotateTo(getNextPossibleDirection(direction));
  }

  private static EnumFacing getNextPossibleDirection(EnumFacing direction) {
    int newOrd = direction.ordinal() + 1;
    if(newOrd >= EnumFacing.values().length) {
      newOrd = 0;
    }
    return EnumFacing.getFront(newOrd);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);

    if(compound.hasKey("direction")) {
      this.direction = EnumFacing.getFront(compound.getInteger("direction"));
    } else {
      this.direction = null;
    }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    if(direction != null) {
      compound.setInteger("direction", this.direction.ordinal());
    }
    return super.writeToNBT(compound);
  }

}
