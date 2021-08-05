package javassist.bytecode.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class Type {

    private final CtClass clazz;
    private final boolean special;
    private static final Map prims = new IdentityHashMap();
    public static final Type DOUBLE = new Type(CtClass.doubleType);
    public static final Type BOOLEAN = new Type(CtClass.booleanType);
    public static final Type LONG = new Type(CtClass.longType);
    public static final Type CHAR = new Type(CtClass.charType);
    public static final Type BYTE = new Type(CtClass.byteType);
    public static final Type SHORT = new Type(CtClass.shortType);
    public static final Type INTEGER = new Type(CtClass.intType);
    public static final Type FLOAT = new Type(CtClass.floatType);
    public static final Type VOID = new Type(CtClass.voidType);
    public static final Type UNINIT = new Type((CtClass) null);
    public static final Type RETURN_ADDRESS = new Type((CtClass) null, true);
    public static final Type TOP = new Type((CtClass) null, true);
    public static final Type BOGUS = new Type((CtClass) null, true);
    public static final Type OBJECT = lookupType("java.lang.Object");
    public static final Type SERIALIZABLE = lookupType("java.io.Serializable");
    public static final Type CLONEABLE = lookupType("java.lang.Cloneable");
    public static final Type THROWABLE = lookupType("java.lang.Throwable");

    public static Type get(CtClass clazz) {
        Type type = (Type) Type.prims.get(clazz);

        return type != null ? type : new Type(clazz);
    }

    private static Type lookupType(String name) {
        try {
            return new Type(ClassPool.getDefault().get(name));
        } catch (NotFoundException notfoundexception) {
            throw new RuntimeException(notfoundexception);
        }
    }

    Type(CtClass clazz) {
        this(clazz, false);
    }

    private Type(CtClass clazz, boolean special) {
        this.clazz = clazz;
        this.special = special;
    }

    boolean popChanged() {
        return false;
    }

    public int getSize() {
        return this.clazz != CtClass.doubleType && this.clazz != CtClass.longType && this != Type.TOP ? 1 : 2;
    }

    public CtClass getCtClass() {
        return this.clazz;
    }

    public boolean isReference() {
        return !this.special && (this.clazz == null || !this.clazz.isPrimitive());
    }

    public boolean isSpecial() {
        return this.special;
    }

    public boolean isArray() {
        return this.clazz != null && this.clazz.isArray();
    }

    public int getDimensions() {
        if (!this.isArray()) {
            return 0;
        } else {
            String name = this.clazz.getName();
            int pos = name.length() - 1;

            int count;

            for (count = 0; name.charAt(pos) == 93; ++count) {
                pos -= 2;
            }

            return count;
        }
    }

    public Type getComponent() {
        if (this.clazz != null && this.clazz.isArray()) {
            CtClass component;

            try {
                component = this.clazz.getComponentType();
            } catch (NotFoundException notfoundexception) {
                throw new RuntimeException(notfoundexception);
            }

            Type type = (Type) Type.prims.get(component);

            return type != null ? type : new Type(component);
        } else {
            return null;
        }
    }

    public boolean isAssignableFrom(Type type) {
        if (this == type) {
            return true;
        } else if ((type != Type.UNINIT || !this.isReference()) && (this != Type.UNINIT || !type.isReference())) {
            if (type instanceof MultiType) {
                return ((MultiType) type).isAssignableTo(this);
            } else if (type instanceof MultiArrayType) {
                return ((MultiArrayType) type).isAssignableTo(this);
            } else if (this.clazz != null && !this.clazz.isPrimitive()) {
                try {
                    return type.clazz.subtypeOf(this.clazz);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public Type merge(Type type) {
        if (type == this) {
            return this;
        } else if (type == null) {
            return this;
        } else if (type == Type.UNINIT) {
            return this;
        } else if (this == Type.UNINIT) {
            return type;
        } else if (type.isReference() && this.isReference()) {
            if (type instanceof MultiType) {
                return type.merge(this);
            } else if (type.isArray() && this.isArray()) {
                return this.mergeArray(type);
            } else {
                try {
                    return this.mergeClasses(type);
                } catch (NotFoundException notfoundexception) {
                    throw new RuntimeException(notfoundexception);
                }
            }
        } else {
            return Type.BOGUS;
        }
    }

    Type getRootComponent(Type type) {
        while (type.isArray()) {
            type = type.getComponent();
        }

        return type;
    }

    private Type createArray(Type rootComponent, int dims) {
        if (rootComponent instanceof MultiType) {
            return new MultiArrayType((MultiType) rootComponent, dims);
        } else {
            String name = this.arrayName(rootComponent.clazz.getName(), dims);

            try {
                Type type = get(this.getClassPool(rootComponent).get(name));

                return type;
            } catch (NotFoundException notfoundexception) {
                throw new RuntimeException(notfoundexception);
            }
        }
    }

    String arrayName(String component, int dims) {
        int i = component.length();
        int size = i + dims * 2;
        char[] string = new char[size];

        component.getChars(0, i, string, 0);

        while (i < size) {
            string[i++] = 91;
            string[i++] = 93;
        }

        component = new String(string);
        return component;
    }

    private ClassPool getClassPool(Type rootComponent) {
        ClassPool pool = rootComponent.clazz.getClassPool();

        return pool != null ? pool : ClassPool.getDefault();
    }

    private Type mergeArray(Type type) {
        Type typeRoot = this.getRootComponent(type);
        Type thisRoot = this.getRootComponent(this);
        int typeDims = type.getDimensions();
        int thisDims = this.getDimensions();
        Type targetRoot;

        if (typeDims == thisDims) {
            targetRoot = thisRoot.merge(typeRoot);
            return targetRoot == Type.BOGUS ? Type.OBJECT : this.createArray(targetRoot, thisDims);
        } else {
            int targetDims;

            if (typeDims < thisDims) {
                targetRoot = typeRoot;
                targetDims = typeDims;
            } else {
                targetRoot = thisRoot;
                targetDims = thisDims;
            }

            return !eq(Type.CLONEABLE.clazz, targetRoot.clazz) && !eq(Type.SERIALIZABLE.clazz, targetRoot.clazz) ? this.createArray(Type.OBJECT, targetDims) : this.createArray(targetRoot, targetDims);
        }
    }

    private static CtClass findCommonSuperClass(CtClass one, CtClass two) throws NotFoundException {
        CtClass deep = one;
        CtClass shallow = two;
        CtClass backupDeep = one;

        while (true) {
            if (eq(deep, shallow) && deep.getSuperclass() != null) {
                return deep;
            }

            CtClass deepSuper = deep.getSuperclass();
            CtClass shallowSuper = shallow.getSuperclass();

            if (shallowSuper == null) {
                shallow = two;
                break;
            }

            if (deepSuper == null) {
                backupDeep = two;
                deep = shallow;
                shallow = one;
                break;
            }

            deep = deepSuper;
            shallow = shallowSuper;
        }

        while (true) {
            deep = deep.getSuperclass();
            if (deep == null) {
                for (deep = backupDeep; !eq(deep, shallow); shallow = shallow.getSuperclass()) {
                    deep = deep.getSuperclass();
                }

                return deep;
            }

            backupDeep = backupDeep.getSuperclass();
        }
    }

    private Type mergeClasses(Type type) throws NotFoundException {
        CtClass superClass = findCommonSuperClass(this.clazz, type.clazz);
        Map commonDeclared;

        if (superClass.getSuperclass() == null) {
            commonDeclared = this.findCommonInterfaces(type);
            return (Type) (commonDeclared.size() == 1 ? new Type((CtClass) commonDeclared.values().iterator().next()) : (commonDeclared.size() > 1 ? new MultiType(commonDeclared) : new Type(superClass)));
        } else {
            commonDeclared = this.findExclusiveDeclaredInterfaces(type, superClass);
            return (Type) (commonDeclared.size() > 0 ? new MultiType(commonDeclared, new Type(superClass)) : new Type(superClass));
        }
    }

    private Map findCommonInterfaces(Type type) {
        Map typeMap = this.getAllInterfaces(type.clazz, (Map) null);
        Map thisMap = this.getAllInterfaces(this.clazz, (Map) null);

        return this.findCommonInterfaces(typeMap, thisMap);
    }

    private Map findExclusiveDeclaredInterfaces(Type type, CtClass exclude) {
        Map typeMap = this.getDeclaredInterfaces(type.clazz, (Map) null);
        Map thisMap = this.getDeclaredInterfaces(this.clazz, (Map) null);
        Map excludeMap = this.getAllInterfaces(exclude, (Map) null);
        Iterator i = excludeMap.keySet().iterator();

        while (i.hasNext()) {
            Object intf = i.next();

            typeMap.remove(intf);
            thisMap.remove(intf);
        }

        return this.findCommonInterfaces(typeMap, thisMap);
    }

    Map findCommonInterfaces(Map typeMap, Map alterMap) {
        Iterator i = alterMap.keySet().iterator();

        while (i.hasNext()) {
            if (!typeMap.containsKey(i.next())) {
                i.remove();
            }
        }

        i = (new ArrayList(alterMap.values())).iterator();

        while (i.hasNext()) {
            CtClass intf = (CtClass) i.next();

            CtClass[] interfaces;

            try {
                interfaces = intf.getInterfaces();
            } catch (NotFoundException notfoundexception) {
                throw new RuntimeException(notfoundexception);
            }

            for (int c = 0; c < interfaces.length; ++c) {
                alterMap.remove(interfaces[c].getName());
            }
        }

        return alterMap;
    }

    Map getAllInterfaces(CtClass clazz, Map map) {
        if (map == null) {
            map = new HashMap();
        }

        if (clazz.isInterface()) {
            ((Map) map).put(clazz.getName(), clazz);
        }

        do {
            try {
                CtClass[] e = clazz.getInterfaces();

                for (int i = 0; i < e.length; ++i) {
                    CtClass intf = e[i];

                    ((Map) map).put(intf.getName(), intf);
                    this.getAllInterfaces(intf, (Map) map);
                }

                clazz = clazz.getSuperclass();
            } catch (NotFoundException notfoundexception) {
                throw new RuntimeException(notfoundexception);
            }
        } while (clazz != null);

        return (Map) map;
    }

    Map getDeclaredInterfaces(CtClass clazz, Map map) {
        if (map == null) {
            map = new HashMap();
        }

        if (clazz.isInterface()) {
            ((Map) map).put(clazz.getName(), clazz);
        }

        CtClass[] interfaces;

        try {
            interfaces = clazz.getInterfaces();
        } catch (NotFoundException notfoundexception) {
            throw new RuntimeException(notfoundexception);
        }

        for (int i = 0; i < interfaces.length; ++i) {
            CtClass intf = interfaces[i];

            ((Map) map).put(intf.getName(), intf);
            this.getDeclaredInterfaces(intf, (Map) map);
        }

        return (Map) map;
    }

    public boolean equals(Object o) {
        return !(o instanceof Type) ? false : o.getClass() == this.getClass() && eq(this.clazz, ((Type) o).clazz);
    }

    static boolean eq(CtClass one, CtClass two) {
        return one == two || one != null && two != null && one.getName().equals(two.getName());
    }

    public String toString() {
        return this == Type.BOGUS ? "BOGUS" : (this == Type.UNINIT ? "UNINIT" : (this == Type.RETURN_ADDRESS ? "RETURN ADDRESS" : (this == Type.TOP ? "TOP" : (this.clazz == null ? "null" : this.clazz.getName()))));
    }

    static {
        Type.prims.put(CtClass.doubleType, Type.DOUBLE);
        Type.prims.put(CtClass.longType, Type.LONG);
        Type.prims.put(CtClass.charType, Type.CHAR);
        Type.prims.put(CtClass.shortType, Type.SHORT);
        Type.prims.put(CtClass.intType, Type.INTEGER);
        Type.prims.put(CtClass.floatType, Type.FLOAT);
        Type.prims.put(CtClass.byteType, Type.BYTE);
        Type.prims.put(CtClass.booleanType, Type.BOOLEAN);
        Type.prims.put(CtClass.voidType, Type.VOID);
    }
}
