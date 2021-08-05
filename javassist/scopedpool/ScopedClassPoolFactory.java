package javassist.scopedpool;

import javassist.ClassPool;

public interface ScopedClassPoolFactory {

    ScopedClassPool create(ClassLoader classloader, ClassPool classpool, ScopedClassPoolRepository scopedclasspoolrepository);

    ScopedClassPool create(ClassPool classpool, ScopedClassPoolRepository scopedclasspoolrepository);
}
