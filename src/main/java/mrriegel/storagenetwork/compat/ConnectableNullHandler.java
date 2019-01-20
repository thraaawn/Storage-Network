package mrriegel.storagenetwork.compat;

import mrriegel.storagenetwork.api.capability.IConnectable;
import mrriegel.storagenetwork.capabilities.StorageNetworkCapabilities;
import mrriegel.storagenetwork.api.data.DimPos;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import org.dave.compactmachines3.integration.AbstractNullHandler;
import org.dave.compactmachines3.integration.CapabilityNullHandler;

@CapabilityNullHandler
public class ConnectableNullHandler extends AbstractNullHandler implements IConnectable {
    @Override
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
