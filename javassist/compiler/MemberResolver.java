package javassist.compiler;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;
import javassist.compiler.ast.ASTList;
import javassist.compiler.ast.ASTree;
import javassist.compiler.ast.Declarator;
import javassist.compiler.ast.Keyword;
import javassist.compiler.ast.Symbol;

public class MemberResolver implements TokenId {

    private ClassPool classPool;
    private static final int YES = 0;
    private static final int NO = -1;
    private static final String INVALID = "<invalid>";
    private static WeakHashMap invalidNamesMap = new WeakHashMap();
    private Hashtable invalidNames = null;

    public MemberResolver(ClassPool cp) {
        this.classPool = cp;
    }

    public ClassPool getClassPool() {
        return this.classPool;
    }

    private static void fatal() throws CompileError {
        throw new CompileError("fatal");
    }

    public MemberResolver.Method lookupMethod(CtClass clazz, CtClass currentClass, MethodInfo current, String methodName, int[] argTypes, int[] argDims, String[] argClassNames) throws CompileError {
        MemberResolver.Method maybe = null;

        if (current != null && clazz == currentClass && current.getName().equals(methodName)) {
            int m = this.compareSignature(current.getDescriptor(), argTypes, argDims, argClassNames);

            if (m != -1) {
                MemberResolver.Method r = new MemberResolver.Method(clazz, current, m);

                if (m == 0) {
                    return r;
                }

                maybe = r;
            }
        }

        MemberResolver.Method m1 = this.lookupMethod(clazz, methodName, argTypes, argDims, argClassNames, maybe != null);

        return m1 != null ? m1 : maybe;
    }

    private MemberResolver.Method lookupMethod(CtClass clazz, String methodName, int[] argTypes, int[] argDims, String[] argClassNames, boolean onlyExact) throws CompileError {
        MemberResolver.Method maybe = null;
        ClassFile cf = clazz.getClassFile2();
        int pclazz;
        MemberResolver.Method r;

        if (cf != null) {
            List mod = cf.getMethods();
            int isIntf = mod.size();

            for (int ifs = 0; ifs < isIntf; ++ifs) {
                MethodInfo size = (MethodInfo) mod.get(ifs);

                if (size.getName().equals(methodName) && (size.getAccessFlags() & 64) == 0) {
                    pclazz = this.compareSignature(size.getDescriptor(), argTypes, argDims, argClassNames);
                    if (pclazz != -1) {
                        r = new MemberResolver.Method(clazz, size, pclazz);
                        if (pclazz == 0) {
                            return r;
                        }

                        if (maybe == null || maybe.notmatch > pclazz) {
                            maybe = r;
                        }
                    }
                }
            }
        }

        if (onlyExact) {
            maybe = null;
        } else if (maybe != null) {
            return maybe;
        }

        int i = clazz.getModifiers();
        boolean flag = Modifier.isInterface(i);

        try {
            if (!flag) {
                CtClass ctclass = clazz.getSuperclass();

                if (ctclass != null) {
                    MemberResolver.Method memberresolver_method = this.lookupMethod(ctclass, methodName, argTypes, argDims, argClassNames, onlyExact);

                    if (memberresolver_method != null) {
                        return memberresolver_method;
                    }
                }
            }
        } catch (NotFoundException notfoundexception) {
            ;
        }

        try {
            CtClass[] actclass = clazz.getInterfaces();
            int j = actclass.length;

            for (pclazz = 0; pclazz < j; ++pclazz) {
                r = this.lookupMethod(actclass[pclazz], methodName, argTypes, argDims, argClassNames, onlyExact);
                if (r != null) {
                    return r;
                }
            }

            if (flag) {
                CtClass ctclass1 = clazz.getSuperclass();

                if (ctclass1 != null) {
                    r = this.lookupMethod(ctclass1, methodName, argTypes, argDims, argClassNames, onlyExact);
                    if (r != null) {
                        return r;
                    }
                }
            }
        } catch (NotFoundException notfoundexception1) {
            ;
        }

        return maybe;
    }

    private int compareSignature(String desc, int[] argTypes, int[] argDims, String[] argClassNames) throws CompileError {
        int result = 0;
        int i = 1;
        int nArgs = argTypes.length;

        if (nArgs != Descriptor.numOfParameters(desc)) {
            return -1;
        } else {
            int len = desc.length();

            for (int n = 0; i < len; ++n) {
                char c = desc.charAt(i++);

                if (c == 41) {
                    return n == nArgs ? result : -1;
                }

                if (n >= nArgs) {
                    return -1;
                }

                int dim;

                for (dim = 0; c == 91; c = desc.charAt(i++)) {
                    ++dim;
                }

                if (argTypes[n] == 412) {
                    if (dim == 0 && c != 76) {
                        return -1;
                    }

                    if (c == 76) {
                        i = desc.indexOf(59, i) + 1;
                    }
                } else if (argDims[n] != dim) {
                    if (dim != 0 || c != 76 || !desc.startsWith("java/lang/Object;", i)) {
                        return -1;
                    }

                    i = desc.indexOf(59, i) + 1;
                    ++result;
                    if (i <= 0) {
                        return -1;
                    }
                } else {
                    int t;

                    if (c == 76) {
                        t = desc.indexOf(59, i);
                        if (t < 0 || argTypes[n] != 307) {
                            return -1;
                        }

                        String at = desc.substring(i, t);

                        if (!at.equals(argClassNames[n])) {
                            CtClass clazz = this.lookupClassByJvmName(argClassNames[n]);

                            try {
                                if (!clazz.subtypeOf(this.lookupClassByJvmName(at))) {
                                    return -1;
                                }

                                ++result;
                            } catch (NotFoundException notfoundexception) {
                                ++result;
                            }
                        }

                        i = t + 1;
                    } else {
                        t = descToType(c);
                        int i = argTypes[n];

                        if (t != i) {
                            if (t != 324 || i != 334 && i != 303 && i != 306) {
                                return -1;
                            }

                            ++result;
                        }
                    }
                }
            }

            return -1;
        }
    }

    public CtField lookupFieldByJvmName2(String jvmClassName, Symbol fieldSym, ASTree expr) throws NoFieldException {
        String field = fieldSym.get();
        CtClass cc = null;

        try {
            cc = this.lookupClass(jvmToJavaName(jvmClassName), true);
        } catch (CompileError compileerror) {
            throw new NoFieldException(jvmClassName + "/" + field, expr);
        }

        try {
            return cc.getField(field);
        } catch (NotFoundException notfoundexception) {
            jvmClassName = javaToJvmName(cc.getName());
            throw new NoFieldException(jvmClassName + "$" + field, expr);
        }
    }

    public CtField lookupFieldByJvmName(String jvmClassName, Symbol fieldName) throws CompileError {
        return this.lookupField(jvmToJavaName(jvmClassName), fieldName);
    }

    public CtField lookupField(String className, Symbol fieldName) throws CompileError {
        CtClass cc = this.lookupClass(className, false);

        try {
            return cc.getField(fieldName.get());
        } catch (NotFoundException notfoundexception) {
            throw new CompileError("no such field: " + fieldName.get());
        }
    }

    public CtClass lookupClassByName(ASTList name) throws CompileError {
        return this.lookupClass(Declarator.astToClassName(name, '.'), false);
    }

    public CtClass lookupClassByJvmName(String jvmName) throws CompileError {
        return this.lookupClass(jvmToJavaName(jvmName), false);
    }

    public CtClass lookupClass(Declarator decl) throws CompileError {
        return this.lookupClass(decl.getType(), decl.getArrayDim(), decl.getClassName());
    }

    public CtClass lookupClass(int type, int dim, String classname) throws CompileError {
        String cname = "";

        if (type == 307) {
            CtClass clazz = this.lookupClassByJvmName(classname);

            if (dim <= 0) {
                return clazz;
            }

            cname = clazz.getName();
        } else {
            cname = getTypeName(type);
        }

        while (dim-- > 0) {
            cname = cname + "[]";
        }

        return this.lookupClass(cname, false);
    }

    static String getTypeName(int type) throws CompileError {
        String cname = "";

        switch (type) {
        case 301:
            cname = "boolean";
            break;

        case 303:
            cname = "byte";
            break;

        case 306:
            cname = "char";
            break;

        case 312:
            cname = "double";
            break;

        case 317:
            cname = "float";
            break;

        case 324:
            cname = "int";
            break;

        case 326:
            cname = "long";
            break;

        case 334:
            cname = "short";
            break;

        case 344:
            cname = "void";
            break;

        default:
            fatal();
        }

        return cname;
    }

    public CtClass lookupClass(String name, boolean notCheckInner) throws CompileError {
        Hashtable cache = this.getInvalidNames();
        Object found = cache.get(name);

        if (found == "<invalid>") {
            throw new CompileError("no such class: " + name);
        } else {
            if (found != null) {
                try {
                    return this.classPool.get((String) found);
                } catch (NotFoundException notfoundexception) {
                    ;
                }
            }

            CtClass cc = null;

            try {
                cc = this.lookupClass0(name, notCheckInner);
            } catch (NotFoundException notfoundexception1) {
                cc = this.searchImports(name);
            }

            cache.put(name, cc.getName());
            return cc;
        }
    }

    public static int getInvalidMapSize() {
        return MemberResolver.invalidNamesMap.size();
    }

    private Hashtable getInvalidNames() {
        Hashtable ht = this.invalidNames;

        if (ht == null) {
            Class oclass = MemberResolver.class;

            synchronized (MemberResolver.class) {
                WeakReference ref = (WeakReference) MemberResolver.invalidNamesMap.get(this.classPool);

                if (ref != null) {
                    ht = (Hashtable) ref.get();
                }

                if (ht == null) {
                    ht = new Hashtable();
                    MemberResolver.invalidNamesMap.put(this.classPool, new WeakReference(ht));
                }
            }

            this.invalidNames = ht;
        }

        return ht;
    }

    private CtClass searchImports(String orgName) throws CompileError {
        if (orgName.indexOf(46) < 0) {
            Iterator it = this.classPool.getImportedPackages();

            while (it.hasNext()) {
                String pac = (String) it.next();
                String fqName = pac + '.' + orgName;

                try {
                    return this.classPool.get(fqName);
                } catch (NotFoundException notfoundexception) {
                    try {
                        if (pac.endsWith("." + orgName)) {
                            return this.classPool.get(pac);
                        }
                    } catch (NotFoundException notfoundexception1) {
                        ;
                    }
                }
            }
        }

        this.getInvalidNames().put(orgName, "<invalid>");
        throw new CompileError("no such class: " + orgName);
    }

    private CtClass lookupClass0(String classname, boolean notCheckInner) throws NotFoundException {
        CtClass cc = null;

        do {
            try {
                cc = this.classPool.get(classname);
            } catch (NotFoundException notfoundexception) {
                int i = classname.lastIndexOf(46);

                if (notCheckInner || i < 0) {
                    throw notfoundexception;
                }

                StringBuffer sbuf = new StringBuffer(classname);

                sbuf.setCharAt(i, '$');
                classname = sbuf.toString();
            }
        } while (cc == null);

        return cc;
    }

    public String resolveClassName(ASTList name) throws CompileError {
        return name == null ? null : javaToJvmName(this.lookupClassByName(name).getName());
    }

    public String resolveJvmClassName(String jvmName) throws CompileError {
        return jvmName == null ? null : javaToJvmName(this.lookupClassByJvmName(jvmName).getName());
    }

    public static CtClass getSuperclass(CtClass c) throws CompileError {
        try {
            CtClass sc = c.getSuperclass();

            if (sc != null) {
                return sc;
            }
        } catch (NotFoundException notfoundexception) {
            ;
        }

        throw new CompileError("cannot find the super class of " + c.getName());
    }

    public static CtClass getSuperInterface(CtClass c, String interfaceName) throws CompileError {
        try {
            CtClass[] intfs = c.getInterfaces();

            for (int i = 0; i < intfs.length; ++i) {
                if (intfs[i].getName().equals(interfaceName)) {
                    return intfs[i];
                }
            }
        } catch (NotFoundException notfoundexception) {
            ;
        }

        throw new CompileError("cannot find the super inetrface " + interfaceName + " of " + c.getName());
    }

    public static String javaToJvmName(String classname) {
        return classname.replace('.', '/');
    }

    public static String jvmToJavaName(String classname) {
        return classname.replace('/', '.');
    }

    public static int descToType(char c) throws CompileError {
        switch (c) {
        case 'B':
            return 303;

        case 'C':
            return 306;

        case 'D':
            return 312;

        case 'E':
        case 'G':
        case 'H':
        case 'K':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'T':
        case 'U':
        case 'W':
        case 'X':
        case 'Y':
        default:
            fatal();
            return 344;

        case 'F':
            return 317;

        case 'I':
            return 324;

        case 'J':
            return 326;

        case 'L':
        case '[':
            return 307;

        case 'S':
            return 334;

        case 'V':
            return 344;

        case 'Z':
            return 301;
        }
    }

    public static int getModifiers(ASTList mods) {
        int m = 0;

        while (mods != null) {
            Keyword k = (Keyword) mods.head();

            mods = mods.tail();
            switch (k.get()) {
            case 300:
                m |= 1024;
                break;

            case 315:
                m |= 16;
                break;

            case 330:
                m |= 2;
                break;

            case 331:
                m |= 4;
                break;

            case 332:
                m |= 1;
                break;

            case 335:
                m |= 8;
                break;

            case 338:
                m |= 32;
                break;

            case 342:
                m |= 128;
                break;

            case 345:
                m |= 64;
                break;

            case 347:
                m |= 2048;
            }
        }

        return m;
    }

    public static class Method {

        public CtClass declaring;
        public MethodInfo info;
        public int notmatch;

        public Method(CtClass c, MethodInfo i, int n) {
            this.declaring = c;
            this.info = i;
            this.notmatch = n;
        }

        public boolean isStatic() {
            int acc = this.info.getAccessFlags();

            return (acc & 8) != 0;
        }
    }
}
