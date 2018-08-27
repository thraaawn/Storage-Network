package mrriegel.storagenetwork.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mrriegel.storagenetwork.registry.ModBlocks;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JeiPlugin implements IModPlugin {

  @SuppressWarnings({ "rawtypes", "deprecation" })
  @Override
  public void register(IModRegistry registry) {
    registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(new RequestRecipeTransferHandler());
    registry.getRecipeTransferRegistry().addUniversalRecipeTransferHandler(new RequestRecipeTransferHandlerRemote());
    registry.addRecipeCategoryCraftingItem(new ItemStack(ModBlocks.request), VanillaRecipeCategoryUid.CRAFTING);
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {}

  @Override
  public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {}

  @Override
  public void registerIngredients(IModIngredientRegistration registry) {}

  @Override
  public void registerCategories(IRecipeCategoryRegistration registry) {}
}
