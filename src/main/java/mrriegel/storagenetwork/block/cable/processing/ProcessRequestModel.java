package mrriegel.storagenetwork.block.cable.processing;

import net.minecraft.nbt.NBTTagCompound;

//Not done but planned:
//{A}Text box input for priority (all)
//{B}CLONE settings between multiple cables
public class ProcessRequestModel {

  //EXPORTING:from network to inventory (to start crafting, add ingredients) . also default state
  //IMPORTING: from inventory to network (after crafting is done)
  public enum ProcessStatus {
    HALTED, IMPORTING, EXPORTING;
  }

  private static final String PREFIX = "sn_process_";
  //you can request more than 64
  private int count;
  private boolean alwaysActive = true;
  private ProcessStatus status = ProcessStatus.EXPORTING;

  public int getCount() {
    return count;
  }

  public void reduceCount() {
    if (count > 0) {
      count--;
    }
  }

  public void setCount(int countRequested) {
    if (count <= 0 && countRequested > 0) {
      //if we are going from zero to non zero, kickstart the thing
      this.status = ProcessStatus.EXPORTING;
    }
    this.count = countRequested;
  }

  public void readFromNBT(NBTTagCompound compound) {
    this.count = compound.getInteger(PREFIX + "count");
    this.status = ProcessStatus.values()[compound.getInteger(PREFIX + "status")];
    this.alwaysActive = compound.getBoolean(PREFIX + "always");
  }

  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    compound.setInteger(PREFIX + "count", count);
    compound.setInteger(PREFIX + "status", status.ordinal());
    compound.setBoolean(PREFIX + "always", alwaysActive);
    return compound;
  }

  public ProcessStatus getStatus() {
    return status;
  }

  public void setStatus(ProcessStatus status) {
    this.status = status;
  }

  public boolean isAlwaysActive() {
    return alwaysActive;
  }

  public void setAlwaysActive(boolean alwaysActive) {
    this.alwaysActive = alwaysActive;
  }
}
