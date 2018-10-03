package phylogeny.chiseledblockwand.example;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.ItemType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * A modified version of BitInventoryHelper from Extra Bit Manipulation
 */
public class BitInventoryHelper
{
	/**
	 * Returns an item's internal inventory
	 */
	@Nullable
	private static IItemHandler getItemHandler(ItemStack stack)
	{
		return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
	}

	/**
	 * Generates a map of the state IDs of bits to the amounts of those bit in the player's inventory
	 */
	public static Map<Integer, Integer> getInventoryBitCounts(IChiselAndBitsAPI api, EntityPlayer player, boolean sortMapByCount)
	{
		Map<Integer, Integer> bitCounts = new HashMap<Integer, Integer>();
		for (int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);
			Set<ItemStack> stacks;
			if (isBitStack(api, stack))
			{
				// Add bit stack to set
				stacks = Collections.singleton(stack);
			}
			else
			{
				IItemHandler itemHandler = getItemHandler(stack);
				if (itemHandler == null)
					continue;

				// Add all bit stacks in item's internal inventory to set
				stacks = new HashSet<>();
				for (int j = 0; j < itemHandler.getSlots(); j++)
					stacks.add(itemHandler.getStackInSlot(j));
			}
			for (ItemStack stackBitType : stacks)
			{
				try
				{
					int bitStateID = api.createBrush(stackBitType).getStateID();
					if (!bitCounts.containsKey(bitStateID))
						bitCounts.put(bitStateID, countInventoryBits(api, player, stackBitType));
				}
				catch (InvalidBitItem e) {}
			}
		}
		// Return value-sorted or unsorted map
		return !sortMapByCount ? bitCounts : bitCounts.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (i1, i2) -> i1, LinkedHashMap::new));
	}

	public static int countInventoryBits(IChiselAndBitsAPI api, EntityPlayer player, ItemStack stackBitType)
	{
		int count = 0;
		for (int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack.isEmpty())
				continue;

			// Count bit stack
			count += getBitCountFromStack(api, stackBitType, stack);
			IItemHandler itemHandler = getItemHandler(stack);
			if (itemHandler == null)
				continue;

			// Count all bit stacks in item's internal inventory
			for (int j = 0; j < itemHandler.getSlots(); j++)
				count += getBitCountFromStack(api, stackBitType, itemHandler.getStackInSlot(j));
		}
		return count;
	}

	private static int getBitCountFromStack(IChiselAndBitsAPI api, ItemStack stackBitType, ItemStack stack)
	{
		return areBitStacksEqual(api, stackBitType, stack) ? stack.getCount() : 0;
	}

	private static boolean areBitStacksEqual(IChiselAndBitsAPI api, ItemStack stackBitType, ItemStack putativeBitStack)
	{
		return isBitStack(api, putativeBitStack) && ItemStack.areItemStackTagsEqual(putativeBitStack, stackBitType);
	}

	public static boolean isBitStack(IChiselAndBitsAPI api, ItemStack putativeBitStack)
	{
		return api.getItemType(putativeBitStack) == ItemType.CHISLED_BIT;
	}

	public static int removeInventoryBits(IChiselAndBitsAPI api, EntityPlayer player, ItemStack stackBitType, int quota)
	{
		if (quota <= 0)
			return quota;

		InventoryPlayer inventoy = player.inventory;
		for (int i = 0; i < inventoy.getSizeInventory(); i++)
		{
			// Remove bits from bit stack
			ItemStack stack = inventoy.getStackInSlot(i);
			quota = removeBitsFromStack(api, stackBitType, quota, inventoy, null, i, stack);

			IItemHandler itemHandler = getItemHandler(stack);
			if (itemHandler == null)
				continue;

			// Remove bits from all bit stacks in item's internal inventory
			for (int j = 0; j < itemHandler.getSlots(); j++)
			{
				quota = removeBitsFromStack(api, stackBitType, quota, null, itemHandler, j, itemHandler.getStackInSlot(j));
				if (quota <= 0)
					break;
			}
			if (quota <= 0)
				break;
		}
		return quota;
	}

	private static int removeBitsFromStack(IChiselAndBitsAPI api, ItemStack stackBitType, int quota,
			@Nullable InventoryPlayer inventory, @Nullable IItemHandler itemHandler, int index, ItemStack stack)
	{
		if (areBitStacksEqual(api, stackBitType, stack))
		{
			int size = stack.getCount();
			if (size > quota)
			{
				// Remove some bits from bit stack
				if (itemHandler != null)
					itemHandler.extractItem(index, quota, false);
				else
					stack.shrink(quota);

				quota = 0;
			}
			else
			{
				// Remove all bits from bit stack
				if (itemHandler != null)
					itemHandler.extractItem(index, size, false);
				else if (inventory != null)
					inventory.setInventorySlotContents(index, ItemStack.EMPTY);

				quota -= size;
			}
		}
		return quota;
	}

	public static void takeGiveOrDropStacks(EntityPlayer player, IChiselAndBitsAPI api, @Nullable Map<Integer, Integer> bitTypes)
	{
		if (bitTypes != null)
		{
			for (Integer stateId : bitTypes.keySet())
			{
				// Construct bit and bit stack
				ItemStack stackBitType;
				IBitBrush bitType;
				try
				{
					stackBitType = api.getBitItem(Block.getStateById(stateId));
					if (stackBitType.getItem() == null)
						continue;

					bitType = api.createBrush(stackBitType);
				}
				catch (InvalidBitItem e)
				{
					continue;
				}

				// Give or take bits
				Vec3d spawnPos = new Vec3d(player.posX, player.posY, player.posZ);
				int totalBits = bitTypes.get(stateId);
				if (totalBits >= 0)
				{
					// Take bits
					if (totalBits > 0)
						removeInventoryBits(api, player, stackBitType, totalBits);

					continue;
				}

				// Give bits
				totalBits *= -1;
				int quota;
				while (totalBits > 0)
				{
					quota = totalBits > 64 ? 64 : totalBits;
					api.giveBitToPlayer(player, bitType.getItemStack(quota), spawnPos);
					totalBits -= quota;
				}
			}
			bitTypes.clear();
			player.inventoryContainer.detectAndSendChanges();
		}
	}
}