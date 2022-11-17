package com.keybindmove;

import com.google.common.base.Strings;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.VarClientStr;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.input.KeyListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class KeybindMoveListener implements KeyListener
{
    @Inject
    private KeybindMovePlugin plugin;

    @Inject
    private KeybindMoveConfig config;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    private final Map<Integer, Integer> modified = new HashMap<>();
    private final Set<Character> blockedChars = new HashSet<>();

    @Override
    public void keyTyped(KeyEvent e)
    {
        char keyChar = e.getKeyChar();
        if (keyChar != KeyEvent.CHAR_UNDEFINED && blockedChars.contains(keyChar) && plugin.chatboxFocused())
        {
            e.consume();
        }
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (!plugin.chatboxFocused())
        {
            return;
        }

        if (!plugin.isTyping())
        {
            if (config.forward().matches(e))
            {
                log.info("Forward!");
            }
            else if (config.back().matches(e))
            {
                log.info("Back!");
            }
            else if (config.left().matches(e))
            {
                log.info("Left!");
            }
            else if (config.right().matches(e))
            {
                log.info("Right!");
            }

            switch (e.getKeyCode())
            {
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_SLASH:
                case KeyEvent.VK_COLON:
                    // refocus chatbox
                    plugin.setTyping(true);
                    clientThread.invoke(plugin::unlockChat);
                    break;
            }

        }
        else
        {
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_ESCAPE:
                    // When exiting typing mode, block the escape key
                    // so that it doesn't trigger the in-game hotkeys
                    e.consume();
                    plugin.setTyping(false);
                    clientThread.invoke(() ->
                    {
                        client.setVarcStrValue(VarClientStr.CHATBOX_TYPED_TEXT, "");
                        plugin.lockChat();
                    });
                    break;
                case KeyEvent.VK_ENTER:
                    plugin.setTyping(false);
                    clientThread.invoke(plugin::lockChat);
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    // Only lock chat on backspace when the typed text is now empty
                    if (Strings.isNullOrEmpty(client.getVarcStrValue(VarClientStr.CHATBOX_TYPED_TEXT)))
                    {
                        plugin.setTyping(false);
                        clientThread.invoke(plugin::lockChat);
                    }
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        final int keyCode = e.getKeyCode();
        final char keyChar = e.getKeyChar();

        if (keyChar != KeyEvent.CHAR_UNDEFINED)
        {
            blockedChars.remove(keyChar);
        }

        final Integer mappedKeyCode = modified.remove(keyCode);
        if (mappedKeyCode != null)
        {
            e.setKeyCode(mappedKeyCode);
            e.setKeyChar(KeyEvent.CHAR_UNDEFINED);
        }
    }
}