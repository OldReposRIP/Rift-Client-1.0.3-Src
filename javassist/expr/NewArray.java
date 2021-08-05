package javassist.expr;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.MethodInfo;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;
import javassist.compiler.JvstCodeGen;
import javassist.compiler.JvstTypeChecker;
import javassist.compiler.ProceedHandler;
import javassist.compiler.ast.ASTList;

public class NewArray extends Expr {

    int opcode;

    protected NewArray(int pos, CodeIterator i, CtClass declaring, MethodInfo m, int op) {
        super(pos, i, declaring, m);
        this.opcode = op;
    }

    public CtBehavior where() {
        return super.where();
    }

    public int getLineNumber() {
        return super.getLineNumber();
    }

    public String getFileName() {
        return super.getFileName();
    }

    public CtClass[] mayThrow() {
        return super.mayThrow();
    }

    public CtClass getComponentType() throws NotFoundException {
        int index;

        if (this.opcode == 188) {
            index = this.iterator.byteAt(this.currentPos + 1);
            return this.getPrimitiveType(index);
        } else if (this.opcode != 189 && this.opcode != 197) {
            throw new RuntimeException("bad opcode: " + this.opcode);
        } else {
            index = this.iterator.u16bitAt(this.currentPos + 1);
            String desc = this.getConstPool().getClassInfo(index);
            int dim = Descriptor.arrayDimension(desc);

            desc = Descriptor.toArrayComponent(desc, dim);
            return Descriptor.toCtClass(desc, this.thisClass.getClassPool());
        }
    }

    CtClass getPrimitiveType(int atype) {
        switch (atype) {
        case 4:
            return CtClass.booleanType;

        case 5:
            return CtClass.charType;

        case 6:
            return CtClass.floatType;

        case 7:
            return CtClass.doubleType;

        case 8:
            return CtClass.byteType;

        case 9:
            return CtClass.shortType;

        case 10:
            return CtClass.intType;

        case 11:
            return CtClass.longType;

        default:
            throw new RuntimeException("bad atype: " + atype);
        }
    }

    public int getDimension() {
        if (this.opcode == 188) {
            return 1;
        } else if (this.opcode != 189 && this.opcode != 197) {
            throw new RuntimeException("bad opcode: " + this.opcode);
        } else {
            int index = this.iterator.u16bitAt(this.currentPos + 1);
            String desc = this.getConstPool().getClassInfo(index);

            return Descriptor.arrayDimension(desc) + (this.opcode == 189 ? 1 : 0);
        }
    }

    public int getCreatedDimensions() {
        return this.opcode == 197 ? this.iterator.byteAt(this.currentPos + 3) : 1;
    }

    public void replace(String statement) throws CannotCompileException {
        try {
            this.replace2(statement);
        } catch (CompileError compileerror) {
            throw new CannotCompileException(compileerror);
        } catch (NotFoundException notfoundexception) {
            throw new CannotCompileException(notfoundexception);
        } catch (BadBytecode badbytecode) {
            throw new CannotCompileException("broken method");
        }
    }

    private void replace2(String statement) throws CompileError, NotFoundException, BadBytecode, CannotCompileException {
        this.thisClass.getClassFile();
        ConstPool constPool = this.getConstPool();
        int pos = this.currentPos;
        boolean index = false;
        int dim = 1;
        byte codeLength;
        String desc;
        int i;

        if (this.opcode == 188) {
            i = this.iterator.byteAt(this.currentPos + 1);
            CtPrimitiveType jc = (CtPrimitiveType) this.getPrimitiveType(i);

            desc = "[" + jc.getDescriptor();
            codeLength = 2;
        } else if (this.opcode == 189) {
            i = this.iterator.u16bitAt(pos + 1);
            desc = constPool.getClassInfo(i);
            if (desc.startsWith("[")) {
                desc = "[" + desc;
            } else {
                desc = "[L" + desc + ";";
            }

            codeLength = 3;
        } else {
            if (this.opcode != 197) {
                throw new RuntimeException("bad opcode: " + this.opcode);
            }

            i = this.iterator.u16bitAt(this.currentPos + 1);
            desc = constPool.getClassInfo(i);
            dim = this.iterator.byteAt(this.currentPos + 3);
            codeLength = 4;
        }

        CtClass retType = Descriptor.toCtClass(desc, this.thisClass.getClassPool());
        Javac javac = new Javac(this.thisClass);
        CodeAttribute ca = this.iterator.get();
        CtClass[] names = new CtClass[dim];

        int nameVar;

        for (nameVar = 0; nameVar < dim; ++nameVar) {
            names[nameVar] = CtClass.intType;
        }

        nameVar = ca.getMaxLocals();
        javac.recordParams("java.lang.Object", names, true, nameVar, this.withinStatic());
        checkResultValue(retType, statement);
        int retVar = javac.recordReturnType(retType, true);

        javac.recordProceed(new NewArray.ProceedForArray(retType, this.opcode, i, dim));
        Bytecode bytecode = javac.getBytecode();

        storeStack(names, true, nameVar, bytecode);
        javac.recordLocalVariables(ca, pos);
        bytecode.addOpcode(1);
        bytecode.addAstore(retVar);
        javac.compileStmnt(statement);
        bytecode.addAload(retVar);
        this.replace0(pos, bytecode, codeLength);
    }

    static class ProceedForArray implements ProceedHandler {

        CtClass arrayType;
        int opcode;
        int index;
        int dimension;

        ProceedForArray(CtClass type, int op, int i, int dim) {
            this.arrayType = type;
            this.opcode = op;
            this.index = i;
            this.dimension = dim;
        }

        public void doit(JvstCodeGen gen, Bytecode bytecode, ASTList args) throws CompileError {
            int num = gen.getMethodArgsLength(args);

            if (num != this.dimension) {
                throw new CompileError("$proceed() with a wrong number of nameeters");
            } else {
                gen.atMethodArgs(args, new int[num], new int[num], new String[num]);
                bytecode.addOpcode(this.opcode);
                if (this.opcode == 189) {
                    bytecode.addIndex(this.index);
                } else if (this.opcode == 188) {
                    bytecode.add(this.index);
                } else {
                    bytecode.addIndex(this.index);
                    bytecode.add(this.dimension);
                    bytecode.growStack(1 - this.dimension);
                }

                gen.setType(this.arrayType);
            }
        }

        public void setReturnType(JvstTypeChecker c, ASTList args) throws CompileError {
            c.setType(this.arrayType);
        }
    }
}
