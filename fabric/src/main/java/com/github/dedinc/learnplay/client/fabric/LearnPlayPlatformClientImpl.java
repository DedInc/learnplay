package com.github.dedinc.learnplay.client.fabric;

import com.github.dedinc.learnplay.data.model.Flashcard;
import com.github.dedinc.learnplay.data.model.SRSState;
import com.github.dedinc.learnplay.fabriclike.client.FabricLikeClientHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Fabric implementation of platform-specific client methods.
 * Delegates to shared FabricLikeClientHelper.
 */
@Environment(EnvType.CLIENT)
public class LearnPlayPlatformClientImpl {

    public static Object createReviewScreen(Flashcard card, SRSState state, String playerName) {
        return FabricLikeClientHelper.createReviewScreen(card, state, playerName);
    }

    public static void openReviewScreen(Flashcard card, SRSState state, String playerName) {
        FabricLikeClientHelper.openReviewScreenWithCard(card, state, playerName);
    }
}
