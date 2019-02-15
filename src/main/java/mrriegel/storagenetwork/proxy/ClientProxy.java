package mrriegel.storagenetwork.proxy;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.TesrCable;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.registry.ModBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

  @Override
  public void preInit(FMLPreInitializationEvent event) {
    super.preInit(event);
    ClientRegistry.bindTileEntitySpecialRenderer(TileCable.class, new TesrCable());
  }

  @Override
  public void init(FMLInitializationEvent event) {
    super.init(event);
    ResourceLocation link = new ResourceLocation(StorageNetwork.MODID, "textures/tile/link.png");
    ResourceLocation ex = new ResourceLocation(StorageNetwork.MODID, "textures/tile/ex.png");
    ResourceLocation im = new ResourceLocation(StorageNetwork.MODID, "textures/tile/im.png");
    ResourceLocation storage = new ResourceLocation(StorageNetwork.MODID, "textures/tile/storage.png");
    ResourceLocation process = new ResourceLocation(StorageNetwork.MODID, "textures/tile/process.png");
    TesrCable.addCableRender(ModBlocks.kabel, link);
    TesrCable.addCableRender(ModBlocks.exKabel, ex);
    TesrCable.addCableRender(ModBlocks.imKabel, im);
    TesrCable.addCableRender(ModBlocks.storageKabel, storage);
    TesrCable.addCableRender(ModBlocks.processKabel, process);
  }
}
