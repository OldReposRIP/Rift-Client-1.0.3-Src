package javassist.bytecode.analysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javassist.CtClass;

public class MultiType extends Type {

    private Map interfaces;
    private Type resolved;
    private Type potentialClass;
    private MultiType mergeSource;
    private boolean changed;

    public MultiType(Map interfaces) {
        this(interfaces, (Type) null);
    }

    public MultiType(Map interfaces, Type potentialClass) {
        super((CtClass) null);
        this.changed = false;
        this.interfaces = interfaces;
        this.potentialClass = potentialClass;
    }

    public CtClass getCtClass() {
        return this.resolved != null ? this.resolved.getCtClass() : Type.OBJECT.getCtClass();
    }

    public Type getComponent() {
        return null;
    }

    public int getSize() {
        return 1;
    }

    public boolean isArray() {
        return false;
    }

    boolean popChanged() {
        boolean changed = this.changed;

        this.changed = false;
        return changed;
    }

    public boolean isAssignableFrom(Type type) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isAssignableTo(Type type) {
        if (this.resolved != null) {
            return type.isAssignableFrom(this.resolved);
        } else if (Type.OBJECT.equals(type)) {
            return true;
        } else {
            if (this.potentialClass != null && !type.isAssignableFrom(this.potentialClass)) {
                this.potentialClass = null;
            }

            Map map = this.mergeMultiAndSingle(this, type);

            if (map.size() == 1 && this.potentialClass == null) {
                this.resolved = Type.get((CtClass) map.values().iterator().next());
                this.propogateResolved();
                return true;
            } else if (map.size() >= 1) {
                this.interfaces = map;
                this.propogateState();
                return true;
            } else if (this.potentialClass != null) {
                this.resolved = this.potentialClass;
                this.propogateResolved();
                return true;
            } else {
                return false;
            }
        }
    }

    private void propogateState() {
        for (MultiType source = this.mergeSource; source != null; source = source.mergeSource) {
            source.interfaces = this.interfaces;
            source.potentialClass = this.potentialClass;
        }

    }

    private void propogateResolved() {
        for (MultiType source = this.mergeSource; source != null; source = source.mergeSource) {
            source.resolved = this.resolved;
        }

    }

    public boolean isReference() {
        return true;
    }

    private Map getAllMultiInterfaces(MultiType type) {
        HashMap map = new HashMap();
        Iterator iter = type.interfaces.values().iterator();

        while (iter.hasNext()) {
            CtClass intf = (CtClass) iter.next();

            map.put(intf.getName(), intf);
            this.getAllInterfaces(intf, map);
        }

        return map;
    }

    private Map mergeMultiInterfaces(MultiType type1, MultiType type2) {
        Map map1 = this.getAllMultiInterfaces(type1);
        Map map2 = this.getAllMultiInterfaces(type2);

        return this.findCommonInterfaces(map1, map2);
    }

    private Map mergeMultiAndSingle(MultiType multi, Type single) {
        Map map1 = this.getAllMultiInterfaces(multi);
        Map map2 = this.getAllInterfaces(single.getCtClass(), (Map) null);

        return this.findCommonInterfaces(map1, map2);
    }

    private boolean inMergeSource(MultiType source) {
        while (source != null) {
            if (source == this) {
                return true;
            }

            source = source.mergeSource;
        }

        return false;
    }

    public Type merge(Type type) {
        if (this == type) {
            return this;
        } else if (type == MultiType.UNINIT) {
            return this;
        } else if (type == MultiType.BOGUS) {
            return MultiType.BOGUS;
        } else if (type == null) {
            return this;
        } else if (this.resolved != null) {
            return this.resolved.merge(type);
        } else {
            if (this.potentialClass != null) {
                Type merged = this.potentialClass.merge(type);

                if (!merged.equals(this.potentialClass) || merged.popChanged()) {
                    this.potentialClass = Type.OBJECT.equals(merged) ? null : merged;
                    this.changed = true;
                }
            }

            Map merged1;

            if (type instanceof MultiType) {
                MultiType iter = (MultiType) type;

                if (iter.resolved != null) {
                    merged1 = this.mergeMultiAndSingle(this, iter.resolved);
                } else {
                    merged1 = this.mergeMultiInterfaces(iter, this);
                    if (!this.inMergeSource(iter)) {
                        this.mergeSource = iter;
                    }
                }
            } else {
                merged1 = this.mergeMultiAndSingle(this, type);
            }

            if (merged1.size() <= 1 && (merged1.size() != 1 || this.potentialClass == null)) {
                if (merged1.size() == 1) {
                    this.resolved = Type.get((CtClass) merged1.values().iterator().next());
                } else if (this.potentialClass != null) {
                    this.resolved = this.potentialClass;
                } else {
                    this.resolved = MultiType.OBJECT;
                }

                this.propogateResolved();
                return this.resolved;
            } else {
                if (merged1.size() != this.interfaces.size()) {
                    this.changed = true;
                } else if (!this.changed) {
                    Iterator iter1 = merged1.keySet().iterator();

                    while (iter1.hasNext()) {
                        if (!this.interfaces.containsKey(iter1.next())) {
                            this.changed = true;
                        }
                    }
                }

                this.interfaces = merged1;
                this.propogateState();
                return this;
            }
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof MultiType)) {
            return false;
        } else {
            MultiType multi = (MultiType) o;

            return this.resolved != null ? this.resolved.equals(multi.resolved) : (multi.resolved != null ? false : this.interfaces.keySet().equals(multi.interfaces.keySet()));
        }
    }

    public String toString() {
        if (this.resolved != null) {
            return this.resolved.toString();
        } else {
            StringBuffer buffer = new StringBuffer("{");
            Iterator iter = this.interfaces.keySet().iterator();

            while (iter.hasNext()) {
                buffer.append(iter.next());
                buffer.append(", ");
            }

            buffer.setLength(buffer.length() - 2);
            if (this.potentialClass != null) {
                buffer.append(", *").append(this.potentialClass.toString());
            }

            buffer.append("}");
            return buffer.toString();
        }
    }
}
