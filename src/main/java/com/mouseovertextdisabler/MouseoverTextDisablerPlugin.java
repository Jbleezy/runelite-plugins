package com.mouseovertextdisabler;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.PostClientTick;
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

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invokeLater(() -> {
			disableMouseoverText(true);
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invokeLater(() -> {
			disableMouseoverText(false);
		});
	}

	@Subscribe
	public void onPostClientTick(PostClientTick postClientTick)
	{
		disableMouseoverText(true);
	}

	public void disableMouseoverText(boolean disable) {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}

		if (disable) {
			if (client.isMouseoverTextEnabled()) {
				client.setMouseoverTextEnabled(false);
			}
		}
		else {
			if (!client.isMouseoverTextEnabled()) {
				client.setMouseoverTextEnabled(true);
			}
		}
	}

	@Provides
	MouseoverTextDisablerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MouseoverTextDisablerConfig.class);
	}
}