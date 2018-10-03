package phylogeny.chiseledblockwand.extra;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import phylogeny.chiseledblockwand.example.ChiseledBlockWand;
import phylogeny.chiseledblockwand.example.init.ItemsMod;

public class CreativeTabMod
{
	public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(ChiseledBlockWand.MOD_ID)
	{
		@Override
		@SideOnly(Side.CLIENT)
		public ItemStack getTabIconItem()
		{
			return new ItemStack(ItemsMod.wand);
		}
	};
}