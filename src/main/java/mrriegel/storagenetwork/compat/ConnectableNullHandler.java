package mrriegel.storagenetwork.compat;

import mrriegel.storagenetwork.api.capability.IConnectable;
import mrriegel.storagenetwork.api.data.DimPos;
import mrriegel.storagenetwork.capabilities.StorageNetworkCapabilities;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

public class ConnectableNullHandler implements IConnectable {

    public Capability getCapability() {
        return StorageNetworkCapabilities.CONNECTABLE_CAPABILITY;
    }


    @Override
    public DimPos getMasterPos() {
        return new DimPos(0, new BlockPos(0,0,0));
    }

    @Override
    public DimPos getPos() {
        return new DimPos(0, new BlockPos(0, 0, 0));
    }

    @Override
    public void setMasterPos(DimPos masterPos) {
    }
}
