package mrriegel.storagenetwork.remote;
import java.util.List;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.data.FilterItem;
import mrriegel.storagenetwork.data.StackWrapper;
import mrriegel.storagenetwork.gui.ContainerNetworkBase;
import mrriegel.storagenetwork.helper.NBTHelper;
import mrriegel.storagenetwork.master.TileMaster;
import mrriegel.storagenetwork.network.StacksMessage;
import mrriegel.storagenetwork.registry.ModItems;
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

public class ContainerRemote extends ContainerNetworkBase {
  public TileMaster tileMaster;
  public ItemStack remoteItemStack;
  public ContainerRemote(final InventoryPlayer playerInv) {
    matrix = new InventoryCrafting(this, 3, 3);
    this.playerInv = playerInv;
    result = new InventoryCraftResult();
    remoteItemStack = playerInv.getCurrentItem();
    if (!playerInv.player.world.isRemote) {
      tileMaster = ItemRemote.getTile(remoteItemStack);
    }
    for (int i = 0; i < 9; i++) {
      matrix.setInventorySlotContents(i, NBTHelper.getItemStack(remoteItemStack, "c" + i));
    }
    SlotCrafting slotCraftOutput = new SlotCrafting(playerInv.player, matrix, result, 0, 101, 128) {
      @Override
      public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
        if (playerIn.world.isRemote) {
          return stack;
        }
        //        this.onCrafting(stack);
        List<ItemStack> lis = Lists.newArrayList();
        for (int i = 0; i < matrix.getSizeInventory(); i++)
          lis.add(matrix.getStackInSlot(i).copy());
        super.onTake(playerIn, stack);
        detectAndSendChanges();
        for (int i = 0; i < matrix.getSizeInventory(); i++) {
          if (matrix.getStackInSlot(i) == null || matrix.getStackInSlot(i).isEmpty()
              && tileMaster != null) {
            ItemStack req = tileMaster.request(
                !lis.get(i).isEmpty() ? new FilterItem(lis.get(i), true, false, false) : null, 1, false);
            if (!req.isEmpty())
              matrix.setInventorySlotContents(i, req);
          }
        }
        if (tileMaster != null) {
          List<StackWrapper> list = tileMaster.getStacks();
          PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, tileMaster.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
          detectAndSendChanges();
        }
        return stack;
      }
    };
    this.addSlotToContainer(slotCraftOutput);
    int index = 0;
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 3; ++j) {
        this.addSlotToContainer(new Slot(matrix, index++, 8 + j * 18, 110 + i * 18));
      }
    }
    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
      }
    }
    for (int i = 0; i < 9; ++i) {
      if (i == playerInv.currentItem)
        this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 232) {
          @Override
          public boolean isItemValid(ItemStack stack) {
            return false;
          }
          @Override
          public boolean canTakeStack(EntityPlayer playerIn) {
            return false;
          }
        });
      else
        this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 232));
    }
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
      if (slotIndex == 0) {
        // when shift click crafting
        craftShift(playerIn, this.getTileMaster());
        //        this.onCraftMatrixChanged(this.craftMatrix);
        return ItemStack.EMPTY;
      }
      else if (tileMaster != null) {
        int rest = tileMaster.insertStack(itemstack1, null, false);
        ItemStack stack = (rest == 0) ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(itemstack1, rest);
        slot.putStack(stack);
        detectAndSendChanges();
        List<StackWrapper> list = tileMaster.getStacks();
        PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, tileMaster.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
        return ItemStack.EMPTY;
      }
    }
    return itemstack;
  }
  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    if (tileMaster == null) {
      return false;
    }
    if (!playerIn.world.isRemote && playerIn.world.getTotalWorldTime() % 40 == 0) {
      List<StackWrapper> list = tileMaster.getStacks();
      PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, tileMaster.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
    }
    return playerIn.inventory.getCurrentItem() != null && playerIn.inventory.getCurrentItem().getItem() == ModItems.remote;
  }
  @Override
  public void slotChanged() {
    for (int i = 0; i < 9; i++) {
      NBTHelper.setItemStack(remoteItemStack, "c" + i, matrix.getStackInSlot(i));
    }
  }
  @Override
  public void onContainerClosed(EntityPlayer playerIn) {
    slotChanged();
    super.onContainerClosed(playerIn);
  }
  @Override
  public void onCraftMatrixChanged(IInventory inventoryIn) {
    IRecipe r = CraftingManager.findMatchingRecipe(matrix, this.playerInv.player.world);
    if (r != null) {
      ItemStack itemstack = r.getCraftingResult(this.matrix);
      //real way to not lose nbt tags BETTER THAN COPY 
      this.result.setInventorySlotContents(0, itemstack);
    }
    else {
      this.result.setInventorySlotContents(0, ItemStack.EMPTY);
    }
  }
  @Override
  public InventoryCrafting getCraftMatrix() {
    return this.matrix;
  }
  @Override
  public TileMaster getTileMaster() {
    return ItemRemote.getTile(remoteItemStack);
  }
}