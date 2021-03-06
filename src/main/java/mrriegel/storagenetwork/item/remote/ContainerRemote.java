package mrriegel.storagenetwork.item.remote;

import java.util.ArrayList;
import java.util.List;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.gui.ContainerNetworkBase;
import mrriegel.storagenetwork.gui.InventoryCraftingNetwork;
import mrriegel.storagenetwork.network.StackRefreshClientMessage;
import mrriegel.storagenetwork.registry.ModItems;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.NBTHelper;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerRemote extends ContainerNetworkBase {

  private ItemStack remoteItemStack;

  public ContainerRemote(final InventoryPlayer playerInv) {
    this.playerInv = playerInv;
    result = new InventoryCraftResult();
    remoteItemStack = playerInv.getCurrentItem();
    List<ItemStack> storage = new ArrayList<ItemStack>();
    for (int i = 0; i < 9; i++) {
      storage.add(NBTHelper.getItemStack(remoteItemStack, "c" + i));
    }
    matrix = new InventoryCraftingNetwork(this, storage);
    SlotCraftingNetwork slotCraftOutput = new SlotCraftingNetwork(playerInv.player, matrix, result, 0, 101, 128);
    slotCraftOutput.setTileMaster(this.getTileMaster());
    this.addSlotToContainer(slotCraftOutput);
    bindGrid();
    bindPlayerInvo(playerInv);
    bindHotbar();
    this.onCraftMatrixChanged(this.matrix);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void setAll(List<ItemStack> listIn) {
    //    super.setAll(p_190896_1_);
    matrix.skipEvents = true;
    for (int i = 0; i < listIn.size(); ++i) {
      this.getSlot(i).putStack(listIn.get(i));
    }
    matrix.skipEvents = false;
  }

  @Override
  public boolean canInteractWith(EntityPlayer playerIn) {
    TileMaster tileMaster = this.getTileMaster();
    if (tileMaster == null) {
      return false;
    }
    if (!playerIn.world.isRemote && playerIn.world.getTotalWorldTime() % 40 == 0) {
      List<StackWrapper> list = tileMaster.getStacks();
      PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<StackWrapper>()), (EntityPlayerMP) playerIn);
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
  public void onCraftMatrixChanged(IInventory inventoryIn) {
    findMatchingRecipe(matrix);
  }

  @Override
  public TileMaster getTileMaster() {
    return ItemRemote.getTile(remoteItemStack);
  }

  @Override
  public void bindHotbar() {
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
  }

  @Override
  public boolean isRequest() {
    return false;
  }
}