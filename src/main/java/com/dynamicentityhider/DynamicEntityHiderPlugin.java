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
import java.util.stream.Collectors;

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
	private List<Player> playersToShow = new ArrayList<>();
	private List<Player> prevPlayersToShow = new ArrayList<>();

	@Provides
	DynamicEntityHiderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DynamicEntityHiderConfig.class);
	}

	@Override
	protected void startUp()
	{
		playersToShow = new ArrayList<>(); // re-randomize players

		renderCallbackManager.register(drawListener);
	}

	@Override
	protected void shutDown()
	{
		renderCallbackManager.unregister(drawListener);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals(DynamicEntityHiderConfig.GROUP))
		{
			if (Objects.equals(e.getNewValue(), Mode.RANDOM.toString()))
			{
				playersToShow = new ArrayList<>(); // re-randomize players
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

			if (prevTime != System.currentTimeMillis())
			{
				prevTime = System.currentTimeMillis();
				prevPlayersToShow = new ArrayList<>(playersToShow);

				playersToShow = client.getTopLevelWorldView().players().stream()
						.map(p -> (Player) p)
						.collect(Collectors.toCollection(ArrayList::new));

				playersToShow.remove(local);

				if (config.mode().equals(Mode.DISTANCE))
				{
					playersToShow.sort(new SortByDistance());
				}
				else if (config.mode().equals(Mode.RANDOM))
				{
					List<Player> retainPlayersToShow = new ArrayList<>(playersToShow);
					retainPlayersToShow.retainAll(prevPlayersToShow);

					List<Player> newPlayersToShow = new ArrayList<>(playersToShow);
					newPlayersToShow.removeAll(retainPlayersToShow);
					Collections.shuffle(newPlayersToShow);

					playersToShow = new ArrayList<>(retainPlayersToShow);
					playersToShow.addAll(newPlayersToShow);
				}

				if (config.maxPlayersShown() < playersToShow.size())
				{
					playersToShow = playersToShow.subList(0, config.maxPlayersShown());
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

				if (npc.getComposition().isFollower() && npc != client.getFollower())
				{
					Actor interacting = npc.getInteracting();

					if (interacting instanceof Player)
					{
						return playersToShow.contains((Player) interacting);
					}
				}
			}

			return true;
		}
	};

	class SortByDistance implements Comparator<Player>
	{
		public int compare(Player a, Player b)
		{
			return client.getLocalPlayer().getLocalLocation().distanceTo(a.getLocalLocation()) - client.getLocalPlayer().getLocalLocation().distanceTo(b.getLocalLocation());
		}
	}
}
