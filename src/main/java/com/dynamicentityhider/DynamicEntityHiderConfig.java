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
			min = 1,
			max = 2000
	)
	@ConfigItem(
			keyName = "maxPlayersShown",
			name = "Max Players Shown",
			description = "Maximum amount of other players to show"
	)
	default int maxPlayersShown()
	{
		return 100;
	}

	@ConfigItem(
			keyName = "mode",
			name = "Mode",
			description = "Mode to hide players: default (by PID) or by distance"
	)
	default Mode mode()
	{
		return Mode.DEFAULT;
	}
}
