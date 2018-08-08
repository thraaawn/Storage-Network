package mrriegel.storagenetwork.jei;

import mrriegel.storagenetwork.StorageNetwork;
import net.minecraftforge.fml.common.Optional;

public class JeiHooks {

  public static String getFilterText() {
    try {
      return getJeiTextInternal();
    }
    catch (Exception e) {
      StorageNetwork.instance.logger.error(" mezz.jei.Internal not found ", e);
    }
    return "";
  }

  /**
   * so if JEI is not loaded, this will be called but then its an empty FN
   * 
   * @param s
   */
  public static void setFilterText(String s) {
    try {
      setJeiTextInternal(s);
    }
    catch (Exception e) {
      StorageNetwork.instance.logger.error(" mezz.jei.Internal not found ", e);
    }
  }

  @Optional.Method(modid = "jei")
  private static void setJeiTextInternal(String s) {
    mezz.jei.Internal.getRuntime().getItemListOverlay().setFilterText(s);
  }

  @Optional.Method(modid = "jei")
  private static String getJeiTextInternal() {
    return mezz.jei.Internal.getRuntime().getItemListOverlay().getFilterText();
  }
}
