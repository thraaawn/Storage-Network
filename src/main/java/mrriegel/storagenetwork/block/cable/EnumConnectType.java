package mrriegel.storagenetwork.block.cable;

import net.minecraft.util.IStringSerializable;

public enum EnumConnectType implements IStringSerializable {
  CONNECT, STORAGE, NULL;

  @Override
  public String getName() {
    return name().toLowerCase();
  }
}