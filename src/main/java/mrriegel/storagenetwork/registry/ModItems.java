package mrriegel.storagenetwork.registry;

import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.item.remote.ItemRemote;
import net.minecraft.item.Item;

public class ModItems {

  public static final Item upgrade = new ItemUpgrade();
  public static final Item remote = new ItemRemote().setRegistryName("remote");
}
