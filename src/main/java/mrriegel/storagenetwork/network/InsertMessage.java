package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.gui.IStorageContainer;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class InsertMessage implements IMessage, IMessageHandler<InsertMessage, IMessage> {

  private int dim, mouseButton;
  private ItemStack stack;

  public InsertMessage() {}

  public InsertMessage(int dim, int buttonID, ItemStack stack) {
    this.dim = dim;
    this.stack = stack;
    this.mouseButton = buttonID;
  }

  @Override
  public IMessage onMessage(final InsertMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        TileMaster tileMaster = null;
        if (player.openContainer instanceof IStorageContainer) {
          tileMaster = ((IStorageContainer) player.openContainer).getTileMaster();
        }
        int rest;
        ItemStack send = ItemStack.EMPTY;
        if (message.mouseButton == UtilTileEntity.MOUSE_BTN_LEFT) {//TODO ENUM OR SOMETHING
          rest = tileMaster.insertStack(message.stack, false);
          if (rest != 0)
            send = ItemHandlerHelper.copyStackWithSize(message.stack, rest);
        }
        else if (message.mouseButton == UtilTileEntity.MOUSE_BTN_RIGHT) {
          ItemStack stack1 = message.stack.copy();
          stack1.setCount(1);
          message.stack.shrink(1);
          rest = tileMaster.insertStack(stack1, false) + message.stack.getCount();
          if (rest != 0)
            send = ItemHandlerHelper.copyStackWithSize(message.stack, rest);
        }
        //TODO: WHY TWO messages/?
        player.inventory.setItemStack(send);


        PacketRegistry.INSTANCE.sendTo(new StackResponseClientMessage(send), player);
        List<ItemStack> list = tileMaster.getStacks();
        PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<>()), player);
        player.openContainer.detectAndSendChanges();
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.dim = buf.readInt();
    this.stack = ByteBufUtils.readItemStack(buf);
    this.mouseButton = buf.readInt();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(this.dim);
    ByteBufUtils.writeItemStack(buf, this.stack);
    buf.writeInt(this.mouseButton);
  }
}
