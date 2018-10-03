package phylogeny.chiseledblockwand.example;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import phylogeny.chiseledblockwand.example.init.PacketRegistration;
import phylogeny.chiseledblockwand.extra.BitCrafting;

@Mod(modid = ChiseledBlockWand.MOD_ID,
	 version = ChiseledBlockWand.VERSION,
	 acceptedMinecraftVersions = ChiseledBlockWand.MC_VERSIONS_ACCEPTED,
	 updateJSON = ChiseledBlockWand.UPDATE_JSON)
public class ChiseledBlockWand
{
	public static final String MOD_ID = "chiseledblockwand";
	public static final String MOD_NAME = "Chiseled Block Wand";
	public static final String MOD_PATH = "phylogeny." + MOD_ID;
	public static final String VERSION = "@VERSION@";
	public static final String UPDATE_JSON = "@UPDATE@";
	public static final String MC_VERSIONS_ACCEPTED = "[1.12.2,)";
	public static final String PROXY_CLIENT = MOD_PATH + ".example.ProxyClient";
	public static boolean jeiLoaded;
	public static Logger logger;
	public static SimpleNetworkWrapper packetNetwork = NetworkRegistry.INSTANCE.newSimpleChannel(ChiseledBlockWand.MOD_ID);

	@SidedProxy(clientSide = ChiseledBlockWand.PROXY_CLIENT)
	public static IProxy proxy;
	public static class ServerProxy implements IProxy {}

	public interface IProxy
	{
		default void registerKeyBindings() {}
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		proxy.registerKeyBindings();
		if (Loader.isModLoaded("chiselsandbits"))
		{
			PacketRegistration.registerPackets();
			MinecraftForge.EVENT_BUS.register(new BitCrafting());
			jeiLoaded = Loader.isModLoaded("jei");
		}
	}

	@EventHandler
	public void onServerStarting(@SuppressWarnings("unused") FMLServerStartingEvent event)
	{
		ConfigMod.SERVER_SYNCED.initServerValues();
	}
}