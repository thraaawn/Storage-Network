package mrriegel.storagenetwork.util.data;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class StackWrapper {

  private ItemStack stack;
  private int size;

  public StackWrapper(@Nonnull ItemStack stack, int size) {
    super();
    this.stack = stack;
    this.size = size;
  }

  private StackWrapper() {}

  public void readFromNBT(NBTTagCompound compound) {
    NBTTagCompound c = compound.getCompoundTag("stack");
    stack = new ItemStack(c);
    size = compound.getInteger("size");
  }

  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    NBTTagCompound c = new NBTTagCompound();
    ItemStack copy = stack.copy();
    //this fixes the first half of https://github.com/PrinceOfAmber/Storage-Network/issues/19, items not rendering
    copy.setCount(1);//count outside of [1,64] gets set to EMPTY STACK, breaking things like Storage Drawers
    copy.writeToNBT(c);
    compound.setTag("stack", c);
    compound.setInteger("size", size);
    return compound;
  }

  @Override
  public String toString() {
    return "StackWrapper [stack=" + stack.getDisplayName() + ", size=" + size + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof StackWrapper)) {
      return false;
    }
    StackWrapper o = (StackWrapper) obj;
    return o.stack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(o.stack, stack);
  }

  public ItemStack getStack() {
    return stack;
  }

  public void setStack(@Nonnull ItemStack stack) {
    this.stack = stack;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public StackWrapper copy() {
    return new StackWrapper(stack.copy(), size);
  }

  public static StackWrapper loadStackWrapperFromNBT(NBTTagCompound nbt) {
    StackWrapper wrap = new StackWrapper();
    wrap.readFromNBT(nbt);
    if (wrap.getStack() == null || wrap.getStack().getItem() == null) {
      return null;
    }
    return wrap;
  }
}
