package javassist.util.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class ProxyObjectInputStream extends ObjectInputStream {

    private ClassLoader loader = Thread.currentThread().getContextClassLoader();

    public ProxyObjectInputStream(InputStream in) throws IOException {
        super(in);
        if (this.loader == null) {
            this.loader = ClassLoader.getSystemClassLoader();
        }

    }

    public void setClassLoader(ClassLoader loader) {
        if (loader != null) {
            this.loader = loader;
        } else {
            loader = ClassLoader.getSystemClassLoader();
        }

    }

    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        boolean isProxy = this.readBoolean();

        if (!isProxy) {
            return super.readClassDescriptor();
        } else {
            String name = (String) this.readObject();
            Class superClass = this.loader.loadClass(name);
            int length = this.readInt();
            Class[] interfaces = new Class[length];

            for (int signature = 0; signature < length; ++signature) {
                name = (String) this.readObject();
                interfaces[signature] = this.loader.loadClass(name);
            }

            length = this.readInt();
            byte[] abyte = new byte[length];

            this.read(abyte);
            ProxyFactory factory = new ProxyFactory();

            factory.setUseCache(true);
            factory.setUseWriteReplace(false);
            factory.setSuperclass(superClass);
            factory.setInterfaces(interfaces);
            Class proxyClass = factory.createClass(abyte);

            return ObjectStreamClass.lookup(proxyClass);
        }
    }
}
