package mrriegel.storagenetwork.data;
public enum EnumFilterDirection {
  IN, OUT, BOTH;

  public boolean match(EnumFilterDirection way) {
    if (this == BOTH || way == BOTH)
      return true;
    return this == way;
  }

  public EnumFilterDirection next() {
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