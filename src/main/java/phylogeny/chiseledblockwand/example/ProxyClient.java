package phylogeny.chiseledblockwand.example;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import phylogeny.chiseledblockwand.example.ChiseledBlockWand.IProxy;
import phylogeny.chiseledblockwand.example.api.ChiselsAndBitsAPIProxy;
import phylogeny.chiseledblockwand.example.init.ItemsMod;
import phylogeny.chiseledblockwand.example.init.KeyBindingsMod;
import phylogeny.chiseledblockwand.example.packet.PacketRotateBlock;
import phylogeny.chiseledblockwand.example.packet.PacketUseWand;

@EventBusSubscriber(value = Side.CLIENT)
public class ProxyClient implements IProxy
{
	private static NBTTagCompound dataSavedBlock;
	private static int displayListSavedBlock;

	@Override
	public void registerKeyBindings()
	{
		KeyBindingsMod.registerKeyBindings();
	}

	@SubscribeEvent
	public static void initItemModels(@SuppressWarnings("unused") ModelRegistryEvent event)
	{
		ModelLoader.setCustomModelResourceLocation(ItemsMod.wand, 0, new ModelResourceLocation(ItemsMod.wand.getRegistryName(), "inventory"));
	}

	@SubscribeEvent
	public static void rotateBlockKeyboard(@SuppressWarnings("unused") KeyInputEvent event)
	{
		rotateBlock();
	}

	@SubscribeEvent
	public static void rotateBlockMouse(@SuppressWarnings("unused") MouseInputEvent event)
	{
		rotateBlock();
	}

	private static void rotateBlock()
	{
		if (ChiselsAndBitsAPIProxy.isApiPresent() && KeyBindingsMod.ROTATE.isKeyDown())
			ChiseledBlockWand.packetNetwork.sendToServer(new PacketRotateBlock());
	}

	@SubscribeEvent
	public static void useWand(MouseEvent event)
	{
		if (!event.isButtonstate() || Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode() + 100 != event.getButton())
			return;

		EntityPlayer player = ClientHelper.getPlayer();
		ItemStack wand = ItemWand.getHeldWand(player);
		if (wand.isEmpty())
			return;

		boolean mainHand = ItemWand.isWandInMainhand(player);
		if (mainHand)
			event.setCanceled(true);

		RayTraceResult target = ClientHelper.getObjectMouseOver();
		if (target != null && target.typeOfHit == Type.BLOCK)
		{
			BlockPos pos = target.getBlockPos();
			EnumFacing facing = target.sideHit;
			if (ChiselsAndBitsAPIProxy.useWand(wand, player, player.world, pos, facing))
			{
				player.swingArm(mainHand ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
				ChiseledBlockWand.packetNetwork.sendToServer(new PacketUseWand(pos, facing));
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void renderBlockGhosts(RenderWorldLastEvent event)
	{
		if (!ChiselsAndBitsAPIProxy.isApiPresent() || !ConfigMod.CLIENT.rendering.renderGhosts)
			return;

		EntityPlayer player = ClientHelper.getPlayer();
		ItemStack wand = ItemWand.getHeldWand(player);
		if (wand.isEmpty())
			return;

		RayTraceResult target = ClientHelper.getObjectMouseOver();
		if (target == null || !target.typeOfHit.equals(RayTraceResult.Type.BLOCK))
			return;

		NBTTagCompound data = wand.getSubCompound(NBTKeys.SAVED_BLOCK);
		if (data == null)
			return;

		double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
		double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
		double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();
		BlockPos pos = target.getBlockPos();
		if (player.isSneaking())
			pos = pos.offset(target.sideHit);

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(pos.getX() - playerX - 0.001, pos.getY() - playerY - 0.001, pos.getZ() - playerZ - 0.001);
			if (data.equals(dataSavedBlock))
				GlStateManager.callList(displayListSavedBlock);
			else
			{
				dataSavedBlock = data;
				if (displayListSavedBlock != 0)
					GlStateManager.glDeleteLists(displayListSavedBlock, 1);

				displayListSavedBlock = GLAllocation.generateDisplayLists(1);
				GlStateManager.glNewList(displayListSavedBlock, GL11.GL_COMPILE_AND_EXECUTE);
				{
					GlStateManager.scale(1.002, 1.002, 1.002);
					GlStateManager.bindTexture(Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId());
					GlStateManager.color(1, 1, 1, 1);
					GlStateManager.enableBlend();
					GlStateManager.enableTexture2D();
					GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

					// Render portion that is in front of other objects
					ChiselsAndBitsAPIProxy.renderGhostBlock(data, pos, 100);

					// Render portion that is behind other objects
					GlStateManager.depthFunc(GL11.GL_GREATER);
					ChiselsAndBitsAPIProxy.renderGhostBlock(data, pos, 50);
					GlStateManager.depthFunc(GL11.GL_LEQUAL);

					GlStateManager.disableBlend();
				}
				GlStateManager.glEndList();
			}
		}
		GlStateManager.popMatrix();
	}
}