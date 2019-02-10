package mrriegel.storagenetwork.api.capability;

import mrriegel.storagenetwork.api.data.DimPos;
import mrriegel.storagenetwork.api.network.INetworkMaster;

/**
 * All blocks that can connect to the storage-network need to expose this capability. Because of the way the storage-networking is built up, each connectable needs to expose its own position and
 * dimension, so it can be fully traversed when necessary.
 *
 * If you want to expose this yourself instead of accessing it, you probably want to extend the DefaultConnectable implementation for your own capability.
 */
public interface IConnectable {

  /**
   * Return the position of the master. For historic reasons each block currently needs to know this.
   *
   * @return a DimPos with the proper dimension and position
   */
  DimPos getMasterPos();

  /**
   * Return the position of this connectable.
   *
   * This is used to traverse the network and might be different from the actual block position. For example the Compact Machines mod bridges capabilities across dimensions and we need to continue
   * traversing the network inside the compact machine and not at the machine block itself.
   *
   * You should simply return the position of your block here.
   *
   * @return
   */
  DimPos getPos();

  /**
   * When your block is placed and a connected network updates, it calls this method to tell your capability where the {@link INetworkMaster} is. Store this value and return it in getMasterPos().
   *
   * @param masterPos
   */
  void setMasterPos(DimPos masterPos);
}
