package mrriegel.storagenetwork.block.cable.processing;

import mrriegel.storagenetwork.block.cable.BlockCableWithFacing;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockCableProcessing extends BlockCableWithFacing {
  public BlockCableProcessing(String registryName) {
    super(registryName);
  }

  @Nullable
  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileCableProcess();
  }
}
