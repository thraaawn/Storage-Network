package mrriegel.storagenetwork.block.cable.processing;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.block.cable.TileCableWithFacing;
import mrriegel.storagenetwork.block.master.TileMaster;
import mrriegel.storagenetwork.data.ItemStackMatcher;
import mrriegel.storagenetwork.util.UtilInventory;
import mrriegel.storagenetwork.util.inventory.ProcessingItemStackHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class TileCableProcess extends TileCableWithFacing {
  private ProcessRequestModel processModel = new ProcessRequestModel();
  public EnumFacing processingTop = EnumFacing.UP;
  public EnumFacing processingBottom = EnumFacing.DOWN;

  public ProcessingItemStackHandler filters = new ProcessingItemStackHandler();

  @Override
  public void readFromNBT(NBTTagCompound compound) {
    processingTop = EnumFacing.values()[compound.getInteger("processingTop")];
    processingBottom = EnumFacing.values()[compound.getInteger("processingBottom")];

    ProcessRequestModel pm = new ProcessRequestModel();
    pm.readFromNBT(compound);
    this.setProcessModel(pm);

    NBTTagCompound filters = compound.getCompoundTag("filters");
    this.filters.deserializeNBT(filters);

    super.readFromNBT(compound);
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    super.writeToNBT(compound);

    this.processModel.writeToNBT(compound);
    compound.setInteger("processingBottom", processingBottom.ordinal());
    compound.setInteger("processingTop", processingTop.ordinal());

    NBTTagCompound filters = this.filters.serializeNBT();
    compound.setTag("filters", filters);

    return compound;
  }

  public void run() {
    ProcessRequestModel processRequest = getRequest();
    if (processRequest == null) {
      return;
    }

    if (processRequest.isAlwaysActive() == false && processRequest.getCount() <= 0) {
      return; //no more left to do
    }

    if(getMaster() == null || getMaster().getTileEntity(TileMaster.class) == null) {
      return;
    }

    if(!hasDirection()) {
      return;
    }

    TileMaster master = getMaster().getTileEntity(TileMaster.class);

    //now check item filter for input/output
    List<ItemStack> ingredients = getProcessIngredients();
    //well should this only be a single output?
    List<ItemStack> outputs = getProcessOutputs();

    //EXAMPLE REQUEST:
    //automate a furnace:
    // ingredient is one cobblestone (network provides-exports this)
    // output is one smoothstone (network gets-imports this)
    //
    IItemHandler inventoryLinked = UtilInventory.getItemHandler(world.getTileEntity(this.getFacingPosition()), getDirection().getOpposite());
    //      StorageNetwork.log("ST " + request.getStatus() + "  ingredients " + ingredients.size());
    //we need to input ingredients FROM network into target
    //PROBLEM: two ingredients: dirt + gravel
    // network has tons dirt, no gravel.
    //it will insert dirt, skip gravel, stay on exporting
    //and keep sending dirt forever
    // StorageNetwork.log("exporting SIZE = " + ingredients.size() + "/" + tileCable.getPos());
    if (processRequest.getStatus() == ProcessRequestModel.ProcessStatus.EXPORTING && ingredients.size() > 0) { //EXPORTING:from network to inventory . also default state
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
      for (ItemStack ingred : ingredients) {
        //  how many are needed. request them
        //true is using nbt
        inventoryLinked = UtilInventory.getItemHandler(world.getTileEntity(this.getFacingPosition()), this.getFacingTopRow());
        ItemStack requestedFromNetwork = master.request(
                new ItemStackMatcher(ingred.copy(), this.filters.meta, this.filters.ores, this.filters.nbt),
                ingred.getCount(), simulate);//false means 4real, no simulate
        int found = requestedFromNetwork.getCount();
        //   StorageNetwork.log("ingr size " + ingred.getSize() + " found +" + found + " of " + ingred.getStack().getDisplayName());
        ItemStack remain = ItemHandlerHelper.insertItemStacked(inventoryLinked, requestedFromNetwork, simulate);
        if (remain.isEmpty() && found >= ingred.getCount()) {
          numSatisfiedIngredients++;
          //then do it for real
          //            simulate = false;
          //            requestedFromNetwork = this.request(new ItemStackMatcher(ingred.getStack()), ingred.getSize(), simulate);//false means 4real, no simulate
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
        for (ItemStack ingred : ingredients) {
          ItemStack requestedFromNetwork = master.request(new ItemStackMatcher(ingred), ingred.getCount(), simulate);//false means 4real, no simulate
          ItemHandlerHelper.insertItemStacked(inventoryLinked, requestedFromNetwork, simulate);
        }
        //flip that waitingResult flag on request (and save)
        processRequest.setStatus(ProcessRequestModel.ProcessStatus.IMPORTING);
        this.setRequest(processRequest);
      }
    }
    else if (processRequest.getStatus() == ProcessRequestModel.ProcessStatus.IMPORTING && outputs.size() > 0) { //from inventory to network
      //try to find/get from the blocks outputs into network
      // look for "output" items that can be   from target
      for (ItemStack out : outputs) {
        //pull this many from targe
        inventoryLinked = UtilInventory.getItemHandler(world.getTileEntity(this.getFacingPosition()), this.getFacingBottomRow());
        boolean simulate = true;
        int targetStillNeeds = UtilInventory.containsAtLeastHowManyNeeded(inventoryLinked, out, out.getCount());//.extractItem(inventoryLinked, new ItemStackMatcher(out.getStack().copy()), out.getSize(), simulate);
        ItemStack stackToMove = out.copy();
        //  StorageNetwork.log("IMPORTING: " + stackToMove.toString());
        stackToMove.setCount(out.getCount());
        int countNotInserted = master.insertStack(stackToMove, simulate);
        if (countNotInserted == 0 && targetStillNeeds == 0) { //extracted.getCount() == out.getSize() && countNotInserted == extracted.getCount()) {
          //success
          simulate = false;
          //            InventoryHelper.
          //new extract item using capabilityies
          //            StorageNetwork.log("importing acutally a success. send to face " + tileCable.getFacingBottomRow() + "?" + inventoryLinked + "?" + stackToMove.getDisplayName());
          //            StorageNetwork.log("-> IMPORTING: out =  " + out.toString());
          //            StorageNetwork.log("IMPORTING: stackToMove= " + stackToMove.toString());
          ItemStack extracted = UtilInventory.extractItem(inventoryLinked, new ItemStackMatcher(out), out.getCount(), simulate);
          countNotInserted = master.insertStack(stackToMove, simulate);
          // IF all found
          //then complete extraction (and insert into network)
          //then toggle that waitingResult flag on request (and save)
          processRequest.setStatus(ProcessRequestModel.ProcessStatus.EXPORTING);
          //  StorageNetwork.log("IMPORTING: TO STATUS EXPORTING  ");
          this.setRequest(processRequest);
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
    this.setRequest(processRequest);
    this.markDirty();
  }

  public List<ItemStack> getProcessIngredients() {
    return filters.getInputs().stream().map(stack -> stack.copy()).collect(Collectors.toList());
  }

  public List<ItemStack> getProcessOutputs() {
    return filters.getOutputs().stream().map(stack -> stack.copy()).collect(Collectors.toList());
  }

  @Nonnull
  public ItemStack getFirstRecipeOut() {
    if(filters.isOutputEmpty()) {
      return ItemStack.EMPTY;
    }

    return filters.getOutputs().get(0);
  }

  public EnumFacing getFacingBottomRow() {
    return this.processingBottom;
  }

  public EnumFacing getFacingTopRow() {
    return this.processingTop;
  }

  public ProcessRequestModel getProcessModel() {
    return processModel;
  }

  public void setProcessModel(ProcessRequestModel processModel) {
    this.processModel = processModel;
  }

  //TODO: also list of requests ordered . and nbt saved
  // where a process terminal lists some nodes and I "turn node on for 6 cycles" and it keeps track, maybe stuck after 2.
  public ProcessRequestModel getRequest() {
    return getProcessModel();
  }

  public void setRequest(ProcessRequestModel request) {
    this.setProcessModel(request);
  }


}
