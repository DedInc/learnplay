package com.github.dedinc.learnplay.quilt;

import com.github.dedinc.learnplay.fabriclike.LearnPlayFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public final class LearnPlayQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        // Run the Fabric-like setup.
        LearnPlayFabricLike.init();
    }
}
