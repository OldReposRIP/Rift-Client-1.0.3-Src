package org.spongepowered.asm.service;

import java.io.InputStream;
import java.util.Collection;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.util.ReEntranceLock;

public interface IMixinService {

    String getName();

    boolean isValid();

    void prepare();

    MixinEnvironment.Phase getInitialPhase();

    void init();

    void beginPhase();

    void checkEnv(Object object);

    ReEntranceLock getReEntranceLock();

    IClassProvider getClassProvider();

    IClassBytecodeProvider getBytecodeProvider();

    Collection getPlatformAgents();

    InputStream getResourceAsStream(String s);

    void registerInvalidClass(String s);

    boolean isClassLoaded(String s);

    String getClassRestrictions(String s);

    Collection getTransformers();

    String getSideName();
}
