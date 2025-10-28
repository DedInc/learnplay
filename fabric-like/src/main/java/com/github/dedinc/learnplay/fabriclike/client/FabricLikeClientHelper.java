package com.github.dedinc.learnplay.fabriclike.client;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.client.LearnPlayPlatformClient;
import com.github.dedinc.learnplay.client.gui.NoCardsAvailableScreen;
import com.github.dedinc.learnplay.client.hud.StatsHudRenderer;
import com.github.dedinc.learnplay.data.model.Flashcard;
import com.github.dedinc.learnplay.data.model.SRSState;
import com.github.dedinc.learnplay.fabriclike.trigger.DeathTriggerHandler;
import com.github.dedinc.learnplay.fabriclike.trigger.TimerTriggerHandler;
import com.github.dedinc.learnplay.player.PlayerProgressManager;
import com.github.dedinc.learnplay.srs.ReviewScheduler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Shared client helper for Fabric-like loaders (Fabric, Quilt).
 * Handles keybinding registration and tick events.
 */
public class FabricLikeClientHelper {

    private static KeyBinding openReviewKey;
    private static KeyBinding openConfigKey;
    private static ReviewScheduler reviewScheduler;

    /**
     * Initialize the client-side code for Fabric-like loaders.
     * Call this from your loader's ClientModInitializer.
     */
    public static void init() {
        // Initialize review scheduler
        reviewScheduler = new ReviewScheduler();

        // Register keybindings
        openReviewKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.learnplay.open_review",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.learnplay"
        ));

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.learnplay.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.learnplay"
        ));

        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Review screen keybind
            while (openReviewKey.wasPressed()) {
                if (client.player != null) {
                    openReviewScreen(client);
                }
            }

            // Config screen keybind
            while (openConfigKey.wasPressed()) {
                client.setScreen(new com.github.dedinc.learnplay.client.gui.ConfigScreen(client.currentScreen));
            }
        });

        // Register trigger handlers
        DeathTriggerHandler.register();
        TimerTriggerHandler.register();
        com.github.dedinc.learnplay.fabriclike.trigger.ChatTriggerHandler.register();
        com.github.dedinc.learnplay.fabriclike.trigger.EntityKillTriggerHandler.register();
        com.github.dedinc.learnplay.fabriclike.trigger.BlockBreakTriggerHandler.register();
        com.github.dedinc.learnplay.fabriclike.trigger.BlockPlaceTriggerHandler.register();

        // Register HUD renderer
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            StatsHudRenderer.render(context, client);
        });

        // Register main menu button
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                // Add "LearnPlay Settings" button to main menu
                // Position in bottom-right corner to avoid overlapping with main buttons
                int buttonWidth = 150;
                int buttonHeight = 20;
                int x = screen.width - buttonWidth - 4; // 4px padding from right edge
                int y = screen.height - buttonHeight - 4; // 4px padding from bottom edge

                ButtonWidget settingsButton = ButtonWidget.builder(
                        Text.literal("LearnPlay Settings"),
                        button -> client.setScreen(new com.github.dedinc.learnplay.client.gui.ConfigScreen(screen))
                ).dimensions(x, y, buttonWidth, buttonHeight).build();

                // Use reflection to add button since addDrawableChild is protected
                try {
                    java.lang.reflect.Method addDrawableChild = Screen.class.getDeclaredMethod("addDrawableChild", net.minecraft.client.gui.Element.class);
                    addDrawableChild.setAccessible(true);
                    addDrawableChild.invoke(screen, settingsButton);
                } catch (Exception e) {
                    LearnPlay.LOGGER.error("Failed to add LearnPlay Settings button to main menu", e);
                }
            }
        });

        LearnPlay.LOGGER.info("LearnPlay client initialized - Press 'I' to open review screen (SM-2 scheduled)");
        LearnPlay.LOGGER.info("Trigger system active - Death, Timer, and Chat triggers registered");
        LearnPlay.LOGGER.info("HUD stats renderer registered");
        LearnPlay.LOGGER.info("Main menu button registered");
    }

    /**
     * Open review screen with intelligently selected card using SM-2 scheduling.
     * Cards are selected in this priority order:
     * 1. Due cards (sorted by nextReview timestamp - weak cards first)
     * 2. New cards (never reviewed)
     * 3. If no cards available, show message
     */
    private static void openReviewScreen(MinecraftClient client) {
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

    /**
     * Create a review screen instance.
     * Used by platform-specific implementations.
     */
    public static com.github.dedinc.learnplay.client.gui.ReviewScreen createReviewScreen(Flashcard card, SRSState state, String playerName) {
        return new com.github.dedinc.learnplay.client.gui.ReviewScreen(card, state, playerName);
    }

    /**
     * Open a review screen with specified card.
     * Used by platform-specific implementations.
     */
    public static void openReviewScreenWithCard(Flashcard card, SRSState state, String playerName) {
        MinecraftClient.getInstance().setScreen(new com.github.dedinc.learnplay.client.gui.ReviewScreen(card, state, playerName));
    }
}

