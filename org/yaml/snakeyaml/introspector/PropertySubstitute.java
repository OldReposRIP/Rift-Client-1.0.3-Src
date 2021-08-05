package org.yaml.snakeyaml.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.error.YAMLException;

public class PropertySubstitute extends Property {

    private static final Logger log = Logger.getLogger(PropertySubstitute.class.getPackage().getName());
    protected Class targetType;
    private final String readMethod;
    private final String writeMethod;
    private transient Method read;
    private transient Method write;
    private Field field;
    protected Class[] nameeters;
    private Property delegate;
    private boolean filler;

    public PropertySubstitute(String name, Class type, String readMethod, String writeMethod, Class... names) {
        super(name, type);
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
        this.setActualTypeArguments(names);
        this.filler = false;
    }

    public PropertySubstitute(String name, Class type, Class... names) {
        this(name, type, (String) null, (String) null, names);
    }

    public Class[] getActualTypeArguments() {
        return this.nameeters == null && this.delegate != null ? this.delegate.getActualTypeArguments() : this.nameeters;
    }

    public void setActualTypeArguments(Class... args) {
        if (args != null && args.length > 0) {
            this.nameeters = args;
        } else {
            this.nameeters = null;
        }

    }

    public void set(Object object, Object value) throws Exception {
        if (this.write != null) {
            if (!this.filler) {
                this.write.invoke(object, new Object[] { value});
            } else if (value != null) {
                Iterator i;

                if (value instanceof Collection) {
                    Collection len = (Collection) value;

                    i = len.iterator();

                    while (i.hasNext()) {
                        Object entry = i.next();

                        this.write.invoke(object, new Object[] { entry});
                    }
                } else if (value instanceof Map) {
                    Map map = (Map) value;

                    i = map.entrySet().iterator();

                    while (i.hasNext()) {
                        Entry entry = (Entry) i.next();

                        this.write.invoke(object, new Object[] { entry.getKey(), entry.getValue()});
                    }
                } else if (value.getClass().isArray()) {
                    int i = Array.getLength(value);

                    for (int j = 0; j < i; ++j) {
                        this.write.invoke(object, new Object[] { Array.get(value, j)});
                    }
                }
            }
        } else if (this.field != null) {
            this.field.set(object, value);
        } else if (this.delegate != null) {
            this.delegate.set(object, value);
        } else {
            PropertySubstitute.log.warning("No setter/delegate for \'" + this.getName() + "\' on object " + object);
        }

    }

    public Object get(Object object) {
        try {
            if (this.read != null) {
                return this.read.invoke(object, new Object[0]);
            }

            if (this.field != null) {
                return this.field.get(object);
            }
        } catch (Exception exception) {
            throw new YAMLException("Unable to find getter for property \'" + this.getName() + "\' on object " + object + ":" + exception);
        }

        if (this.delegate != null) {
            return this.delegate.get(object);
        } else {
            throw new YAMLException("No getter or delegate for property \'" + this.getName() + "\' on object " + object);
        }
    }

    public List getAnnotations() {
        Annotation[] annotations = null;

        if (this.read != null) {
            annotations = this.read.getAnnotations();
        } else if (this.field != null) {
            annotations = this.field.getAnnotations();
        }

        return annotations != null ? Arrays.asList(annotations) : this.delegate.getAnnotations();
    }

    public Annotation getAnnotation(Class annotationType) {
        Annotation annotation;

        if (this.read != null) {
            annotation = this.read.getAnnotation(annotationType);
        } else if (this.field != null) {
            annotation = this.field.getAnnotation(annotationType);
        } else {
            annotation = this.delegate.getAnnotation(annotationType);
        }

        return annotation;
    }

    public void setTargetType(Class targetType) {
        if (this.targetType != targetType) {
            this.targetType = targetType;
            String name = this.getName();

            for (Class c = targetType; c != null; c = c.getSuperclass()) {
                Field[] afield = c.getDeclaredFields();
                int i = afield.length;

                for (int j = 0; j < i; ++j) {
                    Field f = afield[j];

                    if (f.getName().equals(name)) {
                        int modifiers = f.getModifiers();

                        if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                            f.setAccessible(true);
                            this.field = f;
                        }
                        break;
                    }
                }
            }

            if (this.field == null && PropertySubstitute.log.isLoggable(Level.FINE)) {
                PropertySubstitute.log.fine(String.format("Failed to find field for %s.%s", new Object[] { targetType.getName(), this.getName()}));
            }

            if (this.readMethod != null) {
                this.read = this.discoverMethod(targetType, this.readMethod, new Class[0]);
            }

            if (this.writeMethod != null) {
                this.filler = false;
                this.write = this.discoverMethod(targetType, this.writeMethod, new Class[] { this.getType()});
                if (this.write == null && this.nameeters != null) {
                    this.filler = true;
                    this.write = this.discoverMethod(targetType, this.writeMethod, this.nameeters);
                }
            }
        }

    }

    private Method discoverMethod(Class type, String name, Class... names) {
        for (Class c = type; c != null; c = c.getSuperclass()) {
            Method[] amethod = c.getDeclaredMethods();
            int i = amethod.length;

            for (int j = 0; j < i; ++j) {
                Method method = amethod[j];

                if (name.equals(method.getName())) {
                    Class[] nameeterTypes = method.getParameterTypes();

                    if (nameeterTypes.length == names.length) {
                        boolean found = true;

                        for (int i = 0; i < nameeterTypes.length; ++i) {
                            if (!nameeterTypes[i].isAssignableFrom(names[i])) {
                                found = false;
                            }
                        }

                        if (found) {
                            method.setAccessible(true);
                            return method;
                        }
                    }
                }
            }
        }

        if (PropertySubstitute.log.isLoggable(Level.FINE)) {
            PropertySubstitute.log.fine(String.format("Failed to find [%s(%d args)] for %s.%s", new Object[] { name, Integer.valueOf(names.length), this.targetType.getName(), this.getName()}));
        }

        return null;
    }

    public String getName() {
        String n = super.getName();

        return n != null ? n : (this.delegate != null ? this.delegate.getName() : null);
    }

    public Class getType() {
        Class t = super.getType();

        return t != null ? t : (this.delegate != null ? this.delegate.getType() : null);
    }

    public boolean isReadable() {
        return this.read != null || this.field != null || this.delegate != null && this.delegate.isReadable();
    }

    public boolean isWritable() {
        return this.write != null || this.field != null || this.delegate != null && this.delegate.isWritable();
    }

    public void setDelegate(Property delegate) {
        this.delegate = delegate;
        if (this.writeMethod != null && this.write == null && !this.filler) {
            this.filler = true;
            this.write = this.discoverMethod(this.targetType, this.writeMethod, this.getActualTypeArguments());
        }

    }
}
