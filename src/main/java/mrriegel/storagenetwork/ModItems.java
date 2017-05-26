package mrriegel.storagenetwork;

import mrriegel.storagenetwork.items.ItemDuplicator;
import mrriegel.storagenetwork.items.ItemTemplate;
import mrriegel.storagenetwork.items.ItemUpgrade;
import mrriegel.storagenetwork.remote.ItemRemote;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
	public static final Item upgrade = new ItemUpgrade();//REMOVE
	public static final Item remote = new ItemRemote().setRegistryName("remote");
 
	public static final Item template = new ItemTemplate();//REMOVE
	public static final Item duplicator = new ItemDuplicator();//REMOVE

	public static void init() {
		GameRegistry.register(upgrade);
		GameRegistry.register(remote.setUnlocalizedName(remote.getRegistryName().toString()));
 
		GameRegistry.register(duplicator);
	}

}
