package mrriegel.storagenetwork.block.cable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class BlockCableWithFacing extends BlockCable {

  public BlockCableWithFacing(String registryName) {
    super(registryName);
  }

  @Override
  public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
    TileEntity tile = world.getTileEntity(pos);
    if(!(tile instanceof TileCableWithFacing)) {
      return super.rotateBlock(world, pos, axis);
    }

    TileCableWithFacing facingTile = (TileCableWithFacing)tile;
    facingTile.rotate();

    return true;
  }

  @Override
  public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    super.onBlockPlacedBy(world, pos, state, placer, stack);

    if(world.isRemote) {
      return;
    }

    TileEntity tile = world.getTileEntity(pos);
    if(!(tile instanceof TileCableWithFacing)) {
      return;
    }

    TileCableWithFacing facingTile = (TileCableWithFacing)tile;
    facingTile.findNewDirection();
    facingTile.markDirty();
  }

  @Override
  public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
    super.onNeighborChange(world, pos, neighbor);

    if(!(world instanceof WorldServer)) {
      return;
    }

    WorldServer worldServer = (WorldServer)world;

    TileEntity tile = world.getTileEntity(pos);
    if(!(tile instanceof TileCableWithFacing)) {
      return;
    }

    TileCableWithFacing facingTile = (TileCableWithFacing)tile;
    facingTile.findNewDirection();
    //facingTile.markDirty();

    worldServer.markChunkDirty(pos, facingTile);
    worldServer.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
  }

  @Override
  protected UnlistedPropertyBlockNeighbors.BlockNeighbors getBlockNeighbors(IBlockAccess world, BlockPos pos) {
    UnlistedPropertyBlockNeighbors.BlockNeighbors result = super.getBlockNeighbors(world, pos);
    TileEntity tile = world.getTileEntity(pos);
    if(!(tile instanceof TileCableWithFacing)) {
      return result;
    }

    TileCableWithFacing facingTile = (TileCableWithFacing)tile;
    if(facingTile.hasDirection()) {
      result.setNeighborType(facingTile.getDirection(), UnlistedPropertyBlockNeighbors.EnumNeighborType.SPECIAL);
    }

    return result;
  }
}
