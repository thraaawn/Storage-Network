package mrriegel.storagenetwork.block.cable;

import net.minecraft.util.math.BlockPos;

public class ProcessRequestModel {

  private BlockPos pos;
  private int countRequested;
  private int countCompleted;
  private boolean waitingResult;

  public BlockPos getPos() {
    return pos;
  }

  public void setPos(BlockPos pos) {
    this.pos = pos;
  }

  public int getCountRequested() {
    return countRequested;
  }

  public void setCountRequested(int countRequested) {
    this.countRequested = countRequested;
  }

  public int getCountCompleted() {
    return countCompleted;
  }

  public void setCountCompleted(int countCompleted) {
    this.countCompleted = countCompleted;
  }

  public boolean isWaitingResult() {
    return waitingResult;
  }

  public void setWaitingResult(boolean waitingResult) {
    this.waitingResult = waitingResult;
  }
}
