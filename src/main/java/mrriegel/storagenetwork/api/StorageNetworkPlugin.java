package mrriegel.storagenetwork.api;
/**
 * Annotate your {@link IStorageNetworkPlugin} with this so it gets automatically loaded by the Simple Storage Networks mod.
 */
public @interface StorageNetworkPlugin {

  /**
   * If your plugin requires another mod to be loaded you can give its modid here. Return an empty string to always load this plugin.
   *
   * @return
   */
  String mod() default "";
}
