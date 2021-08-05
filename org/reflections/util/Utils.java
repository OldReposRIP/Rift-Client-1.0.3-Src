package org.reflections.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Utils {

    public static String repeat(String string, int times) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < times; ++i) {
            sb.append(string);
        }

        return sb.toString();
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isEmpty(Object[] objects) {
        return objects == null || objects.length == 0;
    }

    public static File prepareFile(String filename) {
        File file = new File(filename);
        File parent = file.getAbsoluteFile().getParentFile();

        if (!parent.exists()) {
            parent.mkdirs();
        }

        return file;
    }

    public static Member getMemberFromDescriptor(String descriptor, ClassLoader... classLoaders) throws ReflectionsException {
        int p0 = descriptor.lastIndexOf(40);
        String memberKey = p0 != -1 ? descriptor.substring(0, p0) : descriptor;
        String methodParameters = p0 != -1 ? descriptor.substring(p0 + 1, descriptor.lastIndexOf(41)) : "";
        int p1 = Math.max(memberKey.lastIndexOf(46), memberKey.lastIndexOf("$"));
        String className = memberKey.substring(memberKey.lastIndexOf(32) + 1, p1);
        String memberName = memberKey.substring(p1 + 1);
        Class[] nameeterTypes = null;

        if (!isEmpty(methodParameters)) {
            String[] aClass = methodParameters.split(",");
            ArrayList e = new ArrayList(aClass.length);
            String[] astring = aClass;
            int i = aClass.length;

            for (int j = 0; j < i; ++j) {
                String name = astring[j];

                e.add(ReflectionUtils.forName(name.trim(), classLoaders));
            }

            nameeterTypes = (Class[]) e.toArray(new Class[e.size()]);
        }

        Class oclass = ReflectionUtils.forName(className, classLoaders);

        while (oclass != null) {
            try {
                if (descriptor.contains("(")) {
                    if (!isConstructor(descriptor)) {
                        return oclass.isInterface() ? oclass.getMethod(memberName, nameeterTypes) : oclass.getDeclaredMethod(memberName, nameeterTypes);
                    }

                    return oclass.isInterface() ? oclass.getConstructor(nameeterTypes) : oclass.getDeclaredConstructor(nameeterTypes);
                }

                return oclass.isInterface() ? oclass.getField(memberName) : oclass.getDeclaredField(memberName);
            } catch (Exception exception) {
                oclass = oclass.getSuperclass();
            }
        }

        throw new ReflectionsException("Can\'t resolve member named " + memberName + " for class " + className);
    }

    public static Set getMethodsFromDescriptors(Iterable annotatedWith, ClassLoader... classLoaders) {
        HashSet result = Sets.newHashSet();
        Iterator iterator = annotatedWith.iterator();

        while (iterator.hasNext()) {
            String annotated = (String) iterator.next();

            if (!isConstructor(annotated)) {
                Method member = (Method) getMemberFromDescriptor(annotated, classLoaders);

                if (member != null) {
                    result.add(member);
                }
            }
        }

        return result;
    }

    public static Set getConstructorsFromDescriptors(Iterable annotatedWith, ClassLoader... classLoaders) {
        HashSet result = Sets.newHashSet();
        Iterator iterator = annotatedWith.iterator();

        while (iterator.hasNext()) {
            String annotated = (String) iterator.next();

            if (isConstructor(annotated)) {
                Constructor member = (Constructor) getMemberFromDescriptor(annotated, classLoaders);

                if (member != null) {
                    result.add(member);
                }
            }
        }

        return result;
    }

    public static Set getMembersFromDescriptors(Iterable values, ClassLoader... classLoaders) {
        HashSet result = Sets.newHashSet();
        Iterator iterator = values.iterator();

        while (iterator.hasNext()) {
            String value = (String) iterator.next();

            try {
                result.add(getMemberFromDescriptor(value, classLoaders));
            } catch (ReflectionsException reflectionsexception) {
                throw new ReflectionsException("Can\'t resolve member named " + value, reflectionsexception);
            }
        }

        return result;
    }

    public static Field getFieldFromString(String field, ClassLoader... classLoaders) {
        String className = field.substring(0, field.lastIndexOf(46));
        String fieldName = field.substring(field.lastIndexOf(46) + 1);

        try {
            return ReflectionUtils.forName(className, classLoaders).getDeclaredField(fieldName);
        } catch (NoSuchFieldException nosuchfieldexception) {
            throw new ReflectionsException("Can\'t resolve field named " + fieldName, nosuchfieldexception);
        }
    }

    public static void close(InputStream closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioexception) {
            if (Reflections.log != null) {
                Reflections.log.warn("Could not close InputStream", ioexception);
            }
        }

    }

    @Nullable
    public static Logger findLogger(Class aClass) {
        try {
            Class.forName("org.slf4j.impl.StaticLoggerBinder");
            return LoggerFactory.getLogger(aClass);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static boolean isConstructor(String fqn) {
        return fqn.contains("init>");
    }

    public static String name(Class type) {
        if (!type.isArray()) {
            return type.getName();
        } else {
            int dim;

            for (dim = 0; type.isArray(); type = type.getComponentType()) {
                ++dim;
            }

            return type.getName() + repeat("[]", dim);
        }
    }

    public static List names(Iterable types) {
        ArrayList result = new ArrayList();
        Iterator iterator = types.iterator();

        while (iterator.hasNext()) {
            Class type = (Class) iterator.next();

            result.add(name(type));
        }

        return result;
    }

    public static List names(Class... types) {
        return names((Iterable) Arrays.asList(types));
    }

    public static String name(Constructor constructor) {
        return constructor.getName() + ".<init>(" + Joiner.on(", ").join(names(constructor.getParameterTypes())) + ")";
    }

    public static String name(Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName() + "(" + Joiner.on(", ").join(names(method.getParameterTypes())) + ")";
    }

    public static String name(Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }
}
