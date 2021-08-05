package org.reflections.scanners;

import org.reflections.vfs.Vfs;

/** @deprecated */
@Deprecated
public class TypesScanner extends AbstractScanner {

    public Object scan(Vfs.File file, Object classObject) {
        classObject = super.scan(file, classObject);
        String className = this.getMetadataAdapter().getClassName(classObject);

        this.getStore().put(className, className);
        return classObject;
    }

    public void scan(Object cls) {
        throw new UnsupportedOperationException("should not get here");
    }
}
