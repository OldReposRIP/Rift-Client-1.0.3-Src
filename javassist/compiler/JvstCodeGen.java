package javassist.compiler;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import javassist.bytecode.Descriptor;
import javassist.compiler.ast.ASTList;
import javassist.compiler.ast.ASTree;
import javassist.compiler.ast.CallExpr;
import javassist.compiler.ast.CastExpr;
import javassist.compiler.ast.Declarator;
import javassist.compiler.ast.Expr;
import javassist.compiler.ast.Member;
import javassist.compiler.ast.Stmnt;
import javassist.compiler.ast.Symbol;

public class JvstCodeGen extends MemberCodeGen {

    String nameArrayName = null;
    String nameListName = null;
    CtClass[] nameTypeList = null;
    private int nameVarBase = 0;
    private boolean useParam0 = false;
    private String name0Type = null;
    public static final String sigName = "$sig";
    public static final String dollarTypeName = "$type";
    public static final String clazzName = "$class";
    private CtClass dollarType = null;
    CtClass returnType = null;
    String returnCastName = null;
    private String returnVarName = null;
    public static final String wrapperCastName = "$w";
    String proceedName = null;
    public static final String cflowName = "$cflow";
    ProceedHandler procHandler = null;

    public JvstCodeGen(Bytecode b, CtClass cc, ClassPool cp) {
        super(b, cc, cp);
        this.setTypeChecker(new JvstTypeChecker(cc, cp, this));
    }

    private int indexOfParam1() {
        return this.nameVarBase + (this.useParam0 ? 1 : 0);
    }

    public void setProceedHandler(ProceedHandler h, String name) {
        this.proceedName = name;
        this.procHandler = h;
    }

    public void addNullIfVoid() {
        if (this.exprType == 344) {
            this.bytecode.addOpcode(1);
            this.exprType = 307;
            this.arrayDim = 0;
            this.className = "java/lang/Object";
        }

    }

    public void atMember(Member mem) throws CompileError {
        String name = mem.get();

        if (name.equals(this.nameArrayName)) {
            compileParameterList(this.bytecode, this.nameTypeList, this.indexOfParam1());
            this.exprType = 307;
            this.arrayDim = 1;
            this.className = "java/lang/Object";
        } else if (name.equals("$sig")) {
            this.bytecode.addLdc(Descriptor.ofMethod(this.returnType, this.nameTypeList));
            this.bytecode.addInvokestatic("javassist/runtime/Desc", "getParams", "(Ljava/lang/String;)[Ljava/lang/Class;");
            this.exprType = 307;
            this.arrayDim = 1;
            this.className = "java/lang/Class";
        } else if (name.equals("$type")) {
            if (this.dollarType == null) {
                throw new CompileError("$type is not available");
            }

            this.bytecode.addLdc(Descriptor.of(this.dollarType));
            this.callGetType("getType");
        } else if (name.equals("$class")) {
            if (this.name0Type == null) {
                throw new CompileError("$class is not available");
            }

            this.bytecode.addLdc(this.name0Type);
            this.callGetType("getClazz");
        } else {
            super.atMember(mem);
        }

    }

    private void callGetType(String method) {
        this.bytecode.addInvokestatic("javassist/runtime/Desc", method, "(Ljava/lang/String;)Ljava/lang/Class;");
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = "java/lang/Class";
    }

    protected void atFieldAssign(Expr expr, int op, ASTree left, ASTree right, boolean doDup) throws CompileError {
        if (left instanceof Member && ((Member) left).get().equals(this.nameArrayName)) {
            if (op != 61) {
                throw new CompileError("bad operator for " + this.nameArrayName);
            }

            right.accept(this);
            if (this.arrayDim != 1 || this.exprType != 307) {
                throw new CompileError("invalid type for " + this.nameArrayName);
            }

            this.atAssignParamList(this.nameTypeList, this.bytecode);
            if (!doDup) {
                this.bytecode.addOpcode(87);
            }
        } else {
            super.atFieldAssign(expr, op, left, right, doDup);
        }

    }

    protected void atAssignParamList(CtClass[] names, Bytecode code) throws CompileError {
        if (names != null) {
            int i = this.indexOfParam1();
            int n = names.length;

            for (int i = 0; i < n; ++i) {
                code.addOpcode(89);
                code.addIconst(i);
                code.addOpcode(50);
                this.compileUnwrapValue(names[i], code);
                code.addStore(i, names[i]);
                i += is2word(this.exprType, this.arrayDim) ? 2 : 1;
            }

        }
    }

    public void atCastExpr(CastExpr expr) throws CompileError {
        ASTList classname = expr.getClassName();

        if (classname != null && expr.getArrayDim() == 0) {
            ASTree p = classname.head();

            if (p instanceof Symbol && classname.tail() == null) {
                String typename = ((Symbol) p).get();

                if (typename.equals(this.returnCastName)) {
                    this.atCastToRtype(expr);
                    return;
                }

                if (typename.equals("$w")) {
                    this.atCastToWrapper(expr);
                    return;
                }
            }
        }

        super.atCastExpr(expr);
    }

    protected void atCastToRtype(CastExpr expr) throws CompileError {
        expr.getOprand().accept(this);
        if (this.exprType != 344 && !isRefType(this.exprType) && this.arrayDim <= 0) {
            if (!(this.returnType instanceof CtPrimitiveType)) {
                throw new CompileError("invalid cast");
            }

            CtPrimitiveType pt = (CtPrimitiveType) this.returnType;
            int destType = MemberResolver.descToType(pt.getDescriptor());

            this.atNumCastExpr(this.exprType, destType);
            this.exprType = destType;
            this.arrayDim = 0;
            this.className = null;
        } else {
            this.compileUnwrapValue(this.returnType, this.bytecode);
        }

    }

    protected void atCastToWrapper(CastExpr expr) throws CompileError {
        expr.getOprand().accept(this);
        if (!isRefType(this.exprType) && this.arrayDim <= 0) {
            CtClass clazz = this.resolver.lookupClass(this.exprType, this.arrayDim, this.className);

            if (clazz instanceof CtPrimitiveType) {
                CtPrimitiveType pt = (CtPrimitiveType) clazz;
                String wrapper = pt.getWrapperName();

                this.bytecode.addNew(wrapper);
                this.bytecode.addOpcode(89);
                if (pt.getDataSize() > 1) {
                    this.bytecode.addOpcode(94);
                } else {
                    this.bytecode.addOpcode(93);
                }

                this.bytecode.addOpcode(88);
                this.bytecode.addInvokespecial(wrapper, "<init>", "(" + pt.getDescriptor() + ")V");
                this.exprType = 307;
                this.arrayDim = 0;
                this.className = "java/lang/Object";
            }

        }
    }

    public void atCallExpr(CallExpr expr) throws CompileError {
        ASTree method = expr.oprand1();

        if (method instanceof Member) {
            String name = ((Member) method).get();

            if (this.procHandler != null && name.equals(this.proceedName)) {
                this.procHandler.doit(this, this.bytecode, (ASTList) expr.oprand2());
                return;
            }

            if (name.equals("$cflow")) {
                this.atCflow((ASTList) expr.oprand2());
                return;
            }
        }

        super.atCallExpr(expr);
    }

    protected void atCflow(ASTList cname) throws CompileError {
        StringBuffer sbuf = new StringBuffer();

        if (cname != null && cname.tail() == null) {
            makeCflowName(sbuf, cname.head());
            String name = sbuf.toString();
            Object[] names = this.resolver.getClassPool().lookupCflow(name);

            if (names == null) {
                throw new CompileError("no such $cflow: " + name);
            } else {
                this.bytecode.addGetstatic((String) names[0], (String) names[1], "Ljavassist/runtime/Cflow;");
                this.bytecode.addInvokevirtual("javassist.runtime.Cflow", "value", "()I");
                this.exprType = 324;
                this.arrayDim = 0;
                this.className = null;
            }
        } else {
            throw new CompileError("bad $cflow");
        }
    }

    private static void makeCflowName(StringBuffer sbuf, ASTree name) throws CompileError {
        if (name instanceof Symbol) {
            sbuf.append(((Symbol) name).get());
        } else {
            if (name instanceof Expr) {
                Expr expr = (Expr) name;

                if (expr.getOperator() == 46) {
                    makeCflowName(sbuf, expr.oprand1());
                    sbuf.append('.');
                    makeCflowName(sbuf, expr.oprand2());
                    return;
                }
            }

            throw new CompileError("bad $cflow");
        }
    }

    public boolean isParamListName(ASTList args) {
        if (this.nameTypeList != null && args != null && args.tail() == null) {
            ASTree left = args.head();

            return left instanceof Member && ((Member) left).get().equals(this.nameListName);
        } else {
            return false;
        }
    }

    public int getMethodArgsLength(ASTList args) {
        String pname = this.nameListName;

        int n;

        for (n = 0; args != null; args = args.tail()) {
            ASTree a = args.head();

            if (a instanceof Member && ((Member) a).get().equals(pname)) {
                if (this.nameTypeList != null) {
                    n += this.nameTypeList.length;
                }
            } else {
                ++n;
            }
        }

        return n;
    }

    public void atMethodArgs(ASTList args, int[] types, int[] dims, String[] cnames) throws CompileError {
        CtClass[] names = this.nameTypeList;
        String pname = this.nameListName;

        for (int i = 0; args != null; args = args.tail()) {
            ASTree a = args.head();

            if (a instanceof Member && ((Member) a).get().equals(pname)) {
                if (names != null) {
                    int n = names.length;
                    int regno = this.indexOfParam1();

                    for (int k = 0; k < n; ++k) {
                        CtClass p = names[k];

                        regno += this.bytecode.addLoad(regno, p);
                        this.setType(p);
                        types[i] = this.exprType;
                        dims[i] = this.arrayDim;
                        cnames[i] = this.className;
                        ++i;
                    }
                }
            } else {
                a.accept(this);
                types[i] = this.exprType;
                dims[i] = this.arrayDim;
                cnames[i] = this.className;
                ++i;
            }
        }

    }

    void compileInvokeSpecial(ASTree target, int methodIndex, String descriptor, ASTList args) throws CompileError {
        target.accept(this);
        int nargs = this.getMethodArgsLength(args);

        this.atMethodArgs(args, new int[nargs], new int[nargs], new String[nargs]);
        this.bytecode.addInvokespecial(methodIndex, descriptor);
        this.setReturnType(descriptor, false, false);
        this.addNullIfVoid();
    }

    protected void atReturnStmnt(Stmnt st) throws CompileError {
        ASTree result = st.getLeft();

        if (result != null && this.returnType == CtClass.voidType) {
            this.compileExpr(result);
            if (is2word(this.exprType, this.arrayDim)) {
                this.bytecode.addOpcode(88);
            } else if (this.exprType != 344) {
                this.bytecode.addOpcode(87);
            }

            result = null;
        }

        this.atReturnStmnt2(result);
    }

    public int recordReturnType(CtClass type, String castName, String resultName, SymbolTable tbl) throws CompileError {
        this.returnType = type;
        this.returnCastName = castName;
        this.returnVarName = resultName;
        if (resultName == null) {
            return -1;
        } else {
            int i = this.getMaxLocals();
            int locals = i + this.recordVar(type, resultName, i, tbl);

            this.setMaxLocals(locals);
            return i;
        }
    }

    public void recordType(CtClass t) {
        this.dollarType = t;
    }

    public int recordParams(CtClass[] names, boolean isStatic, String prefix, String nameVarName, String namesName, SymbolTable tbl) throws CompileError {
        return this.recordParams(names, isStatic, prefix, nameVarName, namesName, !isStatic, 0, this.getThisName(), tbl);
    }

    public int recordParams(CtClass[] names, boolean isStatic, String prefix, String nameVarName, String namesName, boolean use0, int nameBase, String target, SymbolTable tbl) throws CompileError {
        this.nameTypeList = names;
        this.nameArrayName = nameVarName;
        this.nameListName = namesName;
        this.nameVarBase = nameBase;
        this.useParam0 = use0;
        if (target != null) {
            this.name0Type = MemberResolver.jvmToJavaName(target);
        }

        this.inStaticMethod = isStatic;
        int i = nameBase;

        if (use0) {
            String i = prefix + "0";
            String s = MemberResolver.javaToJvmName(target);

            i = nameBase + 1;
            Declarator decl = new Declarator(307, s, 0, nameBase, new Symbol(i));

            tbl.append(i, decl);
        }

        for (int j = 0; j < names.length; ++j) {
            i += this.recordVar(names[j], prefix + (j + 1), i, tbl);
        }

        if (this.getMaxLocals() < i) {
            this.setMaxLocals(i);
        }

        return i;
    }

    public int recordVariable(CtClass type, String s, SymbolTable tbl) throws CompileError {
        if (s == null) {
            return -1;
        } else {
            int i = this.getMaxLocals();
            int locals = i + this.recordVar(type, s, i, tbl);

            this.setMaxLocals(locals);
            return i;
        }
    }

    private int recordVar(CtClass cc, String s, int i, SymbolTable tbl) throws CompileError {
        if (cc == CtClass.voidType) {
            this.exprType = 307;
            this.arrayDim = 0;
            this.className = "java/lang/Object";
        } else {
            this.setType(cc);
        }

        Declarator decl = new Declarator(this.exprType, this.className, this.arrayDim, i, new Symbol(s));

        tbl.append(s, decl);
        return is2word(this.exprType, this.arrayDim) ? 2 : 1;
    }

    public void recordVariable(String typeDesc, String s, int i, SymbolTable tbl) throws CompileError {
        char c;
        int dim;

        for (dim = 0; (c = typeDesc.charAt(dim)) == 91; ++dim) {
            ;
        }

        int type = MemberResolver.descToType(c);
        String cname = null;

        if (type == 307) {
            if (dim == 0) {
                cname = typeDesc.substring(1, typeDesc.length() - 1);
            } else {
                cname = typeDesc.substring(dim + 1, typeDesc.length() - 1);
            }
        }

        Declarator decl = new Declarator(type, cname, dim, i, new Symbol(s));

        tbl.append(s, decl);
    }

    public static int compileParameterList(Bytecode code, CtClass[] names, int regno) {
        if (names == null) {
            code.addIconst(0);
            code.addAnewarray("java.lang.Object");
            return 1;
        } else {
            CtClass[] args = new CtClass[1];
            int n = names.length;

            code.addIconst(n);
            code.addAnewarray("java.lang.Object");

            for (int i = 0; i < n; ++i) {
                code.addOpcode(89);
                code.addIconst(i);
                if (names[i].isPrimitive()) {
                    CtPrimitiveType pt = (CtPrimitiveType) names[i];
                    String wrapper = pt.getWrapperName();

                    code.addNew(wrapper);
                    code.addOpcode(89);
                    int s = code.addLoad(regno, pt);

                    regno += s;
                    args[0] = pt;
                    code.addInvokespecial(wrapper, "<init>", Descriptor.ofMethod(CtClass.voidType, args));
                } else {
                    code.addAload(regno);
                    ++regno;
                }

                code.addOpcode(83);
            }

            return 8;
        }
    }

    protected void compileUnwrapValue(CtClass type, Bytecode code) throws CompileError {
        if (type == CtClass.voidType) {
            this.addNullIfVoid();
        } else if (this.exprType == 344) {
            throw new CompileError("invalid type for " + this.returnCastName);
        } else {
            if (type instanceof CtPrimitiveType) {
                CtPrimitiveType pt = (CtPrimitiveType) type;
                String wrapper = pt.getWrapperName();

                code.addCheckcast(wrapper);
                code.addInvokevirtual(wrapper, pt.getGetMethodName(), pt.getGetMethodDescriptor());
                this.setType(type);
            } else {
                code.addCheckcast(type);
                this.setType(type);
            }

        }
    }

    public void setType(CtClass type) throws CompileError {
        this.setType(type, 0);
    }

    private void setType(CtClass type, int dim) throws CompileError {
        if (type.isPrimitive()) {
            CtPrimitiveType e = (CtPrimitiveType) type;

            this.exprType = MemberResolver.descToType(e.getDescriptor());
            this.arrayDim = dim;
            this.className = null;
        } else if (type.isArray()) {
            try {
                this.setType(type.getComponentType(), dim + 1);
            } catch (NotFoundException notfoundexception) {
                throw new CompileError("undefined type: " + type.getName());
            }
        } else {
            this.exprType = 307;
            this.arrayDim = dim;
            this.className = MemberResolver.javaToJvmName(type.getName());
        }

    }

    public void doNumCast(CtClass type) throws CompileError {
        if (this.arrayDim == 0 && !isRefType(this.exprType)) {
            if (!(type instanceof CtPrimitiveType)) {
                throw new CompileError("type mismatch");
            }

            CtPrimitiveType pt = (CtPrimitiveType) type;

            this.atNumCastExpr(this.exprType, MemberResolver.descToType(pt.getDescriptor()));
        }

    }
}
