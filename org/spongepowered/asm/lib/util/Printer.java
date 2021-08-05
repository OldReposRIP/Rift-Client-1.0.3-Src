package org.spongepowered.asm.lib.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.lib.Attribute;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.Label;
import org.spongepowered.asm.lib.TypePath;

public abstract class Printer {

    public static final String[] OPCODES;
    public static final String[] TYPES;
    public static final String[] HANDLE_TAG;
    protected final int api;
    protected final StringBuffer buf;
    public final List text;

    protected Printer(int api) {
        this.api = api;
        this.buf = new StringBuffer();
        this.text = new ArrayList();
    }

    public abstract void visit(int i, int j, String s, String s1, String s2, String[] astring);

    public abstract void visitSource(String s, String s1);

    public abstract void visitOuterClass(String s, String s1, String s2);

    public abstract Printer visitClassAnnotation(String s, boolean flag);

    public Printer visitClassTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        throw new RuntimeException("Must be overriden");
    }

    public abstract void visitClassAttribute(Attribute attribute);

    public abstract void visitInnerClass(String s, String s1, String s2, int i);

    public abstract Printer visitField(int i, String s, String s1, String s2, Object object);

    public abstract Printer visitMethod(int i, String s, String s1, String s2, String[] astring);

    public abstract void visitClassEnd();

    public abstract void visit(String s, Object object);

    public abstract void visitEnum(String s, String s1, String s2);

    public abstract Printer visitAnnotation(String s, String s1);

    public abstract Printer visitArray(String s);

    public abstract void visitAnnotationEnd();

    public abstract Printer visitFieldAnnotation(String s, boolean flag);

    public Printer visitFieldTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        throw new RuntimeException("Must be overriden");
    }

    public abstract void visitFieldAttribute(Attribute attribute);

    public abstract void visitFieldEnd();

    public void visitParameter(String name, int access) {
        throw new RuntimeException("Must be overriden");
    }

    public abstract Printer visitAnnotationDefault();

    public abstract Printer visitMethodAnnotation(String s, boolean flag);

    public Printer visitMethodTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        throw new RuntimeException("Must be overriden");
    }

    public abstract Printer visitParameterAnnotation(int i, String s, boolean flag);

    public abstract void visitMethodAttribute(Attribute attribute);

    public abstract void visitCode();

    public abstract void visitFrame(int i, int j, Object[] aobject, int k, Object[] aobject1);

    public abstract void visitInsn(int i);

    public abstract void visitIntInsn(int i, int j);

    public abstract void visitVarInsn(int i, int j);

    public abstract void visitTypeInsn(int i, String s);

    public abstract void visitFieldInsn(int i, String s, String s1, String s2);

    /** @deprecated */
    @Deprecated
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (this.api >= 327680) {
            boolean itf = opcode == 185;

            this.visitMethodInsn(opcode, owner, name, desc, itf);
        } else {
            throw new RuntimeException("Must be overriden");
        }
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (this.api < 327680) {
            if (itf != (opcode == 185)) {
                throw new IllegalArgumentException("INVOKESPECIAL/STATIC on interfaces require ASM 5");
            } else {
                this.visitMethodInsn(opcode, owner, name, desc);
            }
        } else {
            throw new RuntimeException("Must be overriden");
        }
    }

    public abstract void visitInvokeDynamicInsn(String s, String s1, Handle handle, Object... aobject);

    public abstract void visitJumpInsn(int i, Label label);

    public abstract void visitLabel(Label label);

    public abstract void visitLdcInsn(Object object);

    public abstract void visitIincInsn(int i, int j);

    public abstract void visitTableSwitchInsn(int i, int j, Label label, Label... alabel);

    public abstract void visitLookupSwitchInsn(Label label, int[] aint, Label[] alabel);

    public abstract void visitMultiANewArrayInsn(String s, int i);

    public Printer visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        throw new RuntimeException("Must be overriden");
    }

    public abstract void visitTryCatchBlock(Label label, Label label1, Label label2, String s);

    public Printer visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        throw new RuntimeException("Must be overriden");
    }

    public abstract void visitLocalVariable(String s, String s1, String s2, Label label, Label label1, int i);

    public Printer visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
        throw new RuntimeException("Must be overriden");
    }

    public abstract void visitLineNumber(int i, Label label);

    public abstract void visitMaxs(int i, int j);

    public abstract void visitMethodEnd();

    public List getText() {
        return this.text;
    }

    public void print(PrintWriter pw) {
        printList(pw, this.text);
    }

    public static void appendString(StringBuffer buf, String s) {
        buf.append('\"');

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);

            if (c == 10) {
                buf.append("\\n");
            } else if (c == 13) {
                buf.append("\\r");
            } else if (c == 92) {
                buf.append("\\\\");
            } else if (c == 34) {
                buf.append("\\\"");
            } else if (c >= 32 && c <= 127) {
                buf.append(c);
            } else {
                buf.append("\\u");
                if (c < 16) {
                    buf.append("000");
                } else if (c < 256) {
                    buf.append("00");
                } else if (c < 4096) {
                    buf.append('0');
                }

                buf.append(Integer.toString(c, 16));
            }
        }

        buf.append('\"');
    }

    static void printList(PrintWriter pw, List l) {
        for (int i = 0; i < l.size(); ++i) {
            Object o = l.get(i);

            if (o instanceof List) {
                printList(pw, (List) o);
            } else {
                pw.print(o.toString());
            }
        }

    }

    static {
        String s = "NOP,ACONST_NULL,ICONST_M1,ICONST_0,ICONST_1,ICONST_2,ICONST_3,ICONST_4,ICONST_5,LCONST_0,LCONST_1,FCONST_0,FCONST_1,FCONST_2,DCONST_0,DCONST_1,BIPUSH,SIPUSH,LDC,,,ILOAD,LLOAD,FLOAD,DLOAD,ALOAD,,,,,,,,,,,,,,,,,,,,,IALOAD,LALOAD,FALOAD,DALOAD,AALOAD,BALOAD,CALOAD,SALOAD,ISTORE,LSTORE,FSTORE,DSTORE,ASTORE,,,,,,,,,,,,,,,,,,,,,IASTORE,LASTORE,FASTORE,DASTORE,AASTORE,BASTORE,CASTORE,SASTORE,POP,POP2,DUP,DUP_X1,DUP_X2,DUP2,DUP2_X1,DUP2_X2,SWAP,IADD,LADD,FADD,DADD,ISUB,LSUB,FSUB,DSUB,IMUL,LMUL,FMUL,DMUL,IDIV,LDIV,FDIV,DDIV,IREM,LREM,FREM,DREM,INEG,LNEG,FNEG,DNEG,ISHL,LSHL,ISHR,LSHR,IUSHR,LUSHR,IAND,LAND,IOR,LOR,IXOR,LXOR,IINC,I2L,I2F,I2D,L2I,L2F,L2D,F2I,F2L,F2D,D2I,D2L,D2F,I2B,I2C,I2S,LCMP,FCMPL,FCMPG,DCMPL,DCMPG,IFEQ,IFNE,IFLT,IFGE,IFGT,IFLE,IF_ICMPEQ,IF_ICMPNE,IF_ICMPLT,IF_ICMPGE,IF_ICMPGT,IF_ICMPLE,IF_ACMPEQ,IF_ACMPNE,GOTO,JSR,RET,TABLESWITCH,LOOKUPSWITCH,IRETURN,LRETURN,FRETURN,DRETURN,ARETURN,RETURN,GETSTATIC,PUTSTATIC,GETFIELD,PUTFIELD,INVOKEVIRTUAL,INVOKESPECIAL,INVOKESTATIC,INVOKEINTERFACE,INVOKEDYNAMIC,NEW,NEWARRAY,ANEWARRAY,ARRAYLENGTH,ATHROW,CHECKCAST,INSTANCEOF,MONITORENTER,MONITOREXIT,,MULTIANEWARRAY,IFNULL,IFNONNULL,";

        OPCODES = new String[200];
        int i = 0;

        int j;
        int l;

        for (j = 0; (l = s.indexOf(44, j)) > 0; j = l + 1) {
            Printer.OPCODES[i++] = j + 1 == l ? null : s.substring(j, l);
        }

        s = "T_BOOLEAN,T_CHAR,T_FLOAT,T_DOUBLE,T_BYTE,T_SHORT,T_INT,T_LONG,";
        TYPES = new String[12];
        j = 0;

        for (i = 4; (l = s.indexOf(44, j)) > 0; j = l + 1) {
            Printer.TYPES[i++] = s.substring(j, l);
        }

        s = "H_GETFIELD,H_GETSTATIC,H_PUTFIELD,H_PUTSTATIC,H_INVOKEVIRTUAL,H_INVOKESTATIC,H_INVOKESPECIAL,H_NEWINVOKESPECIAL,H_INVOKEINTERFACE,";
        HANDLE_TAG = new String[10];
        j = 0;

        for (i = 1; (l = s.indexOf(44, j)) > 0; j = l + 1) {
            Printer.HANDLE_TAG[i++] = s.substring(j, l);
        }

    }
}
