package phylogeny.chiseledblockwand.example.init;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import phylogeny.chiseledblockwand.example.ChiseledBlockWand;
import phylogeny.chiseledblockwand.example.ClientHelper;
import phylogeny.chiseledblockwand.example.ItemWand;

public enum KeyBindingsMod
{
	ROTATE(Keyboard.KEY_R, new KeyConflictContextWand());

	private KeyBinding keyBinding;
	private int keyCode;
	private KeyModifier keyModifier;
	private IKeyConflictContext conflictContext;

	private KeyBindingsMod(int key, IKeyConflictContext conflictContext)
	{
		this(KeyModifier.NONE, key, conflictContext);
	}

	private KeyBindingsMod(KeyModifier keyModifier, int keyCode, IKeyConflictContext conflictContext)
	{
		this.keyCode = keyCode;
		this.keyModifier = keyModifier;
		this.conflictContext = conflictContext;
	}

	public boolean isKeyDown()
	{
		return keyBinding.isKeyDown();
	}

	public static void registerKeyBindings()
	{
		for (KeyBindingsMod keyBinding : KeyBindingsMod.values())
			keyBinding.register();
	}

	public void register()
	{
		keyBinding = new KeyBinding("keybinding." + ChiseledBlockWand.MOD_ID + "." + name().toLowerCase(), conflictContext, keyModifier, keyCode, "itemGroup." + ChiseledBlockWand.MOD_ID);
		ClientRegistry.registerKeyBinding(keyBinding);
	}

	private static class KeyConflictContextWand implements IKeyConflictContext
	{
		@Override
		public boolean isActive()
		{
			if (!KeyConflictContext.GUI.isActive() && ClientHelper.getPlayer() != null)
				return !ItemWand.getHeldWand(ClientHelper.getPlayer()).isEmpty();

			return false;
		}

		@Override
		public boolean conflicts(IKeyConflictContext other)
		{
			return other == this || other == KeyConflictContext.IN_GAME;
		}
	}
}