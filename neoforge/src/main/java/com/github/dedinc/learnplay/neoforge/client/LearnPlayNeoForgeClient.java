package com.github.dedinc.learnplay.neoforge.client;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.client.LearnPlayPlatformClient;
import com.github.dedinc.learnplay.client.gui.NoCardsAvailableScreen;
import com.github.dedinc.learnplay.client.hud.StatsHudRenderer;
import com.github.dedinc.learnplay.data.model.Flashcard;
import com.github.dedinc.learnplay.data.model.SRSState;
import com.github.dedinc.learnplay.player.PlayerProgressManager;
import com.github.dedinc.learnplay.srs.ReviewScheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

/**
 * NeoForge client-side initialization.
 * Uses Yarn mappings (same as Fabric) since Architectury Loom uses Yarn for all platforms.
 */
@Mod.EventBusSubscriber(modid = LearnPlay.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LearnPlayNeoForgeClient {

    private static KeyBinding openReviewKey;
    private static KeyBinding openConfigKey;
    private static ReviewScheduler reviewScheduler;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Initialize review scheduler
        reviewScheduler = new ReviewScheduler();

        LearnPlay.LOGGER.info("LearnPlay NeoForge client initialized - Press 'I' to open review screen (SM-2 scheduled)");
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        openReviewKey = new KeyBinding(
                "key.learnplay.open_review",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.learnplay"
        );
        event.register(openReviewKey);

        openConfigKey = new KeyBinding(
                "key.learnplay.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.learnplay"
        );
        event.register(openConfigKey);
    }

    @Mod.EventBusSubscriber(modid = LearnPlay.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class NeoForgeClientEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                MinecraftClient client = MinecraftClient.getInstance();

                // Check review keybind
                while (openReviewKey.wasPressed()) {
                    if (client.player != null) {
                        openTestReviewScreen(client);
                    }
                }

                // Check config keybind
                while (openConfigKey.wasPressed()) {
                    client.setScreen(new com.github.dedinc.learnplay.client.gui.ConfigScreen(client.currentScreen));
                }

                // Run trigger handlers
                com.github.dedinc.learnplay.forgelike.trigger.DeathTriggerHandler.onClientTick();
                com.github.dedinc.learnplay.forgelike.trigger.TimerTriggerHandler.onClientTick();
            }
        }

        @SubscribeEvent
        public static void onLivingDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
            // Forward to forge-like handler
            com.github.dedinc.learnplay.forgelike.trigger.EntityKillTriggerHandler.onEntityKilled(
                    event.getEntity(),
                    event.getSource()
            );
        }

        @SubscribeEvent
        public static void onBlockBreak(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
            // Forward to forge-like handler
            if (event.getPlayer() != null) {
                com.github.dedinc.learnplay.forgelike.trigger.BlockBreakTriggerHandler.onBlockBroken(
                        event.getState(),
                        event.getPlayer()
                );
            }
        }

        @SubscribeEvent
        public static void onBlockPlace(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event) {
            // Forward to forge-like handler
            // Only track player placements
            if (event.getEntity() instanceof net.minecraft.entity.player.PlayerEntity player) {
                com.github.dedinc.learnplay.forgelike.trigger.BlockPlaceTriggerHandler.onBlockPlaced(
                        event.getPlacedBlock(),
                        player
                );
            }
        }

        @SubscribeEvent
        public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
            MinecraftClient client = MinecraftClient.getInstance();
            StatsHudRenderer.render(event.getGuiGraphics(), client);
        }

        @SubscribeEvent
        public static void onScreenInit(ScreenEvent.Init.Post event) {
            if (event.getScreen() instanceof TitleScreen) {
                // Add "LearnPlay Settings" button to main menu
                // Position in bottom-right corner to avoid overlapping with main buttons
                int buttonWidth = 150;
                int buttonHeight = 20;
                int x = event.getScreen().width - buttonWidth - 4; // 4px padding from right edge
                int y = event.getScreen().height - buttonHeight - 4; // 4px padding from bottom edge

                ButtonWidget settingsButton = ButtonWidget.builder(
                        Text.literal("LearnPlay Settings"),
                        button -> MinecraftClient.getInstance().setScreen(
                                new com.github.dedinc.learnplay.client.gui.ConfigScreen(event.getScreen())
                        )
                ).dimensions(x, y, buttonWidth, buttonHeight).build();

                event.addListener(settingsButton);
            }
        }

        @SubscribeEvent
        public static void onChatMessage(ClientChatReceivedEvent event) {
            // Handle chat messages (works for both single-player and multiplayer)
            // overlay = false for regular chat messages
            com.github.dedinc.learnplay.forgelike.trigger.ChatTriggerHandler.onChatMessage(
                    event.getMessage(),
                    false
            );
        }
    }

    /**
     * Open review screen with intelligently selected card using SM-2 scheduling.
     * Cards are selected in this priority order:
     * 1. Due cards (sorted by nextReview timestamp - weak cards first)
     * 2. New cards (never reviewed)
     * 3. If no cards available, show message
     */
    private static void openTestReviewScreen(MinecraftClient client) {
        try {
            String playerName = client.player.getName().getString();

            // Get next card using SM-2 scheduling
            Flashcard card = reviewScheduler.getNextCardForReview(playerName);

            if (card == null) {
                // No cards available - show info screen
                ReviewScheduler.ReviewStats stats = reviewScheduler.getReviewStats(playerName);
                LearnPlay.LOGGER.info("No cards available for review. All cards are scheduled for future review!");
                client.setScreen(new NoCardsAvailableScreen(stats));
                return;
            }

            // Get or create SRS state for this card
            PlayerProgressManager progressManager = PlayerProgressManager.getInstance();
            SRSState state = progressManager.getOrCreateCardState(playerName, card.getId());

            // Log review stats
            ReviewScheduler.ReviewStats stats = reviewScheduler.getReviewStats(playerName);
            LearnPlay.LOGGER.info("Opening review for card: {} | {}", card.getId(), stats);

            // Open review screen using platform-specific implementation
            LearnPlayPlatformClient.openReviewScreen(card, state, playerName);

        } catch (Exception e) {
            LearnPlay.LOGGER.error("Failed to open review screen", e);
        }
    }
}

