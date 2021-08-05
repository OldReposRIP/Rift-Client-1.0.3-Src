package javassist;

import java.util.Map;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;

public class CtNewMethod {

    public static CtMethod make(String src, CtClass declaring) throws CannotCompileException {
        return make(src, declaring, (String) null, (String) null);
    }

    public static CtMethod make(String src, CtClass declaring, String delegateObj, String delegateMethod) throws CannotCompileException {
        Javac compiler = new Javac(declaring);

        try {
            if (delegateMethod != null) {
                compiler.recordProceed(delegateObj, delegateMethod);
            }

            CtMember e = compiler.compile(src);

            if (e instanceof CtMethod) {
                return (CtMethod) e;
            }
        } catch (CompileError compileerror) {
            throw new CannotCompileException(compileerror);
        }

        throw new CannotCompileException("not a method");
    }

    public static CtMethod make(CtClass returnType, String mname, CtClass[] nameeters, CtClass[] exceptions, String body, CtClass declaring) throws CannotCompileException {
        return make(1, returnType, mname, nameeters, exceptions, body, declaring);
    }

    public static CtMethod make(int modifiers, CtClass returnType, String mname, CtClass[] nameeters, CtClass[] exceptions, String body, CtClass declaring) throws CannotCompileException {
        try {
            CtMethod e = new CtMethod(returnType, mname, nameeters, declaring);

            e.setModifiers(modifiers);
            e.setExceptionTypes(exceptions);
            e.setBody(body);
            return e;
        } catch (NotFoundException notfoundexception) {
            throw new CannotCompileException(notfoundexception);
        }
    }

    public static CtMethod copy(CtMethod src, CtClass declaring, ClassMap map) throws CannotCompileException {
        return new CtMethod(src, declaring, map);
    }

    public static CtMethod copy(CtMethod src, String name, CtClass declaring, ClassMap map) throws CannotCompileException {
        CtMethod cm = new CtMethod(src, declaring, map);

        cm.setName(name);
        return cm;
    }

    public static CtMethod abstractMethod(CtClass returnType, String mname, CtClass[] nameeters, CtClass[] exceptions, CtClass declaring) throws NotFoundException {
        CtMethod cm = new CtMethod(returnType, mname, nameeters, declaring);

        cm.setExceptionTypes(exceptions);
        return cm;
    }

    public static CtMethod getter(String methodName, CtField field) throws CannotCompileException {
        FieldInfo finfo = field.getFieldInfo2();
        String fieldType = finfo.getDescriptor();
        String desc = "()" + fieldType;
        ConstPool cp = finfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, methodName, desc);

        minfo.setAccessFlags(1);
        Bytecode code = new Bytecode(cp, 2, 1);

        try {
            String cc = finfo.getName();

            if ((finfo.getAccessFlags() & 8) == 0) {
                code.addAload(0);
                code.addGetfield(Bytecode.THIS, cc, fieldType);
            } else {
                code.addGetstatic(Bytecode.THIS, cc, fieldType);
            }

            code.addReturn(field.getType());
        } catch (NotFoundException notfoundexception) {
            throw new CannotCompileException(notfoundexception);
        }

        minfo.setCodeAttribute(code.toCodeAttribute());
        CtClass cc1 = field.getDeclaringClass();

        return new CtMethod(minfo, cc1);
    }

    public static CtMethod setter(String methodName, CtField field) throws CannotCompileException {
        FieldInfo finfo = field.getFieldInfo2();
        String fieldType = finfo.getDescriptor();
        String desc = "(" + fieldType + ")V";
        ConstPool cp = finfo.getConstPool();
        MethodInfo minfo = new MethodInfo(cp, methodName, desc);

        minfo.setAccessFlags(1);
        Bytecode code = new Bytecode(cp, 3, 3);

        try {
            String cc = finfo.getName();

            if ((finfo.getAccessFlags() & 8) == 0) {
                code.addAload(0);
                code.addLoad(1, field.getType());
                code.addPutfield(Bytecode.THIS, cc, fieldType);
            } else {
                code.addLoad(1, field.getType());
                code.addPutstatic(Bytecode.THIS, cc, fieldType);
            }

            code.addReturn((CtClass) null);
        } catch (NotFoundException notfoundexception) {
            throw new CannotCompileException(notfoundexception);
        }

        minfo.setCodeAttribute(code.toCodeAttribute());
        CtClass cc1 = field.getDeclaringClass();

        return new CtMethod(minfo, cc1);
    }

    public static CtMethod delegator(CtMethod delegate, CtClass declaring) throws CannotCompileException {
        try {
            return delegator0(delegate, declaring);
        } catch (NotFoundException notfoundexception) {
            throw new CannotCompileException(notfoundexception);
        }
    }

    private static CtMethod delegator0(CtMethod delegate, CtClass declaring) throws CannotCompileException, NotFoundException {
        MethodInfo deleInfo = delegate.getMethodInfo2();
        String methodName = deleInfo.getName();
        String desc = deleInfo.getDescriptor();
        ConstPool cp = declaring.getClassFile2().getConstPool();
        MethodInfo minfo = new MethodInfo(cp, methodName, desc);

        minfo.setAccessFlags(deleInfo.getAccessFlags());
        ExceptionsAttribute eattr = deleInfo.getExceptionsAttribute();

        if (eattr != null) {
            minfo.setExceptionsAttribute((ExceptionsAttribute) eattr.copy(cp, (Map) null));
        }

        Bytecode code = new Bytecode(cp, 0, 0);
        boolean isStatic = Modifier.isStatic(delegate.getModifiers());
        CtClass deleClass = delegate.getDeclaringClass();
        CtClass[] names = delegate.getParameterTypes();
        int s;

        if (isStatic) {
            s = code.addLoadParameters(names, 0);
            code.addInvokestatic(deleClass, methodName, desc);
        } else {
            code.addLoad(0, deleClass);
            s = code.addLoadParameters(names, 1);
            code.addInvokespecial(deleClass, methodName, desc);
        }

        code.addReturn(delegate.getReturnType());
        ++s;
        code.setMaxLocals(s);
        code.setMaxStack(s < 2 ? 2 : s);
        minfo.setCodeAttribute(code.toCodeAttribute());
        return new CtMethod(minfo, declaring);
    }

    public static CtMethod wrapped(CtClass returnType, String mname, CtClass[] nameeterTypes, CtClass[] exceptionTypes, CtMethod body, CtMethod.ConstParameter constParam, CtClass declaring) throws CannotCompileException {
        return CtNewWrappedMethod.wrapped(returnType, mname, nameeterTypes, exceptionTypes, body, constParam, declaring);
    }
}
