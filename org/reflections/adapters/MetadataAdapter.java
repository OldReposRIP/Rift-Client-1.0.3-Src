package org.reflections.adapters;

import java.util.List;
import org.reflections.vfs.Vfs;

public interface MetadataAdapter {

    String getClassName(Object object);

    String getSuperclassName(Object object);

    List getInterfacesNames(Object object);

    List getFields(Object object);

    List getMethods(Object object);

    String getMethodName(Object object);

    List getParameterNames(Object object);

    List getClassAnnotationNames(Object object);

    List getFieldAnnotationNames(Object object);

    List getMethodAnnotationNames(Object object);

    List getParameterAnnotationNames(Object object, int i);

    String getReturnTypeName(Object object);

    String getFieldName(Object object);

    Object getOfCreateClassObject(Vfs.File vfs_file) throws Exception;

    String getMethodModifier(Object object);

    String getMethodKey(Object object, Object object1);

    String getMethodFullKey(Object object, Object object1);

    boolean isPublic(Object object);

    boolean acceptsInput(String s);
}
