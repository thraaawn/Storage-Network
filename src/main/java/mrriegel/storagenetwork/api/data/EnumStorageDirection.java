package mrriegel.storagenetwork.api.data;
public enum EnumStorageDirection {
  IN, OUT, BOTH;

  public boolean match(EnumStorageDirection way) {
    if (this == BOTH || way == BOTH)
      return true;
    return this == way;
  }

  public EnumStorageDirection next() {
    //NO MORE input only, bad UX. if i see item in grid and cant get => think is broken
    if (this == OUT) {
      return BOTH;
    }
    else {
      return OUT;
    }
    //return values()[(this.ordinal() + 1) % values().length];
  }
}