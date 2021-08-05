package org.spongepowered.asm.mixin.transformer.ext;

import org.spongepowered.asm.mixin.MixinEnvironment;

public interface IExtension {

    boolean checkActive(MixinEnvironment mixinenvironment);

    void preApply(ITargetClassContext itargetclasscontext);

    void postApply(ITargetClassContext itargetclasscontext);

    void export(MixinEnvironment mixinenvironment, String s, boolean flag, byte[] abyte);
}
