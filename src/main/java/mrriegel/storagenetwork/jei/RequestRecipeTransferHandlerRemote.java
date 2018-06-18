package mrriegel.storagenetwork.jei;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
//import mezz.jei.gui.ingredients.GuiIngredient;
import mrriegel.storagenetwork.network.ClearMessage;
import mrriegel.storagenetwork.network.RecipeMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
import mrriegel.storagenetwork.remote.ContainerRemote;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Optional;

@SuppressWarnings("rawtypes")
@Optional.Interface(iface = "mezz.jei.api.recipe.transfer.IRecipeTransferHandler", modid = "jei", striprefs = true)
public class RequestRecipeTransferHandlerRemote<C extends Container> implements IRecipeTransferHandler {

  @Override
  public Class<? extends Container> getContainerClass() {
    return ContainerRemote.class;
  }

  @Override
  public IRecipeTransferError transferRecipe(Container container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
    if (doTransfer) {
      PacketRegistry.INSTANCE.sendToServer(new ClearMessage());
      NBTTagCompound nbt = RequestRecipeTransferHandler.recipeToTag(container, recipeLayout);
      PacketRegistry.INSTANCE.sendToServer(new RecipeMessage(nbt));
    }
    return null;
  }
}
