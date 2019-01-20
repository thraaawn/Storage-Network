package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.cable.io.ContainerCableIO;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CableLimitMessage implements IMessage, IMessageHandler<CableLimitMessage, IMessage> {

  private int limit;
  private ItemStack stack;

  public CableLimitMessage() {}

  public CableLimitMessage(int limit, ItemStack stack) {
    super();
    this.limit = limit;
    this.stack = stack;
  }

  @Override
  public IMessage onMessage(final CableLimitMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(() -> {
      if (player.openContainer instanceof ContainerCableIO) {
        ContainerCableIO con = (ContainerCableIO) player.openContainer;
        if (con == null || con.autoIO == null) {
          return;
        }

        con.autoIO.operationLimit = message.limit;
        con.autoIO.operationStack = message.stack;
        con.tile.markDirty();
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.limit = buf.readInt();
    this.stack = ByteBufUtils.readItemStack(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(this.limit);
    ByteBufUtils.writeItemStack(buf, this.stack);
  }
}
