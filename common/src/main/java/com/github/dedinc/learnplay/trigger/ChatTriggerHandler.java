package com.github.dedinc.learnplay.trigger;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.config.LearnPlayConfig;
import com.github.dedinc.learnplay.config.TriggerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Common chat trigger handler logic.
 * Checks if chat messages match configured patterns and triggers reviews.
 */
public class ChatTriggerHandler {

    /**
     * Called when a chat message is received (both single-player and multiplayer).
     *
     * @param message The chat message text
     * @param overlay Whether this is an overlay message (action bar, etc.)
     */
    public static void onChatMessage(Text message, boolean overlay) {
        // Ignore overlay messages (action bar, etc.)
        if (overlay) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Get the message as plain text
        String messageText = message.getString();

        // Check if message matches pattern
        if (matchesPattern(messageText)) {
            LearnPlay.LOGGER.info("[CHAT TRIGGER] Pattern matched in message: {}", messageText);

            ClientTriggerManager triggerManager = ClientTriggerManager.getInstance();
            triggerManager.attemptTrigger(TriggerConfig.TriggerType.CHAT);
        }
    }

    /**
     * Check if a message matches the configured chat trigger pattern.
     * Uses case-insensitive contains matching.
     *
     * @param message The message to check
     * @return true if the message matches the pattern
     */
    private static boolean matchesPattern(String message) {
        LearnPlayConfig config = LearnPlayConfig.getInstance();
        String pattern = config.triggers.chatTriggerPattern;

        if (pattern == null || pattern.isEmpty()) {
            return false;
        }

        // Case-insensitive contains check
        return message.toLowerCase().contains(pattern.toLowerCase());
    }
}

