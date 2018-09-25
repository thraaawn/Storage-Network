package mrriegel.storagenetwork.block;

import java.util.HashMap;
import java.util.Map;
import mrriegel.storagenetwork.block.cable.ProcessRequestModel;
import mrriegel.storagenetwork.util.UtilTileEntity;
import mrriegel.storagenetwork.util.data.EnumFilterDirection;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;

/**
 * Base class for Master and Cable
 * 
 */
public abstract class AbstractFilterTile extends TileConnectable {

  public static final int FILTER_SIZE = 18;
  private Map<Integer, StackWrapper> filter = new HashMap<Integer, StackWrapper>();
  private boolean ores = false;
  private boolean metas = false;
  private boolean isWhitelist;
  private int priority;
  protected ProcessRequestModel processModel = new ProcessRequestModel();
  public EnumFacing processingTop = EnumFacing.UP;
  public EnumFacing processingBottom = EnumFacing.UP;
  protected EnumFilterDirection way = EnumFilterDirection.BOTH;

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    readSettings(compound);
  }

  private void readSettings(NBTTagCompound compound) {
    processingTop = EnumFacing.values()[compound.getInteger("processTop")];
    processingBottom = EnumFacing.values()[compound.getInteger("processBottm")];
    this.processModel.readFromNBT(compound);
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
    try {
      way = EnumFilterDirection.valueOf(compound.getString("way"));
    }
    catch (Exception e) {
      way = EnumFilterDirection.BOTH;
    }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);
    writeSettings(compound);
    return compound;
  }

  private void writeSettings(NBTTagCompound compound) {
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
    compound.setString("way", way.toString());
  }

  private boolean doesWrapperMatchStack(StackWrapper stackWrapper, ItemStack stack) {
    ItemStack s = stackWrapper.getStack();
    return ores ? UtilTileEntity.equalOreDict(stack, s) : metas ? stack.isItemEqual(s) : stack.getItem() == s.getItem();
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
    if (isStorage() && !this.way.match(way)) {
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

  public abstract IItemHandler getInventory();

  public abstract BlockPos getSource();

  /**
   * identical to checking === CableKind.storage
   * 
   * @return
   */
  public abstract boolean isStorage();

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

  public boolean getOre() {
    return ores;
  }

  public void setOres(boolean ores) {
    this.ores = ores;
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

  public EnumFilterDirection getWay() {
    return way;
  }

  public void setWay(EnumFilterDirection way) {
    this.way = way;
  }

  public EnumFacing getFacingBottomRow() {
    return this.processingBottom;
  }

  public EnumFacing getFacingTopRow() {
    return this.processingTop;
  }
}
