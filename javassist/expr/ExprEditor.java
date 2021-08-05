package javassist.expr;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.MethodInfo;

public class ExprEditor {

    public boolean doit(CtClass clazz, MethodInfo minfo) throws CannotCompileException {
        CodeAttribute codeAttr = minfo.getCodeAttribute();

        if (codeAttr == null) {
            return false;
        } else {
            CodeIterator iterator = codeAttr.iterator();
            boolean edited = false;
            ExprEditor.LoopContext context = new ExprEditor.LoopContext(codeAttr.getMaxLocals());

            while (iterator.hasNext()) {
                if (this.loopBody(iterator, clazz, minfo, context)) {
                    edited = true;
                }
            }

            ExceptionTable et = codeAttr.getExceptionTable();
            int n = et.size();

            for (int b = 0; b < n; ++b) {
                Handler h = new Handler(et, b, iterator, clazz, minfo);

                this.edit(h);
                if (h.edited()) {
                    edited = true;
                    context.updateMax(h.locals(), h.stack());
                }
            }

            if (codeAttr.getMaxLocals() < context.maxLocals) {
                codeAttr.setMaxLocals(context.maxLocals);
            }

            codeAttr.setMaxStack(codeAttr.getMaxStack() + context.maxStack);

            try {
                if (edited) {
                    minfo.rebuildStackMapIf6(clazz.getClassPool(), clazz.getClassFile2());
                }

                return edited;
            } catch (BadBytecode badbytecode) {
                throw new CannotCompileException(badbytecode.getMessage(), badbytecode);
            }
        }
    }

    boolean doit(CtClass clazz, MethodInfo minfo, ExprEditor.LoopContext context, CodeIterator iterator, int endPos) throws CannotCompileException {
        boolean edited = false;

        while (iterator.hasNext() && iterator.lookAhead() < endPos) {
            int size = iterator.getCodeLength();

            if (this.loopBody(iterator, clazz, minfo, context)) {
                edited = true;
                int size2 = iterator.getCodeLength();

                if (size != size2) {
                    endPos += size2 - size;
                }
            }
        }

        return edited;
    }

    final boolean loopBody(CodeIterator iterator, CtClass clazz, MethodInfo minfo, ExprEditor.LoopContext context) throws CannotCompileException {
        try {
            Object e = null;
            int pos = iterator.next();
            int c = iterator.byteAt(pos);

            if (c >= 178) {
                if (c < 188) {
                    if (c != 184 && c != 185 && c != 182) {
                        if (c != 180 && c != 178 && c != 181 && c != 179) {
                            if (c == 187) {
                                int newList = iterator.u16bitAt(pos + 1);

                                context.newList = new ExprEditor.NewOp(context.newList, pos, minfo.getConstPool().getClassInfo(newList));
                            } else if (c == 183) {
                                ExprEditor.NewOp newList1 = context.newList;

                                if (newList1 != null && minfo.getConstPool().isConstructor(newList1.type, iterator.u16bitAt(pos + 1)) > 0) {
                                    e = new NewExpr(pos, iterator, clazz, minfo, newList1.type, newList1.pos);
                                    this.edit((NewExpr) e);
                                    context.newList = newList1.next;
                                } else {
                                    MethodCall mcall = new MethodCall(pos, iterator, clazz, minfo);

                                    if (mcall.getMethodName().equals("<init>")) {
                                        ConstructorCall ccall = new ConstructorCall(pos, iterator, clazz, minfo);

                                        e = ccall;
                                        this.edit(ccall);
                                    } else {
                                        e = mcall;
                                        this.edit(mcall);
                                    }
                                }
                            }
                        } else {
                            e = new FieldAccess(pos, iterator, clazz, minfo, c);
                            this.edit((FieldAccess) e);
                        }
                    } else {
                        e = new MethodCall(pos, iterator, clazz, minfo);
                        this.edit((MethodCall) e);
                    }
                } else if (c != 188 && c != 189 && c != 197) {
                    if (c == 193) {
                        e = new Instanceof(pos, iterator, clazz, minfo);
                        this.edit((Instanceof) e);
                    } else if (c == 192) {
                        e = new Cast(pos, iterator, clazz, minfo);
                        this.edit((Cast) e);
                    }
                } else {
                    e = new NewArray(pos, iterator, clazz, minfo, c);
                    this.edit((NewArray) e);
                }
            }

            if (e != null && ((Expr) e).edited()) {
                context.updateMax(((Expr) e).locals(), ((Expr) e).stack());
                return true;
            } else {
                return false;
            }
        } catch (BadBytecode badbytecode) {
            throw new CannotCompileException(badbytecode);
        }
    }

    public void edit(NewExpr e) throws CannotCompileException {}

    public void edit(NewArray a) throws CannotCompileException {}

    public void edit(MethodCall m) throws CannotCompileException {}

    public void edit(ConstructorCall c) throws CannotCompileException {}

    public void edit(FieldAccess f) throws CannotCompileException {}

    public void edit(Instanceof i) throws CannotCompileException {}

    public void edit(Cast c) throws CannotCompileException {}

    public void edit(Handler h) throws CannotCompileException {}

    static final class LoopContext {

        ExprEditor.NewOp newList;
        int maxLocals;
        int maxStack;

        LoopContext(int locals) {
            this.maxLocals = locals;
            this.maxStack = 0;
            this.newList = null;
        }

        void updateMax(int locals, int stack) {
            if (this.maxLocals < locals) {
                this.maxLocals = locals;
            }

            if (this.maxStack < stack) {
                this.maxStack = stack;
            }

        }
    }

    static final class NewOp {

        ExprEditor.NewOp next;
        int pos;
        String type;

        NewOp(ExprEditor.NewOp n, int p, String t) {
            this.next = n;
            this.pos = p;
            this.type = t;
        }
    }
}
