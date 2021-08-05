package org.yaml.snakeyaml.introspector;

import java.lang.annotation.Annotation;
import java.util.List;

public abstract class Property implements Comparable {

    private final String name;
    private final Class type;

    public Property(String name, Class type) {
        this.name = name;
        this.type = type;
    }

    public Class getType() {
        return this.type;
    }

    public abstract Class[] getActualTypeArguments();

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.getName() + " of " + this.getType();
    }

    public int compareTo(Property o) {
        return this.getName().compareTo(o.getName());
    }

    public boolean isWritable() {
        return true;
    }

    public boolean isReadable() {
        return true;
    }

    public abstract void set(Object object, Object object1) throws Exception;

    public abstract Object get(Object object);

    public abstract List getAnnotations();

    public abstract Annotation getAnnotation(Class oclass);

    public int hashCode() {
        return this.getName().hashCode() + this.getType().hashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof Property)) {
            return false;
        } else {
            Property p = (Property) other;

            return this.getName().equals(p.getName()) && this.getType().equals(p.getType());
        }
    }
}
