package mrriegel.storagenetwork.gui;

import mrriegel.storagenetwork.block.master.TileMaster;
import net.minecraft.inventory.InventoryCrafting;

/**
 * These methods are for compat with FastBench, they were originally abstract in ContainerNetworkBase.
 * Some names are strange to not conflict with the reobf process.
 * @author Shadows
 *
 */
public interface IStorageContainer {

	public TileMaster getTileMaster();

	InventoryCrafting getCraftMatrix();

	void slotChanged(); //TODO: Remove?  Usages should be onContainerClosed.

	/**
	 * @return True if this is an instance of ContainerRequest or ContainerFastRequest, if false it will be assumed to be ContainerRemote or ContainerFastRemote.
	 */
	boolean isRequest();
}
