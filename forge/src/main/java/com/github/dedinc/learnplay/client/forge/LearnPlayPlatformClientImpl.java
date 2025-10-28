package com.github.dedinc.learnplay.client.forge;

import com.github.dedinc.learnplay.client.gui.ReviewScreen;
import com.github.dedinc.learnplay.data.model.Flashcard;
import com.github.dedinc.learnplay.data.model.SRSState;
import net.minecraft.client.MinecraftClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Forge implementation of platform-specific client methods.
 */
@OnlyIn(Dist.CLIENT)
public class LearnPlayPlatformClientImpl {

    public static Object createReviewScreen(Flashcard card, SRSState state, String playerName) {
        return new ReviewScreen(card, state, playerName);
    }

    public static void openReviewScreen(Flashcard card, SRSState state, String playerName) {
        MinecraftClient.getInstance().setScreen(new ReviewScreen(card, state, playerName));
    }
}

