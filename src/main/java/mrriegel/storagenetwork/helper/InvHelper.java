package mrriegel.storagenetwork.helper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class InvHelper {
  public static boolean hasItemHandler(IBlockAccess world, BlockPos pos, EnumFacing facing) {
    return getItemHandler(world.getTileEntity(pos), facing) != null;
  }
  public static IItemHandler getItemHandler(TileEntity tile, EnumFacing side) {
    if (tile == null)
      return null;
    if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
      return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
    if (tile instanceof ISidedInventory)
      return new SidedInvWrapper((ISidedInventory) tile, side);
    if (tile instanceof IInventory)
      return new InvWrapper((IInventory) tile);
    return null;
  }
  public static ItemStack insert(TileEntity tile, ItemStack stack, EnumFacing side) {
    if (tile == null) { return stack; }
    IItemHandler inv = getItemHandler(tile, side);
    return ItemHandlerHelper.insertItemStacked(inv, stack, false);
  }
  public static int canInsert(IItemHandler inv, ItemStack stack) {
    if (inv == null || stack == null || stack.isEmpty()) { return 0; }
    ItemStack s = ItemHandlerHelper.insertItemStacked(inv, stack, true);
    int rest = s == null ? 0 : s.getCount();
    stack.shrink(rest);
    return stack.getCount();
  }
  public static boolean contains(IItemHandler inv, ItemStack stack) {
    for (int i = 0; i < inv.getSlots(); i++) {
      if (ItemHandlerHelper.canItemStacksStack(inv.getStackInSlot(i), stack)) { return true; }
    }
    return false;
  }
  public static int getAmount(IItemHandler inv, FilterItem fil) {
    if (inv == null || fil == null) { return 0; }
    int amount = 0;
    for (int i = 0; i < inv.getSlots(); i++) {
      ItemStack slot = inv.getStackInSlot(i);
      if (fil.match(slot))
        amount += slot.getCount();
    }
    return amount;
  }
  public static ItemStack extractItem(IItemHandler inv, FilterItem fil, int num, boolean simulate) {
    if (inv == null || fil == null) { return null; }
    int extracted = 0;
    for (int i = 0; i < inv.getSlots(); i++) {
      ItemStack slot = inv.getStackInSlot(i);
      if (fil.match(slot)) {
        ItemStack ex = inv.extractItem(i, 1, simulate);
        if (ex != null) {
          extracted++;
          if (extracted == num)
            return ItemHandlerHelper.copyStackWithSize(slot, num);
          else
            i--;
        }
      }
    }
    return null;
  }
}
