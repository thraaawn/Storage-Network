package mrriegel.storagenetwork.registry;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.block.request.TileRequest;
import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.item.remote.ItemRemote;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class RegistryEvents {

  @SubscribeEvent
  public void onRegistryBlock(RegistryEvent.Register<Block> event) {
    IForgeRegistry<Block> registry = event.getRegistry();
    registry.register(ModBlocks.master);
    registry.register(ModBlocks.request);
    registry.register(ModBlocks.kabel.setUnlocalizedName(ModBlocks.kabel.getRegistryName().toString()));
    registry.register(ModBlocks.storageKabel.setUnlocalizedName(ModBlocks.storageKabel.getRegistryName().toString()));
    registry.register(ModBlocks.exKabel.setUnlocalizedName(ModBlocks.exKabel.getRegistryName().toString()));
    registry.register(ModBlocks.imKabel.setUnlocalizedName(ModBlocks.imKabel.getRegistryName().toString()));
  }

  @SubscribeEvent
  public void onRegistryEvent(RegistryEvent.Register<Item> event) {
    IForgeRegistry<Item> registry = event.getRegistry();
    registry.register(new ItemBlock(ModBlocks.master).setRegistryName(ModBlocks.master.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.request).setRegistryName(ModBlocks.request.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.kabel).setRegistryName(ModBlocks.kabel.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.storageKabel).setRegistryName(ModBlocks.storageKabel.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.exKabel).setRegistryName(ModBlocks.exKabel.getRegistryName()));
    registry.register(new ItemBlock(ModBlocks.imKabel).setRegistryName(ModBlocks.imKabel.getRegistryName()));
    registry.register(ModItems.upgrade);
    registry.register(ModItems.remote.setUnlocalizedName(ModItems.remote.getRegistryName().toString()));
    GameRegistry.registerTileEntity(TileCable.class, "tileKabel");
    GameRegistry.registerTileEntity(TileMaster.class, "tileMaster");
    GameRegistry.registerTileEntity(TileRequest.class, "tileRequest");
  }

  @SubscribeEvent
  public void registerModels(ModelRegistryEvent event) {
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.kabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.exKabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":ex_kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.storageKabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":storage_kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.imKabel), 0, new ModelResourceLocation(StorageNetwork.MODID + ":im_kabel", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.master), 0, new ModelResourceLocation(StorageNetwork.MODID + ":master", "inventory"));
    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.request), 0, new ModelResourceLocation(StorageNetwork.MODID + ":request", "inventory"));
    for (int i = 0; i < ItemUpgrade.NUM; i++) {
      ModelLoader.setCustomModelResourceLocation(ModItems.upgrade, i, new ModelResourceLocation(StorageNetwork.MODID + ":upgrade_" + i, "inventory"));
    }
    for (int i = 0; i < ItemRemote.RemoteType.values().length; i++) {
      ModelLoader.setCustomModelResourceLocation(ModItems.remote, i, new ModelResourceLocation(StorageNetwork.MODID + ":remote_" + i, "inventory"));
    }
  }
}
