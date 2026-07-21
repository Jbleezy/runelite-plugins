package com.dynamicentityhider;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.party.PartyService;
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
	private PartyService partyService;

	@Inject
	private RenderCallbackManager renderCallbackManager;

	@Inject
	private DynamicEntityHiderConfig config;

	private long prevTime = System.currentTimeMillis();
	private List<Player> prevPlayers = new ArrayList<>();
	private List<Player> prevPlayersAlwaysShown = new ArrayList<>();
	private List<Player> prevPlayersAlwaysHidden = new ArrayList<>();

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
				List<WorldView> worldViews = new ArrayList<>();
				WorldView topLevel = client.getTopLevelWorldView();

				worldViews.add(topLevel);

				for (WorldView worldView : topLevel.worldViews())
				{
					worldViews.add(worldView);
				}

				List<Player> currPlayers = new ArrayList<>();
				List<Player> currPlayersAlwaysShown = new ArrayList<>();
				List<Player> currPlayersAlwaysHidden = new ArrayList<>();

				for (WorldView worldView : worldViews)
				{
					for (Player player : worldView.players())
					{
						if (config.hideIgnores() && client.getIgnoreContainer().findByName(player.getName()) != null)
						{
							currPlayersAlwaysHidden.add(player);
						}
						else if (player == local ||
								player.getInteracting() == local ||
								(config.showFriends() && player.isFriend()) ||
								(config.showFriendsChatMembers() && player.isFriendsChatMember()) ||
								(config.showClanChatMembers() && player.isClanMember()) ||
								(config.showPartyMembers() && partyService.isInParty() && partyService.getMemberByDisplayName(player.getName()) != null))
						{
							currPlayersAlwaysShown.add(player);
						}
						else
						{
							currPlayers.add(player);
						}
					}
				}

				int minDistance = config.minDistance();
				int maxDistance = config.maxDistance();
				WorldPoint clientWorldPoint = local.getWorldLocation();

				currPlayers.removeIf(player -> {
					WorldPoint playerWorldPoint = player.getWorldLocation();
					int distance = clientWorldPoint.distanceTo(playerWorldPoint);

					return distance < minDistance || distance > maxDistance;
				});

				List<Player> newPlayers = new ArrayList<>(currPlayers);
				newPlayers.removeAll(prevPlayers);

				Collections.shuffle(newPlayers);

				currPlayers.retainAll(prevPlayers);
				currPlayers.addAll(newPlayers);

				int maxPlayers = config.maxPlayers();

				if (maxPlayers < currPlayers.size()) {
					currPlayers.subList(maxPlayers, currPlayers.size()).clear();
				}

				prevTime = time;
				prevPlayers = currPlayers;
				prevPlayersAlwaysShown = currPlayersAlwaysShown;
				prevPlayersAlwaysHidden = currPlayersAlwaysHidden;
			}

			List<Player> players = new ArrayList<>(prevPlayers);
			players.addAll(prevPlayersAlwaysShown);
			players.removeAll(prevPlayersAlwaysHidden);

			if (renderable instanceof Player)
			{
				Player player = (Player) renderable;

				return players.contains(player);
			}
			else if (renderable instanceof NPC)
			{
				NPC npc = (NPC) renderable;

				if (npc.getComposition().isFollower() && npc != client.getFollower())
				{
					Actor interacting = npc.getInteracting();

					if (interacting instanceof Player)
					{
						Player player = (Player) interacting;

						return players.contains(player);
					}
				}
			}
			else if (renderable instanceof Scene)
			{
				Scene scene = (Scene) renderable;
				WorldEntity worldEntity = client.getTopLevelWorldView().worldEntities().byIndex(scene.getWorldViewId());

				if (worldEntity.getOwnerType() == WorldEntity.OWNER_TYPE_OTHER_PLAYER)
				{
					WorldView worldView = worldEntity.getWorldView();

					for (Player player : worldView.players())
					{
						if (!players.contains(player))
						{
							return false;
						}
					}
				}
			}

			return true;
		}
	};
}
