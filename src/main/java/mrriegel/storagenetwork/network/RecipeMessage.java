package mrriegel.storagenetwork.network;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.ContainerNetworkBase;
import mrriegel.storagenetwork.helper.FilterItem;
import mrriegel.storagenetwork.helper.InvHelper;
import mrriegel.storagenetwork.helper.StackWrapper;
import mrriegel.storagenetwork.master.TileMaster;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeMessage implements IMessage, IMessageHandler<RecipeMessage, IMessage> {
  NBTTagCompound nbt;
  int index;
  public RecipeMessage() {}
  public RecipeMessage(NBTTagCompound nbt, int index) {
    this.nbt = nbt;
    this.index = index;
  }
  @Override
  public void fromBytes(ByteBuf buf) {
    this.nbt = ByteBufUtils.readTag(buf);
    this.index = buf.readInt();
  }
  @Override
  public void toBytes(ByteBuf buf) {
    ByteBufUtils.writeTag(buf, nbt);
    buf.writeInt(this.index);
  }
  @Override
  public IMessage onMessage(final RecipeMessage message, final MessageContext ctx) {
    IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world;
    mainThread.addScheduledTask(new Runnable() {
      @Override
      public void run() {
        Container c = ctx.getServerHandler().playerEntity.openContainer;
        //        World w = ctx.getServerHandler().playerEntity.world;
        if (c instanceof ContainerNetworkBase) {
          ContainerNetworkBase ctr = (ContainerNetworkBase) c;
          TileMaster m = ctr.getTileMaster();
          InventoryCrafting craftMatrix = ctr.getCraftMatrix();
          // if (message.index == 0) {
          //          if (!(ctx.getServerHandler().playerEntity.openContainer instanceof ContainerRequest))
          //            return;
          //   ContainerRequest con = (ContainerRequest) ctx.getServerHandler().playerEntity.openContainer;
          //  TileMaster tile = (TileMaster) ctx.getServerHandler().playerEntity.world.getTileEntity(con.tile.getMaster());
          if (m == null) { return; }
          for (int j = 1; j < 10; j++) {
            Map<Integer, ItemStack> map = new HashMap<Integer, ItemStack>();
            if (message.nbt.hasKey("s" + j, Constants.NBT.TAG_STRING)) {
              List<ItemStack> l = OreDictionary.getOres(message.nbt.getString("s" + j));
              for (int i = 0; i < l.size(); i++)
                map.put(i, l.get(i));
            }
            else {
              NBTTagList invList = message.nbt.getTagList("s" + j, Constants.NBT.TAG_COMPOUND);
              for (int i = 0; i < invList.tagCount(); i++) {
                NBTTagCompound stackTag = invList.getCompoundTagAt(i);
                ItemStack s = new ItemStack(stackTag);
                map.put(i, s);
              }
            }
            for (int i = 0; i < map.size(); i++) {
              ItemStack s = map.get(i);
              if (s == null || s.isEmpty()) {
                continue;
              }
              ItemStack ex = InvHelper.extractItem(new PlayerMainInvWrapper(ctx.getServerHandler().playerEntity.inventory), new FilterItem(s), 1, true);
              if (ex != null && !ex.isEmpty() && craftMatrix.getStackInSlot(j - 1).isEmpty()) {
                craftMatrix.setInventorySlotContents(j - 1, InvHelper.extractItem(new PlayerMainInvWrapper(ctx.getServerHandler().playerEntity.inventory), new FilterItem(s), 1, false));
                break;
              }
              s = m.request(!map.get(i).isEmpty() ? new FilterItem(map.get(i)) : null, 1, false);
              if (s != null && craftMatrix.getStackInSlot(j - 1).isEmpty()) {
                craftMatrix.setInventorySlotContents(j - 1, s);
                break;
              }
            }
          }
          ctr.slotChanged();
          List<StackWrapper> list = m.getStacks();
          PacketHandler.INSTANCE.sendTo(new StacksMessage(list, m.getCraftableStacks(list)), ctx.getServerHandler().playerEntity);
        }
      }
      //}
    });
    return null;
  }
}
