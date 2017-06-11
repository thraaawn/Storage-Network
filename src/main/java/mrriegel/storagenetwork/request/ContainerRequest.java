package mrriegel.storagenetwork.request;
import java.util.List;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.ContainerNetworkBase;
import mrriegel.storagenetwork.helper.FilterItem;
import mrriegel.storagenetwork.helper.StackWrapper;
import mrriegel.storagenetwork.helper.Util;
import mrriegel.storagenetwork.master.TileMaster;
import mrriegel.storagenetwork.network.PacketHandler;
import mrriegel.storagenetwork.network.StacksMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.items.ItemHandlerHelper;

public class ContainerRequest extends ContainerNetworkBase {
  //  public InventoryPlayer playerInv;
  public TileRequest tile;
  //  public InventoryCraftResult result;
  //  public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
  //  
  public ContainerRequest(final TileRequest tile, final InventoryPlayer playerInv) {
    craftMatrix = new InventoryCrafting(this, 3, 3);
    this.tile = tile;
    this.playerInv = playerInv;
    result = new InventoryCraftResult();
    for (int i = 0; i < 9; i++) {
      if (tile.matrix.get(i) != null && tile.matrix.get(i).isEmpty() == false)
        craftMatrix.setInventorySlotContents(i, tile.matrix.get(i));
    }
    SlotCrafting slotCraftOutput = new SlotCrafting(playerInv.player, craftMatrix, result, 0, 101, 128) {
      @Override
      public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
        if (playerIn.world.isRemote) { return stack; }
        List<ItemStack> lis = Lists.newArrayList();
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++)
          lis.add(craftMatrix.getStackInSlot(i).copy());
        super.onTake(playerIn, stack);
        TileMaster t = (TileMaster) tile.getWorld().getTileEntity(tile.getMaster());
        detectAndSendChanges();
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
          if (craftMatrix.getStackInSlot(i) == null || craftMatrix.getStackInSlot(i).isEmpty()) {
            ItemStack req = t.request(
                !lis.get(i).isEmpty() ? new FilterItem(lis.get(i), true, false, false) : null, 1, false);
            if (!req.isEmpty())
              craftMatrix.setInventorySlotContents(i, req);
          }
        }
        List<StackWrapper> list = t.getStacks();
        PacketHandler.INSTANCE.sendTo(new StacksMessage(list, t.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
        detectAndSendChanges();
        return stack;
      }
    };
    this.addSlotToContainer(slotCraftOutput);
    int index = 0;
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        this.addSlotToContainer(new Slot(craftMatrix, index++, 8 + j * 18, 110 + i * 18));
      }
    }
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
      }
    }
    for (int i = 0; i < 9; ++i) {
      this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 232));
    }
    this.onCraftMatrixChanged(this.craftMatrix);
  }
  @Override
  public void onCraftMatrixChanged(IInventory inventoryIn) {
    //.getInstance()
    this.result.setInventorySlotContents(0, CraftingManager.findMatchingRecipe(craftMatrix, tile.getWorld()));
  }
  @Override
  public void onContainerClosed(EntityPlayer playerIn) {
    slotChanged();
    super.onContainerClosed(playerIn);
  }
  public void slotChanged() {
    for (int i = 0; i < 9; i++) {
      tile.matrix.put(i, craftMatrix.getStackInSlot(i));
    }
    Util.updateTile(tile.getWorld(), tile.getPos());
  }
  @Override
  public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex) {
    if (playerIn.world.isRemote) { return ItemStack.EMPTY; }
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(slotIndex);
    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();
      if (slotIndex == 0) {
        craftShift(playerIn, (TileMaster) this.tile.getWorld().getTileEntity(this.tile.getMaster()));
        return ItemStack.EMPTY;
      }
      if (slotIndex <= 9) {
        if (!this.mergeItemStack(itemstack1, 10, 10 + 36, true)) { return ItemStack.EMPTY; }
        slot.onSlotChange(itemstack1, itemstack);
      }
      else {
        TileMaster tile = (TileMaster) this.tile.getWorld().getTileEntity(this.tile.getMaster());
        if (tile != null) {
          int rest = tile.insertStack(itemstack1, null, false);
          ItemStack stack = rest == 0 ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(itemstack1, rest);
          slot.putStack(stack);
          detectAndSendChanges();
          List<StackWrapper> list = tile.getStacks();
          PacketHandler.INSTANCE.sendTo(new StacksMessage(list, tile.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
          if (stack.isEmpty())
            return ItemStack.EMPTY;
          slot.onTake(playerIn, itemstack1);
          return ItemStack.EMPTY;
        }
      }
      if (itemstack1.getCount() == 0) {
        slot.putStack(ItemStack.EMPTY);
      }
      else {
        slot.onSlotChanged();
      }
      if (itemstack1.getCount() == itemstack.getCount()) { return ItemStack.EMPTY; }
      slot.onTake(playerIn, itemstack1);
    }
    return itemstack;
  }
  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    if (tile == null || tile.getMaster() == null || !(tile.getWorld().getTileEntity(tile.getMaster()) instanceof TileMaster))
      return false;
    TileMaster t = (TileMaster) tile.getWorld().getTileEntity(tile.getMaster());
    if (!tile.getWorld().isRemote && tile.getWorld().getTotalWorldTime() % 40 == 0) {
      List<StackWrapper> list = t.getStacks();
      PacketHandler.INSTANCE.sendTo(new StacksMessage(list, t.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
    }
    return playerIn.getDistanceSq(tile.getPos().getX() + 0.5D, tile.getPos().getY() + 0.5D, tile.getPos().getZ() + 0.5D) <= 64.0D;
  }
  @Override
  public boolean canMergeSlot(ItemStack stack, Slot slot) {
    return slot.inventory != this.result && super.canMergeSlot(stack, slot);
  }
  @Override
  public InventoryCrafting getCraftMatrix() {
    return this.craftMatrix;
  }
  @Override
  public TileMaster getTileMaster() {
    return (TileMaster) tile.getWorld().getTileEntity(tile.getMaster());
  }
}
