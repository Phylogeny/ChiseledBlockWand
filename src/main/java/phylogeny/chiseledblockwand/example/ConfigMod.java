package phylogeny.chiseledblockwand.example;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Ignore;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import phylogeny.chiseledblockwand.example.packet.PacketSyncClientConfigs;

/**
 * The text passed to {@link net.minecraftforge.common.config.Config.Name @Name} should match the lang file entry specified by 
 * {@link net.minecraftforge.common.config.Config.LangKey @LangKey}.
 * The text passed to {@link net.minecraftforge.common.config.Config.Comment @Comment} should match the lang file entry specified by 
 * {@link net.minecraftforge.common.config.Config.LangKey @LangKey} + ".tooltip".
 * <p>
 * <b>EXAMPLE:</b>
 * <pre>
 * <b>Config Class:</b>
 * @Name("Client")
 * @Comment("Client-only configs.")
 * @LangKey("config.chiseledblockwand.client")
 * public static final Client CLIENT = new Client();
 * 
 * <b>Lang File:</b>
 * config.chiseledblockwand.client=Client
 * config.chiseledblockwand.client.tooltip=Client-only configs.
 * </pre>
 */
@Config(modid = ChiseledBlockWand.MOD_ID, category = "")
@LangKey(ChiseledBlockWand.MOD_ID + ".config.title")
@EventBusSubscriber
public class ConfigMod
{
	private static final String PREFIX = "config." + ChiseledBlockWand.MOD_ID;

	@Name("Client")
	@Comment("Client-only configs.")
	@LangKey(Client.PREFIX)
	public static final Client CLIENT = new Client();

	public static class Client
	{
		private static final String PREFIX = ConfigMod.PREFIX + ".client";

		@Name("Effects")
		@Comment("Configures the spawning of particles and playing of sounds when using/crafting wands.")
		@LangKey(Effects.PREFIX)
		public Effects effects = new Effects();

		public static class Effects
		{
			private static final String PREFIX = Client.PREFIX + ".effects";

			@Name("In-World Crafting Enabled")
			@Comment("If true, particles will spawn and sounds will play upon a successful nearby wand creating operation.")
			@LangKey(PREFIX + ".crafting")
			public boolean craftingEnabled = true;

			@Name("Usage Enabled")
			@Comment("If true, particles will spawn and sounds will play upon using a wand.")
			@LangKey(PREFIX + ".usage")
			public boolean usageEnabled = true;
		}

		@Name("Block Rendering")
		@Comment("Configures in-world rendering of blocks.")
		@LangKey(RenderingBlocks.PREFIX)
		public RenderingBlocks rendering = new RenderingBlocks();

		public static class RenderingBlocks
		{
			private static final String PREFIX = Client.PREFIX + ".rendering.blocks";

			@Name("Render Ghosts")
			@Comment("If true, semi-transparent ghosts of the blocks to be placed in-world will render.")
			@LangKey(PREFIX + ".ghost")
			public boolean renderGhosts = true;
		}
	}

	@Name("Server")
	@Comment("Server-only configs.")
	@LangKey(Server.PREFIX)
	public static final Server SERVER = new Server();

	public static class Server
	{
		private static final String PREFIX = ConfigMod.PREFIX + ".server";

		@Name("In-World Bit Crafting of Wands")
		@Comment("Configures the in-world crafting of wands via the chiseling blocks above crafting tables.")
		@LangKey(BitCraftingWand.PREFIX)
		public BitCraftingWand bitCraftingWand = new BitCraftingWand();

		public static class BitCraftingWand
		{
			private static final String PREFIX = Server.PREFIX + ".bit_crafting.wand";

			@Name("Enabled")
			@Comment("If true, chiseling a wand with a 2x2x10 body of obsidian and 2x2x3 caps of quarts above a crafting table will convert it into a wand item.")
			@LangKey(PREFIX + ".enabled")
			public boolean enabled = true;

			@Name("Send Effect Packets")
			@Comment("If true, packets will be sent to all nearby players to generate particles and play sounds upon a successful creating operation.")
			@LangKey(PREFIX + ".effects.send")
			public boolean generateEffects = true;

			@Name("Effect Radius")
			@Comment("When a wand is crafted, any player in this radius will be sent an effect packet, if enabled.")
			@LangKey(PREFIX + ".effects.radius")
			@RangeDouble(min = 0)
			public double effectRange = 50;
		}

		@Name("Effects")
		@Comment("Configures the playing of sounds for everyone when using wands.")
		@LangKey(Effects.PREFIX)
		public Effects effects = new Effects();

		public static class Effects
		{
			private static final String PREFIX = Server.PREFIX + ".effects";

			@Name("Usage Sounds Enabled")
			@Comment("If true, sounds will play for everyone upon using a wand.")
			@LangKey(PREFIX + ".uesage.sounds")
			public boolean usageEnabled = true;
		}

		@Name("Data Storage")
		@Comment("Configures the format that chiseld blocks are saved in.")
		@LangKey(DataStorage.PREFIX)
		public DataStorage dataStorage = new DataStorage();

		public static class DataStorage
		{
			private static final String PREFIX = Server.PREFIX + ".storage.data";

			@Name("Save Blocks Cross-World")
			@Comment("If true, blocks will be saved/loaded in a more verbose, but cross-world compatible format.")
			@LangKey(PREFIX + ".blocks.cross_world")
			@RequiresMcRestart
			public boolean blocksCrossWorld = true;// The client version of this field is auto-synced with the server version.
		}
	}

	@Ignore
	public static ServerSynced SERVER_SYNCED = new ServerSynced();

	/**
	 * A container for separate versions of configs that need to be determined by the server, yet accessed on both the server and the client.
	 */
	public static class ServerSynced
	{
		public boolean blocksCrossWorld;

		public void initServerValues()
		{
			blocksCrossWorld = SERVER.dataStorage.blocksCrossWorld;
		}

		public void toBytes(ByteBuf buffer)
		{
			buffer.writeBoolean(blocksCrossWorld);
		}

		public void fromBytes(ByteBuf buffer)
		{
			blocksCrossWorld = buffer.readBoolean();
		}
	}

	@SubscribeEvent
	public static void syncClientConfigsWithServer(PlayerLoggedInEvent event)
	{
		// Send server version of synced configs to the client
		ChiseledBlockWand.packetNetwork.sendTo(new PacketSyncClientConfigs(SERVER_SYNCED), (EntityPlayerMP) event.player);
	}

	@SubscribeEvent
	public static void onConfigChanged(OnConfigChangedEvent event)
	{
		if (event.getModID().equalsIgnoreCase(ChiseledBlockWand.MOD_ID))
			ConfigManager.sync(ChiseledBlockWand.MOD_ID, Type.INSTANCE);
	}
}