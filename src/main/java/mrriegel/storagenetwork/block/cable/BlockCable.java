package mrriegel.storagenetwork.block.cable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.Maps;
import mrriegel.storagenetwork.CreativeTab;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.AbstractBlockConnectable;
import mrriegel.storagenetwork.block.IConnectable;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.gui.GuiHandler;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.util.UtilInventory;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCable extends AbstractBlockConnectable {

  public BlockCable() {
    super(Material.ROCK);
    this.setHardness(1.4F);
    this.setCreativeTab(CreativeTab.tab);
  }

  @Override
  public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
    return false;
  }

  @Override
  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }

  @Override
  public BlockFaceShape getBlockFaceShape(IBlockAccess p_193383_1_, IBlockState p_193383_2_, BlockPos p_193383_3_, EnumFacing p_193383_4_) {
    return BlockFaceShape.MIDDLE_POLE_THIN;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean isTranslucent(IBlockState state) {
    return true;
  }

  @Override
  public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
    return false;
  }

  @Override
  public boolean isFullCube(IBlockState state) {
    return false;
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return 0;
  }

  boolean validInventory(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
    return UtilInventory.hasItemHandler(worldIn, pos, side)
        && TileMaster.isTargetAllowed(worldIn, pos);
  }

  @Override
  public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
    return layer == BlockRenderLayer.SOLID;
  }

  @Override
  public EnumBlockRenderType getRenderType(IBlockState state) {
    return EnumBlockRenderType.INVISIBLE;
  }

  @Override
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    TileEntity tile = worldIn.getTileEntity(pos);
    if (tile instanceof TileCable == false) {
      return false;
    }
    if (worldIn.isRemote) {
      return true;
    }
    //if process - do own gui!
    if (tile.getBlockType() != ModBlocks.kabel) {
      playerIn.openGui(StorageNetwork.instance, GuiHandler.CABLE, worldIn, pos.getX(), pos.getY(), pos.getZ());
      return true;
    }
    return false;
  }

  public TileCable getTileCableOrNull(IBlockAccess world, BlockPos pos) {
    TileEntity tileHere = world.getTileEntity(pos);
    if (tileHere instanceof TileCable)
      return (TileCable) tileHere;
    return null;
  }

  /**
   * What direction is my storage inventory that im connected to?
   * 
   * @param tile
   * @return facing nullable
   */
  private EnumFacing getConFacingByType(@Nonnull TileCable tile, EnumCableType connectType) {
    Map<EnumFacing, EnumCableType> previousConnectionMap = tile.getConnects();
    for (Entry<EnumFacing, EnumCableType> e : previousConnectionMap.entrySet()) {
      if (e.getValue() == connectType) {
        return e.getKey();
      }
    }
    return null;
  }
  //
  //  private Map<EnumFacing, EnumConnectType> getConnectionsAllowed(World world, BlockPos pos) {
  //    Map<EnumFacing, EnumConnectType> newMap = Maps.newHashMap();
  //    for (EnumFacing facing : EnumFacing.values()) {
  //      //save all directions. order doesnt matter
  //      EnumConnectType connectType = getConnectionTypeBetween(world, pos, pos.offset(facing));
  //      newMap.put(facing, connectType);
  //    }
  //    return newMap;
  //  }

  //TODO: connect to the one getting hit not anything else
  //problem: tile is null
  //  @Override
  //  public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
  //    //    setConnections(worldIn, pos, state, false);
  //    TileCable here = this.getTileCableOrNull(world, pos);
  //    StorageNetwork.log("getStateForPlacement :" + facing.toString() + " is tileCable null? " + here);
  //    //YES tile is null
  //    //first use facing and try to set storage connection on that
  //    //if we cant then set old wayyeah
  //    setConnections(world, pos, world.getBlockState(pos), false);
  //    return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
  //  }
  @Override
  public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
    TileCable tile = getTileCableOrNull(world, pos);
    if (tile != null) {
      //wipe previous connection
      tile.setInventoryFace(null);
      tile.setConnectedInventory(null);
      Map<EnumFacing, EnumCableType> newMap = Maps.newHashMap();
      tile.setConnects(newMap);
    }
    //then find new connection in different order using shuffle
    IBlockState state = world.getBlockState(pos);
    List<EnumFacing> shuffled = Arrays.asList(EnumFacing.values());
    //we could pick smart order instead of shuffle here
    Collections.shuffle(shuffled);
    state = getNewState(world, pos, shuffled);
    super.setConnections(world, pos, state, true);
    UtilTileEntity.updateTile(world, pos);
    return super.rotateBlock(world, pos, axis);
  }

  /**
   * called by getActualState and setConnections
   * 
   * @param world
   * @param pos
   * @return
   */
  private IBlockState getNewState(IBlockAccess world, BlockPos pos, List<EnumFacing> facingOrder) {
    TileCable tile = getTileCableOrNull(world, pos);
    if (tile == null) {
      return world.getBlockState(pos);
    }
    BlockPos con = null;
    Map<EnumFacing, EnumCableType> newMap = Maps.newHashMap();
    EnumFacing facingStorage = getConFacingByType(tile, EnumCableType.STORAGE);
    EnumFacing face = null;
    boolean storage = false;
    boolean first = false;
    //fill in newMap based on current storage connection
    if (facingStorage != null && getConnectionTypeBetween(world, pos, pos.offset(facingStorage)) == EnumCableType.STORAGE) {
      newMap.put(facingStorage, EnumCableType.STORAGE);
      storage = true;
      first = true;
    }
    //look in all directions // shuffle here??
    // and set newmap to type based on what block lives there
    for (EnumFacing facing : facingOrder) {
      if (facingStorage == facing && first) {
        continue;
      }
      //what connection type is possible here before i save it (conn, null, str)
      EnumCableType connectType = getConnectionTypeBetween(world, pos, pos.offset(facing));
      if (connectType == EnumCableType.STORAGE) {
        //make sure it only picks ONE storage connection to main
        if (!storage) {
          newMap.put(facing, connectType);
          storage = true;
        }
        else {
          //replace storage with null
          newMap.put(facing, EnumCableType.NULL);
        }
      }
      else {//just save it
        newMap.put(facing, connectType);
      }
    }
    tile.setConnects(newMap);
    if (tile.north == EnumCableType.STORAGE) {
      face = EnumFacing.NORTH;
      con = pos.north();
    }
    else if (tile.south == EnumCableType.STORAGE) {
      face = EnumFacing.SOUTH;
      con = pos.south();
    }
    else if (tile.east == EnumCableType.STORAGE) {
      face = EnumFacing.EAST;
      con = pos.east();
    }
    else if (tile.west == EnumCableType.STORAGE) {
      face = EnumFacing.WEST;
      con = pos.west();
    }
    else if (tile.down == EnumCableType.STORAGE) {
      face = EnumFacing.DOWN;
      con = pos.down();
    }
    else if (tile.up == EnumCableType.STORAGE) {
      face = EnumFacing.UP;
      con = pos.up();
    }
    tile.setInventoryFace(face);
    tile.setConnectedInventory(con);
    return world.getBlockState(pos);
  }

  @Override
  public void setConnections(World worldIn, BlockPos pos, IBlockState state, boolean refresh) {
    state = getNewState(worldIn, pos, Arrays.asList(EnumFacing.values()));
    super.setConnections(worldIn, pos, state, refresh);
    if (refresh) {
      UtilTileEntity.updateTile(worldIn, pos);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    try {
      IBlockState foo = getNewState(worldIn, pos, Arrays.asList(EnumFacing.values()));
      return foo;
    }
    catch (Exception e) {
      e.printStackTrace();
      return super.getActualState(state, worldIn, pos);
    }
  }

  public static EnumFacing getFacingBetween(BlockPos too, BlockPos froom) {
    if (too.up().equals(froom))
      return EnumFacing.DOWN;
    if (too.down().equals(froom))
      return EnumFacing.UP;
    if (too.west().equals(froom))
      return EnumFacing.EAST;
    if (too.east().equals(froom))
      return EnumFacing.WEST;
    if (too.north().equals(froom))
      return EnumFacing.SOUTH;
    if (too.south().equals(froom))
      return EnumFacing.NORTH;
    return null;
  }

  @Override
  public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
    TileEntity tileHere = worldIn.getTileEntity(pos);
    if (!(tileHere instanceof TileCable)) {
      return;
    }
    state = state.getActualState(worldIn, pos);
    TileCable tile = (TileCable) tileHere;
    float f = 0.3125F;
    float f1 = 0.6875F;
    float f2 = 0.3125F;
    float f3 = 0.6875F;
    float f4 = 0.3125F;
    float f5 = 0.6875F;
    addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    if (tile.north != EnumCableType.NULL) {
      f2 = 0f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (tile.south != EnumCableType.NULL) {
      f3 = 1f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (tile.west != EnumCableType.NULL) {
      f = 0f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (tile.east != EnumCableType.NULL) {
      f1 = 1f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (tile.down != EnumCableType.NULL) {
      f4 = 0f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (tile.up != EnumCableType.NULL) {
      f5 = 1f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
  }

  @Override
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
    TileEntity tileHere = world.getTileEntity(pos);
    if (tileHere == null || !(tileHere instanceof TileCable)) {
      return FULL_BLOCK_AABB;
    }
    state = state.getActualState(world, pos);
    TileCable tile = (TileCable) tileHere;
    float x1 = 0.37F;
    float x2 = 0.63F;
    float y1 = 0.37F;
    float y2 = 0.63F;
    float z1 = 0.37F;
    float z2 = 0.63F;
    if (tile.north != EnumCableType.NULL) {
      y1 = 0f;
    }
    if (tile.south != EnumCableType.NULL) {
      y2 = 1f;
    }
    if (tile.west != EnumCableType.NULL) {
      x1 = 0f;
    }
    if (tile.east != EnumCableType.NULL) {
      x2 = 1f;
    }
    if (tile.down != EnumCableType.NULL) {
      z1 = 0f;
    }
    if (tile.up != EnumCableType.NULL) {
      z2 = 1f;
    }
    return new AxisAlignedBB(x1, z1, y1, x2, z2, y2);
  }

  protected EnumCableType getConnectionTypeBetween(IBlockAccess world, BlockPos posTarget, BlockPos posHere) {
    TileEntity tileHere = world.getTileEntity(posHere);
    Block targetBlock = world.getBlockState(posTarget).getBlock();
    if (tileHere instanceof IConnectable || tileHere instanceof TileMaster) {
      return EnumCableType.CONNECT;
    }
    if (targetBlock == ModBlocks.kabel) {
      return EnumCableType.NULL;
    }
    EnumFacing face = getFacingBetween(posTarget, posHere);
    if (!validInventory(world, posHere, face)) {
      return EnumCableType.NULL;
    }
    return EnumCableType.STORAGE;
  }

  @Override
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    if (tileentity instanceof TileCable) {
      TileCable tile = (TileCable) tileentity;
      for (int i = 0; i < tile.getUpgrades().size(); i++) {
        UtilTileEntity.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), tile.getUpgrades().get(i));
      }
    }
    worldIn.updateComparatorOutputLevel(pos, this);
    super.breakBlock(worldIn, pos, state);
  }

  @Override
  public TileEntity createNewTileEntity(World worldIn, int meta) {
    return new TileCable();
  }

  @Override
  public void addInformation(ItemStack stack, World playerIn, List<String> tooltip, ITooltipFlag advanced) {
    super.addInformation(stack, playerIn, tooltip, advanced);
    if (stack.getItem() == Item.getItemFromBlock(ModBlocks.exKabel))
      tooltip.add(I18n.format("tooltip.storagenetwork.kabel_E"));
    else if (stack.getItem() == Item.getItemFromBlock(ModBlocks.imKabel))
      tooltip.add(I18n.format("tooltip.storagenetwork.kabel_I"));
    else if (stack.getItem() == Item.getItemFromBlock(ModBlocks.storageKabel))
      tooltip.add(I18n.format("tooltip.storagenetwork.kabel_S"));
    else if (stack.getItem() == Item.getItemFromBlock(ModBlocks.kabel))
      tooltip.add(I18n.format("tooltip.storagenetwork.kabel_L"));
    else if (stack.getItem() == Item.getItemFromBlock(ModBlocks.processKabel))
      tooltip.add(I18n.format("tooltip.storagenetwork.kabel_P"));
  }
}
