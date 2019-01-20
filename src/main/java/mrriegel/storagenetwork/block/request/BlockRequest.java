package mrriegel.storagenetwork.block.request;

import java.util.List;
import javax.annotation.Nullable;
import mrriegel.storagenetwork.CreativeTab;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.AbstractBlockConnectable;
import mrriegel.storagenetwork.api.capability.IConnectable;
import mrriegel.storagenetwork.capabilities.StorageNetworkCapabilities;
import mrriegel.storagenetwork.gui.GuiHandler;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRequest extends AbstractBlockConnectable {

  public static final PropertyDirection FACING = PropertyDirection.create("facing");

  public BlockRequest(String registryName) {
    super(Material.IRON, registryName);
    this.setHardness(3.0F);
    this.setCreativeTab(CreativeTab.tab);
    this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
  }

  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileRequest();
  }

  @Override
  public EnumBlockRenderType getRenderType(IBlockState state) {
    return EnumBlockRenderType.MODEL;
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
    EnumFacing enumfacing = EnumFacing.getFront(meta);
    if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
      enumfacing = EnumFacing.NORTH;
    }
    return this.getDefaultState().withProperty(FACING, enumfacing);
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return state.getValue(FACING).getIndex();
  }

  @Override
  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, new IProperty[] { FACING });
  }

  @Override
  public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    return this.getDefaultState().withProperty(FACING, facing.getOpposite());
  }

  @Override
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    TileEntity tileHere = worldIn.getTileEntity(pos);
    if (tileHere == null || !tileHere.hasCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null)) {
      return false;
    }
    IConnectable tile = tileHere.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null);
    if (!worldIn.isRemote && tile.getMasterPos() != null) {
      playerIn.openGui(StorageNetwork.instance, GuiHandler.GuiIDs.REQUEST.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
      return true;
    }
    return true;
  }

  @Override
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileRequest) {
      TileRequest tile = (TileRequest) tileentity;
      for (int i = 0; i < 9; i++) {
        UtilTileEntity.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), tile.matrix.get(i));
      }
    }
    super.breakBlock(worldIn, pos, state);
  }

  @Override
  public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced) {
    super.addInformation(stack, playerIn, tooltip, advanced);
    tooltip.add(I18n.format("tooltip.storagenetwork.request"));
  }
}
