package phylogeny.chiseledblockwand.extra.api;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import phylogeny.chiseledblockwand.example.api.ChiselsAndBitsAPIProxy;

@JEIPlugin
public class JustEnoughItemsPlugin implements IModPlugin
{
	@Override
	public void register(IModRegistry registry)
	{
		if (!ChiselsAndBitsAPIProxy.isApiPresent())
			return;

		registry.handleRecipes(InWorldRecipe.class, new InWorldRecipeHandler(), InWorldRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(Blocks.CRAFTING_TABLE), InWorldRecipeCategory.UID);
		registry.addRecipes(WandRecipe.create(registry.getJeiHelpers().getGuiHelper()), InWorldRecipeCategory.UID);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry)
	{
		if (ChiselsAndBitsAPIProxy.isApiPresent())
			registry.addRecipeCategories(new InWorldRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
	}
}