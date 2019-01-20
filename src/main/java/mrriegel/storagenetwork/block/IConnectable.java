package mrriegel.storagenetwork.block;

import net.minecraft.util.math.BlockPos;

/**
 * All blocks that can connect to the network implement this.
 * 
 * Cables, Request table, Master.
 * 
 *
 */
public interface IConnectable {
  public int getMasterDimension();

  public void setMasterDimension(int dimMaster);

  public BlockPos getMaster();

  public void setMaster(BlockPos master);
}
