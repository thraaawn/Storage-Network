package mrriegel.storagenetwork.compat;

import mrriegel.storagenetwork.block.IConnectable;
import mrriegel.storagenetwork.data.CapabilityConnectable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import org.dave.compactmachines3.integration.AbstractNullHandler;
import org.dave.compactmachines3.integration.CapabilityNullHandler;

@CapabilityNullHandler
public class ConnectableNullHandler extends AbstractNullHandler implements IConnectable {
    @Override
    public Capability getCapability() {
        return CapabilityConnectable.CONNECTABLE_CAPABILITY;
    }


    @Override
    public int getMasterDimension() {
        return 0;
    }

    @Override
    public void setMasterDimension(int dimMaster) {
    }

    @Override
    public BlockPos getMasterPos() {
        return new BlockPos(0,0,0);
    }

    @Override
    public void setMasterPos(BlockPos master) {
    }

    @Override
    public int getDim() {
        return 0;
    }

    @Override
    public BlockPos getPos() {
        return new BlockPos(0, 0, 0);
    }
}
