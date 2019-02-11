package mrriegel.storagenetwork.api;
/**
 * Implement this interface to get access to some helper methods via the {@link IStorageNetworkHelpers} instance.
 *
 * Annotate it with {@link StorageNetworkPlugin} to let it get auto instantiated by Simple Storage Networks. Make sure your class has a constructor without parameters!
 */
public interface IStorageNetworkPlugin {

  /**
   * Store a reference to the instance you get passed here for later access.
   *
   * @param helpers
   *          An instance of the helper class for you to use.
   */
  default void helpersReady(IStorageNetworkHelpers helpers) {}
}
