package mrriegel.storagenetwork.block.cable;

import net.minecraft.nbt.NBTTagCompound;

public class ProcessRequestModel {

  public enum ProcessStatus {
    HALTED, IMPORTING, EXPORTING;
  }
  private static final String PREFIX = "sn_process";

  //you can request more than 64
  private int count;
  //import cable = from inventory to network
  //export cable = from network to inventory
  //so initial value is filling ingredients = export
  private ProcessStatus status = ProcessStatus.EXPORTING;

  public int getCount() {
    return count;
  }

  public void setCount(int countRequested) {
    this.count = countRequested;
  }


  public void readFromNBT(NBTTagCompound compound) {
    this.count = compound.getInteger(PREFIX + "count");
    this.status = ProcessStatus.values()[compound.getInteger(PREFIX + "status")];
  }

  public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    compound.setInteger(PREFIX + "count", count);
    compound.setInteger(PREFIX + "status", status.ordinal());
    return compound;
  }

  public ProcessStatus getStatus() {
    return status;
  }

  public void setStatus(ProcessStatus status) {
    this.status = status;
  }
}
