package mrriegel.storagenetwork.network;

import java.util.ArrayList;
import java.util.List;
import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.ProcessRequestModel;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.block.control.ProcessWrapper;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.gui.IStorageContainer;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RequestCableMessage implements IMessage, IMessageHandler<RequestCableMessage, IMessage> {

  public RequestCableMessage() {}

  @Override
  public IMessage onMessage(final RequestCableMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        TileMaster tileMaster = null;
        if (player.openContainer instanceof IStorageContainer) {
          IStorageContainer ctr = (IStorageContainer) player.openContainer;
          tileMaster = ctr.getTileMaster();
        }
        if (tileMaster == null) {
          //maybe the table broke after doing this, rare case
          return;
        }
        List<TileEntity> links = tileMaster.getAttachedTileEntities();
        List<TileCable> processCables = tileMaster.getAttachedCables(links, ModBlocks.processKabel);
        List<ProcessWrapper> list = new ArrayList<>();
        //group together any with that match
        //meaning: same output, AND same machine blockid 
        for (TileCable tileCable : processCables) {
          IBlockState blockState = player.world.getBlockState(tileCable.getConnectedInventory());
          String name = blockState.getBlock().getLocalizedName();
          try {
            ItemStack pickBlock = blockState.getBlock().getPickBlock(blockState, player.rayTrace(1.0, 0), player.world,
                tileCable.getConnectedInventory(), player);
            if (pickBlock.isEmpty() == false) {
              name = pickBlock.getDisplayName();
            }
          }
          catch (Exception e) {
            StorageNetwork.instance.logger.error("Error with display name ", e);
          }
          ProcessRequestModel proc = tileCable.getProcessModel();
          //if list of models then wrapper would not need to change at all
          ProcessWrapper processor = new ProcessWrapper(tileCable.getPos(), tileCable.getFirstRecipeOut(),
              proc.getCount(), name, proc.isAlwaysActive());
          processor.ingredients = tileCable.getProcessIngredients();
          processor.blockId = blockState.getBlock().getRegistryName();
          list.add(processor);
        }
        //now all cables have been wraped to send related info 
        //now send wrappers back to gui
        PacketRegistry.INSTANCE.sendTo(new CableRefreshClientMessage(list), player);
        player.openContainer.detectAndSendChanges();
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    //    this.id = buf.readInt();
    //    this.stack = ByteBufUtils.readItemStack(buf);
    //    this.shift = buf.readBoolean();
    //    this.ctrl = buf.readBoolean();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    //    buf.writeInt(this.id);
    //    ByteBufUtils.writeItemStack(buf, stack);
    //    buf.writeBoolean(this.shift);
    //    buf.writeBoolean(this.ctrl);
  }
}
