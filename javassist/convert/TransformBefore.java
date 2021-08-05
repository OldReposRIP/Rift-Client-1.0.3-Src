package javassist.convert;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;

public class TransformBefore extends TransformCall {

    protected CtClass[] nameeterTypes;
    protected int locals;
    protected int maxLocals;
    protected byte[] saveCode;
    protected byte[] loadCode;

    public TransformBefore(Transformer next, CtMethod origMethod, CtMethod beforeMethod) throws NotFoundException {
        super(next, origMethod, beforeMethod);
        this.methodDescriptor = origMethod.getMethodInfo2().getDescriptor();
        this.nameeterTypes = origMethod.getParameterTypes();
        this.locals = 0;
        this.maxLocals = 0;
        this.saveCode = this.loadCode = null;
    }

    public void initialize(ConstPool cp, CodeAttribute attr) {
        super.initialize(cp, attr);
        this.locals = 0;
        this.maxLocals = attr.getMaxLocals();
        this.saveCode = this.loadCode = null;
    }

    protected int match(int c, int pos, CodeIterator iterator, int typedesc, ConstPool cp) throws BadBytecode {
        if (this.newIndex == 0) {
            String desc = Descriptor.ofParameters(this.nameeterTypes) + 'V';

            desc = Descriptor.insertParameter(this.classname, desc);
            int nt = cp.addNameAndTypeInfo(this.newMethodname, desc);
            int ci = cp.addClassInfo(this.newClassname);

            this.newIndex = cp.addMethodrefInfo(ci, nt);
            this.constPool = cp;
        }

        if (this.saveCode == null) {
            this.makeCode(this.nameeterTypes, cp);
        }

        return this.match2(pos, iterator);
    }

    protected int match2(int pos, CodeIterator iterator) throws BadBytecode {
        iterator.move(pos);
        iterator.insert(this.saveCode);
        iterator.insert(this.loadCode);
        int p = iterator.insertGap(3);

        iterator.writeByte(184, p);
        iterator.write16bit(this.newIndex, p + 1);
        iterator.insert(this.loadCode);
        return iterator.next();
    }

    public int extraLocals() {
        return this.locals;
    }

    protected void makeCode(CtClass[] nameTypes, ConstPool cp) {
        Bytecode save = new Bytecode(cp, 0, 0);
        Bytecode load = new Bytecode(cp, 0, 0);
        int i = this.maxLocals;
        int len = nameTypes == null ? 0 : nameTypes.length;

        load.addAload(i);
        this.makeCode2(save, load, 0, len, nameTypes, i + 1);
        save.addAstore(i);
        this.saveCode = save.get();
        this.loadCode = load.get();
    }

    private void makeCode2(Bytecode save, Bytecode load, int i, int n, CtClass[] nameTypes, int i) {
        if (i < n) {
            int size = load.addLoad(i, nameTypes[i]);

            this.makeCode2(save, load, i + 1, n, nameTypes, i + size);
            save.addStore(i, nameTypes[i]);
        } else {
            this.locals = i - this.maxLocals;
        }

    }
}
