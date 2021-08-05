package org.reflections.scanners;

import com.google.common.base.Joiner;
import java.util.Iterator;

public class TypeElementsScanner extends AbstractScanner {

    private boolean includeFields = true;
    private boolean includeMethods = true;
    private boolean includeAnnotations = true;
    private boolean publicOnly = true;

    public void scan(Object cls) {
        String className = this.getMetadataAdapter().getClassName(cls);

        if (this.acceptResult(className)) {
            this.getStore().put(className, "");
            Iterator iterator;
            Object annotation;
            String methodKey;

            if (this.includeFields) {
                iterator = this.getMetadataAdapter().getFields(cls).iterator();

                while (iterator.hasNext()) {
                    annotation = iterator.next();
                    methodKey = this.getMetadataAdapter().getFieldName(annotation);
                    this.getStore().put(className, methodKey);
                }
            }

            if (this.includeMethods) {
                iterator = this.getMetadataAdapter().getMethods(cls).iterator();

                while (iterator.hasNext()) {
                    annotation = iterator.next();
                    if (!this.publicOnly || this.getMetadataAdapter().isPublic(annotation)) {
                        methodKey = this.getMetadataAdapter().getMethodName(annotation) + "(" + Joiner.on(", ").join(this.getMetadataAdapter().getParameterNames(annotation)) + ")";
                        this.getStore().put(className, methodKey);
                    }
                }
            }

            if (this.includeAnnotations) {
                iterator = this.getMetadataAdapter().getClassAnnotationNames(cls).iterator();

                while (iterator.hasNext()) {
                    annotation = iterator.next();
                    this.getStore().put(className, "@" + annotation);
                }
            }

        }
    }

    public TypeElementsScanner includeFields() {
        return this.includeFields(true);
    }

    public TypeElementsScanner includeFields(boolean include) {
        this.includeFields = include;
        return this;
    }

    public TypeElementsScanner includeMethods() {
        return this.includeMethods(true);
    }

    public TypeElementsScanner includeMethods(boolean include) {
        this.includeMethods = include;
        return this;
    }

    public TypeElementsScanner includeAnnotations() {
        return this.includeAnnotations(true);
    }

    public TypeElementsScanner includeAnnotations(boolean include) {
        this.includeAnnotations = include;
        return this;
    }

    public TypeElementsScanner publicOnly(boolean only) {
        this.publicOnly = only;
        return this;
    }

    public TypeElementsScanner publicOnly() {
        return this.publicOnly(true);
    }
}
