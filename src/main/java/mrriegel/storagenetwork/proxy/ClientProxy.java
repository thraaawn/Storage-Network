package mrriegel.storagenetwork.proxy;
import mrriegel.storagenetwork.ModBlocks;
import mrriegel.storagenetwork.ModItems;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.cable.TileCable;
import mrriegel.storagenetwork.items.ItemUpgrade;
import mrriegel.storagenetwork.render.CableRenderer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
  @Override
  public void preInit(FMLPreInitializationEvent event) {
    super.preInit(event);
    registerRenderers();
    registerItemModels();
  }
  @Override
  public void init(FMLInitializationEvent event) {
    super.init(event);
  }
  @Override
  public void postInit(FMLPostInitializationEvent event) {
    super.postInit(event);
  }
  public void registerItemModels() {

  }
  public void registerRenderers() {
    ClientRegistry.bindTileEntitySpecialRenderer(TileCable.class, new CableRenderer());
  }
}
