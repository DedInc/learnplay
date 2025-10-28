package com.github.dedinc.learnplay.trigger;

import com.github.dedinc.learnplay.config.TriggerConfig;
import net.minecraft.client.MinecraftClient;

/**
 * Common timer trigger handler logic.
 */
public class TimerTriggerHandler {

    /**
     * Called every client tick to check if timer should trigger.
     */
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

