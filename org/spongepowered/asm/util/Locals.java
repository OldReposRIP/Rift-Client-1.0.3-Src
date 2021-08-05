package org.spongepowered.asm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FrameNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.LineNumberNode;
import org.spongepowered.asm.lib.tree.LocalVariableNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;
import org.spongepowered.asm.lib.tree.analysis.Analyzer;
import org.spongepowered.asm.lib.tree.analysis.AnalyzerException;
import org.spongepowered.asm.lib.tree.analysis.BasicValue;
import org.spongepowered.asm.lib.tree.analysis.Frame;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.util.asm.MixinVerifier;
import org.spongepowered.asm.util.throwables.LVTGeneratorException;

public final class Locals {

    private static final Map calculatedLocalVariables = new HashMap();

    public static void loadLocals(Type[] locals, InsnList insns, int pos, int limit) {
        for (; pos < locals.length && limit > 0; ++pos) {
            if (locals[pos] != null) {
                insns.add((AbstractInsnNode) (new VarInsnNode(locals[pos].getOpcode(21), pos)));
                --limit;
            }
        }

    }

    public static LocalVariableNode[] getLocalsAt(ClassNode classNode, MethodNode method, AbstractInsnNode node) {
        for (int classInfo = 0; classInfo < 3 && (node instanceof LabelNode || node instanceof LineNumberNode); ++classInfo) {
            node = nextNode(method.instructions, node);
        }

        ClassInfo classinfo = ClassInfo.forName(classNode.name);

        if (classinfo == null) {
            throw new LVTGeneratorException("Could not load class metadata for " + classNode.name + " generating LVT for " + method.name);
        } else {
            ClassInfo.Method methodInfo = classinfo.findMethod(method);

            if (methodInfo == null) {
                throw new LVTGeneratorException("Could not locate method metadata for " + method.name + " generating LVT in " + classNode.name);
            } else {
                List frames = methodInfo.getFrames();
                LocalVariableNode[] frame = new LocalVariableNode[method.maxLocals];
                int local = 0;
                int index = 0;

                if ((method.access & 8) == 0) {
                    frame[local++] = new LocalVariableNode("this", classNode.name, (String) null, (LabelNode) null, (LabelNode) null, 0);
                }

                Type[] initialFrameSize = Type.getArgumentTypes(method.desc);
                int frameIndex = initialFrameSize.length;

                int locals;

                for (locals = 0; locals < frameIndex; ++locals) {
                    Type l = initialFrameSize[locals];

                    frame[local] = new LocalVariableNode("arg" + index++, l.toString(), (String) null, (LabelNode) null, (LabelNode) null, local);
                    local += l.getSize();
                }

                int i = local;

                frameIndex = -1;
                locals = 0;
                ListIterator listiterator = method.instructions.iterator();

                while (listiterator.hasNext()) {
                    AbstractInsnNode insn = (AbstractInsnNode) listiterator.next();

                    if (insn instanceof FrameNode) {
                        ++frameIndex;
                        FrameNode framenode = (FrameNode) insn;
                        ClassInfo.FrameData frameData = frameIndex < frames.size() ? (ClassInfo.FrameData) frames.get(frameIndex) : null;

                        locals = frameData != null && frameData.type == 0 ? Math.min(locals, frameData.locals) : framenode.local.size();
                        int localPos = 0;

                        for (int framePos = 0; framePos < frame.length; ++localPos) {
                            Object localType = localPos < framenode.local.size() ? framenode.local.get(localPos) : null;

                            if (localType instanceof String) {
                                frame[framePos] = getLocalVariableAt(classNode, method, node, framePos);
                            } else if (localType instanceof Integer) {
                                boolean isMarkerType = localType == Opcodes.UNINITIALIZED_THIS || localType == Opcodes.NULL;
                                boolean is32bitValue = localType == Opcodes.INTEGER || localType == Opcodes.FLOAT;
                                boolean is64bitValue = localType == Opcodes.DOUBLE || localType == Opcodes.LONG;

                                if (localType != Opcodes.TOP) {
                                    if (isMarkerType) {
                                        frame[framePos] = null;
                                    } else {
                                        if (!is32bitValue && !is64bitValue) {
                                            throw new LVTGeneratorException("Unrecognised locals opcode " + localType + " in locals array at position " + localPos + " in " + classNode.name + "." + method.name + method.desc);
                                        }

                                        frame[framePos] = getLocalVariableAt(classNode, method, node, framePos);
                                        if (is64bitValue) {
                                            ++framePos;
                                            frame[framePos] = null;
                                        }
                                    }
                                }
                            } else {
                                if (localType != null) {
                                    throw new LVTGeneratorException("Invalid value " + localType + " in locals array at position " + localPos + " in " + classNode.name + "." + method.name + method.desc);
                                }

                                if (framePos >= i && framePos >= locals && locals > 0) {
                                    frame[framePos] = null;
                                }
                            }

                            ++framePos;
                        }
                    } else if (insn instanceof VarInsnNode) {
                        VarInsnNode varinsnnode = (VarInsnNode) insn;

                        frame[varinsnnode1.var] = getLocalVariableAt(classNode, method, node, varinsnnode2.var);
                    }

                    if (insn == node) {
                        break;
                    }
                }

                for (int j = 0; j < frame.length; ++j) {
                    if (frame[j] != null && frame[j].desc == null) {
                        frame[j] = null;
                    }
                }

                return frame;
            }
        }
    }

    public static LocalVariableNode getLocalVariableAt(ClassNode classNode, MethodNode method, AbstractInsnNode node, int i) {
        return getLocalVariableAt(classNode, method, method.instructions.indexOf(node), i);
    }

    private static LocalVariableNode getLocalVariableAt(ClassNode classNode, MethodNode method, int pos, int i) {
        LocalVariableNode localVariableNode = null;
        LocalVariableNode fallbackNode = null;
        Iterator iterator = getLocalVariableTable(classNode, method).iterator();

        LocalVariableNode local;

        while (iterator.hasNext()) {
            local = (LocalVariableNode) iterator.next();
            if (local.index == i) {
                if (isOpcodeInRange(method.instructions, local, pos)) {
                    localVariableNode = local;
                } else if (localVariableNode == null) {
                    fallbackNode = local;
                }
            }
        }

        if (localVariableNode == null && !method.localVariables.isEmpty()) {
            iterator = getGeneratedLocalVariableTable(classNode, method).iterator();

            while (iterator.hasNext()) {
                local = (LocalVariableNode) iterator.next();
                if (local.index == i && isOpcodeInRange(method.instructions, local, pos)) {
                    localVariableNode = local;
                }
            }
        }

        return localVariableNode != null ? localVariableNode : fallbackNode;
    }

    private static boolean isOpcodeInRange(InsnList insns, LocalVariableNode local, int pos) {
        return insns.indexOf(local.start) < pos && insns.indexOf(local.end) > pos;
    }

    public static List getLocalVariableTable(ClassNode classNode, MethodNode method) {
        return method.localVariables.isEmpty() ? getGeneratedLocalVariableTable(classNode, method) : method.localVariables;
    }

    public static List getGeneratedLocalVariableTable(ClassNode classNode, MethodNode method) {
        String methodId = String.format("%s.%s%s", new Object[] { classNode.name, method.name, method.desc});
        List localVars = (List) Locals.calculatedLocalVariables.get(methodId);

        if (localVars != null) {
            return localVars;
        } else {
            localVars = generateLocalVariableTable(classNode, method);
            Locals.calculatedLocalVariables.put(methodId, localVars);
            return localVars;
        }
    }

    public static List generateLocalVariableTable(ClassNode classNode, MethodNode method) {
        ArrayList interfaces = null;

        if (classNode.interfaces != null) {
            interfaces = new ArrayList();
            Iterator objectType = classNode.interfaces.iterator();

            while (objectType.hasNext()) {
                String analyzer = (String) objectType.next();

                interfaces.add(Type.getObjectType(analyzer));
            }
        }

        Type type = null;

        if (classNode.superName != null) {
            type = Type.getObjectType(classNode.superName);
        }

        Analyzer analyzer = new Analyzer(new MixinVerifier(Type.getObjectType(classNode.name), type, interfaces, false));

        try {
            analyzer.analyze(classNode.name, method);
        } catch (AnalyzerException analyzerexception) {
            analyzerexception.printStackTrace();
        }

        Frame[] frames = analyzer.getFrames();
        int methodSize = method.instructions.size();
        ArrayList localVariables = new ArrayList();
        LocalVariableNode[] localNodes = new LocalVariableNode[method.maxLocals];
        BasicValue[] locals = new BasicValue[method.maxLocals];
        LabelNode[] labels = new LabelNode[methodSize];
        String[] lastKnownType = new String[method.maxLocals];

        for (int label = 0; label < methodSize; ++label) {
            Frame n = frames[label];

            if (n != null) {
                LabelNode label1 = null;

                for (int j = 0; j < n.getLocals(); ++j) {
                    BasicValue local = (BasicValue) n.getLocal(j);

                    if ((local != null || locals[j] != null) && (local == null || !local.equals(locals[j]))) {
                        if (label1 == null) {
                            AbstractInsnNode desc = method.instructions.get(label);

                            if (desc instanceof LabelNode) {
                                label1 = (LabelNode) desc;
                            } else {
                                labels[label] = label1 = new LabelNode();
                            }
                        }

                        if (local == null && locals[j] != null) {
                            localVariables.add(localNodes[j]);
                            localNodes[j].end = label1;
                            localNodes[j] = null;
                        } else if (local != null) {
                            if (locals[j] != null) {
                                localVariables.add(localNodes[j]);
                                localNodes[j].end = label1;
                                localNodes[j] = null;
                            }

                            String s = local.getType() != null ? local.getType().getDescriptor() : lastKnownType[j];

                            localNodes[j] = new LocalVariableNode("var" + j, s, (String) null, label1, (LabelNode) null, j);
                            if (s != null) {
                                lastKnownType[j] = s;
                            }
                        }

                        locals[j] = local;
                    }
                }
            }
        }

        LabelNode labelnode = null;

        int i;

        for (i = 0; i < localNodes.length; ++i) {
            if (localNodes[i] != null) {
                if (labelnode == null) {
                    labelnode = new LabelNode();
                    method.instructions.add((AbstractInsnNode) labelnode);
                }

                localNodes[i].end = labelnode;
                localVariables.add(localNodes[i]);
            }
        }

        for (i = methodSize - 1; i >= 0; --i) {
            if (labels[i] != null) {
                method.instructions.insert(method.instructions.get(i), (AbstractInsnNode) labels[i]);
            }
        }

        return localVariables;
    }

    private static AbstractInsnNode nextNode(InsnList insns, AbstractInsnNode insn) {
        int index = insns.indexOf(insn) + 1;

        return index > 0 && index < insns.size() ? insns.get(index) : insn;
    }
}
