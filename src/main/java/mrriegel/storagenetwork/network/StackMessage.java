package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.StorageNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class StackMessage implements IMessage, IMessageHandler<StackMessage, IMessage> {

  private ItemStack stack;

  public StackMessage() {}

  public StackMessage(ItemStack a) {
    this.stack = a;
  }

  @Override
  public IMessage onMessage(final StackMessage message, final MessageContext ctx) {
    //when player TAKES an item, go here... (maybe other cases too)
    IThreadListener mainThread = Minecraft.getMinecraft();
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        Minecraft.getMinecraft().player.inventory.setItemStack(message.stack);
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.stack = ByteBufUtils.readItemStack(buf);
    StorageNetwork.log("StackMessage readItemStack  " + stack);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    ByteBufUtils.writeItemStack(buf, this.stack);
    StorageNetwork.log("StackMessage writeItemStack  " + stack);
  }
}
