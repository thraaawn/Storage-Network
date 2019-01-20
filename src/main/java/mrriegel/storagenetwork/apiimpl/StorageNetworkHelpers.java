package mrriegel.storagenetwork.apiimpl;

import mrriegel.storagenetwork.api.capability.IConnectable;
import mrriegel.storagenetwork.api.data.IItemStackMatcher;
import mrriegel.storagenetwork.api.IStorageNetworkHelpers;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.data.ItemStackMatcher;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StorageNetworkHelpers implements IStorageNetworkHelpers  {
    /**
     * This can only be called on the server side!
     * It returns the TileMaster tile entity for the given connectable.
     *
     * @param connectable
     * @return
     */
    @Nullable
    public TileMaster getTileMasterForConnectable(@Nonnull IConnectable connectable) {
        return connectable.getMasterPos().getTileEntity(TileMaster.class);
    }

    @Override
    public IItemStackMatcher createItemStackMatcher(ItemStack stack, boolean ore, boolean nbt, boolean meta) {
        return new ItemStackMatcher(stack, meta, ore, nbt);
    }
}
