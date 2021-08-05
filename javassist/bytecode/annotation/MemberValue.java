package javassist.bytecode.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;

public abstract class MemberValue {

    ConstPool cp;
    char tag;

    MemberValue(char tag, ConstPool cp) {
        this.cp = cp;
        this.tag = tag;
    }

    abstract Object getValue(ClassLoader classloader, ClassPool classpool, Method method) throws ClassNotFoundException;

    abstract Class getType(ClassLoader classloader) throws ClassNotFoundException;

    static Class loadClass(ClassLoader cl, String classname) throws ClassNotFoundException, NoSuchClassError {
        try {
            return Class.forName(convertFromArray(classname), true, cl);
        } catch (LinkageError linkageerror) {
            throw new NoSuchClassError(classname, linkageerror);
        }
    }

    private static String convertFromArray(String classname) {
        int index = classname.indexOf("[]");

        if (index == -1) {
            return classname;
        } else {
            String rawType = classname.substring(0, index);

            StringBuffer sb;

            for (sb = new StringBuffer(Descriptor.of(rawType)); index != -1; index = classname.indexOf("[]", index + 1)) {
                sb.insert(0, "[");
            }

            return sb.toString().replace('/', '.');
        }
    }

    public abstract void accept(MemberValueVisitor membervaluevisitor);

    public abstract void write(AnnotationsWriter annotationswriter) throws IOException;
}
