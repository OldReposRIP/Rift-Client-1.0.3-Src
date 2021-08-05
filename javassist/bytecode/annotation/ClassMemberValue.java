package javassist.bytecode.annotation;

import java.io.IOException;
import java.lang.reflect.Method;
import javassist.ClassPool;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.SignatureAttribute;

public class ClassMemberValue extends MemberValue {

    int valueIndex;

    public ClassMemberValue(int index, ConstPool cp) {
        super('c', cp);
        this.valueIndex = index;
    }

    public ClassMemberValue(String className, ConstPool cp) {
        super('c', cp);
        this.setValue(className);
    }

    public ClassMemberValue(ConstPool cp) {
        super('c', cp);
        this.setValue("java.lang.Class");
    }

    Object getValue(ClassLoader cl, ClassPool cp, Method m) throws ClassNotFoundException {
        String classname = this.getValue();

        return classname.equals("void") ? Void.TYPE : (classname.equals("int") ? Integer.TYPE : (classname.equals("byte") ? Byte.TYPE : (classname.equals("long") ? Long.TYPE : (classname.equals("double") ? Double.TYPE : (classname.equals("float") ? Float.TYPE : (classname.equals("char") ? Character.TYPE : (classname.equals("short") ? Short.TYPE : (classname.equals("boolean") ? Boolean.TYPE : loadClass(cl, classname)))))))));
    }

    Class getType(ClassLoader cl) throws ClassNotFoundException {
        return loadClass(cl, "java.lang.Class");
    }

    public String getValue() {
        String v = this.cp.getUtf8Info(this.valueIndex);

        try {
            return SignatureAttribute.toTypeSignature(v).jvmTypeName();
        } catch (BadBytecode badbytecode) {
            throw new RuntimeException(badbytecode);
        }
    }

    public void setValue(String newClassName) {
        String setTo = Descriptor.of(newClassName);

        this.valueIndex = this.cp.addUtf8Info(setTo);
    }

    public String toString() {
        return this.getValue().replace('$', '.') + ".class";
    }

    public void write(AnnotationsWriter writer) throws IOException {
        writer.classInfoIndex(this.cp.getUtf8Info(this.valueIndex));
    }

    public void accept(MemberValueVisitor visitor) {
        visitor.visitClassMemberValue(this);
    }
}
