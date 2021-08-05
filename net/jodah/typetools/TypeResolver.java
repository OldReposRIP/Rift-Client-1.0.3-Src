package net.jodah.typetools;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import sun.misc.Unsafe;

public final class TypeResolver {

    private static final Map TYPE_VARIABLE_CACHE = Collections.synchronizedMap(new WeakHashMap());
    private static volatile boolean CACHE_ENABLED = true;
    private static boolean RESOLVES_LAMBDAS;
    private static Method GET_CONSTANT_POOL;
    private static Method GET_CONSTANT_POOL_SIZE;
    private static Method GET_CONSTANT_POOL_METHOD_AT;
    private static final Map OBJECT_METHODS = new HashMap();
    private static final Map PRIMITIVE_WRAPPERS;
    private static final Double JAVA_VERSION = Double.valueOf(Double.parseDouble(System.getProperty("java.specification.version", "0")));

    public static void enableCache() {
        TypeResolver.CACHE_ENABLED = true;
    }

    public static void disableCache() {
        TypeResolver.TYPE_VARIABLE_CACHE.clear();
        TypeResolver.CACHE_ENABLED = false;
    }

    public static Class resolveRawArgument(Class type, Class subType) {
        return resolveRawArgument(resolveGenericType(type, subType), subType);
    }

    public static Class resolveRawArgument(Type genericType, Class subType) {
        Class[] arguments = resolveRawArguments(genericType, subType);

        if (arguments == null) {
            return TypeResolver.Unknown.class;
        } else if (arguments.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument for generic type " + genericType + " but found " + arguments.length);
        } else {
            return arguments[0];
        }
    }

    public static Class[] resolveRawArguments(Class type, Class subType) {
        return resolveRawArguments(resolveGenericType(type, subType), subType);
    }

    public static Class[] resolveRawArguments(Type genericType, Class subType) {
        Class[] result = null;
        Class functionalInterface = null;

        if (TypeResolver.RESOLVES_LAMBDAS && subType.isSynthetic()) {
            Class typeParams = genericType instanceof ParameterizedType && ((ParameterizedType) genericType).getRawType() instanceof Class ? (Class) ((ParameterizedType) genericType).getRawType() : (genericType instanceof Class ? (Class) genericType : null);

            if (typeParams != null && typeParams.isInterface()) {
                functionalInterface = typeParams;
            }
        }

        if (genericType instanceof ParameterizedType) {
            ParameterizedType nameeterizedtype = (ParameterizedType) genericType;
            Type[] i = nameeterizedtype.getActualTypeArguments();

            result = new Class[i.length];

            for (int i1 = 0; i1 < i.length; ++i1) {
                result[i1] = resolveRawClass(i[i1], subType, functionalInterface);
            }
        } else if (genericType instanceof TypeVariable) {
            result = new Class[] { resolveRawClass(genericType, subType, functionalInterface)};
        } else if (genericType instanceof Class) {
            TypeVariable[] atypevariable = ((Class) genericType).getTypeParameters();

            result = new Class[atypevariable.length];

            for (int i = 0; i < atypevariable.length; ++i) {
                result[i] = resolveRawClass(atypevariable[i], subType, functionalInterface);
            }
        }

        return result;
    }

    public static Type resolveGenericType(Class type, Type subType) {
        Class rawType;

        if (subType instanceof ParameterizedType) {
            rawType = (Class) ((ParameterizedType) subType).getRawType();
        } else {
            rawType = (Class) subType;
        }

        if (type.equals(rawType)) {
            return subType;
        } else {
            Type result;

            if (type.isInterface()) {
                Type[] superClass = rawType.getGenericInterfaces();
                int i = superClass.length;

                for (int j = 0; j < i; ++j) {
                    Type superInterface = superClass[j];

                    if (superInterface != null && !superInterface.equals(Object.class) && (result = resolveGenericType(type, superInterface)) != null) {
                        return result;
                    }
                }
            }

            Type type = rawType.getGenericSuperclass();

            return type != null && !type.equals(Object.class) && (result = resolveGenericType(type, type)) != null ? result : null;
        }
    }

    public static Class resolveRawClass(Type genericType, Class subType) {
        return resolveRawClass(genericType, subType, (Class) null);
    }

    private static Class resolveRawClass(Type genericType, Class subType, Class functionalInterface) {
        if (genericType instanceof Class) {
            return (Class) genericType;
        } else if (genericType instanceof ParameterizedType) {
            return resolveRawClass(((ParameterizedType) genericType).getRawType(), subType, functionalInterface);
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType genericarraytype = (GenericArrayType) genericType;
            Class component = resolveRawClass(genericarraytype.getGenericComponentType(), subType, functionalInterface);

            return Array.newInstance(component, 0).getClass();
        } else {
            if (genericType instanceof TypeVariable) {
                TypeVariable typevariable = (TypeVariable) genericType;
                Type genericType1 = (Type) getTypeVariableMap(subType, functionalInterface).get(typevariable);

                genericType = genericType1 == null ? resolveBound(typevariable) : resolveRawClass(genericType1, subType, functionalInterface);
            }

            return genericType instanceof Class ? (Class) genericType : TypeResolver.Unknown.class;
        }
    }

    private static Map getTypeVariableMap(Class targetType, Class functionalInterface) {
        Reference ref = (Reference) TypeResolver.TYPE_VARIABLE_CACHE.get(targetType);
        Object map = ref != null ? (Map) ref.get() : null;

        if (map == null) {
            map = new HashMap();
            if (functionalInterface != null) {
                populateLambdaArgs(functionalInterface, targetType, (Map) map);
            }

            populateSuperTypeArgs(targetType.getGenericInterfaces(), (Map) map, functionalInterface != null);
            Type genericType = targetType.getGenericSuperclass();

            Class type;

            for (type = targetType.getSuperclass(); type != null && !Object.class.equals(type); type = type.getSuperclass()) {
                if (genericType instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType) genericType, (Map) map, false);
                }

                populateSuperTypeArgs(type.getGenericInterfaces(), (Map) map, false);
                genericType = type.getGenericSuperclass();
            }

            for (type = targetType; type.isMemberClass(); type = type.getEnclosingClass()) {
                genericType = type.getGenericSuperclass();
                if (genericType instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType) genericType, (Map) map, functionalInterface != null);
                }
            }

            if (TypeResolver.CACHE_ENABLED) {
                TypeResolver.TYPE_VARIABLE_CACHE.put(targetType, new WeakReference(map));
            }
        }

        return (Map) map;
    }

    private static void populateSuperTypeArgs(Type[] types, Map map, boolean depthFirst) {
        Type[] atype = types;
        int i = types.length;

        for (int j = 0; j < i; ++j) {
            Type type = atype[j];

            if (type instanceof ParameterizedType) {
                ParameterizedType nameeterizedType = (ParameterizedType) type;

                if (!depthFirst) {
                    populateTypeArgs(nameeterizedType, map, depthFirst);
                }

                Type rawType = nameeterizedType.getRawType();

                if (rawType instanceof Class) {
                    populateSuperTypeArgs(((Class) rawType).getGenericInterfaces(), map, depthFirst);
                }

                if (depthFirst) {
                    populateTypeArgs(nameeterizedType, map, depthFirst);
                }
            } else if (type instanceof Class) {
                populateSuperTypeArgs(((Class) type).getGenericInterfaces(), map, depthFirst);
            }
        }

    }

    private static void populateTypeArgs(ParameterizedType type, Map map, boolean depthFirst) {
        if (type.getRawType() instanceof Class) {
            TypeVariable[] typeVariables = ((Class) type.getRawType()).getTypeParameters();
            Type[] typeArguments = type.getActualTypeArguments();

            if (type.getOwnerType() != null) {
                Type i = type.getOwnerType();

                if (i instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType) i, map, depthFirst);
                }
            }

            for (int i = 0; i < typeArguments.length; ++i) {
                TypeVariable typevariable = typeVariables[i];
                Type typeArgument = typeArguments[i];

                if (typeArgument instanceof Class) {
                    map.put(typevariable, typeArgument);
                } else if (typeArgument instanceof GenericArrayType) {
                    map.put(typevariable, typeArgument);
                } else if (typeArgument instanceof ParameterizedType) {
                    map.put(typevariable, typeArgument);
                } else if (typeArgument instanceof TypeVariable) {
                    TypeVariable typeVariableArgument = (TypeVariable) typeArgument;
                    Type resolvedType;

                    if (depthFirst) {
                        resolvedType = (Type) map.get(typevariable);
                        if (resolvedType != null) {
                            map.put(typeVariableArgument, resolvedType);
                            continue;
                        }
                    }

                    resolvedType = (Type) map.get(typeVariableArgument);
                    if (resolvedType == null) {
                        resolvedType = resolveBound(typeVariableArgument);
                    }

                    map.put(typevariable, resolvedType);
                }
            }
        }

    }

    public static Type resolveBound(TypeVariable typeVariable) {
        Type[] bounds = typeVariable.getBounds();

        if (bounds.length == 0) {
            return TypeResolver.Unknown.class;
        } else {
            Type bound = bounds[0];

            if (bound instanceof TypeVariable) {
                bound = resolveBound((TypeVariable) bound);
            }

            return (Type) (bound == Object.class ? TypeResolver.Unknown.class : bound);
        }
    }

    private static void populateLambdaArgs(Class functionalInterface, Class lambdaType, Map map) {
        if (TypeResolver.RESOLVES_LAMBDAS) {
            Method[] amethod = functionalInterface.getMethods();
            int i = amethod.length;

            for (int j = 0; j < i; ++j) {
                Method m = amethod[j];

                if (!isDefaultMethod(m) && !Modifier.isStatic(m.getModifiers()) && !m.isBridge()) {
                    Method objectMethod = (Method) TypeResolver.OBJECT_METHODS.get(m.getName());

                    if (objectMethod == null || !Arrays.equals(m.getTypeParameters(), objectMethod.getTypeParameters())) {
                        Type returnTypeVar = m.getGenericReturnType();
                        Type[] nameTypeVars = m.getGenericParameterTypes();
                        Member member = getMemberRef(lambdaType);

                        if (member == null) {
                            return;
                        }

                        if (returnTypeVar instanceof TypeVariable) {
                            Class arguments = member instanceof Method ? ((Method) member).getReturnType() : ((Constructor) member).getDeclaringClass();

                            arguments = wrapPrimitives(arguments);
                            if (!arguments.equals(Void.class)) {
                                map.put((TypeVariable) returnTypeVar, arguments);
                            }
                        }

                        Class[] aclass = member instanceof Method ? ((Method) member).getParameterTypes() : ((Constructor) member).getParameterTypes();
                        byte nameOffset = 0;

                        if (nameTypeVars.length > 0 && nameTypeVars[0] instanceof TypeVariable && nameTypeVars.length == aclass.length + 1) {
                            Class argOffset = member.getDeclaringClass();

                            map.put((TypeVariable) nameTypeVars[0], argOffset);
                            nameOffset = 1;
                        }

                        int k = 0;

                        if (nameTypeVars.length < aclass.length) {
                            k = aclass.length - nameTypeVars.length;
                        }

                        for (int i = 0; i + k < aclass.length; ++i) {
                            if (nameTypeVars[i] instanceof TypeVariable) {
                                map.put((TypeVariable) nameTypeVars[i + nameOffset], wrapPrimitives(aclass[i + k]));
                            }
                        }

                        return;
                    }
                }
            }
        }

    }

    private static boolean isDefaultMethod(Method m) {
        return TypeResolver.JAVA_VERSION.doubleValue() >= 1.8D && m.isDefault();
    }

    private static Member getMemberRef(Class type) {
        Object constantPool;

        try {
            constantPool = TypeResolver.GET_CONSTANT_POOL.invoke(type, new Object[0]);
        } catch (Exception exception) {
            return null;
        }

        Member result = null;

        for (int i = getConstantPoolSize(constantPool) - 1; i >= 0; --i) {
            Member member = getConstantPoolMethodAt(constantPool, i);

            if (member != null && (!(member instanceof Constructor) || !member.getDeclaringClass().getName().equals("java.lang.invoke.SerializedLambda")) && !member.getDeclaringClass().isAssignableFrom(type)) {
                result = member;
                if (!(member instanceof Method) || !isAutoBoxingMethod((Method) member)) {
                    break;
                }
            }
        }

        return result;
    }

    private static boolean isAutoBoxingMethod(Method method) {
        Class[] nameeters = method.getParameterTypes();

        return method.getName().equals("valueOf") && nameeters.length == 1 && nameeters[0].isPrimitive() && wrapPrimitives(nameeters[0]).equals(method.getDeclaringClass());
    }

    private static Class wrapPrimitives(Class clazz) {
        return clazz.isPrimitive() ? (Class) TypeResolver.PRIMITIVE_WRAPPERS.get(clazz) : clazz;
    }

    private static int getConstantPoolSize(Object constantPool) {
        try {
            return ((Integer) TypeResolver.GET_CONSTANT_POOL_SIZE.invoke(constantPool, new Object[0])).intValue();
        } catch (Exception exception) {
            return 0;
        }
    }

    private static Member getConstantPoolMethodAt(Object constantPool, int i) {
        try {
            return (Member) TypeResolver.GET_CONSTANT_POOL_METHOD_AT.invoke(constantPool, new Object[] { Integer.valueOf(i)});
        } catch (Exception exception) {
            return null;
        }
    }

    static {
        try {
            Unsafe types = (Unsafe) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Unsafe run() throws Exception {
                    Field f = Unsafe.class.getDeclaredField("theUnsafe");

                    f.setAccessible(true);
                    return (Unsafe) f.get((Object) null);
                }
            });

            TypeResolver.GET_CONSTANT_POOL = Class.class.getDeclaredMethod("getConstantPool", new Class[0]);
            String constantPoolName = TypeResolver.JAVA_VERSION.doubleValue() < 9.0D ? "sun.reflect.ConstantPool" : "jdk.internal.reflect.ConstantPool";
            Class constantPoolClass = Class.forName(constantPoolName);

            TypeResolver.GET_CONSTANT_POOL_SIZE = constantPoolClass.getDeclaredMethod("getSize", new Class[0]);
            TypeResolver.GET_CONSTANT_POOL_METHOD_AT = constantPoolClass.getDeclaredMethod("getMethodAt", new Class[] { Integer.TYPE});
            Field overrideField = AccessibleObject.class.getDeclaredField("override");
            long overrideFieldOffset = types.objectFieldOffset(overrideField);

            types.putBoolean(TypeResolver.GET_CONSTANT_POOL, overrideFieldOffset, true);
            types.putBoolean(TypeResolver.GET_CONSTANT_POOL_SIZE, overrideFieldOffset, true);
            types.putBoolean(TypeResolver.GET_CONSTANT_POOL_METHOD_AT, overrideFieldOffset, true);
            Object constantPool = TypeResolver.GET_CONSTANT_POOL.invoke(Object.class, new Object[0]);

            TypeResolver.GET_CONSTANT_POOL_SIZE.invoke(constantPool, new Object[0]);
            Method[] amethod = Object.class.getDeclaredMethods();
            int i = amethod.length;

            for (int j = 0; j < i; ++j) {
                Method method = amethod[j];

                TypeResolver.OBJECT_METHODS.put(method.getName(), method);
            }

            TypeResolver.RESOLVES_LAMBDAS = true;
        } catch (Exception exception) {
            ;
        }

        HashMap hashmap = new HashMap();

        hashmap.put(Boolean.TYPE, Boolean.class);
        hashmap.put(Byte.TYPE, Byte.class);
        hashmap.put(Character.TYPE, Character.class);
        hashmap.put(Double.TYPE, Double.class);
        hashmap.put(Float.TYPE, Float.class);
        hashmap.put(Integer.TYPE, Integer.class);
        hashmap.put(Long.TYPE, Long.class);
        hashmap.put(Short.TYPE, Short.class);
        hashmap.put(Void.TYPE, Void.class);
        PRIMITIVE_WRAPPERS = Collections.unmodifiableMap(hashmap);
    }

    public static final class Unknown {

    }
}
