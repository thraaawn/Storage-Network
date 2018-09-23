package mrriegel.storagenetwork.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mrriegel.storagenetwork.block.request.ContainerRequest;
import mrriegel.storagenetwork.gui.GuiHandler;
import mrriegel.storagenetwork.gui.fb.ContainerFastRemote;
import mrriegel.storagenetwork.gui.fb.ContainerFastRequest;
import mrriegel.storagenetwork.item.remote.ContainerRemote;
import mrriegel.storagenetwork.registry.ModBlocks;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JeiPlugin implements IModPlugin {

  @Override
  public void register(IModRegistry registry) {
    registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(new RequestRecipeTransferHandler<>(ContainerRequest.class));
    registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(new RequestRecipeTransferHandlerRemote<>(ContainerRemote.class));
    registry.addRecipeCatalyst(new ItemStack(ModBlocks.request), VanillaRecipeCategoryUid.CRAFTING);
    if (GuiHandler.FB_LOADED) {
      registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(new RequestRecipeTransferHandler<>(ContainerFastRequest.class));
      registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(new RequestRecipeTransferHandlerRemote<>(ContainerFastRemote.class));
      registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(new RequestRecipeTransferHandler<>(ContainerFastRequest.Client.class));
      registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(new RequestRecipeTransferHandlerRemote<>(ContainerFastRemote.Client.class));
    }
  }
}
