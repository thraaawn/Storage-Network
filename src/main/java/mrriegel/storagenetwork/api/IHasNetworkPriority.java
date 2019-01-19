package mrriegel.storagenetwork.api;


public interface IHasNetworkPriority {

  boolean isImportCable();

  boolean isExportCable();

  boolean isStorageCable();

  int getPriority();
}
