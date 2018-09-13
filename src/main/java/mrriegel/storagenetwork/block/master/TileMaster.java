package mrriegel.storagenetwork.block.master;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.AbstractFilterTile;
import mrriegel.storagenetwork.block.IConnectable;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.block.master.RecentPointer.StackSlot;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.util.UtilInventory;
import mrriegel.storagenetwork.util.UtilTileEntity;
import mrriegel.storagenetwork.util.data.EnumFilterDirection;
import mrriegel.storagenetwork.util.data.FilterItem;
import mrriegel.storagenetwork.util.data.StackWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileMaster extends TileEntity implements ITickable {

  private Set<BlockPos> connectables;
  private List<BlockPos> storageInventorys;
  private Map<String, RecentPointer> recentImports = new HashMap<>();

  public List<StackWrapper> getStacks() {
    List<StackWrapper> stacks = Lists.newArrayList();
    if (getConnectables() == null) {
      refreshNetwork();
    }
    List<AbstractFilterTile> invs = getConnectedFilterTiles();
    for (AbstractFilterTile tileConnected : invs) {
      IItemHandler inv = tileConnected.getInventory();
      ItemStack stack;
      for (int i = 0; i < inv.getSlots(); i++) {
        stack = inv.getStackInSlot(i);
        if (!stack.isEmpty() && tileConnected.canTransfer(stack, EnumFilterDirection.BOTH))
          addToList(stacks, stack.copy(), stack.getCount());
        //        else
        //                  StorageNetwork.log(" reject   " + inv.getStackInSlot(i).getDisplayName());
      }
    }
    return stacks;
  }

  private AbstractFilterTile getAbstractFilterTileOrNull(BlockPos pos) {
    TileEntity tileHere = world.getTileEntity(pos);
    if (tileHere instanceof AbstractFilterTile) {
      AbstractFilterTile tile = (AbstractFilterTile) tileHere;
      if (tile.isStorage() && tile.getInventory() != null) {
        return tile;
      }
    }
    return null;
  }

  private List<AbstractFilterTile> getConnectedFilterTiles() {
    if (getConnectables() == null) {
      refreshNetwork();
    }
    List<AbstractFilterTile> invs = Lists.newArrayList();
    for (BlockPos p : getConnectables()) {
      AbstractFilterTile tile = getAbstractFilterTileOrNull(p);
      if (tile != null) {
        invs.add(tile);
      }
    }
    return invs;
  }

  private List<TileCable> getAttachedCables(List<TileEntity> links, Block kind) {
    List<TileCable> attachedCables = Lists.newArrayList();
    for (TileEntity tileIn : links) {
      if (tileIn instanceof TileCable) {
        TileCable tile = (TileCable) tileIn;
        if (tile.getBlockType() == kind && tile.getInventory() != null) {
          attachedCables.add(tile);
        }
      }
    }
    sortCablesByPriority(attachedCables);
    return attachedCables;
  }

  public int emptySlots() {
    int countEmpty = 0;
    List<AbstractFilterTile> invs = getConnectedFilterTiles();
    for (AbstractFilterTile tile : invs) {
      IItemHandler inv = tile.getInventory();
      for (int i = 0; i < inv.getSlots(); i++) {
        if (inv.getStackInSlot(i).isEmpty()) {
          countEmpty++;
        }
      }
    }
    return countEmpty;
  }

  private void addToList(List<StackWrapper> lis, ItemStack s, int num) {
    boolean added = false;
    for (int i = 0; i < lis.size(); i++) {
      ItemStack stack = lis.get(i).getStack();
      if (ItemHandlerHelper.canItemStacksStack(s, stack)) {
        lis.get(i).setSize(lis.get(i).getSize() + num);
        added = true;
      }
      else {
        //        lis.add(new StackWrapper(stack,stack.getCount()));
      }
    }
    if (!added) {
      lis.add(new StackWrapper(s, num));
    }
  }

  public int getAmount(FilterItem fil) {
    if (fil == null) {
      return 0;
    }
    int size = 0;
    //ItemStack s = fil.getStack();
    for (StackWrapper w : getStacks()) {
      if (fil.match(w.getStack()))
        size += w.getSize();
    }
    return size;
  }

  @Override
  public NBTTagCompound getUpdateTag() {
    return writeToNBT(new NBTTagCompound());
  }

  private void addConnectables(final BlockPos pos) {
    if (pos == null || world == null || this.getWorld().isBlockLoaded(pos) == false) {
      return;
    }
    for (BlockPos bl : UtilTileEntity.getSides(pos)) {
      if (this.getWorld().isBlockLoaded(bl) == false) {
        continue;
      }
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
      if (world.getTileEntity(bl) != null && world.getTileEntity(bl) instanceof IConnectable && !getConnectables().contains(bl)) {
        getConnectables().add(bl);
        ((IConnectable) world.getTileEntity(bl)).setMaster(this.pos);
        chunk.setModified(true);
        addConnectables(bl);
      }
    }
  }

  private void addInventorys() {
    setStorageInventorys(Lists.newArrayList());
    for (BlockPos cable : getConnectables()) {
      if (world.getTileEntity(cable) instanceof AbstractFilterTile) {
        AbstractFilterTile s = (AbstractFilterTile) world.getTileEntity(cable);
        if (s.getInventory() != null && s.isStorage()) {
          BlockPos pos = s.getSource();
          if (world.getChunkFromBlockCoords(pos).isLoaded())
            getStorageInventorys().add(pos);
        }
      }
    }
  }

  public void refreshNetwork() {
    if (world.isRemote) {
      return;
    }
    setConnectables(Sets.newHashSet());
    try {
      addConnectables(pos);
    }
    catch (Throwable e) {
      StorageNetwork.instance.logger.error("Refresh network error ", e);
    }
    addInventorys();
    world.getChunkFromBlockCoords(pos).setModified(true);//.setChunkModified();
  }

  private ItemStack insertStackSingleTarget(AbstractFilterTile tileCable, ItemStack stackInCopy, boolean simulate, int slot) {
    IItemHandler inventoryLinked = tileCable.getInventory();
    if (!UtilInventory.contains(inventoryLinked, stackInCopy))
      return stackInCopy;
    if (!tileCable.canTransfer(stackInCopy, EnumFilterDirection.IN))
      return stackInCopy;
    //    if (tileCable.getSource().equals(source))
    //      continue;
    //    if(slot < 0)
    //    else
    String key = getStackKey(stackInCopy);
    int originalSize = stackInCopy.getCount();
    //      
    RecentPointer.StackSlot response = insertItemStacked(inventoryLinked, stackInCopy, simulate, slot);
    ItemStack remain = response.stack;
    stackInCopy = ItemHandlerHelper.copyStackWithSize(stackInCopy, remain.getCount());
    if (stackInCopy.getCount() < originalSize) {
      //String key = getStackKey(stackInCop y);
      RecentPointer ptr = new RecentPointer();
      if (response.slot >= 0)
        ptr.setSlot(response.slot);
      ptr.setPos(tileCable.getPos());
      if (recentImports.containsKey(key) == false) {
        StorageNetwork.log(key + " insert mapped " + response.slot);
      } //but i guess still overwrite?
      recentImports.put(key, ptr);
    }
    return remain;
  }

  /**
   * net.minecraftforge.items.ItemHandlerHelper;
   * 
   * CHANGED TO use an existing/cached slot
   * 
   * Inserts the ItemStack into the inventory, filling up already present stacks first.
   * 
   * This is equivalent to the behaviour of a player picking up an item.
   * 
   * Note: This function stacks items without subtypes with different metadata together.
   */
  private StackSlot insertItemStacked(IItemHandler inventory, @Nonnull ItemStack stack, boolean simulate, int existingSlot) {
    RecentPointer.StackSlot response = new RecentPointer.StackSlot();
    //    return ItemHandlerHelper.insertItemStacked(inventory, stack, simulate);
    if (inventory == null || stack.isEmpty())
      return response;
    // not stackable -> just insert into a new slot
    if (!stack.isStackable()) {
      response.stack = ItemHandlerHelper.insertItem(inventory, stack, simulate);
      return response;
    }
    int sizeInventory = inventory.getSlots();
    if (existingSlot >= 0) {
      StorageNetwork.log("Found slot " + existingSlot);
      stack = inventory.insertItem(existingSlot, stack, simulate);
    }
    else {
      // go through the inventory and try to fill up already existing items
      for (int i = 0; i < sizeInventory; i++) {
        ItemStack slot = inventory.getStackInSlot(i);
        if (ItemHandlerHelper.canItemStacksStackRelaxed(slot, stack)) {
          stack = inventory.insertItem(i, stack, simulate);
          if (stack.isEmpty()) {
            response.slot = i;
            break;
          }
        }
      }
    }
    // insert remainder into empty slots
    if (!stack.isEmpty()) {
      // find empty slot
      for (int i = 0; i < sizeInventory; i++) {
        if (inventory.getStackInSlot(i).isEmpty()) {
          stack = inventory.insertItem(i, stack, simulate);
          if (stack.isEmpty()) {
            response.slot = i;
            break;
          }
        }
      }
    }
    response.stack = stack;
    return response;
  }

  /**
   * Insert item stack from anywhere (imports, GUI player interaction, recipe messages) into the system. Searches everything connected to the system in order to find where to put it. Returns the
   * number of things moved out of the stack
   * 
   * @return count moved
   */
  public int insertStack(ItemStack stack, BlockPos source, boolean simulate) {
    if (stack.isEmpty()) {
      return 0;
    }
    //    int originalSize = stack.getCount();
    //refactor this garbage why are there too loops LOL 
    List<AbstractFilterTile> invs = getConnectedFilterTiles();
    ItemStack stackInCopy = stack.copy();
    //only if it does NOT contains
    String key = getStackKey(stackInCopy);
    if (this.recentImports.containsKey(key)) {
      StorageNetwork.log("Found key" + key);
      RecentPointer pointer = this.recentImports.get(key);
      AbstractFilterTile aTile = getAbstractFilterTileOrNull(pointer.getPos());
      if (aTile == null) {
        StorageNetwork.log("delete key" + key);
        this.recentImports.remove(key);
      }
      else {
        StorageNetwork.log("USE  key" + key);
        stackInCopy = insertStackSingleTarget(aTile, stackInCopy, simulate, pointer.getSlot());
      }
      //      rest = insertStack(ItemHandlerHelper.copyStackWithSize(stackCurrent, insert), tileCable.getConnectedInventory(), false);
    }
    if (stackInCopy.isEmpty() == false) {
      //cache pointer failed, use normal way
      for (AbstractFilterTile tileCable : invs) {
        //      IItemHandler inventoryLinked = tileCable.getInventory();
        //      if (!UtilInventory.contains(inventoryLinked, stackInCopy))
        //        continue;
        //      if (!tileCable.canTransfer(stackInCopy, EnumFilterDirection.IN))
        //        continue;
        if (tileCable.getSource().equals(source))
          continue;
        stackInCopy = insertStackSingleTarget(tileCable, stackInCopy, simulate, -1);
        //      //      if (remain.isEmpty()) {
        //      //        return 0;
        //      //      }
        //success 
        //      stackInCopy = ItemHandlerHelper.copyStackWithSize(stackInCopy, remain.getCount());
        world.markChunkDirty(tileCable.getSource(), world.getTileEntity(tileCable.getSource()));
      }
      //no existing item match found, look for empty slot 
      for (AbstractFilterTile tileCabl : invs) {
        IItemHandler inventoryLinked = tileCabl.getInventory();
        if (UtilInventory.contains(inventoryLinked, stackInCopy))
          continue;
        if (!tileCabl.canTransfer(stackInCopy, EnumFilterDirection.IN))
          continue;
        if (tileCabl.getSource().equals(source))
          continue;
        ItemStack remain = ItemHandlerHelper.insertItem(inventoryLinked, stackInCopy, simulate);
        //      if (remain.isEmpty()) {
        //        return 0;
        //      }
        stackInCopy = ItemHandlerHelper.copyStackWithSize(stackInCopy, remain.getCount());
        world.markChunkDirty(tileCabl.getSource(), world.getTileEntity(tileCabl.getSource()));
      }
    }
    return stackInCopy.getCount();
  }

  private String getStackKey(ItemStack stackInCopy) {
    return stackInCopy.getItem().getRegistryName().toString() + "/" + stackInCopy.getItemDamage();
  }

  /**
   * Pull into the network from the relevant linked cables
   * 
   * @param attachedCables
   */
  public void updateImports(List<TileCable> attachedCables) {

    StorageNetwork.log("[KEYSIZE] " + this.recentImports.keySet().size());

    //    for (RecentPointer pointer : this.recentImports) {
    //      // TODO: use this first
    //    }
    for (TileCable tileCable : attachedCables) {
      IItemHandler inventoryLinked = tileCable.getInventory();
      int speedRatio = tileCable.getUpgradesOfType(ItemUpgrade.SPEED) + 1;
      //      StorageNetwork.log("speedratio " + speedRatio+" and the divisor is "+(30 / speedRatio)
      //          + " ===GO=== " + (world.getTotalWorldTime()  % (30 / speedRatio) == 0) );
      if (world.getTotalWorldTime() % (30 / speedRatio) != 0) {
        continue;
      }
      boolean hasStackUpgrade = tileCable.getUpgradesOfType(ItemUpgrade.STACK) > 0;
      for (int slot = 0; slot < inventoryLinked.getSlots(); slot++) {
        //import FROM linked in this slot INTO the system
        ItemStack stackCurrent = inventoryLinked.getStackInSlot(slot);
        if (stackCurrent.isEmpty()) {
          continue;
        }
        if (!tileCable.canTransfer(stackCurrent, EnumFilterDirection.OUT)) {
          continue;
        }
        if (!tileCable.doesPassOperationFilterLimit()) {
          continue; // nope, cant pass by. operation filter in place and all set
        }
        int maxInsert = (hasStackUpgrade) ? 64 : 4;
        int insert = Math.min(stackCurrent.getCount(), maxInsert);
        ItemStack extracted = inventoryLinked.extractItem(slot, insert, true);
        if (extracted.isEmpty() || extracted.getCount() < insert) {
          continue;
        }
        int rest = insertStack(ItemHandlerHelper.copyStackWithSize(stackCurrent, insert), tileCable.getConnectedInventory(), false);
        int countMoved = insert - rest;
        if (countMoved > 0) {
          inventoryLinked.extractItem(slot, countMoved, false);
          world.markChunkDirty(pos, this);
        }
        break;
      }
    }
  }

  public void updateExports(List<TileCable> attachedCables) {
    for (TileCable tileCable : attachedCables) {
      if (tileCable == null || tileCable.getInventory() == null) {
        continue;
      }
      if ((world.getTotalWorldTime() + 20) % (30 / (tileCable.getUpgradesOfType(ItemUpgrade.SPEED) + 1)) != 0) {
        continue;
      }
      IItemHandler inv = tileCable.getInventory();
      boolean ore = tileCable.getOre();
      boolean meta = tileCable.getMeta();
      //now check the filter inside this dudlio
      Map<Integer, StackWrapper> tilesFilter = tileCable.getFilter();
      for (int i = 0; i < AbstractFilterTile.FILTER_SIZE; i++) {
        if (getStorageInventorys().contains(tileCable.getPos())) {//constantly check if it gets removed
          continue;
        }
        StackWrapper currentFilter = tilesFilter.get(i);
        if (currentFilter == null) {
          continue;
        }
        ItemStack stackToFilter = currentFilter.getStack().copy();
        if (stackToFilter == null || stackToFilter.isEmpty()) {
          continue;
        }
        ItemStack stackCurrent = this.request(new FilterItem(stackToFilter, meta, ore, false), 1, true);
        //^ 1
        if (stackCurrent == null || stackCurrent.isEmpty()) {
          continue;
        }
        int maxStackSize = stackCurrent.getMaxStackSize();
        if ((tileCable.getUpgradesOfType(ItemUpgrade.STOCK) > 0)) {
          maxStackSize = Math.min(maxStackSize, currentFilter.getSize() - UtilInventory.getAmount(inv, new FilterItem(stackCurrent, meta, ore, false)));
        }
        if (maxStackSize <= 0) {
          continue;
        }
        ItemStack max = ItemHandlerHelper.copyStackWithSize(stackCurrent, maxStackSize);
        ItemStack remain = ItemHandlerHelper.insertItemStacked(inv, max, true);
        int insert = remain == null ? max.getCount() : max.getCount() - remain.getCount();
        boolean hasStackUpgrade = tileCable.getUpgradesOfType(ItemUpgrade.STACK) > 0;
        insert = Math.min(insert, hasStackUpgrade ? 64 : 4);
        if (!tileCable.doesPassOperationFilterLimit()) {
          continue;
        }
        ItemStack rec = this.request(new FilterItem(stackCurrent, meta, ore, false), insert, false);
        if (rec == null || rec.isEmpty()) {
          continue;
        }
        //now insert the stack we just pulled out 
        ItemHandlerHelper.insertItemStacked(inv, rec, false);
        world.markChunkDirty(pos, this);// is this needed?
        break;
      }
    }
  }

  public ItemStack request(FilterItem fil, final int size, boolean simulate) {
    if (size == 0 || fil == null) {
      return ItemStack.EMPTY;
    }
    //   StorageNetwork.benchmark( "first rebuild connectables");
    List<AbstractFilterTile> invs = Lists.newArrayList();
    for (BlockPos p : getConnectables()) {
      if (world.getTileEntity(p) instanceof AbstractFilterTile) {
        AbstractFilterTile tile = (AbstractFilterTile) world.getTileEntity(p);
        if (tile.isStorage() && tile.getInventory() != null) {
          invs.add(tile);
        }
      }
    }
    //  StorageNetwork.benchmark( "after r connectables");
    ItemStack res = ItemStack.EMPTY;
    int result = 0;
    for (AbstractFilterTile t : invs) {
      IItemHandler inv = t.getInventory();
      for (int i = 0; i < inv.getSlots(); i++) {
        ItemStack stackCurrent = inv.getStackInSlot(i);
        if (stackCurrent == null || stackCurrent.isEmpty()) {
          continue;
        }
        if (res != null && !res.isEmpty() && !ItemHandlerHelper.canItemStacksStack(stackCurrent, res)) {
          continue;
        }
        if (!fil.match(stackCurrent)) {
          continue;
        }
        if (!t.canTransfer(stackCurrent, EnumFilterDirection.OUT)) {
          continue;
        }
        int miss = size - result;
        int extractedCount = Math.min(inv.getStackInSlot(i).getCount(), miss);
        //   StorageNetwork.log("inv.extractItem  slot=" + i + ", size=" + extractedCount + ", simulated=" + simulate);
        ItemStack extracted = inv.extractItem(i, extractedCount, simulate);
        // StorageNetwork.log("[TileMaster] inv.extractItem RESULT I WAS GIVEN IS   " + extracted);
        //   StorageNetwork.log("DISABLE markChunkDirty at  extracted " + extracted + "?" + extracted.isEmpty() + extracted.getDisplayName());//for non SDRAWERS this is still the real thing
        //world.markChunkDirty(pos, this);
        //the other KEY fix for https://github.com/PrinceOfAmber/Storage-Network/issues/19, where it 
        //voided stuff when you took all from storage drawer: extracted can have a >0 stacksize, but still be air,
        //so the getCount overrides the 16, and gives zero instead, so i di my own override of, if empty then it got all so use source
        result += Math.min(extracted.isEmpty() ? stackCurrent.getCount() : extracted.getCount(), miss);
        res = stackCurrent.copy();
        if (res.isEmpty()) { //workaround for storage drawer and chest thing
          res = extracted.copy();
          res.setCount(result);
        }
        //        StorageNetwork.log(t.getPos() + "?" + size + "!TileMaster:request: yes actually remove items from source now " + res + "__" + result);
        //  int rest = s.getCount();
        if (result == size) {
          return ItemHandlerHelper.copyStackWithSize(res, size);
        }
      }
    }
    if (result == 0) {
      return ItemStack.EMPTY;
    }
    return ItemHandlerHelper.copyStackWithSize(res, result);
  }

  @Override
  public void update() {
    if (world == null || world.isRemote) {
      return;
    }
    //refresh time in config, default 200 ticks aka 10 seconds
    try {
      if (getStorageInventorys() == null || getConnectables() == null
          || (world.getTotalWorldTime() % (ConfigHandler.refreshTicks) == 0)) {
        refreshNetwork();
      }
      List<TileEntity> links = getAttachedTileEntities();
      List<TileCable> importCables = getAttachedCables(links, ModBlocks.imKabel);
      updateImports(importCables);
      List<TileCable> exportCables = getAttachedCables(links, ModBlocks.exKabel);
      updateExports(exportCables);
    }
    catch (Throwable e) {
      StorageNetwork.instance.logger.error("Refresh network error ", e);
    }
  }

  private void sortCablesByPriority(List<TileCable> attachedCables) {
    Collections.sort(attachedCables, new Comparator<TileCable>() {

      @Override
      public int compare(TileCable o1, TileCable o2) {
        return Integer.compare(o1.getPriority(), o2.getPriority());
      }
    });
  }

  private List<TileEntity> getAttachedTileEntities() {
    List<TileEntity> attachedCables = Lists.newArrayList();
    TileEntity tile = null;
    for (BlockPos p : getConnectables()) {
      tile = world.getTileEntity(p);
      attachedCables.add(tile);
    }
    return attachedCables;
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

  public Set<BlockPos> getConnectables() {
    return connectables;
  }

  public void setConnectables(Set<BlockPos> connectables) {
    this.connectables = connectables;
  }

  public List<BlockPos> getStorageInventorys() {
    return storageInventorys;
  }

  public void setStorageInventorys(List<BlockPos> storageInventorys) {
    this.storageInventorys = storageInventorys;
  }
}
