package mrriegel.storagenetwork;
import net.minecraftforge.fml.common.Optional;

public class JeiHooks {
  /**
   * so if JEI is not loaded, this will be called but then its an empty FN
   * @param s
   */
  public static void setFilterText(String s){
    _setFilterText(s);
  }
  @Optional.Method( modid = "jei")
  private static void _setFilterText(String s){
    mezz.jei.Internal.getRuntime().getItemListOverlay().setFilterText(s);
  }
}
