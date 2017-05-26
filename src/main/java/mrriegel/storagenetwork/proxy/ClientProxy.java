package mrriegel.storagenetwork.proxy;
import mrriegel.storagenetwork.ModBlocks;
import mrriegel.storagenetwork.ModItems;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.cable.TileKabel;
import mrriegel.storagenetwork.items.ItemUpgrade;
import mrriegel.storagenetwork.render.CableRenderer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
  @Override
  public void preInit(FMLPreInitializationEvent event) {
    super.preInit(event);
    registerRenderers();
    registerItemModels();
  }
  @Override
  public void init(FMLInitializationEvent event) {
    super.init(event);
  }
  @Override
  public void postInit(FMLPostInitializationEvent event) {
    super.postInit(event);
  }
  public void registerItemModels() {
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.kabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.exKabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":ex_kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.storageKabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":storage_kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.imKabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":im_kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.master), 0, new ModelResourceLocation(StorageNetwork.MODID + ":master", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.request), 0, new ModelResourceLocation(StorageNetwork.MODID + ":request", "inventory"));
    for (int i = 0; i < ItemUpgrade.NUM; i++) {
      ModelLoader.setCustomModelResourceLocation(ModItems.upgrade, i, new ModelResourceLocation(StorageNetwork.MODID + ":upgrade_" + i, "inventory"));
    }
    for (int i = 0; i < 2; i++) {
      ModelLoader.setCustomModelResourceLocation(ModItems.remote, i, new ModelResourceLocation(StorageNetwork.MODID + ":remote_" + i, "inventory"));
    }
    ModelLoader.setCustomModelResourceLocation(ModItems.duplicator, 0, new ModelResourceLocation(StorageNetwork.MODID + ":duplicator", "inventory"));
    //		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.toggler), 0, new ModelResourceLocation(StorageNetwork.MODID + ":toggler", "inventory"));
  }
  public void registerRenderers() {
    ClientRegistry.bindTileEntitySpecialRenderer(TileKabel.class, new CableRenderer());
  }
}
