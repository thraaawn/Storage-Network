package mrriegel.storagenetwork.jei;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mrriegel.storagenetwork.StorageNetwork;
//import mezz.jei.gui.ingredients.GuiIngredient;
import mrriegel.storagenetwork.network.ClearMessage;
import mrriegel.storagenetwork.network.RecipeMessage;
import mrriegel.storagenetwork.registry.PacketRegistry;
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
  public static NBTTagCompound recipeToTag(Container container, IRecipeLayout recipeLayout){
    NBTTagCompound nbt = new NBTTagCompound();
    StorageNetwork.log(" recipeLayout  " + recipeLayout);
    Map<Integer, ? extends IGuiIngredient<ItemStack>> inputs = recipeLayout.getItemStacks().getGuiIngredients();
    Map<Integer, List<ItemStack>> map = new HashMap<Integer, List<ItemStack>>();
    NBTTagCompound recipe = new NBTTagCompound();
    for (Slot slot : container.inventorySlots) {
      if (slot.inventory instanceof InventoryCrafting) {
        //for some reason it was looping like this  (int j = 1; j < 10; j++)
        StorageNetwork.log("found a crafting slot eh" +  slot.getSlotIndex());
        IGuiIngredient<ItemStack> ingredient = inputs.get(slot.getSlotIndex() + 1);
        if (ingredient == null) {
          continue;
        }
        List<ItemStack> possibleItems = ingredient.getAllIngredients();
        if (possibleItems == null) {
          continue;
        }
        NBTTagList invList = new NBTTagList();
        for (int i = 0; i < possibleItems.size(); i++) {
          if (i >= 5) {
            break; // Max 5 possible items to avoid reaching max network packet size
          }
          if (!possibleItems.get(i).isEmpty()) {
            NBTTagCompound stackTag = new NBTTagCompound();
            possibleItems.get(i).writeToNBT(stackTag);
            invList.appendTag(stackTag);
            
            
          }
        }
        nbt.setTag("s" + (slot.getSlotIndex()), invList);
      }
    }
    return nbt;
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
  /**
   * get all matching ore dictionary strings for these recipe inputs
   * 
   * @param lis
   * @return
   */
  private List<String> getOresForStack(List<ItemStack> lis) {
    if (lis == null) {
      return null;
    }
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
