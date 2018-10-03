package phylogeny.chiseledblockwand.extra.api;

import javax.annotation.Nullable;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import phylogeny.chiseledblockwand.example.ChiseledBlockWand;

public class InWorldRecipeCategory implements IRecipeCategory<InWorldRecipe>
{
	public static final String TITLE = "in_world_crafting";
	public static final String UID = ChiseledBlockWand.MOD_ID + ":" + TITLE;
	public static final String LANG_KEY = "jei." + ChiseledBlockWand.MOD_ID + ".category." + TITLE;
	public static final ResourceLocation TEXTURE_GUI = new ResourceLocation(ChiseledBlockWand.MOD_ID, "textures/jei/in_world_crafting_gui.png");
	private IDrawable icon, background, backgroundSlot, arow;
	private String title, bitQuota;

	public InWorldRecipeCategory() {}

	public InWorldRecipeCategory(IGuiHelper guiHelper)
	{
		icon = guiHelper.createDrawable(new ResourceLocation(getModName(), "textures/jei/" + TITLE + "_icon.png"), 0, 0, 16, 16, 16, 16);
		backgroundSlot = guiHelper.createDrawable(new ResourceLocation("minecraft", "textures/gui/container/furnace.png"), 111, 30, 26, 26);
		arow = guiHelper.createDrawable(TEXTURE_GUI, 32, 0, 22, 15);
		background = guiHelper.createBlankDrawable(160, 125);
		title = I18n.format(LANG_KEY);
		bitQuota = I18n.format(LANG_KEY + ".bit_quota");
	}

	@Override
	public String getUid()
	{
		return UID;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public String getModName()
	{
		return ChiseledBlockWand.MOD_ID;
	}

	@Override
	@Nullable
	public IDrawable getIcon()
	{
		return icon;
	}

	@Override
	public IDrawable getBackground()
	{
		return background;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, InWorldRecipe recipeWrapper, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(0, false, 132, 10);
		guiItemStacks.set(ingredients);
	}

	@Override
	public void drawExtras(Minecraft mc)
	{
		int offsetX = 100;
		arow.draw(mc, offsetX, 11);
		backgroundSlot.draw(mc, offsetX + 28, 6);
		mc.fontRenderer.drawString(bitQuota, offsetX + 6, 50, 0);
	}
}