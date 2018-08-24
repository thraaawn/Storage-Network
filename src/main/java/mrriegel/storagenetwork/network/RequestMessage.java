package mrriegel.storagenetwork.network;

import java.util.ArrayList;
import java.util.List;
import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.block.request.ContainerRequest;
import mrriegel.storagenetwork.item.remote.ContainerRemote;
import mrriegel.storagenetwork.item.remote.ItemRemote;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.data.FilterItem;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.ItemHandlerHelper;

public class RequestMessage implements IMessage, IMessageHandler<RequestMessage, IMessage> {

  private int id = 0;
  private ItemStack stack = ItemStack.EMPTY;
  private boolean shift, ctrl;

  public RequestMessage() {}

  public RequestMessage(int id, ItemStack stack, boolean shift, boolean ctrl) {
    this.id = id;
    this.stack = stack;
    this.shift = shift;
    this.ctrl = ctrl;
  }

  @Override
  public IMessage onMessage(final RequestMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        TileMaster tileMaster = null;
        if (player.openContainer instanceof ContainerRequest) {
          ContainerRequest ctrRequest = (ContainerRequest) player.openContainer;
          tileMaster = (TileMaster) player.world.getTileEntity(ctrRequest.getTileRequest().getMaster());
        }
        else if (player.openContainer instanceof ContainerRemote) {
          tileMaster = ItemRemote.getTile(player.inventory.getCurrentItem());
        }
        if (tileMaster == null) {
          //maybe the table broke after doing this, rare case
          return;
        }
        int in = tileMaster.getAmount(new FilterItem(message.stack, true, false, true));
        // int in = tile.getAmount(new FilterItem(message.stack, true, false, true));
        ItemStack stack;

        int sizeRequested = Math.max(
            message.id == 0 ? message.stack.getMaxStackSize()
                : message.ctrl ? 1 : Math.min(message.stack.getMaxStackSize() / 2, in / 2),
            1);
        stack = tileMaster.request(
            new FilterItem(message.stack, true, false, true),
            sizeRequested, false);
        if (stack.isEmpty()) {
          //try again with NBT as false 
          stack = tileMaster.request(
              new FilterItem(message.stack, true, false, false),
              sizeRequested, false);
        }
        if (!stack.isEmpty()) {
          if (message.shift) {
            ItemHandlerHelper.giveItemToPlayer(player, stack);
          }
          else {
            //when player TAKES an item, go here
            player.inventory.setItemStack(stack);
            PacketRegistry.INSTANCE.sendTo(new StackMessage(stack), player);
          }
        }
        List<StackWrapper> list = tileMaster.getStacks();
        PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, new ArrayList<StackWrapper>()), player);
        player.openContainer.detectAndSendChanges();
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
    ByteBufUtils.writeItemStack(buf, stack);
    buf.writeBoolean(this.shift);
    buf.writeBoolean(this.ctrl);
  }
}
