package org.reflections.scanners;

import java.lang.annotation.Inherited;
import java.util.Iterator;

public class TypeAnnotationsScanner extends AbstractScanner {

    public void scan(Object cls) {
        String className = this.getMetadataAdapter().getClassName(cls);
        Iterator iterator = this.getMetadataAdapter().getClassAnnotationNames(cls).iterator();

        while (iterator.hasNext()) {
            String annotationType = (String) iterator.next();

            if (this.acceptResult(annotationType) || annotationType.equals(Inherited.class.getName())) {
                this.getStore().put(annotationType, className);
            }
        }

    }
}
