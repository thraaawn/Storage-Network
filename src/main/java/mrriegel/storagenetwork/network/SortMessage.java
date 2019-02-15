package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.request.TileRequest;
import mrriegel.storagenetwork.data.EnumSortType;
import mrriegel.storagenetwork.gui.IStorageContainer;
import mrriegel.storagenetwork.util.NBTHelper;
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
        if (player.openContainer instanceof IStorageContainer) {
          if (((IStorageContainer) player.openContainer).isRequest()) {
            TileEntity tileEntity = player.world.getTileEntity(message.pos);
            if (tileEntity instanceof TileRequest) {
              TileRequest tile = (TileRequest) tileEntity;
              tile.setSort(message.sort);
              tile.setDownwards(message.direction);
            }
            tileEntity.markDirty();
          }
          else {
            ItemStack stackPlayerHeld = player.inventory.getCurrentItem();
            NBTHelper.setBoolean(stackPlayerHeld, "down", message.direction);
            NBTHelper.setString(stackPlayerHeld, "sort", message.sort.toString());
            return;
          }
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
