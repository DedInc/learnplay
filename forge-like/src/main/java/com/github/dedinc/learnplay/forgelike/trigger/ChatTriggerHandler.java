package com.github.dedinc.learnplay.forgelike.trigger;

import net.minecraft.text.Text;

/**
 * Forge-like chat trigger handler.
 * This is a common handler that will be called by platform-specific event listeners.
 */
public class ChatTriggerHandler {

    /**
     * Called when a chat message is received.
     * This method should be called from platform-specific event handlers.
     *
     * @param message The chat message
     * @param overlay Whether this is an overlay message
     */
    public static void onChatMessage(Text message, boolean overlay) {
        com.github.dedinc.learnplay.trigger.ChatTriggerHandler.onChatMessage(message, overlay);
    }
}

