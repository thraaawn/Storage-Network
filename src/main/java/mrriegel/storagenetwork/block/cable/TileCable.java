package mrriegel.storagenetwork.block.cable;

import mrriegel.storagenetwork.block.TileConnectable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Base class for TileCable
 *
 */
public class TileCable extends TileConnectable {
  public TileCable() {
    super();
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);

  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);
    return compound;
  }

  @Override
  public AxisAlignedBB getRenderBoundingBox() {
    double renderExtention = 1.0d;
    AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - renderExtention, pos.getY() - renderExtention, pos.getZ() - renderExtention, pos.getX() + 1 + renderExtention, pos.getY() + 1 + renderExtention, pos.getZ() + 1 + renderExtention);
    return bb;
  }
}
