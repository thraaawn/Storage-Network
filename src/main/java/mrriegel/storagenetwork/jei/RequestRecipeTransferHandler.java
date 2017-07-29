package mrriegel.storagenetwork.jei;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.primitives.Ints;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
//import mezz.jei.gui.ingredients.GuiIngredient;
import mrriegel.storagenetwork.network.ClearMessage;
import mrriegel.storagenetwork.network.PacketHandler;
import mrriegel.storagenetwork.network.RecipeMessage;
import mrriegel.storagenetwork.request.ContainerRequest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.oredict.OreDictionary;

@SuppressWarnings("rawtypes")
@Optional.Interface(iface = "mezz.jei.api.recipe.transfer.IRecipeTransferHandler", modid = "jei", striprefs = true)
public class RequestRecipeTransferHandler<C extends Container> implements IRecipeTransferHandler {
  @Override
  public Class<? extends Container> getContainerClass() {
    return ContainerRequest.class;
  }
 
  @Override
  public IRecipeTransferError transferRecipe(Container container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
    if (doTransfer) {
      PacketHandler.INSTANCE.sendToServer(new ClearMessage());
      Map<Integer, ? extends IGuiIngredient<ItemStack>> inputs = recipeLayout.getItemStacks().getGuiIngredients();
      Map<Integer, List<ItemStack>> map = new HashMap<Integer, List<ItemStack>>();
      for (int j = 0; j < container.inventorySlots.size(); j++) {
        Slot slot = container.inventorySlots.get(j);
        if ((slot.inventory instanceof InventoryCrafting)) {
          try {
            IGuiIngredient<ItemStack> ingredient = inputs.get(slot.getSlotIndex() + 1);
            if (ingredient != null) {
              map.put(j, ingredient.getAllIngredients());
            }
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      NBTTagCompound nbt = new NBTTagCompound();
      for (int j = 1; j < 10; j++) {
        if (map.get(j) != null) {
          if (getOreDict(map.get(j)) != null)
            nbt.setString("s" + j, getOreDict(map.get(j)));
          else {
            NBTTagList invList = new NBTTagList();
            for (int i = 0; i < map.get(j).size(); i++) {
              if (!map.get(j).get(i).isEmpty()) {
                NBTTagCompound stackTag = new NBTTagCompound();
                map.get(j).get(i).writeToNBT(stackTag);
                invList.appendTag(stackTag);
              }
            }
            nbt.setTag("s" + j, invList);
          }
        }
      }
      PacketHandler.INSTANCE.sendToServer(new RecipeMessage(nbt, 0));
    }
    return null;
  }
  private String getOreDict(List<ItemStack> lis) {
    if (lis.size() < 2 || lis.get(0).isEmpty())
      return null;
    for (int i : OreDictionary.getOreIDs(lis.get(0))) {
      boolean foo = true;
      for (ItemStack stack : lis) {
        if (!Ints.asList(OreDictionary.getOreIDs(stack)).contains(i)) {
          foo = false;
          break;
        }
      }
      if (foo) { return OreDictionary.getOreName(i); }
    }
    return null;
  }
}
