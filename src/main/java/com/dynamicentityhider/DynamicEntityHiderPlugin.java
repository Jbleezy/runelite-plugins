package com.dynamicentityhider;

import com.dynamicentityhider.config.Mode;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.*;

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
	private RenderCallbackManager renderCallbackManager;

	private long prevTime = System.currentTimeMillis();
	private List<Player> prevPlayers = new ArrayList<>();

	@Provides
	DynamicEntityHiderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DynamicEntityHiderConfig.class);
	}

	@Override
	protected void startUp()
	{
		prevPlayers = new ArrayList<>(); // re-randomize players

		renderCallbackManager.register(drawListener);
	}

	@Override
	protected void shutDown()
	{
		renderCallbackManager.unregister(drawListener);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!Objects.equals(configChanged.getGroup(), DynamicEntityHiderConfig.GROUP))
		{
			return;
		}

		if (Objects.equals(configChanged.getKey(), "mode"))
		{
			if (Objects.equals(configChanged.getNewValue(), Mode.RANDOM.name()))
			{
				prevPlayers = new ArrayList<>(); // re-randomize players
			}
		}
	}

	private final RenderCallback drawListener = new RenderCallback()
	{
		@Override
		public boolean addEntity(Renderable renderable, boolean ui)
		{
			if (!client.isClientThread())
			{
				return true;
			}

			if (config.disableInWilderness() && client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) == 1)
			{
				return true;
			}

			Player local = client.getLocalPlayer();
			long time = System.currentTimeMillis();

			if (prevTime != time)
			{
				List<Player> currPlayers = new ArrayList<>(client.getTopLevelWorldView().players().stream()
						.map(Player.class::cast)
						.toList());

				currPlayers.remove(local);

				Mode mode = config.mode();

				if (mode == Mode.DISTANCE)
				{
					currPlayers.sort((a, b) -> {
						return client.getLocalPlayer().getLocalLocation().distanceTo(a.getLocalLocation()) - client.getLocalPlayer().getLocalLocation().distanceTo(b.getLocalLocation());
					});
				}
				else if (mode == Mode.RANDOM)
				{
					List<Player> newPlayers = new ArrayList<>(currPlayers);
					newPlayers.removeAll(prevPlayers);

					Collections.shuffle(newPlayers);

					currPlayers.retainAll(prevPlayers);
					currPlayers.addAll(newPlayers);
				}

				int maxPlayersShown = config.maxPlayersShown();

				if (maxPlayersShown < currPlayers.size()) {
					currPlayers.subList(maxPlayersShown, currPlayers.size()).clear();
				}

				prevTime = time;
				prevPlayers = currPlayers;
			}

			List<Player> players = prevPlayers;

			if (renderable instanceof Player)
			{
				Player player = (Player) renderable;

				if (player != local)
				{
					return players.contains(player);
				}
			}
			else if (renderable instanceof NPC)
			{
				NPC npc = (NPC) renderable;

				if (npc.getComposition().isFollower() && npc != client.getFollower())
				{
					Actor interacting = npc.getInteracting();

					if (interacting instanceof Player)
					{
						return players.contains((Player) interacting);
					}
				}
			}

			return true;
		}
	};
}
