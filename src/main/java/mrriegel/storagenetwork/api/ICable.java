package mrriegel.storagenetwork.api;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public interface ICable {

  /**
   * Lowest number goes first. Network goes in rounds, first it runs imports, then exports, then processors
   * 
   * @return
   */
  int getPriority();
  
  /**
   * Location of connected tile entity (What I am connected to)
   * 
   * @return
   */
  BlockPos getConnectedInventory();
  
  /**
   * position of this tile entity
   * 
   * @return
   */
  BlockPos getPos();

  /**
   * Connected Inventory
   * 
   * @return
   */
  IItemHandler getInventory();


}
