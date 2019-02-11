package mrriegel.storagenetwork.block.linkplain;

import javax.annotation.Nullable;
import mrriegel.storagenetwork.block.cable.BlockCableWithFacing;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockCableLinkPlain extends BlockCableWithFacing {

  public BlockCableLinkPlain(String registryName) {
    super(registryName);
  }

  @Nullable
  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileCableLinkPlain();
  }
}
