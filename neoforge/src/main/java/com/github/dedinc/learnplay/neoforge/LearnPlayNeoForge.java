package com.github.dedinc.learnplay.neoforge;

import com.github.dedinc.learnplay.LearnPlay;
import net.neoforged.fml.common.Mod;

@Mod(LearnPlay.MOD_ID)
public final class LearnPlayNeoForge {
    public LearnPlayNeoForge() {
        // Run our common setup.
        LearnPlay.init();
    }
}
