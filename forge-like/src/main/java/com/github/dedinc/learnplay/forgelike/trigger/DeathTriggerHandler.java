package com.github.dedinc.learnplay.forgelike.trigger;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.config.TriggerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Forge-like death trigger handler using state machine to detect death/respawn pattern.
 */
public class DeathTriggerHandler {

    private enum State {
        IDLE,
        SAW_LOW,
        AWAIT_RESPAWN
    }

    private static State currentState = State.IDLE;
    private static int tickCounter = 0;
    private static int lowHealthTick = 0;
    private static Vec3d lowHealthPosition = null;

    private static final int CHECK_INTERVAL = 2;
    private static final float LOW_HEALTH_RATIO = 0.5f;
    private static final float FULL_HEALTH_RATIO = 0.95f;
    private static final int TIMEOUT_TICKS = 100;
    private static final double MIN_DISTANCE_SQUARED = 2.0;

    public static void onClientTick() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) {
            resetState();
            return;
        }

        tickCounter++;

        if (tickCounter >= CHECK_INTERVAL) {
            float currentHealth = client.player.getHealth();
            float maxHealth = client.player.getMaxHealth();
            Vec3d currentPosition = client.player.getPos();

            switch (currentState) {
                case IDLE:
                    handleIdleState(currentHealth, maxHealth, currentPosition);
                    break;
                case SAW_LOW:
                    handleSawLowState(currentHealth, maxHealth, currentPosition);
                    break;
                case AWAIT_RESPAWN:
                    handleAwaitRespawnState(currentHealth, maxHealth, currentPosition);
                    break;
            }

            tickCounter = 0;
        }
    }

    private static void handleIdleState(float health, float maxHealth, Vec3d position) {
        float healthRatio = health / maxHealth;

        if (healthRatio <= LOW_HEALTH_RATIO) {
            currentState = State.SAW_LOW;
            lowHealthTick = 0;
            lowHealthPosition = position;

            LearnPlay.LOGGER.debug("[DEATH TRIGGER] Low health detected: {}/{} HP ({:.1f}%)",
                    health, maxHealth, healthRatio * 100);
        }
    }

    private static void handleSawLowState(float health, float maxHealth, Vec3d position) {
        lowHealthTick++;
        float healthRatio = health / maxHealth;

        if (lowHealthTick > TIMEOUT_TICKS) {
            LearnPlay.LOGGER.debug("[DEATH TRIGGER] Timeout - no respawn detected, returning to IDLE");
            currentState = State.IDLE;
            return;
        }

        if (healthRatio >= FULL_HEALTH_RATIO) {
            currentState = State.AWAIT_RESPAWN;

            LearnPlay.LOGGER.debug("[DEATH TRIGGER] Full health detected: {}/{} HP ({:.1f}%), checking position...",
                    health, maxHealth, healthRatio * 100);
        }
    }

    private static void handleAwaitRespawnState(float health, float maxHealth, Vec3d position) {
        if (lowHealthPosition == null) {
            currentState = State.IDLE;
            return;
        }

        double distanceSquared = position.squaredDistanceTo(lowHealthPosition);

        if (distanceSquared >= MIN_DISTANCE_SQUARED) {
            double distance = Math.sqrt(distanceSquared);

            LearnPlay.LOGGER.info("[DEATH TRIGGER] ✅ Death detected! Health restored to {}/{} HP, moved {:.2f} blocks",
                    health, maxHealth, distance);

            onPlayerDeath();
            currentState = State.IDLE;
        } else {
            LearnPlay.LOGGER.debug("[DEATH TRIGGER] Full health but no position change ({:.2f} blocks) - likely healing, not death",
                    Math.sqrt(distanceSquared));

            currentState = State.IDLE;
        }
    }

    private static void resetState() {
        currentState = State.IDLE;
        tickCounter = 0;
        lowHealthTick = 0;
        lowHealthPosition = null;
    }

    private static void onPlayerDeath() {
        ClientTriggerManager triggerManager = ClientTriggerManager.getInstance();
        triggerManager.attemptTrigger(TriggerConfig.TriggerType.DEATH);
    }
}
