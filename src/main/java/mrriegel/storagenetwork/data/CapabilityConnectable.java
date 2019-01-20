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
        TileEntity tileEntity = world.getTileEntity(connectable.getMaster());
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
    public BlockPos getMaster() {
        return posMaster;
    }

    @Override
    public void setMaster(BlockPos master) {
        this.posMaster = master;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound result = new NBTTagCompound();
        if(posMaster == null) {
            return result;
        }

        NBTTagCompound masterData = NBTUtil.createPosTag(posMaster);
        masterData.setInteger("Dim", dimMaster);


        result.setTag("master", masterData);

        return result;

    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagCompound masterData = nbt.getCompoundTag("master");

        BlockPos masterPos = NBTUtil.getPosFromTag(masterData);
        this.setMaster(masterPos);
        this.setMasterDimension(masterData.getInteger("Dim"));
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
