package mrriegel.storagenetwork.tile;

import mrriegel.storagenetwork.blocks.BlockAnnexer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

public class TileFannexer {//extends TileConnectable implements ITickable 
//
//	@Override
//	public void update() {
//		if (!world.isRemote && world.getTotalWorldTime() % 20 == 0 && master != null && world.getTileEntity(master) instanceof TileMaster && !world.isBlockPowered(pos)) {
//			BlockPos p = pos.offset(world.getBlockState(pos).getValue(BlockAnnexer.FACING).getOpposite());
//			IBlockState state = world.getBlockState(p);
//			if (drainBlock(state, world, p, false) == null)
//				return;
//			TileMaster mas = (TileMaster) world.getTileEntity(master);
//			FluidStack fluid = drainBlock(state, world, p, false);
//			if (mas.insertFluid(fluid, null, true) > 0)
//				return;
//			if (!mas.consumeRF(1000, false))
//				return;
//			int rest = mas.insertFluid(fluid, null, false);
//			if (rest > 0)
//				mas.frequest(fluid.getFluid(), fluid.amount - rest, false);
//			else {
//				drainBlock(state, world, p, true);
//			}
//
//		}
//	}
//
//	private FluidStack drainBlock(IBlockState state, World world, BlockPos pos, boolean doDrain) {
//		Block block = state.getBlock();
//		Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
//
//		if (fluid != null && FluidRegistry.isFluidRegistered(fluid)) {
//			if (block instanceof IFluidBlock) {
//				IFluidBlock fluidBlock = (IFluidBlock) block;
//				if (!fluidBlock.canDrain(world, pos)) {
//					return null;
//				}
//				return fluidBlock.drain(world, pos, doDrain);
//			} else {
//				int level = state.getValue(BlockLiquid.LEVEL);
//				if (level != 0) {
//					return null;
//				}
//
//				if (doDrain) {
//					world.setBlockToAir(pos);
//				}
//
//				return new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
//			}
//		} else {
//			return null;
//		}
//	}
}
