package org.spongepowered.asm.mixin.refmap;

public interface IReferenceMapper {

    boolean isDefault();

    String getResourceName();

    String getStatus();

    String getContext();

    void setContext(String s);

    String remap(String s, String s1);

    String remapWithContext(String s, String s1, String s2);
}
