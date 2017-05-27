package mrriegel.storagenetwork.master;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import mrriegel.storagenetwork.ConfigHandler;
import mrriegel.storagenetwork.IConnectable;
import mrriegel.storagenetwork.cable.TileCable;
import mrriegel.storagenetwork.cable.TileCable.Kind;
import mrriegel.storagenetwork.helper.FilterItem;
import mrriegel.storagenetwork.helper.InvHelper;
import mrriegel.storagenetwork.helper.NBTHelper;
import mrriegel.storagenetwork.helper.StackWrapper;
import mrriegel.storagenetwork.helper.Util;
import mrriegel.storagenetwork.items.ItemUpgrade;
import mrriegel.storagenetwork.tile.AbstractFilterTile;
import mrriegel.storagenetwork.tile.AbstractFilterTile.Direction; 
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileMaster extends TileEntity implements ITickable {
  public Set<BlockPos> connectables;
  public List<BlockPos> storageInventorys;
  public List<StackWrapper> getStacks() {
    List<StackWrapper> stacks = Lists.newArrayList();
    List<AbstractFilterTile> invs = Lists.newArrayList();
    if (connectables == null) {
      refreshNetwork();
    }
    for (BlockPos p : connectables) {
      if (world.getTileEntity(p) instanceof AbstractFilterTile) {
        AbstractFilterTile tile = (AbstractFilterTile) world.getTileEntity(p);
        if (tile.isStorage() && tile.getInventory() != null) {
          invs.add(tile);
        }
      }
    }
    for (AbstractFilterTile t : invs) {
      IItemHandler inv = t.getInventory();
      for (int i = 0; i < inv.getSlots(); i++) {
        if (inv.getStackInSlot(i) != null && !inv.getStackInSlot(i).isEmpty() && t.canTransfer(inv.getStackInSlot(i), Direction.BOTH))
          addToList(stacks, inv.getStackInSlot(i).copy(), inv.getStackInSlot(i).getCount());
      }
    }
    return stacks;
  }
  public int emptySlots() {
    int res = 0;
    //    List<StackWrapper> stacks = Lists.newArrayList();
    List<AbstractFilterTile> invs = Lists.newArrayList();
    for (BlockPos p : connectables) {
      if (world.getTileEntity(p) instanceof AbstractFilterTile) {
        AbstractFilterTile tile = (AbstractFilterTile) world.getTileEntity(p);
        if (tile.isStorage() && tile.getInventory() != null) {
          invs.add(tile);
        }
      }
    }
    for (AbstractFilterTile t : invs) {
      IItemHandler inv = t.getInventory();
      for (int i = 0; i < inv.getSlots(); i++) {
        if (inv.getStackInSlot(i) == null || inv.getStackInSlot(i).isEmpty()) {
          res++;
        }
      }
    }
    return res;
  }
  public List<StackWrapper> getCraftableStacks(List<StackWrapper> stacks) {
    List<StackWrapper> craftableStacks = Lists.newArrayList();
//    List<AbstractTileContainer> invs = Lists.newArrayList();
//    for (BlockPos p : connectables) {
//      if (!(world.getTileEntity(p) instanceof AbstractTileContainer))
//        continue;
////      AbstractTileContainer tile = (AbstractTileContainer) world.getTileEntity(p);
////      invs.add(tile);
//    }
//    for (AbstractTileContainer t : invs) {
//      for (int i = 0; i < t.getSizeInventory(); i++) {
//        if (t.getStackInSlot(i) != null && !t.getStackInSlot(i).isEmpty()) {
//          NBTTagCompound res = (NBTTagCompound) t.getStackInSlot(i).getTagCompound().getTag("res");
//          if (!Util.contains(stacks, new StackWrapper(new ItemStack(res), 0), new Comparator<StackWrapper>() {
//            @Override
//            public int compare(StackWrapper o1, StackWrapper o2) {
//              if (ItemHandlerHelper.canItemStacksStack(o1.getStack(), o2.getStack())) { return 0; }
//              return 1;
//            }
//          })) {
//            addToList(craftableStacks, new ItemStack(res), 0);
//          }
//        }
//      }
//    }
    return craftableStacks;
  }
  private void addToList(List<StackWrapper> lis, ItemStack s, int num) {
    boolean added = false;
    for (int i = 0; i < lis.size(); i++) {
      ItemStack stack = lis.get(i).getStack();
      if (ItemHandlerHelper.canItemStacksStack(s, stack)) {
        lis.get(i).setSize(lis.get(i).getSize() + num);
        added = true;
      }
    }
    if (!added) {
      lis.add(new StackWrapper(s, num));
    }
  }
  public int getAmount(FilterItem fil) {
    if (fil == null) { return 0; }
    int size = 0;
    ItemStack s = fil.getStack();
    for (StackWrapper w : getStacks()) {
      if (fil.match(w.getStack()))
        size += w.getSize();
    }
    return size;
  }
//  public List<AbstractTileContainer> getContainers() {
//    List<AbstractTileContainer> lis = Lists.newArrayList();
//    for (BlockPos p : connectables) {
//      if (!(world.getTileEntity(p) instanceof AbstractTileContainer)) {
//        continue;
//      }
//      lis.add((AbstractTileContainer) world.getTileEntity(p));
//    }
//    return lis;
//  }
  public List<FilterItem> getIngredients(ItemStack template) {
    Map<Integer, ItemStack> stacks = Maps.<Integer, ItemStack> newHashMap();
    Map<Integer, Boolean> metas = Maps.<Integer, Boolean> newHashMap();
    Map<Integer, Boolean> ores = Maps.<Integer, Boolean> newHashMap();
    NBTTagList invList = template.getTagCompound().getTagList("crunchItem", Constants.NBT.TAG_COMPOUND);
    for (int i = 0; i < invList.tagCount(); i++) {
      NBTTagCompound stackTag = invList.getCompoundTagAt(i);
      int slot = stackTag.getByte("Slot");
      stacks.put(slot, new ItemStack(stackTag));
    }
    List<FilterItem> list = Lists.newArrayList();
    for (int i = 1; i < 10; i++) {
      metas.put(i - 1, NBTHelper.getBoolean(template, "meta" + i));
      ores.put(i - 1, NBTHelper.getBoolean(template, "ore" + i));
    }
    for (Entry<Integer, ItemStack> e : stacks.entrySet()) {
      if (e.getValue() != null) {
        boolean meta = metas.get(e.getKey()), ore = ores.get(e.getKey());
        list.add(new FilterItem(e.getValue(), meta, ore, false));
      }
    }
    return list;
  }
  @Override
  public NBTTagCompound getUpdateTag() {
    return writeToNBT(new NBTTagCompound());
  }
  //  @Override
  //  public void readFromNBT(NBTTagCompound compound) {
  //    super.readFromNBT(compound);
  //    //    en.readFromNBT(compound);
  //    //    NBTTagList tasksList = compound.getTagList("tasks", Constants.NBT.TAG_COMPOUND);
  //    //    tasks = Lists.newArrayList();
  //    //    for (int i = 0; i < tasksList.tagCount(); i++) {
  //    //      NBTTagCompound stackTag = tasksList.getCompoundTagAt(i);
  //    //      tasks.add(CraftingTask.loadCraftingTaskFromNBT(stackTag));
  //    //    }
  //  }
  //  @Override
  //  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
  //    super.writeToNBT(compound);
  //    //    en.writeToNBT(compound);
  //    //    NBTTagList tasksList = new NBTTagList();
  //    //    for (CraftingTask t : tasks) {
  //    //      NBTTagCompound stackTag = new NBTTagCompound();
  //    //      t.writeToNBT(stackTag);
  //    //      tasksList.appendTag(stackTag);
  //    //    }
  //    //    compound.setTag("tasks", tasksList);
  //    return compound;
  //  }
  private void addConnectables(final BlockPos pos) {
    if (pos == null || world == null)
      return;
    for (BlockPos bl : Util.getSides(pos)) {
      Chunk chunk = world.getChunkFromBlockCoords(bl);
      if (chunk == null || !chunk.isLoaded()) {
        continue;
      }
      if (world.getTileEntity(bl) != null && world.getTileEntity(bl) instanceof TileMaster && !bl.equals(this.pos)) {
        world.getBlockState(bl).getBlock().dropBlockAsItem(world, bl, world.getBlockState(bl), 0);
        world.setBlockToAir(bl);
        world.removeTileEntity(bl);
        continue;
      }
      if (world.getTileEntity(bl) != null && world.getTileEntity(bl) instanceof IConnectable && !connectables.contains(bl)) {
        connectables.add(bl);
        ((IConnectable) world.getTileEntity(bl)).setMaster(this.pos);
        chunk.setChunkModified();
        addConnectables(bl);
      }
    }
  }
  private void addInventorys() {
    storageInventorys = Lists.newArrayList();
    for (BlockPos cable : connectables) {
      if (world.getTileEntity(cable) instanceof AbstractFilterTile) {
        AbstractFilterTile s = (AbstractFilterTile) world.getTileEntity(cable);
        if (s.getInventory() != null && s.isStorage()) {
          BlockPos pos = s.getSource();
          if (world.getChunkFromBlockCoords(pos).isLoaded())
            storageInventorys.add(pos);
        }
      }
    }
  }
  public void refreshNetwork() {
    if (world.isRemote) { return; }
    connectables = Sets.newHashSet();
    try {
      addConnectables(pos);
    }
    catch (Error e) {
      e.printStackTrace();
    }
    addInventorys();
    world.getChunkFromBlockCoords(pos).setChunkModified();
  }
  public int insertStack(ItemStack stack, BlockPos source, boolean simulate) {
    if (stack == null || stack.isEmpty()) { return 0; }
    List<AbstractFilterTile> invs = Lists.newArrayList();
    if (connectables == null) {
      refreshNetwork();
    }
    for (BlockPos p : connectables) {
      if (world.getTileEntity(p) instanceof AbstractFilterTile) {
        AbstractFilterTile tile = (AbstractFilterTile) world.getTileEntity(p);
        if (tile.isStorage() && tile.getInventory() != null) {
          invs.add(tile);
        }
      }
    }
    Collections.sort(invs, new Comparator<AbstractFilterTile>() {
      @Override
      public int compare(AbstractFilterTile o1, AbstractFilterTile o2) {
        return Integer.compare(o2.getPriority(), o1.getPriority());
      }
    });
    ItemStack in = stack.copy();
    for (AbstractFilterTile t : invs) {
      IItemHandler inv = t.getInventory();
      if (!InvHelper.contains(inv, in))
        continue;
      if (!t.canTransfer(in, Direction.IN))
        continue;
      if (t.getSource().equals(source))
        continue;
      ItemStack remain = ItemHandlerHelper.insertItemStacked(inv, in, simulate);
      if (remain == null || remain.isEmpty())
        return 0;
      in = ItemHandlerHelper.copyStackWithSize(in, remain.getCount());
      world.markChunkDirty(t.getSource(), world.getTileEntity(t.getSource()));
    }
    for (AbstractFilterTile t : invs) {
      IItemHandler inv = t.getInventory();
      if (InvHelper.contains(inv, in))
        continue;
      if (!t.canTransfer(in, Direction.IN))
        continue;
      if (t.getSource().equals(source))
        continue;
      ItemStack remain = ItemHandlerHelper.insertItem(inv, in, simulate);
      if (remain == null || remain.isEmpty())
        return 0;
      in = ItemHandlerHelper.copyStackWithSize(in, remain.getCount());
      world.markChunkDirty(t.getSource(), world.getTileEntity(t.getSource()));
    }
    return in.getCount();
  }
  public void updateImports() {
    List<TileCable> invs = Lists.newArrayList();
    for (BlockPos p : connectables) {
      if (!(world.getTileEntity(p) instanceof TileCable))
        continue;
      TileCable tile = (TileCable) world.getTileEntity(p);
      if (tile.getKind() == Kind.imKabel && tile.getInventory() != null) {
        invs.add(tile);
      }
    }
    Collections.sort(invs, new Comparator<TileCable>() {
      @Override
      public int compare(TileCable o1, TileCable o2) {
        return Integer.compare(o2.getPriority(), o1.getPriority());
      }
    });
    for (TileCable t : invs) {
      IItemHandler inv = t.getInventory();
      if ((world.getTotalWorldTime() + 10) % (30 / (t.getUpgradesOfType(ItemUpgrade.SPEED) + 1)) != 0)
        continue;
      for (int i = 0; i < inv.getSlots(); i++) {
        ItemStack s = inv.getStackInSlot(i);
        if (s == null || s.isEmpty()) {
          continue;
        }
        if (!t.canTransfer(s, Direction.OUT)) {
          continue;
        }
        if (!t.status()) {
          continue;
        }
        int num = s.getCount();
        int insert = Math.min(s.getCount(), (int) Math.pow(2, t.getUpgradesOfType(ItemUpgrade.STACK) + 2));
        ItemStack extracted = inv.extractItem(i, insert, true);
        if (extracted == null || extracted.getCount() < insert) {
          continue;
        }
        int rest = insertStack(ItemHandlerHelper.copyStackWithSize(s, insert), t.getConnectedInventory(), false);
        inv.extractItem(i, insert - rest, false);
        world.markChunkDirty(pos, this);
        break;
      }
    }
  }
  public void updateExports() {
    List<TileCable> invs = Lists.newArrayList();
    for (BlockPos p : connectables) {
      if (!(world.getTileEntity(p) instanceof TileCable)) {
        continue;
      }
      TileCable tile = (TileCable) world.getTileEntity(p);
      if (tile.getKind() == Kind.exKabel && tile.getInventory() != null) {
        invs.add(tile);
      }
    }
    Collections.sort(invs, new Comparator<TileCable>() {
      @Override
      public int compare(TileCable o1, TileCable o2) {
        return Integer.compare(o1.getPriority(), o2.getPriority());
      }
    });
    for (TileCable t : invs) {
      IItemHandler inv = t.getInventory();
      if ((world.getTotalWorldTime() + 20) % (30 / (t.getUpgradesOfType(ItemUpgrade.SPEED) + 1)) != 0) {
        continue;
      }
      for (int i = 0; i < 18; i++) {
        if (t.getFilter().get(i) == null) {
          continue;
        }
        boolean ore = t.getOre(i);
        boolean meta = t.getMeta(i);
        ItemStack fil = t.getFilter().get(i).getStack();
        if (fil == null || fil.isEmpty()) {
          continue;
        }
        if (storageInventorys.contains(t.getPos())) {
          continue;
        }
        ItemStack g = request(new FilterItem(fil, meta, ore, false), 1, true);
        if (g == null || g.isEmpty()) {
          continue;
        }
        int m = g.getMaxStackSize();
        if ((t.getUpgradesOfType(ItemUpgrade.STOCK) > 0)) {
          
          m = Math.min(m, t.getFilter().get(i).getSize() - InvHelper.getAmount(inv, new FilterItem(g, meta, ore, false)));
        }
        if (m <= 0) {
          continue;
        }
        ItemStack max = ItemHandlerHelper.copyStackWithSize(g, m);
        ItemStack remain = ItemHandlerHelper.insertItemStacked(inv, max, true);
        int insert = remain == null ? max.getCount() : max.getCount() - remain.getCount();
        insert = Math.min(insert, (int) Math.pow(2, t.getUpgradesOfType(ItemUpgrade.STACK) + 2));
        if (!t.status()) {
          continue;
        }
        ItemStack rec = request(new FilterItem(g, meta, ore, false), insert, false);
        if (rec == null || rec.isEmpty()) {
          continue;
        }
        rec.shrink(t.getUpgradesOfType(ItemUpgrade.SPEED));
        ItemHandlerHelper.insertItemStacked(inv, rec, false);
        world.markChunkDirty(pos, this);
        break;
      }
    }
  }
  public ItemStack request(FilterItem fil, final int size, boolean simulate) {
    if (size == 0 || fil == null) { return ItemStack.EMPTY; }
    List<AbstractFilterTile> invs = Lists.newArrayList();
    for (BlockPos p : connectables) {
      if (world.getTileEntity(p) instanceof AbstractFilterTile) {
        AbstractFilterTile tile = (AbstractFilterTile) world.getTileEntity(p);
        if (tile.isStorage() && tile.getInventory() != null) {
          invs.add(tile);
        }
      }
    }
    ItemStack res = ItemStack.EMPTY;
    int result = 0;
    for (AbstractFilterTile t : invs) {
      IItemHandler inv = t.getInventory();
      for (int i = 0; i < inv.getSlots(); i++) {
        ItemStack s = inv.getStackInSlot(i);
        if (s == null || s.isEmpty()) {
          continue;
        }
        if (res != null && !res.isEmpty() && !ItemHandlerHelper.canItemStacksStack(s, res)) {
          continue;
        }
        if (!fil.match(s)) {
          continue;
        }
        if (!t.canTransfer(s, Direction.OUT)) {
          continue;
        }
        // System.out.println("!TileMaster IS NOT EMPTY" + s + "_" + s.getCount());
        int miss = size - result;
        ItemStack extracted = inv.extractItem(i, Math.min(inv.getStackInSlot(i).getCount(), miss), simulate);
        world.markChunkDirty(pos, this);
        result += Math.min((extracted == null || extracted.isEmpty()) ? 0 : extracted.getCount(), miss);
        res = extracted.copy();
        //  int rest = s.getCount();
        if (result == size) { return ItemHandlerHelper.copyStackWithSize(res, size); }
      }
    }
    if (result == 0) { return ItemStack.EMPTY; }
    return ItemHandlerHelper.copyStackWithSize(res, result);
  }
  @Override
  public void update() {
    if (world == null || world.isRemote) { return; }
    //refresh time in config, default 200 ticks
    if (storageInventorys == null || connectables == null
        || (world.getTotalWorldTime() % (ConfigHandler.refreshTicks) == 0)) {
      refreshNetwork();
    }
    updateImports();
    updateExports();
  }
  @Override
  public SPacketUpdateTileEntity getUpdatePacket() {
    NBTTagCompound syncData = new NBTTagCompound();
    this.writeToNBT(syncData);
    return new SPacketUpdateTileEntity(this.pos, 1, syncData);
  }
  @Override
  public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    readFromNBT(pkt.getNbtCompound());
  }
  @Override
  public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
    return oldState.getBlock() != newSate.getBlock();
  }
}
