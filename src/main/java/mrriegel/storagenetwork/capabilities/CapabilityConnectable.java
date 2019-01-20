package mrriegel.storagenetwork.capabilities;

import mrriegel.storagenetwork.api.capability.DefaultConnectable;
import mrriegel.storagenetwork.api.capability.IConnectable;
import mrriegel.storagenetwork.api.data.DimPos;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class CapabilityConnectable extends DefaultConnectable implements INBTSerializable<NBTTagCompound> {

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound result = new NBTTagCompound();
        if(getMasterPos() == null) {
            return result;
        }

        result.setTag("master", getMasterPos().serializeNBT());
        result.setTag("self", getPos().serializeNBT());

        return result;

    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.setMasterPos(new DimPos(nbt.getCompoundTag("master")));
        if(nbt.hasKey("self")) {
            this.setPos(new DimPos(nbt.getCompoundTag("self")));
        }
    }


    public static class Storage implements Capability.IStorage<IConnectable> {
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
