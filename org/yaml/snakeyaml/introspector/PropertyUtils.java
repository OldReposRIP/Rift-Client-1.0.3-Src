package org.yaml.snakeyaml.introspector;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.util.PlatformFeatureDetector;

public class PropertyUtils {

    private static final Logger log = Logger.getLogger(PropertyUtils.class.getPackage().getName());
    private final Map propertiesCache;
    private final Map readableProperties;
    private BeanAccess beanAccess;
    private boolean allowReadOnlyProperties;
    private boolean skipMissingProperties;
    private PlatformFeatureDetector platformFeatureDetector;
    private boolean transientMethodChecked;
    private Method isTransientMethod;

    public PropertyUtils() {
        this(new PlatformFeatureDetector());
    }

    PropertyUtils(PlatformFeatureDetector platformFeatureDetector) {
        this.propertiesCache = new HashMap();
        this.readableProperties = new HashMap();
        this.beanAccess = BeanAccess.DEFAULT;
        this.allowReadOnlyProperties = false;
        this.skipMissingProperties = false;
        this.platformFeatureDetector = platformFeatureDetector;
        if (platformFeatureDetector.isRunningOnAndroid()) {
            this.beanAccess = BeanAccess.FIELD;
        }

    }

    protected Map getPropertiesMap(Class type, BeanAccess bAccess) {
        if (this.propertiesCache.containsKey(type)) {
            return (Map) this.propertiesCache.get(type);
        } else {
            LinkedHashMap properties;
            boolean inaccessableFieldsExist;

            properties = new LinkedHashMap();
            inaccessableFieldsExist = false;
            Class c;
            Field[] afield;
            int i;
            int property;
            Field field;
            int modifiers;

            label91:
            switch (bAccess) {
            case FIELD:
                c = type;

                while (true) {
                    if (c == null) {
                        break label91;
                    }

                    afield = c.getDeclaredFields();
                    i = afield.length;

                    for (property = 0; property < i; ++property) {
                        field = afield[property];
                        modifiers = field.getModifiers();
                        if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) && !properties.containsKey(field.getName())) {
                            properties.put(field.getName(), new FieldProperty(field));
                        }
                    }

                    c = c.getSuperclass();
                }

            default:
                try {
                    PropertyDescriptor[] apropertydescriptor = Introspector.getBeanInfo(type).getPropertyDescriptors();
                    int j = apropertydescriptor.length;

                    for (i = 0; i < j; ++i) {
                        PropertyDescriptor propertydescriptor = apropertydescriptor[i];
                        Method method = propertydescriptor.getReadMethod();

                        if ((method == null || !method.getName().equals("getClass")) && !this.isTransient(propertydescriptor)) {
                            properties.put(propertydescriptor.getName(), new MethodProperty(propertydescriptor));
                        }
                    }
                } catch (IntrospectionException introspectionexception) {
                    throw new YAMLException(introspectionexception);
                }

                for (c = type; c != null; c = c.getSuperclass()) {
                    afield = c.getDeclaredFields();
                    i = afield.length;

                    for (property = 0; property < i; ++property) {
                        field = afield[property];
                        modifiers = field.getModifiers();
                        if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                            if (Modifier.isPublic(modifiers)) {
                                properties.put(field.getName(), new FieldProperty(field));
                            } else {
                                inaccessableFieldsExist = true;
                            }
                        }
                    }
                }
            }

            if (properties.isEmpty() && inaccessableFieldsExist) {
                throw new YAMLException("No JavaBean properties found in " + type.getName());
            } else {
                this.propertiesCache.put(type, properties);
                return properties;
            }
        }
    }

    private boolean isTransient(FeatureDescriptor fd) {
        if (!this.transientMethodChecked) {
            this.transientMethodChecked = true;

            try {
                this.isTransientMethod = FeatureDescriptor.class.getDeclaredMethod("isTransient", new Class[0]);
                this.isTransientMethod.setAccessible(true);
            } catch (NoSuchMethodException nosuchmethodexception) {
                PropertyUtils.log.fine("NoSuchMethod: FeatureDescriptor.isTransient(). Don\'t check it anymore.");
            } catch (SecurityException securityexception) {
                securityexception.printStackTrace();
                this.isTransientMethod = null;
            }
        }

        if (this.isTransientMethod != null) {
            try {
                return Boolean.TRUE.equals(this.isTransientMethod.invoke(fd, new Object[0]));
            } catch (IllegalAccessException illegalaccessexception) {
                illegalaccessexception.printStackTrace();
            } catch (IllegalArgumentException illegalargumentexception) {
                illegalargumentexception.printStackTrace();
            } catch (InvocationTargetException invocationtargetexception) {
                invocationtargetexception.printStackTrace();
            }

            this.isTransientMethod = null;
        }

        return false;
    }

    public Set getProperties(Class type) {
        return this.getProperties(type, this.beanAccess);
    }

    public Set getProperties(Class type, BeanAccess bAccess) {
        if (this.readableProperties.containsKey(type)) {
            return (Set) this.readableProperties.get(type);
        } else {
            Set properties = this.createPropertySet(type, bAccess);

            this.readableProperties.put(type, properties);
            return properties;
        }
    }

    protected Set createPropertySet(Class type, BeanAccess bAccess) {
        TreeSet properties = new TreeSet();
        Collection props = this.getPropertiesMap(type, bAccess).values();
        Iterator iterator = props.iterator();

        while (iterator.hasNext()) {
            Property property = (Property) iterator.next();

            if (property.isReadable() && (this.allowReadOnlyProperties || property.isWritable())) {
                properties.add(property);
            }
        }

        return properties;
    }

    public Property getProperty(Class type, String name) {
        return this.getProperty(type, name, this.beanAccess);
    }

    public Property getProperty(Class type, String name, BeanAccess bAccess) {
        Map properties = this.getPropertiesMap(type, bAccess);
        Object property = (Property) properties.get(name);

        if (property == null && this.skipMissingProperties) {
            property = new MissingProperty(name);
        }

        if (property == null) {
            throw new YAMLException("Unable to find property \'" + name + "\' on class: " + type.getName());
        } else {
            return (Property) property;
        }
    }

    public void setBeanAccess(BeanAccess beanAccess) {
        if (this.platformFeatureDetector.isRunningOnAndroid() && beanAccess != BeanAccess.FIELD) {
            throw new IllegalArgumentException("JVM is Android - only BeanAccess.FIELD is available");
        } else {
            if (this.beanAccess != beanAccess) {
                this.beanAccess = beanAccess;
                this.propertiesCache.clear();
                this.readableProperties.clear();
            }

        }
    }

    public void setAllowReadOnlyProperties(boolean allowReadOnlyProperties) {
        if (this.allowReadOnlyProperties != allowReadOnlyProperties) {
            this.allowReadOnlyProperties = allowReadOnlyProperties;
            this.readableProperties.clear();
        }

    }

    public boolean isAllowReadOnlyProperties() {
        return this.allowReadOnlyProperties;
    }

    public void setSkipMissingProperties(boolean skipMissingProperties) {
        if (this.skipMissingProperties != skipMissingProperties) {
            this.skipMissingProperties = skipMissingProperties;
            this.readableProperties.clear();
        }

    }

    public boolean isSkipMissingProperties() {
        return this.skipMissingProperties;
    }
}
