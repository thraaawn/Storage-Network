package mrriegel.storagenetwork.registry;

import mrriegel.storagenetwork.block.cable.BlockCable;
import mrriegel.storagenetwork.block.control.BlockControl;
import mrriegel.storagenetwork.block.master.BlockMaster;
import mrriegel.storagenetwork.block.request.BlockRequest;
import net.minecraft.block.Block;

public class ModBlocks {

  public static final Block master = new BlockMaster().setRegistryName("master");//network center
  public static final Block request = new BlockRequest().setRegistryName("request");//the main request table
  public static final Block kabel = new BlockCable().setRegistryName("kabel");
  public static final Block storageKabel = new BlockCable().setRegistryName("storage_kabel");//cable that connects to chest
  public static final Block exKabel = new BlockCable().setRegistryName("ex_kabel");
  public static final Block imKabel = new BlockCable().setRegistryName("im_kabel");
  public static final Block processKabel = new BlockCable().setRegistryName("process_kabel");
  public static final Block controller = new BlockControl().setRegistryName("controller");
}
