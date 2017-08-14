package mrriegel.storagenetwork.jei;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.primitives.Ints;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mrriegel.storagenetwork.StorageNetwork;
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
      StorageNetwork.log(" recipeLayout  " + recipeLayout);
      Map<Integer, ? extends IGuiIngredient<ItemStack>> inputs = recipeLayout.getItemStacks().getGuiIngredients();
      Map<Integer, List<ItemStack>> map = new HashMap<Integer, List<ItemStack>>();
      for (int j = 0; j < container.inventorySlots.size(); j++) {
        Slot slot = container.inventorySlots.get(j);
        if ((slot.inventory instanceof InventoryCrafting)) {
          try {
            IGuiIngredient<ItemStack> ingredient = inputs.get(slot.getSlotIndex() + 1);
            if (ingredient != null) {
              StorageNetwork.log(" ingredients at j   " + ingredient.getAllIngredients());//this could be empty array, or something like [1xtile.bwm:wood_siding@5]
              //or for an ore dict entry it might be    [1xitem.ingotIron@0]
              map.put(j, ingredient.getAllIngredients());
            }
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      NBTTagCompound nbt = new NBTTagCompound();
      List<ItemStack> current;
      for (int j = 1; j < 10; j++) {//its a 3x3 grid eh
        current = map.get(j);
        if (current != null) {
          StorageNetwork.log("TEST SKIP ORE DICT IN HANDLER!! current at j   " + current);
          //yep this was the issue. dont force oredict here. letJEI recipeIngredients handle it and follow forward
          List<String> oresForStack = null;//getOresForStack(current);
          if (oresForStack != null) {
//            StorageNetwork.log("ORE DIDCT STRING CSV WHA T" + String.join(",", oresForStack));
            nbt.setString("s" + j, String.join(",", oresForStack));
          }
          else {//current is null
            NBTTagList invList = new NBTTagList();
            for (int i = 0; i < current.size(); i++) {
              if (!current.get(i).isEmpty()) {
                NBTTagCompound stackTag = new NBTTagCompound();
                current.get(i).writeToNBT(stackTag);
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
  /**
   * get all matching ore dictionary strings for these recipe inputs
   * 
   * @param lis
   * @return
   */
  private List<String> getOresForStack(List<ItemStack> lis) {
    if (lis == null) { return null; }
    List<String> matchingDicts = new ArrayList<String>();
    //ore dict has no "get keys for stack" ..w ell it does but it gets ids not strings
    String oreName;
    for (ItemStack s : lis) {
      if (s == null || s.isEmpty()) {
        continue;
      }
      for (int i : OreDictionary.getOreIDs(s)) {
        oreName = OreDictionary.getOreName(i);
        if (oreName != null && matchingDicts.contains(oreName) == false)
          matchingDicts.add(oreName);
      }
    }
    //    for (int i : OreDictionary.getOreIDs(lis.get(0))) {
    //      boolean foo = true;
    //      for (ItemStack stack : lis) {
    //        if (!Ints.asList(OreDictionary.getOreIDs(stack)).contains(i)) {
    //          foo = false;
    //          break;//DONT BREAK: an item could be registered to more than one dictionary!
    //        }
    //      }
    //      if (foo) { return OreDictionary.getOreName(i); }
    //    }
    return matchingDicts;
  }
}
