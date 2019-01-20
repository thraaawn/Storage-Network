package mrriegel.storagenetwork.util;

import com.google.common.base.Objects;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class DimPos implements INBTSerializable<NBTTagCompound> {
    public int dimension;
    private BlockPos pos;

    public DimPos() {
    }

    public DimPos(int dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos;
    }

    public DimPos(World world, BlockPos pos) {
        this.dimension = world.provider.getDimension();
        this.pos = pos;
    }

    @Nullable
    public World getWorld() {
        return DimensionManager.getWorld(this.dimension);
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    public IBlockState getBlockState() {
        return getWorld().getBlockState(getBlockPos());
    }

    public <V> V getTileEntity(Class<V> tileEntityClassOrInterface) {
        TileEntity tileEntity = getWorld().getTileEntity(getBlockPos());
        if(tileEntity == null) {
            return null;
        }

        if(!tileEntityClassOrInterface.isAssignableFrom(tileEntity.getClass())) {
            return null;
        }

        return (V) tileEntity;
    }

    public boolean isLoaded() {
        if(getWorld() == null) {
            return false;
        }

        return getWorld().isBlockLoaded(pos);
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


    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound result = NBTUtil.createPosTag(this.pos);
        result.setInteger("Dim", this.dimension);
        return result;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.pos = NBTUtil.getPosFromTag(nbt);
        this.dimension = nbt.getInteger("Dim");
    }
}
