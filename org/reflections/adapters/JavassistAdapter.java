package org.reflections.adapters;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import org.reflections.ReflectionsException;
import org.reflections.util.Utils;
import org.reflections.vfs.Vfs;

public class JavassistAdapter implements MetadataAdapter {

    public static boolean includeInvisibleTag = true;

    public List getFields(ClassFile cls) {
        return cls.getFields();
    }

    public List getMethods(ClassFile cls) {
        return cls.getMethods();
    }

    public String getMethodName(MethodInfo method) {
        return method.getName();
    }

    public List getParameterNames(MethodInfo method) {
        String descriptor = method.getDescriptor();

        descriptor = descriptor.substring(descriptor.indexOf("(") + 1, descriptor.lastIndexOf(")"));
        return this.splitDescriptorToTypeNames(descriptor);
    }

    public List getClassAnnotationNames(ClassFile aClass) {
        return this.getAnnotationNames(new AnnotationsAttribute[] { (AnnotationsAttribute) aClass.getAttribute("RuntimeVisibleAnnotations"), JavassistAdapter.includeInvisibleTag ? (AnnotationsAttribute) aClass.getAttribute("RuntimeInvisibleAnnotations") : null});
    }

    public List getFieldAnnotationNames(FieldInfo field) {
        return this.getAnnotationNames(new AnnotationsAttribute[] { (AnnotationsAttribute) field.getAttribute("RuntimeVisibleAnnotations"), JavassistAdapter.includeInvisibleTag ? (AnnotationsAttribute) field.getAttribute("RuntimeInvisibleAnnotations") : null});
    }

    public List getMethodAnnotationNames(MethodInfo method) {
        return this.getAnnotationNames(new AnnotationsAttribute[] { (AnnotationsAttribute) method.getAttribute("RuntimeVisibleAnnotations"), JavassistAdapter.includeInvisibleTag ? (AnnotationsAttribute) method.getAttribute("RuntimeInvisibleAnnotations") : null});
    }

    public List getParameterAnnotationNames(MethodInfo method, int nameeterIndex) {
        ArrayList result = Lists.newArrayList();
        ArrayList nameeterAnnotationsAttributes = Lists.newArrayList(new ParameterAnnotationsAttribute[] { (ParameterAnnotationsAttribute) method.getAttribute("RuntimeVisibleParameterAnnotations"), (ParameterAnnotationsAttribute) method.getAttribute("RuntimeInvisibleParameterAnnotations")});

        if (nameeterAnnotationsAttributes != null) {
            Iterator iterator = nameeterAnnotationsAttributes.iterator();

            while (iterator.hasNext()) {
                ParameterAnnotationsAttribute nameeterAnnotationsAttribute = (ParameterAnnotationsAttribute) iterator.next();

                if (nameeterAnnotationsAttribute != null) {
                    Annotation[][] annotations = nameeterAnnotationsAttribute.getAnnotations();

                    if (nameeterIndex < annotations.length) {
                        Annotation[] annotation = annotations[nameeterIndex];

                        result.addAll(this.getAnnotationNames(annotation));
                    }
                }
            }
        }

        return result;
    }

    public String getReturnTypeName(MethodInfo method) {
        String descriptor = method.getDescriptor();

        descriptor = descriptor.substring(descriptor.lastIndexOf(")") + 1);
        return (String) this.splitDescriptorToTypeNames(descriptor).get(0);
    }

    public String getFieldName(FieldInfo field) {
        return field.getName();
    }

    public ClassFile getOfCreateClassObject(Vfs.File file) {
        InputStream inputStream = null;

        ClassFile classfile;

        try {
            inputStream = file.openInputStream();
            DataInputStream e = new DataInputStream(new BufferedInputStream(inputStream));

            classfile = new ClassFile(e);
        } catch (IOException ioexception) {
            throw new ReflectionsException("could not create class file from " + file.getName(), ioexception);
        } finally {
            Utils.close(inputStream);
        }

        return classfile;
    }

    public String getMethodModifier(MethodInfo method) {
        int accessFlags = method.getAccessFlags();

        return AccessFlag.isPrivate(accessFlags) ? "private" : (AccessFlag.isProtected(accessFlags) ? "protected" : (this.isPublic(Integer.valueOf(accessFlags)) ? "public" : ""));
    }

    public String getMethodKey(ClassFile cls, MethodInfo method) {
        return this.getMethodName(method) + "(" + Joiner.on(", ").join(this.getParameterNames(method)) + ")";
    }

    public String getMethodFullKey(ClassFile cls, MethodInfo method) {
        return this.getClassName(cls) + "." + this.getMethodKey(cls, method);
    }

    public boolean isPublic(Object o) {
        Integer accessFlags = Integer.valueOf(o instanceof ClassFile ? ((ClassFile) o).getAccessFlags() : (o instanceof FieldInfo ? ((FieldInfo) o).getAccessFlags() : (o instanceof MethodInfo ? Integer.valueOf(((MethodInfo) o).getAccessFlags()) : null).intValue()));

        return accessFlags != null && AccessFlag.isPublic(accessFlags.intValue());
    }

    public String getClassName(ClassFile cls) {
        return cls.getName();
    }

    public String getSuperclassName(ClassFile cls) {
        return cls.getSuperclass();
    }

    public List getInterfacesNames(ClassFile cls) {
        return Arrays.asList(cls.getInterfaces());
    }

    public boolean acceptsInput(String file) {
        return file.endsWith(".class");
    }

    private List getAnnotationNames(AnnotationsAttribute... annotationsAttributes) {
        ArrayList result = Lists.newArrayList();

        if (annotationsAttributes != null) {
            AnnotationsAttribute[] aannotationsattribute = annotationsAttributes;
            int i = annotationsAttributes.length;

            for (int j = 0; j < i; ++j) {
                AnnotationsAttribute annotationsAttribute = aannotationsattribute[j];

                if (annotationsAttribute != null) {
                    Annotation[] aannotation = annotationsAttribute.getAnnotations();
                    int k = aannotation.length;

                    for (int l = 0; l < k; ++l) {
                        Annotation annotation = aannotation[l];

                        result.add(annotation.getTypeName());
                    }
                }
            }
        }

        return result;
    }

    private List getAnnotationNames(Annotation[] annotations) {
        ArrayList result = Lists.newArrayList();
        Annotation[] aannotation = annotations;
        int i = annotations.length;

        for (int j = 0; j < i; ++j) {
            Annotation annotation = aannotation[j];

            result.add(annotation.getTypeName());
        }

        return result;
    }

    private List splitDescriptorToTypeNames(String descriptors) {
        ArrayList result = Lists.newArrayList();

        if (descriptors != null && descriptors.length() != 0) {
            ArrayList indices = Lists.newArrayList();
            Descriptor.Iterator iterator = new Descriptor.Iterator(descriptors);

            while (iterator.hasNext()) {
                indices.add(Integer.valueOf(iterator.next()));
            }

            indices.add(Integer.valueOf(descriptors.length()));

            for (int i = 0; i < indices.size() - 1; ++i) {
                String s1 = Descriptor.toString(descriptors.substring(((Integer) indices.get(i)).intValue(), ((Integer) indices.get(i + 1)).intValue()));

                result.add(s1);
            }
        }

        return result;
    }
}
