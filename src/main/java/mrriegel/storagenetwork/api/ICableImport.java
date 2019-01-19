package mrriegel.storagenetwork.api;

import mrriegel.storagenetwork.data.EnumFilterDirection;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public interface ICableImport extends IHasNetworkPriority {

  //getOperationLimit 
  //isOperationEnabled?
  //isOperationMode

  BlockPos getConnectedInventory();
  IItemHandler getInventory();

  // example
  //      int speedRatio = tileCable.getUpgradesOfType(ItemUpgrade.SPEED) + 1; 
  //return (world.getTotalWorldTime() % (30 / speedRatio) == 0)
  //how frequently does this tick. 
  //  int getSpeed(); 
  boolean runNow();

  //from upgrade. or from whatever 
  //  boolean fullStackEnabled();
  //number to send per tick. for example
  //stock item upgrade does 64
  //speed does up from 4+
  int getTransferRate();

  boolean canTransfer(ItemStack stack, EnumFilterDirection way);
}
