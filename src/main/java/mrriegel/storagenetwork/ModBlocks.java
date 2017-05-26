package mrriegel.storagenetwork;
 
 

import mrriegel.storagenetwork.blocks.BlockMaster;
import mrriegel.storagenetwork.blocks.BlockRequest;
import mrriegel.storagenetwork.blocks.BlockToggle;
import mrriegel.storagenetwork.cable.BlockKabel;
import mrriegel.storagenetwork.cable.TileKabel;
import mrriegel.storagenetwork.tile.TileMaster;
import mrriegel.storagenetwork.tile.TileRequest;
import mrriegel.storagenetwork.tile.TileToggler;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {
	public static final Block master = new BlockMaster();//network center
	public static final Block request = new BlockRequest();//the main request table
 
	public static final Block kabel = new BlockKabel().setRegistryName("kabel");//cable
	public static final Block storageKabel = new BlockKabel().setRegistryName("storage_kabel");//cable that connects to chest
	public static final Block exKabel = new BlockKabel().setRegistryName("ex_kabel");//import
	public static final Block imKabel = new BlockKabel().setRegistryName("im_kabel");//export
 

	public static final Block toggler = new BlockToggle();

	public static void init() {
		GameRegistry.register(master);
		GameRegistry.register(request);
 
		GameRegistry.register(kabel.setUnlocalizedName(kabel.getRegistryName().toString()));
		GameRegistry.register(storageKabel.setUnlocalizedName(storageKabel.getRegistryName().toString()));
		GameRegistry.register(exKabel.setUnlocalizedName(exKabel.getRegistryName().toString()));
		GameRegistry.register(imKabel.setUnlocalizedName(imKabel.getRegistryName().toString()));
 

		GameRegistry.register(toggler);

		GameRegistry.register(new BlockMaster.Item(master).setRegistryName(master.getRegistryName()));
		GameRegistry.register(new BlockRequest.Item(request).setRegistryName(request.getRegistryName()));

		GameRegistry.register(new BlockKabel.Item(kabel).setRegistryName(kabel.getRegistryName()));
		GameRegistry.register(new BlockKabel.Item(storageKabel).setRegistryName(storageKabel.getRegistryName()));
		GameRegistry.register(new BlockKabel.Item(exKabel).setRegistryName(exKabel.getRegistryName()));
		GameRegistry.register(new BlockKabel.Item(imKabel).setRegistryName(imKabel.getRegistryName()));

		GameRegistry.register(new BlockToggle.Item(toggler).setRegistryName(toggler.getRegistryName()));

		GameRegistry.registerTileEntity(TileKabel.class, "tileKabel");
		GameRegistry.registerTileEntity(TileMaster.class, "tileMaster");
		GameRegistry.registerTileEntity(TileRequest.class, "tileRequest");
  
		GameRegistry.registerTileEntity(TileToggler.class, "tileToggler");

	}

}
