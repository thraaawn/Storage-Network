package mrriegel.storagenetwork.config;

import java.io.File;
import mrriegel.storagenetwork.block.master.TileMaster;
import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {

  public static Configuration config;
  public static int rangeWirelessAccessor;
  public static long refreshTicks;
  public static boolean allowFastWorkBenchIntegration;
  public static boolean logEverything;
  public static boolean reloadNetworkWhenUnloadChunk;

  public static void refreshConfig(File file) {
    config = new Configuration(file);
    config.load();
    syncConfig();
    if (config.hasChanged()) {
      config.save();
    }
  }

  private static void syncConfig() {
    //    default 200 ticks aka 10 seconds
    String category = Configuration.CATEGORY_GENERAL;
    refreshTicks = config.getInt("AutoRefreshTicks", category, 200, 1, 10000, "How often to auto-refresh a network (one second is 20 ticks)");
    rangeWirelessAccessor = config.getInt("StorageRemoteRange", category, 128, 1, 10000, "How far the Remote item can reach (non-advanced)");
    allowFastWorkBenchIntegration = config.getBoolean("allowFastWorkBenchIntegration", category, true, "Allow 'fastworkbench' project to integrate into storage network crafting grids.  Turning off lets you disable integration without uninstalling mod.  Client and server should match for best outcome.");
    ConfigHandler.logEverything = config.getBoolean("LogSpamAllTheThings", category, false, "Log lots of events, some with systemtime benchmarking. WARNING: VERY SPAMMY. Only use when debugging lag or other issues.");
    reloadNetworkWhenUnloadChunk = config.getBoolean("ReloadNetworkWhenUnloadChunk", category, true, "If this is true, reload network when a chunk unloads, this keeps your network always up to date.  It has been reported that this cause lag and chunk load issues on servers, so disable if you have any problems. ");
    TileMaster.blacklist = config.getStringList("BlacklistBlocks", category, new String[] {
        "extrautils2:playerchest"
    }, "Disable these blocks from ever being able to connect to the network, they will be treated as a non-inventory.");
    //    TileMaster.blacklist 
  }
}
