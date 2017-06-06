package mrriegel.storagenetwork;
import java.util.List;
import com.google.common.collect.Lists;
import mrriegel.storagenetwork.helper.FilterItem;
import mrriegel.storagenetwork.helper.StackWrapper;
import mrriegel.storagenetwork.master.TileMaster;
import mrriegel.storagenetwork.network.PacketHandler;
import mrriegel.storagenetwork.network.StacksMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public abstract class ContainerNetworkBase extends Container {
  public InventoryPlayer playerInv;
  public InventoryCraftResult result;
  public InventoryCrafting craftMatrix;
  public abstract InventoryCrafting getCraftMatrix();
  public abstract TileMaster getTileMaster();
  public abstract void slotChanged();
  public void craftShift(EntityPlayer player, TileMaster tile) {
    SlotCrafting sl = new SlotCrafting(player, craftMatrix, result, 0, 0, 0);
    int crafted = 0;
    List<ItemStack> lis = Lists.newArrayList();
    for (int i = 0; i < craftMatrix.getSizeInventory(); i++)
      lis.add(craftMatrix.getStackInSlot(i).copy());
    ItemStack res = result.getStackInSlot(0);
    while (crafted + res.getCount() <= res.getMaxStackSize()) {
      if (!ItemHandlerHelper.insertItemStacked(new PlayerMainInvWrapper(playerInv), res.copy(), true).isEmpty()) {
        break;
      }
      ItemHandlerHelper.insertItemStacked(new PlayerMainInvWrapper(playerInv), res.copy(), false);
      sl.onTake(player, res);
      crafted += res.getCount();
      for (int i = 0; i < craftMatrix.getSizeInventory(); i++)
        if (craftMatrix.getStackInSlot(i).isEmpty()) {
          ItemStack req = tile.request(!lis.get(i).isEmpty() ? new FilterItem(lis.get(i), true, false, false) : null, 1, false);
          craftMatrix.setInventorySlotContents(i, req);
        }
      onCraftMatrixChanged(craftMatrix);
      if (!ItemHandlerHelper.canItemStacksStack(res, result.getStackInSlot(0)))
        break;
      else
        res = result.getStackInSlot(0);
    }
    List<StackWrapper> list = tile.getStacks();
    PacketHandler.INSTANCE.sendTo(new StacksMessage(list, tile.getCraftableStacks(list)), (EntityPlayerMP) player);
    detectAndSendChanges();
  }
}
