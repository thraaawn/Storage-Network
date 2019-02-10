package mrriegel.storagenetwork.block.cable.io;

import mrriegel.storagenetwork.api.capability.IConnectableItemAutoIO;
import mrriegel.storagenetwork.block.cable.ContainerCable;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.capabilities.CapabilityConnectableAutoIO;
import mrriegel.storagenetwork.capabilities.StorageNetworkCapabilities;
import mrriegel.storagenetwork.registry.ModItems;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public class ContainerCableIO extends ContainerCable {
  public static final int UPGRADE_COUNT = 4;

  @Nullable
  public CapabilityConnectableAutoIO autoIO;

  public ContainerCableIO(TileCable tile, InventoryPlayer playerInv) {
    super(tile, playerInv);

    if(!tile.hasCapability(StorageNetworkCapabilities.CONNECTABLE_AUTO_IO, null)) {
      return;
    }

    IConnectableItemAutoIO rawAutoIO = tile.getCapability(StorageNetworkCapabilities.CONNECTABLE_AUTO_IO, null);
    if(!(rawAutoIO instanceof CapabilityConnectableAutoIO)) {
      return;
    }

    this.autoIO = (CapabilityConnectableAutoIO)rawAutoIO;

    for (int ii = 0; ii < UPGRADE_COUNT; ii++) {
      this.addSlotToContainer(new SlotItemHandler(autoIO.upgrades, ii, 98 + ii * sq, 6) {

        @Override
        public boolean isItemValid(ItemStack stack) {
          return stack.getItem() == ModItems.upgrade;
        }
      });
    }
  }


}
