package javassist.scopedpool;

import java.util.Map;
import javassist.ClassPool;

public interface ScopedClassPoolRepository {

    void setClassPoolFactory(ScopedClassPoolFactory scopedclasspoolfactory);

    ScopedClassPoolFactory getClassPoolFactory();

    boolean isPrune();

    void setPrune(boolean flag);

    ScopedClassPool createScopedClassPool(ClassLoader classloader, ClassPool classpool);

    ClassPool findClassPool(ClassLoader classloader);

    ClassPool registerClassLoader(ClassLoader classloader);

    Map getRegisteredCLs();

    void clearUnregisteredClassLoaders();

    void unregisterClassLoader(ClassLoader classloader);
}
