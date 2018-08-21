package mrriegel.storagenetwork.proxy;

import mrriegel.storagenetwork.block.cable.CableRenderer;
import mrriegel.storagenetwork.block.cable.TileCable;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

  @Override
  public void preInit(FMLPreInitializationEvent event) {
    super.preInit(event);
    ClientRegistry.bindTileEntitySpecialRenderer(TileCable.class, new CableRenderer());
  }
}
