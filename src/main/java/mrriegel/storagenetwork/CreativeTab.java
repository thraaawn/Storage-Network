package mrriegel.storagenetwork;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CreativeTab {
	public static CreativeTabs tab1 = new CreativeTabs(StorageNetwork.MODID) {
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(ModBlocks.request);
		}

		@Override
		public String getTranslatedTabLabel() {
			return StorageNetwork.MODNAME;
		}
	};
}
