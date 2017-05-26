package mrriegel.storagenetwork;

import mrriegel.storagenetwork.items.ItemDuplicator;
import mrriegel.storagenetwork.items.ItemUpgrade;
import mrriegel.storagenetwork.remote.ItemRemote;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
	public static final Item upgrade = new ItemUpgrade();//REMOVE
	public static final Item remote = new ItemRemote().setRegistryName("remote");
 
	public static final Item duplicator = new ItemDuplicator();//REMOVE
 

}
