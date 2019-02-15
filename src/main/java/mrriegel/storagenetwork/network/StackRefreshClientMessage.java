package mrriegel.storagenetwork.network;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.gui.IStorageInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

/**
 * Refresh the current screen with large data set of stacks.
 *
 * Used by Containers displaying network inventory as well as most other packets that perform small actions
 *
 */
public class StackRefreshClientMessage implements IMessage, IMessageHandler<StackRefreshClientMessage, IMessage> {

  private int size, csize;
  private List<ItemStack> stacks, craftableStacks;

  public StackRefreshClientMessage() {}

  public StackRefreshClientMessage(List<ItemStack> stacks, List<ItemStack> craftableStacks) {
    super();
    this.stacks = stacks;
    this.craftableStacks = craftableStacks;
    this.size = stacks.size();
    this.csize = craftableStacks.size();
  }

  @Override
  public IMessage onMessage(final StackRefreshClientMessage message, final MessageContext ctx) {
    IThreadListener mainThread = Minecraft.getMinecraft();
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        if (Minecraft.getMinecraft().currentScreen instanceof IStorageInventory) {
          IStorageInventory gui = (IStorageInventory) Minecraft.getMinecraft().currentScreen;

          gui.setStacks(message.stacks);
          gui.setCraftableStacks(message.craftableStacks);
        }
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.size = buf.readInt();
    this.csize = buf.readInt();

    stacks = Lists.newArrayList();
    for (int i = 0; i < size; i++) {
      ItemStack stack = new ItemStack(ByteBufUtils.readTag(buf));
      stack.setCount(buf.readInt());
      stacks.add(stack);
    }

    craftableStacks = Lists.newArrayList();
    for (int i = 0; i < csize; i++) {
      ItemStack stack = new ItemStack(ByteBufUtils.readTag(buf));
      stack.setCount(buf.readInt());
      craftableStacks.add(stack);
    }
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(this.size);
    buf.writeInt(this.csize);

    for (ItemStack stack : stacks) {
      ByteBufUtils.writeTag(buf, stack.serializeNBT());
      buf.writeInt(stack.getCount());
    }

    for (ItemStack stack : craftableStacks) {
      ByteBufUtils.writeTag(buf, stack.serializeNBT());
      buf.writeInt(stack.getCount());
    }
  }
}
