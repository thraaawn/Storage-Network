package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.request.ContainerRequest;
import mrriegel.storagenetwork.block.request.TileRequest;
import mrriegel.storagenetwork.item.remote.ContainerRemote;
import mrriegel.storagenetwork.util.NBTHelper;
import mrriegel.storagenetwork.util.data.EnumSortType;
import net.minecraft.entity.player.EntityPlayerMP;
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

  private BlockPos pos;
  private boolean direction;
  private EnumSortType sort;

  public SortMessage() {}

  public SortMessage(BlockPos pos, boolean direction, EnumSortType sort) {
    this.pos = pos;
    this.direction = direction;
    this.sort = sort;
  }

  @Override
  public IMessage onMessage(final SortMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        if (player.openContainer instanceof ContainerRemote) {//|| ctx.getServerHandler().playerEntity.openContainer instanceof ContainerFRemote
          ItemStack s = player.inventory.getCurrentItem();
          NBTHelper.setBoolean(s, "down", message.direction);
          NBTHelper.setString(s, "sort", message.sort.toString());
          return;
        }
        if (player.openContainer instanceof ContainerRequest) {//|| ctx.getServerHandler().playerEntity.openContainer instanceof ContainerFRequest
          TileEntity t = player.world.getTileEntity(message.pos);
          if (t instanceof TileRequest) {
            TileRequest tile = (TileRequest) t;
            tile.setSort(message.sort);
            tile.setDownwards(message.direction);
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
