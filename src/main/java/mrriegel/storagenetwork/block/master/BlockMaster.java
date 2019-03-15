package mrriegel.storagenetwork.block.master;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.CreativeTab;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.api.capability.IConnectable;
import mrriegel.storagenetwork.api.data.DimPos;
import mrriegel.storagenetwork.block.BaseBlock;
import mrriegel.storagenetwork.capabilities.StorageNetworkCapabilities;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class BlockMaster extends BaseBlock {

  public BlockMaster(String registryName) {
    super(Material.IRON, registryName);
    this.setHardness(3.0F);
    this.setCreativeTab(CreativeTab.tab);
  }

  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileMaster();
  }

  @Override
  public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
    //onBlockPlacedBy(worldIn, pos, state, null, null);
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    DimPos masterPos = null;
    if (worldIn.isRemote) {
      return;
    }
    TileEntity tileHere = null;
    IConnectable connect = null;
    for (BlockPos p : UtilTileEntity.getSides(pos)) {
      tileHere = worldIn.getTileEntity(p);
      if (tileHere != null && tileHere.hasCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null)) {
        connect = tileHere.getCapability(StorageNetworkCapabilities.CONNECTABLE_CAPABILITY, null);
        if (connect != null && connect.getMasterPos() != null && connect.getMasterPos().equals(worldIn, pos)) {
          masterPos = connect.getMasterPos();
          break;
        }
      }
    }
    if (masterPos != null) {
      // we found an existing master on the network, cannot add a new one. so break this one
      //    TileMaster tileMaster = (TileMaster) worldIn.getTileEntity(masterPos);
      worldIn.setBlockToAir(pos);
      Block.spawnAsEntity(worldIn, pos, ItemHandlerHelper.copyStackWithSize(stack, 1));
      //      ((TileMaster) worldIn.getTileEntity(masterPos)).refreshNetwork();
    }
    else {//my position is tile so refresh myself
      TileEntity tileAtPos = worldIn.getTileEntity(pos);
      if (tileAtPos != null) {
        ((TileMaster) tileAtPos).refreshNetwork();
      }
    }
  }

  @Override
  public EnumBlockRenderType getRenderType(IBlockState state) {
    return EnumBlockRenderType.MODEL;
  }

  @Override
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (worldIn.isRemote) {
      return true;
    }
    TileEntity tileHere = worldIn.getTileEntity(pos);
    if (!(tileHere instanceof TileMaster)) {
      return false;
    }
    TileMaster tileMaster = (TileMaster) tileHere;
    playerIn.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + StorageNetwork.lang("chat.master.emptyslots") + tileMaster.emptySlots()));
    playerIn.sendMessage(new TextComponentString(TextFormatting.DARK_AQUA + StorageNetwork.lang("chat.master.connectables") + tileMaster.getConnectablePositions().size()));
    Map<String, Integer> mapNamesToCount = new HashMap<String, Integer>();
    Iterator<DimPos> iter = tileMaster.getConnectablePositions().iterator();
    while (iter.hasNext()) {
      final DimPos p = iter.next();

      String block = p.getBlockState().getBlock().getLocalizedName();
      mapNamesToCount.put(block, mapNamesToCount.get(block) != null ? (mapNamesToCount.get(block) + 1) : 1);
    }
    List<Entry<String, Integer>> listDisplayStrings = Lists.newArrayList();
    for (Entry<String, Integer> e : mapNamesToCount.entrySet()) {
      listDisplayStrings.add(e);
    }
    Collections.sort(listDisplayStrings, new Comparator<Entry<String, Integer>>() {

      @Override
      public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
        return Integer.compare(o2.getValue(), o1.getValue());
      }
    });
    for (Entry<String, Integer> e : listDisplayStrings) {
      playerIn.sendMessage(new TextComponentString(TextFormatting.AQUA + "    " + e.getKey() + ": " + e.getValue()));
    }
    return false;
  }

  @Override
  public void addInformation(ItemStack stack, @Nullable World playerIn, List<String> tooltip, ITooltipFlag advanced) {
    super.addInformation(stack, playerIn, tooltip, advanced);
    tooltip.add(I18n.format("tooltip.storagenetwork.master"));
  }
}
