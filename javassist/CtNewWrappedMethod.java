package javassist;

import java.util.Hashtable;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.SyntheticAttribute;
import javassist.compiler.JvstCodeGen;

class CtNewWrappedMethod {

    private static final String addedWrappedMethod = "_added_m$";

    public static CtMethod wrapped(CtClass returnType, String mname, CtClass[] nameeterTypes, CtClass[] exceptionTypes, CtMethod body, CtMethod.ConstParameter constParam, CtClass declaring) throws CannotCompileException {
        CtMethod mt = new CtMethod(returnType, mname, nameeterTypes, declaring);

        mt.setModifiers(body.getModifiers());

        try {
            mt.setExceptionTypes(exceptionTypes);
        } catch (NotFoundException notfoundexception) {
            throw new CannotCompileException(notfoundexception);
        }

        Bytecode code = makeBody(declaring, declaring.getClassFile2(), body, nameeterTypes, returnType, constParam);
        MethodInfo minfo = mt.getMethodInfo2();

        minfo.setCodeAttribute(code.toCodeAttribute());
        return mt;
    }

    static Bytecode makeBody(CtClass clazz, ClassFile classfile, CtMethod wrappedBody, CtClass[] nameeters, CtClass returnType, CtMethod.ConstParameter cname) throws CannotCompileException {
        boolean isStatic = Modifier.isStatic(wrappedBody.getModifiers());
        Bytecode code = new Bytecode(classfile.getConstPool(), 0, 0);
        int stacksize = makeBody0(clazz, classfile, wrappedBody, isStatic, nameeters, returnType, cname, code);

        code.setMaxStack(stacksize);
        code.setMaxLocals(isStatic, nameeters, 0);
        return code;
    }

    protected static int makeBody0(CtClass clazz, ClassFile classfile, CtMethod wrappedBody, boolean isStatic, CtClass[] nameeters, CtClass returnType, CtMethod.ConstParameter cname, Bytecode code) throws CannotCompileException {
        if (!(clazz instanceof CtClassType)) {
            throw new CannotCompileException("bad declaring class" + clazz.getName());
        } else {
            if (!isStatic) {
                code.addAload(0);
            }

            int stacksize = compileParameterList(code, nameeters, isStatic ? 0 : 1);
            int stacksize2;
            String desc;

            if (cname == null) {
                stacksize2 = 0;
                desc = CtMethod.ConstParameter.defaultDescriptor();
            } else {
                stacksize2 = cname.compile(code);
                desc = cname.descriptor();
            }

            checkSignature(wrappedBody, desc);

            String bodyname;

            try {
                bodyname = addBodyMethod((CtClassType) clazz, classfile, wrappedBody);
            } catch (BadBytecode badbytecode) {
                throw new CannotCompileException(badbytecode);
            }

            if (isStatic) {
                code.addInvokestatic(Bytecode.THIS, bodyname, desc);
            } else {
                code.addInvokespecial(Bytecode.THIS, bodyname, desc);
            }

            compileReturn(code, returnType);
            if (stacksize < stacksize2 + 2) {
                stacksize = stacksize2 + 2;
            }

            return stacksize;
        }
    }

    private static void checkSignature(CtMethod wrappedBody, String descriptor) throws CannotCompileException {
        if (!descriptor.equals(wrappedBody.getMethodInfo2().getDescriptor())) {
            throw new CannotCompileException("wrapped method with a bad signature: " + wrappedBody.getDeclaringClass().getName() + '.' + wrappedBody.getName());
        }
    }

    private static String addBodyMethod(CtClassType clazz, ClassFile classfile, CtMethod src) throws BadBytecode, CannotCompileException {
        Hashtable bodies = clazz.getHiddenMethods();
        String bodyname = (String) bodies.get(src);

        if (bodyname == null) {
            do {
                bodyname = "_added_m$" + clazz.getUniqueNumber();
            } while (classfile.getMethod(bodyname) != null);

            ClassMap map = new ClassMap();

            map.put(src.getDeclaringClass().getName(), clazz.getName());
            MethodInfo body = new MethodInfo(classfile.getConstPool(), bodyname, src.getMethodInfo2(), map);
            int acc = body.getAccessFlags();

            body.setAccessFlags(AccessFlag.setPrivate(acc));
            body.addAttribute(new SyntheticAttribute(classfile.getConstPool()));
            classfile.addMethod(body);
            bodies.put(src, bodyname);
            CtMember.Cache cache = clazz.hasMemberCache();

            if (cache != null) {
                cache.addMethod(new CtMethod(body, clazz));
            }
        }

        return bodyname;
    }

    static int compileParameterList(Bytecode code, CtClass[] names, int regno) {
        return JvstCodeGen.compileParameterList(code, names, regno);
    }

    private static void compileReturn(Bytecode code, CtClass type) {
        if (type.isPrimitive()) {
            CtPrimitiveType pt = (CtPrimitiveType) type;

            if (pt != CtClass.voidType) {
                String wrapper = pt.getWrapperName();

                code.addCheckcast(wrapper);
                code.addInvokevirtual(wrapper, pt.getGetMethodName(), pt.getGetMethodDescriptor());
            }

            code.addOpcode(pt.getReturnOp());
        } else {
            code.addCheckcast(type);
            code.addOpcode(176);
        }

    }
}
