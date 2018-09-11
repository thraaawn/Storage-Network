package mrriegel.storagenetwork.gui;

import java.util.List;

import mrriegel.storagenetwork.util.data.StackWrapper;

public interface IStorageInventory {

	void setStacks(List<StackWrapper> stacks);
	
	void setCraftableStacks(List<StackWrapper> stacks);
	
}
