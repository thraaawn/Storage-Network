package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.cable.TileCable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CableLimitMessage implements IMessage, IMessageHandler<CableLimitMessage, IMessage> {

  private int limit;
  private BlockPos pos;
  private ItemStack stack;

  public CableLimitMessage() {}

  public CableLimitMessage(int limit, BlockPos pos, ItemStack stack) {
    super();
    this.limit = limit;
    this.pos = pos;
    this.stack = stack;
  }

  @Override
  public IMessage onMessage(final CableLimitMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        TileCable tile = (TileCable) player.world.getTileEntity(message.pos);
        tile.setOperationLimit(message.limit);
        tile.setOperationStack(message.stack);
        tile.markDirty();
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BlockPos.fromLong(buf.readLong());
    this.limit = buf.readInt();
    this.stack = ByteBufUtils.readItemStack(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeLong(this.pos.toLong());
    buf.writeInt(this.limit);
    ByteBufUtils.writeItemStack(buf, this.stack);
  }
}
