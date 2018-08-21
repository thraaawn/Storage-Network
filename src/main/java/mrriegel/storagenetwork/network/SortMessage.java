package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.request.ContainerRequest;
import mrriegel.storagenetwork.block.request.TileRequest;
import mrriegel.storagenetwork.block.request.TileRequest.EnumSortType;
import mrriegel.storagenetwork.item.remote.ContainerRemote;
import mrriegel.storagenetwork.util.NBTHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SortMessage implements IMessage, IMessageHandler<SortMessage, IMessage> {

  BlockPos pos;
  boolean direction;
  EnumSortType sort;

  public SortMessage() {}

  public SortMessage(BlockPos pos, boolean direction, EnumSortType sort) {
    this.pos = pos;
    this.direction = direction;
    this.sort = sort;
  }

  @Override
  public IMessage onMessage(final SortMessage message, final MessageContext ctx) {
    IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        if (ctx.getServerHandler().player.openContainer instanceof ContainerRemote) {//|| ctx.getServerHandler().playerEntity.openContainer instanceof ContainerFRemote
          ItemStack s = ctx.getServerHandler().player.inventory.getCurrentItem();
          NBTHelper.setBoolean(s, "down", message.direction);
          NBTHelper.setString(s, "sort", message.sort.toString());
          return;
        }
        if (ctx.getServerHandler().player.openContainer instanceof ContainerRequest) {//|| ctx.getServerHandler().playerEntity.openContainer instanceof ContainerFRequest
          TileEntity t = ctx.getServerHandler().player.world.getTileEntity(message.pos);
          if (t instanceof TileRequest) {
            TileRequest tile = (TileRequest) t;
            tile.sort = message.sort;
            tile.downwards = message.direction;
          }
          t.markDirty();
        }
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BlockPos.fromLong(buf.readLong());
    this.direction = buf.readBoolean();
    this.sort = EnumSortType.valueOf(ByteBufUtils.readUTF8String(buf));
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeLong(this.pos.toLong());
    buf.writeBoolean(this.direction);
    ByteBufUtils.writeUTF8String(buf, this.sort.toString());
  }
}
