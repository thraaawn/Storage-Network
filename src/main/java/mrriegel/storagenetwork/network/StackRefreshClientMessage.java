package mrriegel.storagenetwork.network;

import java.util.List;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.gui.GuiContainerStorageInventory;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Refresh the current screen with large data set of stacks.
 * 
 * Used by Containers displaying network inventory as well as most other packets that perform small actions
 *
 */
public class StackRefreshClientMessage implements IMessage, IMessageHandler<StackRefreshClientMessage, IMessage> {

  private int size, csize;
  private List<StackWrapper> stacks, craftableStacks;

  public StackRefreshClientMessage() {}

  public StackRefreshClientMessage(List<StackWrapper> stacks, List<StackWrapper> craftableStacks) {
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
        if (Minecraft.getMinecraft().currentScreen instanceof GuiContainerStorageInventory) {
          GuiContainerStorageInventory gui = (GuiContainerStorageInventory) Minecraft.getMinecraft().currentScreen;
          gui.stacks = message.stacks;
          gui.craftableStacks = message.craftableStacks;
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
      stacks.add(StackWrapper.loadStackWrapperFromNBT(ByteBufUtils.readTag(buf)));
    }
    craftableStacks = Lists.newArrayList();
    for (int i = 0; i < csize; i++) {
      craftableStacks.add(StackWrapper.loadStackWrapperFromNBT(ByteBufUtils.readTag(buf)));
    }
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(this.size);
    buf.writeInt(this.csize);
    for (StackWrapper w : stacks) {
      NBTTagCompound compound = new NBTTagCompound();
      w.writeToNBT(compound);
      ByteBufUtils.writeTag(buf, compound);
    }
    for (StackWrapper w : craftableStacks) {
      NBTTagCompound compound = new NBTTagCompound();
      w.writeToNBT(compound);
      ByteBufUtils.writeTag(buf, compound);
    }
  }
}
