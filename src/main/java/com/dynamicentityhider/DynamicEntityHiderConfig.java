package com.dynamicentityhider;

import com.dynamicentityhider.config.Mode;
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
			keyName = "maxPlayersShown",
			name = "Max Players Shown",
			description = "Maximum amount of other players to show"
	)
	default int maxPlayersShown()
	{
		return 100;
	}

	@ConfigItem(
			position = 2,
			keyName = "mode",
			name = "Mode",
			description = "Determines how to hide other players"
	)
	default Mode mode()
	{
		return Mode.RANDOM;
	}

	@ConfigItem(
			position = 3,
			keyName = "disableInWilderness",
			name = "Disable In Wilderness",
			description = "Disables hiding other players in the Wilderness"
	)
	default boolean disableInWilderness() { return false; }
}
