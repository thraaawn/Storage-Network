package mrriegel.storagenetwork.registry;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.network.CableDataMessage;
import mrriegel.storagenetwork.network.ClearMessage;
import mrriegel.storagenetwork.network.FilterMessage;
import mrriegel.storagenetwork.network.InsertMessage;
import mrriegel.storagenetwork.network.LimitMessage;
import mrriegel.storagenetwork.network.RecipeMessage;
import mrriegel.storagenetwork.network.RequestMessage;
import mrriegel.storagenetwork.network.SortMessage;
import mrriegel.storagenetwork.network.StackMessage;
import mrriegel.storagenetwork.network.StacksMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketRegistry {
  public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(StorageNetwork.MODID);
  public static void init() {
    int id = 0;
    INSTANCE.registerMessage(CableDataMessage.class, CableDataMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(StacksMessage.class, StacksMessage.class, id++, Side.CLIENT);
 
    INSTANCE.registerMessage(RequestMessage.class, RequestMessage.class, id++, Side.SERVER);
 
    INSTANCE.registerMessage(ClearMessage.class, ClearMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(SortMessage.class, SortMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(RecipeMessage.class, RecipeMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(LimitMessage.class, LimitMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(InsertMessage.class, InsertMessage.class, id++, Side.SERVER);
    INSTANCE.registerMessage(StackMessage.class, StackMessage.class, id++, Side.CLIENT);
    INSTANCE.registerMessage(FilterMessage.class, FilterMessage.class, id++, Side.SERVER);
 
  }
}
