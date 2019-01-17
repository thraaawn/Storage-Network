package mrriegel.storagenetwork.block.cable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import mrriegel.storagenetwork.api.ICableStorage;
import mrriegel.storagenetwork.block.TileConnectable;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.data.EnumCableType;
import mrriegel.storagenetwork.data.EnumFilterDirection;
import mrriegel.storagenetwork.data.FilterItem;
import mrriegel.storagenetwork.data.StackWrapper;
import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.registry.ModItems;
import mrriegel.storagenetwork.util.UtilInventory;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;

/**
 * Base class for TileCable
 * 
 */
public class TileCable extends TileConnectable implements IInventory, ICableStorage {

  public static enum Fields {
    STATUS, FACINGTOPROW, FACINGBOTTOMROW;
  }

  public static final int FILTER_SIZE = 18;
  public EnumCableType north, south, east, west, up, down;
  protected boolean mode = true;
  protected int limit = 0;
  protected ItemStack stack = ItemStack.EMPTY;

  protected NonNullList<ItemStack> upgrades = NonNullList.withSize(ContainerCable.UPGRADE_COUNT, ItemStack.EMPTY);
  protected EnumFacing inventoryFace;
  protected BlockPos connectedInventory;
  private Map<Integer, StackWrapper> filter = new HashMap<Integer, StackWrapper>();
  private boolean ores = false;
  private boolean metas = false;
  private boolean nbt = false;
  private boolean isWhitelist;
  private int priority;
  private ProcessRequestModel processModel = new ProcessRequestModel();
  public EnumFacing processingTop = EnumFacing.UP;
  public EnumFacing processingBottom = EnumFacing.DOWN;
  private EnumFilterDirection transferDirection = EnumFilterDirection.BOTH;

  public TileCable() {
    this.setOres(false);
    this.setMeta(true);
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    processingTop = EnumFacing.values()[compound.getInteger("processingTop")];
    processingBottom = EnumFacing.values()[compound.getInteger("processingBottom")];
    ProcessRequestModel pm = new ProcessRequestModel();
    pm.readFromNBT(compound);
    this.setProcessModel(pm);
    isWhitelist = compound.getBoolean("white");
    priority = compound.getInteger("prio");
    NBTTagList invList = compound.getTagList("crunchTE", Constants.NBT.TAG_COMPOUND);
    filter = new HashMap<Integer, StackWrapper>();
    for (int i = 0; i < invList.tagCount(); i++) {
      NBTTagCompound stackTag = invList.getCompoundTagAt(i);
      int slot = stackTag.getByte("Slot");
      filter.put(slot, StackWrapper.loadStackWrapperFromNBT(stackTag));
    }
    ores = compound.getBoolean("ores");
    metas = compound.getBoolean("metas");
    nbt = compound.getBoolean("nbtFilter");
    try {
      transferDirection = EnumFilterDirection.valueOf(compound.getString("way"));
    }
    catch (Exception e) {
      transferDirection = EnumFilterDirection.BOTH;
    }
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
    this.processModel.writeToNBT(compound);
    compound.setInteger("processingBottom", processingBottom.ordinal());
    compound.setInteger("processingTop", processingTop.ordinal());
    compound.setBoolean("white", isWhitelist);
    compound.setInteger("prio", priority);
    NBTTagList invList = new NBTTagList();
    for (int i = 0; i < FILTER_SIZE; i++) {
      if (filter.get(i) != null) {
        NBTTagCompound stackTag = new NBTTagCompound();
        stackTag.setByte("Slot", (byte) i);
        filter.get(i).writeToNBT(stackTag);
        invList.appendTag(stackTag);
      }
    }
    compound.setTag("crunchTE", invList);
    compound.setBoolean("ores", ores);
    compound.setBoolean("metas", metas);
    compound.setBoolean("nbtFilter", nbt);
    compound.setString("way", transferDirection.toString());
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

  private boolean doesWrapperMatchStack(StackWrapper stackWrapper, ItemStack stack) {
    ItemStack s = stackWrapper.getStack();
    if (ores) {
      return UtilTileEntity.equalOreDict(stack, s);
    }
    else if (metas) {
      return stack.isItemEqual(s);
    }
    else {
      return stack.getItem() == s.getItem();
    }
    //    return ores ? UtilTileEntity.equalOreDict(stack, s) : metas ? stack.isItemEqual(s) : stack.getItem() == s.getItem();
  }

  /* key function used by TileMaster for all item trafic
   * 
   * TODO: TEST CASES
   * 
   * export + meta
   * 
   * export - meta
   * 
   * import + meta ; whitelist
   * 
   * import - meta ; whitelist
   * 
   * import + meta ; blacklist
   * 
   * import - meta ; blacklist */
  public boolean canTransfer(ItemStack stack, EnumFilterDirection way) {
    if (isStorage() && !this.getTransferDirection().match(way)) {
      return false;
    }
    if (this.isWhitelist()) {
      boolean tmp = false;
      for (StackWrapper stackWrapper : this.filter.values()) {
        if (stackWrapper == null || stackWrapper.getStack() == null) {
          continue;
        }
        if (doesWrapperMatchStack(stackWrapper, stack)) {
          tmp = true;
          break;
        }
      }
      return tmp;
    }
    else {
      boolean tmp = true;
      for (StackWrapper stackWrapper : this.filter.values()) {
        if (stackWrapper == null || stackWrapper.getStack() == null) {
          continue;
        }
        if (doesWrapperMatchStack(stackWrapper, stack)) {
          tmp = false;
          break;
        }
      }
      return tmp;
    }
  }

  public BlockPos getConnectedInventory() {
    return connectedInventory;
  }

  public void setConnectedInventory(BlockPos connectedInventory) {
    this.connectedInventory = connectedInventory;
  }

  public IItemHandler getInventory() {
    if (getConnectedInventory() != null)
      return UtilInventory.getItemHandler(world.getTileEntity(getConnectedInventory()), inventoryFace.getOpposite());
    return null;
  }

  /**
   * identical to checking === CableKind.storage
   * 
   * @return
   */
  public boolean isStorage() {
    return this.getBlockType() == ModBlocks.storageKabel;
  }

  /**
   * the whitelist / blacklist (ghost stacks in gui)
   * 
   * @return
   */
  public Map<Integer, StackWrapper> getFilter() {
    return filter;
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
    if (isOperationMode()) {
      return amount > getOperationLimit();
    }
    else {
      return amount <= getOperationLimit();
    }
  }

  public List<StackWrapper> getFilterTop() {
    Map<Integer, StackWrapper> flt = this.getFilter();
    List<StackWrapper> half = new ArrayList<>();
    for (Integer i : flt.keySet()) {
      if (i <= 8 && flt.get(i) != null && flt.get(i).getStack().isEmpty() == false) {
        half.add(flt.get(i));
      }
    }
    return half;
  }
  public void setFilter(Map<Integer, StackWrapper> filter) {
    this.filter = filter;
  }

  public boolean getOre() {
    return ores;
  }

  public void setOres(boolean ores) {
    this.ores = ores;
  }

  public boolean getNbt() {
    return nbt;
  }

  public void setNbt(boolean ores) {
    this.nbt = ores;
  }

  public boolean getMeta() {
    return metas;
  }

  public void setMeta(boolean ores) {
    this.metas = ores;
  }

  public boolean isWhitelist() {
    return isWhitelist;
  }

  public void setWhite(boolean white) {
    this.isWhitelist = white;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public EnumFilterDirection getTransferDirection() {
    return transferDirection;
  }

  public void setTransferDirection(EnumFilterDirection way) {
    this.transferDirection = way;
  }

  public EnumFacing getFacingBottomRow() {
    return this.processingBottom;
  }

  public EnumFacing getFacingTopRow() {
    return this.processingTop;
  }

  public ProcessRequestModel getProcessModel() {
    return processModel;
  }

  public void setProcessModel(ProcessRequestModel processModel) {
    this.processModel = processModel;
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

  public boolean isOperationMode() {
    return mode;
  }

  public void setOperationMode(boolean mode) {
    this.mode = mode;
  }

  public int getOperationLimit() {
    return limit;
  }

  public void setOperationLimit(int limit) {
    this.limit = limit;
  }

  public ItemStack getOperationStack() {
    return stack;
  }

  public void setOperationStack(ItemStack stack) {
    this.stack = stack;
  }

  @Override
  public AxisAlignedBB getRenderBoundingBox() {
    double renderExtention = 1.0d;
    AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - renderExtention, pos.getY() - renderExtention, pos.getZ() - renderExtention, pos.getX() + 1 + renderExtention, pos.getY() + 1 + renderExtention, pos.getZ() + 1 + renderExtention);
    return bb;
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

  public boolean isTopEmpty() {
    for (StackWrapper w : this.getFilterTop()) {
      if (w.getStack().isEmpty() == false) {
        return false;
      }
    }
    return true;
  }

  public boolean isBottomEmpty() {
    for (StackWrapper w : this.getFilterBottom()) {
      if (w.getStack().isEmpty() == false) {
        return false;
      }
    }
    return true;
  }

  public List<StackWrapper> getFilterBottom() {
    Map<Integer, StackWrapper> flt = this.getFilter();
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

  @Nonnull
  public ItemStack getFirstRecipeOut() {
    List<StackWrapper> topRow = getFilterBottom();
    if (topRow.size() == 0) {
      return ItemStack.EMPTY;
    }
    return topRow.get(0).getStack();
  }
  @Override
  public int getField(int id) {
    return 0;
  }

  @Override
  public void setField(int id, int value) {}

  @Override
  public void clear() {}
}
