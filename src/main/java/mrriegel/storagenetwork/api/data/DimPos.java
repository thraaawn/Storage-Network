package mrriegel.storagenetwork.api.data;

import javax.annotation.Nullable;
import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.StorageNetwork;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

public class DimPos implements INBTSerializable<NBTTagCompound> {

  public int dimension;
  private BlockPos pos = new BlockPos(0, 0, 0);
  private World world;

  public DimPos() {}

  public DimPos(NBTTagCompound tag) {
    this.deserializeNBT(tag);
  }

  public DimPos(ByteBuf buf) {
    this.dimension = buf.readInt();
    this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
  }

  public DimPos(int dimension, BlockPos pos) {
    this.dimension = dimension;
    this.pos = pos;
  }

  public DimPos(World world, BlockPos pos) {
    this.world = world;
    this.dimension = world == null ? 0 : world.provider.getDimension();
    this.pos = pos;
  }

  @Nullable
  public World getWorld() {
    if (world != null) {
      return world;
    }
    return DimensionManager.getWorld(this.dimension);
  }

  public BlockPos getBlockPos() {
    return pos;
  }

  public IBlockState getBlockState() {
    return getWorld().getBlockState(getBlockPos());
  }

  @Nullable
  public <V> V getTileEntity(Class<V> tileEntityClassOrInterface) {
    World world = getWorld();
    if (world == null || getBlockPos() == null) {
      return null;
    }
    TileEntity tileEntity = world.getTileEntity(getBlockPos());
    if (tileEntity == null) {
      return null;
    }
    if (!tileEntityClassOrInterface.isAssignableFrom(tileEntity.getClass())) {
      return null;
    }
    return (V) tileEntity;
  }

  @Nullable
  public <V> V getCapability(Capability<V> capability, EnumFacing side) {
    World world = getWorld();
    if (world == null || getBlockPos() == null) {
      return null;
    }
    TileEntity tileEntity = world.getTileEntity(getBlockPos());
    if (tileEntity == null) {
      return null;
    }
    if (!tileEntity.hasCapability(capability, side)) {
      return null;
    }
    return tileEntity.getCapability(capability, side);
  }

  public boolean isLoaded() {
    if (getWorld() == null) {
      return false;
    }
    return getWorld().isBlockLoaded(pos);
  }

  public boolean equals(World world, BlockPos pos) {
    return this.dimension == world.provider.getDimension() && pos.equals(this.pos);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DimPos dimPos = (DimPos) o;
    return dimension == dimPos.dimension &&
        Objects.equal(pos, dimPos.pos);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(dimension, pos);
  }

  @Override
  public String toString() {
    return "[" +
        "dimension=" + dimension +
        ", pos=" + pos +
        ']';
  }

  public void writeToByteBuf(ByteBuf buf) {
    buf.writeInt(this.dimension);
    buf.writeInt(this.pos.getX());
    buf.writeInt(this.pos.getY());
    buf.writeInt(this.pos.getZ());
  }

  @Override
  public NBTTagCompound serializeNBT() {
    if (pos == null) {
      pos = new BlockPos(0, 0, 0);
    }
    NBTTagCompound result = NBTUtil.createPosTag(this.pos);
    result.setInteger("Dim", this.dimension);
    return result;
  }

  @Override
  public void deserializeNBT(NBTTagCompound nbt) {
    this.pos = NBTUtil.getPosFromTag(nbt);
    this.dimension = nbt.getInteger("Dim");
  }

  public DimPos offset(EnumFacing direction) {
    if (pos == null || direction == null) {
      StorageNetwork.log("Error: null offset in DimPos " + direction);
      return null;
    }
    return new DimPos(this.dimension, this.pos.offset(direction));
  }

  public Chunk getChunk() {
    return getWorld().getChunkFromBlockCoords(this.pos);
  }
}
