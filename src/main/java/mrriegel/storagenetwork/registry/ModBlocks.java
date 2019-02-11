package mrriegel.storagenetwork.registry;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.BlockCable;
import mrriegel.storagenetwork.block.cable.io.BlockCableIO;
import mrriegel.storagenetwork.block.cable.link.BlockCableLink;
import mrriegel.storagenetwork.block.control.BlockControl;
import mrriegel.storagenetwork.block.master.BlockMaster;
import mrriegel.storagenetwork.block.request.BlockRequest;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(StorageNetwork.MODID)
public class ModBlocks {

  @GameRegistry.ObjectHolder("master")
  public static BlockMaster master;//network center

  @GameRegistry.ObjectHolder("request")
  public static BlockRequest request;//the main request table

  @GameRegistry.ObjectHolder("kabel")
  public static BlockCable kabel;

  @GameRegistry.ObjectHolder("storage_kabel")
  public static BlockCableLink storageKabel;

  // @GameRegistry.ObjectHolder("storage_kabel_plain")
  // public static BlockCableLinkPlain storage_kabel_plain;

  @GameRegistry.ObjectHolder("ex_kabel")
  public static BlockCableIO exKabel;

  @GameRegistry.ObjectHolder("im_kabel")
  public static BlockCableIO imKabel;

  @GameRegistry.ObjectHolder("process_kabel")
  public static BlockCable processKabel;

  @GameRegistry.ObjectHolder("controller")
  public static BlockControl controller;
}
