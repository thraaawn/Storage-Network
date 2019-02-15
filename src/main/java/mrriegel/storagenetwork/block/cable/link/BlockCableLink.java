package mrriegel.storagenetwork.block.cable.link;

import mrriegel.storagenetwork.block.cable.BlockCableWithFacing;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockCableLink extends BlockCableWithFacing {
  public BlockCableLink(String registryName) {
    super(registryName);
  }

  @Nullable
  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileCableLink();
  }
}
