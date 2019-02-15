package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.data.ItemStackMatcher;
import mrriegel.storagenetwork.gui.IStorageContainer;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.util.UtilInventory;
import net.minecraft.entity.player.EntityPlayerMP;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeMessage implements IMessage, IMessageHandler<RecipeMessage, IMessage> {

  /** @formatter:off
   * Sample data structure can have list of items for each slot (example: ore dictionary)
   * {
   *  s0:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s1:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s2:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s3:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s4:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s5:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s6:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s7:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}],
   *  s8:[{id:"ic2:ingot",Count:1b,Damage:2s},{id:"immersiveengineering:metal",Count:1b,Damage:0s}]
   *  }
   * @formatter:on
   */
  private NBTTagCompound nbt;
  private int index = 0;

  public RecipeMessage() {}

  public RecipeMessage(NBTTagCompound nbt) {
    this.nbt = nbt;
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
    EntityPlayerMP player = ctx.getServerHandler().player;
    IThreadListener mainThread = (WorldServer) player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        if (player.openContainer instanceof IStorageContainer == false) {
          return;
        }
        IStorageContainer ctr = (IStorageContainer) player.openContainer;
        TileMaster master = ctr.getTileMaster();
        if (master == null) {
          return;
        }
        InventoryCrafting craftMatrix = ctr.getCraftMatrix();
        String[] oreDictKeys;// = oreDictKey.split(",");
        for (int slot = 0; slot < 9; slot++) {
          Map<Integer, ItemStack> map = new HashMap<Integer, ItemStack>();
          //if its a string, then ore dict is allowed
          /*********
           * parse nbt of the slot, whether its ore dict, itemstack, ore empty
           **********/
          boolean isOreDict;
          if (message.nbt.hasKey("s" + slot, Constants.NBT.TAG_STRING)) {
            //i am 80% sure this ore string branch never hits anymore
            //JEI recipe transfer sends list of items only
            isOreDict = true;
            /*************
             * NEW: each item stack could be in MULTIPLE ore dicts. such as betterthanmods multiblocks
             **/
            oreDictKeys = message.nbt.getString("s" + slot).split(",");
            List<ItemStack> l = new ArrayList<ItemStack>();
            for (String oreKey : oreDictKeys) {
              l.addAll(OreDictionary.getOres(oreKey));
            }
            //              List<ItemStack> l = OreDictionary.getOres(oreKey);
            for (int i = 0; i < l.size(); i++) {
              map.put(i, l.get(i));
            }
            //  StorageNetwork.log(message.nbt.getString("s" + slot) + " ore dict keyS found  " + l);
          }
          else { // is not string, so just simple item stacks
            isOreDict = false;
            NBTTagList invList = message.nbt.getTagList("s" + slot, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < invList.tagCount(); i++) {
              NBTTagCompound stackTag = invList.getCompoundTagAt(i);
              ItemStack s = new ItemStack(stackTag);
              map.put(i, s);
            }
            //              StorageNetwork.log(slot + "  slot has potential [ore] matches  " + map.keySet().size());
            //   StorageNetwork.log(slot + "  slot has potential [ore] matches  " + map.values());
          }
          /********* end parse nbt of this current slot ******/
          /********** now start trying to fill in recipe **/
          for (int i = 0; i < map.size(); i++) {
            ItemStack stackCurrent = map.get(i);
            if (stackCurrent == null || stackCurrent.isEmpty()) {
              continue;
            }
            ItemStackMatcher itemStackMatcher = new ItemStackMatcher(stackCurrent);
            itemStackMatcher.setNbt(true);
            itemStackMatcher.setOre(isOreDict);//important: set this for correct matching
            //   StorageNetwork.log("CALL exctractItem   " + stackCurrent + " isOreDict " + isOreDict);
            ItemStack ex = UtilInventory.extractItem(new PlayerMainInvWrapper(player.inventory), itemStackMatcher, 1, true);
            /*********** First try and use the players inventory **/
            //              int slot = j ;//- 1;
            if (ex != null && !ex.isEmpty() && craftMatrix.getStackInSlot(slot).isEmpty()) {
              UtilInventory.extractItem(new PlayerMainInvWrapper(player.inventory), itemStackMatcher, 1, false);
              //make sure to add the real item after the nonsimulated withdrawl is complete https://github.com/PrinceOfAmber/Storage-Network/issues/16
              craftMatrix.setInventorySlotContents(slot, ex);
              break;
            }
            /********* now find it from the network ***/
            stackCurrent = master.request(!stackCurrent.isEmpty() ? itemStackMatcher : null, 1, false);
            if (!stackCurrent.isEmpty() && craftMatrix.getStackInSlot(slot).isEmpty()) {
              craftMatrix.setInventorySlotContents(slot, stackCurrent);
              break;
            }
          }
          /************** finished recipe population **/
        }
        //now make sure client sync happens.
        ctr.slotChanged();

        List<ItemStack> list = master.getStacks();
        PacketRegistry.INSTANCE.sendTo(new StackRefreshClientMessage(list, new ArrayList<>()), player);
      }//end run
    });
    return null;
  }
}
