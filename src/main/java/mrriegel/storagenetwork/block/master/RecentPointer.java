package mrriegel.storagenetwork.block.master;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class RecentPointer {

  //Must Be A Copy
  private ItemStack itemStack;
  private BlockPos pos;
  private int slot = -1;

  public int getSlot() {
    return slot;
  }

  public void setSlot(int slot) {
    this.slot = slot;
  }

  public BlockPos getPos() {
    return pos;
  }

  public void setPos(BlockPos pos) {
    this.pos = pos;
  }

  public ItemStack getItemStack() {
    return itemStack;
  }

  public void setItemStack(ItemStack itemStack) {
    this.itemStack = itemStack;
  }

  public static class StackSlot {

    public int slot = -1;
    public ItemStack stack = ItemStack.EMPTY;
  }
}
