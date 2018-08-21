package mrriegel.storagenetwork.gui;

import java.util.List;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.util.data.FilterItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public abstract class ContainerNetworkBase extends Container {

  public InventoryPlayer playerInv;
  protected InventoryCraftResult result;
  protected InventoryCraftingNetwork matrix;
  public boolean recipeLocked = false;

  public abstract InventoryCrafting getCraftMatrix();

  public abstract TileMaster getTileMaster();

  public abstract void slotChanged();

  boolean test = false;

  @Override
  public void detectAndSendChanges() {
    //   StorageNetwork.log("detectAndSendChanges  ");
    super.detectAndSendChanges();
  }

  @Override
  public void onCraftMatrixChanged(IInventory inventoryIn) {
    // StorageNetwork.log("onCraftMatrixChanged  ");
    super.onCraftMatrixChanged(inventoryIn);
  }

  protected void findMatchingRecipe(InventoryCrafting craftMatrix) {
    IRecipe recipe = null;
    try {
      StorageNetwork.benchmark("findMatchingRecipe start");
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
      StorageNetwork.benchmark("findMatchingRecipe end success");
      ItemStack itemstack = recipe.getCraftingResult(this.matrix);
      //real way to not lose nbt tags BETTER THAN COPY 
      this.result.setInventorySlotContents(0, itemstack);
    }
    else {
      StorageNetwork.benchmark("findMatchingRecipe end fail ");
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
  public void craftShift(EntityPlayer player, TileMaster tile) {
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
      StorageNetwork.instance.logger.error("Recipe output is an empty stack " + recipeCurrent);
      return;
    }
    int sizePerCraft = res.getCount();
    int sizeFull = res.getMaxStackSize();
    int numberToCraft = sizeFull / sizePerCraft;
    StorageNetwork.log("numberToCraft = " + numberToCraft + " for stack " + res);
    while (crafted + sizePerCraft <= res.getMaxStackSize()) {
      res = recipeCurrent.getCraftingResult(matrix);
      StorageNetwork.log("crafted = " + crafted + " ; res.count() = " + res.getCount() + " MAX=" + res.getMaxStackSize());
      if (!ItemHandlerHelper.insertItemStacked(new PlayerMainInvWrapper(playerInv), res, true).isEmpty()) {
        break;
      }
      //stop if empty
      if (recipeCurrent.matches(matrix, player.world) == false) {
        break;
      }
      //onTake replaced with this handcoded rewrite
      //  this.getSlot(0).onTake(player, res);// ontake this does the actaul craft see ContainerRequest
      StorageNetwork.log("addItemStackToInventory " + res);
      if (!player.inventory.addItemStackToInventory(res)) {
        player.dropItem(res, false);
      }
      NonNullList<ItemStack> remainder = CraftingManager.getRemainingItems(matrix, player.world);
      //StorageNetwork.benchmark("after getRemainingItems");
      for (int i = 0; i < remainder.size(); ++i) {
        //StorageNetwork.benchmark("before getstackinslot");
        ItemStack slot = this.matrix.getStackInSlot(i);
        ItemStack remainderCurrent = remainder.get(i);
        //        StorageNetwork.benchmark("A");
        if (slot.getItem().getContainerItem() != null) { //is the fix for milk and similar
          slot = new ItemStack(slot.getItem().getContainerItem());
          matrix.setInventorySlotContents(i, slot);
        }
        else if (!slot.getItem().getContainerItem(slot).isEmpty()) { //is the fix for milk and similar
          slot = slot.getItem().getContainerItem(slot);
          matrix.setInventorySlotContents(i, slot);
        }
        else if (!remainderCurrent.isEmpty()) {
          if (slot.isEmpty()) {
            //   StorageNetwork.benchmark("B");
            this.matrix.setInventorySlotContents(i, remainderCurrent);
            // StorageNetwork.benchmark("C");
          }
          else if (ItemStack.areItemsEqual(slot, remainderCurrent) && ItemStack.areItemStackTagsEqual(slot, remainderCurrent)) {
            // StorageNetwork.benchmark("D");
            remainderCurrent.grow(slot.getCount());
            this.matrix.setInventorySlotContents(i, remainderCurrent);
            //StorageNetwork.benchmark("E");
          }
          else if (ItemStack.areItemsEqualIgnoreDurability(slot, remainderCurrent)) {
            //crafting that consumes durability
            StorageNetwork.benchmark("fix for crafting eating durability");
            this.matrix.setInventorySlotContents(i, remainderCurrent);
          }
          else {
            StorageNetwork.log("Gadd to inventory " + remainderCurrent);
            if (!player.inventory.addItemStackToInventory(remainderCurrent)) {
              // StorageNetwork.benchmark("G");
              player.dropItem(remainderCurrent, false);
            }
          }
          //   StorageNetwork.benchmark("H");
        }
        else if (!slot.isEmpty()) {
          //      StorageNetwork.benchmark("start isempty section");
          this.matrix.decrStackSize(i, 1);
          slot = this.matrix.getStackInSlot(i);
          //    StorageNetwork.benchmark("after isempty section");
        }

      }
      //END onTake redo
      //StorageNetwork.benchmark("after onTake REFACTORED!");
      crafted += sizePerCraft;
      ItemStack stackInSlot;
      ItemStack recipeStack;
      FilterItem filterItemCurrent;
      // StorageNetwork.benchmark( "before FOR loop");
      for (int i = 0; i < matrix.getSizeInventory(); i++) {
        // StorageNetwork.benchmark( "start of FOR loop");
        stackInSlot = matrix.getStackInSlot(i);
        if (stackInSlot.isEmpty()) {
          recipeStack = recipeCopy.get(i);
          // StorageNetwork.benchmark( "Container.craftShift loop " + crafted + " slotIndex " + i);
          //////////////// booleans are meta, ore(?ignored?), nbt
          filterItemCurrent = !recipeStack.isEmpty() ? new FilterItem(recipeStack, true, false, false) : null;
          //false here means dont simulate
          //StorageNetwork.benchmark("before request");
          ItemStack req = tile.request(filterItemCurrent, 1, false);
          //StorageNetwork.benchmark("after request & before setInventorySlotContents");
          matrix.setInventorySlotContents(i, req);
          //StorageNetwork.benchmark("after setInventorySlotContents");
        }
      }

      onCraftMatrixChanged(matrix);

    }
    detectAndSendChanges();
    this.recipeLocked = false;
    //update recipe again in case remnants left : IE hammer and such
    this.onCraftMatrixChanged(this.matrix);
    //StorageNetwork.benchmark("[network base] end :: " + result.getStackInSlot(0));
    StorageNetwork.benchmark("end");
  }
}
