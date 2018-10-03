package phylogeny.chiseledblockwand.extra;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleCloud;
import net.minecraft.client.particle.ParticleDragonBreath;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleSpell;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import phylogeny.chiseledblockwand.example.ClientHelper;
import phylogeny.chiseledblockwand.example.ConfigMod;
import phylogeny.chiseledblockwand.example.ItemWand;

public class EffectsWandUsage
{
	private static float DEG_TO_RAD = 0.017453292F;

	public static void addBlockEffectsSuccess(EntityPlayer player, World world, BlockPos pos, int stateIdMostCommon, boolean wasSawpped)
	{
		if (world.isRemote ? !ConfigMod.CLIENT.effects.usageEnabled : ConfigMod.SERVER.effects.usageEnabled)
			return;

		if (world.isRemote)
		{
			ParticleManager particleManager = ClientHelper.getParticleManager();
			Vec3d hitBlock = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
			Random rand = world.rand;
			for (int i = 0; i < 30; i++)
			{
				Particle particle = new ParticleCloud.Factory().createParticle(0, world,
						hitBlock.x + (rand.nextDouble() - 0.5) * 1.6, hitBlock.y + (rand.nextDouble() - 0.5) * 1.6, hitBlock.z + (rand.nextDouble() - 0.5) * 1.6,
						(rand.nextDouble() - 0.5) * 0.2, (rand.nextDouble() - 0.5) * 0.2, (rand.nextDouble() - 0.5) * 0.2);
				particleManager.addEffect(particle);
			}
		}
		SoundsMod.playSound(player, world, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 0.5F);
		if (wasSawpped)
			SoundsMod.BLOCK_SWAP.play(player, world, pos, 1);
		else
		{
			@SuppressWarnings("deprecation")
			SoundType soundtype = Block.getStateById(stateIdMostCommon).getBlock().getSoundType();
			world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
		}
	}

	public static void addBlockEffectsFailure(EntityPlayer player, World world, BlockPos pos)
	{
		if (world.isRemote ? !ConfigMod.CLIENT.effects.usageEnabled : ConfigMod.SERVER.effects.usageEnabled)
			return;

		if (world.isRemote)
		{
			Vec3d wandOffset = getWandOffset(player, 0.5);
			Vec3d locWand = player.getPositionEyes(1).subtract(0, 0.2, 0).add(wandOffset);
			Random rand = world.rand;
			ParticleManager particleManager = ClientHelper.getParticleManager();
			for (int i = 0; i < 10; i++)
			{
				Particle particle = new ParticleSpell.Factory().createParticle(0, world,
						locWand.x + (rand.nextDouble() - 0.5) * 0.6, locWand.y + (rand.nextDouble() - 0.5) * 0.6, locWand.z + (rand.nextDouble() - 0.5) * 0.6,
						(rand.nextDouble() - 0.5) * 0.2 * wandOffset.x, (rand.nextDouble() - 0.5) * 0.2 * wandOffset.y, (rand.nextDouble() - 0.5) * 0.2 * wandOffset.z);
				particle.setRBGColorF(200, 0, 0);
				particleManager.addEffect(particle);
			}
		}
		SoundsMod.playSound(player, world, pos, SoundEvents.BLOCK_NOTE_HAT, 0.5F);
		SoundsMod.playSound(player, world, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, 0.5F);
	}

	public static void addBlockEffectsCopy(EntityPlayer player, World world, BlockPos pos)
	{
		if (world.isRemote ? !ConfigMod.CLIENT.effects.usageEnabled : ConfigMod.SERVER.effects.usageEnabled)
			return;

		if (world.isRemote)
		{
			RayTraceResult target = ClientHelper.getObjectMouseOver();
			if (target != null)
			{
				Vec3d wandOffset = getWandOffset(player, 0.3);
				Vec3d locWand = player.getPositionEyes(1).subtract(0, 0.2, 0).add(wandOffset);
				Vec3d hitBlock = target.hitVec.add(player.getLookVec().scale(-0.1));
				wandOffset = locWand.subtract(hitBlock).scale(0.1);
				Random rand = world.rand;
				ParticleManager particleManager = ClientHelper.getParticleManager();
				for (int i = 0; i < 20; i++)
				{
					Particle particle = new ParticleDragonBreath.Factory().createParticle(0, world,
							hitBlock.x + (rand.nextDouble() - 0.5) * 0.6, hitBlock.y + (rand.nextDouble() - 0.5) * 0.6, hitBlock.z + (rand.nextDouble() - 0.5) * 0.6,
							wandOffset.x, wandOffset.y, wandOffset.z);
					particle.setMaxAge((int)(5.0D / (rand.nextFloat() * 0.8D + 0.2D)));
					particleManager.addEffect(particle);
				}
			}
		}
		SoundsMod.playSound(player, world, pos, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, 1);
	}

	private static Vec3d getWandOffset(EntityPlayer player, double amount)
	{
		float yaw = player.rotationYaw + (ItemWand.isWandInMainhand(player) ? 90 : -90);
		float x = -MathHelper.sin(-yaw * DEG_TO_RAD - (float) Math.PI);
		float z = -MathHelper.cos(-yaw * DEG_TO_RAD - (float) Math.PI);
		return player.getLookVec().scale(0.75).add(new Vec3d(x, (player.isSneaking() ? -0.2 : 0), z).scale(amount));
	}
}