package phylogeny.chiseledblockwand.example;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import phylogeny.chiseledblockwand.example.api.ChiselsAndBitsAPIProxy;
import phylogeny.chiseledblockwand.extra.CreativeTabMod;

public class ItemWand extends Item
{
	public ItemWand()
	{
		setRegistryName("wand");
		setUnlocalizedName(ChiseledBlockWand.MOD_ID + ".wand");
		setCreativeTab(CreativeTabMod.CREATIVE_TAB);
		setMaxStackSize(1);
	}

	public static ItemStack getHeldWand(EntityPlayer player)
	{
		for (EnumHand hand : EnumHand.values())
		{
			ItemStack stack = player.getHeldItem(hand);
			if (stack.getItem() instanceof ItemWand)
				return stack;
		}
		return ItemStack.EMPTY;
	}

	public static boolean isWandInMainhand(EntityPlayer player)
	{
		return player.getHeldItemMainhand().getItem() instanceof ItemWand;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
	{
		String prefix = "tooltip." + ChiseledBlockWand.MOD_ID + ".wand.";
		if (!ChiselsAndBitsAPIProxy.isApiPresent())
		{
			tooltip.add(I18n.format(prefix + "invalid"));
			return;
		}
		boolean shiftDown = GuiScreen.isShiftKeyDown();
		if (shiftDown || (!ChiseledBlockWand.jeiLoaded && GuiScreen.isCtrlKeyDown()))
		{
			tooltip.add(I18n.format(prefix + (shiftDown ? "usage" : "crafting")));
			return;
		}
		NBTTagCompound data = stack.getSubCompound(NBTKeys.SAVED_BLOCK);
		if (data != null)
			tooltip.add(I18n.format(prefix + "saved_block", new ItemStack(data).getDisplayName()));

		tooltip.add(I18n.format(prefix + "usage.key"));
		if (!ChiseledBlockWand.jeiLoaded)
			tooltip.add(I18n.format(prefix + "crafting.key"));
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		return ChiselsAndBitsAPIProxy.saveBlockToWand(player, world, pos, hand);
	}
}