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
import mrriegel.storagenetwork.api.ICableImport;
import mrriegel.storagenetwork.api.ICableStorage;
import mrriegel.storagenetwork.api.IHasNetworkPriority;
import mrriegel.storagenetwork.block.IConnectable;
import mrriegel.storagenetwork.block.cable.ProcessRequestModel;
import mrriegel.storagenetwork.block.cable.ProcessRequestModel.ProcessStatus;
import mrriegel.storagenetwork.block.cable.TileCable;
import mrriegel.storagenetwork.block.master.RecentSlotPointer.StackSlot;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.data.EnumFilterDirection;
import mrriegel.storagenetwork.data.FilterItem;
import mrriegel.storagenetwork.data.StackWrapper;
import mrriegel.storagenetwork.item.ItemUpgrade;
import mrriegel.storagenetwork.registry.ModBlocks;
import mrriegel.storagenetwork.util.UtilInventory;
import mrriegel.storagenetwork.util.UtilTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileMaster extends TileEntity implements ITickable {

  private Set<BlockPos> connectables;
  private List<BlockPos> storageInventorys;
  private Map<String, RecentSlotPointer> importCache = new HashMap<>();
  public static String[] blacklist;

  public List<StackWrapper> getStacks() {
    List<StackWrapper> stacks = Lists.newArrayList();
    if (getConnectables() == null) {
      refreshNetwork();
    }
    List<ICableStorage> invs = getSortedStorageCables(getAttachedTileEntities());
    for (ICableStorage tileConnected : invs) {
      IItemHandler inv = tileConnected.getInventory();
      ItemStack stack;
      for (int i = 0; i < inv.getSlots(); i++) {
        stack = inv.getStackInSlot(i);
        if (!stack.isEmpty() && tileConnected.canTransfer(stack, EnumFilterDirection.BOTH))
          addToList(stacks, stack.copy(), stack.getCount());
      }
    }
    return stacks;
  }

  private TileCable getAbstractFilterTileOrNull(BlockPos pos) {
    TileEntity tileHere = world.getTileEntity(pos);
    if (tileHere instanceof TileCable) {
      TileCable tile = (TileCable) tileHere;
      if (tile.isStorage() && tile.getInventory() != null) {
        return tile;
      }
    }
    return null;
  }

  private List<ICableStorage> getSortedStorageCables(List<TileEntity> links) {
    List<ICableStorage> attachedCables = Lists.newArrayList();
    for (TileEntity tileIn : links) {
      if (tileIn instanceof ICableStorage) {
        ICableStorage tile = (ICableStorage) tileIn;
        if (tile.getInventory() != null) {
          attachedCables.add(tile);
        }
      }
    }
    sortCablesByPriority(attachedCables);
    return attachedCables;
  }

  private List<ICableImport> getSortedICables(List<TileEntity> links) {
    List<ICableImport> attachedCables = Lists.newArrayList();
    for (TileEntity tileIn : links) {
      if (tileIn instanceof ICableStorage) {
        ICableImport tile = (ICableImport) tileIn;
        if (tile.getInventory() != null) {
          attachedCables.add(tile);
        }
      }
    }
    sortCablesByPriority(attachedCables);
    return attachedCables;
  }

  public List<TileCable> getAttachedCables(List<TileEntity> links, Block kind) {
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
    List<ICableStorage> invs = getSortedStorageCables(getAttachedTileEntities());
    for (ICableStorage tile : invs) {
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
    for (BlockPos blockPos : UtilTileEntity.getSides(pos)) {
      if (this.getWorld().isBlockLoaded(blockPos) == false) {
        continue;
      }
      Chunk chunk = world.getChunkFromBlockCoords(blockPos);
      if (chunk == null || !chunk.isLoaded()) {
        continue;
      }
      if (!isTargetAllowed(world, blockPos)) {
        continue;
      }
      TileEntity tileHere = world.getTileEntity(blockPos);
      if (tileHere instanceof TileMaster && !blockPos.equals(this.pos)) {
        world.getBlockState(blockPos).getBlock().dropBlockAsItem(world, blockPos, world.getBlockState(blockPos), 0);
        world.setBlockToAir(blockPos);
        world.removeTileEntity(blockPos);
        continue;
      }
      if (tileHere instanceof IConnectable && !getConnectables().contains(blockPos)) {
        getConnectables().add(blockPos);
        ((IConnectable) tileHere).setMaster(this.pos);
        chunk.setModified(true);
        addConnectables(blockPos);
      }
    }
  }

  public static boolean isTargetAllowed(IBlockAccess world, BlockPos bl) {
    String blockId = world.getBlockState(bl).getBlock().getRegistryName().toString();
    for (String s : blacklist) {
      if (s != null && s.equals(blockId)) {
        return false;
      }
    }
    return true;
  }

  private void addInventorys() {
    setStorageInventorys(Lists.newArrayList());
    for (BlockPos cable : getConnectables()) {
      if (world.getTileEntity(cable) instanceof TileCable) {
        TileCable s = (TileCable) world.getTileEntity(cable);
        if (s.getInventory() != null && s.isStorage()) {
          BlockPos pos = s.getConnectedInventory();
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

  private ItemStack insertStackSingleTarget(TileCable tileCable, ItemStack stackInCopy, boolean simulate, int slot) {
    IItemHandler inventoryLinked = tileCable.getInventory();
    if (!UtilInventory.contains(inventoryLinked, stackInCopy)) {
      return stackInCopy;
    }
    if (!tileCable.canTransfer(stackInCopy, EnumFilterDirection.IN)) {
      return stackInCopy;
    }
    //    if (tileCable.getSource().equals(source))
    //      continue;
    //    if(slot < 0)
    //    else
    String key = getStackKey(stackInCopy);
    int originalSize = stackInCopy.getCount();
    //      
    RecentSlotPointer.StackSlot response = insertItemStacked(inventoryLinked, stackInCopy, simulate, slot);
    ItemStack remain = response.stack;
    stackInCopy = ItemHandlerHelper.copyStackWithSize(stackInCopy, remain.getCount());
    if (stackInCopy.getCount() < originalSize) {
      RecentSlotPointer ptr = new RecentSlotPointer();
      ptr.setPos(tileCable.getPos());
      if (response.slot >= 0) {
        ptr.setSlot(response.slot);
      }
      if (importCache.containsKey(key) == false) {
        StorageNetwork.log("INSERT KEY " + key + " => " + ptr.getPos() + "@" + ptr.getSlot()
            + "  KEYSIZE = " + this.importCache.keySet().size());
      } //still overwrite
      importCache.put(key, ptr);
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
    RecentSlotPointer.StackSlot response = new RecentSlotPointer.StackSlot();
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
      stack = inventory.insertItem(existingSlot, stack, simulate);
    }
    if (!stack.isEmpty()) {
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
   * @return count of remaining leftover, not count moved
   */
  public int insertStack(ItemStack stack, BlockPos source, boolean simulate) {
    if (stack.isEmpty()) {
      return 0;
    }
    List<ICableStorage> invs = getSortedStorageCables(getAttachedTileEntities());
    ItemStack stackInCopy = stack.copy();
    //only if it does NOT contains
    String key = getStackKey(stackInCopy);
    if (this.importCache.containsKey(key)) {
      RecentSlotPointer pointer = this.importCache.get(key);
      TileCable aTile = getAbstractFilterTileOrNull(pointer.getPos());
      if (aTile == null) {
        StorageNetwork.log("DELETE key" + key + " KEYSIZE " + this.importCache.keySet().size());
        this.importCache.remove(key);
      }
      else {
        stackInCopy = insertStackSingleTarget(aTile, stackInCopy, simulate, pointer.getSlot());
      }
    }
    if (stackInCopy.isEmpty() == false) {
      for (ICableStorage tileCabl : invs) {
        if (tileCabl.getConnectedInventory().equals(source)) {
          continue;
        }
        IItemHandler inventoryLinked = tileCabl.getInventory();
        if (!tileCabl.canTransfer(stackInCopy, EnumFilterDirection.IN))
          continue;
        //try existing slot then try new
        ItemStack remain = ItemStack.EMPTY;
        try {
          remain = ItemHandlerHelper.insertItemStacked(inventoryLinked, stackInCopy, simulate);
        }
        catch (Exception e) {
          StorageNetwork.instance.logger.error("External connected block has thrown error", e);
          continue;
          //third party blocks can throw exceptions , for example:
          //          java.lang.IndexOutOfBoundsException: Index: 4, Size: 4
          //          at java.util.ArrayList.rangeCheck(Unknown Source) ~[?:1.8.0_151]
          //          at java.util.ArrayList.get(Unknown Source) ~[?:1.8.0_151]
          //          at com.tattyseal.compactstorage.tileentity.TileEntityChestBuilder.isItemValidForSlot(TileEntityChestBuilder.java:253) ~[TileEntityChestBuilder.class:?]
        }
        stackInCopy = ItemHandlerHelper.copyStackWithSize(stackInCopy, remain.getCount());
        world.markChunkDirty(tileCabl.getConnectedInventory(), world.getTileEntity(tileCabl.getConnectedInventory()));
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
  private void updateImports(List<ICableImport> attachedCables) {

    for (ICableImport tileCable : attachedCables) {
      IItemHandler inventoryLinked = tileCable.getInventory();
      if (!tileCable.runNow()) {
        continue;
      }
      for (int slot = 0; slot < inventoryLinked.getSlots(); slot++) {
        //import FROM linked in this slot INTO the system
        ItemStack stackCurrent = inventoryLinked.getStackInSlot(slot);
        if (stackCurrent.isEmpty()) {
          continue;
        }
        if (!tileCable.canTransfer(stackCurrent, EnumFilterDirection.OUT)) {
          continue;
        }

        int transferRate = tileCable.getTransferRate();
        int needToInsert = Math.min(stackCurrent.getCount(), transferRate);
        ItemStack extracted = inventoryLinked.extractItem(slot, needToInsert, true);
        if (extracted.isEmpty() || extracted.getCount() < needToInsert) {
          continue;
        }
        int countUnmoved = insertStack(ItemHandlerHelper.copyStackWithSize(stackCurrent, needToInsert), tileCable.getConnectedInventory(), false);
        int countMoved = needToInsert - countUnmoved;
        if (countMoved > 0) {
          inventoryLinked.extractItem(slot, countMoved, false);
          world.markChunkDirty(pos, this);
        }
        break;
      }
    }
  }

  private void updateProcess(List<TileCable> processCables) {
    //take the first X request (constant or configured, max # jobs per tick) 
    //it knows count, pos to use
    // run it (import , output, flip)
    // if remaining == 0 then delete the Request
    //user will create a request, store in memory list
    for (TileCable tileCable : processCables) {
      if (tileCable == null || tileCable.getInventory() == null || tileCable.getBlockType() != ModBlocks.processKabel) {
        continue;
      }
      ProcessRequestModel processRequest = tileCable.getRequest();
      if (processRequest == null) {
        continue;
      }
      //StorageNetwork.log(processRequest.isAlwaysActive() + " pc " + processRequest.getCount());
      if (processRequest.isAlwaysActive() == false && processRequest.getCount() <= 0) {
        continue; //no more left to do 
      }
      //now check item filter for input/output
      List<StackWrapper> ingredients = tileCable.getFilterTop();
      //well should this only be a single output? 
      List<StackWrapper> outputs = tileCable.getFilterBottom();
      //EXAMPLE REQUEST:
      //automate a furnace: 
      // ingredient is one cobblestone (network provides-exports this)
      // output is one smoothstone (network gets-imports this) 
      //
      IItemHandler inventoryLinked = tileCable.getInventory();
      //      StorageNetwork.log("ST " + request.getStatus() + "  ingredients " + ingredients.size());
      //we need to input ingredients FROM network into target
      //PROBLEM: two ingredients: dirt + gravel
      // network has tons dirt, no gravel. 
      //it will insert dirt, skip gravel, stay on exporting
      //and keep sending dirt forever
      // StorageNetwork.log("exporting SIZE = " + ingredients.size() + "/" + tileCable.getPos());
      if (processRequest.getStatus() == ProcessStatus.EXPORTING && ingredients.size() > 0) { //EXPORTING:from network to inventory . also default state
        //also TOP ROW  
        //NEW : this mode more stubborn. ex auto crafter.
        //if the target already has items, who cares, i was told to be in export mode so export a set if possible right away always.
        //then (assuming that works or even if not)
        //check if it has required
        //does the target have everything it needs, yes or no
        //look for full set, 
        //if we get all
        boolean simulate = true;
        int numSatisfiedIngredients = 0;
        for (StackWrapper ingred : ingredients) {
          //  how many are needed. request them
          //true is using nbt 
          inventoryLinked = UtilInventory.getItemHandler(world.getTileEntity(tileCable.getConnectedInventory()), tileCable.getFacingTopRow());
          ItemStack requestedFromNetwork = this.request(
              new FilterItem(ingred.getStack().copy(),
                  tileCable.getMeta(), tileCable.getOre(),
                  tileCable.getNbt()),
              ingred.getSize(), simulate);//false means 4real, no simulate
          int found = requestedFromNetwork.getCount();
          //   StorageNetwork.log("ingr size " + ingred.getSize() + " found +" + found + " of " + ingred.getStack().getDisplayName());
          ItemStack remain = ItemHandlerHelper.insertItemStacked(inventoryLinked, requestedFromNetwork, simulate);
          if (remain.isEmpty() && found >= ingred.getSize()) {
            numSatisfiedIngredients++;
            //then do it for real
            //            simulate = false;
            //            requestedFromNetwork = this.request(new FilterItem(ingred.getStack()), ingred.getSize(), simulate);//false means 4real, no simulate
            //            remain = ItemHandlerHelper.insertItemStacked(inventoryLinked, requestedFromNetwork, simulate);
            //done
            //now count whats needed, SHOULD be zero
          }
          //          int manyMoreNeeded = UtilInventory.containsAtLeastHowManyNeeded(inventoryLinked, ingred.getStack(), ingred.getSize());
          //          if (manyMoreNeeded == 0) {
          //            //ok it has ingredients here
          //          }
        } //end loop on ingredients
          //NOW do real inserts 
          //   StorageNetwork.log("satisfied # + " + numSatisfiedIngredients + " / " + ingredients.size());
        if (numSatisfiedIngredients == ingredients.size()) {
          //and if we can insert all
          //then complete transaction (get and put items)
          simulate = false;
          for (StackWrapper ingred : ingredients) {
            ItemStack requestedFromNetwork = this.request(new FilterItem(ingred.getStack()), ingred.getSize(), simulate);//false means 4real, no simulate
            ItemHandlerHelper.insertItemStacked(inventoryLinked, requestedFromNetwork, simulate);
          }
          //flip that waitingResult flag on request (and save)
          processRequest.setStatus(ProcessStatus.IMPORTING);
          tileCable.setRequest(processRequest);
        }
      }
      else if (processRequest.getStatus() == ProcessStatus.IMPORTING && outputs.size() > 0) { //from inventory to network
        //try to find/get from the blocks outputs into network
        // look for "output" items that can be   from target
        for (StackWrapper out : outputs) {
          //pull this many from targe  
          inventoryLinked = UtilInventory.getItemHandler(world.getTileEntity(tileCable.getConnectedInventory()), tileCable.getFacingBottomRow());
          boolean simulate = true;
          int targetStillNeeds = UtilInventory.containsAtLeastHowManyNeeded(inventoryLinked, out.getStack(), out.getSize());//.extractItem(inventoryLinked, new FilterItem(out.getStack().copy()), out.getSize(), simulate);
          ItemStack stackToMove = out.getStack().copy();
          //  StorageNetwork.log("IMPORTING: " + stackToMove.toString());
          stackToMove.setCount(out.getSize());
          int countNotInserted = this.insertStack(stackToMove, tileCable.getPos(), simulate);
          if (countNotInserted == 0 && targetStillNeeds == 0) { //extracted.getCount() == out.getSize() && countNotInserted == extracted.getCount()) {
            //success
            simulate = false;
            //            InventoryHelper.
            //new extract item using capabilityies
            //            StorageNetwork.log("importing acutally a success. send to face " + tileCable.getFacingBottomRow() + "?" + inventoryLinked + "?" + stackToMove.getDisplayName());
            //            StorageNetwork.log("-> IMPORTING: out =  " + out.toString());
            //            StorageNetwork.log("IMPORTING: stackToMove= " + stackToMove.toString());
            ItemStack extracted = UtilInventory.extractItem(inventoryLinked, new FilterItem(out.getStack()), out.getSize(), simulate);
            countNotInserted = this.insertStack(stackToMove, tileCable.getPos(), simulate);
            // IF all found 
            //then complete extraction (and insert into network)
            //then toggle that waitingResult flag on request (and save)
            processRequest.setStatus(ProcessStatus.EXPORTING);
            //  StorageNetwork.log("IMPORTING: TO STATUS EXPORTING  ");
            tileCable.setRequest(processRequest);
            //we got what we needed 
            if (processRequest.isAlwaysActive() == false) {
              processRequest.reduceCount();
            }
          }
        }
      }
      //      else {
      //        ModCyclic.logger.error("Status was halted or other " + request.getStatus());
      //        request.setStatus(ProcessStatus.IMPORTING);//?? i dont know
      //      }
      tileCable.setRequest(processRequest);
    }
  }

  private void updateExports(List<TileCable> attachedCables) {
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
      for (int i = 0; i < TileCable.FILTER_SIZE; i++) {
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
        ItemStack stackCurrent = this.request(new FilterItem(stackToFilter, meta, ore, tileCable.getNbt()), 1, true);
        //^ 1
        if (stackCurrent == null || stackCurrent.isEmpty()) {
          continue;
        }
        if (!tileCable.canTransfer(stackCurrent, EnumFilterDirection.IN)) {
          continue;
        }
        int maxStackSize = stackCurrent.getMaxStackSize();
        if ((tileCable.getUpgradesOfType(ItemUpgrade.STOCK) > 0)) {
          maxStackSize = Math.min(maxStackSize, currentFilter.getSize() - UtilInventory.getAmount(inv, new FilterItem(stackCurrent, meta, ore, tileCable.getNbt())));
        }
        if (maxStackSize <= 0) {
          continue;
        }
        ItemStack max = ItemHandlerHelper.copyStackWithSize(stackCurrent, maxStackSize);
        ItemStack remain = ItemHandlerHelper.insertItemStacked(inv, max, true);
        int insert = remain == null ? max.getCount() : max.getCount() - remain.getCount();
        boolean hasStackUpgrade = tileCable.getUpgradesOfType(ItemUpgrade.STACK) > 0;
        insert = Math.min(insert, hasStackUpgrade ? 64 : 4);

        ItemStack rec = this.request(new FilterItem(stackCurrent, meta, ore, tileCable.getNbt()), insert, false);
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
    List<TileCable> invs = Lists.newArrayList();
    for (BlockPos p : getConnectables()) {
      if (world.getTileEntity(p) instanceof TileCable) {
        TileCable tile = (TileCable) world.getTileEntity(p);
        if (tile.isStorage() && tile.getInventory() != null) {
          invs.add(tile);
        }
      }
    }
    //  StorageNetwork.benchmark( "after r connectables");
    ItemStack res = ItemStack.EMPTY;
    int result = 0;
    for (TileCable t : invs) {
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
      List<ICableImport> importCables = getSortedICables(links);
      updateImports(importCables);
      List<TileCable> exportCables = getAttachedCables(links, ModBlocks.exKabel);
      updateExports(exportCables);
      List<TileCable> processCables = getAttachedCables(links, ModBlocks.processKabel);
      updateProcess(processCables);
    }
    catch (Throwable e) {
      StorageNetwork.instance.logger.error("Refresh network error ", e);
    }
  }

  private void sortCablesByPriority(List<? extends IHasNetworkPriority> attachedCables) {
    Collections.sort(attachedCables, new Comparator<IHasNetworkPriority>() {

      @Override
      public int compare(IHasNetworkPriority o1, IHasNetworkPriority o2) {
        return Integer.compare(o1.getPriority(), o2.getPriority());
      }
    });
  }

  public List<TileEntity> getAttachedTileEntities() {
    List<TileEntity> attachedCables = Lists.newArrayList();
    TileEntity tile = null;
    for (BlockPos p : getConnectables()) {
      if (!isTargetAllowed(world, p)) {
        continue;
      }
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

  public void clearCache() {
    importCache = new HashMap<>();
  }
}
