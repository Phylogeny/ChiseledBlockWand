package phylogeny.chiseledblockwand.extra;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.EventBlockBitPostModification;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.StateCount;
import mod.chiselsandbits.api.VoxelStats;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import phylogeny.chiseledblockwand.example.ChiseledBlockWand;
import phylogeny.chiseledblockwand.example.ConfigMod;
import phylogeny.chiseledblockwand.example.api.ChiselsAndBitsAPI;
import phylogeny.chiseledblockwand.example.init.ItemsMod;

/**
 * Allows items to be crafted in the world by chiseling blocks above crafting tables
 */
public class BitCrafting
{
	@SubscribeEvent
	public void craftWandWithBits(EventBlockBitPostModification event)
	{
		// Return if no crafting tale below chiseled block
		if (!ConfigMod.SERVER.bitCraftingWand.enabled || event.getWorld().isRemote
				|| !(event.getWorld().getBlockState(event.getPos().down()).getBlock() instanceof BlockWorkbench))
			return;

		IChiselAndBitsAPI api = ChiselsAndBitsAPI.api;
		IBitAccess bitAccess;
		try
		{
			bitAccess = api.getBitAccess(event.getWorld(), event.getPos());
		}
		catch (CannotBeChiseled e)
		{
			return;
		}

		// Return if chiseled block is not predominately obsidian bits of count 40
		VoxelStats voxelStats = bitAccess.getVoxelStats();
		int obsidian = Block.getStateId(Blocks.OBSIDIAN.getDefaultState());
		if (voxelStats.mostCommonState != obsidian || voxelStats.mostCommonStateTotal != 40)
			return;

		// Return if there are not two other bit types
		int quartz = Block.getStateId(Blocks.QUARTZ_BLOCK.getDefaultState());
		List<StateCount> stateCounts = bitAccess.getStateCounts();
		if (stateCounts.size() != 3)
			return;

		// Return if remainder of bits are not 24 quartz and 4032 air
		StateCount state = stateCounts.get(0);
		if (state.stateId != quartz)
		{
			if (state.stateId != 0 && state.stateId != obsidian)
				return;

			state = stateCounts.get(1);
			if (state.stateId != quartz)
			{
				if (state.stateId != 0 && state.stateId != obsidian)
					return;

				state = stateCounts.get(2);
			}
		}
		if (state.stateId != quartz || state.quantity != 24)
			return;

		// Return if chiseled block has more than one collision box
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		List<AxisAlignedBB> boxesCollision = new ArrayList<>();
		world.getBlockState(pos).addCollisionBoxToList(world, pos, TileEntity.INFINITE_EXTENT_AABB, boxesCollision, null, true);
		if (boxesCollision.size() != 1)
			return;

		// Return if collision box dimensions are not 2x2x16 bits
		AxisAlignedBB box = boxesCollision.get(0).offset(-pos.getX(), -pos.getY(), -pos.getZ());
		double pixel = 0.0625;
		int minX = (int) (box.minX / pixel);
		int minY = (int) (box.minY / pixel);
		int minZ = (int) (box.minZ / pixel);
		int maxX = Math.min((int) (box.maxX / pixel), 15);
		int maxY = Math.min((int) (box.maxY / pixel), 15);
		int maxZ = Math.min((int) (box.maxZ / pixel), 15);
		int lenX = maxX - minX;
		int lenY = maxY - minY;
		int lenZ = maxZ - minZ;
		Axis axis;
		if (lenX == 15)
		{
			if (lenY != 1 || lenZ != 1)
				return;

			axis = Axis.X;
		}
		else if (lenY == 15)
		{
			if (lenX != 1 || lenZ != 1)
				return;

			axis = Axis.Y;
		}
		else if (lenZ == 15)
		{
			if (lenX != 1 || lenY != 1)
				return;

			axis = Axis.Z;
		}
		else
			return;

		// Return if bit access is not comprised of a 2x2x10 wand body, and two 2x2x3 wand caps
		double bodyStart = 3;
		double bodyEnd = 15 - bodyStart;
		int x, y, z, id;
		try
		{
			for (x = minX; x <= maxX; x++)
			{
				for (y = minY; y <= maxY; y++)
				{
					for (z = minZ; z <= maxZ; z++)
					{
						id = bitAccess.getBitAt(x, y, z).getStateID();
						switch (axis)
						{
							case X: if (id != (x < bodyStart || x > bodyEnd ? quartz : obsidian))
										return;
									break;
							case Y: if (id != (y < bodyStart || y > bodyEnd ? quartz : obsidian))
										return;
									break;
							case Z: if (id != (z < bodyStart || z > bodyEnd ? quartz : obsidian))
										return;
						}
						bitAccess.setBitAt(x, y, z, null);
					}
				}
			}
		}
		catch (SpaceOccupied e)
		{
			return;
		}

		// Remove bits from world, and spawn wand stack with effects
		bitAccess.commitChanges(true);
		Vec3d loc = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		if (ConfigMod.SERVER.bitCraftingWand.generateEffects)
			ChiseledBlockWand.packetNetwork.sendToAllAround(new PacketAddWandCraftingEffects(loc),
					new TargetPoint(world.provider.getDimension(), loc.x, loc.y, loc.z, ConfigMod.SERVER.bitCraftingWand.effectRange));

		EntityItem item = new EntityItem(world, loc.x, loc.y, loc.z, new ItemStack(ItemsMod.wand));
		world.spawnEntity(item);
		if (ConfigMod.SERVER.bitCraftingWand.generateEffects)
			SoundsMod.playSound(null, world, new BlockPos(loc), SoundEvents.ENTITY_PLAYER_LEVELUP, 1);
	}
}