package javassist.bytecode;

import java.util.Map;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;

public class Descriptor {

    public static String toJvmName(String classname) {
        return classname.replace('.', '/');
    }

    public static String toJavaName(String classname) {
        return classname.replace('/', '.');
    }

    public static String toJvmName(CtClass clazz) {
        return clazz.isArray() ? of(clazz) : toJvmName(clazz.getName());
    }

    public static String toClassName(String descriptor) {
        int arrayDim = 0;
        int i = 0;

        char c;

        for (c = descriptor.charAt(0); c == 91; c = descriptor.charAt(i)) {
            ++arrayDim;
            ++i;
        }

        String name;

        if (c == 76) {
            int sbuf = descriptor.indexOf(59, i++);

            name = descriptor.substring(i, sbuf).replace('/', '.');
            i = sbuf;
        } else if (c == 86) {
            name = "void";
        } else if (c == 73) {
            name = "int";
        } else if (c == 66) {
            name = "byte";
        } else if (c == 74) {
            name = "long";
        } else if (c == 68) {
            name = "double";
        } else if (c == 70) {
            name = "float";
        } else if (c == 67) {
            name = "char";
        } else if (c == 83) {
            name = "short";
        } else {
            if (c != 90) {
                throw new RuntimeException("bad descriptor: " + descriptor);
            }

            name = "boolean";
        }

        if (i + 1 != descriptor.length()) {
            throw new RuntimeException("multiple descriptors?: " + descriptor);
        } else if (arrayDim == 0) {
            return name;
        } else {
            StringBuffer stringbuffer = new StringBuffer(name);

            do {
                stringbuffer.append("[]");
                --arrayDim;
            } while (arrayDim > 0);

            return stringbuffer.toString();
        }
    }

    public static String of(String classname) {
        return classname.equals("void") ? "V" : (classname.equals("int") ? "I" : (classname.equals("byte") ? "B" : (classname.equals("long") ? "J" : (classname.equals("double") ? "D" : (classname.equals("float") ? "F" : (classname.equals("char") ? "C" : (classname.equals("short") ? "S" : (classname.equals("boolean") ? "Z" : "L" + toJvmName(classname) + ";"))))))));
    }

    public static String rename(String desc, String oldname, String newname) {
        if (desc.indexOf(oldname) < 0) {
            return desc;
        } else {
            StringBuffer newdesc = new StringBuffer();
            int head = 0;
            int i = 0;

            int len;

            while (true) {
                len = desc.indexOf(76, i);
                if (len < 0) {
                    break;
                }

                if (desc.startsWith(oldname, len + 1) && desc.charAt(len + oldname.length() + 1) == 59) {
                    newdesc.append(desc.substring(head, len));
                    newdesc.append('L');
                    newdesc.append(newname);
                    newdesc.append(';');
                    head = i = len + oldname.length() + 2;
                } else {
                    i = desc.indexOf(59, len) + 1;
                    if (i < 1) {
                        break;
                    }
                }
            }

            if (head == 0) {
                return desc;
            } else {
                len = desc.length();
                if (head < len) {
                    newdesc.append(desc.substring(head, len));
                }

                return newdesc.toString();
            }
        }
    }

    public static String rename(String desc, Map map) {
        if (map == null) {
            return desc;
        } else {
            StringBuffer newdesc = new StringBuffer();
            int head = 0;
            int i = 0;

            int len;

            while (true) {
                len = desc.indexOf(76, i);
                if (len < 0) {
                    break;
                }

                int k = desc.indexOf(59, len);

                if (k < 0) {
                    break;
                }

                i = k + 1;
                String name = desc.substring(len + 1, k);
                String name2 = (String) map.get(name);

                if (name2 != null) {
                    newdesc.append(desc.substring(head, len));
                    newdesc.append('L');
                    newdesc.append(name2);
                    newdesc.append(';');
                    head = i;
                }
            }

            if (head == 0) {
                return desc;
            } else {
                len = desc.length();
                if (head < len) {
                    newdesc.append(desc.substring(head, len));
                }

                return newdesc.toString();
            }
        }
    }

    public static String of(CtClass type) {
        StringBuffer sbuf = new StringBuffer();

        toDescriptor(sbuf, type);
        return sbuf.toString();
    }

    private static void toDescriptor(StringBuffer desc, CtClass type) {
        if (type.isArray()) {
            desc.append('[');

            try {
                toDescriptor(desc, type.getComponentType());
            } catch (NotFoundException notfoundexception) {
                desc.append('L');
                String name = type.getName();

                desc.append(toJvmName(name.substring(0, name.length() - 2)));
                desc.append(';');
            }
        } else if (type.isPrimitive()) {
            CtPrimitiveType pt = (CtPrimitiveType) type;

            desc.append(pt.getDescriptor());
        } else {
            desc.append('L');
            desc.append(type.getName().replace('.', '/'));
            desc.append(';');
        }

    }

    public static String ofConstructor(CtClass[] nameTypes) {
        return ofMethod(CtClass.voidType, nameTypes);
    }

    public static String ofMethod(CtClass returnType, CtClass[] nameTypes) {
        StringBuffer desc = new StringBuffer();

        desc.append('(');
        if (nameTypes != null) {
            int n = nameTypes.length;

            for (int i = 0; i < n; ++i) {
                toDescriptor(desc, nameTypes[i]);
            }
        }

        desc.append(')');
        if (returnType != null) {
            toDescriptor(desc, returnType);
        }

        return desc.toString();
    }

    public static String ofParameters(CtClass[] nameTypes) {
        return ofMethod((CtClass) null, nameTypes);
    }

    public static String appendParameter(String classname, String desc) {
        int i = desc.indexOf(41);

        if (i < 0) {
            return desc;
        } else {
            StringBuffer newdesc = new StringBuffer();

            newdesc.append(desc.substring(0, i));
            newdesc.append('L');
            newdesc.append(classname.replace('.', '/'));
            newdesc.append(';');
            newdesc.append(desc.substring(i));
            return newdesc.toString();
        }
    }

    public static String insertParameter(String classname, String desc) {
        return desc.charAt(0) != 40 ? desc : "(L" + classname.replace('.', '/') + ';' + desc.substring(1);
    }

    public static String appendParameter(CtClass type, String descriptor) {
        int i = descriptor.indexOf(41);

        if (i < 0) {
            return descriptor;
        } else {
            StringBuffer newdesc = new StringBuffer();

            newdesc.append(descriptor.substring(0, i));
            toDescriptor(newdesc, type);
            newdesc.append(descriptor.substring(i));
            return newdesc.toString();
        }
    }

    public static String insertParameter(CtClass type, String descriptor) {
        return descriptor.charAt(0) != 40 ? descriptor : "(" + of(type) + descriptor.substring(1);
    }

    public static String changeReturnType(String classname, String desc) {
        int i = desc.indexOf(41);

        if (i < 0) {
            return desc;
        } else {
            StringBuffer newdesc = new StringBuffer();

            newdesc.append(desc.substring(0, i + 1));
            newdesc.append('L');
            newdesc.append(classname.replace('.', '/'));
            newdesc.append(';');
            return newdesc.toString();
        }
    }

    public static CtClass[] getParameterTypes(String desc, ClassPool cp) throws NotFoundException {
        if (desc.charAt(0) != 40) {
            return null;
        } else {
            int num = numOfParameters(desc);
            CtClass[] args = new CtClass[num];
            int n = 0;
            int i = 1;

            do {
                i = toCtClass(cp, desc, i, args, n++);
            } while (i > 0);

            return args;
        }
    }

    public static boolean eqParamTypes(String desc1, String desc2) {
        if (desc1.charAt(0) != 40) {
            return false;
        } else {
            int i = 0;

            while (true) {
                char c = desc1.charAt(i);

                if (c != desc2.charAt(i)) {
                    return false;
                }

                if (c == 41) {
                    return true;
                }

                ++i;
            }
        }
    }

    public static String getParamDescriptor(String decl) {
        return decl.substring(0, decl.indexOf(41) + 1);
    }

    public static CtClass getReturnType(String desc, ClassPool cp) throws NotFoundException {
        int i = desc.indexOf(41);

        if (i < 0) {
            return null;
        } else {
            CtClass[] type = new CtClass[1];

            toCtClass(cp, desc, i + 1, type, 0);
            return type[0];
        }
    }

    public static int numOfParameters(String desc) {
        int n = 0;
        int i = 1;

        while (true) {
            char c = desc.charAt(i);

            if (c == 41) {
                return n;
            }

            while (c == 91) {
                ++i;
                c = desc.charAt(i);
            }

            if (c == 76) {
                i = desc.indexOf(59, i) + 1;
                if (i <= 0) {
                    throw new IndexOutOfBoundsException("bad descriptor");
                }
            } else {
                ++i;
            }

            ++n;
        }
    }

    public static CtClass toCtClass(String desc, ClassPool cp) throws NotFoundException {
        CtClass[] clazz = new CtClass[1];
        int res = toCtClass(cp, desc, 0, clazz, 0);

        return res >= 0 ? clazz[0] : cp.get(desc.replace('/', '.'));
    }

    private static int toCtClass(ClassPool cp, String desc, int i, CtClass[] args, int n) throws NotFoundException {
        int arrayDim = 0;

        char c;

        for (c = desc.charAt(i); c == 91; c = desc.charAt(i)) {
            ++arrayDim;
            ++i;
        }

        int i2;
        String name;

        if (c == 76) {
            ++i;
            i2 = desc.indexOf(59, i);
            name = desc.substring(i, i2++).replace('/', '.');
        } else {
            CtClass sbuf = toPrimitiveClass(c);

            if (sbuf == null) {
                return -1;
            }

            i2 = i + 1;
            if (arrayDim == 0) {
                args[n] = sbuf;
                return i2;
            }

            name = sbuf.getName();
        }

        if (arrayDim > 0) {
            StringBuffer stringbuffer = new StringBuffer(name);

            while (arrayDim-- > 0) {
                stringbuffer.append("[]");
            }

            name = stringbuffer.toString();
        }

        args[n] = cp.get(name);
        return i2;
    }

    static CtClass toPrimitiveClass(char c) {
        CtClass type = null;

        switch (c) {
        case 'B':
            type = CtClass.byteType;
            break;

        case 'C':
            type = CtClass.charType;
            break;

        case 'D':
            type = CtClass.doubleType;

        case 'E':
        case 'G':
        case 'H':
        case 'K':
        case 'L':
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
            break;

        case 'F':
            type = CtClass.floatType;
            break;

        case 'I':
            type = CtClass.intType;
            break;

        case 'J':
            type = CtClass.longType;
            break;

        case 'S':
            type = CtClass.shortType;
            break;

        case 'V':
            type = CtClass.voidType;
            break;

        case 'Z':
            type = CtClass.booleanType;
        }

        return type;
    }

    public static int arrayDimension(String desc) {
        int dim;

        for (dim = 0; desc.charAt(dim) == 91; ++dim) {
            ;
        }

        return dim;
    }

    public static String toArrayComponent(String desc, int dim) {
        return desc.substring(dim);
    }

    public static int dataSize(String desc) {
        return dataSize(desc, true);
    }

    public static int nameSize(String desc) {
        return -dataSize(desc, false);
    }

    private static int dataSize(String desc, boolean withRet) {
        int n = 0;
        char c = desc.charAt(0);

        if (c == 40) {
            int i = 1;

            while (true) {
                c = desc.charAt(i);
                if (c == 41) {
                    c = desc.charAt(i + 1);
                    break;
                }

                boolean array;

                for (array = false; c == 91; c = desc.charAt(i)) {
                    array = true;
                    ++i;
                }

                if (c == 76) {
                    i = desc.indexOf(59, i) + 1;
                    if (i <= 0) {
                        throw new IndexOutOfBoundsException("bad descriptor");
                    }
                } else {
                    ++i;
                }

                if (!array && (c == 74 || c == 68)) {
                    n -= 2;
                } else {
                    --n;
                }
            }
        }

        if (withRet) {
            if (c != 74 && c != 68) {
                if (c != 86) {
                    ++n;
                }
            } else {
                n += 2;
            }
        }

        return n;
    }

    public static String toString(String desc) {
        return Descriptor.PrettyPrinter.toString(desc);
    }

    public static class Iterator {

        private String desc;
        private int index;
        private int curPos;
        private boolean name;

        public Iterator(String s) {
            this.desc = s;
            this.index = this.curPos = 0;
            this.name = false;
        }

        public boolean hasNext() {
            return this.index < this.desc.length();
        }

        public boolean isParameter() {
            return this.name;
        }

        public char currentChar() {
            return this.desc.charAt(this.curPos);
        }

        public boolean is2byte() {
            char c = this.currentChar();

            return c == 68 || c == 74;
        }

        public int next() {
            int nextPos = this.index;
            char c = this.desc.charAt(nextPos);

            if (c == 40) {
                ++this.index;
                ++nextPos;
                c = this.desc.charAt(nextPos);
                this.name = true;
            }

            if (c == 41) {
                ++this.index;
                ++nextPos;
                c = this.desc.charAt(nextPos);
                this.name = false;
            }

            while (c == 91) {
                ++nextPos;
                c = this.desc.charAt(nextPos);
            }

            if (c == 76) {
                nextPos = this.desc.indexOf(59, nextPos) + 1;
                if (nextPos <= 0) {
                    throw new IndexOutOfBoundsException("bad descriptor");
                }
            } else {
                ++nextPos;
            }

            this.curPos = this.index;
            this.index = nextPos;
            return this.curPos;
        }
    }

    static class PrettyPrinter {

        static String toString(String desc) {
            StringBuffer sbuf = new StringBuffer();

            if (desc.charAt(0) == 40) {
                int pos = 1;

                sbuf.append('(');

                for (; desc.charAt(pos) != 41; pos = readType(sbuf, pos, desc)) {
                    if (pos > 1) {
                        sbuf.append(',');
                    }
                }

                sbuf.append(')');
            } else {
                readType(sbuf, 0, desc);
            }

            return sbuf.toString();
        }

        static int readType(StringBuffer sbuf, int pos, String desc) {
            char c = desc.charAt(pos);

            int arrayDim;

            for (arrayDim = 0; c == 91; c = desc.charAt(pos)) {
                ++arrayDim;
                ++pos;
            }

            if (c == 76) {
                while (true) {
                    ++pos;
                    c = desc.charAt(pos);
                    if (c == 59) {
                        break;
                    }

                    if (c == 47) {
                        c = 46;
                    }

                    sbuf.append(c);
                }
            } else {
                CtClass t = Descriptor.toPrimitiveClass(c);

                sbuf.append(t.getName());
            }

            while (arrayDim-- > 0) {
                sbuf.append("[]");
            }

            return pos + 1;
        }
    }
}
