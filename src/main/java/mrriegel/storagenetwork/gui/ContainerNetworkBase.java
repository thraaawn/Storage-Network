package mrriegel.storagenetwork.gui;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.data.ItemStackMatcher;
import mrriegel.storagenetwork.network.StackRefreshClientMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public abstract class ContainerNetworkBase extends Container implements IStorageContainer {

  protected InventoryPlayer playerInv;
  protected InventoryCraftResult result;
  protected InventoryCraftingNetwork matrix;
  protected boolean recipeLocked = false;
  protected boolean isSimple;
  @Override
  public InventoryCrafting getCraftMatrix() {
    return this.matrix;
  }

  @Override
  public abstract TileMaster getTileMaster();

  public abstract void bindHotbar();

  @Override
  public abstract void slotChanged();

  boolean test = false;

  protected void bindPlayerInvo(final InventoryPlayer playerInv) {
    //player inventory
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
      }
    }
  }

  protected void bindGrid() {
    int index = 0;
    //3x3 crafting grid
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        this.addSlotToContainer(new Slot(matrix, index++, 8 + j * 18, 110 + i * 18));
      }
    }
  }

  @Override
  public void onContainerClosed(EntityPlayer playerIn) {
    slotChanged();
    super.onContainerClosed(playerIn);
  }

  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();
  }

  @Override
  public void onCraftMatrixChanged(IInventory inventoryIn) {
    super.onCraftMatrixChanged(inventoryIn);
  }

  protected void findMatchingRecipe(InventoryCrafting craftMatrix) {
    IRecipe recipe = null;
    try {
      recipe = CraftingManager.findMatchingRecipe(matrix, this.playerInv.player.world);
    }
    catch (java.util.NoSuchElementException err) {
      // this seems basically out of my control, its DEEP in vanilla and some library, no idea whats up with that
      // https://pastebin.com/2S9LSe23
      StorageNetwork.instance.logger.error("Error finding recipe [0] Possible conflict with forge, vanilla, or Storage Network", err);
    }
    catch (Throwable e) {
      StorageNetwork.instance.logger.error("Error finding recipe [-1]", e);
    }
    if (recipe != null) {
      ItemStack itemstack = recipe.getCraftingResult(this.matrix);
      //real way to not lose nbt tags BETTER THAN COPY
      this.result.setInventorySlotContents(0, itemstack);
    }
    else {
      this.result.setInventorySlotContents(0, ItemStack.EMPTY);
    }
  }

  /**
   * A note on the shift-craft delay bug root cause was ANY interaction with matrix (setting contents etc) was causing triggers/events to do a recipe lookup. Meaning during this shift-click action you
   * can get up to 9x64 FULL recipe scans Solution is just to disable all those triggers but only for duration of this action
   *
   * @param player
   * @param tile
   */
  protected void craftShift(EntityPlayer player, TileMaster tile) {
    if (matrix == null) {
      return;
    }
    IRecipe recipeCurrent = CraftingManager.findMatchingRecipe(matrix, player.world);
    if (recipeCurrent == null) {
      return;
    }
    this.recipeLocked = true;
    int crafted = 0;
    List<ItemStack> recipeCopy = Lists.newArrayList();
    for (int i = 0; i < matrix.getSizeInventory(); i++) {
      recipeCopy.add(matrix.getStackInSlot(i).copy());
    }
    ItemStack res = recipeCurrent.getCraftingResult(matrix);
    if (res.isEmpty()) {
      //  StorageNetwork.instance.logger.error("Recipe output is an empty stack " + recipeCurrent);
      return;
    }
    int sizePerCraft = res.getCount();
    // int sizeFull = res.getMaxStackSize();
    //   int numberToCraft = sizeFull / sizePerCraft;
    // StorageNetwork.log("[craftShift] numberToCraft = " + numberToCraft + " for stack " + res);
    while (crafted + sizePerCraft <= res.getMaxStackSize()) {
      res = recipeCurrent.getCraftingResult(matrix);
      //  StorageNetwork.log("[craftShift]  crafted = " + crafted + " ; res.count() = " + res.getCount() + " MAX=" + res.getMaxStackSize());
      if (!ItemHandlerHelper.insertItemStacked(new PlayerMainInvWrapper(playerInv), res, true).isEmpty()) {
        //  StorageNetwork.log("[craftShift] cannot insert more, end");
        break;
      }
      //stop if empty
      if (recipeCurrent.matches(matrix, player.world) == false) {
        //      StorageNetwork.log("[craftShift] recipe doesnt match i quit");
        break;
      }
      //onTake replaced with this handcoded rewrite
      //  StorageNetwork.log("[craftShift] addItemStackToInventory " + res);
      if (!player.inventory.addItemStackToInventory(res)) {
        player.dropItem(res, false);
      }
      NonNullList<ItemStack> remainder = CraftingManager.getRemainingItems(matrix, player.world);
      //  StorageNetwork.log("[craftShift] getRemainingItems ");
      for (int i = 0; i < remainder.size(); ++i) {
        ItemStack remainderCurrent = remainder.get(i);
        ItemStack slot = this.matrix.getStackInSlot(i);
        if (remainderCurrent.isEmpty()) {
          //     StorageNetwork.log("[craftShift] getRemainingItems  set empty " + i);
          matrix.getStackInSlot(i).shrink(1);
          continue;
        }
        if (remainderCurrent.isItemDamaged() && remainderCurrent.getItemDamage() > remainderCurrent.getMaxDamage()) {
          remainderCurrent = ItemStack.EMPTY;
        }
        if (slot.getItem().getContainerItem() != null) { //is the fix for milk and similar
          slot = new ItemStack(slot.getItem().getContainerItem());
          matrix.setInventorySlotContents(i, slot);
        }
        else if (!slot.getItem().getContainerItem(slot).isEmpty()) { //is the fix for milk and similar
          slot = slot.getItem().getContainerItem(slot);
          matrix.setInventorySlotContents(i, slot);
        }
        else if (!remainderCurrent.isEmpty()) {
          //   StorageNetwork.log("[craftShift] NONEMPTY " + remainderCurrent);
          if (slot.isEmpty()) {
            this.matrix.setInventorySlotContents(i, remainderCurrent);
          }
          else if (ItemStack.areItemsEqual(slot, remainderCurrent) && ItemStack.areItemStackTagsEqual(slot, remainderCurrent)) {
            remainderCurrent.grow(slot.getCount());
            this.matrix.setInventorySlotContents(i, remainderCurrent);
          }
          else if (ItemStack.areItemsEqualIgnoreDurability(slot, remainderCurrent)) {
            //crafting that consumes durability
            this.matrix.setInventorySlotContents(i, remainderCurrent);
          }
          else {
            if (!player.inventory.addItemStackToInventory(remainderCurrent)) {
              player.dropItem(remainderCurrent, false);
            }
          }
        }
        else if (!slot.isEmpty()) {
          this.matrix.decrStackSize(i, 1);
          slot = this.matrix.getStackInSlot(i);
        }
      } //end loop on remiainder
      //END onTake redo
      crafted += sizePerCraft;
      ItemStack stackInSlot;
      ItemStack recipeStack;
      ItemStackMatcher itemStackMatcherCurrent;
      for (int i = 0; i < matrix.getSizeInventory(); i++) {
        stackInSlot = matrix.getStackInSlot(i);
        if (stackInSlot.isEmpty()) {
          recipeStack = recipeCopy.get(i);
          //////////////// booleans are meta, ore(?ignored?), nbt
          itemStackMatcherCurrent = !recipeStack.isEmpty() ? new ItemStackMatcher(recipeStack, true, false, false) : null;
          //false here means dont simulate
          ItemStack req = tile.request(itemStackMatcherCurrent, 1, false);
          matrix.setInventorySlotContents(i, req);
        }
      }
      onCraftMatrixChanged(matrix);
    }
    detectAndSendChanges();
    this.recipeLocked = false;
    //update recipe again in case remnants left : IE hammer and such
    this.onCraftMatrixChanged(this.matrix);
  }

  @Override
  public ItemStack transferStackInSlot(EntityPlayer playerIn, int slotIndex) {
    if (playerIn.world.isRemote) {
      return ItemStack.EMPTY;
    }
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(slotIndex);
    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();
      TileMaster tileMaster = this.getTileMaster();
      if (slotIndex == 0) {
        craftShift(playerIn, tileMaster);
        return ItemStack.EMPTY;
      }
      else if (tileMaster != null) {
        int rest = tileMaster.insertStack(itemstack1, false);
        ItemStack stack = rest == 0 ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(itemstack1, rest);
        slot.putStack(stack);
        detectAndSendChanges();

        List<ItemStack> list = tileMaster.getStacks();
        PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<>()), (EntityPlayerMP) playerIn);

        if (stack.isEmpty()) {
          return ItemStack.EMPTY;
        }
        slot.onTake(playerIn, itemstack1);
        return ItemStack.EMPTY;
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

  public final class SlotCraftingNetwork extends SlotCrafting {

    public SlotCraftingNetwork(EntityPlayer player,
        InventoryCrafting craftingInventory, IInventory inventoryIn,
        int slotIndex, int xPosition, int yPosition) {
      super(player, craftingInventory, inventoryIn, slotIndex, xPosition, yPosition);
    }

    private TileMaster tileMaster;

    @Override
    public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
      if (playerIn.world.isRemote) {
        return stack;
      }
      List<ItemStack> lis = Lists.newArrayList();
      for (int i = 0; i < matrix.getSizeInventory(); i++) {
        lis.add(matrix.getStackInSlot(i).copy());
      }
      super.onTake(playerIn, stack);
      detectAndSendChanges();
      for (int i = 0; i < matrix.getSizeInventory(); i++) {
        if (matrix.getStackInSlot(i).isEmpty() && getTileMaster() != null) {
          ItemStack req = getTileMaster().request(
              !lis.get(i).isEmpty() ? new ItemStackMatcher(lis.get(i), true, false, false) : null, 1, false);
          if (!req.isEmpty()) {
            matrix.setInventorySlotContents(i, req);
          }
        }
      }
      detectAndSendChanges();
      return stack;
    }

    public TileMaster getTileMaster() {
      return tileMaster;
    }

    public void setTileMaster(TileMaster tileMaster) {
      this.tileMaster = tileMaster;
    }
  }
}
