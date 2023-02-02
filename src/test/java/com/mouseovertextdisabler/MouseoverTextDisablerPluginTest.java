package com.mouseovertextdisabler;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MouseoverTextDisablerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MouseoverTextDisablerPlugin.class);
		RuneLite.main(args);
	}
}