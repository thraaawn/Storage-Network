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
   * Location of connected tile entity
   * 
   * @return
   */
  BlockPos getConnectedInventory();

  /**
   * Connected Inventory
   * 
   * @return
   */
  IItemHandler getInventory();

  /**
   * Is this used for importing
   * 
   * TODO: ENUM These
   * 
   * @return
   */
  boolean isImportCable();

  /**
   * Is this used for exmporting.
   * 
   * @return
   */
  boolean isExportCable();

  /**
   * Is this inventory used for storage
   * 
   * @return
   */
  boolean isStorageCable();

}
