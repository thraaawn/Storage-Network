package mrriegel.storagenetwork.block.cable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import mrriegel.storagenetwork.block.AbstractFilterTile;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.registry.ModItems;
import mrriegel.storagenetwork.util.UtilInventory;
import mrriegel.storagenetwork.util.data.FilterItem;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public class TileCable extends AbstractFilterTile implements IInventory {

  private BlockPos connectedInventory;
  private EnumFacing inventoryFace;
  private NonNullList<ItemStack> upgrades = NonNullList.withSize(ContainerCable.UPGRADE_COUNT, ItemStack.EMPTY);
  private boolean mode = true;
  private int limit = 0;
  public EnumCableType north, south, east, west, up, down;
  private ItemStack stack = ItemStack.EMPTY;

  public static enum Fields {
    STATUS, FACINGTOPROW, FACINGBOTTOMROW;
  }

  public TileCable() {
    this.setOres(false);
    this.setMeta(true);
  }

  public int getUpgradesOfType(int num) {
    int res = 0;
    for (ItemStack s : upgrades) {
      if (s != null && !s.isEmpty() && s.getItemDamage() == num) {
        res += s.getCount();
      }
    }
    return res;
  }

  public boolean isUpgradeable() {
    return this.blockType == ModBlocks.imKabel ||
        this.blockType == ModBlocks.exKabel;
  }

  public Map<EnumFacing, EnumCableType> getConnects() {
    Map<EnumFacing, EnumCableType> map = Maps.newHashMap();
    map.put(EnumFacing.NORTH, north);
    map.put(EnumFacing.SOUTH, south);
    map.put(EnumFacing.EAST, east);
    map.put(EnumFacing.WEST, west);
    map.put(EnumFacing.UP, up);
    map.put(EnumFacing.DOWN, down);
    return map;
  }

  public void setConnects(Map<EnumFacing, EnumCableType> map) {
    north = map.get(EnumFacing.NORTH);
    south = map.get(EnumFacing.SOUTH);
    east = map.get(EnumFacing.EAST);
    west = map.get(EnumFacing.WEST);
    up = map.get(EnumFacing.UP);
    down = map.get(EnumFacing.DOWN);
  }

  public boolean doesPassOperationFilterLimit() {
    if (getUpgradesOfType(ItemUpgrade.OPERATION) < 1) {
      return true;
    }
    //ok operation upgrade does NOT exist
    TileMaster m = (TileMaster) world.getTileEntity(getMaster());
    if (getOperationStack() == null || getOperationStack().isEmpty()) {
      return true;
    }
    int amount = m.getAmount(new FilterItem(getOperationStack()));
    if (isMode()) {
      return amount > getLimit();
    }
    else {
      return amount <= getLimit();
    }
  }

  @SuppressWarnings("serial")
  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);

    connectedInventory = new Gson().fromJson(compound.getString("connectedInventory"), new TypeToken<BlockPos>() {}.getType());
    inventoryFace = EnumFacing.byName(compound.getString("inventoryFace"));
    mode = compound.getBoolean("mode");
    limit = compound.getInteger("limit");
    if (compound.hasKey("stack", 10))
      stack = new ItemStack(compound.getCompoundTag("stack"));
    else
      stack = ItemStack.EMPTY;
    if (compound.hasKey("north"))
      north = EnumCableType.valueOf(compound.getString("north"));
    if (compound.hasKey("south"))
      south = EnumCableType.valueOf(compound.getString("south"));
    if (compound.hasKey("east"))
      east = EnumCableType.valueOf(compound.getString("east"));
    if (compound.hasKey("west"))
      west = EnumCableType.valueOf(compound.getString("west"));
    if (compound.hasKey("up"))
      up = EnumCableType.valueOf(compound.getString("up"));
    if (compound.hasKey("down"))
      down = EnumCableType.valueOf(compound.getString("down"));
    NBTTagList nbttaglist = compound.getTagList("Items", 10);
    upgrades = NonNullList.withSize(4, ItemStack.EMPTY);
    for (int i = 0; i < nbttaglist.tagCount(); ++i) {
      NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
      int j = nbttagcompound.getByte("Slot") & 255;
      if (j >= 0 && j < 4) {// TODO: 4 const reference
        upgrades.set(j, new ItemStack(nbttagcompound));
      }
    }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);
    compound.setString("connectedInventory", new Gson().toJson(connectedInventory));
    if (inventoryFace != null)
      compound.setString("inventoryFace", inventoryFace.toString());
    compound.setBoolean("mode", mode);
    compound.setInteger("limit", limit);
    if (!stack.isEmpty())
      compound.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
    if (north != null)
      compound.setString("north", north.toString());
    if (south != null)
      compound.setString("south", south.toString());
    if (east != null)
      compound.setString("east", east.toString());
    if (west != null)
      compound.setString("west", west.toString());
    if (up != null)
      compound.setString("up", up.toString());
    if (down != null)
      compound.setString("down", down.toString());
    NBTTagList nbttaglist = new NBTTagList();
    for (int i = 0; i < upgrades.size(); ++i) {
      if (upgrades.get(i) != null) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setByte("Slot", (byte) i);
        upgrades.get(i).writeToNBT(nbttagcompound);
        nbttaglist.appendTag(nbttagcompound);
      }
    }
    compound.setTag("Items", nbttaglist);
    return compound;
  }

  @Override
  public AxisAlignedBB getRenderBoundingBox() {
    double renderExtention = 1.0d;
    AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - renderExtention, pos.getY() - renderExtention, pos.getZ() - renderExtention, pos.getX() + 1 + renderExtention, pos.getY() + 1 + renderExtention, pos.getZ() + 1 + renderExtention);
    return bb;
  }

  public BlockPos getConnectedInventory() {
    return connectedInventory;
  }

  public void setConnectedInventory(BlockPos connectedInventory) {
    this.connectedInventory = connectedInventory;
  }

  public EnumFacing getInventoryFace() {
    return inventoryFace;
  }

  public void setInventoryFace(EnumFacing inventoryFace) {
    this.inventoryFace = inventoryFace;
  }

  public NonNullList<ItemStack> getUpgrades() {
    return upgrades;
  }

  public boolean isMode() {
    return mode;
  }

  public void setMode(boolean mode) {
    this.mode = mode;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public ItemStack getOperationStack() {
    return stack;
  }

  public void setOperationStack(ItemStack stack) {
    this.stack = stack;
  }

  @Override
  public IItemHandler getInventory() {
    if (getConnectedInventory() != null)
      return UtilInventory.getItemHandler(world.getTileEntity(getConnectedInventory()), inventoryFace.getOpposite());
    return null;
  }

  @Override
  public BlockPos getSource() {
    return getConnectedInventory();
  }

  @Override
  public boolean isStorage() {
    return this.getBlockType() == ModBlocks.storageKabel;
  }

  @Override
  public String getName() {
    return blockType.getUnlocalizedName();
  }

  @Override
  public boolean hasCustomName() {
    return false;
  }

  @Override
  public int getSizeInventory() {
    return ContainerCable.UPGRADE_COUNT;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public ItemStack getStackInSlot(int index) {
    return upgrades.get(index);
  }

  @Override
  public ItemStack decrStackSize(int index, int count) {
    ItemStack stack = getStackInSlot(index);
    if (!stack.isEmpty()) {
      if (stack.getMaxStackSize() <= count) {
        setInventorySlotContents(index, ItemStack.EMPTY);
      }
      else {
        stack = stack.splitStack(count);
        if (stack.getMaxStackSize() == 0) {
          setInventorySlotContents(index, ItemStack.EMPTY);
        }
      }
    }
    return stack;
  }

  @Override
  public ItemStack removeStackFromSlot(int index) {
    ItemStack stack = getStackInSlot(index);
    setInventorySlotContents(index, ItemStack.EMPTY);
    return stack;
  }

  @Override
  public void setInventorySlotContents(int index, ItemStack stack) {
    if (stack.getItem() == ModItems.upgrade)
      upgrades.set(index, stack);
  }

  @Override
  public int getInventoryStackLimit() {
    return 1;
  }

  @Override
  public boolean isUsableByPlayer(EntityPlayer player) {
    return true;
  }

  @Override
  public void openInventory(EntityPlayer player) {}

  @Override
  public void closeInventory(EntityPlayer player) {}

  @Override
  public boolean isItemValidForSlot(int index, ItemStack stack) {
    return false;// this is to stop hoppers/etc getting them in
    // stack.getItem() == ModItems.upgrade ;
  }

  @Override
  public int getField(int id) {

    return 0;
  }

  @Override
  public void setField(int id, int value) {


  }

  @Override
  public void clear() {}

  public List<StackWrapper> getFilterTop() {
    Map<Integer, StackWrapper> flt = super.getFilter();
    List<StackWrapper> half = new ArrayList<>();
    for (Integer i : flt.keySet()) {
      if (i <= 8 && flt.get(i) != null && flt.get(i).getStack().isEmpty() == false) {
        half.add(flt.get(i));
      }
    }
    return half;
  }

  public List<StackWrapper> getFilterBottom() {
    Map<Integer, StackWrapper> flt = super.getFilter();
    List<StackWrapper> half = new ArrayList<>();
    for (Integer i : flt.keySet()) {
      if (i >= 9 && flt.get(i) != null && flt.get(i).getStack().isEmpty() == false) {
        half.add(flt.get(i));
      }
    }
    return half;
  }

  //TODO: also list of requests ordered . and nbt saved
  // where a process terminal lists some nodes and I "turn node on for 6 cycles" and it keeps track, maybe stuck after 2.
  public ProcessRequestModel getRequest() {
    return getProcessModel();
  }


  public void setRequest(ProcessRequestModel request) {
    this.setProcessModel(request);
  }


  @Override
  public int getFieldCount() {
    return 0;
  }

  public List<ItemStack> getProcessIngredients() {
    List<StackWrapper> topRow = getFilterTop();
    List<ItemStack> list = new ArrayList<>();
    for (StackWrapper sw : topRow) {
      if (sw.getStack().isEmpty() == false) {
        ItemStack staCopy = sw.getStack().copy();
        staCopy.setCount(sw.getSize());
        list.add(staCopy);
      }
    }
    return list;
  }
  @Nonnull
  public ItemStack getFirstRecipeOut() {
    List<StackWrapper> topRow = getFilterBottom();
    if (topRow.size() == 0) {
      return ItemStack.EMPTY;
    }
    return topRow.get(0).getStack();
  }
}
