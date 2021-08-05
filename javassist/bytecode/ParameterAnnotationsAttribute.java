package javassist.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationsWriter;

public class ParameterAnnotationsAttribute extends AttributeInfo {

    public static final String visibleTag = "RuntimeVisibleParameterAnnotations";
    public static final String invisibleTag = "RuntimeInvisibleParameterAnnotations";

    public ParameterAnnotationsAttribute(ConstPool cp, String attrname, byte[] info) {
        super(cp, attrname, info);
    }

    public ParameterAnnotationsAttribute(ConstPool cp, String attrname) {
        this(cp, attrname, new byte[] { (byte) 0});
    }

    ParameterAnnotationsAttribute(ConstPool cp, int n, DataInputStream in) throws IOException {
        super(cp, n, in);
    }

    public int numParameters() {
        return this.info[0] & 255;
    }

    public AttributeInfo copy(ConstPool newCp, Map classnames) {
        AnnotationsAttribute.Copier copier = new AnnotationsAttribute.Copier(this.info, this.constPool, newCp, classnames);

        try {
            copier.nameeters();
            return new ParameterAnnotationsAttribute(newCp, this.getName(), copier.close());
        } catch (Exception exception) {
            throw new RuntimeException(exception.toString());
        }
    }

    public Annotation[][] getAnnotations() {
        try {
            return (new AnnotationsAttribute.Parser(this.info, this.constPool)).parseParameters();
        } catch (Exception exception) {
            throw new RuntimeException(exception.toString());
        }
    }

    public void setAnnotations(Annotation[][] names) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        AnnotationsWriter writer = new AnnotationsWriter(output, this.constPool);

        try {
            int e = names.length;

            writer.numParameters(e);
            int i = 0;

            while (true) {
                if (i >= e) {
                    writer.close();
                    break;
                }

                Annotation[] anno = names[i];

                writer.numAnnotations(anno.length);

                for (int j = 0; j < anno.length; ++j) {
                    anno[j].write(writer);
                }

                ++i;
            }
        } catch (IOException ioexception) {
            throw new RuntimeException(ioexception);
        }

        this.set(output.toByteArray());
    }

    void renameClass(String oldname, String newname) {
        HashMap map = new HashMap();

        map.put(oldname, newname);
        this.renameClass(map);
    }

    void renameClass(Map classnames) {
        AnnotationsAttribute.Renamer renamer = new AnnotationsAttribute.Renamer(this.info, this.getConstPool(), classnames);

        try {
            renamer.nameeters();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    void getRefClasses(Map classnames) {
        this.renameClass(classnames);
    }

    public String toString() {
        Annotation[][] aa = this.getAnnotations();
        StringBuilder sbuf = new StringBuilder();
        int k = 0;

        while (k < aa.length) {
            Annotation[] a = aa[k++];
            int i = 0;

            while (i < a.length) {
                sbuf.append(a[i++].toString());
                if (i != a.length) {
                    sbuf.append(" ");
                }
            }

            if (k != aa.length) {
                sbuf.append(", ");
            }
        }

        return sbuf.toString();
    }
}
