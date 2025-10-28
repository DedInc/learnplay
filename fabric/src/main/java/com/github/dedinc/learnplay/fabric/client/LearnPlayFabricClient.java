package com.github.dedinc.learnplay.fabric.client;

import com.github.dedinc.learnplay.fabriclike.client.FabricLikeClientHelper;
import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric client-side initialization.
 * Delegates to shared FabricLikeClientHelper.
 */
public final class LearnPlayFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricLikeClientHelper.init();
    }
}
