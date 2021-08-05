package javassist;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;

public class SerialVersionUID {

    public static void setSerialVersionUID(CtClass clazz) throws CannotCompileException, NotFoundException {
        try {
            clazz.getDeclaredField("serialVersionUID");
        } catch (NotFoundException notfoundexception) {
            if (isSerializable(clazz)) {
                CtField field = new CtField(CtClass.longType, "serialVersionUID", clazz);

                field.setModifiers(26);
                clazz.addField(field, calculateDefault(clazz) + "L");
            }
        }
    }

    private static boolean isSerializable(CtClass clazz) throws NotFoundException {
        ClassPool pool = clazz.getClassPool();

        return clazz.subtypeOf(pool.get("java.io.Serializable"));
    }

    public static long calculateDefault(CtClass clazz) throws CannotCompileException {
        try {
            ByteArrayOutputStream e = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(e);
            ClassFile classFile = clazz.getClassFile();
            String javaName = javaName(clazz);

            out.writeUTF(javaName);
            CtMethod[] methods = clazz.getDeclaredMethods();
            int classMods = clazz.getModifiers();

            if ((classMods & 512) != 0) {
                if (methods.length > 0) {
                    classMods |= 1024;
                } else {
                    classMods &= -1025;
                }
            }

            out.writeInt(classMods);
            String[] interfaces = classFile.getInterfaces();

            int fields;

            for (fields = 0; fields < interfaces.length; ++fields) {
                interfaces[fields] = javaName(interfaces[fields]);
            }

            Arrays.sort(interfaces);

            for (fields = 0; fields < interfaces.length; ++fields) {
                out.writeUTF(interfaces[fields]);
            }

            CtField[] actfield = clazz.getDeclaredFields();

            Arrays.sort(actfield, new Comparator() {
                public int compare(Object o1, Object o2) {
                    CtField field1 = (CtField) o1;
                    CtField field2 = (CtField) o2;

                    return field1.getName().compareTo(field2.getName());
                }
            });

            for (int constructors = 0; constructors < actfield.length; ++constructors) {
                CtField digest = actfield[constructors];
                int digested = digest.getModifiers();

                if ((digested & 2) == 0 || (digested & 136) == 0) {
                    out.writeUTF(digest.getName());
                    out.writeInt(digested);
                    out.writeUTF(digest.getFieldInfo2().getDescriptor());
                }
            }

            if (classFile.getStaticInitializer() != null) {
                out.writeUTF("<clinit>");
                out.writeInt(8);
                out.writeUTF("()V");
            }

            CtConstructor[] actconstructor = clazz.getDeclaredConstructors();

            Arrays.sort(actconstructor, new Comparator() {
                public int compare(Object o1, Object o2) {
                    CtConstructor c1 = (CtConstructor) o1;
                    CtConstructor c2 = (CtConstructor) o2;

                    return c1.getMethodInfo2().getDescriptor().compareTo(c2.getMethodInfo2().getDescriptor());
                }
            });

            int hash;
            int i;

            for (i = 0; i < actconstructor.length; ++i) {
                CtConstructor ctconstructor = actconstructor[i];

                hash = ctconstructor.getModifiers();
                if ((hash & 2) == 0) {
                    out.writeUTF("<init>");
                    out.writeInt(hash);
                    out.writeUTF(ctconstructor.getMethodInfo2().getDescriptor().replace('/', '.'));
                }
            }

            Arrays.sort(methods, new Comparator() {
                public int compare(Object o1, Object o2) {
                    CtMethod m1 = (CtMethod) o1;
                    CtMethod m2 = (CtMethod) o2;
                    int value = m1.getName().compareTo(m2.getName());

                    if (value == 0) {
                        value = m1.getMethodInfo2().getDescriptor().compareTo(m2.getMethodInfo2().getDescriptor());
                    }

                    return value;
                }
            });

            for (i = 0; i < methods.length; ++i) {
                CtMethod ctmethod = methods[i];

                hash = ctmethod.getModifiers() & 3391;
                if ((hash & 2) == 0) {
                    out.writeUTF(ctmethod.getName());
                    out.writeInt(hash);
                    out.writeUTF(ctmethod.getMethodInfo2().getDescriptor().replace('/', '.'));
                }
            }

            out.flush();
            MessageDigest messagedigest = MessageDigest.getInstance("SHA");
            byte[] abyte = messagedigest.digest(e.toByteArray());
            long j = 0L;

            for (int i = Math.min(abyte.length, 8) - 1; i >= 0; --i) {
                j = j << 8 | (long) (abyte[i] & 255);
            }

            return j;
        } catch (IOException ioexception) {
            throw new CannotCompileException(ioexception);
        } catch (NoSuchAlgorithmException nosuchalgorithmexception) {
            throw new CannotCompileException(nosuchalgorithmexception);
        }
    }

    private static String javaName(CtClass clazz) {
        return Descriptor.toJavaName(Descriptor.toJvmName(clazz));
    }

    private static String javaName(String name) {
        return Descriptor.toJavaName(Descriptor.toJvmName(name));
    }
}
