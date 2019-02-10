package mrriegel.storagenetwork.block.cable.io;

import mrriegel.storagenetwork.api.data.EnumStorageDirection;
import mrriegel.storagenetwork.api.capability.IConnectableItemAutoIO;
import mrriegel.storagenetwork.block.cable.BlockCableWithFacing;
import mrriegel.storagenetwork.capabilities.CapabilityConnectableAutoIO;
import mrriegel.storagenetwork.capabilities.StorageNetworkCapabilities;
import mrriegel.storagenetwork.api.data.DimPos;
import mrriegel.storagenetwork.util.inventory.UpgradesItemStackHandler;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockCableIO extends BlockCableWithFacing {
  private EnumStorageDirection storageDirection;

  public BlockCableIO(String registryName, EnumStorageDirection storageDirection) {
    super(registryName);
    this.storageDirection = storageDirection;
  }

  @Nullable
  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileCableIO(storageDirection);
  }

  @Override
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    DimPos breakPos = new DimPos(worldIn, pos);

    TileCableIO tile = breakPos.getTileEntity(TileCableIO.class);
    if(tile == null) {
      super.breakBlock(worldIn, pos, state);
      return;
    }

    IConnectableItemAutoIO iConnectable = breakPos.getCapability(StorageNetworkCapabilities.CONNECTABLE_AUTO_IO, null);
    if(iConnectable == null || !(iConnectable instanceof CapabilityConnectableAutoIO)) {
      super.breakBlock(worldIn, pos, state);
      return;
    }

    UpgradesItemStackHandler upgrades = ((CapabilityConnectableAutoIO) iConnectable).upgrades;
    for(ItemStack stack : upgrades.getStacks()) {
      UtilTileEntity.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    worldIn.updateComparatorOutputLevel(pos, this);
    super.breakBlock(worldIn, pos, state);
  }
}
