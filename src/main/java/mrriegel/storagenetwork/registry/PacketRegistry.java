package mrriegel.storagenetwork.registry;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.network.*;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketRegistry {

  public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(StorageNetwork.MODID);

  public static void init() {
    int id = 0;
    INSTANCE.registerMessage(CableDataMessage.class, CableDataMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(StackRefreshClientMessage.class, StackRefreshClientMessage.class, id++, Side.CLIENT);
    INSTANCE.registerMessage(RequestMessage.class, RequestMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(ClearRecipeMessage.class, ClearRecipeMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(SortMessage.class, SortMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(RecipeMessage.class, RecipeMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(CableLimitMessage.class, CableLimitMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(InsertMessage.class, InsertMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(StackResponseClientMessage.class, StackResponseClientMessage.class, id++, Side.CLIENT);
    INSTANCE.registerMessage(CableFilterMessage.class, CableFilterMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(CableRefreshClientMessage.class, CableRefreshClientMessage.class, id++, Side.CLIENT);
    INSTANCE.registerMessage(RequestCableMessage.class, RequestCableMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(CableControlMessage.class, CableControlMessage.class, id++, Side.SERVER);
  }
}
