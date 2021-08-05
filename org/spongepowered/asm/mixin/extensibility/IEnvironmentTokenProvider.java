package org.spongepowered.asm.mixin.extensibility;

import org.spongepowered.asm.mixin.MixinEnvironment;

public interface IEnvironmentTokenProvider {

    int DEFAULT_PRIORITY = 1000;

    int getPriority();

    Integer getToken(String s, MixinEnvironment mixinenvironment);
}
