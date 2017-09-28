package mrriegel.storagenetwork.tile;
import java.util.HashMap;
import java.util.Map;
import mrriegel.storagenetwork.helper.StackWrapper;
import mrriegel.storagenetwork.helper.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;

public abstract class AbstractFilterTile extends TileConnectable {
  public static final int FILTER_SIZE = 18;
  private Map<Integer, StackWrapper> filter = new HashMap<Integer, StackWrapper>();
  private Map<Integer, Boolean> ores = new HashMap<Integer, Boolean>();
  private Map<Integer, Boolean> metas = new HashMap<Integer, Boolean>();
  private boolean isWhitelist;
  private int priority;
  private Direction way = Direction.BOTH;
  public enum Direction {
    IN, OUT, BOTH;
    public boolean match(Direction way) {
      if (this == BOTH || way == BOTH)
        return true;
      return this == way;
    }
    public Direction next() {
      return values()[(this.ordinal() + 1) % values().length];
    }
  }
  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    readSettings(compound);
  }
  public void readSettings(NBTTagCompound compound) {
    isWhitelist = compound.getBoolean("white");
    priority = compound.getInteger("prio");
    NBTTagList invList = compound.getTagList("crunchTE", Constants.NBT.TAG_COMPOUND);
    filter = new HashMap<Integer, StackWrapper>();
    for (int i = 0; i < invList.tagCount(); i++) {
      NBTTagCompound stackTag = invList.getCompoundTagAt(i);
      int slot = stackTag.getByte("Slot");
      filter.put(slot, StackWrapper.loadStackWrapperFromNBT(stackTag));
    }
    NBTTagList oreList = compound.getTagList("ores", Constants.NBT.TAG_COMPOUND);
    ores = new HashMap<Integer, Boolean>();
    for (int i = 0; i < FILTER_SIZE; i++)
      ores.put(i, false);
    for (int i = 0; i < oreList.tagCount(); i++) {
      NBTTagCompound stackTag = oreList.getCompoundTagAt(i);
      int slot = stackTag.getByte("Slot");
      ores.put(slot, stackTag.getBoolean("Ore"));
    }
    NBTTagList metaList = compound.getTagList("metas", Constants.NBT.TAG_COMPOUND);
    metas = new HashMap<Integer, Boolean>();
    for (int i = 0; i < FILTER_SIZE; i++)
      metas.put(i, true);
    for (int i = 0; i < metaList.tagCount(); i++) {
      NBTTagCompound stackTag = metaList.getCompoundTagAt(i);
      int slot = stackTag.getByte("Slot");
      metas.put(slot, stackTag.getBoolean("Meta"));
    }
    try {
      way = Direction.valueOf(compound.getString("way"));
    }
    catch (Exception e) {
      way = Direction.BOTH;
    }
  }
  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);
    writeSettings(compound);
    return compound;
  }
  public void writeSettings(NBTTagCompound compound) {
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
    NBTTagList oreList = new NBTTagList();
    for (int i = 0; i < FILTER_SIZE; i++) {
      if (ores.get(i) != null) {
        NBTTagCompound stackTag = new NBTTagCompound();
        stackTag.setByte("Slot", (byte) i);
        stackTag.setBoolean("Ore", ores.get(i));
        oreList.appendTag(stackTag);
      }
    }
    compound.setTag("ores", oreList);
    NBTTagList metaList = new NBTTagList();
    for (int i = 0; i < FILTER_SIZE; i++) {
      if (metas.get(i) != null) {
        NBTTagCompound stackTag = new NBTTagCompound();
        stackTag.setByte("Slot", (byte) i);
        stackTag.setBoolean("Meta", metas.get(i));
        metaList.appendTag(stackTag);
      }
    }
    compound.setTag("metas", metaList);
    compound.setString("way", way.toString());
  }
  public boolean canTransfer(ItemStack stack, Direction way) {
    if (isStorage() && !this.way.match(way))
      return false;
    if (isWhitelist()) {
      boolean tmp = false;
      for (int i = 0; i < FILTER_SIZE; i++) {
        if (getFilter().get(i) == null)
          continue;
        ItemStack s = getFilter().get(i).getStack();
        if (s == null)
          continue;
        boolean ore = getOre(i);
        boolean meta = getMeta(i);
        if (ore ? Util.equalOreDict(stack, s) : meta ? stack.isItemEqual(s) : stack.getItem() == s.getItem()) {
          tmp = true;
          break;
        }
      }
      return tmp;
    }
    else {
      boolean tmp = true;
      for (int i = 0; i < FILTER_SIZE; i++) {
        if (getFilter().get(i) == null)
          continue;
        ItemStack s = getFilter().get(i).getStack();
        if (s == null || s.isEmpty())
          continue;
        boolean ore = getOre(i);
        boolean meta = getMeta(i);
        if (ore ? Util.equalOreDict(stack, s) : meta ? stack.isItemEqual(s) : stack.getItem() == s.getItem()) {
          tmp = false;
          break;
        }
      }
      return tmp;
    }
  }
  public abstract IItemHandler getInventory();
  public abstract BlockPos getSource();
/**
 * identical to checking === CableKind.storage
 * @return
 */
  public abstract boolean isStorage();
  public boolean getOre(int i) {
    return getOres().get(i) == null ? false : getOres().get(i);
  }
  public boolean getMeta(int i) {
    return getMetas().get(i) == null ? true : getMetas().get(i);
  }
  /**
   * the whitelist / blacklist (ghost stacks in gui)
   * 
   * @return
   */
  public Map<Integer, StackWrapper> getFilter() {
    return filter;
  }
  public void setFilter(Map<Integer, StackWrapper> filter) {
    this.filter = filter;
  }
  public Map<Integer, Boolean> getOres() {
    return ores;
  }
//  public void setOres(Map<Integer, Boolean> ores) {
//    this.ores = ores;
//  }
  public Map<Integer, Boolean> getMetas() {
    return metas;
  }
//  public void setMetas(Map<Integer, Boolean> metas) {
//    this.metas = metas;
//  }
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
  public Direction getWay() {
    return way;
  }
  public void setWay(Direction way) {
    this.way = way;
  }
}
