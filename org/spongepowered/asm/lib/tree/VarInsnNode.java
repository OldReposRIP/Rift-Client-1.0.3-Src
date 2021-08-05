package org.spongepowered.asm.lib.tree;

import java.util.Map;
import org.spongepowered.asm.lib.MethodVisitor;

public class VarInsnNode extends AbstractInsnNode {

    public int var;

    public VarInsnNode(int opcode, int i) {
        super(opcode);
        this.var = i;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public int getType() {
        return 2;
    }

    public void accept(MethodVisitor mv) {
        mv.visitVarInsn(this.opcode, this.var);
        this.acceptAnnotations(mv);
    }

    public AbstractInsnNode clone(Map labels) {
        return (new VarInsnNode(this.opcode, this.var)).cloneAnnotations(this);
    }
}
