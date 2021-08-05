package org.spongepowered.asm.mixin.transformer.ext;

public interface IHotSwap {

    void registerMixinClass(String s);

    void registerTargetClass(String s, byte[] abyte);
}
