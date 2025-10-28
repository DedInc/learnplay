package com.github.dedinc.learnplay.forge;

import com.github.dedinc.learnplay.LearnPlay;
import com.github.dedinc.learnplay.forge.client.LearnPlayForgeClient;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(LearnPlay.MOD_ID)
public final class LearnPlayForge {
    public LearnPlayForge(FMLJavaModLoadingContext context) {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(LearnPlay.MOD_ID, context.getModEventBus());

        // Run our common setup.
        LearnPlay.init();

        // Initialize client-side code only on client
        if (FMLEnvironment.dist == Dist.CLIENT) {
            LearnPlayForgeClient.init();
        }
    }
}
