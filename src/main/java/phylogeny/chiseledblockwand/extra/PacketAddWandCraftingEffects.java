package phylogeny.chiseledblockwand.extra;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import phylogeny.chiseledblockwand.example.ClientHelper;
import phylogeny.chiseledblockwand.example.ConfigMod;

public class PacketAddWandCraftingEffects implements IMessage
{
	private Vec3d loc;

	public PacketAddWandCraftingEffects() {}

	public PacketAddWandCraftingEffects(Vec3d loc)
	{
		this.loc = loc;
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeDouble(loc.x);
		buffer.writeDouble(loc.y);
		buffer.writeDouble(loc.z);
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		loc = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
	}

	public static class Handler implements IMessageHandler<PacketAddWandCraftingEffects, IMessage>
	{
		@Override
		public IMessage onMessage(PacketAddWandCraftingEffects message, MessageContext ctx)
		{
			if (ConfigMod.CLIENT.effects.craftingEnabled)
				ClientHelper.getThreadListener().addScheduledTask(() -> ClientHelper.generateRandomFireworkExplosionWithSmoke(message.loc));

			return null;
		}
	}
}