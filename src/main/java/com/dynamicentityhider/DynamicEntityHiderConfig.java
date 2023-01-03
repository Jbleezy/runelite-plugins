package com.dynamicentityhider;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("example")
public interface DynamicEntityHiderConfig extends Config
{
	@Range(
			min = 1,
			max = 2000
	)
	@ConfigItem(
			keyName = "showOthersAmount",
			name = "Show Others Amount",
			description = "Maximum amount of other players to show"
	)
	default int showOthersAmount()
	{
		return 100;
	}
}
