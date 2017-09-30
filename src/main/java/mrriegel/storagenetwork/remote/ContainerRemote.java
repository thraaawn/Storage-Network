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
  //  public InventoryPlayer playerInv;
  public TileMaster tile;
  //  public InventoryCraftResult result;
  //  public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
  public ItemStack remote;
  public ContainerRemote(final InventoryPlayer playerInv) {
    craftMatrix = new InventoryCrafting(this, 3, 3);
    this.playerInv = playerInv;
    result = new InventoryCraftResult();
    remote = playerInv.getCurrentItem();
    if (!playerInv.player.world.isRemote)
      tile = ItemRemote.getTile(remote);
    for (int i = 0; i < 9; i++) {
      craftMatrix.setInventorySlotContents(i, NBTHelper.getItemStack(remote, "c" + i));
    }
    SlotCrafting slotCraftOutput = new SlotCrafting(playerInv.player, craftMatrix, result, 0, 101, 128) {
      @Override
      public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
        if (playerIn.world.isRemote) {
          return stack;
        }
        List<ItemStack> lis = Lists.newArrayList();
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++)
          lis.add(craftMatrix.getStackInSlot(i).copy());
        super.onTake(playerIn, stack);
        detectAndSendChanges();
        for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
          if (craftMatrix.getStackInSlot(i) == null || craftMatrix.getStackInSlot(i).isEmpty()
              && tile != null) {
            ItemStack req = tile.request(
                !lis.get(i).isEmpty() ? new FilterItem(lis.get(i), true, false, false) : null, 1, false);
            if (!req.isEmpty())
              craftMatrix.setInventorySlotContents(i, req);
          }
        }
        if (tile != null) {
          List<StackWrapper> list = tile.getStacks();
          PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, tile.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
          detectAndSendChanges();
        }
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
    this.onCraftMatrixChanged(this.craftMatrix);
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
        craftShift(playerIn, this.getTileMaster());
        return ItemStack.EMPTY;
      }
      else if (tile != null) {
        int rest = tile.insertStack(itemstack1, null, false);
        ItemStack stack = (rest == 0) ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(itemstack1, rest);
        slot.putStack(stack);
        detectAndSendChanges();
        List<StackWrapper> list = tile.getStacks();
        PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, tile.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
        return ItemStack.EMPTY;
      }
    }
    return itemstack;
  }
  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    if (tile == null) {
      return false;
    }
    if (!playerIn.world.isRemote && playerIn.world.getTotalWorldTime() % 40 == 0) {
      List<StackWrapper> list = tile.getStacks();
      PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, tile.getCraftableStacks(list)), (EntityPlayerMP) playerIn);
    }
    return playerIn.inventory.getCurrentItem() != null && playerIn.inventory.getCurrentItem().getItem() == ModItems.remote;
  }
  public void slotChanged() {
    for (int i = 0; i < 9; i++) {
      NBTHelper.setItemStack(remote, "c" + i, craftMatrix.getStackInSlot(i));
    }
  }
  @Override
  public void onContainerClosed(EntityPlayer playerIn) {
    slotChanged();
    super.onContainerClosed(playerIn);
  }
  @Override
  public void onCraftMatrixChanged(IInventory inventoryIn) {
    IRecipe r = CraftingManager.findMatchingRecipe(craftMatrix, this.playerInv.player.world);
    if (r != null) {
      this.result.setInventorySlotContents(0, r.getRecipeOutput().copy());
    }
    else {
      this.result.setInventorySlotContents(0, ItemStack.EMPTY);
    }
  }
  @Override
  public InventoryCrafting getCraftMatrix() {
    return this.craftMatrix;
  }
  @Override
  public TileMaster getTileMaster() {
    return ItemRemote.getTile(remote);
  }
}