package phylogeny.chiseledblockwand.extra.api;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import mezz.jei.api.IGuiHelper;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import net.minecraft.init.Blocks;
import phylogeny.chiseledblockwand.example.ChiseledBlockWand;
import phylogeny.chiseledblockwand.example.init.ItemsMod;

public class WandRecipe extends InWorldRecipe
{
	protected WandRecipe(IGuiHelper guiHelper) throws InvalidBitItem
	{
		super(guiHelper, ItemsMod.wand, new BitBox(Blocks.OBSIDIAN, 3, 0, 7, 13, 2, 9),
				new BitBox(Blocks.QUARTZ_BLOCK, 0, 0, 7, 3, 2, 9), new BitBox(Blocks.QUARTZ_BLOCK, 13, 0, 7, 16, 2, 9));
	}

	@Nullable
	public static List<WandRecipe> create(IGuiHelper guiHelper)
	{
		List<WandRecipe> recipes = new ArrayList<>();
		try
		{
			recipes.add(new WandRecipe(guiHelper));
		}
		catch (InvalidBitItem e)
		{
			ChiseledBlockWand.logger.error("Wand recipe failed to initialize -- some of the bits for this in-world recipe are disabled.");
		}
		return recipes;
	}
}