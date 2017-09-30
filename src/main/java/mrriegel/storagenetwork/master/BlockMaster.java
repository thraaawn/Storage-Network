package mrriegel.storagenetwork.master;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.CreativeTab;
import mrriegel.storagenetwork.IConnectable;
import mrriegel.storagenetwork.helper.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockMaster extends BlockContainer {
  public BlockMaster() {
    super(Material.IRON);
    this.setHardness(3.0F);
    this.setCreativeTab(CreativeTab.tab);
    this.setRegistryName("master");
    this.setUnlocalizedName(getRegistryName().toString());
  }
  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileMaster();
  }
  @Override
  public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
    onBlockPlacedBy(worldIn, pos, state, null, null);
  }
  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    BlockPos mas = null;
    if (worldIn.isRemote)
      return;
    for (BlockPos p : Util.getSides(pos)) {
      if (worldIn.getTileEntity(p) instanceof IConnectable && ((IConnectable) worldIn.getTileEntity(p)).getMaster() != null && !((IConnectable) worldIn.getTileEntity(p)).getMaster().equals(pos)) {
        mas = ((IConnectable) worldIn.getTileEntity(p)).getMaster();
        break;
      }
    }
    if (mas != null) {
      worldIn.setBlockToAir(pos);
      Block.spawnAsEntity(worldIn, pos, ItemHandlerHelper.copyStackWithSize(stack, 1));
      ((TileMaster) worldIn.getTileEntity(mas)).refreshNetwork();
    }
    else {
      if (worldIn.getTileEntity(pos) != null)
        ((TileMaster) worldIn.getTileEntity(pos)).refreshNetwork();
    }
  }
  @Override
  public EnumBlockRenderType getRenderType(IBlockState state) {
    return EnumBlockRenderType.MODEL;
  }
  @Override
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (!(worldIn.getTileEntity(pos) instanceof TileMaster)) {
      return false;
    }
    TileMaster tile = (TileMaster) worldIn.getTileEntity(pos);
    if (!worldIn.isRemote) {
      playerIn.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "(Potential) Empty Slots: " + tile.emptySlots()));
      playerIn.sendMessage(new TextComponentString(TextFormatting.DARK_AQUA + "Connectables: " + tile.connectables.size()));
      Map<String, Integer> map = new HashMap<String, Integer>();
      for (BlockPos p : tile.connectables) {
        String block = worldIn.getBlockState(p).getBlock().getLocalizedName();
        map.put(block, map.get(block) != null ? (map.get(block) + 1) : 1);
      }
      List<Entry<String, Integer>> lis = Lists.newArrayList();
      for (Entry<String, Integer> e : map.entrySet()) {
        lis.add(e);
      }
      Collections.sort(lis, new Comparator<Entry<String, Integer>>() {
        @Override
        public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
          return Integer.compare(o2.getValue(), o1.getValue());
        }
      });
      for (Entry<String, Integer> e : lis)
        playerIn.sendMessage(new TextComponentString(TextFormatting.AQUA + "    " + e.getKey() + ": " + e.getValue()));
      return false;
    }
    return true;
  }
  public static class Item extends ItemBlock {
    public Item(Block block) {
      super(block);
    }
    @Override
    public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced) {
      super.addInformation(stack, playerIn, tooltip, advanced);
      tooltip.add(I18n.format("tooltip.storagenetwork.master"));
      //			if (ConfigHandler.energyNeeded)
      //				tooltip.add(I18n.format("tooltip.storagenetwork.RFNeeded"));
    }
  }
}
