package mrriegel.storagenetwork;
import mrriegel.storagenetwork.cable.BlockCable;
import mrriegel.storagenetwork.cable.TileCable;
import mrriegel.storagenetwork.master.BlockMaster;
import mrriegel.storagenetwork.master.TileMaster;
import mrriegel.storagenetwork.request.BlockRequest;
import mrriegel.storagenetwork.request.TileRequest;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {
  public static final Block master = new BlockMaster();//network center
  public static final Block request = new BlockRequest();//the main request table
  public static final Block kabel = new BlockCable().setRegistryName("kabel");//cable
  public static final Block storageKabel = new BlockCable().setRegistryName("storage_kabel");//cable that connects to chest
  public static final Block exKabel = new BlockCable().setRegistryName("ex_kabel");//import
  public static final Block imKabel = new BlockCable().setRegistryName("im_kabel");//export
  public static void init() {
    GameRegistry.register(master);
    GameRegistry.register(request);
    GameRegistry.register(kabel.setUnlocalizedName(kabel.getRegistryName().toString()));
    GameRegistry.register(storageKabel.setUnlocalizedName(storageKabel.getRegistryName().toString()));
    GameRegistry.register(exKabel.setUnlocalizedName(exKabel.getRegistryName().toString()));
    GameRegistry.register(imKabel.setUnlocalizedName(imKabel.getRegistryName().toString()));
    GameRegistry.register(new BlockMaster.Item(master).setRegistryName(master.getRegistryName()));
    GameRegistry.register(new BlockRequest.Item(request).setRegistryName(request.getRegistryName()));
    GameRegistry.register(new BlockCable.Item(kabel).setRegistryName(kabel.getRegistryName()));
    GameRegistry.register(new BlockCable.Item(storageKabel).setRegistryName(storageKabel.getRegistryName()));
    GameRegistry.register(new BlockCable.Item(exKabel).setRegistryName(exKabel.getRegistryName()));
    GameRegistry.register(new BlockCable.Item(imKabel).setRegistryName(imKabel.getRegistryName()));
    GameRegistry.registerTileEntity(TileCable.class, "tileKabel");
    GameRegistry.registerTileEntity(TileMaster.class, "tileMaster");
    GameRegistry.registerTileEntity(TileRequest.class, "tileRequest");
  }
}
