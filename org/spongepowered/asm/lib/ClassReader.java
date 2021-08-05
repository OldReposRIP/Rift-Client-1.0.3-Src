package org.spongepowered.asm.lib;

import java.io.IOException;
import java.io.InputStream;

public class ClassReader {

    static final boolean SIGNATURES = true;
    static final boolean ANNOTATIONS = true;
    static final boolean FRAMES = true;
    static final boolean WRITER = true;
    static final boolean RESIZE = true;
    public static final int SKIP_CODE = 1;
    public static final int SKIP_DEBUG = 2;
    public static final int SKIP_FRAMES = 4;
    public static final int EXPAND_FRAMES = 8;
    static final int EXPAND_ASM_INSNS = 256;
    public final byte[] b;
    private final int[] items;
    private final String[] strings;
    private final int maxStringLength;
    public final int header;

    public ClassReader(byte[] b) {
        this(b, 0, b.length);
    }

    public ClassReader(byte[] b, int off, int len) {
        this.b = b;
        if (this.readShort(off + 6) > 52) {
            throw new IllegalArgumentException();
        } else {
            this.items = new int[this.readUnsignedShort(off + 8)];
            int n = this.items.length;

            this.strings = new String[n];
            int max = 0;
            int index = off + 10;

            for (int i = 1; i < n; ++i) {
                this.items[i] = index + 1;
                int size;

                switch (b[index]) {
                case 1:
                    size = 3 + this.readUnsignedShort(index + 1);
                    if (size > max) {
                        max = size;
                    }
                    break;

                case 2:
                case 7:
                case 8:
                case 13:
                case 14:
                case 16:
                case 17:
                default:
                    size = 3;
                    break;

                case 3:
                case 4:
                case 9:
                case 10:
                case 11:
                case 12:
                case 18:
                    size = 5;
                    break;

                case 5:
                case 6:
                    size = 9;
                    ++i;
                    break;

                case 15:
                    size = 4;
                }

                index += size;
            }

            this.maxStringLength = max;
            this.header = index;
        }
    }

    public int getAccess() {
        return this.readUnsignedShort(this.header);
    }

    public String getClassName() {
        return this.readClass(this.header + 2, new char[this.maxStringLength]);
    }

    public String getSuperName() {
        return this.readClass(this.header + 4, new char[this.maxStringLength]);
    }

    public String[] getInterfaces() {
        int index = this.header + 6;
        int n = this.readUnsignedShort(index);
        String[] interfaces = new String[n];

        if (n > 0) {
            char[] buf = new char[this.maxStringLength];

            for (int i = 0; i < n; ++i) {
                index += 2;
                interfaces[i] = this.readClass(index, buf);
            }
        }

        return interfaces;
    }

    void copyPool(ClassWriter classWriter) {
        char[] buf = new char[this.maxStringLength];
        int ll = this.items.length;
        Item[] items2 = new Item[ll];

        int off;

        for (off = 1; off < ll; ++off) {
            int index = this.items[off];
            byte tag = this.b[index - 1];
            Item item = new Item(off);
            int nameType;
            int index2;

            switch (tag) {
            case 1:
                String s = this.strings[off];

                if (s == null) {
                    index = this.items[off];
                    s = this.strings[off] = this.readUTF(index + 2, this.readUnsignedShort(index), buf);
                }

                item.set(tag, s, (String) null, (String) null);
                break;

            case 2:
            case 7:
            case 8:
            case 13:
            case 14:
            case 16:
            case 17:
            default:
                item.set(tag, this.readUTF8(index, buf), (String) null, (String) null);
                break;

            case 3:
                item.set(this.readInt(index));
                break;

            case 4:
                item.set(Float.intBitsToFloat(this.readInt(index)));
                break;

            case 5:
                item.set(this.readLong(index));
                ++off;
                break;

            case 6:
                item.set(Double.longBitsToDouble(this.readLong(index)));
                ++off;
                break;

            case 9:
            case 10:
            case 11:
                nameType = this.items[this.readUnsignedShort(index + 2)];
                item.set(tag, this.readClass(index, buf), this.readUTF8(nameType, buf), this.readUTF8(nameType + 2, buf));
                break;

            case 12:
                item.set(tag, this.readUTF8(index, buf), this.readUTF8(index + 2, buf), (String) null);
                break;

            case 15:
                index2 = this.items[this.readUnsignedShort(index + 1)];
                nameType = this.items[this.readUnsignedShort(index2 + 2)];
                item.set(20 + this.readByte(index), this.readClass(index2, buf), this.readUTF8(nameType, buf), this.readUTF8(nameType + 2, buf));
                break;

            case 18:
                if (classWriter.bootstrapMethods == null) {
                    this.copyBootstrapMethods(classWriter, items2, buf);
                }

                nameType = this.items[this.readUnsignedShort(index + 2)];
                item.set(this.readUTF8(nameType, buf), this.readUTF8(nameType + 2, buf), this.readUnsignedShort(index));
            }

            index2 = item.hashCode % items2.length;
            item.next = items2[index2];
            items2[index2] = item;
        }

        off = this.items[1] - 1;
        classWriter.pool.putByteArray(this.b, off, this.header - off);
        classWriter.items = items2;
        classWriter.threshold = (int) (0.75D * (double) ll);
        classWriter.index = ll;
    }

    private void copyBootstrapMethods(ClassWriter classWriter, Item[] items, char[] c) {
        int u = this.getAttributes();
        boolean found = false;

        int boostrapMethodCount;

        for (boostrapMethodCount = this.readUnsignedShort(u); boostrapMethodCount > 0; --boostrapMethodCount) {
            String attrSize = this.readUTF8(u + 2, c);

            if ("BootstrapMethods".equals(attrSize)) {
                found = true;
                break;
            }

            u += 6 + this.readInt(u + 4);
        }

        if (found) {
            boostrapMethodCount = this.readUnsignedShort(u + 8);
            int i = 0;

            for (int bootstrapMethods = u + 10; i < boostrapMethodCount; ++i) {
                int position = bootstrapMethods - u - 10;
                int hashCode = this.readConst(this.readUnsignedShort(bootstrapMethods), c).hashCode();

                for (int item = this.readUnsignedShort(bootstrapMethods + 2); item > 0; --item) {
                    hashCode ^= this.readConst(this.readUnsignedShort(bootstrapMethods + 4), c).hashCode();
                    bootstrapMethods += 2;
                }

                bootstrapMethods += 4;
                Item item = new Item(i);

                item.set(position, hashCode & Integer.MAX_VALUE);
                int index = item.hashCode % items.length;

                item.next = items[index];
                items[index] = item;
            }

            i = this.readInt(u + 4);
            ByteVector bytevector = new ByteVector(i + 62);

            bytevector.putByteArray(this.b, u + 10, i - 2);
            classWriter.bootstrapMethodsCount = boostrapMethodCount;
            classWriter.bootstrapMethods = bytevector;
        }
    }

    public ClassReader(InputStream is) throws IOException {
        this(readClass(is, false));
    }

    public ClassReader(String name) throws IOException {
        this(readClass(ClassLoader.getSystemResourceAsStream(name.replace('.', '/') + ".class"), true));
    }

    private static byte[] readClass(InputStream is, boolean close) throws IOException {
        if (is == null) {
            throw new IOException("Class not found");
        } else {
            try {
                byte[] b = new byte[is.available()];
                int len = 0;

                while (true) {
                    int n = is.read(b, len, b.length - len);

                    if (n == -1) {
                        byte[] abyte;

                        if (len < b.length) {
                            abyte = new byte[len];
                            System.arraycopy(b, 0, abyte, 0, len);
                            b = abyte;
                        }

                        abyte = b;
                        return abyte;
                    }

                    len += n;
                    if (len == b.length) {
                        int last = is.read();
                        byte[] c;

                        if (last < 0) {
                            c = b;
                            return c;
                        }

                        c = new byte[b.length + 1000];
                        System.arraycopy(b, 0, c, 0, len);
                        c[len++] = (byte) last;
                        b = c;
                    }
                }
            } finally {
                if (close) {
                    is.close();
                }

            }
        }
    }

    public void accept(ClassVisitor classVisitor, int flags) {
        this.accept(classVisitor, new Attribute[0], flags);
    }

    public void accept(ClassVisitor classVisitor, Attribute[] attrs, int flags) {
        int u = this.header;
        char[] c = new char[this.maxStringLength];
        Context context = new Context();

        context.attrs = attrs;
        context.flags = flags;
        context.buffer = c;
        int access = this.readUnsignedShort(u);
        String name = this.readClass(u + 2, c);
        String superClass = this.readClass(u + 4, c);
        String[] interfaces = new String[this.readUnsignedShort(u + 6)];

        u += 8;

        for (int signature = 0; signature < interfaces.length; ++signature) {
            interfaces[signature] = this.readClass(u, c);
            u += 2;
        }

        String s = null;
        String sourceFile = null;
        String sourceDebug = null;
        String enclosingOwner = null;
        String enclosingName = null;
        String enclosingDesc = null;
        int anns = 0;
        int ianns = 0;
        int tanns = 0;
        int itanns = 0;
        int innerClasses = 0;
        Attribute attributes = null;

        u = this.getAttributes();

        int i;

        for (i = this.readUnsignedShort(u); i > 0; --i) {
            String i1 = this.readUTF8(u + 2, c);

            if ("SourceFile".equals(i1)) {
                sourceFile = this.readUTF8(u + 8, c);
            } else if ("InnerClasses".equals(i1)) {
                innerClasses = u + 8;
            } else {
                int attr;

                if ("EnclosingMethod".equals(i1)) {
                    enclosingOwner = this.readClass(u + 8, c);
                    attr = this.readUnsignedShort(u + 10);
                    if (attr != 0) {
                        enclosingName = this.readUTF8(this.items[attr], c);
                        enclosingDesc = this.readUTF8(this.items[attr] + 2, c);
                    }
                } else if ("Signature".equals(i1)) {
                    s = this.readUTF8(u + 8, c);
                } else if ("RuntimeVisibleAnnotations".equals(i1)) {
                    anns = u + 8;
                } else if ("RuntimeVisibleTypeAnnotations".equals(i1)) {
                    tanns = u + 8;
                } else if ("Deprecated".equals(i1)) {
                    access |= 131072;
                } else if ("Synthetic".equals(i1)) {
                    access |= 266240;
                } else if ("SourceDebugExtension".equals(i1)) {
                    attr = this.readInt(u + 4);
                    sourceDebug = this.readUTF(u + 8, attr, new char[attr]);
                } else if ("RuntimeInvisibleAnnotations".equals(i1)) {
                    ianns = u + 8;
                } else if ("RuntimeInvisibleTypeAnnotations".equals(i1)) {
                    itanns = u + 8;
                } else if ("BootstrapMethods".equals(i1)) {
                    int[] aint = new int[this.readUnsignedShort(u + 8)];
                    int j = 0;

                    for (int v = u + 10; j < aint.length; ++j) {
                        aint[j] = v;
                        v += 2 + this.readUnsignedShort(v + 2) << 1;
                    }

                    context.bootstrapMethods = aint;
                } else {
                    Attribute attribute = this.readAttribute(attrs, i1, u + 8, this.readInt(u + 4), c, -1, (Label[]) null);

                    if (attribute != null) {
                        attribute.next = attributes;
                        attributes = attribute;
                    }
                }
            }

            u += 6 + this.readInt(u + 4);
        }

        classVisitor.visit(this.readInt(this.items[1] - 7), access, name, s, superClass, interfaces);
        if ((flags & 2) == 0 && (sourceFile != null || sourceDebug != null)) {
            classVisitor.visitSource(sourceFile, sourceDebug);
        }

        if (enclosingOwner != null) {
            classVisitor.visitOuterClass(enclosingOwner, enclosingName, enclosingDesc);
        }

        int i;

        if (anns != 0) {
            i = this.readUnsignedShort(anns);

            for (i = anns + 2; i > 0; --i) {
                i = this.readAnnotationValues(i + 2, c, true, classVisitor.visitAnnotation(this.readUTF8(i, c), true));
            }
        }

        if (ianns != 0) {
            i = this.readUnsignedShort(ianns);

            for (i = ianns + 2; i > 0; --i) {
                i = this.readAnnotationValues(i + 2, c, true, classVisitor.visitAnnotation(this.readUTF8(i, c), false));
            }
        }

        if (tanns != 0) {
            i = this.readUnsignedShort(tanns);

            for (i = tanns + 2; i > 0; --i) {
                i = this.readAnnotationTarget(context, i);
                i = this.readAnnotationValues(i + 2, c, true, classVisitor.visitTypeAnnotation(context.typeRef, context.typePath, this.readUTF8(i, c), true));
            }
        }

        if (itanns != 0) {
            i = this.readUnsignedShort(itanns);

            for (i = itanns + 2; i > 0; --i) {
                i = this.readAnnotationTarget(context, i);
                i = this.readAnnotationValues(i + 2, c, true, classVisitor.visitTypeAnnotation(context.typeRef, context.typePath, this.readUTF8(i, c), false));
            }
        }

        while (attributes != null) {
            Attribute attribute1 = attributes.next;

            attributes.next = null;
            classVisitor.visitAttribute(attributes);
            attributes = attribute1;
        }

        if (innerClasses != 0) {
            i = innerClasses + 2;

            for (i = this.readUnsignedShort(innerClasses); i > 0; --i) {
                classVisitor.visitInnerClass(this.readClass(i, c), this.readClass(i + 2, c), this.readUTF8(i + 4, c), this.readUnsignedShort(i + 6));
                i += 8;
            }
        }

        u = this.header + 10 + 2 * interfaces.length;

        for (i = this.readUnsignedShort(u - 2); i > 0; --i) {
            u = this.readField(classVisitor, context, u);
        }

        u += 2;

        for (i = this.readUnsignedShort(u - 2); i > 0; --i) {
            u = this.readMethod(classVisitor, context, u);
        }

        classVisitor.visitEnd();
    }

    private int readField(ClassVisitor classVisitor, Context context, int u) {
        char[] c = context.buffer;
        int access = this.readUnsignedShort(u);
        String name = this.readUTF8(u + 2, c);
        String desc = this.readUTF8(u + 4, c);

        u += 6;
        String signature = null;
        int anns = 0;
        int ianns = 0;
        int tanns = 0;
        int itanns = 0;
        Object value = null;
        Attribute attributes = null;

        int v;

        for (int fv = this.readUnsignedShort(u); fv > 0; --fv) {
            String attr = this.readUTF8(u + 2, c);

            if ("ConstantValue".equals(attr)) {
                v = this.readUnsignedShort(u + 8);
                value = v == 0 ? null : this.readConst(v, c);
            } else if ("Signature".equals(attr)) {
                signature = this.readUTF8(u + 8, c);
            } else if ("Deprecated".equals(attr)) {
                access |= 131072;
            } else if ("Synthetic".equals(attr)) {
                access |= 266240;
            } else if ("RuntimeVisibleAnnotations".equals(attr)) {
                anns = u + 8;
            } else if ("RuntimeVisibleTypeAnnotations".equals(attr)) {
                tanns = u + 8;
            } else if ("RuntimeInvisibleAnnotations".equals(attr)) {
                ianns = u + 8;
            } else if ("RuntimeInvisibleTypeAnnotations".equals(attr)) {
                itanns = u + 8;
            } else {
                Attribute attribute = this.readAttribute(context.attrs, attr, u + 8, this.readInt(u + 4), c, -1, (Label[]) null);

                if (attribute != null) {
                    attribute.next = attributes;
                    attributes = attribute;
                }
            }

            u += 6 + this.readInt(u + 4);
        }

        u += 2;
        FieldVisitor fieldvisitor = classVisitor.visitField(access, name, desc, signature, value);

        if (fieldvisitor == null) {
            return u;
        } else {
            int i;

            if (anns != 0) {
                i = this.readUnsignedShort(anns);

                for (v = anns + 2; i > 0; --i) {
                    v = this.readAnnotationValues(v + 2, c, true, fieldvisitor.visitAnnotation(this.readUTF8(v, c), true));
                }
            }

            if (ianns != 0) {
                i = this.readUnsignedShort(ianns);

                for (v = ianns + 2; i > 0; --i) {
                    v = this.readAnnotationValues(v + 2, c, true, fieldvisitor.visitAnnotation(this.readUTF8(v, c), false));
                }
            }

            if (tanns != 0) {
                i = this.readUnsignedShort(tanns);

                for (v = tanns + 2; i > 0; --i) {
                    v = this.readAnnotationTarget(context, v);
                    v = this.readAnnotationValues(v + 2, c, true, fieldvisitor.visitTypeAnnotation(context.typeRef, context.typePath, this.readUTF8(v, c), true));
                }
            }

            if (itanns != 0) {
                i = this.readUnsignedShort(itanns);

                for (v = itanns + 2; i > 0; --i) {
                    v = this.readAnnotationTarget(context, v);
                    v = this.readAnnotationValues(v + 2, c, true, fieldvisitor.visitTypeAnnotation(context.typeRef, context.typePath, this.readUTF8(v, c), false));
                }
            }

            while (attributes != null) {
                Attribute attribute1 = attributes.next;

                attributes.next = null;
                fieldvisitor.visitAttribute(attributes);
                attributes = attribute1;
            }

            fieldvisitor.visitEnd();
            return u;
        }
    }

    private int readMethod(ClassVisitor classVisitor, Context context, int u) {
        char[] c = context.buffer;

        context.access = this.readUnsignedShort(u);
        context.name = this.readUTF8(u + 2, c);
        context.desc = this.readUTF8(u + 4, c);
        u += 6;
        int code = 0;
        int exception = 0;
        String[] exceptions = null;
        String signature = null;
        int methodParameters = 0;
        int anns = 0;
        int ianns = 0;
        int tanns = 0;
        int itanns = 0;
        int dann = 0;
        int mpanns = 0;
        int impanns = 0;
        int firstAttribute = u;
        Attribute attributes = null;

        int v;

        for (int mv = this.readUnsignedShort(u); mv > 0; --mv) {
            String attr = this.readUTF8(u + 2, c);

            if ("Code".equals(attr)) {
                if ((context.flags & 1) == 0) {
                    code = u + 8;
                }
            } else if ("Exceptions".equals(attr)) {
                exceptions = new String[this.readUnsignedShort(u + 8)];
                exception = u + 10;

                for (v = 0; v < exceptions.length; ++v) {
                    exceptions[v] = this.readClass(exception, c);
                    exception += 2;
                }
            } else if ("Signature".equals(attr)) {
                signature = this.readUTF8(u + 8, c);
            } else if ("Deprecated".equals(attr)) {
                context.access |= 131072;
            } else if ("RuntimeVisibleAnnotations".equals(attr)) {
                anns = u + 8;
            } else if ("RuntimeVisibleTypeAnnotations".equals(attr)) {
                tanns = u + 8;
            } else if ("AnnotationDefault".equals(attr)) {
                dann = u + 8;
            } else if ("Synthetic".equals(attr)) {
                context.access |= 266240;
            } else if ("RuntimeInvisibleAnnotations".equals(attr)) {
                ianns = u + 8;
            } else if ("RuntimeInvisibleTypeAnnotations".equals(attr)) {
                itanns = u + 8;
            } else if ("RuntimeVisibleParameterAnnotations".equals(attr)) {
                mpanns = u + 8;
            } else if ("RuntimeInvisibleParameterAnnotations".equals(attr)) {
                impanns = u + 8;
            } else if ("MethodParameters".equals(attr)) {
                methodParameters = u + 8;
            } else {
                Attribute attribute = this.readAttribute(context.attrs, attr, u + 8, this.readInt(u + 4), c, -1, (Label[]) null);

                if (attribute != null) {
                    attribute.next = attributes;
                    attributes = attribute;
                }
            }

            u += 6 + this.readInt(u + 4);
        }

        u += 2;
        MethodVisitor methodvisitor = classVisitor.visitMethod(context.access, context.name, context.desc, signature, exceptions);

        if (methodvisitor == null) {
            return u;
        } else {
            if (methodvisitor instanceof MethodWriter) {
                MethodWriter methodwriter = (MethodWriter) methodvisitor;

                if (methodwriter.cw.cr == this && signature == methodwriter.signature) {
                    boolean flag = false;

                    if (exceptions == null) {
                        flag = methodwriter.exceptionCount == 0;
                    } else if (exceptions.length == methodwriter.exceptionCount) {
                        flag = true;

                        for (int j = exceptions.length - 1; j >= 0; --j) {
                            exception -= 2;
                            if (methodwriter.exceptions[j] != this.readUnsignedShort(exception)) {
                                flag = false;
                                break;
                            }
                        }
                    }

                    if (flag) {
                        methodwriter.classReaderOffset = firstAttribute;
                        methodwriter.classReaderLength = u - firstAttribute;
                        return u;
                    }
                }
            }

            int i;

            if (methodParameters != 0) {
                i = this.b[methodParameters] & 255;

                for (v = methodParameters + 1; i > 0; v += 4) {
                    methodvisitor.visitParameter(this.readUTF8(v, c), this.readUnsignedShort(v + 2));
                    --i;
                }
            }

            if (dann != 0) {
                AnnotationVisitor annotationvisitor = methodvisitor.visitAnnotationDefault();

                this.readAnnotationValue(dann, c, (String) null, annotationvisitor);
                if (annotationvisitor != null) {
                    annotationvisitor.visitEnd();
                }
            }

            if (anns != 0) {
                i = this.readUnsignedShort(anns);

                for (v = anns + 2; i > 0; --i) {
                    v = this.readAnnotationValues(v + 2, c, true, methodvisitor.visitAnnotation(this.readUTF8(v, c), true));
                }
            }

            if (ianns != 0) {
                i = this.readUnsignedShort(ianns);

                for (v = ianns + 2; i > 0; --i) {
                    v = this.readAnnotationValues(v + 2, c, true, methodvisitor.visitAnnotation(this.readUTF8(v, c), false));
                }
            }

            if (tanns != 0) {
                i = this.readUnsignedShort(tanns);

                for (v = tanns + 2; i > 0; --i) {
                    v = this.readAnnotationTarget(context, v);
                    v = this.readAnnotationValues(v + 2, c, true, methodvisitor.visitTypeAnnotation(context.typeRef, context.typePath, this.readUTF8(v, c), true));
                }
            }

            if (itanns != 0) {
                i = this.readUnsignedShort(itanns);

                for (v = itanns + 2; i > 0; --i) {
                    v = this.readAnnotationTarget(context, v);
                    v = this.readAnnotationValues(v + 2, c, true, methodvisitor.visitTypeAnnotation(context.typeRef, context.typePath, this.readUTF8(v, c), false));
                }
            }

            if (mpanns != 0) {
                this.readParameterAnnotations(methodvisitor, context, mpanns, true);
            }

            if (impanns != 0) {
                this.readParameterAnnotations(methodvisitor, context, impanns, false);
            }

            while (attributes != null) {
                Attribute attribute1 = attributes.next;

                attributes.next = null;
                methodvisitor.visitAttribute(attributes);
                attributes = attribute1;
            }

            if (code != 0) {
                methodvisitor.visitCode();
                this.readCode(methodvisitor, context, code);
            }

            methodvisitor.visitEnd();
            return u;
        }
    }

    private void readCode(MethodVisitor mv, Context context, int u) {
        byte[] b = this.b;
        char[] c = context.buffer;
        int maxStack = this.readUnsignedShort(u);
        int maxLocals = this.readUnsignedShort(u + 2);
        int codeLength = this.readInt(u + 4);

        u += 8;
        int codeStart = u;
        int codeEnd = u + codeLength;
        Label[] labels = context.labels = new Label[codeLength + 2];

        this.readLabel(codeLength + 1, labels);

        int tanns;
        int tann;

        while (u < codeEnd) {
            tanns = u - codeStart;
            int itanns = b[u] & 255;

            switch (ClassWriter.TYPE[itanns]) {
            case 0:
            case 4:
                ++u;
                break;

            case 1:
            case 3:
            case 11:
                u += 2;
                break;

            case 2:
            case 5:
            case 6:
            case 12:
            case 13:
                u += 3;
                break;

            case 7:
            case 8:
                u += 5;
                break;

            case 9:
                this.readLabel(tanns + this.readShort(u + 1), labels);
                u += 3;
                break;

            case 10:
                this.readLabel(tanns + this.readInt(u + 1), labels);
                u += 5;
                break;

            case 14:
                u = u + 4 - (tanns & 3);
                this.readLabel(tanns + this.readInt(u), labels);

                for (tann = this.readInt(u + 8) - this.readInt(u + 4) + 1; tann > 0; --tann) {
                    this.readLabel(tanns + this.readInt(u + 12), labels);
                    u += 4;
                }

                u += 12;
                break;

            case 15:
                u = u + 4 - (tanns & 3);
                this.readLabel(tanns + this.readInt(u), labels);

                for (tann = this.readInt(u + 4); tann > 0; --tann) {
                    this.readLabel(tanns + this.readInt(u + 12), labels);
                    u += 8;
                }

                u += 8;
                break;

            case 16:
            default:
                u += 4;
                break;

            case 17:
                itanns = b[u + 1] & 255;
                if (itanns == 132) {
                    u += 6;
                } else {
                    u += 4;
                }
                break;

            case 18:
                this.readLabel(tanns + this.readUnsignedShort(u + 1), labels);
                u += 3;
            }
        }

        for (tanns = this.readUnsignedShort(u); tanns > 0; --tanns) {
            Label label = this.readLabel(this.readUnsignedShort(u + 2), labels);
            Label label1 = this.readLabel(this.readUnsignedShort(u + 4), labels);
            Label itann = this.readLabel(this.readUnsignedShort(u + 6), labels);
            String ntoff = this.readUTF8(this.items[this.readUnsignedShort(u + 8)], c);

            mv.visitTryCatchBlock(label, label1, itann, ntoff);
            u += 8;
        }

        u += 2;
        int[] aint = null;
        int[] aint1 = null;

        tann = 0;
        int i = 0;
        int j = -1;
        int nitoff = -1;
        int k = 0;
        int l = 0;
        boolean zip = true;
        boolean unzip = (context.flags & 8) != 0;
        int stackMap = 0;
        int stackMapSize = 0;
        int frameCount = 0;
        Context frame = null;
        Attribute attributes = null;

        int opcodeDelta;
        int v;
        int start;
        int length;
        Label index;

        for (opcodeDelta = this.readUnsignedShort(u); opcodeDelta > 0; --opcodeDelta) {
            String attr = this.readUTF8(u + 2, c);
            Label label2;

            if ("LocalVariableTable".equals(attr)) {
                if ((context.flags & 2) == 0) {
                    k = u + 8;
                    v = this.readUnsignedShort(u + 8);

                    for (start = u; v > 0; --v) {
                        length = this.readUnsignedShort(start + 10);
                        if (labels[length] == null) {
                            label2 = this.readLabel(length, labels);
                            label2.status |= 1;
                        }

                        length += this.readUnsignedShort(start + 12);
                        if (labels[length] == null) {
                            label2 = this.readLabel(length, labels);
                            label2.status |= 1;
                        }

                        start += 10;
                    }
                }
            } else if ("LocalVariableTypeTable".equals(attr)) {
                l = u + 8;
            } else if ("LineNumberTable".equals(attr)) {
                if ((context.flags & 2) == 0) {
                    v = this.readUnsignedShort(u + 8);

                    for (start = u; v > 0; --v) {
                        length = this.readUnsignedShort(start + 10);
                        if (labels[length] == null) {
                            label2 = this.readLabel(length, labels);
                            label2.status |= 1;
                        }

                        for (index = labels[length]; index.line > 0; index = index.next) {
                            if (index.next == null) {
                                index.next = new Label();
                            }
                        }

                        index.line = this.readUnsignedShort(start + 12);
                        start += 4;
                    }
                }
            } else if ("RuntimeVisibleTypeAnnotations".equals(attr)) {
                aint = this.readTypeAnnotations(mv, context, u + 8, true);
                j = aint.length != 0 && this.readByte(aint[0]) >= 67 ? this.readUnsignedShort(aint[0] + 1) : -1;
            } else if ("RuntimeInvisibleTypeAnnotations".equals(attr)) {
                aint1 = this.readTypeAnnotations(mv, context, u + 8, false);
                nitoff = aint1.length != 0 && this.readByte(aint1[0]) >= 67 ? this.readUnsignedShort(aint1[0] + 1) : -1;
            } else if ("StackMapTable".equals(attr)) {
                if ((context.flags & 4) == 0) {
                    stackMap = u + 10;
                    stackMapSize = this.readInt(u + 4);
                    frameCount = this.readUnsignedShort(u + 8);
                }
            } else if ("StackMap".equals(attr)) {
                if ((context.flags & 4) == 0) {
                    zip = false;
                    stackMap = u + 10;
                    stackMapSize = this.readInt(u + 4);
                    frameCount = this.readUnsignedShort(u + 8);
                }
            } else {
                for (v = 0; v < context.attrs.length; ++v) {
                    if (context.attrs[v].type.equals(attr)) {
                        Attribute attribute = context.attrs[v].read(this, u + 8, this.readInt(u + 4), c, codeStart - 8, labels);

                        if (attribute != null) {
                            attribute.next = attributes;
                            attributes = attribute;
                        }
                    }
                }
            }

            u += 6 + this.readInt(u + 4);
        }

        u += 2;
        int i1;

        if (stackMap != 0) {
            frame = context;
            context.offset = -1;
            context.mode = 0;
            context.localCount = 0;
            context.localDiff = 0;
            context.stackCount = 0;
            context.local = new Object[maxLocals];
            context.stack = new Object[maxStack];
            if (unzip) {
                this.getImplicitFrame(context);
            }

            for (opcodeDelta = stackMap; opcodeDelta < stackMap + stackMapSize - 2; ++opcodeDelta) {
                if (b[opcodeDelta] == 8) {
                    i1 = this.readUnsignedShort(opcodeDelta + 1);
                    if (i1 >= 0 && i1 < codeLength && (b[codeStart + i1] & 255) == 187) {
                        this.readLabel(i1, labels);
                    }
                }
            }
        }

        if ((context.flags & 256) != 0) {
            mv.visitFrame(-1, maxLocals, (Object[]) null, 0, (Object[]) null);
        }

        opcodeDelta = (context.flags & 256) == 0 ? -33 : 0;
        u = codeStart;

        int j1;
        String s;
        int k1;

        while (u < codeEnd) {
            i1 = u - codeStart;
            Label label3 = labels[i1];

            if (label3 != null) {
                Label label4 = label3.next;

                label3.next = null;
                mv.visitLabel(label3);
                if ((context.flags & 2) == 0 && label3.line > 0) {
                    mv.visitLineNumber(label3.line, label3);

                    while (label4 != null) {
                        mv.visitLineNumber(label4.line, label3);
                        label4 = label4.next;
                    }
                }
            }

            while (frame != null && (frame.offset == i1 || frame.offset == -1)) {
                if (frame.offset != -1) {
                    if (zip && !unzip) {
                        mv.visitFrame(frame.mode, frame.localDiff, frame.local, frame.stackCount, frame.stack);
                    } else {
                        mv.visitFrame(-1, frame.localCount, frame.local, frame.stackCount, frame.stack);
                    }
                }

                if (frameCount > 0) {
                    stackMap = this.readFrame(stackMap, zip, unzip, frame);
                    --frameCount;
                } else {
                    frame = null;
                }
            }

            start = b[u] & 255;
            Label[] j;
            int bsmArgs;

            switch (ClassWriter.TYPE[start]) {
            case 0:
                mv.visitInsn(start);
                ++u;
                break;

            case 1:
                mv.visitIntInsn(start, b[u + 1]);
                u += 2;
                break;

            case 2:
                mv.visitIntInsn(start, this.readShort(u + 1));
                u += 3;
                break;

            case 3:
                mv.visitVarInsn(start, b[u + 1] & 255);
                u += 2;
                break;

            case 4:
                if (start > 54) {
                    start -= 59;
                    mv.visitVarInsn(54 + (start >> 2), start & 3);
                } else {
                    start -= 26;
                    mv.visitVarInsn(21 + (start >> 2), start & 3);
                }

                ++u;
                break;

            case 5:
                mv.visitTypeInsn(start, this.readClass(u + 1, c));
                u += 3;
                break;

            case 6:
            case 7:
                length = this.items[this.readUnsignedShort(u + 1)];
                boolean flag = b[length - 1] == 11;

                s = this.readClass(length, c);
                length = this.items[this.readUnsignedShort(length + 2)];
                String s1 = this.readUTF8(length, c);
                String s2 = this.readUTF8(length + 2, c);

                if (start < 182) {
                    mv.visitFieldInsn(start, s, s1, s2);
                } else {
                    mv.visitMethodInsn(start, s, s1, s2, flag);
                }

                if (start == 185) {
                    u += 5;
                } else {
                    u += 3;
                }
                break;

            case 8:
                length = this.items[this.readUnsignedShort(u + 1)];
                j1 = context.bootstrapMethods[this.readUnsignedShort(length)];
                Handle handle = (Handle) this.readConst(this.readUnsignedShort(j1), c);

                k1 = this.readUnsignedShort(j1 + 2);
                Object[] aobject = new Object[k1];

                j1 += 4;

                for (int iname = 0; iname < k1; ++iname) {
                    aobject[iname] = this.readConst(this.readUnsignedShort(j1), c);
                    j1 += 2;
                }

                length = this.items[this.readUnsignedShort(length + 2)];
                String s3 = this.readUTF8(length, c);
                String idesc = this.readUTF8(length + 2, c);

                mv.visitInvokeDynamicInsn(s3, idesc, handle, aobject);
                u += 5;
                break;

            case 9:
                mv.visitJumpInsn(start, labels[i1 + this.readShort(u + 1)]);
                u += 3;
                break;

            case 10:
                mv.visitJumpInsn(start + opcodeDelta, labels[i1 + this.readInt(u + 1)]);
                u += 5;
                break;

            case 11:
                mv.visitLdcInsn(this.readConst(b[u + 1] & 255, c));
                u += 2;
                break;

            case 12:
                mv.visitLdcInsn(this.readConst(this.readUnsignedShort(u + 1), c));
                u += 3;
                break;

            case 13:
                mv.visitIincInsn(b[u + 1] & 255, b[u + 2]);
                u += 3;
                break;

            case 14:
                u = u + 4 - (i1 & 3);
                length = i1 + this.readInt(u);
                j1 = this.readInt(u + 4);
                int l1 = this.readInt(u + 8);

                j = new Label[l1 - j1 + 1];
                u += 12;

                for (bsmArgs = 0; bsmArgs < j.length; ++bsmArgs) {
                    j[bsmArgs] = labels[i1 + this.readInt(u)];
                    u += 4;
                }

                mv.visitTableSwitchInsn(j1, l1, labels[length], j);
                break;

            case 15:
                u = u + 4 - (i1 & 3);
                length = i1 + this.readInt(u);
                j1 = this.readInt(u + 4);
                int[] vsignature = new int[j1];

                j = new Label[j1];
                u += 8;

                for (bsmArgs = 0; bsmArgs < j1; ++bsmArgs) {
                    vsignature[bsmArgs] = this.readInt(u);
                    j[bsmArgs] = labels[i1 + this.readInt(u + 4)];
                    u += 8;
                }

                mv.visitLookupSwitchInsn(labels[length], vsignature, j);
                break;

            case 16:
            default:
                mv.visitMultiANewArrayInsn(this.readClass(u + 1, c), b[u + 3] & 255);
                u += 4;
                break;

            case 17:
                start = b[u + 1] & 255;
                if (start == 132) {
                    mv.visitIincInsn(this.readUnsignedShort(u + 2), this.readShort(u + 4));
                    u += 6;
                } else {
                    mv.visitVarInsn(start, this.readUnsignedShort(u + 2));
                    u += 4;
                }
                break;

            case 18:
                start = start < 218 ? start - 49 : start - 20;
                Label label5 = labels[i1 + this.readUnsignedShort(u + 1)];

                if (start != 167 && start != 168) {
                    start = start <= 166 ? (start + 1 ^ 1) - 1 : start ^ 1;
                    index = new Label();
                    mv.visitJumpInsn(start, index);
                    mv.visitJumpInsn(200, label5);
                    mv.visitLabel(index);
                    if (stackMap != 0 && (frame == null || frame.offset != i1 + 3)) {
                        mv.visitFrame(256, 0, (Object[]) null, 0, (Object[]) null);
                    }
                } else {
                    mv.visitJumpInsn(start + 33, label5);
                }

                u += 3;
            }

            while (aint != null && tann < aint.length && j <= i1) {
                if (j == i1) {
                    length = this.readAnnotationTarget(context, aint[tann]);
                    this.readAnnotationValues(length + 2, c, true, mv.visitInsnAnnotation(context.typeRef, context.typePath, this.readUTF8(length, c), true));
                }

                ++tann;
                j = tann < aint.length && this.readByte(aint[tann]) >= 67 ? this.readUnsignedShort(aint[tann] + 1) : -1;
            }

            while (aint1 != null && i < aint1.length && nitoff <= i1) {
                if (nitoff == i1) {
                    length = this.readAnnotationTarget(context, aint1[i]);
                    this.readAnnotationValues(length + 2, c, true, mv.visitInsnAnnotation(context.typeRef, context.typePath, this.readUTF8(length, c), false));
                }

                ++i;
                nitoff = i < aint1.length && this.readByte(aint1[i]) >= 67 ? this.readUnsignedShort(aint1[i] + 1) : -1;
            }
        }

        if (labels[codeLength] != null) {
            mv.visitLabel(labels[codeLength]);
        }

        if ((context.flags & 2) == 0 && k != 0) {
            int[] aint2 = null;

            if (l != 0) {
                u = l + 2;
                aint2 = new int[this.readUnsignedShort(l) * 3];

                for (v = aint2.length; v > 0; u += 10) {
                    --v;
                    aint2[v] = u + 6;
                    --v;
                    aint2[v] = this.readUnsignedShort(u + 8);
                    --v;
                    aint2[v] = this.readUnsignedShort(u);
                }
            }

            u = k + 2;

            for (v = this.readUnsignedShort(k); v > 0; --v) {
                start = this.readUnsignedShort(u);
                length = this.readUnsignedShort(u + 2);
                j1 = this.readUnsignedShort(u + 8);
                s = null;
                if (aint2 != null) {
                    for (k1 = 0; k1 < aint2.length; k1 += 3) {
                        if (aint2[k1] == start && aint2[k1 + 1] == j1) {
                            s = this.readUTF8(aint2[k1 + 2], c);
                            break;
                        }
                    }
                }

                mv.visitLocalVariable(this.readUTF8(u + 4, c), this.readUTF8(u + 6, c), s, labels[start], labels[start + length], j1);
                u += 10;
            }
        }

        if (aint != null) {
            for (i1 = 0; i1 < aint.length; ++i1) {
                if (this.readByte(aint[i1]) >> 1 == 32) {
                    v = this.readAnnotationTarget(context, aint[i1]);
                    this.readAnnotationValues(v + 2, c, true, mv.visitLocalVariableAnnotation(context.typeRef, context.typePath, context.start, context.end, context.index, this.readUTF8(v, c), true));
                }
            }
        }

        if (aint1 != null) {
            for (i1 = 0; i1 < aint1.length; ++i1) {
                if (this.readByte(aint1[i1]) >> 1 == 32) {
                    v = this.readAnnotationTarget(context, aint1[i1]);
                    this.readAnnotationValues(v + 2, c, true, mv.visitLocalVariableAnnotation(context.typeRef, context.typePath, context.start, context.end, context.index, this.readUTF8(v, c), false));
                }
            }
        }

        while (attributes != null) {
            Attribute attribute1 = attributes.next;

            attributes.next = null;
            mv.visitAttribute(attributes);
            attributes = attribute1;
        }

        mv.visitMaxs(maxStack, maxLocals);
    }

    private int[] readTypeAnnotations(MethodVisitor mv, Context context, int u, boolean visible) {
        char[] c = context.buffer;
        int[] offsets = new int[this.readUnsignedShort(u)];

        u += 2;

        for (int i = 0; i < offsets.length; ++i) {
            offsets[i] = u;
            int target = this.readInt(u);
            int pathLength;

            switch (target >>> 24) {
            case 0:
            case 1:
            case 22:
                u += 2;
                break;

            case 19:
            case 20:
            case 21:
                ++u;
                break;

            case 64:
            case 65:
                for (pathLength = this.readUnsignedShort(u + 1); pathLength > 0; --pathLength) {
                    int path = this.readUnsignedShort(u + 3);
                    int length = this.readUnsignedShort(u + 5);

                    this.readLabel(path, context.labels);
                    this.readLabel(path + length, context.labels);
                    u += 6;
                }

                u += 3;
                break;

            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
                u += 4;
                break;

            default:
                u += 3;
            }

            pathLength = this.readByte(u);
            if (target >>> 24 == 66) {
                TypePath typepath = pathLength == 0 ? null : new TypePath(this.b, u);

                u += 1 + 2 * pathLength;
                u = this.readAnnotationValues(u + 2, c, true, mv.visitTryCatchAnnotation(target, typepath, this.readUTF8(u, c), visible));
            } else {
                u = this.readAnnotationValues(u + 3 + 2 * pathLength, c, true, (AnnotationVisitor) null);
            }
        }

        return offsets;
    }

    private int readAnnotationTarget(Context context, int u) {
        int target;
        int pathLength;

        target = this.readInt(u);
        label29:
        switch (target >>> 24) {
        case 0:
        case 1:
        case 22:
            target &= -65536;
            u += 2;
            break;

        case 19:
        case 20:
        case 21:
            target &= -16777216;
            ++u;
            break;

        case 64:
        case 65:
            target &= -16777216;
            pathLength = this.readUnsignedShort(u + 1);
            context.start = new Label[pathLength];
            context.end = new Label[pathLength];
            context.index = new int[pathLength];
            u += 3;
            int i = 0;

            while (true) {
                if (i >= pathLength) {
                    break label29;
                }

                int start = this.readUnsignedShort(u);
                int length = this.readUnsignedShort(u + 2);

                context.start[i] = this.readLabel(start, context.labels);
                context.end[i] = this.readLabel(start + length, context.labels);
                context.index[i] = this.readUnsignedShort(u + 4);
                u += 6;
                ++i;
            }

        case 71:
        case 72:
        case 73:
        case 74:
        case 75:
            target &= -16776961;
            u += 4;
            break;

        default:
            target &= target >>> 24 < 67 ? -256 : -16777216;
            u += 3;
        }

        pathLength = this.readByte(u);
        context.typeRef = target;
        context.typePath = pathLength == 0 ? null : new TypePath(this.b, u);
        return u + 1 + 2 * pathLength;
    }

    private void readParameterAnnotations(MethodVisitor mv, Context context, int v, boolean visible) {
        int n = this.b[v++] & 255;
        int synthetics = Type.getArgumentTypes(context.desc).length - n;

        int i;
        AnnotationVisitor av;

        for (i = 0; i < synthetics; ++i) {
            av = mv.visitParameterAnnotation(i, "Ljava/lang/Synthetic;", false);
            if (av != null) {
                av.visitEnd();
            }
        }

        for (char[] c = context.buffer; i < n + synthetics; ++i) {
            int j = this.readUnsignedShort(v);

            for (v += 2; j > 0; --j) {
                av = mv.visitParameterAnnotation(i, this.readUTF8(v, c), visible);
                v = this.readAnnotationValues(v + 2, c, true, av);
            }
        }

    }

    private int readAnnotationValues(int v, char[] buf, boolean named, AnnotationVisitor av) {
        int i = this.readUnsignedShort(v);

        v += 2;
        if (named) {
            while (i > 0) {
                v = this.readAnnotationValue(v + 2, buf, this.readUTF8(v, buf), av);
                --i;
            }
        } else {
            while (i > 0) {
                v = this.readAnnotationValue(v, buf, (String) null, av);
                --i;
            }
        }

        if (av != null) {
            av.visitEnd();
        }

        return v;
    }

    private int readAnnotationValue(int v, char[] buf, String name, AnnotationVisitor av) {
        if (av == null) {
            switch (this.b[v] & 255) {
            case 64:
                return this.readAnnotationValues(v + 3, buf, true, (AnnotationVisitor) null);

            case 91:
                return this.readAnnotationValues(v + 1, buf, false, (AnnotationVisitor) null);

            case 101:
                return v + 5;

            default:
                return v + 3;
            }
        } else {
            switch (this.b[v++] & 255) {
            case 64:
                v = this.readAnnotationValues(v + 2, buf, true, av.visitAnnotation(name, this.readUTF8(v, buf)));

            case 65:
            case 69:
            case 71:
            case 72:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            case 100:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 114:
            default:
                break;

            case 66:
                av.visit(name, Byte.valueOf((byte) this.readInt(this.items[this.readUnsignedShort(v)])));
                v += 2;
                break;

            case 67:
                av.visit(name, Character.valueOf((char) this.readInt(this.items[this.readUnsignedShort(v)])));
                v += 2;
                break;

            case 68:
            case 70:
            case 73:
            case 74:
                av.visit(name, this.readConst(this.readUnsignedShort(v), buf));
                v += 2;
                break;

            case 83:
                av.visit(name, Short.valueOf((short) this.readInt(this.items[this.readUnsignedShort(v)])));
                v += 2;
                break;

            case 90:
                av.visit(name, this.readInt(this.items[this.readUnsignedShort(v)]) == 0 ? Boolean.FALSE : Boolean.TRUE);
                v += 2;
                break;

            case 91:
                int size = this.readUnsignedShort(v);

                v += 2;
                if (size == 0) {
                    return this.readAnnotationValues(v - 2, buf, false, av.visitArray(name));
                }

                int i;

                switch (this.b[v++] & 255) {
                case 66:
                    byte[] bv = new byte[size];

                    for (i = 0; i < size; ++i) {
                        bv[i] = (byte) this.readInt(this.items[this.readUnsignedShort(v)]);
                        v += 3;
                    }

                    av.visit(name, bv);
                    --v;
                    return v;

                case 67:
                    char[] cv = new char[size];

                    for (i = 0; i < size; ++i) {
                        cv[i] = (char) this.readInt(this.items[this.readUnsignedShort(v)]);
                        v += 3;
                    }

                    av.visit(name, cv);
                    --v;
                    return v;

                case 68:
                    double[] dv = new double[size];

                    for (i = 0; i < size; ++i) {
                        dv[i] = Double.longBitsToDouble(this.readLong(this.items[this.readUnsignedShort(v)]));
                        v += 3;
                    }

                    av.visit(name, dv);
                    --v;
                    return v;

                case 69:
                case 71:
                case 72:
                case 75:
                case 76:
                case 77:
                case 78:
                case 79:
                case 80:
                case 81:
                case 82:
                case 84:
                case 85:
                case 86:
                case 87:
                case 88:
                case 89:
                default:
                    v = this.readAnnotationValues(v - 3, buf, false, av.visitArray(name));
                    return v;

                case 70:
                    float[] fv = new float[size];

                    for (i = 0; i < size; ++i) {
                        fv[i] = Float.intBitsToFloat(this.readInt(this.items[this.readUnsignedShort(v)]));
                        v += 3;
                    }

                    av.visit(name, fv);
                    --v;
                    return v;

                case 73:
                    int[] iv = new int[size];

                    for (i = 0; i < size; ++i) {
                        iv[i] = this.readInt(this.items[this.readUnsignedShort(v)]);
                        v += 3;
                    }

                    av.visit(name, iv);
                    --v;
                    return v;

                case 74:
                    long[] lv = new long[size];

                    for (i = 0; i < size; ++i) {
                        lv[i] = this.readLong(this.items[this.readUnsignedShort(v)]);
                        v += 3;
                    }

                    av.visit(name, lv);
                    --v;
                    return v;

                case 83:
                    short[] sv = new short[size];

                    for (i = 0; i < size; ++i) {
                        sv[i] = (short) this.readInt(this.items[this.readUnsignedShort(v)]);
                        v += 3;
                    }

                    av.visit(name, sv);
                    --v;
                    return v;

                case 90:
                    boolean[] zv = new boolean[size];

                    for (i = 0; i < size; ++i) {
                        zv[i] = this.readInt(this.items[this.readUnsignedShort(v)]) != 0;
                        v += 3;
                    }

                    av.visit(name, zv);
                    --v;
                    return v;
                }

            case 99:
                av.visit(name, Type.getType(this.readUTF8(v, buf)));
                v += 2;
                break;

            case 101:
                av.visitEnum(name, this.readUTF8(v, buf), this.readUTF8(v + 2, buf));
                v += 4;
                break;

            case 115:
                av.visit(name, this.readUTF8(v, buf));
                v += 2;
            }

            return v;
        }
    }

    private void getImplicitFrame(Context frame) {
        String desc = frame.desc;
        Object[] locals = frame.local;
        int local = 0;

        if ((frame.access & 8) == 0) {
            if ("<init>".equals(frame.name)) {
                locals[local++] = Opcodes.UNINITIALIZED_THIS;
            } else {
                locals[local++] = this.readClass(this.header + 2, frame.buffer);
            }
        }

        int i = 1;

        while (true) {
            int j = i;

            switch (desc.charAt(i++)) {
            case 'B':
            case 'C':
            case 'I':
            case 'S':
            case 'Z':
                locals[local++] = Opcodes.INTEGER;
                break;

            case 'D':
                locals[local++] = Opcodes.DOUBLE;
                break;

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
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            default:
                frame.localCount = local;
                return;

            case 'F':
                locals[local++] = Opcodes.FLOAT;
                break;

            case 'J':
                locals[local++] = Opcodes.LONG;
                break;

            case 'L':
                while (desc.charAt(i) != 59) {
                    ++i;
                }

                locals[local++] = desc.substring(j + 1, i++);
                break;

            case '[':
                while (desc.charAt(i) == 91) {
                    ++i;
                }

                if (desc.charAt(i) == 76) {
                    ++i;

                    while (desc.charAt(i) != 59) {
                        ++i;
                    }
                }

                int i = local++;

                ++i;
                locals[i] = desc.substring(j, i);
            }
        }
    }

    private int readFrame(int stackMap, boolean zip, boolean unzip, Context frame) {
        char[] c = frame.buffer;
        Label[] labels = frame.labels;
        int tag;

        if (zip) {
            tag = this.b[stackMap++] & 255;
        } else {
            tag = 255;
            frame.offset = -1;
        }

        frame.localDiff = 0;
        int delta;

        if (tag < 64) {
            delta = tag;
            frame.mode = 3;
            frame.stackCount = 0;
        } else if (tag < 128) {
            delta = tag - 64;
            stackMap = this.readFrameType(frame.stack, 0, stackMap, c, labels);
            frame.mode = 4;
            frame.stackCount = 1;
        } else {
            delta = this.readUnsignedShort(stackMap);
            stackMap += 2;
            if (tag == 247) {
                stackMap = this.readFrameType(frame.stack, 0, stackMap, c, labels);
                frame.mode = 4;
                frame.stackCount = 1;
            } else if (tag >= 248 && tag < 251) {
                frame.mode = 2;
                frame.localDiff = 251 - tag;
                frame.localCount -= frame.localDiff;
                frame.stackCount = 0;
            } else if (tag == 251) {
                frame.mode = 3;
                frame.stackCount = 0;
            } else {
                int n;
                int stack;

                if (tag < 255) {
                    n = unzip ? frame.localCount : 0;

                    for (stack = tag - 251; stack > 0; --stack) {
                        stackMap = this.readFrameType(frame.local, n++, stackMap, c, labels);
                    }

                    frame.mode = 1;
                    frame.localDiff = tag - 251;
                    frame.localCount += frame.localDiff;
                    frame.stackCount = 0;
                } else {
                    frame.mode = 0;
                    n = this.readUnsignedShort(stackMap);
                    stackMap += 2;
                    frame.localDiff = n;
                    frame.localCount = n;

                    for (stack = 0; n > 0; --n) {
                        stackMap = this.readFrameType(frame.local, stack++, stackMap, c, labels);
                    }

                    n = this.readUnsignedShort(stackMap);
                    stackMap += 2;
                    frame.stackCount = n;

                    for (stack = 0; n > 0; --n) {
                        stackMap = this.readFrameType(frame.stack, stack++, stackMap, c, labels);
                    }
                }
            }
        }

        frame.offset += delta + 1;
        this.readLabel(frame.offset, labels);
        return stackMap;
    }

    private int readFrameType(Object[] frame, int index, int v, char[] buf, Label[] labels) {
        int type = this.b[v++] & 255;

        switch (type) {
        case 0:
            frame[index] = Opcodes.TOP;
            break;

        case 1:
            frame[index] = Opcodes.INTEGER;
            break;

        case 2:
            frame[index] = Opcodes.FLOAT;
            break;

        case 3:
            frame[index] = Opcodes.DOUBLE;
            break;

        case 4:
            frame[index] = Opcodes.LONG;
            break;

        case 5:
            frame[index] = Opcodes.NULL;
            break;

        case 6:
            frame[index] = Opcodes.UNINITIALIZED_THIS;
            break;

        case 7:
            frame[index] = this.readClass(v, buf);
            v += 2;
            break;

        default:
            frame[index] = this.readLabel(this.readUnsignedShort(v), labels);
            v += 2;
        }

        return v;
    }

    protected Label readLabel(int offset, Label[] labels) {
        if (labels[offset] == null) {
            labels[offset] = new Label();
        }

        return labels[offset];
    }

    private int getAttributes() {
        int u = this.header + 8 + this.readUnsignedShort(this.header + 6) * 2;

        int i;
        int j;

        for (i = this.readUnsignedShort(u); i > 0; --i) {
            for (j = this.readUnsignedShort(u + 8); j > 0; --j) {
                u += 6 + this.readInt(u + 12);
            }

            u += 8;
        }

        u += 2;

        for (i = this.readUnsignedShort(u); i > 0; --i) {
            for (j = this.readUnsignedShort(u + 8); j > 0; --j) {
                u += 6 + this.readInt(u + 12);
            }

            u += 8;
        }

        return u + 2;
    }

    private Attribute readAttribute(Attribute[] attrs, String type, int off, int len, char[] buf, int codeOff, Label[] labels) {
        for (int i = 0; i < attrs.length; ++i) {
            if (attrs[i].type.equals(type)) {
                return attrs[i].read(this, off, len, buf, codeOff, labels);
            }
        }

        return (new Attribute(type)).read(this, off, len, (char[]) null, -1, (Label[]) null);
    }

    public int getItemCount() {
        return this.items.length;
    }

    public int getItem(int item) {
        return this.items[item];
    }

    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    public int readByte(int index) {
        return this.b[index] & 255;
    }

    public int readUnsignedShort(int index) {
        byte[] b = this.b;

        return (b[index] & 255) << 8 | b[index + 1] & 255;
    }

    public short readShort(int index) {
        byte[] b = this.b;

        return (short) ((b[index] & 255) << 8 | b[index + 1] & 255);
    }

    public int readInt(int index) {
        byte[] b = this.b;

        return (b[index] & 255) << 24 | (b[index + 1] & 255) << 16 | (b[index + 2] & 255) << 8 | b[index + 3] & 255;
    }

    public long readLong(int index) {
        long l1 = (long) this.readInt(index);
        long l0 = (long) this.readInt(index + 4) & 4294967295L;

        return l1 << 32 | l0;
    }

    public String readUTF8(int index, char[] buf) {
        int item = this.readUnsignedShort(index);

        if (index != 0 && item != 0) {
            String s = this.strings[item];

            if (s != null) {
                return s;
            } else {
                index = this.items[item];
                return this.strings[item] = this.readUTF(index + 2, this.readUnsignedShort(index), buf);
            }
        } else {
            return null;
        }
    }

    private String readUTF(int index, int utfLen, char[] buf) {
        int endIndex = index + utfLen;
        byte[] b = this.b;
        int strLen = 0;
        byte st = 0;
        char cc = 0;

        while (index < endIndex) {
            byte c = b[index++];

            switch (st) {
            case 0:
                int i = c & 255;

                if (i < 128) {
                    buf[strLen++] = (char) i;
                } else {
                    if (i < 224 && i > 191) {
                        cc = (char) (i & 31);
                        st = 1;
                        continue;
                    }

                    cc = (char) (i & 15);
                    st = 2;
                }
                break;

            case 1:
                buf[strLen++] = (char) (cc << 6 | c & 63);
                st = 0;
                break;

            case 2:
                cc = (char) (cc << 6 | c & 63);
                st = 1;
            }
        }

        return new String(buf, 0, strLen);
    }

    public String readClass(int index, char[] buf) {
        return this.readUTF8(this.items[this.readUnsignedShort(index)], buf);
    }

    public Object readConst(int item, char[] buf) {
        int index = this.items[item];

        switch (this.b[index - 1]) {
        case 3:
            return Integer.valueOf(this.readInt(index));

        case 4:
            return Float.valueOf(Float.intBitsToFloat(this.readInt(index)));

        case 5:
            return Long.valueOf(this.readLong(index));

        case 6:
            return Double.valueOf(Double.longBitsToDouble(this.readLong(index)));

        case 7:
            return Type.getObjectType(this.readUTF8(index, buf));

        case 8:
            return this.readUTF8(index, buf);

        case 9:
        case 10:
        case 11:
        case 12:
        case 13:
        case 14:
        case 15:
        default:
            int tag = this.readByte(index);
            int[] items = this.items;
            int cpIndex = items[this.readUnsignedShort(index + 1)];
            boolean itf = this.b[cpIndex - 1] == 11;
            String owner = this.readClass(cpIndex, buf);

            cpIndex = items[this.readUnsignedShort(cpIndex + 2)];
            String name = this.readUTF8(cpIndex, buf);
            String desc = this.readUTF8(cpIndex + 2, buf);

            return new Handle(tag, owner, name, desc, itf);

        case 16:
            return Type.getMethodType(this.readUTF8(index, buf));
        }
    }
}
