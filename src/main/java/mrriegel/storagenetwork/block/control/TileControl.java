package mrriegel.storagenetwork.block.control;

import java.util.HashMap;
import java.util.Map;
import mrriegel.storagenetwork.block.TileConnectable;
import mrriegel.storagenetwork.data.EnumSortType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class TileControl extends TileConnectable {

  public Map<Integer, ItemStack> matrix = new HashMap<Integer, ItemStack>();
  private boolean downwards;
  private EnumSortType sort = EnumSortType.NAME;

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    super.readFromNBT(compound);
    setDownwards(compound.getBoolean("dir"));
    setSort(EnumSortType.valueOf(compound.getString("sort")));
    NBTTagList invList = compound.getTagList("matrix", Constants.NBT.TAG_COMPOUND);
    matrix = new HashMap<Integer, ItemStack>();
    for (int i = 0; i < invList.tagCount(); i++) {
      NBTTagCompound stackTag = invList.getCompoundTagAt(i);
      int slot = stackTag.getByte("Slot");
      ItemStack s = new ItemStack(stackTag);
      matrix.put(slot, s);
    }
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);
    compound.setBoolean("dir", isDownwards());
    compound.setString("sort", getSort().toString());
    NBTTagList invList = new NBTTagList();
    invList = new NBTTagList();
    for (int i = 0; i < 9; i++) {
      if (matrix.get(i) != null && matrix.get(i).isEmpty() == false) {
        NBTTagCompound stackTag = new NBTTagCompound();
        stackTag.setByte("Slot", (byte) i);
        matrix.get(i).writeToNBT(stackTag);
        invList.appendTag(stackTag);
      }
    }
    compound.setTag("matrix", invList);
    return compound;
  }

  public boolean isDownwards() {
    return downwards;
  }

  public void setDownwards(boolean downwards) {
    this.downwards = downwards;
  }

  public EnumSortType getSort() {
    return sort;
  }

  public void setSort(EnumSortType sort) {
    this.sort = sort;
  }
}
