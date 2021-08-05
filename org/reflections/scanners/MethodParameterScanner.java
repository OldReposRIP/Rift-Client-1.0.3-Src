package org.reflections.scanners;

import java.util.Iterator;
import java.util.List;
import org.reflections.adapters.MetadataAdapter;

public class MethodParameterScanner extends AbstractScanner {

    public void scan(Object cls) {
        MetadataAdapter md = this.getMetadataAdapter();
        Iterator iterator = md.getMethods(cls).iterator();

        while (iterator.hasNext()) {
            Object method = iterator.next();
            String signature = md.getParameterNames(method).toString();

            if (this.acceptResult(signature)) {
                this.getStore().put(signature, md.getMethodFullKey(cls, method));
            }

            String returnTypeName = md.getReturnTypeName(method);

            if (this.acceptResult(returnTypeName)) {
                this.getStore().put(returnTypeName, md.getMethodFullKey(cls, method));
            }

            List nameeterNames = md.getParameterNames(method);

            for (int i = 0; i < nameeterNames.size(); ++i) {
                Iterator iterator1 = md.getParameterAnnotationNames(method, i).iterator();

                while (iterator1.hasNext()) {
                    Object nameAnnotation = iterator1.next();

                    if (this.acceptResult((String) nameAnnotation)) {
                        this.getStore().put((String) nameAnnotation, md.getMethodFullKey(cls, method));
                    }
                }
            }
        }

    }
}
