package phylogeny.chiseledblockwand.example.packet;

import io.netty.buffer.ByteBuf;
import mod.chiselsandbits.api.IBitAccess;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import phylogeny.chiseledblockwand.example.ItemWand;
import phylogeny.chiseledblockwand.example.NBTKeys;
import phylogeny.chiseledblockwand.example.api.ChiselsAndBitsAPI;

public class PacketRotateBlock implements IMessage
{
	public PacketRotateBlock() {}

	@Override
	public void fromBytes(ByteBuf buffer) {}

	@Override
	public void toBytes(ByteBuf buffer) {}

	public static class Handler implements IMessageHandler<PacketRotateBlock, IMessage>
	{
		@Override
		public IMessage onMessage(PacketRotateBlock message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() ->
			{
				ItemStack wand = ItemWand.getHeldWand(player);
				if (!wand.isEmpty())
				{
					NBTTagCompound data = wand.getSubCompound(NBTKeys.SAVED_BLOCK);
					if (data != null)
					{
						IBitAccess bitAccess = ChiselsAndBitsAPI.api.createBitItem(new ItemStack(data));
						if (bitAccess != null)
						{
							bitAccess.rotate(Axis.Y, player.isSneaking() ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90);
							ChiselsAndBitsAPI.getBitsAsBlockStack(bitAccess).writeToNBT(data);
						}
					}
				}
			});
			return null;
		}
	}
}
