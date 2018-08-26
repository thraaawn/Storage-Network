package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.cable.ContainerCable;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CableFilterMessage implements IMessage, IMessageHandler<CableFilterMessage, IMessage> {

  private int index;
  private StackWrapper wrap;
  private boolean ore, meta;

  public CableFilterMessage() {}

  public CableFilterMessage(int index, StackWrapper wrap, boolean ore, boolean meta) {
    this.index = index;
    this.wrap = wrap;
    this.ore = ore;
    this.meta = meta;
  }

  @Override
  public IMessage onMessage(final CableFilterMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        if (player.openContainer instanceof ContainerCable) {
          ContainerCable con = (ContainerCable) player.openContainer;
          TileCable tile = con.getTile();
          if (message.wrap != null && message.index >= 0) {
            tile.getFilter().put(message.index, message.wrap);
          }
          tile.setOres(message.ore);
          tile.setMeta(message.meta);
          tile.markDirty();
          //          con.slotChanged(); 
        }
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.index = buf.readInt();
    this.ore = buf.readBoolean();
    this.meta = buf.readBoolean();
    this.wrap = StackWrapper.loadStackWrapperFromNBT(ByteBufUtils.readTag(buf));
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(this.index);
    buf.writeBoolean(this.ore);
    buf.writeBoolean(this.meta);
    NBTTagCompound nbt = new NBTTagCompound();
    if (this.wrap != null)
      this.wrap.writeToNBT(nbt);
    ByteBufUtils.writeTag(buf, nbt);
  }
}
