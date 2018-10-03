package phylogeny.chiseledblockwand.example.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import phylogeny.chiseledblockwand.example.ChiseledBlockWand;
import phylogeny.chiseledblockwand.example.ClientHelper;
import phylogeny.chiseledblockwand.extra.EffectsWandUsage;

/**
 * Provides a means of accessing C&B's API without reference to API classes or imports. Doing so is required if C&B is not a required mod.
 */
public class ChiselsAndBitsAPIProxy
{
	static boolean apiPresent;

	public static boolean isApiPresent()
	{
		return apiPresent;
	}

	public static boolean isApiPresent(EntityPlayer player, World world, BlockPos pos)
	{
		if (!apiPresent && world.isRemote)
		{
			ClientHelper.printTranslationChatMessageWithDeletion("chat." + ChiseledBlockWand.MOD_ID + ".wand.invalid");
			EffectsWandUsage.addBlockEffectsFailure(player, world, pos);
		}
		return apiPresent;
	}

	public static boolean useWand(ItemStack wand, EntityPlayer player, World world, BlockPos pos, EnumFacing facing)
	{
		return isApiPresent(player, world, pos) ? ChiselsAndBitsAPI.useWand(wand, player, world, pos, facing) : false;
	}

	public static EnumActionResult saveBlockToWand(EntityPlayer player, World world, BlockPos pos, EnumHand hand)
	{
		return isApiPresent(player, world, pos) ? ChiselsAndBitsAPI.saveBlockToWand(player, world, pos, hand) : EnumActionResult.FAIL;
	}

	public static void renderGhostBlock(NBTTagCompound data, BlockPos pos, int alpha)
	{
		// Retrieve the backed model for the saved block stack, and render it with transparency
		if (apiPresent)
			ChiselsAndBitsAPI.api.renderModel(ClientHelper.getRenderItem().getItemModelWithOverrides(new ItemStack(data), null, null), ClientHelper.getWorld(), pos, alpha);
	}
}