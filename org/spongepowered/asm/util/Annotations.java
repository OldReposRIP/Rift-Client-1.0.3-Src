package org.spongepowered.asm.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.lib.tree.MethodNode;

public final class Annotations {

    public static void setVisible(FieldNode field, Class annotationClass, Object... value) {
        AnnotationNode node = createNode(Type.getDescriptor(annotationClass), value);

        field.visibleAnnotations = add(field.visibleAnnotations, node);
    }

    public static void setInvisible(FieldNode field, Class annotationClass, Object... value) {
        AnnotationNode node = createNode(Type.getDescriptor(annotationClass), value);

        field.invisibleAnnotations = add(field.invisibleAnnotations, node);
    }

    public static void setVisible(MethodNode method, Class annotationClass, Object... value) {
        AnnotationNode node = createNode(Type.getDescriptor(annotationClass), value);

        method.visibleAnnotations = add(method.visibleAnnotations, node);
    }

    public static void setInvisible(MethodNode method, Class annotationClass, Object... value) {
        AnnotationNode node = createNode(Type.getDescriptor(annotationClass), value);

        method.invisibleAnnotations = add(method.invisibleAnnotations, node);
    }

    private static AnnotationNode createNode(String annotationType, Object... value) {
        AnnotationNode node = new AnnotationNode(annotationType);

        for (int pos = 0; pos < value.length - 1; pos += 2) {
            if (!(value[pos] instanceof String)) {
                throw new IllegalArgumentException("Annotation keys must be strings, found " + value[pos].getClass().getSimpleName() + " with " + value[pos].toString() + " at index " + pos + " creating " + annotationType);
            }

            node.visit((String) value[pos], value[pos + 1]);
        }

        return node;
    }

    private static List add(List annotations, AnnotationNode node) {
        if (annotations == null) {
            annotations = new ArrayList(1);
        } else {
            ((List) annotations).remove(get((List) annotations, node.desc));
        }

        ((List) annotations).add(node);
        return (List) annotations;
    }

    public static AnnotationNode getVisible(FieldNode field, Class annotationClass) {
        return get(field.visibleAnnotations, Type.getDescriptor(annotationClass));
    }

    public static AnnotationNode getInvisible(FieldNode field, Class annotationClass) {
        return get(field.invisibleAnnotations, Type.getDescriptor(annotationClass));
    }

    public static AnnotationNode getVisible(MethodNode method, Class annotationClass) {
        return get(method.visibleAnnotations, Type.getDescriptor(annotationClass));
    }

    public static AnnotationNode getInvisible(MethodNode method, Class annotationClass) {
        return get(method.invisibleAnnotations, Type.getDescriptor(annotationClass));
    }

    public static AnnotationNode getSingleVisible(MethodNode method, Class... annotationClasses) {
        return getSingle(method.visibleAnnotations, annotationClasses);
    }

    public static AnnotationNode getSingleInvisible(MethodNode method, Class... annotationClasses) {
        return getSingle(method.invisibleAnnotations, annotationClasses);
    }

    public static AnnotationNode getVisible(ClassNode classNode, Class annotationClass) {
        return get(classNode.visibleAnnotations, Type.getDescriptor(annotationClass));
    }

    public static AnnotationNode getInvisible(ClassNode classNode, Class annotationClass) {
        return get(classNode.invisibleAnnotations, Type.getDescriptor(annotationClass));
    }

    public static AnnotationNode getVisibleParameter(MethodNode method, Class annotationClass, int nameIndex) {
        return getParameter(method.visibleParameterAnnotations, Type.getDescriptor(annotationClass), nameIndex);
    }

    public static AnnotationNode getInvisibleParameter(MethodNode method, Class annotationClass, int nameIndex) {
        return getParameter(method.invisibleParameterAnnotations, Type.getDescriptor(annotationClass), nameIndex);
    }

    public static AnnotationNode getParameter(List[] nameeterAnnotations, String annotationType, int nameIndex) {
        return nameeterAnnotations != null && nameIndex >= 0 && nameIndex < nameeterAnnotations.length ? get(nameeterAnnotations[nameIndex], annotationType) : null;
    }

    public static AnnotationNode get(List annotations, String annotationType) {
        if (annotations == null) {
            return null;
        } else {
            Iterator iterator = annotations.iterator();

            AnnotationNode annotation;

            do {
                if (!iterator.hasNext()) {
                    return null;
                }

                annotation = (AnnotationNode) iterator.next();
            } while (!annotationType.equals(annotation.desc));

            return annotation;
        }
    }

    private static AnnotationNode getSingle(List annotations, Class[] annotationClasses) {
        ArrayList nodes = new ArrayList();
        Class[] foundNodes = annotationClasses;
        int i = annotationClasses.length;

        for (int j = 0; j < i; ++j) {
            Class annotationClass = foundNodes[j];
            AnnotationNode annotation = get(annotations, Type.getDescriptor(annotationClass));

            if (annotation != null) {
                nodes.add(annotation);
            }
        }

        int k = nodes.size();

        if (k > 1) {
            throw new IllegalArgumentException("Conflicting annotations found: " + Lists.transform(nodes, new Function() {
                public String apply(AnnotationNode input) {
                    return input.desc;
                }
            }));
        } else {
            return k == 0 ? null : (AnnotationNode) nodes.get(0);
        }
    }

    public static Object getValue(AnnotationNode annotation) {
        return getValue(annotation, "value");
    }

    public static Object getValue(AnnotationNode annotation, String key, Object defaultValue) {
        Object returnValue = getValue(annotation, key);

        return returnValue != null ? returnValue : defaultValue;
    }

    public static Object getValue(AnnotationNode annotation, String key, Class annotationClass) {
        Preconditions.checkNotNull(annotationClass, "annotationClass cannot be null");
        Object value = getValue(annotation, key);

        if (value == null) {
            try {
                value = annotationClass.getDeclaredMethod(key, new Class[0]).getDefaultValue();
            } catch (NoSuchMethodException nosuchmethodexception) {
                ;
            }
        }

        return value;
    }

    public static Object getValue(AnnotationNode annotation, String key) {
        boolean getNextValue = false;

        if (annotation != null && annotation.values != null) {
            Iterator iterator = annotation.values.iterator();

            while (iterator.hasNext()) {
                Object value = iterator.next();

                if (getNextValue) {
                    return value;
                }

                if (value.equals(key)) {
                    getNextValue = true;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    public static Enum getValue(AnnotationNode annotation, String key, Class enumClass, Enum defaultValue) {
        String[] value = (String[]) getValue(annotation, key);

        return value == null ? defaultValue : toEnumValue(enumClass, value);
    }

    public static List getValue(AnnotationNode annotation, String key, boolean notNull) {
        Object value = getValue(annotation, key);

        if (value instanceof List) {
            return (List) value;
        } else if (value != null) {
            ArrayList list = new ArrayList();

            list.add(value);
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    public static List getValue(AnnotationNode annotation, String key, boolean notNull, Class enumClass) {
        Object value = getValue(annotation, key);

        if (!(value instanceof List)) {
            if (value instanceof String[]) {
                ArrayList list1 = new ArrayList();

                list1.add(toEnumValue(enumClass, (String[]) ((String[]) value)));
                return list1;
            } else {
                return Collections.emptyList();
            }
        } else {
            ListIterator list = ((List) value).listIterator();

            while (list.hasNext()) {
                list.set(toEnumValue(enumClass, (String[]) ((String[]) list.next())));
            }

            return (List) value;
        }
    }

    private static Enum toEnumValue(Class enumClass, String[] value) {
        if (!enumClass.getName().equals(Type.getType(value[0]).getClassName())) {
            throw new IllegalArgumentException("The supplied enum class does not match the stored enum value");
        } else {
            return Enum.valueOf(enumClass, value[1]);
        }
    }
}
