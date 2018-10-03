package phylogeny.chiseledblockwand.extra.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Stopwatch;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.api.StateCount;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.client.config.GuiUtils;
import phylogeny.chiseledblockwand.example.api.ChiselsAndBitsAPI;

public abstract class InWorldRecipe implements IRecipeWrapper
{
	private List<ItemStack> inputs;
	private ItemStack output, renderStack;
	private List<String> tooltipLines;
	private ItemStack craftingTable, grass;
	private List<IDrawable> slots;
	private Stopwatch timer;

	public static class BitBox
	{
		private IBlockState state;
		private AxisAlignedBB box;

		public BitBox(Block block, int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
		{
			state = block.getDefaultState();
			box = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
		}
	}

	protected InWorldRecipe(IGuiHelper guiHelper, Item output, BitBox... bitBoxes) throws InvalidBitItem
	{
		inputs = new ArrayList<>();
		slots = new ArrayList<>();
		IChiselAndBitsAPI api = ChiselsAndBitsAPI.api;
		IBitAccess bitAccess = api.createBitItem(ItemStack.EMPTY);
		if (bitAccess == null)
			throw new InvalidBitItem();

		// Build the bitaccess from the bit boxes
		for (BitBox bitBox : bitBoxes)
		{
			IBitBrush bit = api.createBrushFromState(bitBox.state);
			bitAccess.visitBits((x, y, z, bitCurrent) -> bitBox.box.contains(new Vec3d(x + 0.5, y + 0.5, z + 0.5)) ? bit : bitCurrent);
		}

		// Create backgrounds and stacks for each bit type
		for (StateCount stateCount : bitAccess.getStateCounts())
		{
			if (stateCount.stateId > 0)
			{
				inputs.add(api.createBrushFromState(Block.getStateById(stateCount.stateId)).getItemStack(stateCount.quantity));
				slots.add(guiHelper.getSlotDrawable());
			}
		}
		this.output = new ItemStack(output);
		tooltipLines = new ArrayList<>();
		tooltipLines.addAll(Arrays.asList(I18n.format(InWorldRecipeCategory.LANG_KEY + ".hover_text").split("\\\\n")));
		craftingTable = new ItemStack(Blocks.CRAFTING_TABLE);
		grass = new ItemStack(Blocks.GRASS);
		renderStack = bitAccess.getBitsAsItem(null, ItemType.CHISLED_BLOCK, false);
	}

	@Override
	public void getIngredients(IIngredients ingredients)
	{
		ingredients.setInputLists(ItemStack.class, Collections.singletonList(inputs));
		ingredients.setOutputs(ItemStack.class, Collections.singletonList(output));
	}

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		List<String> tooltip = new ArrayList<String>();
		if (mouseX < 90)
		{
			// Add instructions to in-world preview
			tooltip.addAll(tooltipLines);
			return tooltip;
		}
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		if (screen == null)
			return tooltip;

		// Add tooltips for hovered bit stacks
		int x = 105;
		for (int i = 0; i < inputs.size(); i++)
		{
			int y = 65 + i * 20;
			if (mouseX > x && mouseX < x + 18 && mouseY > y && mouseY < y + 18)
				tooltip.addAll(screen.getItemToolTip(inputs.get(i)));
		}
		return tooltip;
	}

	@Override
	public void drawInfo(Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		if (timer == null)
			timer = Stopwatch.createStarted();

		if (timer.isRunning() && GuiScreen.isShiftKeyDown())
			timer.stop();
		else if (!timer.isRunning() && !GuiScreen.isShiftKeyDown())
			timer.start();

		// Use GL scissors to render in-world preview within the area
		final ScaledResolution sr = new ScaledResolution(mc);
		final int mouseAbsX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
		final int mouseAbsY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		int scaleFactor = sr.getScaleFactor();
		int width = recipeWidth + 4 - 70;
		int height = recipeHeight + 4;
		int x = mouseAbsX - mouseX - 2;
		int y = mouseAbsY - mouseY + 2 + recipeHeight - height;
		GL11.glScissor(x * scaleFactor, mc.displayHeight - (y + height) * scaleFactor, width * scaleFactor, height * scaleFactor);
		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(width * 0.5 - 2, recipeHeight - height * 0.2, 0);
			GlStateManager.scale(60, 60, 60);

			GlStateManager.translate(0, 0, 10);
			GlStateManager.rotate(180, 0, 0, 1);
			GlStateManager.rotate(-30, 1, 0, 0);
			GlStateManager.rotate(45, 0, 1, 0);
			GlStateManager.rotate(360 * (timer.elapsed(TimeUnit.MILLISECONDS) / 10000F), 0, 1, 0);

			// Render ground, crafting table, and chiseled block
			renderTiledItemModelTop(mc, grass, 4);
			RenderHelper.enableStandardItemLighting();
			GlStateManager.disableDepth();
			mc.getRenderItem().renderItem(craftingTable, TransformType.NONE);
			GlStateManager.translate(0, 1, 0);
			GlStateManager.scale(2, 2, 2);
			mc.getRenderItem().renderItem(renderStack, TransformType.NONE);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.scale(0.5, 0.5, 0.5);

			// Render bounding box around chiseled block
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			GlStateManager.glLineWidth(2);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
			AxisAlignedBB box = Block.FULL_BLOCK_AABB.grow(0.0020000000949949026).offset(-0.5, -0.5, -0.5);
			buffer.pos(box.minX, box.minY, box.minZ).color(255, 255, 255, 0.4F).endVertex();
			buffer.pos(box.minX, box.maxY, box.minZ).color(255, 255, 255, 0.4F).endVertex();
			buffer.pos(box.maxX, box.maxY, box.minZ).color(255, 255, 255, 0.4F).endVertex();
			buffer.pos(box.maxX, box.minY, box.minZ).color(255, 255, 255, 0.4F).endVertex();
			buffer.pos(box.maxX, box.maxY, box.minZ).color(255, 255, 255, 0).endVertex();
			buffer.pos(box.maxX, box.maxY, box.maxZ).color(255, 255, 255, 0.4F).endVertex();
			buffer.pos(box.maxX, box.minY, box.maxZ).color(255, 255, 255, 0.4F).endVertex();
			buffer.pos(box.maxX, box.maxY, box.maxZ).color(255, 255, 255, 0).endVertex();
			buffer.pos(box.minX, box.maxY, box.maxZ).color(255, 255, 255, 0.4F).endVertex();
			buffer.pos(box.minX, box.minY, box.maxZ).color(255, 255, 255, 0.4F).endVertex();
			buffer.pos(box.minX, box.maxY, box.maxZ).color(255, 255, 255, 0).endVertex();
			buffer.pos(box.minX, box.maxY, box.minZ).color(255, 255, 255, 0.4F).endVertex();
			tessellator.draw();
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
		}
		GlStateManager.popMatrix();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);

		// Render background for required bit stacks
		int widthSlot = 20;
		GuiUtils.drawContinuousTexturedBox(InWorldRecipeCategory.TEXTURE_GUI, 100, 60, 0, 0, 54, 8 + slots.size() * widthSlot, 32, 16, 2, 0);
		GlStateManager.translate(0, 0, 10);
		x = 105;
		for (int i = 0; i < slots.size(); i++)
		{
			// Render bit stacks with required counts
			y = 65 + i * widthSlot;
			slots.get(i).draw(mc, x, y);
			GlStateManager.enableRescaleNormal();
			RenderHelper.enableGUIStandardItemLighting();
			mc.getRenderItem().renderItemIntoGUI(inputs.get(i), x + 1, y + 1);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableRescaleNormal();
			if (mouseX > x && mouseX < x + 18 && mouseY > y && mouseY < y + 18)
			{
				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				GlStateManager.colorMask(true, true, true, false);
				Gui.drawRect(x + 1, y + 1, x + 17, y + 17, -2130706433);
				GlStateManager.colorMask(true, true, true, true);
				GlStateManager.enableDepth();
			}
			mc.fontRenderer.drawString(Integer.toString(inputs.get(i).getCount()), x + 21, y + 5, 0);
			GlStateManager.color(1, 1, 1, 1);
		}
	}

	/**
	 * Renders multiple instances of the passed stack's model's top facing quads in a square with the passed semi-diameter
	 */
	protected void renderTiledItemModelTop(Minecraft mc, ItemStack stack, int semiDiameter)
	{
		if (stack.isEmpty())
			return;

		IBakedModel model = mc.getRenderItem().getItemModelWithOverrides(stack, null, null);
		TextureManager textureManager = mc.getTextureManager();
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.pushMatrix();
		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(-0.5F, -0.5F, -0.5F);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			List<BakedQuad> quads = model.getQuads(null, EnumFacing.UP, 0L);
			int x, z;
			for (x = -semiDiameter; x <= semiDiameter; x++)
			{
				for (z = -semiDiameter; z <= semiDiameter; z++)
				{
					GlStateManager.pushMatrix();
					{
						GlStateManager.translate(x, -1, z);
						buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
						int i = 0;
						for (int j = quads.size(); i < j; i++)
						{
							BakedQuad quad = quads.get(i);
							int color = -1;
							if (quad.hasTintIndex())
							{
								color = mc.getItemColors().colorMultiplier(stack, quad.getTintIndex());
								if (EntityRenderer.anaglyphEnable)
									color = TextureUtil.anaglyphColor(color);

								color = color | -16777216;
							}
							LightUtil.renderQuadColor(buffer, quad, color);
						}
						tessellator.draw();
					}
					GlStateManager.popMatrix();
				}
			}
		}
		GlStateManager.popMatrix();
		GlStateManager.cullFace(GlStateManager.CullFace.BACK);
		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
	}
}