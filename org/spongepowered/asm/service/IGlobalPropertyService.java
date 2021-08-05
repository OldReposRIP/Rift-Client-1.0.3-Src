package org.spongepowered.asm.service;

public interface IGlobalPropertyService {

    Object getProperty(String s);

    void setProperty(String s, Object object);

    Object getProperty(String s, Object object);

    String getPropertyString(String s, String s1);
}
