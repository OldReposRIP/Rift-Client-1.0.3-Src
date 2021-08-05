package org.spongepowered.asm.lib.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.spongepowered.asm.lib.AnnotationVisitor;
import org.spongepowered.asm.lib.Attribute;
import org.spongepowered.asm.lib.ClassVisitor;
import org.spongepowered.asm.lib.Handle;
import org.spongepowered.asm.lib.Label;
import org.spongepowered.asm.lib.MethodVisitor;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.TypePath;

public class MethodNode extends MethodVisitor {

    public int access;
    public String name;
    public String desc;
    public String signature;
    public List exceptions;
    public List nameeters;
    public List visibleAnnotations;
    public List invisibleAnnotations;
    public List visibleTypeAnnotations;
    public List invisibleTypeAnnotations;
    public List attrs;
    public Object annotationDefault;
    public List[] visibleParameterAnnotations;
    public List[] invisibleParameterAnnotations;
    public InsnList instructions;
    public List tryCatchBlocks;
    public int maxStack;
    public int maxLocals;
    public List localVariables;
    public List visibleLocalVariableAnnotations;
    public List invisibleLocalVariableAnnotations;
    private boolean visited;

    public MethodNode() {
        this(327680);
        if (this.getClass() != MethodNode.class) {
            throw new IllegalStateException();
        }
    }

    public MethodNode(int api) {
        super(api);
        this.instructions = new InsnList();
    }

    public MethodNode(int access, String name, String desc, String signature, String[] exceptions) {
        this(327680, access, name, desc, signature, exceptions);
        if (this.getClass() != MethodNode.class) {
            throw new IllegalStateException();
        }
    }

    public MethodNode(int api, int access, String name, String desc, String signature, String[] exceptions) {
        super(api);
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.exceptions = new ArrayList(exceptions == null ? 0 : exceptions.length);
        boolean isAbstract = (access & 1024) != 0;

        if (!isAbstract) {
            this.localVariables = new ArrayList(5);
        }

        this.tryCatchBlocks = new ArrayList();
        if (exceptions != null) {
            this.exceptions.addAll(Arrays.asList(exceptions));
        }

        this.instructions = new InsnList();
    }

    public void visitParameter(String name, int access) {
        if (this.nameeters == null) {
            this.nameeters = new ArrayList(5);
        }

        this.nameeters.add(new ParameterNode(name, access));
    }

    public AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationNode(new ArrayList(0) {
            public boolean add(Object o) {
                MethodNode.this.annotationDefault = o;
                return super.add(o);
            }
        });
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationNode an = new AnnotationNode(desc);

        if (visible) {
            if (this.visibleAnnotations == null) {
                this.visibleAnnotations = new ArrayList(1);
            }

            this.visibleAnnotations.add(an);
        } else {
            if (this.invisibleAnnotations == null) {
                this.invisibleAnnotations = new ArrayList(1);
            }

            this.invisibleAnnotations.add(an);
        }

        return an;
    }

    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        TypeAnnotationNode an = new TypeAnnotationNode(typeRef, typePath, desc);

        if (visible) {
            if (this.visibleTypeAnnotations == null) {
                this.visibleTypeAnnotations = new ArrayList(1);
            }

            this.visibleTypeAnnotations.add(an);
        } else {
            if (this.invisibleTypeAnnotations == null) {
                this.invisibleTypeAnnotations = new ArrayList(1);
            }

            this.invisibleTypeAnnotations.add(an);
        }

        return an;
    }

    public AnnotationVisitor visitParameterAnnotation(int nameeter, String desc, boolean visible) {
        AnnotationNode an = new AnnotationNode(desc);
        int names;

        if (visible) {
            if (this.visibleParameterAnnotations == null) {
                names = Type.getArgumentTypes(this.desc).length;
                this.visibleParameterAnnotations = (List[]) (new List[names]);
            }

            if (this.visibleParameterAnnotations[nameeter] == null) {
                this.visibleParameterAnnotations[nameeter] = new ArrayList(1);
            }

            this.visibleParameterAnnotations[nameeter].add(an);
        } else {
            if (this.invisibleParameterAnnotations == null) {
                names = Type.getArgumentTypes(this.desc).length;
                this.invisibleParameterAnnotations = (List[]) (new List[names]);
            }

            if (this.invisibleParameterAnnotations[nameeter] == null) {
                this.invisibleParameterAnnotations[nameeter] = new ArrayList(1);
            }

            this.invisibleParameterAnnotations[nameeter].add(an);
        }

        return an;
    }

    public void visitAttribute(Attribute attr) {
        if (this.attrs == null) {
            this.attrs = new ArrayList(1);
        }

        this.attrs.add(attr);
    }

    public void visitCode() {}

    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        this.instructions.add((AbstractInsnNode) (new FrameNode(type, nLocal, local == null ? null : this.getLabelNodes(local), nStack, stack == null ? null : this.getLabelNodes(stack))));
    }

    public void visitInsn(int opcode) {
        this.instructions.add((AbstractInsnNode) (new InsnNode(opcode)));
    }

    public void visitIntInsn(int opcode, int operand) {
        this.instructions.add((AbstractInsnNode) (new IntInsnNode(opcode, operand)));
    }

    public void visitVarInsn(int opcode, int i) {
        this.instructions.add((AbstractInsnNode) (new VarInsnNode(opcode, i)));
    }

    public void visitTypeInsn(int opcode, String type) {
        this.instructions.add((AbstractInsnNode) (new TypeInsnNode(opcode, type)));
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        this.instructions.add((AbstractInsnNode) (new FieldInsnNode(opcode, owner, name, desc)));
    }

    /** @deprecated */
    @Deprecated
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (this.api >= 327680) {
            super.visitMethodInsn(opcode, owner, name, desc);
        } else {
            this.instructions.add((AbstractInsnNode) (new MethodInsnNode(opcode, owner, name, desc)));
        }
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (this.api < 327680) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        } else {
            this.instructions.add((AbstractInsnNode) (new MethodInsnNode(opcode, owner, name, desc, itf)));
        }
    }

    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        this.instructions.add((AbstractInsnNode) (new InvokeDynamicInsnNode(name, desc, bsm, bsmArgs)));
    }

    public void visitJumpInsn(int opcode, Label label) {
        this.instructions.add((AbstractInsnNode) (new JumpInsnNode(opcode, this.getLabelNode(label))));
    }

    public void visitLabel(Label label) {
        this.instructions.add((AbstractInsnNode) this.getLabelNode(label));
    }

    public void visitLdcInsn(Object cst) {
        this.instructions.add((AbstractInsnNode) (new LdcInsnNode(cst)));
    }

    public void visitIincInsn(int i, int increment) {
        this.instructions.add((AbstractInsnNode) (new IincInsnNode(i, increment)));
    }

    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        this.instructions.add((AbstractInsnNode) (new TableSwitchInsnNode(min, max, this.getLabelNode(dflt), this.getLabelNodes(labels))));
    }

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        this.instructions.add((AbstractInsnNode) (new LookupSwitchInsnNode(this.getLabelNode(dflt), keys, this.getLabelNodes(labels))));
    }

    public void visitMultiANewArrayInsn(String desc, int dims) {
        this.instructions.add((AbstractInsnNode) (new MultiANewArrayInsnNode(desc, dims)));
    }

    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        AbstractInsnNode insn;

        for (insn = this.instructions.getLast(); insn.getOpcode() == -1; insn = insn.getPrevious()) {
            ;
        }

        TypeAnnotationNode an = new TypeAnnotationNode(typeRef, typePath, desc);

        if (visible) {
            if (insn.visibleTypeAnnotations == null) {
                insn.visibleTypeAnnotations = new ArrayList(1);
            }

            insn.visibleTypeAnnotations.add(an);
        } else {
            if (insn.invisibleTypeAnnotations == null) {
                insn.invisibleTypeAnnotations = new ArrayList(1);
            }

            insn.invisibleTypeAnnotations.add(an);
        }

        return an;
    }

    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        this.tryCatchBlocks.add(new TryCatchBlockNode(this.getLabelNode(start), this.getLabelNode(end), this.getLabelNode(handler), type));
    }

    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        TryCatchBlockNode tcb = (TryCatchBlockNode) this.tryCatchBlocks.get((typeRef & 16776960) >> 8);
        TypeAnnotationNode an = new TypeAnnotationNode(typeRef, typePath, desc);

        if (visible) {
            if (tcb.visibleTypeAnnotations == null) {
                tcb.visibleTypeAnnotations = new ArrayList(1);
            }

            tcb.visibleTypeAnnotations.add(an);
        } else {
            if (tcb.invisibleTypeAnnotations == null) {
                tcb.invisibleTypeAnnotations = new ArrayList(1);
            }

            tcb.invisibleTypeAnnotations.add(an);
        }

        return an;
    }

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        this.localVariables.add(new LocalVariableNode(name, desc, signature, this.getLabelNode(start), this.getLabelNode(end), index));
    }

    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
        LocalVariableAnnotationNode an = new LocalVariableAnnotationNode(typeRef, typePath, this.getLabelNodes(start), this.getLabelNodes(end), index, desc);

        if (visible) {
            if (this.visibleLocalVariableAnnotations == null) {
                this.visibleLocalVariableAnnotations = new ArrayList(1);
            }

            this.visibleLocalVariableAnnotations.add(an);
        } else {
            if (this.invisibleLocalVariableAnnotations == null) {
                this.invisibleLocalVariableAnnotations = new ArrayList(1);
            }

            this.invisibleLocalVariableAnnotations.add(an);
        }

        return an;
    }

    public void visitLineNumber(int line, Label start) {
        this.instructions.add((AbstractInsnNode) (new LineNumberNode(line, this.getLabelNode(start))));
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
    }

    public void visitEnd() {}

    protected LabelNode getLabelNode(Label l) {
        if (!(l.info instanceof LabelNode)) {
            l.info = new LabelNode();
        }

        return (LabelNode) l.info;
    }

    private LabelNode[] getLabelNodes(Label[] l) {
        LabelNode[] nodes = new LabelNode[l.length];

        for (int i = 0; i < l.length; ++i) {
            nodes[i] = this.getLabelNode(l[i]);
        }

        return nodes;
    }

    private Object[] getLabelNodes(Object[] objs) {
        Object[] nodes = new Object[objs.length];

        for (int i = 0; i < objs.length; ++i) {
            Object o = objs[i];

            if (o instanceof Label) {
                o = this.getLabelNode((Label) o);
            }

            nodes[i] = o;
        }

        return nodes;
    }

    public void check(int api) {
        if (api == 262144) {
            if (this.visibleTypeAnnotations != null && this.visibleTypeAnnotations.size() > 0) {
                throw new RuntimeException();
            }

            if (this.invisibleTypeAnnotations != null && this.invisibleTypeAnnotations.size() > 0) {
                throw new RuntimeException();
            }

            int n = this.tryCatchBlocks == null ? 0 : this.tryCatchBlocks.size();

            int i;

            for (i = 0; i < n; ++i) {
                TryCatchBlockNode insn = (TryCatchBlockNode) this.tryCatchBlocks.get(i);

                if (insn.visibleTypeAnnotations != null && insn.visibleTypeAnnotations.size() > 0) {
                    throw new RuntimeException();
                }

                if (insn.invisibleTypeAnnotations != null && insn.invisibleTypeAnnotations.size() > 0) {
                    throw new RuntimeException();
                }
            }

            for (i = 0; i < this.instructions.size(); ++i) {
                AbstractInsnNode abstractinsnnode = this.instructions.get(i);

                if (abstractinsnnode.visibleTypeAnnotations != null && abstractinsnnode.visibleTypeAnnotations.size() > 0) {
                    throw new RuntimeException();
                }

                if (abstractinsnnode.invisibleTypeAnnotations != null && abstractinsnnode.invisibleTypeAnnotations.size() > 0) {
                    throw new RuntimeException();
                }

                if (abstractinsnnode instanceof MethodInsnNode) {
                    boolean itf = ((MethodInsnNode) abstractinsnnode).itf;

                    if (itf != (abstractinsnnode.opcode == 185)) {
                        throw new RuntimeException();
                    }
                }
            }

            if (this.visibleLocalVariableAnnotations != null && this.visibleLocalVariableAnnotations.size() > 0) {
                throw new RuntimeException();
            }

            if (this.invisibleLocalVariableAnnotations != null && this.invisibleLocalVariableAnnotations.size() > 0) {
                throw new RuntimeException();
            }
        }

    }

    public void accept(ClassVisitor cv) {
        String[] exceptions = new String[this.exceptions.size()];

        this.exceptions.toArray(exceptions);
        MethodVisitor mv = cv.visitMethod(this.access, this.name, this.desc, this.signature, exceptions);

        if (mv != null) {
            this.accept(mv);
        }

    }

    public void accept(MethodVisitor mv) {
        int n = this.nameeters == null ? 0 : this.nameeters.size();

        int i;

        for (i = 0; i < n; ++i) {
            ParameterNode l = (ParameterNode) this.nameeters.get(i);

            mv.visitParameter(l.name, l.access);
        }

        if (this.annotationDefault != null) {
            AnnotationVisitor annotationvisitor = mv.visitAnnotationDefault();

            AnnotationNode.accept(annotationvisitor, (String) null, this.annotationDefault);
            if (annotationvisitor != null) {
                annotationvisitor.visitEnd();
            }
        }

        n = this.visibleAnnotations == null ? 0 : this.visibleAnnotations.size();

        AnnotationNode annotationnode;

        for (i = 0; i < n; ++i) {
            annotationnode = (AnnotationNode) this.visibleAnnotations.get(i);
            annotationnode.accept(mv.visitAnnotation(annotationnode.desc, true));
        }

        n = this.invisibleAnnotations == null ? 0 : this.invisibleAnnotations.size();

        for (i = 0; i < n; ++i) {
            annotationnode = (AnnotationNode) this.invisibleAnnotations.get(i);
            annotationnode.accept(mv.visitAnnotation(annotationnode.desc, false));
        }

        n = this.visibleTypeAnnotations == null ? 0 : this.visibleTypeAnnotations.size();

        TypeAnnotationNode typeannotationnode;

        for (i = 0; i < n; ++i) {
            typeannotationnode = (TypeAnnotationNode) this.visibleTypeAnnotations.get(i);
            typeannotationnode.accept(mv.visitTypeAnnotation(typeannotationnode.typeRef, typeannotationnode.typePath, typeannotationnode.desc, true));
        }

        n = this.invisibleTypeAnnotations == null ? 0 : this.invisibleTypeAnnotations.size();

        for (i = 0; i < n; ++i) {
            typeannotationnode = (TypeAnnotationNode) this.invisibleTypeAnnotations.get(i);
            typeannotationnode.accept(mv.visitTypeAnnotation(typeannotationnode.typeRef, typeannotationnode.typePath, typeannotationnode.desc, false));
        }

        n = this.visibleParameterAnnotations == null ? 0 : this.visibleParameterAnnotations.length;

        int j;
        AnnotationNode an;
        List list;

        for (i = 0; i < n; ++i) {
            list = this.visibleParameterAnnotations[i];
            if (list != null) {
                for (j = 0; j < list.size(); ++j) {
                    an = (AnnotationNode) list.get(j);
                    an.accept(mv.visitParameterAnnotation(i, an.desc, true));
                }
            }
        }

        n = this.invisibleParameterAnnotations == null ? 0 : this.invisibleParameterAnnotations.length;

        for (i = 0; i < n; ++i) {
            list = this.invisibleParameterAnnotations[i];
            if (list != null) {
                for (j = 0; j < list.size(); ++j) {
                    an = (AnnotationNode) list.get(j);
                    an.accept(mv.visitParameterAnnotation(i, an.desc, false));
                }
            }
        }

        if (this.visited) {
            this.instructions.resetLabels();
        }

        n = this.attrs == null ? 0 : this.attrs.size();

        for (i = 0; i < n; ++i) {
            mv.visitAttribute((Attribute) this.attrs.get(i));
        }

        if (this.instructions.size() > 0) {
            mv.visitCode();
            n = this.tryCatchBlocks == null ? 0 : this.tryCatchBlocks.size();

            for (i = 0; i < n; ++i) {
                ((TryCatchBlockNode) this.tryCatchBlocks.get(i)).updateIndex(i);
                ((TryCatchBlockNode) this.tryCatchBlocks.get(i)).accept(mv);
            }

            this.instructions.accept(mv);
            n = this.localVariables == null ? 0 : this.localVariables.size();

            for (i = 0; i < n; ++i) {
                ((LocalVariableNode) this.localVariables.get(i)).accept(mv);
            }

            n = this.visibleLocalVariableAnnotations == null ? 0 : this.visibleLocalVariableAnnotations.size();

            for (i = 0; i < n; ++i) {
                ((LocalVariableAnnotationNode) this.visibleLocalVariableAnnotations.get(i)).accept(mv, true);
            }

            n = this.invisibleLocalVariableAnnotations == null ? 0 : this.invisibleLocalVariableAnnotations.size();

            for (i = 0; i < n; ++i) {
                ((LocalVariableAnnotationNode) this.invisibleLocalVariableAnnotations.get(i)).accept(mv, false);
            }

            mv.visitMaxs(this.maxStack, this.maxLocals);
            this.visited = true;
        }

        mv.visitEnd();
    }
}
