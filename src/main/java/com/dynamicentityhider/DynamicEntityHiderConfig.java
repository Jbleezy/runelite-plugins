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
			keyName = "showFriends",
			name = "Show friends",
			description = "Always show friends"
	)
	default boolean showFriends() { return false; }

	@ConfigItem(
			position = 5,
			keyName = "showFriendsChatMembers",
			name = "Show friends chat members",
			description = "Always show friends chat members"
	)
	default boolean showFriendsChatMembers() { return false; }

	@ConfigItem(
			position = 6,
			keyName = "showClanChatMembers",
			name = "Show clan chat members",
			description = "Always show clan chat members"
	)
	default boolean showClanChatMembers() { return false; }

	@ConfigItem(
			position = 7,
			keyName = "showPartyMembers",
			name = "Show party members",
			description = "Always show party members"
	)
	default boolean showPartyMembers() { return false; }

	@ConfigItem(
			position = 8,
			keyName = "hideIgnores",
			name = "Hide ignores",
			description = "Always hide ignored players"
	)
	default boolean hideIgnores() { return false; }

	@ConfigItem(
			position = 9,
			keyName = "disableInWilderness",
			name = "Disable in Wilderness",
			description = "Disable hiding other players in the Wilderness"
	)
	default boolean disableInWilderness() { return false; }
}
