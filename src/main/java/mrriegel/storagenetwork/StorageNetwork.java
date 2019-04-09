package mrriegel.storagenetwork;

import org.apache.logging.log4j.Logger;
import mrriegel.storagenetwork.apiimpl.AnnotatedInstanceUtil;
import mrriegel.storagenetwork.apiimpl.PluginRegistry;
import mrriegel.storagenetwork.apiimpl.StorageNetworkHelpers;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.datafixes.FixManager;
import mrriegel.storagenetwork.proxy.CommonProxy;
import mrriegel.storagenetwork.registry.RegistryEvents;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = StorageNetwork.MODID, name = StorageNetwork.MODNAME, certificateFingerprint = "@FINGERPRINT@", version = StorageNetwork.VERSION, updateJSON = "https://raw.githubusercontent.com/Lothrazar/Storage-Network/master/update.json")
public class StorageNetwork {

  public Logger logger;
  public static final String MODID = "storagenetwork";
  public static final String MODNAME = "Simple Storage Network";
  public static final String VERSION = "@VERSION@";
  @Instance(StorageNetwork.MODID)
  public static StorageNetwork instance;
  @SidedProxy(clientSide = "mrriegel.storagenetwork.proxy.ClientProxy", serverSide = "mrriegel.storagenetwork.proxy.CommonProxy")
  public static CommonProxy proxy;
  public static final PluginRegistry pluginRegistry = new PluginRegistry();
  public static StorageNetworkHelpers helpers = new StorageNetworkHelpers();
  private static FixManager fixManager;

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    logger = event.getModLog();
    fixManager = new FixManager();
    // Load all the plugins by instantiating all annotated instances of IStorageNetworkPlugin
    AnnotatedInstanceUtil.asmDataTable = event.getAsmData();
    pluginRegistry.loadStorageNetworkPlugins();
    proxy.preInit(event);
    MinecraftForge.EVENT_BUS.register(this);
    MinecraftForge.EVENT_BUS.register(new RegistryEvents());
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    proxy.init(event);
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    // Notify each plugin that they can now use the IStorageNetworkHelpers instance
    pluginRegistry.forEach(iStorageNetworkPlugin -> iStorageNetworkPlugin.helpersReady(helpers));
    proxy.postInit(event);
  }

  @EventHandler
  public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
    // https://tutorials.darkhax.net/tutorials/jar_signing/
    String source = (event.getSource() == null) ? "" : event.getSource().getName() + " ";
    String msg = "Storage Network: Invalid fingerprint detected! The file " + source + "may have been tampered with. This version will NOT be supported by the author!";
    if (logger == null) {
      System.out.println(msg);
    }
    else {
      logger.error(msg);
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

  @SuppressWarnings("deprecation")
  public static String lang(String message) {
    return net.minecraft.util.text.translation.I18n.translateToLocal(message);
  }

  private static long lastTime;

  public static void log(String s) {
    if (ConfigHandler.logEverything) {
      instance.logger.info(s);
    }
  }

  public static void info(String s) {
    instance.logger.info(s);
  }
  public static void error(String s) {
    instance.logger.error(s);
  }

  private static void benchmark(String s) {
    long now = System.currentTimeMillis();
    long DIFF = now - lastTime;
    lastTime = now;
    StorageNetwork.log(now
        + " [" + DIFF + "]" + " : " + s);
  }
}
