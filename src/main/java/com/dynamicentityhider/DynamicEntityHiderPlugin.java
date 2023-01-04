package com.dynamicentityhider;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@PluginDescriptor(
		name = "Dynamic Entity Hider",
		description = "Hides players when there are too many showing"
)
public class DynamicEntityHiderPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private DynamicEntityHiderConfig config;

	@Inject
	private Hooks hooks;

	private int maxPlayersShown;
	private long prevTime = System.currentTimeMillis();
	private List<Player> playersToShow = new ArrayList<>();

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	@Provides
	DynamicEntityHiderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DynamicEntityHiderConfig.class);
	}

	@Override
	protected void startUp()
	{
		updateConfig();

		hooks.registerRenderableDrawListener(drawListener);
	}

	@Override
	protected void shutDown()
	{
		hooks.unregisterRenderableDrawListener(drawListener);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals(DynamicEntityHiderConfig.GROUP))
		{
			updateConfig();
		}
	}

	private void updateConfig()
	{
		maxPlayersShown = config.maxPlayersShown();
	}

	@VisibleForTesting
	boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		Player local = client.getLocalPlayer();

		if (prevTime != System.currentTimeMillis())
		{
			prevTime = System.currentTimeMillis();
			playersToShow.clear();

			for (Player otherPlayer : client.getPlayers())
			{
				if (otherPlayer == local)
				{
					continue;
				}

				playersToShow.add(otherPlayer);

				if (playersToShow.size() >= maxPlayersShown)
				{
					break;
				}
			}
		}

		if (renderable instanceof Player)
		{
			Player player = (Player) renderable;

			if (player != local)
			{
				return playersToShow.contains(player);
			}
		}
		else if (renderable instanceof NPC)
		{
			NPC npc = (NPC) renderable;

			if (npc.getComposition().isFollower() && npc != client.getFollower() && npc.getInteracting() != null)
			{
				return playersToShow.contains(npc.getInteracting());
			}
		}

		return true;
	}
}
