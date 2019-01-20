package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.cable.processing.ProcessRequestModel;
import mrriegel.storagenetwork.block.cable.processing.TileCableProcess;
import mrriegel.storagenetwork.api.data.DimPos;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CableControlMessage implements IMessage, IMessageHandler<CableControlMessage, IMessage> {
  private int id;
  private int value = 0;

  private DimPos pos;

  public CableControlMessage() {
  }

  public CableControlMessage(int id, int value, DimPos pos) {
    this.id = id;
    this.value = value;
    this.pos = pos;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.id = buf.readInt();
    this.value = buf.readInt();
    this.pos = new DimPos(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(this.id);
    buf.writeInt(this.value);
    this.pos.writeToByteBuf(buf);
  }

  @Override
  public IMessage onMessage(CableControlMessage message, MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(new Runnable() {
      @Override
      public void run() {
        TileCableProcess processCable = message.pos.getTileEntity(TileCableProcess.class);
        if(processCable == null) {
          return;
        }

        ProcessRequestModel m = processCable.getProcessModel();
        CableDataMessage.CableMessageType type = CableDataMessage.CableMessageType.values()[message.id];
        switch (type) {
          case P_ONOFF:
            //process cable toggle always on
            m.setAlwaysActive(message.value == 1);
            processCable.setProcessModel(m);
            break;
          case P_CTRL_LESS:
            m.setCount(message.value);
            break;
          case P_CTRL_MORE:
            m.setCount(message.value);
            processCable.setProcessModel(m);
            break;
        }

        processCable.markDirty();
        UtilTileEntity.updateTile(processCable.getWorld(), processCable.getPos());
      }
    });

    return null;
  }
}
