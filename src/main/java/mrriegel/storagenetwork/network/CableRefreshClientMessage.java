package mrriegel.storagenetwork.network;

import java.util.ArrayList;
import java.util.List;
import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.control.GuiControl;
import mrriegel.storagenetwork.block.control.ProcessWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.common.util.Constants;
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
public class CableRefreshClientMessage implements IMessage, IMessageHandler<CableRefreshClientMessage, IMessage> {

  private List<ProcessWrapper> cables;

  public CableRefreshClientMessage() {}

  public CableRefreshClientMessage(List<ProcessWrapper> list) {
    cables = list;
  }

  @Override
  public IMessage onMessage(final CableRefreshClientMessage message, final MessageContext ctx) {
    IThreadListener mainThread = Minecraft.getMinecraft();
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiControl) {
          GuiControl gui = (GuiControl) Minecraft.getMinecraft().currentScreen;
          gui.setTiles(message.cables);
        }
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    NBTTagCompound tags = ByteBufUtils.readTag(buf);
    NBTTagList tagList = tags.getTagList("invo", Constants.NBT.TAG_COMPOUND);
    cables = new ArrayList<>();
    for (int i = 0; i < tagList.tagCount(); i++) {
      NBTTagCompound tag = tagList.getCompoundTagAt(i);
      ProcessWrapper rec = new ProcessWrapper();
      rec.readFromNBT(tag);
      this.cables.add(rec);
    }
  }

  @Override
  public void toBytes(ByteBuf buf) {
    NBTTagCompound tags = new NBTTagCompound();
    NBTTagList itemList = new NBTTagList();
    for (ProcessWrapper rec : cables) {
      NBTTagCompound mytag = new NBTTagCompound();
      rec.writeToNBT(mytag);
      itemList.appendTag(mytag);
    }
    tags.setTag("invo", itemList);
    ByteBufUtils.writeTag(buf, tags);
  }
}
