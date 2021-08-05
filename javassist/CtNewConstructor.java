package javassist;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ConstPool;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;

public class CtNewConstructor {

    public static final int PASS_NONE = 0;
    public static final int PASS_ARRAY = 1;
    public static final int PASS_PARAMS = 2;

    public static CtConstructor make(String src, CtClass declaring) throws CannotCompileException {
        Javac compiler = new Javac(declaring);

        try {
            CtMember e = compiler.compile(src);

            if (e instanceof CtConstructor) {
                return (CtConstructor) e;
            }
        } catch (CompileError compileerror) {
            throw new CannotCompileException(compileerror);
        }

        throw new CannotCompileException("not a constructor");
    }

    public static CtConstructor make(CtClass[] nameeters, CtClass[] exceptions, String body, CtClass declaring) throws CannotCompileException {
        try {
            CtConstructor e = new CtConstructor(nameeters, declaring);

            e.setExceptionTypes(exceptions);
            e.setBody(body);
            return e;
        } catch (NotFoundException notfoundexception) {
            throw new CannotCompileException(notfoundexception);
        }
    }

    public static CtConstructor copy(CtConstructor c, CtClass declaring, ClassMap map) throws CannotCompileException {
        return new CtConstructor(c, declaring, map);
    }

    public static CtConstructor defaultConstructor(CtClass declaring) throws CannotCompileException {
        CtConstructor cons = new CtConstructor((CtClass[]) null, declaring);
        ConstPool cp = declaring.getClassFile2().getConstPool();
        Bytecode code = new Bytecode(cp, 1, 1);

        code.addAload(0);

        try {
            code.addInvokespecial(declaring.getSuperclass(), "<init>", "()V");
        } catch (NotFoundException notfoundexception) {
            throw new CannotCompileException(notfoundexception);
        }

        code.add(177);
        cons.getMethodInfo2().setCodeAttribute(code.toCodeAttribute());
        return cons;
    }

    public static CtConstructor skeleton(CtClass[] nameeters, CtClass[] exceptions, CtClass declaring) throws CannotCompileException {
        return make(nameeters, exceptions, 0, (CtMethod) null, (CtMethod.ConstParameter) null, declaring);
    }

    public static CtConstructor make(CtClass[] nameeters, CtClass[] exceptions, CtClass declaring) throws CannotCompileException {
        return make(nameeters, exceptions, 2, (CtMethod) null, (CtMethod.ConstParameter) null, declaring);
    }

    public static CtConstructor make(CtClass[] nameeters, CtClass[] exceptions, int howto, CtMethod body, CtMethod.ConstParameter cname, CtClass declaring) throws CannotCompileException {
        return CtNewWrappedConstructor.wrapped(nameeters, exceptions, howto, body, cname, declaring);
    }
}
