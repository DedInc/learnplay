package com.github.dedinc.learnplay.forgelike.trigger;

import com.github.dedinc.learnplay.config.TriggerConfig;
import net.minecraft.client.MinecraftClient;

/**
 * Forge-like timer trigger handler.
 */
public class TimerTriggerHandler {

    public static void onClientTick() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            return;
        }

        ClientTriggerManager triggerManager = ClientTriggerManager.getInstance();

        if (triggerManager.shouldTimerTrigger(client.player.getName().getString())) {
            triggerManager.attemptTrigger(TriggerConfig.TriggerType.TIMER);
        }
    }
}
