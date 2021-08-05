package org.spongepowered.asm.obfuscation.mapping;

public interface IMapping {

    IMapping.Type getType();

    Object move(String s);

    Object remap(String s);

    Object transform(String s);

    Object copy();

    String getName();

    String getSimpleName();

    String getOwner();

    String getDesc();

    Object getSuper();

    String serialise();

    public static enum Type {

        FIELD, METHOD, CLASS, PACKAGE;
    }
}
