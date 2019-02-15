package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.gui.IStorageContainer;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
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

        PacketRegistry.INSTANCE.sendTo(new CableRefreshClientMessage(tileMaster.getProcessors()), player);
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
