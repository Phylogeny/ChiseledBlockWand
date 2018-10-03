package phylogeny.chiseledblockwand.example.init;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import phylogeny.chiseledblockwand.example.ChiseledBlockWand;
import phylogeny.chiseledblockwand.example.ItemWand;

@EventBusSubscriber
public class ItemsMod
{
	@ObjectHolder(ChiseledBlockWand.MOD_ID + ":wand")
	public static ItemWand wand;

	@SubscribeEvent
	public static void registerItems(Register<Item> event)
	{
		event.getRegistry().register(new ItemWand());
	}
}