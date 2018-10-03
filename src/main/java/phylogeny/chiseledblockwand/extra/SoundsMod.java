package phylogeny.chiseledblockwand.extra;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import phylogeny.chiseledblockwand.example.ChiseledBlockWand;

@EventBusSubscriber
public enum SoundsMod
{
	BLOCK_SWAP(SoundCategory.BLOCKS);

	private SoundEvent sound;
	private SoundCategory category;

	private SoundsMod()
	{
		this(SoundCategory.MASTER);
	}

	private SoundsMod(SoundCategory category)
	{
		this.category = category;
	}

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event)
	{
		for (SoundsMod sound : SoundsMod.values())
			sound.register(event);
	}

	public void register(RegistryEvent.Register<SoundEvent> event)
	{
		ResourceLocation soundNameResLoc = new ResourceLocation(ChiseledBlockWand.MOD_ID + ":" + name().toLowerCase());
		sound = new SoundEvent(soundNameResLoc).setRegistryName(soundNameResLoc);
		event.getRegistry().register(sound);
	}

	public void play(@Nullable EntityPlayer player, World world, BlockPos pos, float volume)
	{
		world.playSound(player, pos, sound, category, volume, world.rand.nextFloat() * 0.5F + 0.5F);
	}

	public static void playSound(@Nullable EntityPlayer player, World world, BlockPos pos, SoundEvent sound, float volume)
	{
		world.playSound(player, pos, sound, SoundCategory.BLOCKS, volume, 1);
	}
}