package mrriegel.storagenetwork.data;

import mrriegel.storagenetwork.block.IConnectable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class CapabilityConnectable implements IConnectable, INBTSerializable<NBTTagCompound> {
    protected BlockPos posMaster;

    @CapabilityInject(IConnectable.class)
    public static Capability<IConnectable> CONNECTABLE_CAPABILITY = null;

    public static void initCapability() {
        CapabilityManager.INSTANCE.register(IConnectable.class, new Storage(), new Factory());
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
        BlockPos masterPos = this.getMaster();
        NBTTagCompound masterData = NBTUtil.createPosTag(masterPos);

        NBTTagCompound result = new NBTTagCompound();
        result.setTag("master", masterData);

        return result;

    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagCompound masterData = nbt.getCompoundTag("master");

        BlockPos masterPos = NBTUtil.getPosFromTag(masterData);
        this.setMaster(masterPos);
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
