package org.spongepowered.asm.service;

public interface ILegacyClassTransformer extends ITransformer {

    String getName();

    boolean isDelegationExcluded();

    byte[] transformClassBytes(String s, String s1, byte[] abyte);
}
