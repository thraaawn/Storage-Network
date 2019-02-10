package mrriegel.storagenetwork.api.data;
public enum EnumUpgradeType {
  SPEED(0), OPERATION(1), STACK(2), STOCK(3);

  private int id;

  EnumUpgradeType(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
