package mrriegel.storagenetwork.block.control;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class ProcessWrapper {

  public ProcessWrapper(BlockPos p, ItemStack s, int c, String name, boolean on) {
    pos = p;
    output = s;
    currentRequests = c;
    this.name = name;
    alwaysOn = on;
  }

  public ProcessWrapper() {
  }

  public void readFromNBT(NBTTagCompound compound) {
    name = compound.getString("name");
    alwaysOn = compound.getBoolean("aon");
    int x = compound.getInteger("xx");
    int y = compound.getInteger("yy");
    int z = compound.getInteger("zz");
    pos = new BlockPos(x, y, z);
    output = new ItemStack(compound);
  }

  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    output.writeToNBT(compound);
    compound.setString("sname", name);
    compound.setBoolean("aon", alwaysOn);
    compound.setInteger("xx", pos.getX());
    compound.setInteger("yy", pos.getY());
    compound.setInteger("zz", pos.getZ());
    return compound;
  }

  public boolean alwaysOn;
  public String name;
  public BlockPos pos;
  public ItemStack output;
  public int currentRequests;
}
