package javassist.compiler;

import java.util.ArrayList;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.compiler.ast.ASTList;
import javassist.compiler.ast.ASTree;
import javassist.compiler.ast.ArrayInit;
import javassist.compiler.ast.CallExpr;
import javassist.compiler.ast.Declarator;
import javassist.compiler.ast.Expr;
import javassist.compiler.ast.Keyword;
import javassist.compiler.ast.Member;
import javassist.compiler.ast.MethodDecl;
import javassist.compiler.ast.NewExpr;
import javassist.compiler.ast.Pair;
import javassist.compiler.ast.Stmnt;
import javassist.compiler.ast.Symbol;

public class MemberCodeGen extends CodeGen {

    protected MemberResolver resolver;
    protected CtClass thisClass;
    protected MethodInfo thisMethod;
    protected boolean resultStatic;

    public MemberCodeGen(Bytecode b, CtClass cc, ClassPool cp) {
        super(b);
        this.resolver = new MemberResolver(cp);
        this.thisClass = cc;
        this.thisMethod = null;
    }

    public int getMajorVersion() {
        ClassFile cf = this.thisClass.getClassFile2();

        return cf == null ? ClassFile.MAJOR_VERSION : cf.getMajorVersion();
    }

    public void setThisMethod(CtMethod m) {
        this.thisMethod = m.getMethodInfo2();
        if (this.typeChecker != null) {
            this.typeChecker.setThisMethod(this.thisMethod);
        }

    }

    public CtClass getThisClass() {
        return this.thisClass;
    }

    protected String getThisName() {
        return MemberResolver.javaToJvmName(this.thisClass.getName());
    }

    protected String getSuperName() throws CompileError {
        return MemberResolver.javaToJvmName(MemberResolver.getSuperclass(this.thisClass).getName());
    }

    protected void insertDefaultSuperCall() throws CompileError {
        this.bytecode.addAload(0);
        this.bytecode.addInvokespecial(MemberResolver.getSuperclass(this.thisClass), "<init>", "()V");
    }

    protected void atTryStmnt(Stmnt st) throws CompileError {
        Bytecode bc = this.bytecode;
        Stmnt body = (Stmnt) st.getLeft();

        if (body != null) {
            ASTList catchList = (ASTList) st.getRight().getLeft();
            Stmnt finallyBlock = (Stmnt) st.getRight().getRight().getLeft();
            ArrayList gotoList = new ArrayList();
            MemberCodeGen.JsrHook jsrHook = null;

            if (finallyBlock != null) {
                jsrHook = new MemberCodeGen.JsrHook(this);
            }

            int start = bc.currentPc();

            body.accept(this);
            int end = bc.currentPc();

            if (start == end) {
                throw new CompileError("empty try block");
            } else {
                boolean tryNotReturn = !this.hasReturned;

                if (tryNotReturn) {
                    bc.addOpcode(167);
                    gotoList.add(new Integer(bc.currentPc()));
                    bc.addIndex(0);
                }

                int i = this.getMaxLocals();

                this.incMaxLocals(1);

                while (catchList != null) {
                    Pair pcEnd = (Pair) catchList.head();

                    catchList = catchList.tail();
                    Declarator decl = (Declarator) pcEnd.getLeft();
                    Stmnt block = (Stmnt) pcEnd.getRight();

                    decl.setLocalVar(i);
                    CtClass type = this.resolver.lookupClassByJvmName(decl.getClassName());

                    decl.setClassName(MemberResolver.javaToJvmName(type.getName()));
                    bc.addExceptionHandler(start, end, bc.currentPc(), type);
                    bc.growStack(1);
                    bc.addAstore(i);
                    this.hasReturned = false;
                    if (block != null) {
                        block.accept(this);
                    }

                    if (!this.hasReturned) {
                        bc.addOpcode(167);
                        gotoList.add(new Integer(bc.currentPc()));
                        bc.addIndex(0);
                        tryNotReturn = true;
                    }
                }

                int pcEnd1;

                if (finallyBlock != null) {
                    jsrHook.remove(this);
                    pcEnd1 = bc.currentPc();
                    bc.addExceptionHandler(start, pcEnd1, pcEnd1, 0);
                    bc.growStack(1);
                    bc.addAstore(i);
                    this.hasReturned = false;
                    finallyBlock.accept(this);
                    if (!this.hasReturned) {
                        bc.addAload(i);
                        bc.addOpcode(191);
                    }

                    this.addFinally(jsrHook.jsrList, finallyBlock);
                }

                pcEnd1 = bc.currentPc();
                this.patchGoto(gotoList, pcEnd1);
                this.hasReturned = !tryNotReturn;
                if (finallyBlock != null && tryNotReturn) {
                    finallyBlock.accept(this);
                }

            }
        }
    }

    private void addFinally(ArrayList returnList, Stmnt finallyBlock) throws CompileError {
        Bytecode bc = this.bytecode;
        int n = returnList.size();

        for (int i = 0; i < n; ++i) {
            int[] ret = (int[]) ((int[]) returnList.get(i));
            int pc = ret[0];

            bc.write16bit(pc, bc.currentPc() - pc + 1);
            MemberCodeGen.JsrHook2 hook = new MemberCodeGen.JsrHook2(this, ret);

            finallyBlock.accept(this);
            hook.remove(this);
            if (!this.hasReturned) {
                bc.addOpcode(167);
                bc.addIndex(pc + 3 - bc.currentPc());
            }
        }

    }

    public void atNewExpr(NewExpr expr) throws CompileError {
        if (expr.isArray()) {
            this.atNewArrayExpr(expr);
        } else {
            CtClass clazz = this.resolver.lookupClassByName(expr.getClassName());
            String cname = clazz.getName();
            ASTList args = expr.getArguments();

            this.bytecode.addNew(cname);
            this.bytecode.addOpcode(89);
            this.atMethodCallCore(clazz, "<init>", args, false, true, -1, (MemberResolver.Method) null);
            this.exprType = 307;
            this.arrayDim = 0;
            this.className = MemberResolver.javaToJvmName(cname);
        }

    }

    public void atNewArrayExpr(NewExpr expr) throws CompileError {
        int type = expr.getArrayType();
        ASTList size = expr.getArraySize();
        ASTList classname = expr.getClassName();
        ArrayInit init = expr.getInitializer();

        if (size.length() > 1) {
            if (init != null) {
                throw new CompileError("sorry, multi-dimensional array initializer for new is not supported");
            } else {
                this.atMultiNewArray(type, classname, size);
            }
        } else {
            ASTree sizeExpr = size.head();

            this.atNewArrayExpr2(type, sizeExpr, Declarator.astToClassName(classname, '/'), init);
        }
    }

    private void atNewArrayExpr2(int type, ASTree sizeExpr, String jvmClassname, ArrayInit init) throws CompileError {
        if (init == null) {
            if (sizeExpr == null) {
                throw new CompileError("no array size");
            }

            sizeExpr.accept(this);
        } else {
            if (sizeExpr != null) {
                throw new CompileError("unnecessary array size specified for new");
            }

            int elementClass = init.length();

            this.bytecode.addIconst(elementClass);
        }

        String s;

        if (type == 307) {
            s = this.resolveClassName(jvmClassname);
            this.bytecode.addAnewarray(MemberResolver.jvmToJavaName(s));
        } else {
            s = null;
            byte s = 0;

            switch (type) {
            case 301:
                s = 4;
                break;

            case 303:
                s = 8;
                break;

            case 306:
                s = 5;
                break;

            case 312:
                s = 7;
                break;

            case 317:
                s = 6;
                break;

            case 324:
                s = 10;
                break;

            case 326:
                s = 11;
                break;

            case 334:
                s = 9;
                break;

            default:
                badNewExpr();
            }

            this.bytecode.addOpcode(188);
            this.bytecode.add(s);
        }

        if (init != null) {
            int i = init.length();
            Object list = init;

            for (int i = 0; i < i; ++i) {
                this.bytecode.addOpcode(89);
                this.bytecode.addIconst(i);
                ((ASTList) list).head().accept(this);
                if (!isRefType(type)) {
                    this.atNumCastExpr(this.exprType, type);
                }

                this.bytecode.addOpcode(getArrayWriteOp(type, 0));
                list = ((ASTList) list).tail();
            }
        }

        this.exprType = type;
        this.arrayDim = 1;
        this.className = s;
    }

    private static void badNewExpr() throws CompileError {
        throw new CompileError("bad new expression");
    }

    protected void atArrayVariableAssign(ArrayInit init, int i, int j, String s) throws CompileError {
        this.atNewArrayExpr2(i, (ASTree) null, s, init);
    }

    public void atArrayInit(ArrayInit init) throws CompileError {
        throw new CompileError("array initializer is not supported");
    }

    protected void atMultiNewArray(int type, ASTList classname, ASTList size) throws CompileError {
        int dim = size.length();

        int count;

        for (count = 0; size != null; size = size.tail()) {
            ASTree desc = size.head();

            if (desc == null) {
                break;
            }

            ++count;
            desc.accept(this);
            if (this.exprType != 324) {
                throw new CompileError("bad type for array size");
            }
        }

        this.exprType = type;
        this.arrayDim = dim;
        String s;

        if (type == 307) {
            this.className = this.resolveClassName(classname);
            s = toJvmArrayName(this.className, dim);
        } else {
            s = toJvmTypeName(type, dim);
        }

        this.bytecode.addMultiNewarray(s, count);
    }

    public void atCallExpr(CallExpr expr) throws CompileError {
        String mname = null;
        CtClass targetClass = null;
        ASTree method = expr.oprand1();
        ASTList args = (ASTList) expr.oprand2();
        boolean isStatic = false;
        boolean isSpecial = false;
        int aload0pos = -1;
        MemberResolver.Method cached = expr.getMethod();

        if (method instanceof Member) {
            mname = ((Member) method).get();
            targetClass = this.thisClass;
            if (!this.inStaticMethod && (cached == null || !cached.isStatic())) {
                aload0pos = this.bytecode.currentPc();
                this.bytecode.addAload(0);
            } else {
                isStatic = true;
            }
        } else if (method instanceof Keyword) {
            isSpecial = true;
            mname = "<init>";
            targetClass = this.thisClass;
            if (this.inStaticMethod) {
                throw new CompileError("a constructor cannot be static");
            }

            this.bytecode.addAload(0);
            if (((Keyword) method).get() == 336) {
                targetClass = MemberResolver.getSuperclass(targetClass);
            }
        } else if (method instanceof Expr) {
            Expr e = (Expr) method;

            mname = ((Symbol) e.oprand2()).get();
            int op = e.getOperator();

            if (op == 35) {
                targetClass = this.resolver.lookupClass(((Symbol) e.oprand1()).get(), false);
                isStatic = true;
            } else if (op == 46) {
                ASTree target = e.oprand1();
                String classFollowedByDotSuper = TypeChecker.isDotSuper(target);

                if (classFollowedByDotSuper != null) {
                    isSpecial = true;
                    targetClass = MemberResolver.getSuperInterface(this.thisClass, classFollowedByDotSuper);
                    if (!this.inStaticMethod && (cached == null || !cached.isStatic())) {
                        aload0pos = this.bytecode.currentPc();
                        this.bytecode.addAload(0);
                    } else {
                        isStatic = true;
                    }
                } else {
                    if (target instanceof Keyword && ((Keyword) target).get() == 336) {
                        isSpecial = true;
                    }

                    try {
                        target.accept(this);
                    } catch (NoFieldException nofieldexception) {
                        if (nofieldexception.getExpr() != target) {
                            throw nofieldexception;
                        }

                        this.exprType = 307;
                        this.arrayDim = 0;
                        this.className = nofieldexception.getField();
                        isStatic = true;
                    }

                    if (this.arrayDim > 0) {
                        targetClass = this.resolver.lookupClass("java.lang.Object", true);
                    } else if (this.exprType == 307) {
                        targetClass = this.resolver.lookupClassByJvmName(this.className);
                    } else {
                        badMethod();
                    }
                }
            } else {
                badMethod();
            }
        } else {
            fatal();
        }

        this.atMethodCallCore(targetClass, mname, args, isStatic, isSpecial, aload0pos, cached);
    }

    private static void badMethod() throws CompileError {
        throw new CompileError("bad method");
    }

    public void atMethodCallCore(CtClass targetClass, String mname, ASTList args, boolean isStatic, boolean isSpecial, int aload0pos, MemberResolver.Method found) throws CompileError {
        int nargs = this.getMethodArgsLength(args);
        int[] types = new int[nargs];
        int[] dims = new int[nargs];
        String[] cnames = new String[nargs];

        if (!isStatic && found != null && found.isStatic()) {
            this.bytecode.addOpcode(87);
            isStatic = true;
        }

        int stack = this.bytecode.getStackDepth();

        this.atMethodArgs(args, types, dims, cnames);
        if (found == null) {
            found = this.resolver.lookupMethod(targetClass, this.thisClass, this.thisMethod, mname, types, dims, cnames);
        }

        if (found == null) {
            String msg;

            if (mname.equals("<init>")) {
                msg = "constructor not found";
            } else {
                msg = "Method " + mname + " not found in " + targetClass.getName();
            }

            throw new CompileError(msg);
        } else {
            this.atMethodCallCore2(targetClass, mname, isStatic, isSpecial, aload0pos, found);
        }
    }

    private void atMethodCallCore2(CtClass targetClass, String mname, boolean isStatic, boolean isSpecial, int aload0pos, MemberResolver.Method found) throws CompileError {
        CtClass declClass = found.declaring;
        MethodInfo minfo = found.info;
        String desc = minfo.getDescriptor();
        int acc = minfo.getAccessFlags();

        if (mname.equals("<init>")) {
            isSpecial = true;
            if (declClass != targetClass) {
                throw new CompileError("no such constructor: " + targetClass.getName());
            }

            if (declClass != this.thisClass && AccessFlag.isPrivate(acc)) {
                desc = this.getAccessibleConstructor(desc, declClass, minfo);
                this.bytecode.addOpcode(1);
            }
        } else if (AccessFlag.isPrivate(acc)) {
            if (declClass == this.thisClass) {
                isSpecial = true;
            } else {
                isSpecial = false;
                isStatic = true;
                if ((acc & 8) == 0) {
                    desc = Descriptor.insertParameter(declClass.getName(), desc);
                }

                acc = AccessFlag.setPackage(acc) | 8;
                mname = this.getAccessiblePrivate(mname, desc, desc, minfo, declClass);
            }
        }

        boolean popTarget = false;

        if ((acc & 8) != 0) {
            if (!isStatic) {
                isStatic = true;
                if (aload0pos >= 0) {
                    this.bytecode.write(aload0pos, 0);
                } else {
                    popTarget = true;
                }
            }

            this.bytecode.addInvokestatic(declClass, mname, desc);
        } else if (isSpecial) {
            this.bytecode.addInvokespecial(targetClass, mname, desc);
        } else {
            if (!Modifier.isPublic(declClass.getModifiers()) || declClass.isInterface() != targetClass.isInterface()) {
                declClass = targetClass;
            }

            if (declClass.isInterface()) {
                int nargs = Descriptor.nameSize(desc) + 1;

                this.bytecode.addInvokeinterface(declClass, mname, desc, nargs);
            } else {
                if (isStatic) {
                    throw new CompileError(mname + " is not static");
                }

                this.bytecode.addInvokevirtual(declClass, mname, desc);
            }
        }

        this.setReturnType(desc, isStatic, popTarget);
    }

    protected String getAccessiblePrivate(String methodName, String desc, String newDesc, MethodInfo minfo, CtClass declClass) throws CompileError {
        if (this.isEnclosing(declClass, this.thisClass)) {
            AccessorMaker maker = declClass.getAccessorMaker();

            if (maker != null) {
                return maker.getMethodAccessor(methodName, desc, newDesc, minfo);
            }
        }

        throw new CompileError("Method " + methodName + " is private");
    }

    protected String getAccessibleConstructor(String desc, CtClass declClass, MethodInfo minfo) throws CompileError {
        if (this.isEnclosing(declClass, this.thisClass)) {
            AccessorMaker maker = declClass.getAccessorMaker();

            if (maker != null) {
                return maker.getConstructor(declClass, desc, minfo);
            }
        }

        throw new CompileError("the called constructor is private in " + declClass.getName());
    }

    private boolean isEnclosing(CtClass outer, CtClass inner) {
        while (true) {
            try {
                if (inner != null) {
                    inner = inner.getDeclaringClass();
                    if (inner != outer) {
                        continue;
                    }

                    return true;
                }
            } catch (NotFoundException notfoundexception) {
                ;
            }

            return false;
        }
    }

    public int getMethodArgsLength(ASTList args) {
        return ASTList.length(args);
    }

    public void atMethodArgs(ASTList args, int[] types, int[] dims, String[] cnames) throws CompileError {
        for (int i = 0; args != null; args = args.tail()) {
            ASTree a = args.head();

            a.accept(this);
            types[i] = this.exprType;
            dims[i] = this.arrayDim;
            cnames[i] = this.className;
            ++i;
        }

    }

    void setReturnType(String desc, boolean isStatic, boolean popTarget) throws CompileError {
        int i = desc.indexOf(41);

        if (i < 0) {
            badMethod();
        }

        ++i;
        char c = desc.charAt(i);

        int dim;

        for (dim = 0; c == 91; c = desc.charAt(i)) {
            ++dim;
            ++i;
        }

        this.arrayDim = dim;
        int etype;

        if (c == 76) {
            etype = desc.indexOf(59, i + 1);
            if (etype < 0) {
                badMethod();
            }

            this.exprType = 307;
            this.className = desc.substring(i + 1, etype);
        } else {
            this.exprType = MemberResolver.descToType(c);
            this.className = null;
        }

        etype = this.exprType;
        if (isStatic && popTarget) {
            if (is2word(etype, dim)) {
                this.bytecode.addOpcode(93);
                this.bytecode.addOpcode(88);
                this.bytecode.addOpcode(87);
            } else if (etype == 344) {
                this.bytecode.addOpcode(87);
            } else {
                this.bytecode.addOpcode(95);
                this.bytecode.addOpcode(87);
            }
        }

    }

    protected void atFieldAssign(Expr expr, int op, ASTree left, ASTree right, boolean doDup) throws CompileError {
        CtField f = this.fieldAccess(left, false);
        boolean is_static = this.resultStatic;

        if (op != 61 && !is_static) {
            this.bytecode.addOpcode(89);
        }

        int fi;

        if (op == 61) {
            FieldInfo fType = f.getFieldInfo2();

            this.setFieldType(fType);
            AccessorMaker fDim = this.isAccessibleField(f, fType);

            if (fDim == null) {
                fi = this.addFieldrefInfo(f, fType);
            } else {
                fi = 0;
            }
        } else {
            fi = this.atFieldRead(f, is_static);
        }

        int fType1 = this.exprType;
        int fDim1 = this.arrayDim;
        String cname = this.className;

        this.atAssignCore(expr, op, right, fType1, fDim1, cname);
        boolean is2w = is2word(fType1, fDim1);

        if (doDup) {
            int dup_code;

            if (is_static) {
                dup_code = is2w ? 92 : 89;
            } else {
                dup_code = is2w ? 93 : 90;
            }

            this.bytecode.addOpcode(dup_code);
        }

        this.atFieldAssignCore(f, is_static, fi, is2w);
        this.exprType = fType1;
        this.arrayDim = fDim1;
        this.className = cname;
    }

    private void atFieldAssignCore(CtField f, boolean is_static, int fi, boolean is2byte) throws CompileError {
        if (fi != 0) {
            if (is_static) {
                this.bytecode.add(179);
                this.bytecode.growStack(is2byte ? -2 : -1);
            } else {
                this.bytecode.add(181);
                this.bytecode.growStack(is2byte ? -3 : -2);
            }

            this.bytecode.addIndex(fi);
        } else {
            CtClass declClass = f.getDeclaringClass();
            AccessorMaker maker = declClass.getAccessorMaker();
            FieldInfo finfo = f.getFieldInfo2();
            MethodInfo minfo = maker.getFieldSetter(finfo, is_static);

            this.bytecode.addInvokestatic(declClass, minfo.getName(), minfo.getDescriptor());
        }

    }

    public void atMember(Member mem) throws CompileError {
        this.atFieldRead(mem);
    }

    protected void atFieldRead(ASTree expr) throws CompileError {
        CtField f = this.fieldAccess(expr, true);

        if (f == null) {
            this.atArrayLength(expr);
        } else {
            boolean is_static = this.resultStatic;
            ASTree cexpr = TypeChecker.getConstantFieldValue(f);

            if (cexpr == null) {
                this.atFieldRead(f, is_static);
            } else {
                cexpr.accept(this);
                this.setFieldType(f.getFieldInfo2());
            }

        }
    }

    private void atArrayLength(ASTree expr) throws CompileError {
        if (this.arrayDim == 0) {
            throw new CompileError(".length applied to a non array");
        } else {
            this.bytecode.addOpcode(190);
            this.exprType = 324;
            this.arrayDim = 0;
        }
    }

    private int atFieldRead(CtField f, boolean isStatic) throws CompileError {
        FieldInfo finfo = f.getFieldInfo2();
        boolean is2byte = this.setFieldType(finfo);
        AccessorMaker maker = this.isAccessibleField(f, finfo);

        if (maker != null) {
            MethodInfo fi1 = maker.getFieldGetter(finfo, isStatic);

            this.bytecode.addInvokestatic(f.getDeclaringClass(), fi1.getName(), fi1.getDescriptor());
            return 0;
        } else {
            int fi = this.addFieldrefInfo(f, finfo);

            if (isStatic) {
                this.bytecode.add(178);
                this.bytecode.growStack(is2byte ? 2 : 1);
            } else {
                this.bytecode.add(180);
                this.bytecode.growStack(is2byte ? 1 : 0);
            }

            this.bytecode.addIndex(fi);
            return fi;
        }
    }

    private AccessorMaker isAccessibleField(CtField f, FieldInfo finfo) throws CompileError {
        if (AccessFlag.isPrivate(finfo.getAccessFlags()) && f.getDeclaringClass() != this.thisClass) {
            CtClass declClass = f.getDeclaringClass();

            if (this.isEnclosing(declClass, this.thisClass)) {
                AccessorMaker maker = declClass.getAccessorMaker();

                if (maker != null) {
                    return maker;
                } else {
                    throw new CompileError("fatal error.  bug?");
                }
            } else {
                throw new CompileError("Field " + f.getName() + " in " + declClass.getName() + " is private.");
            }
        } else {
            return null;
        }
    }

    private boolean setFieldType(FieldInfo finfo) throws CompileError {
        String type = finfo.getDescriptor();
        int i = 0;
        int dim = 0;

        char c;

        for (c = type.charAt(i); c == 91; c = type.charAt(i)) {
            ++dim;
            ++i;
        }

        this.arrayDim = dim;
        this.exprType = MemberResolver.descToType(c);
        if (c == 76) {
            this.className = type.substring(i + 1, type.indexOf(59, i + 1));
        } else {
            this.className = null;
        }

        boolean is2byte = dim == 0 && (c == 74 || c == 68);

        return is2byte;
    }

    private int addFieldrefInfo(CtField f, FieldInfo finfo) {
        ConstPool cp = this.bytecode.getConstPool();
        String cname = f.getDeclaringClass().getName();
        int ci = cp.addClassInfo(cname);
        String name = finfo.getName();
        String type = finfo.getDescriptor();

        return cp.addFieldrefInfo(ci, name, type);
    }

    protected void atClassObject2(String cname) throws CompileError {
        if (this.getMajorVersion() < 49) {
            super.atClassObject2(cname);
        } else {
            this.bytecode.addLdc(this.bytecode.getConstPool().addClassInfo(cname));
        }

    }

    protected void atFieldPlusPlus(int token, boolean isPost, ASTree oprand, Expr expr, boolean doDup) throws CompileError {
        CtField f = this.fieldAccess(oprand, false);
        boolean is_static = this.resultStatic;

        if (!is_static) {
            this.bytecode.addOpcode(89);
        }

        int fi = this.atFieldRead(f, is_static);
        int t = this.exprType;
        boolean is2w = is2word(t, this.arrayDim);
        int dup_code;

        if (is_static) {
            dup_code = is2w ? 92 : 89;
        } else {
            dup_code = is2w ? 93 : 90;
        }

        this.atPlusPlusCore(dup_code, doDup, token, isPost, expr);
        this.atFieldAssignCore(f, is_static, fi, is2w);
    }

    protected CtField fieldAccess(ASTree expr, boolean acceptLength) throws CompileError {
        if (expr instanceof Member) {
            String e1 = ((Member) expr).get();
            CtField op1 = null;

            try {
                op1 = this.thisClass.getField(e1);
            } catch (NotFoundException notfoundexception) {
                throw new NoFieldException(e1, expr);
            }

            boolean f1 = Modifier.isStatic(op1.getModifiers());

            if (!f1) {
                if (this.inStaticMethod) {
                    throw new CompileError("not available in a static method: " + e1);
                }

                this.bytecode.addAload(0);
            }

            this.resultStatic = f1;
            return op1;
        } else {
            if (expr instanceof Expr) {
                Expr e = (Expr) expr;
                int op = e.getOperator();
                CtField f;

                if (op == 35) {
                    f = this.resolver.lookupField(((Symbol) e.oprand1()).get(), (Symbol) e.oprand2());
                    this.resultStatic = true;
                    return f;
                }

                if (op == 46) {
                    f = null;

                    try {
                        e.oprand1().accept(this);
                        if (this.exprType == 307 && this.arrayDim == 0) {
                            f = this.resolver.lookupFieldByJvmName(this.className, (Symbol) e.oprand2());
                        } else {
                            if (acceptLength && this.arrayDim > 0 && ((Symbol) e.oprand2()).get().equals("length")) {
                                return null;
                            }

                            badLvalue();
                        }

                        boolean nfe = Modifier.isStatic(f.getModifiers());

                        if (nfe) {
                            this.bytecode.addOpcode(87);
                        }

                        this.resultStatic = nfe;
                        return f;
                    } catch (NoFieldException nofieldexception) {
                        if (nofieldexception.getExpr() != e.oprand1()) {
                            throw nofieldexception;
                        }

                        Symbol fname = (Symbol) e.oprand2();
                        String cname = nofieldexception.getField();

                        f = this.resolver.lookupFieldByJvmName2(cname, fname, expr);
                        this.resultStatic = true;
                        return f;
                    }
                }

                badLvalue();
            } else {
                badLvalue();
            }

            this.resultStatic = false;
            return null;
        }
    }

    private static void badLvalue() throws CompileError {
        throw new CompileError("bad l-value");
    }

    public CtClass[] makeParamList(MethodDecl md) throws CompileError {
        ASTList plist = md.getParams();
        CtClass[] names;

        if (plist == null) {
            names = new CtClass[0];
        } else {
            int i = 0;

            for (names = new CtClass[plist.length()]; plist != null; plist = plist.tail()) {
                names[i++] = this.resolver.lookupClass((Declarator) plist.head());
            }
        }

        return names;
    }

    public CtClass[] makeThrowsList(MethodDecl md) throws CompileError {
        ASTList list = md.getThrows();

        if (list == null) {
            return null;
        } else {
            int i = 0;

            CtClass[] clist;

            for (clist = new CtClass[list.length()]; list != null; list = list.tail()) {
                clist[i++] = this.resolver.lookupClassByName((ASTList) list.head());
            }

            return clist;
        }
    }

    protected String resolveClassName(ASTList name) throws CompileError {
        return this.resolver.resolveClassName(name);
    }

    protected String resolveClassName(String jvmName) throws CompileError {
        return this.resolver.resolveJvmClassName(jvmName);
    }

    static class JsrHook2 extends CodeGen.ReturnHook {

        int var;
        int target;

        JsrHook2(CodeGen gen, int[] retTarget) {
            super(gen);
            this.target = retTarget[0];
            this.var = retTarget[1];
        }

        protected boolean doit(Bytecode b, int opcode) {
            switch (opcode) {
            case 172:
                b.addIstore(this.var);
                break;

            case 173:
                b.addLstore(this.var);
                break;

            case 174:
                b.addFstore(this.var);
                break;

            case 175:
                b.addDstore(this.var);
                break;

            case 176:
                b.addAstore(this.var);

            case 177:
                break;

            default:
                throw new RuntimeException("fatal");
            }

            b.addOpcode(167);
            b.addIndex(this.target - b.currentPc() + 3);
            return true;
        }
    }

    static class JsrHook extends CodeGen.ReturnHook {

        ArrayList jsrList = new ArrayList();
        CodeGen cgen;
        int var;

        JsrHook(CodeGen gen) {
            super(gen);
            this.cgen = gen;
            this.var = -1;
        }

        private int getVar(int size) {
            if (this.var < 0) {
                this.var = this.cgen.getMaxLocals();
                this.cgen.incMaxLocals(size);
            }

            return this.var;
        }

        private void jsrJmp(Bytecode b) {
            b.addOpcode(167);
            this.jsrList.add(new int[] { b.currentPc(), this.var});
            b.addIndex(0);
        }

        protected boolean doit(Bytecode b, int opcode) {
            switch (opcode) {
            case 172:
                b.addIstore(this.getVar(1));
                this.jsrJmp(b);
                b.addIload(this.var);
                break;

            case 173:
                b.addLstore(this.getVar(2));
                this.jsrJmp(b);
                b.addLload(this.var);
                break;

            case 174:
                b.addFstore(this.getVar(1));
                this.jsrJmp(b);
                b.addFload(this.var);
                break;

            case 175:
                b.addDstore(this.getVar(2));
                this.jsrJmp(b);
                b.addDload(this.var);
                break;

            case 176:
                b.addAstore(this.getVar(1));
                this.jsrJmp(b);
                b.addAload(this.var);
                break;

            case 177:
                this.jsrJmp(b);
                break;

            default:
                throw new RuntimeException("fatal");
            }

            return false;
        }
    }
}
