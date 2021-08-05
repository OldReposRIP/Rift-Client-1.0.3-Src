package org.reflections.scanners;

import java.util.Iterator;

public class MethodAnnotationsScanner extends AbstractScanner {

    public void scan(Object cls) {
        Iterator iterator = this.getMetadataAdapter().getMethods(cls).iterator();

        while (iterator.hasNext()) {
            Object method = iterator.next();
            Iterator iterator1 = this.getMetadataAdapter().getMethodAnnotationNames(method).iterator();

            while (iterator1.hasNext()) {
                String methodAnnotation = (String) iterator1.next();

                if (this.acceptResult(methodAnnotation)) {
                    this.getStore().put(methodAnnotation, this.getMetadataAdapter().getMethodFullKey(cls, method));
                }
            }
        }

    }
}
