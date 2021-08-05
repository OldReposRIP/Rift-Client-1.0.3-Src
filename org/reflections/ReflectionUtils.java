package org.reflections;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.Utils;

public abstract class ReflectionUtils {

    public static boolean includeObject = false;
    private static List primitiveNames;
    private static List primitiveTypes;
    private static List primitiveDescriptors;

    public static Set getAllSuperTypes(Class type, Predicate... predicates) {
        LinkedHashSet result = Sets.newLinkedHashSet();

        if (type != null && (ReflectionUtils.includeObject || !type.equals(Object.class))) {
            result.add(type);
            Iterator iterator = getSuperTypes(type).iterator();

            while (iterator.hasNext()) {
                Class supertype = (Class) iterator.next();

                result.addAll(getAllSuperTypes(supertype, new Predicate[0]));
            }
        }

        return filter((Iterable) result, predicates);
    }

    public static Set getSuperTypes(Class type) {
        LinkedHashSet result = new LinkedHashSet();
        Class superclass = type.getSuperclass();
        Class[] interfaces = type.getInterfaces();

        if (superclass != null && (ReflectionUtils.includeObject || !superclass.equals(Object.class))) {
            result.add(superclass);
        }

        if (interfaces != null && interfaces.length > 0) {
            result.addAll(Arrays.asList(interfaces));
        }

        return result;
    }

    public static Set getAllMethods(Class type, Predicate... predicates) {
        HashSet result = Sets.newHashSet();
        Iterator iterator = getAllSuperTypes(type, new Predicate[0]).iterator();

        while (iterator.hasNext()) {
            Class t = (Class) iterator.next();

            result.addAll(getMethods(t, predicates));
        }

        return result;
    }

    public static Set getMethods(Class t, Predicate... predicates) {
        return filter((Object[]) (t.isInterface() ? t.getMethods() : t.getDeclaredMethods()), predicates);
    }

    public static Set getAllConstructors(Class type, Predicate... predicates) {
        HashSet result = Sets.newHashSet();
        Iterator iterator = getAllSuperTypes(type, new Predicate[0]).iterator();

        while (iterator.hasNext()) {
            Class t = (Class) iterator.next();

            result.addAll(getConstructors(t, predicates));
        }

        return result;
    }

    public static Set getConstructors(Class t, Predicate... predicates) {
        return filter((Object[]) t.getDeclaredConstructors(), predicates);
    }

    public static Set getAllFields(Class type, Predicate... predicates) {
        HashSet result = Sets.newHashSet();
        Iterator iterator = getAllSuperTypes(type, new Predicate[0]).iterator();

        while (iterator.hasNext()) {
            Class t = (Class) iterator.next();

            result.addAll(getFields(t, predicates));
        }

        return result;
    }

    public static Set getFields(Class type, Predicate... predicates) {
        return filter((Object[]) type.getDeclaredFields(), predicates);
    }

    public static Set getAllAnnotations(AnnotatedElement type, Predicate... predicates) {
        HashSet result = Sets.newHashSet();

        if (type instanceof Class) {
            Iterator iterator = getAllSuperTypes((Class) type, new Predicate[0]).iterator();

            while (iterator.hasNext()) {
                Class t = (Class) iterator.next();

                result.addAll(getAnnotations(t, predicates));
            }
        } else {
            result.addAll(getAnnotations(type, predicates));
        }

        return result;
    }

    public static Set getAnnotations(AnnotatedElement type, Predicate... predicates) {
        return filter((Object[]) type.getDeclaredAnnotations(), predicates);
    }

    public static Set getAll(Set elements, Predicate... predicates) {
        return (Set) (Utils.isEmpty((Object[]) predicates) ? elements : Sets.newHashSet(Iterables.filter(elements, Predicates.and(predicates))));
    }

    public static Predicate withName(final String name) {
        return new Predicate() {
            public boolean apply(@Nullable Member input) {
                return input != null && input.getName().equals(name);
            }
        };
    }

    public static Predicate withPrefix(final String prefix) {
        return new Predicate() {
            public boolean apply(@Nullable Member input) {
                return input != null && input.getName().startsWith(prefix);
            }
        };
    }

    public static Predicate withPattern(final String regex) {
        return new Predicate() {
            public boolean apply(@Nullable AnnotatedElement input) {
                return Pattern.matches(regex, input.toString());
            }
        };
    }

    public static Predicate withAnnotation(final Class annotation) {
        return new Predicate() {
            public boolean apply(@Nullable AnnotatedElement input) {
                return input != null && input.isAnnotationPresent(annotation);
            }
        };
    }

    public static Predicate withAnnotations(final Class... annotations) {
        return new Predicate() {
            public boolean apply(@Nullable AnnotatedElement input) {
                return input != null && Arrays.equals(annotations, ReflectionUtils.annotationTypes(input.getAnnotations()));
            }
        };
    }

    public static Predicate withAnnotation(final Annotation annotation) {
        return new Predicate() {
            public boolean apply(@Nullable AnnotatedElement input) {
                return input != null && input.isAnnotationPresent(annotation.annotationType()) && ReflectionUtils.areAnnotationMembersMatching(input.getAnnotation(annotation.annotationType()), annotation);
            }
        };
    }

    public static Predicate withAnnotations(final Annotation... annotations) {
        return new Predicate() {
            public boolean apply(@Nullable AnnotatedElement input) {
                if (input != null) {
                    Annotation[] inputAnnotations = input.getAnnotations();

                    if (inputAnnotations.length == annotations.length) {
                        for (int i = 0; i < inputAnnotations.length; ++i) {
                            if (!ReflectionUtils.areAnnotationMembersMatching(inputAnnotations[i], annotations[i])) {
                                return false;
                            }
                        }
                    }
                }

                return true;
            }
        };
    }

    public static Predicate withParameters(final Class... types) {
        return new Predicate() {
            public boolean apply(@Nullable Member input) {
                return Arrays.equals(ReflectionUtils.nameeterTypes(input), types);
            }
        };
    }

    public static Predicate withParametersAssignableTo(final Class... types) {
        return new Predicate() {
            public boolean apply(@Nullable Member input) {
                if (input != null) {
                    Class[] nameeterTypes = ReflectionUtils.nameeterTypes(input);

                    if (nameeterTypes.length == types.length) {
                        for (int i = 0; i < nameeterTypes.length; ++i) {
                            if (!nameeterTypes[i].isAssignableFrom(types[i]) || nameeterTypes[i] == Object.class && types[i] != Object.class) {
                                return false;
                            }
                        }

                        return true;
                    }
                }

                return false;
            }
        };
    }

    public static Predicate withParametersCount(final int count) {
        return new Predicate() {
            public boolean apply(@Nullable Member input) {
                return input != null && ReflectionUtils.nameeterTypes(input).length == count;
            }
        };
    }

    public static Predicate withAnyParameterAnnotation(final Class annotationClass) {
        return new Predicate() {
            public boolean apply(@Nullable Member input) {
                return input != null && Iterables.any(ReflectionUtils.annotationTypes((Iterable) ReflectionUtils.nameeterAnnotations(input)), new Predicate() {
                    public boolean apply(@Nullable Class input) {
                        return input.equals(annotationClass);
                    }
                });
            }
        };
    }

    public static Predicate withAnyParameterAnnotation(final Annotation annotation) {
        return new Predicate() {
            public boolean apply(@Nullable Member input) {
                return input != null && Iterables.any(ReflectionUtils.nameeterAnnotations(input), new Predicate() {
                    public boolean apply(@Nullable Annotation input) {
                        return ReflectionUtils.areAnnotationMembersMatching(annotation, input);
                    }
                });
            }
        };
    }

    public static Predicate withType(final Class type) {
        return new Predicate() {
            public boolean apply(@Nullable Field input) {
                return input != null && input.getType().equals(type);
            }
        };
    }

    public static Predicate withTypeAssignableTo(final Class type) {
        return new Predicate() {
            public boolean apply(@Nullable Field input) {
                return input != null && type.isAssignableFrom(input.getType());
            }
        };
    }

    public static Predicate withReturnType(final Class type) {
        return new Predicate() {
            public boolean apply(@Nullable Method input) {
                return input != null && input.getReturnType().equals(type);
            }
        };
    }

    public static Predicate withReturnTypeAssignableTo(final Class type) {
        return new Predicate() {
            public boolean apply(@Nullable Method input) {
                return input != null && type.isAssignableFrom(input.getReturnType());
            }
        };
    }

    public static Predicate withModifier(final int mod) {
        return new Predicate() {
            public boolean apply(@Nullable Member input) {
                return input != null && (input.getModifiers() & mod) != 0;
            }
        };
    }

    public static Predicate withClassModifier(final int mod) {
        return new Predicate() {
            public boolean apply(@Nullable Class input) {
                return input != null && (input.getModifiers() & mod) != 0;
            }
        };
    }

    public static Class forName(String typeName, ClassLoader... classLoaders) {
        if (getPrimitiveNames().contains(typeName)) {
            return (Class) getPrimitiveTypes().get(getPrimitiveNames().indexOf(typeName));
        } else {
            String type;

            if (typeName.contains("[")) {
                int reflectionsExceptions = typeName.indexOf("[");

                type = typeName.substring(0, reflectionsExceptions);
                String array = typeName.substring(reflectionsExceptions).replace("]", "");

                if (getPrimitiveNames().contains(type)) {
                    type = (String) getPrimitiveDescriptors().get(getPrimitiveNames().indexOf(type));
                } else {
                    type = "L" + type + ";";
                }

                type = array + type;
            } else {
                type = typeName;
            }

            ArrayList arraylist = Lists.newArrayList();
            ClassLoader[] aclassloader = ClasspathHelper.classLoaders(classLoaders);
            int reflectionsException = aclassloader.length;
            int i = 0;

            while (i < reflectionsException) {
                ClassLoader classLoader = aclassloader[i];

                if (type.contains("[")) {
                    try {
                        return Class.forName(type, false, classLoader);
                    } catch (Throwable throwable) {
                        arraylist.add(new ReflectionsException("could not get type for name " + typeName, throwable));
                    }
                }

                try {
                    return classLoader.loadClass(type);
                } catch (Throwable throwable1) {
                    arraylist.add(new ReflectionsException("could not get type for name " + typeName, throwable1));
                    ++i;
                }
            }

            if (Reflections.log != null) {
                Iterator iterator = arraylist.iterator();

                while (iterator.hasNext()) {
                    ReflectionsException reflectionsexception = (ReflectionsException) iterator.next();

                    Reflections.log.warn("could not get type for name " + typeName + " from any class loader", reflectionsexception);
                }
            }

            return null;
        }
    }

    public static List forNames(Iterable classes, ClassLoader... classLoaders) {
        ArrayList result = new ArrayList();
        Iterator iterator = classes.iterator();

        while (iterator.hasNext()) {
            String className = (String) iterator.next();
            Class type = forName(className, classLoaders);

            if (type != null) {
                result.add(type);
            }
        }

        return result;
    }

    private static Class[] nameeterTypes(Member member) {
        return member != null ? (member.getClass() == Method.class ? ((Method) member).getParameterTypes() : (member.getClass() == Constructor.class ? ((Constructor) member).getParameterTypes() : null)) : null;
    }

    private static Set nameeterAnnotations(Member member) {
        HashSet result = Sets.newHashSet();
        Annotation[][] annotations = member instanceof Method ? ((Method) member).getParameterAnnotations() : (member instanceof Constructor ? ((Constructor) member).getParameterAnnotations() : (Annotation[][]) null);
        Annotation[][] aannotation = annotations;
        int i = annotations.length;

        for (int j = 0; j < i; ++j) {
            Annotation[] annotation = aannotation[j];

            Collections.addAll(result, annotation);
        }

        return result;
    }

    private static Set annotationTypes(Iterable annotations) {
        HashSet result = Sets.newHashSet();
        Iterator iterator = annotations.iterator();

        while (iterator.hasNext()) {
            Annotation annotation = (Annotation) iterator.next();

            result.add(annotation.annotationType());
        }

        return result;
    }

    private static Class[] annotationTypes(Annotation[] annotations) {
        Class[] result = new Class[annotations.length];

        for (int i = 0; i < annotations.length; ++i) {
            result[i] = annotations[i].annotationType();
        }

        return result;
    }

    private static void initPrimitives() {
        if (ReflectionUtils.primitiveNames == null) {
            ReflectionUtils.primitiveNames = Lists.newArrayList(new String[] { "boolean", "char", "byte", "short", "int", "long", "float", "double", "void"});
            ReflectionUtils.primitiveTypes = Lists.newArrayList(new Class[] { Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE, Void.TYPE});
            ReflectionUtils.primitiveDescriptors = Lists.newArrayList(new String[] { "Z", "C", "B", "S", "I", "J", "F", "D", "V"});
        }

    }

    private static List getPrimitiveNames() {
        initPrimitives();
        return ReflectionUtils.primitiveNames;
    }

    private static List getPrimitiveTypes() {
        initPrimitives();
        return ReflectionUtils.primitiveTypes;
    }

    private static List getPrimitiveDescriptors() {
        initPrimitives();
        return ReflectionUtils.primitiveDescriptors;
    }

    static Set filter(Object[] elements, Predicate... predicates) {
        return Utils.isEmpty((Object[]) predicates) ? Sets.newHashSet(elements) : Sets.newHashSet(Iterables.filter(Arrays.asList(elements), Predicates.and(predicates)));
    }

    static Set filter(Iterable elements, Predicate... predicates) {
        return Utils.isEmpty((Object[]) predicates) ? Sets.newHashSet(elements) : Sets.newHashSet(Iterables.filter(elements, Predicates.and(predicates)));
    }

    private static boolean areAnnotationMembersMatching(Annotation annotation1, Annotation annotation2) {
        if (annotation2 != null && annotation1.annotationType() == annotation2.annotationType()) {
            Method[] amethod = annotation1.annotationType().getDeclaredMethods();
            int i = amethod.length;

            for (int j = 0; j < i; ++j) {
                Method method = amethod[j];

                try {
                    if (!method.invoke(annotation1, new Object[0]).equals(method.invoke(annotation2, new Object[0]))) {
                        return false;
                    }
                } catch (Exception exception) {
                    throw new ReflectionsException(String.format("could not invoke method %s on annotation %s", new Object[] { method.getName(), annotation1.annotationType()}), exception);
                }
            }

            return true;
        } else {
            return false;
        }
    }
}
