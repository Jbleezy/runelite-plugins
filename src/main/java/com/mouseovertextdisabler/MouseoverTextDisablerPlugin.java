package com.mouseovertextdisabler;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "Mouseover Text Disabler",
		description = "Disables the mouseover text"
)
public class MouseoverTextDisablerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MouseoverTextDisablerConfig config;

	private boolean mouseoverTextDisabled = false;
	private boolean loginClickToPlayLoaded = false;

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invokeLater(() -> {
			if (client.getGameState() == GameState.LOGGED_IN && !mouseoverTextDisabled)
			{
				mouseoverTextDisabled = true;
				client.runScript(49, "::mouseovertext");
			}
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invokeLater(() -> {
			if (client.getGameState() == GameState.LOGGED_IN && mouseoverTextDisabled)
			{
				mouseoverTextDisabled = false;
				client.runScript(49, "::mouseovertext");
			}
		});
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN || gameStateChanged.getGameState() == GameState.HOPPING) {
			mouseoverTextDisabled = false;
		}
	}

	@Subscribe
	public void onGameTick(net.runelite.api.events.GameTick gameTick) {
		if (client.getGameState() == GameState.LOGGED_IN && !loginClickToPlayLoaded) {
			if (!mouseoverTextDisabled) {
				mouseoverTextDisabled = true;
				client.runScript(49, "::mouseovertext");
			}
		}
	}

	// doesn't work if client is on click to play screen
	@Subscribe
	public void onWidgetLoaded(net.runelite.api.events.WidgetLoaded widgetLoaded) {
		if (widgetLoaded.getGroupId() == WidgetID.LOGIN_CLICK_TO_PLAY_GROUP_ID) {
			loginClickToPlayLoaded = true;
		}
	}

	@Subscribe
	public void onWidgetClosed(net.runelite.api.events.WidgetClosed widgetClosed) {
		if (widgetClosed.getGroupId() == WidgetID.LOGIN_CLICK_TO_PLAY_GROUP_ID) {
			loginClickToPlayLoaded = false;
		}
	}

	@Provides
	MouseoverTextDisablerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MouseoverTextDisablerConfig.class);
	}
}
