package mrriegel.storagenetwork.block.cable;

import net.minecraft.nbt.NBTTagCompound;

//KNOWN BUGS
//1: always inserting second ingredient if first one runs dry, fills too much
//2: have to kickstart, its stuck at start in (inv to network) 
//3: sided inventories aka furnaces need a solution
//4: cant turn off (multiple options for features to address)
//
//long versions
//3a: toggle button for "input = bottom; output = north"
//
//: its stuck in the "network to inventory" loop. but when it fails to insert 100% of recipe it
//doesnt roll back and leaves half the ingredients in there
//1a a bandaid could be "do you have <resin> in your insert-able slots? (ignore output-only slots and autocrafter 3x3)
//1b another one is to rollback transactions / so if it needs both resin and acorn, but acorn empty, dont insert resin every tick forever  
public class ProcessRequestModel {

  public enum ProcessStatus {
    HALTED, IMPORTING, EXPORTING;
  }
  private static final String PREFIX = "sn_process";

  //you can request more than 64
  private int count;
  private ProcessStatus status = ProcessStatus.IMPORTING;

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
