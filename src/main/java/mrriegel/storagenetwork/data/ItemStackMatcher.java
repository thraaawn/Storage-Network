package mrriegel.storagenetwork.data;

import javax.annotation.Nonnull;

import mrriegel.storagenetwork.api.data.IItemStackMatcher;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

public class ItemStackMatcher implements IItemStackMatcher {

  ItemStack stack;
  boolean meta, ore, nbt;

  public ItemStackMatcher(ItemStack stack) {
    this(stack, stack != null ? stack.getItemDamage() != OreDictionary.WILDCARD_VALUE : true, false, false);
  }

  public ItemStackMatcher(ItemStack stack, boolean meta, boolean ore, boolean nbt) {
    this.stack = stack;
    this.meta = meta;
    this.ore = ore;
    this.nbt = nbt;
  }

  private ItemStackMatcher() {}

  public void readFromNBT(NBTTagCompound compound) {
    NBTTagCompound c = compound.getCompoundTag("stack");
    stack = new ItemStack(c);
    meta = compound.getBoolean("meta");
    ore = compound.getBoolean("ore");
    nbt = compound.getBoolean("nbt");
  }

  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    NBTTagCompound c = new NBTTagCompound();
    stack.writeToNBT(c);
    compound.setTag("stack", c);
    compound.setBoolean("meta", meta);
    compound.setBoolean("ore", ore);
    compound.setBoolean("nbt", nbt);
    return c;
  }

  @Override
  public String toString() {
    return "ItemStackMatcher [stack=" + stack + ", meta=" + meta + ", ore=" + ore + ", nbt=" + nbt + "]";
  }

  public ItemStack getStack() {
    return stack;
  }

  public void setStack(@Nonnull ItemStack stack) {
    this.stack = stack;
  }

  public boolean isMeta() {
    return meta;
  }

  public void setMeta(boolean meta) {
    this.meta = meta;
  }

  public boolean isOre() {
    return ore;
  }

  public void setOre(boolean ore) {
    this.ore = ore;
  }

  public boolean isNbt() {
    return nbt;
  }

  public void setNbt(boolean nbt) {
    this.nbt = nbt;
  }

  public static ItemStackMatcher loadFilterItemFromNBT(NBTTagCompound nbt) {
    ItemStackMatcher fil = new ItemStackMatcher();
    fil.readFromNBT(nbt);
    return fil.getStack() != null && fil.getStack().getItem() != null ? fil : null;
  }

  @Override
  public boolean match(@Nonnull ItemStack stackIn) {
    if (stackIn.isEmpty())
      return false;
    if (ore && UtilTileEntity.equalOreDict(stackIn, stack))
      return true;
    if (nbt && !ItemStack.areItemStackTagsEqual(stack, stackIn))
      return false;
    if (meta && stackIn.getItemDamage() != stack.getItemDamage())
      return false;
    return stackIn.getItem() == stack.getItem();
  }
}