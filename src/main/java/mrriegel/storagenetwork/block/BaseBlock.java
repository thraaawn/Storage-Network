package mrriegel.storagenetwork.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;

public abstract class BaseBlock extends BlockContainer {
  public BaseBlock(Material materialIn, String registryName) {
    super(materialIn);
    
    this.setRegistryName(registryName);
    this.setUnlocalizedName(this.getRegistryName().toString());
  }


}
