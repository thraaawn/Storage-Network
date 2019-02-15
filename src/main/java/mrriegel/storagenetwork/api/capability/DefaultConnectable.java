package mrriegel.storagenetwork.api.capability;

import mrriegel.storagenetwork.api.data.DimPos;

public class DefaultConnectable implements IConnectable {

  DimPos master;
  DimPos self;

  @Override
  public DimPos getMasterPos() {
    return master;
  }

  @Override
  public DimPos getPos() {
    return self;
  }

  @Override
  public void setMasterPos(DimPos masterPos) {
    this.master = masterPos;
  }

  public void setPos(DimPos pos) {
    this.self = pos;
  }
}
