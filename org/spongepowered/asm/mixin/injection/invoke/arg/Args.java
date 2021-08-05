package org.spongepowered.asm.mixin.injection.invoke.arg;

public abstract class Args {

    protected final Object[] values;

    protected Args(Object[] values) {
        this.values = values;
    }

    public int size() {
        return this.values.length;
    }

    public Object get(int index) {
        return this.values[index];
    }

    public abstract void set(int i, Object object);

    public abstract void setAll(Object... aobject);
}
