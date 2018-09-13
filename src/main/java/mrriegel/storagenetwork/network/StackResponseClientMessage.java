package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Used by InsertMessage and RequestMessage as a response back to the client
 * 
 *
 */
public class StackResponseClientMessage implements IMessage, IMessageHandler<StackResponseClientMessage, IMessage> {

  private ItemStack stack;

  public StackResponseClientMessage() {}

  public StackResponseClientMessage(ItemStack a) {
    this.stack = a;
  }

  @Override
  public IMessage onMessage(final StackResponseClientMessage message, final MessageContext ctx) {
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
  }

  @Override
  public void toBytes(ByteBuf buf) {
    ByteBufUtils.writeItemStack(buf, this.stack);
  }
}
