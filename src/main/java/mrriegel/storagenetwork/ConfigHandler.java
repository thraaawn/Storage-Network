package mrriegel.storagenetwork;
import java.io.File;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

public class ConfigHandler {
  public static Configuration config;
  public static boolean smallFont, untouchable, jeiLoaded;
  public static int rangeWirelessAccessor;
  public static void refreshConfig(File file) {
    config = new Configuration(file);
    config.load();
    smallFont = config.get(Configuration.CATEGORY_CLIENT, "smallFont", true).getBoolean();
    rangeWirelessAccessor = config.get(Configuration.CATEGORY_GENERAL, "rangeWirelessAccessor", 32).getInt();
    jeiLoaded = Loader.isModLoaded("jei");
    if (config.hasChanged()) {
      config.save();
    }
  }
}
