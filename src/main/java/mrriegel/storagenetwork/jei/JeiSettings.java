package mrriegel.storagenetwork.jei;
public class JeiSettings {

  private static boolean jeiLoaded;
  private static boolean jeiSearch = true;

  public static boolean isJeiLoaded() {
    return jeiLoaded;
  }

  public static void setJeiLoaded(boolean jeiLoaded) {
    JeiSettings.jeiLoaded = jeiLoaded;
  }

  public static boolean isJeiSearch() {
    return jeiSearch;
  }

  public static void setJeiSearch(boolean jeiSearch) {
    JeiSettings.jeiSearch = jeiSearch;
  }
}
