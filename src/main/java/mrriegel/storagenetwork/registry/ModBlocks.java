package mrriegel.storagenetwork.registry;
import mrriegel.storagenetwork.cable.BlockCable;
import mrriegel.storagenetwork.master.BlockMaster;
import mrriegel.storagenetwork.request.BlockRequest;
import net.minecraft.block.Block;

public class ModBlocks {
  public static final Block master = new BlockMaster();//network center
  public static final Block request = new BlockRequest();//the main request table
  public static final Block kabel = new BlockCable().setRegistryName("kabel");//cable
  public static final Block storageKabel = new BlockCable().setRegistryName("storage_kabel");//cable that connects to chest
  public static final Block exKabel = new BlockCable().setRegistryName("ex_kabel");//import
  public static final Block imKabel = new BlockCable().setRegistryName("im_kabel");//export
}
