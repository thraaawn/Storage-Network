package mrriegel.storagenetwork.config;
import java.io.File;
import mrriegel.storagenetwork.tile.TileConnectable;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

public class ConfigHandler {
  public static Configuration config;
  public static boolean smallFont, jeiLoaded;
  public static int rangeWirelessAccessor;
  public static long refreshTicks = 200;
  public static boolean logEverything;
  public static void refreshConfig(File file) {
    config = new Configuration(file);
    config.load();
    smallFont = config.get(Configuration.CATEGORY_CLIENT, "smallFont", true).getBoolean();
    refreshTicks = config.get(Configuration.CATEGORY_GENERAL, "AutoRefreshTicks", 200).getInt();
    rangeWirelessAccessor = config.get(Configuration.CATEGORY_GENERAL, "StorageRemoteRange", 128).getInt();
    ConfigHandler.logEverything = config.getBoolean("LogSpamAllTheThings", Configuration.CATEGORY_GENERAL, false, "Log lots of events, some with systemtime benchmarking. WARNING: VERY SPAMMY. Only use when debugging lag or other issues.");
    jeiLoaded = Loader.isModLoaded("jei");
    TileConnectable.reloadNetworkWhenUnloadChunk = config.getBoolean("ReloadNetworkWhenUnloadChunk", Configuration.CATEGORY_GENERAL, true, "If this is true, reload network when a chunk unloads, this keeps your network always up to date.  It has been reported that this cause lag and chunk load issues on servers, so disable if you have any problems. ");
    if (config.hasChanged()) {
      config.save();
    }
  }
}
