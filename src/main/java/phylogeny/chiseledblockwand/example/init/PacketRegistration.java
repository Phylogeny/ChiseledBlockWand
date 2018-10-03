package phylogeny.chiseledblockwand.example.init;

import phylogeny.chiseledblockwand.example.ChiseledBlockWand;
import phylogeny.chiseledblockwand.example.packet.PacketRotateBlock;
import phylogeny.chiseledblockwand.example.packet.PacketSyncClientConfigs;
import phylogeny.chiseledblockwand.example.packet.PacketUseWand;
import phylogeny.chiseledblockwand.extra.PacketAddWandCraftingEffects;

public class PacketRegistration
{
	public static int packetId = 0;

	public static enum Side
	{
		CLIENT, SERVER, BOTH;
	}

	public static void registerPackets()
	{
		registerPacket(PacketRotateBlock.Handler.class, PacketRotateBlock.class, Side.SERVER);
		registerPacket(PacketUseWand.Handler.class, PacketUseWand.class, Side.SERVER);
		registerPacket(PacketSyncClientConfigs.Handler.class, PacketSyncClientConfigs.class, Side.CLIENT);
		registerPacket(PacketAddWandCraftingEffects.Handler.class, PacketAddWandCraftingEffects.class, Side.CLIENT);
	}

	/**
	 * Allows a packet to be registered on the client, the server, or on both sides
	 */
	private static void registerPacket(Class handler, Class packet, Side side)
	{
		if (side != Side.CLIENT)
			registerPacket(handler, packet, net.minecraftforge.fml.relauncher.Side.SERVER);

		if (side != Side.SERVER)
			registerPacket(handler, packet, net.minecraftforge.fml.relauncher.Side.CLIENT);
	}

	private static void registerPacket(Class handler, Class packet, net.minecraftforge.fml.relauncher.Side side)
	{
		ChiseledBlockWand.packetNetwork.registerMessage(handler, packet, packetId++, side);
	}
}