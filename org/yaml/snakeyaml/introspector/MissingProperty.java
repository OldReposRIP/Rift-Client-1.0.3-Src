package org.yaml.snakeyaml.introspector;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class MissingProperty extends Property {

    public MissingProperty(String name) {
        super(name, Object.class);
    }

    public Class[] getActualTypeArguments() {
        return new Class[0];
    }

    public void set(Object object, Object value) throws Exception {}

    public Object get(Object object) {
        return object;
    }

    public List getAnnotations() {
        return Collections.emptyList();
    }

    public Annotation getAnnotation(Class annotationType) {
        return null;
    }
}
