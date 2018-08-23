package mrriegel.storagenetwork.jei;
public class JeiSettings {

  private static boolean jeiLoaded;
  private static boolean jeiSearchSync = true;

  public static boolean isJeiLoaded() {
    return jeiLoaded;
  }

  public static void setJeiLoaded(boolean jeiLoaded) {
    JeiSettings.jeiLoaded = jeiLoaded;
  }

  public static boolean isJeiSearchSynced() {
    return jeiSearchSync;
  }

  public static void setJeiSearchSync(boolean jeiSearch) {
    JeiSettings.jeiSearchSync = jeiSearch;
  }
}
