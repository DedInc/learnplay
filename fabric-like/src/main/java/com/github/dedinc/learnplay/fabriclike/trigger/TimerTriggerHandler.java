package com.github.dedinc.learnplay.fabriclike.trigger;

import com.github.dedinc.learnplay.LearnPlay;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Fabric-like timer trigger event registration.
 */
public class TimerTriggerHandler {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client ->
                com.github.dedinc.learnplay.trigger.TimerTriggerHandler.onClientTick()
        );
        LearnPlay.LOGGER.info("Registered timer trigger handler");
    }
}
