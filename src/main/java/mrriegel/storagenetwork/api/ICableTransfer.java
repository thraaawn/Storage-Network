package mrriegel.storagenetwork.api;

import java.util.List;
import mrriegel.storagenetwork.data.EnumFilterDirection;
import mrriegel.storagenetwork.data.FilterItem;
import net.minecraft.item.ItemStack;

public interface ICableTransfer extends ICable {

  /**
   * A filter item is used in teh request packet sent to the network when asking to pull an item out.
   * 
   * This is a list of requests that this cable is trying to export
   * 
   * stack size is ignored, instead {@link getTransferRate()} is used
   * 
   * @param stackCurrent
   * @return
   */
  List<FilterItem> getExportFilter();
  /**
   * Called every tick to see if an operation should be processed now
   * 
   * @return
   */
  boolean runNow();

  /**
   * get transfer rate from 0-64
   * 
   * @return
   */
  int getTransferRate();

  /**
   * Can a transfer be done in this direction with this stack.
   * 
   * @param stack
   * @param way
   * @return
   */
  boolean canTransfer(ItemStack stack, EnumFilterDirection way);
}
