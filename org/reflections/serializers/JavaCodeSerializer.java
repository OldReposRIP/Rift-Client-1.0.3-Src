package org.reflections.serializers;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.Utils;

public class JavaCodeSerializer implements Serializer {

    private static final String pathSeparator = "_";
    private static final String doubleSeparator = "__";
    private static final String dotSeparator = ".";
    private static final String arrayDescriptor = "$$";
    private static final String tokenSeparator = "_";

    public Reflections read(InputStream inputStream) {
        throw new UnsupportedOperationException("read is not implemented on JavaCodeSerializer");
    }

    public File save(Reflections reflections, String name) {
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }

        String filename = name.replace('.', '/').concat(".java");
        File file = Utils.prepareFile(filename);
        int lastDot = name.lastIndexOf(46);
        String packageName;
        String className;

        if (lastDot == -1) {
            packageName = "";
            className = name.substring(name.lastIndexOf(47) + 1);
        } else {
            packageName = name.substring(name.lastIndexOf(47) + 1, lastDot);
            className = name.substring(lastDot + 1);
        }

        try {
            StringBuilder e = new StringBuilder();

            e.append("//generated using Reflections JavaCodeSerializer").append(" [").append(new Date()).append("]").append("\n");
            if (packageName.length() != 0) {
                e.append("package ").append(packageName).append(";\n");
                e.append("\n");
            }

            e.append("public interface ").append(className).append(" {\n\n");
            e.append(this.toString(reflections));
            e.append("}\n");
            Files.write(e.toString(), new File(filename), Charset.defaultCharset());
            return file;
        } catch (IOException ioexception) {
            throw new RuntimeException();
        }
    }

    public String toString(Reflections reflections) {
        if (reflections.getStore().get(TypeElementsScanner.class.getSimpleName()).isEmpty() && Reflections.log != null) {
            Reflections.log.warn("JavaCodeSerializer needs TypeElementsScanner configured");
        }

        StringBuilder sb = new StringBuilder();
        ArrayList prevPaths = Lists.newArrayList();
        int indent = 1;
        ArrayList keys = Lists.newArrayList(reflections.getStore().get(TypeElementsScanner.class.getSimpleName()).keySet());

        Collections.sort(keys);

        ArrayList typePaths;

        for (Iterator j = keys.iterator(); j.hasNext(); prevPaths = typePaths) {
            String fqn = (String) j.next();

            typePaths = Lists.newArrayList(fqn.split("\\."));

            int i;

            for (i = 0; i < Math.min(typePaths.size(), prevPaths.size()) && ((String) typePaths.get(i)).equals(prevPaths.get(i)); ++i) {
                ;
            }

            int className;

            for (className = prevPaths.size(); className > i; --className) {
                --indent;
                sb.append(Utils.repeat("\t", indent)).append("}\n");
            }

            for (className = i; className < typePaths.size() - 1; ++className) {
                sb.append(Utils.repeat("\t", indent++)).append("public interface ").append(this.getNonDuplicateName((String) typePaths.get(className), typePaths, className)).append(" {\n");
            }

            String s = (String) typePaths.get(typePaths.size() - 1);
            ArrayList annotations = Lists.newArrayList();
            ArrayList fields = Lists.newArrayList();
            SetMultimap methods = Multimaps.newSetMultimap(new HashMap(), new Supplier() {
                public Set get() {
                    return Sets.newHashSet();
                }
            });
            Iterator iterator = reflections.getStore().get(TypeElementsScanner.class.getSimpleName(), new String[] { fqn}).iterator();

            String annotation;
            String normalized;
            String methodName;

            while (iterator.hasNext()) {
                annotation = (String) iterator.next();
                if (annotation.startsWith("@")) {
                    annotations.add(annotation.substring(1));
                } else if (annotation.contains("(")) {
                    if (!annotation.startsWith("<")) {
                        int nonDuplicateName = annotation.indexOf(40);

                        normalized = annotation.substring(0, nonDuplicateName);
                        methodName = annotation.substring(nonDuplicateName + 1, annotation.indexOf(")"));
                        String namesDescriptor = "";

                        if (methodName.length() != 0) {
                            namesDescriptor = "_" + methodName.replace(".", "_").replace(", ", "__").replace("[]", "$$");
                        }

                        String normalized1 = normalized + namesDescriptor;

                        methods.put(normalized, normalized1);
                    }
                } else if (!Utils.isEmpty(annotation)) {
                    fields.add(annotation);
                }
            }

            sb.append(Utils.repeat("\t", indent++)).append("public interface ").append(this.getNonDuplicateName(s, typePaths, typePaths.size() - 1)).append(" {\n");
            if (!fields.isEmpty()) {
                sb.append(Utils.repeat("\t", indent++)).append("public interface fields {\n");
                iterator = fields.iterator();

                while (iterator.hasNext()) {
                    annotation = (String) iterator.next();
                    sb.append(Utils.repeat("\t", indent)).append("public interface ").append(this.getNonDuplicateName(annotation, typePaths)).append(" {}\n");
                }

                --indent;
                sb.append(Utils.repeat("\t", indent)).append("}\n");
            }

            String s1;

            if (!methods.isEmpty()) {
                sb.append(Utils.repeat("\t", indent++)).append("public interface methods {\n");
                iterator = methods.entries().iterator();

                while (iterator.hasNext()) {
                    Entry entry = (Entry) iterator.next();

                    s1 = (String) entry.getKey();
                    normalized = (String) entry.getValue();
                    methodName = methods.get(s1).size() == 1 ? s1 : normalized;
                    methodName = this.getNonDuplicateName(methodName, fields);
                    sb.append(Utils.repeat("\t", indent)).append("public interface ").append(this.getNonDuplicateName(methodName, typePaths)).append(" {}\n");
                }

                --indent;
                sb.append(Utils.repeat("\t", indent)).append("}\n");
            }

            if (!annotations.isEmpty()) {
                sb.append(Utils.repeat("\t", indent++)).append("public interface annotations {\n");
                iterator = annotations.iterator();

                while (iterator.hasNext()) {
                    annotation = (String) iterator.next();
                    s1 = this.getNonDuplicateName(annotation, typePaths);
                    sb.append(Utils.repeat("\t", indent)).append("public interface ").append(s1).append(" {}\n");
                }

                --indent;
                sb.append(Utils.repeat("\t", indent)).append("}\n");
            }
        }

        for (int i = prevPaths.size(); i >= 1; --i) {
            sb.append(Utils.repeat("\t", i)).append("}\n");
        }

        return sb.toString();
    }

    private String getNonDuplicateName(String candidate, List prev, int offset) {
        String normalized = this.normalize(candidate);

        for (int i = 0; i < offset; ++i) {
            if (normalized.equals(prev.get(i))) {
                return this.getNonDuplicateName(normalized + "_", prev, offset);
            }
        }

        return normalized;
    }

    private String normalize(String candidate) {
        return candidate.replace(".", "_");
    }

    private String getNonDuplicateName(String candidate, List prev) {
        return this.getNonDuplicateName(candidate, prev, prev.size());
    }

    public static Class resolveClassOf(Class element) throws ClassNotFoundException {
        Class cursor = element;

        LinkedList ognl;

        for (ognl = Lists.newLinkedList(); cursor != null; cursor = cursor.getDeclaringClass()) {
            ognl.addFirst(cursor.getSimpleName());
        }

        String classOgnl = Joiner.on(".").join(ognl.subList(1, ognl.size())).replace(".$", "$");

        return Class.forName(classOgnl);
    }

    public static Class resolveClass(Class aClass) {
        try {
            return resolveClassOf(aClass);
        } catch (Exception exception) {
            throw new ReflectionsException("could not resolve to class " + aClass.getName(), exception);
        }
    }

    public static Field resolveField(Class aField) {
        try {
            String e = aField.getSimpleName();
            Class declaringClass = aField.getDeclaringClass().getDeclaringClass();

            return resolveClassOf(declaringClass).getDeclaredField(e);
        } catch (Exception exception) {
            throw new ReflectionsException("could not resolve to field " + aField.getName(), exception);
        }
    }

    public static Annotation resolveAnnotation(Class annotation) {
        try {
            String e = annotation.getSimpleName().replace("_", ".");
            Class declaringClass = annotation.getDeclaringClass().getDeclaringClass();
            Class aClass = resolveClassOf(declaringClass);
            Class aClass1 = ReflectionUtils.forName(e, new ClassLoader[0]);
            Annotation annotation1 = aClass.getAnnotation(aClass1);

            return annotation1;
        } catch (Exception exception) {
            throw new ReflectionsException("could not resolve to annotation " + annotation.getName(), exception);
        }
    }

    public static Method resolveMethod(Class aMethod) {
        String methodOgnl = aMethod.getSimpleName();

        try {
            String e;
            Class[] nameTypes;

            if (methodOgnl.contains("_")) {
                e = methodOgnl.substring(0, methodOgnl.indexOf("_"));
                String[] declaringClass = methodOgnl.substring(methodOgnl.indexOf("_") + 1).split("__");

                nameTypes = new Class[declaringClass.length];

                for (int i = 0; i < declaringClass.length; ++i) {
                    String typeName = declaringClass[i].replace("$$", "[]").replace("_", ".");

                    nameTypes[i] = ReflectionUtils.forName(typeName, new ClassLoader[0]);
                }
            } else {
                e = methodOgnl;
                nameTypes = null;
            }

            Class oclass = aMethod.getDeclaringClass().getDeclaringClass();

            return resolveClassOf(oclass).getDeclaredMethod(e, nameTypes);
        } catch (Exception exception) {
            throw new ReflectionsException("could not resolve to method " + aMethod.getName(), exception);
        }
    }
}
