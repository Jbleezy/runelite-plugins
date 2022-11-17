package com.keybindmove;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class KeybindMovePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(KeybindMovePlugin.class);
		RuneLite.main(args);
	}
}