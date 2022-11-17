package com.keybindmove;

import java.awt.event.KeyEvent;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ModifierlessKeybind;

@ConfigGroup("keybindmove")
public interface KeybindMoveConfig extends Config
{
    @ConfigItem(
            position = 1,
            keyName = "forward",
            name = "Forward",
            description = "The key which will move player forward."
    )
    default ModifierlessKeybind forward()
    {
        return new ModifierlessKeybind(KeyEvent.VK_W, 0);
    }

    @ConfigItem(
            position = 2,
            keyName = "back",
            name = "Back",
            description = "The key which will move player back."
    )
    default ModifierlessKeybind back()
    {
        return new ModifierlessKeybind(KeyEvent.VK_S, 0);
    }

    @ConfigItem(
            position = 3,
            keyName = "left",
            name = "Left",
            description = "The key which will move player left."
    )
    default ModifierlessKeybind left()
    {
        return new ModifierlessKeybind(KeyEvent.VK_A, 0);
    }

    @ConfigItem(
            position = 4,
            keyName = "right",
            name = "Right",
            description = "The key which will move player right."
    )
    default ModifierlessKeybind right()
    {
        return new ModifierlessKeybind(KeyEvent.VK_D, 0);
    }
}
