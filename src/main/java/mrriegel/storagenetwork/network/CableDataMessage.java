package mrriegel.storagenetwork.network;

import java.util.HashMap;
import java.util.Map;
import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.AbstractFilterTile;
import mrriegel.storagenetwork.block.cable.ProcessRequestModel;
import mrriegel.storagenetwork.block.cable.ProcessRequestModel.ProcessStatus;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.util.UtilTileEntity;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.IItemHandler;

public class CableDataMessage implements IMessage, IMessageHandler<CableDataMessage, IMessage> {

  public enum CableMessageType {
    PRIORITY_DOWN, PRIORITY_UP, P_ONOFF, TOGGLE_WHITELIST, TOGGLE_MODE, IMPORT_FILTER, TOGGLE_WAY, P_FACE_TOP, P_FACE_BOTTOM, TOGGLE_P_RESTARTTRIGGER, P_CTRL_MORE, P_CTRL_LESS;
  }

  private int id;
  private int value = 0;
  private BlockPos pos;

  public CableDataMessage() {}

  public CableDataMessage(int id, BlockPos pos) {
    this.id = id;
    this.pos = pos;
  }

  public CableDataMessage(int id, BlockPos pos, int value) {
    this(id, pos);
    this.value = value;
  }

  @Override
  public IMessage onMessage(final CableDataMessage message, final MessageContext ctx) {
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        TileEntity t = player.world.getTileEntity(message.pos);
        if (t instanceof AbstractFilterTile) {
          AbstractFilterTile tile = (AbstractFilterTile) t;
          TileCable tileCable = null;
          if (t instanceof TileCable)
            tileCable = (TileCable) tile;
          CableMessageType type = CableMessageType.values()[message.id];
          switch (type) {
            case TOGGLE_P_RESTARTTRIGGER:
              //stop listening for result, export recipe into block
              if (tileCable != null)
                tileCable.getRequest().setStatus(ProcessStatus.EXPORTING);
            break;
            case PRIORITY_DOWN:
              tile.setPriority(tile.getPriority() - 1);
            break;
            case PRIORITY_UP:
              tile.setPriority(tile.getPriority() + 1);
            break;
            case TOGGLE_WHITELIST:
              tile.setWhite(!tile.isWhitelist());
            break;
            case TOGGLE_MODE://4 
              if (tileCable != null)
                tileCable.setMode(!tileCable.isMode());
            break;
            case IMPORT_FILTER:
              if (tile.getInventory() != null) {
                IItemHandler inv = tile.getInventory();
                tile.setWhite(true);
                int size = 9 * 2;
                for (int i = 0; i < size; i++) {
                  tile.getFilter().put(i, null);
                }
                //track used so if a chest is full of cobble we dont double up
                int index = 0;
                Map<Item, Boolean> used = new HashMap<>();
                for (int i = 0; i < inv.getSlots() && index < size; i++) {
                  ItemStack stackHereCopy = inv.getStackInSlot(i).copy();
                  if (!stackHereCopy.isEmpty() && !used.containsKey(stackHereCopy.getItem())) {
                    used.put(stackHereCopy.getItem(), true);
                    stackHereCopy.setCount(1);
                    tile.getFilter().put(index, new StackWrapper(stackHereCopy, 1));
                    index++;
                  }
                }
              }
            break;
            case TOGGLE_WAY:
              tile.setWay(tile.getWay().next());
            break;
            case P_FACE_BOTTOM:
              if (tileCable != null)
                tileCable.processingBottom = EnumFacing.values()[message.value];
            break;
            case P_FACE_TOP:
              if (tileCable != null) {
                tileCable.processingTop = EnumFacing.values()[message.value];
                //                StorageNetwork.log(tileCable.processingTop.name() + " server is ?" + message.value);
              }
            break;
            case P_ONOFF:
              //process cable toggle always on
              if (tileCable != null) {
                ProcessRequestModel m = tileCable.getProcessModel();
                m.setAlwaysActive(message.value == 1);
                tileCable.setProcessModel(m);
              }
            break;
            case P_CTRL_LESS:
              if (tileCable != null) {
                tileCable.getProcessModel().setCount(message.value);
              }
            break;
            case P_CTRL_MORE:
              if (tileCable != null) {
                ProcessRequestModel m = tileCable.getProcessModel();
                m.setCount(message.value);
                tileCable.setProcessModel(m);
              }
            break;
          }//end of switch
          tile.markDirty();
          UtilTileEntity.updateTile(t.getWorld(), t.getPos());
        } //not the right TE 
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BlockPos.fromLong(buf.readLong());
    this.id = buf.readInt();
    value = buf.readInt();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeLong(this.pos.toLong());
    buf.writeInt(this.id);
    buf.writeInt(value);
  }
}
