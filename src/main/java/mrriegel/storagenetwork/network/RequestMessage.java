package mrriegel.storagenetwork.network;
import java.util.List;
import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.helper.FilterItem;
import mrriegel.storagenetwork.helper.StackWrapper;
import mrriegel.storagenetwork.master.TileMaster;
import mrriegel.storagenetwork.remote.ContainerRemote;
import mrriegel.storagenetwork.remote.ItemRemote;
import mrriegel.storagenetwork.request.ContainerRequest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.ItemHandlerHelper;

public class RequestMessage implements IMessage, IMessageHandler<RequestMessage, IMessage> {
  int id;
  ItemStack stack;
  boolean shift, ctrl;
  public RequestMessage() {}
  public RequestMessage(int id, ItemStack stack, boolean shift, boolean ctrl) {
    this.id = id;
    this.stack = stack;
    this.shift = shift;
    this.ctrl = ctrl;
  }
  @Override
  public IMessage onMessage(final RequestMessage message, final MessageContext ctx) {
    IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
    mainThread.addScheduledTask(new Runnable() {
      @Override
      public void run() {
        if (ctx.getServerHandler().player.openContainer instanceof ContainerRequest) {
          TileMaster tile = (TileMaster) ctx.getServerHandler().player.world.getTileEntity(((ContainerRequest) ctx.getServerHandler().player.openContainer).tile.getMaster());
          if (tile == null)
            return;
          // System.out.println("!RequestMessage message.stack == "+message.stack);
          int in = message.stack.isEmpty() ? 0 : tile.getAmount(new FilterItem(message.stack, true, false, true));
          //   System.out.println("!RequestMessage in == "+in);
          ItemStack stack;
          if (message.stack.isEmpty()) {
            stack = ItemStack.EMPTY;
          }
          else {
            //   System.out.println("!RequestMessage  message.id = == "+ message.id );
            int ss = message.id == 0 ? message.stack.getMaxStackSize() : message.ctrl ? 1 : Math.max(Math.min(message.stack.getMaxStackSize() / 2, in / 2), 1);
            // System.out.println("!RequestMessage  ssssssss "+ ss );
            stack = tile.request(
                new FilterItem(message.stack, true, false, true),
                ss, false);
          }
          if (!stack.isEmpty()) {
            if (message.shift) {
              ItemHandlerHelper.giveItemToPlayer(ctx.getServerHandler().player, stack);
            }
            else {
              ctx.getServerHandler().player.inventory.setItemStack(stack);
              PacketHandler.INSTANCE.sendTo(new StackMessage(stack), ctx.getServerHandler().player);
            }
          }
          List<StackWrapper> list = tile.getStacks();
          PacketHandler.INSTANCE.sendTo(new StacksMessage(list, tile.getCraftableStacks(list)), ctx.getServerHandler().player);
          ctx.getServerHandler().player.openContainer.detectAndSendChanges();
        }
        else if (ctx.getServerHandler().player.openContainer instanceof ContainerRemote) {
          TileMaster tile = ItemRemote.getTile(ctx.getServerHandler().player.inventory.getCurrentItem());
          if (tile == null)
            return;
          int in = message.stack.isEmpty() ? 0 : tile.getAmount(new FilterItem(message.stack, true, false, true));
          ItemStack stack = message.stack.isEmpty() ? ItemStack.EMPTY : tile.request(new FilterItem(message.stack, true, false, true), message.id == 0 ? message.stack.getMaxStackSize() : message.ctrl ? 1 : Math.max(Math.min(message.stack.getMaxStackSize() / 2, in / 2), 1), false);
          if (!stack.isEmpty()) {
            if (message.shift) {
              ItemHandlerHelper.giveItemToPlayer(ctx.getServerHandler().player, stack);
            }
            else {
              ctx.getServerHandler().player.inventory.setItemStack(stack);
              PacketHandler.INSTANCE.sendTo(new StackMessage(stack), ctx.getServerHandler().player);
            }
          }
          List<StackWrapper> list = tile.getStacks();
          PacketHandler.INSTANCE.sendTo(new StacksMessage(list, tile.getCraftableStacks(list)), ctx.getServerHandler().player);
          ctx.getServerHandler().player.openContainer.detectAndSendChanges();
        }
      }
    });
    return null;
  }
  @Override
  public void fromBytes(ByteBuf buf) {
    this.id = buf.readInt();
    this.stack = ByteBufUtils.readItemStack(buf);
    this.shift = buf.readBoolean();
    this.ctrl = buf.readBoolean();
  }
  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(this.id);
    ByteBufUtils.writeItemStack(buf, this.stack);
    buf.writeBoolean(this.shift);
    buf.writeBoolean(this.ctrl);
  }
}
