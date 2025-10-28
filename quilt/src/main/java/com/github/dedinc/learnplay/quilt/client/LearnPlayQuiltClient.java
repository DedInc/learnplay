package com.github.dedinc.learnplay.quilt.client;

import com.github.dedinc.learnplay.fabriclike.client.FabricLikeClientHelper;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

/**
 * Quilt client-side initialization.
 * Delegates to shared FabricLikeClientHelper since Quilt is Fabric-compatible.
 */
public final class LearnPlayQuiltClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(ModContainer mod) {
        // Run the Fabric-like client setup
        FabricLikeClientHelper.init();
    }
}

