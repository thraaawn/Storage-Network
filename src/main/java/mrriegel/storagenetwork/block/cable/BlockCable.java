package mrriegel.storagenetwork.block.cable;

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
import mrriegel.storagenetwork.util.data.EnumConnectType;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
    return UtilInventory.hasItemHandler(worldIn, pos, side);
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
    if (tile.getBlockType() != ModBlocks.kabel) {
      playerIn.openGui(StorageNetwork.instance, GuiHandler.CABLE, worldIn, pos.getX(), pos.getY(), pos.getZ());
      return true;
    }
    return false;
  }

  @Override
  public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
    StorageNetwork.log("Rotateblock");
    setConnections(world, pos, world.getBlockState(pos), true);
    //    TileEntity myselfTile = world.getTileEntity(pos);
    //    if (myselfTile == null || myselfTile instanceof TileCable == false) {
    //      return false;
    //    }
    //    TileCable cable = (TileCable) myselfTile;
    //    Map<EnumFacing, EnumConnectType> oldMap = cable.getConnects();
    //    Map<EnumFacing, EnumConnectType> newMap = Maps.newHashMap();
    //    EnumFacing next = EnumFacing.random(world.rand);
    //    for (Entry<EnumFacing, EnumConnectType> e : oldMap.entrySet()) {
    //      if (e.getValue() == EnumConnectType.STORAGE) {
    //        EnumFacing stor = e.getKey();
    //        break;
    //      }
    //    }
    //    cable.setConnects(newMap);
    //    if (cable.getInventoryFace() != null) {
    //      cable.setInventoryFace(EnumFacing.random(world.rand));
    //    }
    return super.rotateBlock(world, pos, axis);
  }

  @Override
  public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
    //    setConnections(worldIn, pos, state, false);
    StorageNetwork.log("getStateForPlacement :" + facing.toString());
    //first use facing and try to set storage connection on that

    setConnections(world, pos, world.getBlockState(pos), false);
    return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
  }
  //
  //  @Override
  //  public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
  //    StorageNetwork.log("onBlockPlacedBy");
    //possible bandaid to stop double connects but.. seems to expensive. for an only-visual issue
    // https://github.com/PrinceOfAmber/Storage-Network/issues/84
    //    detectLocalMasterNode(worldIn, pos);
    //    if (worldIn.isRemote) {
    //      return;
    //    }
    //    TileCable here = (TileCable) worldIn.getTileEntity(pos);
    //
    //    StorageNetwork.log("!validate cable getConnectedInventory " + here.getConnectedInventory());
    //    StorageNetwork.log("!validate cable getMaster " + here.getMaster());
    //    if (here.isStorage() && here.getConnectedInventory() != null && here.getMaster() != null) {
    //      TileMaster master = (TileMaster) worldIn.getTileEntity(here.getMaster());
    //      if (master == null || master.getConnectables() == null) {
    //        return;
    //      }
    //      //check 
    //      for (BlockPos pCon : master.getConnectables()) {
    //        if (pCon.equals(pos)) {
    //          continue;//not myself 
    //        }
    //        TileEntity tileCon = worldIn.getTileEntity(pCon);
    //        if (tileCon != null && tileCon instanceof TileCable) {
    //          TileCable tileOnSameBlock = (TileCable) tileCon;
    //          if (tileOnSameBlock.getConnectedInventory() != null &&
    //              tileOnSameBlock.getConnectedInventory().equals(here.getConnectedInventory())) {
    //            StorageNetwork.log("found doublo ");
    //THIS IS WHERE  I WOULD DELETE MYSELF TO AIR AND DROP AS ITEM 
    //          }
    //        }
    //      }
    //    }
  //  }



  public TileCable getTileCableOrNull(IBlockAccess world, BlockPos pos) {
    TileEntity tileHere = world.getTileEntity(pos);
    if (tileHere instanceof TileCable)
      return (TileCable) tileHere;
    return null;
  }

  // List<EnumFacing> facingShuffled = Arrays.asList(EnumFacing.values());
  //    facingShuffled.addAll(oldMap.keySet());
  //   Collections.shuffle(facingShuffled);
  //    for (EnumFacing facing : facingShuffled) {
  //      if (oldMap.get(facing) == EnumConnectType.STORAGE) {
  //        stor = facing;
  //        break;
  //      }
  //    }
  /**
   * What direction is my storage inventory that im connected to?
   * 
   * @param tile
   * @return facing nullable
   */
  private EnumFacing getConFacingByType(@Nonnull TileCable tile, EnumConnectType connectType) {
    Map<EnumFacing, EnumConnectType> previousConnectionMap = tile.getConnects();
    for (Entry<EnumFacing, EnumConnectType> e : previousConnectionMap.entrySet()) {
      if (e.getValue() == connectType) {
        return e.getKey();
      }
    }
    return null;
  }

  private Map<EnumFacing, EnumConnectType> getConnectionsAllowed(World world, BlockPos pos) {
    Map<EnumFacing, EnumConnectType> newMap = Maps.newHashMap();
    for (EnumFacing facing : EnumFacing.values()) {
      //save all directions. order doesnt matter
      EnumConnectType connectType = getConnectionTypeBetween(world, pos, pos.offset(facing));
      newMap.put(facing, connectType);
    }
    return newMap;
  }

  private IBlockState getNewState(IBlockAccess world, BlockPos pos) {
    //    StorageNetwork.log("getNewState");
    TileCable tile = getTileCableOrNull(world, pos);
    if (tile == null) {
      return world.getBlockState(pos);
    }
    //  EnumFacing previousFacing = tile.getInventoryFace();
    BlockPos con = null;
    Map<EnumFacing, EnumConnectType> newMap = Maps.newHashMap();
    EnumFacing facingStorage = getConFacingByType(tile, EnumConnectType.STORAGE);
    EnumFacing face = null;
    boolean storage = false;
    boolean first = false;
    //fill in newMap based on current storage connection
    if (facingStorage != null && getConnectionTypeBetween(world, pos, pos.offset(facingStorage)) == EnumConnectType.STORAGE) {
      newMap.put(facingStorage, EnumConnectType.STORAGE);
      storage = true;
      first = true;
    }
    //look in all directions // shuffle here??
    // and set newmap to type based on what block lives there
    for (EnumFacing facing : EnumFacing.values()) {
      if (facingStorage == facing && first) {
        continue;
      }
      //what connection type is possible here before i save it (conn, null, str)
      EnumConnectType connectType = getConnectionTypeBetween(world, pos, pos.offset(facing));

      if (connectType == EnumConnectType.STORAGE) {
        //make sure it only picks ONE storage connection to main
        if (!storage) {
          newMap.put(facing, connectType);
          storage = true;
        }
        else {
          //replace storage with null
          newMap.put(facing, EnumConnectType.NULL);
        }
      }
      else {//just save it
        newMap.put(facing, connectType);
      }
    }
    tile.setConnects(newMap);
    if (tile.north == EnumConnectType.STORAGE) {
      face = EnumFacing.NORTH;
      con = pos.north();
    }
    else if (tile.south == EnumConnectType.STORAGE) {
      face = EnumFacing.SOUTH;
      con = pos.south();
    }
    else if (tile.east == EnumConnectType.STORAGE) {
      face = EnumFacing.EAST;
      con = pos.east();
    }
    else if (tile.west == EnumConnectType.STORAGE) {
      face = EnumFacing.WEST;
      con = pos.west();
    }
    else if (tile.down == EnumConnectType.STORAGE) {
      face = EnumFacing.DOWN;
      con = pos.down();
    }
    else if (tile.up == EnumConnectType.STORAGE) {
      face = EnumFacing.UP;
      con = pos.up();
    }
    tile.setInventoryFace(face);
    tile.setConnectedInventory(con);
    return world.getBlockState(pos);
  }

  @Override
  public void setConnections(World worldIn, BlockPos pos, IBlockState state, boolean refresh) {
    state = getNewState(worldIn, pos);
    super.setConnections(worldIn, pos, state, refresh);
    if (refresh) {
      UtilTileEntity.updateTile(worldIn, pos);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    try {
      IBlockState foo = getNewState(worldIn, pos);
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
    if (tile.north != EnumConnectType.NULL) {
      f2 = 0f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (tile.south != EnumConnectType.NULL) {
      f3 = 1f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (tile.west != EnumConnectType.NULL) {
      f = 0f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (tile.east != EnumConnectType.NULL) {
      f1 = 1f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (tile.down != EnumConnectType.NULL) {
      f4 = 0f;
      addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(f, f4, f2, f1, f5, f3));
    }
    if (tile.up != EnumConnectType.NULL) {
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
    if (tile.north != EnumConnectType.NULL) {
      y1 = 0f;
    }
    if (tile.south != EnumConnectType.NULL) {
      y2 = 1f;
    }
    if (tile.west != EnumConnectType.NULL) {
      x1 = 0f;
    }
    if (tile.east != EnumConnectType.NULL) {
      x2 = 1f;
    }
    if (tile.down != EnumConnectType.NULL) {
      z1 = 0f;
    }
    if (tile.up != EnumConnectType.NULL) {
      z2 = 1f;
    }
    return new AxisAlignedBB(x1, z1, y1, x2, z2, y2);
  }

  protected EnumConnectType getConnectionTypeBetween(IBlockAccess world, BlockPos posTarget, BlockPos posHere) {
    TileEntity tileHere = world.getTileEntity(posHere);
    Block targetBlock = world.getBlockState(posTarget).getBlock();
    if (tileHere instanceof IConnectable || tileHere instanceof TileMaster) {
      return EnumConnectType.CONNECT;
    }
    if (targetBlock == ModBlocks.kabel) {
      return EnumConnectType.NULL;
    }
    EnumFacing face = getFacingBetween(posTarget, posHere);
    if (!validInventory(world, posHere, face)) {
      return EnumConnectType.NULL;
    }
    return EnumConnectType.STORAGE;
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
  }
}
