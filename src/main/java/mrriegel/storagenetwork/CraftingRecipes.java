package mrriegel.storagenetwork;

import java.util.Arrays;
import java.util.List;
import mrriegel.storagenetwork.remote.ItemRemote;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class CraftingRecipes {

	public static void init() {
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.kabel, 8), "sss", "i i", "sss", 's', new ItemStack(Blocks.STONE_SLAB), 'i', Items.IRON_INGOT);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.exKabel, 4), " k ", "kpk", " k ", 'k', new ItemStack(ModBlocks.kabel), 'p', new ItemStack(Blocks.PISTON));
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.imKabel, 4), " k ", "kpk", " k ", 'k', new ItemStack(ModBlocks.kabel), 'p', new ItemStack(Blocks.HOPPER));
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.storageKabel, 4), " k ", "kpk", " k ", 'k', new ItemStack(ModBlocks.kabel), 'p', new ItemStack(Blocks.CHEST));
 //Kabel));
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.request), "dkd", "kck", "dkd", 'd', Items.GOLD_INGOT, 'k', ModBlocks.kabel, 'c', Blocks.CRAFTING_TABLE);
 
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.master), "dkd", "kck", "dkd", 'd', Blocks.QUARTZ_BLOCK, 'k', ModBlocks.kabel, 'c', Items.DIAMOND);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.upgrade, 1, 0), " c ", "gig", " c ", 'c', Blocks.REDSTONE_BLOCK, 'i', Items.IRON_INGOT, 'g', Items.GOLD_INGOT);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.upgrade, 1, 1), " c ", "rir", " c ", 'c', Items.COMPARATOR, 'i', Items.IRON_INGOT, 'r', Items.REDSTONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.upgrade, 1, 2), " c ", "gig", " c ", 'c', Blocks.REDSTONE_BLOCK, 'i', Items.IRON_INGOT, 'g', Items.BLAZE_POWDER);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.upgrade, 1, 3), "c", "i", "c", 'c', Items.COMPARATOR, 'i', Items.IRON_INGOT);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.remote, 1, 0), " c ", "eie", " c ", 'c', Items.GOLD_INGOT, 'i', ModBlocks.kabel, 'e', Items.ENDER_PEARL);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.remote, 1, 1), "c", "i", "d", 'c', Items.NETHER_STAR, 'i', ModItems.remote, 'd', Items.DIAMOND);
		class Foo extends ShapelessRecipes {
			public Foo(ItemStack output, List<ItemStack> inputList) {
				super(output, inputList);
			}

			@Override
			public ItemStack getCraftingResult(InventoryCrafting inv) {
				ItemStack rem = null;
				ItemStack frem = super.getCraftingResult(inv);
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					if (inv.getStackInSlot(i) != null && inv.getStackInSlot(i).getItem() == ModItems.remote) {
						rem = inv.getStackInSlot(i);
						break;
					}
				}
				if (rem == null)
					return frem;
				else {
					if (rem.getTagCompound() != null)
						ItemRemote.copyTag(rem, frem);
					return frem;
				}
			}
		}
		RecipeSorter.register("storagenetwork:foo", Foo.class, Category.SHAPED, "after:minecraft:shaped before:minecraft:shapeless");

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.duplicator), "  t", " s ", "i  ", 'i', Items.IRON_INGOT, 't', Items.PAPER, 's', "stickWood"));
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.duplicator), ModItems.duplicator);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.toggler), " k ", "iti", " k ", 'k', ModBlocks.kabel, 'i', Items.IRON_INGOT, 't', Blocks.REDSTONE_TORCH);
	}

}
