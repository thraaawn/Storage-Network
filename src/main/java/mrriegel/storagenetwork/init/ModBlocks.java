package mrriegel.storagenetwork.init;

import mrriegel.storagenetwork.blocks.BlockAnnexer;
import mrriegel.storagenetwork.blocks.BlockContainer;
import mrriegel.storagenetwork.blocks.BlockCrafter;
import mrriegel.storagenetwork.blocks.BlockIndicator;
import mrriegel.storagenetwork.blocks.BlockItemBox;
import mrriegel.storagenetwork.blocks.BlockKabel;
import mrriegel.storagenetwork.blocks.BlockMaster;
import mrriegel.storagenetwork.blocks.BlockRequest;
import mrriegel.storagenetwork.blocks.BlockToggle;
import mrriegel.storagenetwork.tile.TileAnnexer;
import mrriegel.storagenetwork.tile.TileIndicator;
import mrriegel.storagenetwork.tile.TileItemBox;
import mrriegel.storagenetwork.tile.TileKabel;
import mrriegel.storagenetwork.tile.TileMaster;
import mrriegel.storagenetwork.tile.TileRequest;
import mrriegel.storagenetwork.tile.TileToggler;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {
	public static final Block master = new BlockMaster();//network center
	public static final Block request = new BlockRequest();//the main request table
//	public static final Block frequest = new BlockFRequest();
	public static final Block kabel = new BlockKabel().setRegistryName("kabel");//cable
	public static final Block storageKabel = new BlockKabel().setRegistryName("storage_kabel");//cable that connects to chest
	public static final Block exKabel = new BlockKabel().setRegistryName("ex_kabel");//import
	public static final Block imKabel = new BlockKabel().setRegistryName("im_kabel");//export
	public static final Block vacuumKabel = new BlockKabel().setRegistryName("vacuum_kabel");//vacum-doesntwork?

 
	public static final Block indicator = new BlockIndicator();
	public static final Block annexer = new BlockAnnexer();

	public static final Block toggler = new BlockToggle();

	public static void init() {
		GameRegistry.register(master);
		GameRegistry.register(request);
 
		GameRegistry.register(kabel.setUnlocalizedName(kabel.getRegistryName().toString()));
		GameRegistry.register(storageKabel.setUnlocalizedName(storageKabel.getRegistryName().toString()));
		GameRegistry.register(exKabel.setUnlocalizedName(exKabel.getRegistryName().toString()));
		GameRegistry.register(imKabel.setUnlocalizedName(imKabel.getRegistryName().toString()));
		GameRegistry.register(vacuumKabel.setUnlocalizedName(vacuumKabel.getRegistryName().toString()));

 
		GameRegistry.register(indicator);
		GameRegistry.register(annexer);

		GameRegistry.register(toggler);

		GameRegistry.register(new BlockMaster.Item(master).setRegistryName(master.getRegistryName()));
		GameRegistry.register(new BlockRequest.Item(request).setRegistryName(request.getRegistryName()));
//		GameRegistry.register(new BlockFRequest.Item(frequest).setRegistryName(frequest.getRegistryName()));
		GameRegistry.register(new BlockKabel.Item(kabel).setRegistryName(kabel.getRegistryName()));
		GameRegistry.register(new BlockKabel.Item(storageKabel).setRegistryName(storageKabel.getRegistryName()));
		GameRegistry.register(new BlockKabel.Item(exKabel).setRegistryName(exKabel.getRegistryName()));
		GameRegistry.register(new BlockKabel.Item(imKabel).setRegistryName(imKabel.getRegistryName()));
		GameRegistry.register(new BlockKabel.Item(vacuumKabel).setRegistryName(vacuumKabel.getRegistryName()));

 
		GameRegistry.register(new BlockIndicator.Item(indicator).setRegistryName(indicator.getRegistryName()));
		GameRegistry.register(new BlockAnnexer.Item(annexer).setRegistryName(annexer.getRegistryName()));
//		GameRegistry.register(new BlockFannexer.Item(fannexer).setRegistryName(fannexer.getRegistryName()));
//		GameRegistry.register(new BlockItemBox.Item(itemBox).setRegistryName(itemBox.getRegistryName()));
//		GameRegistry.register(new BlockFluidBox.Item(fluidBox).setRegistryName(fluidBox.getRegistryName()));
		// GameRegistry.register(new
		// BlockContainer.Item(container).setRegistryName(container.getRegistryName()));
		GameRegistry.register(new BlockToggle.Item(toggler).setRegistryName(toggler.getRegistryName()));

		GameRegistry.registerTileEntity(TileKabel.class, "tileKabel");
		GameRegistry.registerTileEntity(TileMaster.class, "tileMaster");
		GameRegistry.registerTileEntity(TileRequest.class, "tileRequest");
//		GameRegistry.registerTileEntity(TileFRequest.class, "tileFRequest");
		// GameRegistry.registerTileEntity(TileContainer.class,
		// "tileContainer");
		// GameRegistry.registerTileEntity(TileCrafter.class, "tileCrafter");
		GameRegistry.registerTileEntity(TileIndicator.class, "tileIndicator");
		GameRegistry.registerTileEntity(TileAnnexer.class, "tileAnnexer");
//		GameRegistry.registerTileEntity(TileFannexer.class, "tileFannexer");
		GameRegistry.registerTileEntity(TileItemBox.class, "tileItemBox");
//		GameRegistry.registerTileEntity(TileFluidBox.class, "tileFluidBox");
		GameRegistry.registerTileEntity(TileToggler.class, "tileToggler");

	}

}
