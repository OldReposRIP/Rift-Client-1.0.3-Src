package javassist;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;

class CtNewWrappedConstructor extends CtNewWrappedMethod {

    private static final int PASS_NONE = 0;
    private static final int PASS_PARAMS = 2;

    public static CtConstructor wrapped(CtClass[] nameeterTypes, CtClass[] exceptionTypes, int howToCallSuper, CtMethod body, CtMethod.ConstParameter constParam, CtClass declaring) throws CannotCompileException {
        try {
            CtConstructor e = new CtConstructor(nameeterTypes, declaring);

            e.setExceptionTypes(exceptionTypes);
            Bytecode code = makeBody(declaring, declaring.getClassFile2(), howToCallSuper, body, nameeterTypes, constParam);

            e.getMethodInfo2().setCodeAttribute(code.toCodeAttribute());
            return e;
        } catch (NotFoundException notfoundexception) {
            throw new CannotCompileException(notfoundexception);
        }
    }

    protected static Bytecode makeBody(CtClass declaring, ClassFile classfile, int howToCallSuper, CtMethod wrappedBody, CtClass[] nameeters, CtMethod.ConstParameter cname) throws CannotCompileException {
        int superclazz = classfile.getSuperclassId();
        Bytecode code = new Bytecode(classfile.getConstPool(), 0, 0);

        code.setMaxLocals(false, nameeters, 0);
        code.addAload(0);
        int stacksize;
        int stacksize2;

        if (howToCallSuper == 0) {
            stacksize = 1;
            code.addInvokespecial(superclazz, "<init>", "()V");
        } else if (howToCallSuper == 2) {
            stacksize = code.addLoadParameters(nameeters, 1) + 1;
            code.addInvokespecial(superclazz, "<init>", Descriptor.ofConstructor(nameeters));
        } else {
            stacksize = compileParameterList(code, nameeters, 1);
            String desc;

            if (cname == null) {
                stacksize2 = 2;
                desc = CtMethod.ConstParameter.defaultConstDescriptor();
            } else {
                stacksize2 = cname.compile(code) + 2;
                desc = cname.constDescriptor();
            }

            if (stacksize < stacksize2) {
                stacksize = stacksize2;
            }

            code.addInvokespecial(superclazz, "<init>", desc);
        }

        if (wrappedBody == null) {
            code.add(177);
        } else {
            stacksize2 = makeBody0(declaring, classfile, wrappedBody, false, nameeters, CtClass.voidType, cname, code);
            if (stacksize < stacksize2) {
                stacksize = stacksize2;
            }
        }

        code.setMaxStack(stacksize);
        return code;
    }
}
