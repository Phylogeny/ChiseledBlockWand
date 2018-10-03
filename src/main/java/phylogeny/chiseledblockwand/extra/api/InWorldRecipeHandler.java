package phylogeny.chiseledblockwand.extra.api;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;

public class InWorldRecipeHandler implements IRecipeWrapperFactory<InWorldRecipe>
{
	@Override
	public IRecipeWrapper getRecipeWrapper(InWorldRecipe recipe)
	{
		return recipe;
	}
}