package mrriegel.storagenetwork.block.request;

public enum EnumSortType {
  AMOUNT, NAME, MOD;

  private static EnumSortType[] vals = values();

  public EnumSortType next() {
    return vals[(this.ordinal() + 1) % vals.length];
  }
}