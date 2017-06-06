package mrriegel.storagenetwork;

import mrriegel.storagenetwork.master.TileMaster;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;

public abstract class ContainerNetworkBase extends Container{
  
  public abstract InventoryCrafting getCraftMatrix();
  public abstract TileMaster getTileMaster();
  public abstract void slotChanged();
}
