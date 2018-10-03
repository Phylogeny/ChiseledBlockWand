package phylogeny.chiseledblockwand.example;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleCloud;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

/**
 * Adds convenient client methods, and acts as a means of preventing client imports from crashing dedicated servers 
 */
public class ClientHelper
{
	private static Minecraft getMinecraft()
	{
		return Minecraft.getMinecraft();
	}

	public static IThreadListener getThreadListener()
	{
		return getMinecraft();
	}

	public static World getWorld()
	{
		return getMinecraft().world;
	}

	public static EntityPlayer getPlayer()
	{
		return getMinecraft().player;
	}

	public static RayTraceResult getObjectMouseOver()
	{
		return getMinecraft().objectMouseOver;
	}

	public static ParticleManager getParticleManager()
	{
		return getMinecraft().effectRenderer;
	}

	public static void printTranslationChatMessageWithDeletion(String key)
	{
		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentTranslation(key), 769050);
	}

	public static RenderItem getRenderItem()
	{
		return getMinecraft().getRenderItem();
	}

	public static void generateRandomFireworkExplosionWithSmoke(Vec3d loc)
	{
		// Generates random firework NBT data
		World world = getWorld();
		Random rand = world.rand;
		String data = "{Flight:1b,Explosions:[{";
		data += "Flicker:" + rand.nextInt(2) + "b,";
		int type = rand.nextInt(4);
		data += "Type:" + (type < 3 ? type : 4) + "b,";
		data += "Trail:" + rand.nextInt(2) + "b,";
		data += "Colors:[I;" + getRandomColor(rand) + "],";
		data += "FadeColors:[I;" + getRandomColor(rand) + "]}]}";
		NBTTagCompound nbt;
		try
		{
			nbt = JsonToNBT.getTagFromJson(data);
		}
		catch (NBTException e)
		{
			nbt = new NBTTagCompound();
		}

		// Spawn cloud particles and initiate firework explosion
		ParticleManager particleManager = Minecraft.getMinecraft().effectRenderer;
		for (int i = 0; i < 20; i++)
		{
			particleManager.addEffect(new ParticleCloud.Factory().createParticle(0, world,
					loc.x + (rand.nextDouble() - 0.5) * 0.6, loc.y + (rand.nextDouble() - 0.5) * 0.6, loc.z + (rand.nextDouble() - 0.5) * 0.6,
					(rand.nextDouble() - 0.5) * 0.2, (rand.nextDouble() - 0.5) * 0.2, (rand.nextDouble() - 0.5) * 0.2));
		}
		particleManager.addEffect(new ParticleFirework.Starter(world, loc.x, loc.y, loc.z, 0, 0, 0, particleManager, nbt));
	}

	private static int getRandomColor(Random rand)
	{
		return (rand.nextInt(256) << 16) | (rand.nextInt(256) << 8) | rand.nextInt(256);
	}
}