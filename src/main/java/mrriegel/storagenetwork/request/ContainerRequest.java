package mrriegel.storagenetwork.request;
import java.util.List;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.data.FilterItem;
import mrriegel.storagenetwork.data.StackWrapper;
import mrriegel.storagenetwork.gui.ContainerNetworkBase;
import mrriegel.storagenetwork.helper.UtilTileEntity;
import mrriegel.storagenetwork.master.TileMaster;
import mrriegel.storagenetwork.network.StacksMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
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
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.items.ItemHandlerHelper;

public class ContainerRequest extends ContainerNetworkBase {
  public TileRequest tile;
  public ContainerRequest(final TileRequest tile, final InventoryPlayer playerInv) {
    matrix = new InventoryCrafting(this, 3, 3);
    this.tile = tile;
    this.playerInv = playerInv;
    result = new InventoryCraftResult();
    //reload saved item stacks FOR the grid
    for (int i = 0; i < 9; i++) {
      if (tile.matrix.get(i) != null && tile.matrix.get(i).isEmpty() == false)
        matrix.setInventorySlotContents(i, tile.matrix.get(i));
    }
    //crafting output slot
    SlotCrafting slotCraftOutput = new SlotCrafting(playerInv.player, matrix, result, 0, 101, 128) {
      @Override
      public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
//        if(  result.getStackInSlot(0) == null 
//            || result.getStackInSlot(0).isEmpty()){
//          StorageNetwork.benchmark("[onTake] EMPTY!!!");
//          result.setInventorySlotContents(0, ItemStack.EMPTY);
//          return ItemStack.EMPTY;
//        }
        if (playerIn.world.isRemote) {
          StorageNetwork.benchmark("[onTake] isRemote");
          return stack;
        }
        StorageNetwork.benchmark("[onTake] start!!"+result.getStackInSlot(0));
        onCraftMatrixChanged(matrix);
        List<ItemStack> lis = Lists.newArrayList();
        for (int i = 0; i < matrix.getSizeInventory(); i++) {
          lis.add(matrix.getStackInSlot(i).copy());
        }
        StorageNetwork.benchmark("[onTake] before superOnTake");
        super.onTake(playerIn, stack);
        StorageNetwork.benchmark("[onTake] after superOnTake");
        TileMaster t = (TileMaster) tile.getWorld().getTileEntity(tile.getMaster());
        StorageNetwork.benchmark("[onTake] before detectSave");
        detectAndSendChanges();
        StorageNetwork.benchmark("[onTake] after detectSave");
        for (int i = 0; i < matrix.getSizeInventory(); i++) {
          if (matrix.getStackInSlot(i) == null || matrix.getStackInSlot(i).isEmpty()) {
            StorageNetwork.benchmark("[onTake] before request " + i);
            ItemStack req = t.request(
                !lis.get(i).isEmpty() ? new FilterItem(lis.get(i), true, false, false) : null, 1, false);
            StorageNetwork.benchmark("[onTake] after request " + i);
            if (!req.isEmpty()) {
              matrix.setInventorySlotContents(i, req);
            }
          }
        }
        StorageNetwork.benchmark("[onTake] after BIG loop");
        //        List<StackWrapper> list = t.getStacks();
        //        StorageNetwork.log("ContainerRequest.onTake DISAGBLE stacksMessage");
        //   PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, t.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
        detectAndSendChanges();
        StorageNetwork.benchmark("[onTake] end");
        return stack;
      }
    };
    this.addSlotToContainer(slotCraftOutput);
    int index = 0;
    //3x3 crafting grid
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        this.addSlotToContainer(new Slot(matrix, index++, 8 + j * 18, 110 + i * 18));
      }
    }
    //player inventory
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
      }
    }
    //player hotbar
    for (int i = 0; i < 9; ++i) {
      this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 232));
    }
    this.onCraftMatrixChanged(this.matrix);
  }
  @Override
  public void onCraftMatrixChanged(IInventory inventoryIn) {
    if (this.recipeLocked) {
      StorageNetwork.log("recipe locked so onCraftMatrixChanged cancelled");
      return;
    }
   // StorageNetwork.benchmark("[cr] start . onCraftMatrixChanged");
    IRecipe r = CraftingManager.findMatchingRecipe(matrix, tile.getWorld());
   // StorageNetwork.benchmark("[cr] start . onCraftMatrixChanged - afterFindRecipe");
    if (r != null) {
      this.result.setInventorySlotContents(0, r.getRecipeOutput().copy());
    }
    else {
      this.result.setInventorySlotContents(0, ItemStack.EMPTY);
    }
  //  StorageNetwork.benchmark("[cr] end . onCraftMatrixChanged");
  }
  @Override
  public void onContainerClosed(EntityPlayer playerIn) {
    slotChanged();
    super.onContainerClosed(playerIn);
  }
  @Override
  public void slotChanged() {
    //parent is abstract
    //seems to not happen from -shiftclick- crafting
    StorageNetwork.log("[cr] start .slotChanged");
    for (int i = 0; i < 9; i++) {
      tile.matrix.put(i, matrix.getStackInSlot(i));
    }
    StorageNetwork.benchmark("[cr] slotChanged before updateTile");
    UtilTileEntity.updateTile(tile.getWorld(), tile.getPos());
    StorageNetwork.benchmark("[cr] slotChanged End updateTile");
  }
  @Override
  public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex) {
    if (playerIn.world.isRemote) {
      return ItemStack.EMPTY;
    }
    StorageNetwork.log("transfer " + slotIndex);
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
        if (!this.mergeItemStack(itemstack1, 10, 10 + 36, true)) {
          return ItemStack.EMPTY;
        }
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
          PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, tile.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
          if (stack.isEmpty()) {
            return ItemStack.EMPTY;
          }
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
      if (itemstack1.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }
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
      PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, t.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
    }
    return playerIn.getDistanceSq(tile.getPos().getX() + 0.5D, tile.getPos().getY() + 0.5D, tile.getPos().getZ() + 0.5D) <= 64.0D;
  }
  @Override
  public boolean canMergeSlot(ItemStack stack, Slot slot) {
    return slot.inventory != this.result && super.canMergeSlot(stack, slot);
  }
  @Override
  public InventoryCrafting getCraftMatrix() {
    return this.matrix;
  }
  @Override
  public TileMaster getTileMaster() {
    return (TileMaster) tile.getWorld().getTileEntity(tile.getMaster());
  }
}
