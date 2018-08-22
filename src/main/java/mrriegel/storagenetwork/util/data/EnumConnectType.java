package mrriegel.storagenetwork.util.data;

import net.minecraft.util.IStringSerializable;

public enum EnumConnectType implements IStringSerializable {
  CONNECT, STORAGE, NULL;

  @Override
  public String getName() {
    return name().toLowerCase();
  }
}