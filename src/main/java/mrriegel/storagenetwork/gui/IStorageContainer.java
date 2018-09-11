package mrriegel.storagenetwork.gui;

import mrriegel.storagenetwork.block.master.TileMaster;
import net.minecraft.inventory.InventoryCrafting;

public interface IStorageContainer {

	public TileMaster getTileMaster();
	
	InventoryCrafting getCraftMatrix();
	
	void detectAndSendChanges();
	
	void slotChanged(); //TODO: Remove?  Usages should be onContainerClosed.
	
	/**
	 * @return True if this is an instance of ContainerRequest or ContainerFastRequest, if false it will be assumed to be ContainerRemote or ContainerFastRemote.
	 */
	boolean isRequest();
}
