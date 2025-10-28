package com.github.dedinc.learnplay.fabriclike.trigger;

import com.github.dedinc.learnplay.LearnPlay;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

/**
 * Fabric-like chat trigger event registration.
 * Uses ClientReceiveMessageEvents which works for both single-player and multiplayer.
 */
public class ChatTriggerHandler {

    public static void register() {
        // Register for game messages (works in both single-player and multiplayer)
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            com.github.dedinc.learnplay.trigger.ChatTriggerHandler.onChatMessage(message, overlay);
        });

        LearnPlay.LOGGER.info("Registered chat trigger handler (single-player + multiplayer)");
    }
}

