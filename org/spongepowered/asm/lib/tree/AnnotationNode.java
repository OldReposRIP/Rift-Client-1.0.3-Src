package org.spongepowered.asm.lib.tree;

import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.lib.AnnotationVisitor;

public class AnnotationNode extends AnnotationVisitor {

    public String desc;
    public List values;

    public AnnotationNode(String desc) {
        this(327680, desc);
        if (this.getClass() != AnnotationNode.class) {
            throw new IllegalStateException();
        }
    }

    public AnnotationNode(int api, String desc) {
        super(api);
        this.desc = desc;
    }

    AnnotationNode(List values) {
        super(327680);
        this.values = values;
    }

    public void visit(String name, Object value) {
        if (this.values == null) {
            this.values = new ArrayList(this.desc != null ? 2 : 1);
        }

        if (this.desc != null) {
            this.values.add(name);
        }

        ArrayList l;
        int i;
        int j;

        if (value instanceof byte[]) {
            byte[] v = (byte[]) ((byte[]) value);

            l = new ArrayList(v.length);
            byte[] abyte = v;

            i = v.length;

            for (j = 0; j < i; ++j) {
                byte f = abyte[j];

                l.add(Byte.valueOf(f));
            }

            this.values.add(l);
        } else if (value instanceof boolean[]) {
            boolean[] aboolean = (boolean[]) ((boolean[]) value);

            l = new ArrayList(aboolean.length);
            boolean[] aboolean1 = aboolean;

            i = aboolean.length;

            for (j = 0; j < i; ++j) {
                boolean flag = aboolean1[j];

                l.add(Boolean.valueOf(flag));
            }

            this.values.add(l);
        } else if (value instanceof short[]) {
            short[] ashort = (short[]) ((short[]) value);

            l = new ArrayList(ashort.length);
            short[] ashort1 = ashort;

            i = ashort.length;

            for (j = 0; j < i; ++j) {
                short short0 = ashort1[j];

                l.add(Short.valueOf(short0));
            }

            this.values.add(l);
        } else if (value instanceof char[]) {
            char[] achar = (char[]) ((char[]) value);

            l = new ArrayList(achar.length);
            char[] achar1 = achar;

            i = achar.length;

            for (j = 0; j < i; ++j) {
                char c0 = achar1[j];

                l.add(Character.valueOf(c0));
            }

            this.values.add(l);
        } else if (value instanceof int[]) {
            int[] aint = (int[]) ((int[]) value);

            l = new ArrayList(aint.length);
            int[] aint1 = aint;

            i = aint.length;

            for (j = 0; j < i; ++j) {
                int k = aint1[j];

                l.add(Integer.valueOf(k));
            }

            this.values.add(l);
        } else if (value instanceof long[]) {
            long[] along = (long[]) ((long[]) value);

            l = new ArrayList(along.length);
            long[] along1 = along;

            i = along.length;

            for (j = 0; j < i; ++j) {
                long d = along1[j];

                l.add(Long.valueOf(d));
            }

            this.values.add(l);
        } else if (value instanceof float[]) {
            float[] afloat = (float[]) ((float[]) value);

            l = new ArrayList(afloat.length);
            float[] afloat1 = afloat;

            i = afloat.length;

            for (j = 0; j < i; ++j) {
                float f = afloat1[j];

                l.add(Float.valueOf(f));
            }

            this.values.add(l);
        } else if (value instanceof double[]) {
            double[] adouble = (double[]) ((double[]) value);

            l = new ArrayList(adouble.length);
            double[] adouble1 = adouble;

            i = adouble.length;

            for (j = 0; j < i; ++j) {
                double d0 = adouble1[j];

                l.add(Double.valueOf(d0));
            }

            this.values.add(l);
        } else {
            this.values.add(value);
        }

    }

    public void visitEnum(String name, String desc, String value) {
        if (this.values == null) {
            this.values = new ArrayList(this.desc != null ? 2 : 1);
        }

        if (this.desc != null) {
            this.values.add(name);
        }

        this.values.add(new String[] { desc, value});
    }

    public AnnotationVisitor visitAnnotation(String name, String desc) {
        if (this.values == null) {
            this.values = new ArrayList(this.desc != null ? 2 : 1);
        }

        if (this.desc != null) {
            this.values.add(name);
        }

        AnnotationNode annotation = new AnnotationNode(desc);

        this.values.add(annotation);
        return annotation;
    }

    public AnnotationVisitor visitArray(String name) {
        if (this.values == null) {
            this.values = new ArrayList(this.desc != null ? 2 : 1);
        }

        if (this.desc != null) {
            this.values.add(name);
        }

        ArrayList array = new ArrayList();

        this.values.add(array);
        return new AnnotationNode(array);
    }

    public void visitEnd() {}

    public void check(int api) {}

    public void accept(AnnotationVisitor av) {
        if (av != null) {
            if (this.values != null) {
                for (int i = 0; i < this.values.size(); i += 2) {
                    String name = (String) this.values.get(i);
                    Object value = this.values.get(i + 1);

                    accept(av, name, value);
                }
            }

            av.visitEnd();
        }

    }

    static void accept(AnnotationVisitor av, String name, Object value) {
        if (av != null) {
            if (value instanceof String[]) {
                String[] v = (String[]) ((String[]) value);

                av.visitEnum(name, v[0], v[1]);
            } else if (value instanceof AnnotationNode) {
                AnnotationNode annotationnode = (AnnotationNode) value;

                annotationnode.accept(av.visitAnnotation(name, annotationnode.desc));
            } else if (value instanceof List) {
                AnnotationVisitor annotationvisitor = av.visitArray(name);

                if (annotationvisitor != null) {
                    List array = (List) value;

                    for (int j = 0; j < array.size(); ++j) {
                        accept(annotationvisitor, (String) null, array.get(j));
                    }

                    annotationvisitor.visitEnd();
                }
            } else {
                av.visit(name, value);
            }
        }

    }
}
