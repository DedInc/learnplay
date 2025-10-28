package com.github.dedinc.learnplay.fabriclike.trigger;

import com.github.dedinc.learnplay.LearnPlay;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Fabric-like death trigger event registration.
 */
public class DeathTriggerHandler {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client ->
                com.github.dedinc.learnplay.trigger.DeathTriggerHandler.onClientTick()
        );

        LearnPlay.LOGGER.info("Registered death trigger handler");
    }
}
