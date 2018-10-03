package phylogeny.chiseledblockwand.example.api;

import java.util.HashMap;
import java.util.Map;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.ChiselsAndBitsAddon;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.IChiselsAndBitsAddon;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.api.StateCount;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import phylogeny.chiseledblockwand.example.BitInventoryHelper;
import phylogeny.chiseledblockwand.example.ChiseledBlockWand;
import phylogeny.chiseledblockwand.example.ConfigMod;
import phylogeny.chiseledblockwand.example.NBTKeys;
import phylogeny.chiseledblockwand.extra.EffectsWandUsage;

/**
 * Provides access to C&B's API
 * <p>
 * Any class with {@link mod.chiselsandbits.api.ChiselsAndBitsAddon @ChiselsAndBitsAddon} that implements
 * {@link mod.chiselsandbits.api.IChiselsAndBitsAddon IChiselsAndBitsAddon} will allow API access.
 * This must separate from the main class (and must not be referenced) if C&B is not a required mod.
 */
@ChiselsAndBitsAddon
public class ChiselsAndBitsAPI implements IChiselsAndBitsAddon
{
	public static IChiselAndBitsAPI api;

	@Override
	public void onReadyChiselsAndBits(IChiselAndBitsAPI api)
	{
		this.api = api;
		ChiselsAndBitsAPIProxy.apiPresent = true;
	}

	static boolean useWand(ItemStack wand, EntityPlayer player, World world, BlockPos pos, EnumFacing facing)
	{
		boolean success = false;
		try
		{
			if (!player.capabilities.allowEdit)
				return false;

			boolean replaceBlockMode = !player.isSneaking();
			NBTTagCompound data = wand.getSubCompound(NBTKeys.SAVED_BLOCK);
			if (data == null)
				return false;

			if (!replaceBlockMode)
				pos = pos.offset(facing);

			ItemStack stackSaved = new ItemStack(data);
			IBitAccess bitAccessSaved = api.createBitItem(stackSaved);
			if (bitAccessSaved == null)
			{
				// If saved block NBT sub-compound is not null, this shouldn't happen
				ChiseledBlockWand.logger.error("Wand was unable load its saved block.");
				return false;
			}
			IBitAccess bitAccessCurrent;
			try
			{
				bitAccessCurrent = api.getBitAccess(world, pos);
			}
			catch (CannotBeChiseled e)
			{
				return false;
			}
			if (replaceBlockMode)
			{
				if (ItemStack.areItemStacksEqual(stackSaved, getBitsAsBlockStack(bitAccessCurrent)))
					return false;
			}

			// Ensure that the player has enough bits to create the block 
			Map<Integer, Integer> bitsRequired = null;
			if (!player.capabilities.isCreativeMode)
			{
				// Maps state IDs of bits to required amounts to be taken from or given to the player (positive = taken; negative = given)
				bitsRequired = new HashMap<Integer, Integer>();
				countRequiredBits(bitAccessSaved, bitsRequired, false);
				if (replaceBlockMode)
					countRequiredBits(bitAccessCurrent, bitsRequired, true);

				Map<Integer, Integer> bitsPosessed = BitInventoryHelper.getInventoryBitCounts(api, player, false);
				for (Integer stateIdRequired : bitsRequired.keySet())
				{
					int countRequired = bitsRequired.get(stateIdRequired);
					if (countRequired <= 0) // Ignore bits to be given
						continue;

					// Return if bits count is insufficient
					Integer countPosessed = bitsPosessed.get(stateIdRequired);
					if (countPosessed == null || countPosessed < bitsRequired.get(stateIdRequired))
						return false;
				}
			}
			// Clear all bits in block to be replaced
			if (replaceBlockMode)
				bitAccessCurrent.visitBits((x, y, z, bitCurrent) -> null);

			try
			{
				int x, y, z;
				IBitBrush bitSaved, bitCurrent;
				for (x = 0; x < 16; x++)
				{
					for (y = 0; y < 16; y++)
					{
						for (z = 0; z < 16; z++)
						{
							// Set current air bit to solid saved bit
							bitSaved = bitAccessSaved.getBitAt(x, y, z);
							if (!bitSaved.isAir())
							{
								bitCurrent = bitAccessCurrent.getBitAt(x, y, z);
								if (bitCurrent.isAir())
									bitAccessCurrent.setBitAt(x, y, z, bitSaved);
								else
									return false; // Return if the space is occupied
							}
						}
					}
				}
			}
			catch (SpaceOccupied e)
			{
				return false;
			}

			// Begins an undo group on the client
			api.beginUndoGroup(player);
			try
			{
				// Apply changes to the world on the server
				bitAccessCurrent.commitChanges(true);
				if (!world.isRemote)
					BitInventoryHelper.takeGiveOrDropStacks(player, api, bitsRequired);
			}
			finally
			{
				// Ensure client undo group ends
				api.endUndoGroup(player);
			}
			EffectsWandUsage.addBlockEffectsSuccess(player, world, pos, bitAccessCurrent.getVoxelStats().mostCommonState, replaceBlockMode);
			success = true;
			return true;
		}
		finally
		{
			if (!success)
				EffectsWandUsage.addBlockEffectsFailure(player, world, pos);
		}
	}

	public static ItemStack getBitsAsBlockStack(IBitAccess bitAccess)
	{
		return bitAccess.getBitsAsItem(null, ItemType.CHISLED_BLOCK, ConfigMod.SERVER_SYNCED.blocksCrossWorld);
	}

	public static EnumActionResult saveBlockToWand(EntityPlayer player, World world, BlockPos pos, EnumHand hand)
	{
		boolean success = false;
		try
		{
			// Get block in the world as an item stack, then save it to the wand's NBT as a sub-compound
			try
			{
				ItemStack savedBlock = getBitsAsBlockStack(api.getBitAccess(world, pos));
				if (savedBlock.isEmpty())
					return EnumActionResult.FAIL;

				if (!world.isRemote)
				{
					NBTTagCompound data = player.getHeldItem(hand).getOrCreateSubCompound(NBTKeys.SAVED_BLOCK);
					savedBlock.writeToNBT(data);
				}
				EffectsWandUsage.addBlockEffectsCopy(player, world, pos);
			}
			catch (CannotBeChiseled e)
			{
				return EnumActionResult.FAIL;
			}
			success = true;
			return EnumActionResult.SUCCESS;
		}
		finally
		{
			if (!success)
				EffectsWandUsage.addBlockEffectsFailure(player, world, pos);
		}
	}

	/**
	 * Adds the counts of bits in the bit access to a map of amounts to be taken from or given to the player (positive = taken; negative = given)
	 * 
	 * @param bitAccess		access to bits
	 * @param bitsRequired	maps state IDs of bits to the amounts to be given/taken
	 * @param give			if true, negative counts will be added to the map
	 */
	private static void countRequiredBits(IBitAccess bitAccess, Map<Integer, Integer> bitsRequired, boolean give)
	{
		Integer count;
		for (StateCount stateCount : bitAccess.getStateCounts())
		{
			if (stateCount.stateId > 0)
			{
				count = bitsRequired.get(stateCount.stateId);
				bitsRequired.put(stateCount.stateId, (give ? -stateCount.quantity : stateCount.quantity) + (count == null ? 0 : count));
			}
		}
	}
}