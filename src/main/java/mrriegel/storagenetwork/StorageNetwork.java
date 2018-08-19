package mrriegel.storagenetwork;

import org.apache.logging.log4j.Logger;
import mrriegel.storagenetwork.cable.BlockCable;
import mrriegel.storagenetwork.cable.TileCable;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.items.ItemUpgrade;
import mrriegel.storagenetwork.master.BlockMaster;
import mrriegel.storagenetwork.master.TileMaster;
import mrriegel.storagenetwork.proxy.CommonProxy;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.registry.ModItems;
import mrriegel.storagenetwork.remote.ItemRemote;
import mrriegel.storagenetwork.request.BlockRequest;
import mrriegel.storagenetwork.request.TileRequest;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = StorageNetwork.MODID, name = StorageNetwork.MODNAME, updateJSON = "https://raw.githubusercontent.com/PrinceOfAmber/Storage-Network/master/update.json")
public class StorageNetwork {

  public Logger logger;
  public static final String MODID = "storagenetwork";
  public static final String MODNAME = "Simple Storage Network";
  @Instance(StorageNetwork.MODID)
  public static StorageNetwork instance;
  @SidedProxy(clientSide = "mrriegel.storagenetwork.proxy.ClientProxy", serverSide = "mrriegel.storagenetwork.proxy.CommonProxy")
  public static CommonProxy proxy;

  public static void log(String s) {
    if (ConfigHandler.logEverything) {
      instance.logger.info(s);
    }
  }

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    logger = event.getModLog();
    proxy.preInit(event);
    MinecraftForge.EVENT_BUS.register(this);
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    proxy.init(event);
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    proxy.postInit(event);
  }

  @SubscribeEvent
  public void onRegistryBlock(RegistryEvent.Register<Block> event) {
    event.getRegistry().register(ModBlocks.master);
    event.getRegistry().register(ModBlocks.request);
    event.getRegistry().register(ModBlocks.kabel.setUnlocalizedName(ModBlocks.kabel.getRegistryName().toString()));
    event.getRegistry().register(ModBlocks.storageKabel.setUnlocalizedName(ModBlocks.storageKabel.getRegistryName().toString()));
    event.getRegistry().register(ModBlocks.exKabel.setUnlocalizedName(ModBlocks.exKabel.getRegistryName().toString()));
    event.getRegistry().register(ModBlocks.imKabel.setUnlocalizedName(ModBlocks.imKabel.getRegistryName().toString()));
  }

  @SubscribeEvent
  public void onRegistryEvent(RegistryEvent.Register<Item> event) {
    event.getRegistry().register(new BlockMaster.Item(ModBlocks.master).setRegistryName(ModBlocks.master.getRegistryName()));
    event.getRegistry().register(new BlockRequest.Item(ModBlocks.request).setRegistryName(ModBlocks.request.getRegistryName()));
    event.getRegistry().register(new BlockCable.ItemCable(ModBlocks.kabel).setRegistryName(ModBlocks.kabel.getRegistryName()));
    event.getRegistry().register(new BlockCable.ItemCable(ModBlocks.storageKabel).setRegistryName(ModBlocks.storageKabel.getRegistryName()));
    event.getRegistry().register(new BlockCable.ItemCable(ModBlocks.exKabel).setRegistryName(ModBlocks.exKabel.getRegistryName()));
    event.getRegistry().register(new BlockCable.ItemCable(ModBlocks.imKabel).setRegistryName(ModBlocks.imKabel.getRegistryName()));
    event.getRegistry().register(ModItems.upgrade);
    event.getRegistry().register(ModItems.remote.setUnlocalizedName(ModItems.remote.getRegistryName().toString()));
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

  public static void chatMessage(EntityPlayer player, String message) {
    if (player.world.isRemote)
      player.sendMessage(new TextComponentString(lang(message)));
  }

  public static void statusMessage(EntityPlayer player, String message) {
    if (player.world.isRemote)
      player.sendStatusMessage(new TextComponentString(lang(message)), true);
  }
  public static String lang(String message) {
    return I18n.translateToLocal(message);
  }
  private static long lastTime;

  public static void benchmark(String s) {
    long now = System.currentTimeMillis();
    long DIFF = now - lastTime;
    lastTime = now;
    StorageNetwork.log(now
        + " [" + DIFF + "]" + " : " + s);
  }
}
