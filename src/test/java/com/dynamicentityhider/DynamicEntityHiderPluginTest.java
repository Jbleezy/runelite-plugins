package com.dynamicentityhider;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DynamicEntityHiderPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DynamicEntityHiderPlugin.class);
		RuneLite.main(args);
	}
}