package mrriegel.storagenetwork.block.cable;

import net.minecraft.nbt.NBTTagCompound;

//Not done but planned:
//{A}Text box input for priority (all) 
//{B}CLONE settings between multiple cables 
//{C}[DONE]Import button that will attempt to read recipe from machine
//{D}Method to enable/disable process. Possibly with some sort of request system
//{E}Gui Redesign !!!
//{F}UPGRADES

public class ProcessRequestModel {

  public enum ProcessStatus {
    HALTED, IMPORTING, EXPORTING;
  }
  private static final String PREFIX = "sn_process";

  //you can request more than 64
  private int count;
  private boolean alwaysActive;
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
    this.count = countRequested;
  }


  public void readFromNBT(NBTTagCompound compound) {
    this.count = compound.getInteger(PREFIX + "count");
    this.status = ProcessStatus.values()[compound.getInteger(PREFIX + "status")];
    this.alwaysActive = compound.getBoolean("always");
  }

  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    compound.setInteger(PREFIX + "count", count);
    compound.setInteger(PREFIX + "status", status.ordinal());
    compound.setBoolean("always", alwaysActive);
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
