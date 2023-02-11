package com.dynamicentityhider;

import com.dynamicentityhider.config.Mode;
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
import java.util.Collections;
import java.util.Comparator;
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
	private Mode mode;
	private long prevTime = System.currentTimeMillis();
	private List<Player> playersToShow = new ArrayList<>();
	private List<Player> prevPlayersToShow = new ArrayList<>();

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

		playersToShow = new ArrayList<>(); // re-randomize players

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

			if (e.getNewValue().equals(Mode.RANDOM.toString()))
			{
				playersToShow = new ArrayList<>(); // re-randomize players
			}
		}
	}

	private void updateConfig()
	{
		maxPlayersShown = config.maxPlayersShown();
		mode = config.mode();
	}

	@VisibleForTesting
	boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		Player local = client.getLocalPlayer();

		if (prevTime != System.currentTimeMillis())
		{
			prevTime = System.currentTimeMillis();
			prevPlayersToShow = new ArrayList<>(playersToShow);

			playersToShow = client.getPlayers();
			playersToShow.remove(local);

			if (mode.equals(Mode.DISTANCE))
			{
				playersToShow.sort(new SortByDistance());
			}
			else if (mode.equals(Mode.RANDOM))
			{
				List<Player> retainPlayersToShow = new ArrayList<>(playersToShow);
				retainPlayersToShow.retainAll(prevPlayersToShow);

				List<Player> newPlayersToShow = new ArrayList<>(playersToShow);
				newPlayersToShow.removeAll(retainPlayersToShow);
				Collections.shuffle(newPlayersToShow);

				playersToShow = new ArrayList<>(retainPlayersToShow);
				playersToShow.addAll(newPlayersToShow);
			}

			playersToShow = playersToShow.subList(0, maxPlayersShown);
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

			if (npc.getComposition().isFollower() && npc != client.getFollower())
			{
				return playersToShow.contains(npc.getInteracting());
			}
		}

		return true;
	}

	class SortByDistance implements Comparator<Player>
	{
		public int compare(Player a, Player b)
		{
			return client.getLocalPlayer().getLocalLocation().distanceTo(a.getLocalLocation()) - client.getLocalPlayer().getLocalLocation().distanceTo(b.getLocalLocation());
		}
	}
}
