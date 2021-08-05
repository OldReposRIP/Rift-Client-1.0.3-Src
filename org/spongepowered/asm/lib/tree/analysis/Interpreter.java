package org.spongepowered.asm.lib.tree.analysis;

import java.util.List;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;

public abstract class Interpreter {

    protected final int api;

    protected Interpreter(int api) {
        this.api = api;
    }

    public abstract Value newValue(Type type);

    public abstract Value newOperation(AbstractInsnNode abstractinsnnode) throws AnalyzerException;

    public abstract Value copyOperation(AbstractInsnNode abstractinsnnode, Value value) throws AnalyzerException;

    public abstract Value unaryOperation(AbstractInsnNode abstractinsnnode, Value value) throws AnalyzerException;

    public abstract Value binaryOperation(AbstractInsnNode abstractinsnnode, Value value, Value value1) throws AnalyzerException;

    public abstract Value ternaryOperation(AbstractInsnNode abstractinsnnode, Value value, Value value1, Value value2) throws AnalyzerException;

    public abstract Value naryOperation(AbstractInsnNode abstractinsnnode, List list) throws AnalyzerException;

    public abstract void returnOperation(AbstractInsnNode abstractinsnnode, Value value, Value value1) throws AnalyzerException;

    public abstract Value merge(Value value, Value value1);
}
