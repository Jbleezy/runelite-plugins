package com.dynamicentityhider;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(DynamicEntityHiderConfig.GROUP)
public interface DynamicEntityHiderConfig extends Config
{
	String GROUP = "dynamicentityhider";

	@Range(
			min = 0,
			max = 2000
	)
	@ConfigItem(
			position = 1,
			keyName = "maxPlayers",
			name = "Max players",
			description = "Maximum amount of other players to show"
	)
	default int maxPlayers()
	{
		return 100;
	}

	@Range(
			min = 0,
			max = 15
	)
	@ConfigItem(
			position = 2,
			keyName = "minDistance",
			name = "Min distance",
			description = "Minimum tile distance to show other players"
	)
	default int minDistance()
	{
		return 0;
	}

	@Range(
			min = 0,
			max = 15
	)
	@ConfigItem(
			position = 3,
			keyName = "maxDistance",
			name = "Max distance",
			description = "Maximum tile distance to show other players"
	)
	default int maxDistance()
	{
		return 15;
	}

	@ConfigItem(
			position = 4,
			keyName = "disableInWilderness",
			name = "Disable in Wilderness",
			description = "Disable hiding other players in the Wilderness"
	)
	default boolean disableInWilderness() { return false; }
}
