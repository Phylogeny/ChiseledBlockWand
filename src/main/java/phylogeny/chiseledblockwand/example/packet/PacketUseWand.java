package phylogeny.chiseledblockwand.example.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import phylogeny.chiseledblockwand.example.ItemWand;
import phylogeny.chiseledblockwand.example.api.ChiselsAndBitsAPIProxy;

public class PacketUseWand implements IMessage
{
	private BlockPos pos;
	private EnumFacing facing;

	public PacketUseWand() {}

	public PacketUseWand(BlockPos pos, EnumFacing facing)
	{
		this.pos = pos;
		this.facing = facing;
	}

	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeLong(pos.toLong());
		buffer.writeInt(facing.ordinal());
	}

	@Override
	public void fromBytes(ByteBuf buffer)
	{
		pos = BlockPos.fromLong(buffer.readLong());
		facing = EnumFacing.getFront(buffer.readInt());
	}

	public static class Handler implements IMessageHandler<PacketUseWand, IMessage>
	{
		@Override
		public IMessage onMessage(PacketUseWand message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() ->
			{
				ItemStack wand = ItemWand.getHeldWand(player);
				if (wand.isEmpty())
					return;

				// Use wand on block if the player isn't too far away to interact with it
				Vec3d pos = new Vec3d(message.pos.getX() + 0.5, message.pos.getY() + 0.5, message.pos.getZ() + 0.5);
				if (player.getPositionEyes(1).distanceTo(pos) <= player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() + 1)
					ChiselsAndBitsAPIProxy.useWand(wand, player, player.world, message.pos, message.facing);
			});
			return null;
		}
	}
}
