package mrriegel.storagenetwork.data;

import mrriegel.storagenetwork.block.IConnectable;
import mrriegel.storagenetwork.block.master.TileMaster;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class CapabilityConnectable implements IConnectable, INBTSerializable<NBTTagCompound> {
    protected int dimMaster;
    protected BlockPos posMaster;
    protected int dim;
    protected BlockPos pos;

    @CapabilityInject(IConnectable.class)
    public static Capability<IConnectable> CONNECTABLE_CAPABILITY = null;

    public static void initCapability() {
        CapabilityManager.INSTANCE.register(IConnectable.class, new Storage(), new Factory());
    }

    /**
     * This can only be called on the server side!
     * It returns the TileMaster tile entity for the given connectable.
     *
     * @param connectable
     * @return
     */
    @Nullable
    public static TileMaster getTileMasterForConnectable(@Nonnull IConnectable connectable) {
        WorldServer world = DimensionManager.getWorld(connectable.getMasterDimension());
        TileEntity tileEntity = world.getTileEntity(connectable.getMasterPos());
        if(tileEntity instanceof TileMaster) {
            return (TileMaster) tileEntity;
        }

        return null;
    }

    @Override
    public int getMasterDimension() {
        return dimMaster;
    }

    @Override
    public void setMasterDimension(int dimMaster) {
        this.dimMaster = dimMaster;
    }

    @Override
    public BlockPos getMasterPos() {
        return posMaster;
    }

    @Override
    public void setMasterPos(BlockPos master) {
        this.posMaster = master;
    }


    @Override
    public int getDim() {
        return dim;
    }

    public void setDimension(int dim) {
        this.dim = dim;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound result = new NBTTagCompound();
        if(posMaster == null) {
            return result;
        }

        NBTTagCompound masterData = NBTUtil.createPosTag(posMaster);
        masterData.setInteger("Dim", dimMaster);

        NBTTagCompound selfData = NBTUtil.createPosTag(pos);
        selfData.setInteger("Dim", dim);

        result.setTag("master", masterData);
        result.setTag("self", selfData);

        return result;

    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagCompound masterData = nbt.getCompoundTag("master");
        this.setMasterPos(NBTUtil.getPosFromTag(masterData));
        this.setMasterDimension(masterData.getInteger("Dim"));

        NBTTagCompound selfData = nbt.getCompoundTag("self");
        this.setPos(NBTUtil.getPosFromTag(selfData));
        this.setDimension(selfData.getInteger("Dim"));
    }


    private static class Factory implements Callable<IConnectable> {

        @Override
        public IConnectable call() throws Exception {
            return new CapabilityConnectable();
        }
    }

    private static class Storage implements Capability.IStorage<IConnectable> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<IConnectable> capability, IConnectable rawInstance, EnumFacing side) {
            CapabilityConnectable instance = (CapabilityConnectable)rawInstance;
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<IConnectable> capability, IConnectable rawInstance, EnumFacing side, NBTBase nbt) {
            CapabilityConnectable instance = (CapabilityConnectable)rawInstance;
            instance.deserializeNBT((NBTTagCompound) nbt);
        }
    }
}
