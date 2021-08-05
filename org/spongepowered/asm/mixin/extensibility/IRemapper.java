package org.spongepowered.asm.mixin.extensibility;

public interface IRemapper {

    String mapMethodName(String s, String s1, String s2);

    String mapFieldName(String s, String s1, String s2);

    String map(String s);

    String unmap(String s);

    String mapDesc(String s);

    String unmapDesc(String s);
}
