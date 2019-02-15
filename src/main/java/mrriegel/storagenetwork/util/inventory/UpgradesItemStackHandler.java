package mrriegel.storagenetwork.util.inventory;

import mrriegel.storagenetwork.api.data.EnumUpgradeType;
import mrriegel.storagenetwork.block.cable.io.ContainerCableIO;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class UpgradesItemStackHandler extends ItemStackHandlerEx {
  public UpgradesItemStackHandler() {
    super(ContainerCableIO.UPGRADE_COUNT);
  }

  @Override
  protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
    return 1;
  }

  public int getUpgradesOfType(EnumUpgradeType upgradeType) {
    int res = 0;
    for (ItemStack stack : getStacks()) {
      if(stack.getItemDamage() != upgradeType.getId()) {
        continue;
      }

      res += stack.getCount();
    }
    return res;
  }
}
