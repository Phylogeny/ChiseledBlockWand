package phylogeny.chiseledblockwand.example.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import phylogeny.chiseledblockwand.example.ClientHelper;
import phylogeny.chiseledblockwand.example.ConfigMod;
import phylogeny.chiseledblockwand.example.ConfigMod.ServerSynced;

public class PacketSyncClientConfigs implements IMessage
{
	private ServerSynced serverValues = new ServerSynced();

	public PacketSyncClientConfigs() {}

	public PacketSyncClientConfigs(ServerSynced serverValues)
	{
		this.serverValues = serverValues;
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		serverValues.toBytes(buffer);
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		serverValues.fromBytes(buffer);
	}

	public static class Handler implements IMessageHandler<PacketSyncClientConfigs, IMessage>
	{
		@Override
		public IMessage onMessage(PacketSyncClientConfigs message, MessageContext ctx)
		{
			ClientHelper.getThreadListener().addScheduledTask(() -> ConfigMod.SERVER_SYNCED = message.serverValues);
			return null;
		}
	}
}