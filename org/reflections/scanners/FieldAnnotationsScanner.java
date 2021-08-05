package org.reflections.scanners;

import java.util.Iterator;
import java.util.List;

public class FieldAnnotationsScanner extends AbstractScanner {

    public void scan(Object cls) {
        String className = this.getMetadataAdapter().getClassName(cls);
        List fields = this.getMetadataAdapter().getFields(cls);
        Iterator iterator = fields.iterator();

        while (iterator.hasNext()) {
            Object field = iterator.next();
            List fieldAnnotations = this.getMetadataAdapter().getFieldAnnotationNames(field);
            Iterator iterator1 = fieldAnnotations.iterator();

            while (iterator1.hasNext()) {
                String fieldAnnotation = (String) iterator1.next();

                if (this.acceptResult(fieldAnnotation)) {
                    String fieldName = this.getMetadataAdapter().getFieldName(field);

                    this.getStore().put(fieldAnnotation, String.format("%s.%s", new Object[] { className, fieldName}));
                }
            }
        }

    }
}
