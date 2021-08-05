package org.spongepowered.asm.lib.tree.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.IincInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.LookupSwitchInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.tree.TableSwitchInsnNode;
import org.spongepowered.asm.lib.tree.TryCatchBlockNode;
import org.spongepowered.asm.lib.tree.VarInsnNode;

public class Analyzer implements Opcodes {

    private final Interpreter interpreter;
    private int n;
    private InsnList insns;
    private List[] handlers;
    private Frame[] frames;
    private Subroutine[] subroutines;
    private boolean[] queued;
    private int[] queue;
    private int top;

    public Analyzer(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public Frame[] analyze(String owner, MethodNode m) throws AnalyzerException {
        if ((m.access & 1280) != 0) {
            this.frames = (Frame[]) (new Frame[0]);
            return this.frames;
        } else {
            this.n = m.instructions.size();
            this.insns = m.instructions;
            this.handlers = (List[]) (new List[this.n]);
            this.frames = (Frame[]) (new Frame[this.n]);
            this.subroutines = new Subroutine[this.n];
            this.queued = new boolean[this.n];
            this.queue = new int[this.n];
            this.top = 0;

            int current;

            for (int main = 0; main < m.tryCatchBlocks.size(); ++main) {
                TryCatchBlockNode subroutineCalls = (TryCatchBlockNode) m.tryCatchBlocks.get(main);
                int subroutineHeads = this.insns.indexOf(subroutineCalls.start);

                current = this.insns.indexOf(subroutineCalls.end);

                for (int handler = subroutineHeads; handler < current; ++handler) {
                    Object args = this.handlers[handler];

                    if (args == null) {
                        args = new ArrayList();
                        this.handlers[handler] = (List) args;
                    }

                    ((List) args).add(subroutineCalls);
                }
            }

            Subroutine subroutine = new Subroutine((LabelNode) null, m.maxLocals, (JumpInsnNode) null);
            ArrayList arraylist = new ArrayList();
            HashMap hashmap = new HashMap();

            this.findSubroutine(0, subroutine, arraylist);

            while (!arraylist.isEmpty()) {
                JumpInsnNode jumpinsnnode = (JumpInsnNode) arraylist.remove(0);
                Subroutine subroutine1 = (Subroutine) hashmap.get(jumpinsnnode.label);

                if (subroutine1 == null) {
                    subroutine1 = new Subroutine(jumpinsnnode.label, m.maxLocals, jumpinsnnode);
                    hashmap.put(jumpinsnnode.label, subroutine1);
                    this.findSubroutine(this.insns.indexOf(jumpinsnnode.label), subroutine1, arraylist);
                } else {
                    subroutine1.callers.add(jumpinsnnode);
                }
            }

            for (current = 0; current < this.n; ++current) {
                if (this.subroutines[current] != null && this.subroutines[current].start == null) {
                    this.subroutines[current] = null;
                }
            }

            Frame frame = this.newFrame(m.maxLocals, m.maxStack);
            Frame frame1 = this.newFrame(m.maxLocals, m.maxStack);

            frame.setReturn(this.interpreter.newValue(Type.getReturnType(m.desc)));
            Type[] atype = Type.getArgumentTypes(m.desc);
            int local = 0;

            if ((m.access & 8) == 0) {
                Type insn = Type.getObjectType(owner);

                frame.setLocal(local++, this.interpreter.newValue(insn));
            }

            int i;

            for (i = 0; i < atype.length; ++i) {
                frame.setLocal(local++, this.interpreter.newValue(atype[i]));
                if (atype[i].getSize() == 2) {
                    frame.setLocal(local++, this.interpreter.newValue((Type) null));
                }
            }

            while (local < m.maxLocals) {
                frame.setLocal(local++, this.interpreter.newValue((Type) null));
            }

            this.merge(0, frame, (Subroutine) null);
            this.init(owner, m);

            while (this.top > 0) {
                i = this.queue[--this.top];
                Frame f = this.frames[i];
                Subroutine subroutine = this.subroutines[i];

                this.queued[i] = false;
                AbstractInsnNode insnNode = null;

                try {
                    insnNode = m.instructions.get(i);
                    int e = insnNode.getOpcode();
                    int insnType = insnNode.getType();
                    int i;

                    if (insnType != 8 && insnType != 15 && insnType != 14) {
                        frame.init(f).execute(insnNode, this.interpreter);
                        subroutine = subroutine == null ? null : subroutine.copy();
                        if (insnNode instanceof JumpInsnNode) {
                            JumpInsnNode insnHandlers = (JumpInsnNode) insnNode;

                            if (e != 167 && e != 168) {
                                this.merge(i + 1, frame, subroutine);
                                this.newControlFlowEdge(i, i + 1);
                            }

                            i = this.insns.indexOf(insnHandlers.label);
                            if (e == 168) {
                                this.merge(i, frame, new Subroutine(insnHandlers.label, m.maxLocals, insnHandlers));
                            } else {
                                this.merge(i, frame, subroutine);
                            }

                            this.newControlFlowEdge(i, i);
                        } else {
                            int tcb;
                            LabelNode type;

                            if (insnNode instanceof LookupSwitchInsnNode) {
                                LookupSwitchInsnNode lookupswitchinsnnode = (LookupSwitchInsnNode) insnNode;

                                i = this.insns.indexOf(lookupswitchinsnnode.dflt);
                                this.merge(i, frame, subroutine);
                                this.newControlFlowEdge(i, i);

                                for (tcb = 0; tcb < lookupswitchinsnnode.labels.size(); ++tcb) {
                                    type = (LabelNode) lookupswitchinsnnode.labels.get(tcb);
                                    i = this.insns.indexOf(type);
                                    this.merge(i, frame, subroutine);
                                    this.newControlFlowEdge(i, i);
                                }
                            } else if (insnNode instanceof TableSwitchInsnNode) {
                                TableSwitchInsnNode tableswitchinsnnode = (TableSwitchInsnNode) insnNode;

                                i = this.insns.indexOf(tableswitchinsnnode.dflt);
                                this.merge(i, frame, subroutine);
                                this.newControlFlowEdge(i, i);

                                for (tcb = 0; tcb < tableswitchinsnnode.labels.size(); ++tcb) {
                                    type = (LabelNode) tableswitchinsnnode.labels.get(tcb);
                                    i = this.insns.indexOf(type);
                                    this.merge(i, frame, subroutine);
                                    this.newControlFlowEdge(i, i);
                                }
                            } else {
                                int j;

                                if (e == 169) {
                                    if (subroutine == null) {
                                        throw new AnalyzerException(insnNode, "RET instruction outside of a sub routine");
                                    }

                                    for (j = 0; j < subroutine.callers.size(); ++j) {
                                        JumpInsnNode jumpinsnnode1 = (JumpInsnNode) subroutine.callers.get(j);

                                        tcb = this.insns.indexOf(jumpinsnnode1);
                                        if (this.frames[tcb] != null) {
                                            this.merge(tcb + 1, this.frames[tcb], frame, this.subroutines[tcb], subroutine.access);
                                            this.newControlFlowEdge(i, tcb + 1);
                                        }
                                    }
                                } else if (e != 191 && (e < 172 || e > 177)) {
                                    if (subroutine != null) {
                                        if (insnNode instanceof VarInsnNode) {
                                            j = ((VarInsnNode) insnNode).var;
                                            subroutine.access[j] = true;
                                            if (e == 22 || e == 24 || e == 55 || e == 57) {
                                                subroutine.access[j + 1] = true;
                                            }
                                        } else if (insnNode instanceof IincInsnNode) {
                                            j = ((IincInsnNode) insnNode).var;
                                            subroutine.access[j] = true;
                                        }
                                    }

                                    this.merge(i + 1, frame, subroutine);
                                    this.newControlFlowEdge(i, i + 1);
                                }
                            }
                        }
                    } else {
                        this.merge(i + 1, f, subroutine);
                        this.newControlFlowEdge(i, i + 1);
                    }

                    List list = this.handlers[i];

                    if (list != null) {
                        for (i = 0; i < list.size(); ++i) {
                            TryCatchBlockNode trycatchblocknode = (TryCatchBlockNode) list.get(i);
                            Type type;

                            if (trycatchblocknode.type == null) {
                                type = Type.getObjectType("java/lang/Throwable");
                            } else {
                                type = Type.getObjectType(trycatchblocknode.type);
                            }

                            int jump = this.insns.indexOf(trycatchblocknode.handler);

                            if (this.newControlFlowExceptionEdge(i, trycatchblocknode)) {
                                frame1.init(f);
                                frame1.clearStack();
                                frame1.push(this.interpreter.newValue(type));
                                this.merge(jump, frame1, subroutine);
                            }
                        }
                    }
                } catch (AnalyzerException analyzerexception) {
                    throw new AnalyzerException(analyzerexception.node, "Error at instruction " + i + ": " + analyzerexception.getMessage(), analyzerexception);
                } catch (Exception exception) {
                    throw new AnalyzerException(insnNode, "Error at instruction " + i + ": " + exception.getMessage(), exception);
                }
            }

            return this.frames;
        }
    }

    private void findSubroutine(int insn, Subroutine sub, List calls) throws AnalyzerException {
        while (insn >= 0 && insn < this.n) {
            if (this.subroutines[insn] != null) {
                return;
            }

            this.subroutines[insn] = sub.copy();
            AbstractInsnNode node = this.insns.get(insn);
            int i;

            if (node instanceof JumpInsnNode) {
                if (node.getOpcode() == 168) {
                    calls.add(node);
                } else {
                    JumpInsnNode insnHandlers = (JumpInsnNode) node;

                    this.findSubroutine(this.insns.indexOf(insnHandlers.label), sub, calls);
                }
            } else {
                LabelNode tcb;

                if (node instanceof TableSwitchInsnNode) {
                    TableSwitchInsnNode tableswitchinsnnode = (TableSwitchInsnNode) node;

                    this.findSubroutine(this.insns.indexOf(tableswitchinsnnode.dflt), sub, calls);

                    for (i = tableswitchinsnnode.labels.size() - 1; i >= 0; --i) {
                        tcb = (LabelNode) tableswitchinsnnode.labels.get(i);
                        this.findSubroutine(this.insns.indexOf(tcb), sub, calls);
                    }
                } else if (node instanceof LookupSwitchInsnNode) {
                    LookupSwitchInsnNode lookupswitchinsnnode = (LookupSwitchInsnNode) node;

                    this.findSubroutine(this.insns.indexOf(lookupswitchinsnnode.dflt), sub, calls);

                    for (i = lookupswitchinsnnode.labels.size() - 1; i >= 0; --i) {
                        tcb = (LabelNode) lookupswitchinsnnode.labels.get(i);
                        this.findSubroutine(this.insns.indexOf(tcb), sub, calls);
                    }
                }
            }

            List list = this.handlers[insn];

            if (list != null) {
                for (i = 0; i < list.size(); ++i) {
                    TryCatchBlockNode trycatchblocknode = (TryCatchBlockNode) list.get(i);

                    this.findSubroutine(this.insns.indexOf(trycatchblocknode.handler), sub, calls);
                }
            }

            switch (node.getOpcode()) {
            case 167:
            case 169:
            case 170:
            case 171:
            case 172:
            case 173:
            case 174:
            case 175:
            case 176:
            case 177:
            case 191:
                return;

            case 168:
            case 178:
            case 179:
            case 180:
            case 181:
            case 182:
            case 183:
            case 184:
            case 185:
            case 186:
            case 187:
            case 188:
            case 189:
            case 190:
            default:
                ++insn;
            }
        }

        throw new AnalyzerException((AbstractInsnNode) null, "Execution can fall off end of the code");
    }

    public Frame[] getFrames() {
        return this.frames;
    }

    public List getHandlers(int insn) {
        return this.handlers[insn];
    }

    protected void init(String owner, MethodNode m) throws AnalyzerException {}

    protected Frame newFrame(int nLocals, int nStack) {
        return new Frame(nLocals, nStack);
    }

    protected Frame newFrame(Frame src) {
        return new Frame(src);
    }

    protected void newControlFlowEdge(int insn, int successor) {}

    protected boolean newControlFlowExceptionEdge(int insn, int successor) {
        return true;
    }

    protected boolean newControlFlowExceptionEdge(int insn, TryCatchBlockNode tcb) {
        return this.newControlFlowExceptionEdge(insn, this.insns.indexOf(tcb.handler));
    }

    private void merge(int insn, Frame frame, Subroutine subroutine) throws AnalyzerException {
        Frame oldFrame = this.frames[insn];
        Subroutine oldSubroutine = this.subroutines[insn];
        boolean changes;

        if (oldFrame == null) {
            this.frames[insn] = this.newFrame(frame);
            changes = true;
        } else {
            changes = oldFrame.merge(frame, this.interpreter);
        }

        if (oldSubroutine == null) {
            if (subroutine != null) {
                this.subroutines[insn] = subroutine.copy();
                changes = true;
            }
        } else if (subroutine != null) {
            changes |= oldSubroutine.merge(subroutine);
        }

        if (changes && !this.queued[insn]) {
            this.queued[insn] = true;
            this.queue[this.top++] = insn;
        }

    }

    private void merge(int insn, Frame beforeJSR, Frame afterRET, Subroutine subroutineBeforeJSR, boolean[] access) throws AnalyzerException {
        Frame oldFrame = this.frames[insn];
        Subroutine oldSubroutine = this.subroutines[insn];

        afterRET.merge(beforeJSR, access);
        boolean changes;

        if (oldFrame == null) {
            this.frames[insn] = this.newFrame(afterRET);
            changes = true;
        } else {
            changes = oldFrame.merge(afterRET, this.interpreter);
        }

        if (oldSubroutine != null && subroutineBeforeJSR != null) {
            changes |= oldSubroutine.merge(subroutineBeforeJSR);
        }

        if (changes && !this.queued[insn]) {
            this.queued[insn] = true;
            this.queue[this.top++] = insn;
        }

    }
}
