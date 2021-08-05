package javassist.tools.rmi;

import java.applet.Applet;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.net.URL;

public class ObjectImporter implements Serializable {

    private final byte[] endofline = new byte[] { (byte) 13, (byte) 10};
    private String servername;
    private String orgServername;
    private int port;
    private int orgPort;
    protected byte[] lookupCommand = "POST /lookup HTTP/1.0".getBytes();
    protected byte[] rmiCommand = "POST /rmi HTTP/1.0".getBytes();
    private static final Class[] proxyConstructorParamTypes = new Class[] { ObjectImporter.class, Integer.TYPE};

    public ObjectImporter(Applet applet) {
        URL codebase = applet.getCodeBase();

        this.orgServername = this.servername = codebase.getHost();
        this.orgPort = this.port = codebase.getPort();
    }

    public ObjectImporter(String servername, int port) {
        this.orgServername = this.servername = servername;
        this.orgPort = this.port = port;
    }

    public Object getObject(String name) {
        try {
            return this.lookupObject(name);
        } catch (ObjectNotFoundException objectnotfoundexception) {
            return null;
        }
    }

    public void setHttpProxy(String host, int port) {
        String proxyHeader = "POST http://" + this.orgServername + ":" + this.orgPort;
        String cmd = proxyHeader + "/lookup HTTP/1.0";

        this.lookupCommand = cmd.getBytes();
        cmd = proxyHeader + "/rmi HTTP/1.0";
        this.rmiCommand = cmd.getBytes();
        this.servername = host;
        this.port = port;
    }

    public Object lookupObject(String name) throws ObjectNotFoundException {
        try {
            Socket e = new Socket(this.servername, this.port);
            OutputStream out = e.getOutputStream();

            out.write(this.lookupCommand);
            out.write(this.endofline);
            out.write(this.endofline);
            ObjectOutputStream dout = new ObjectOutputStream(out);

            dout.writeUTF(name);
            dout.flush();
            BufferedInputStream in = new BufferedInputStream(e.getInputStream());

            this.skipHeader(in);
            ObjectInputStream din = new ObjectInputStream(in);
            int n = din.readInt();
            String classname = din.readUTF();

            din.close();
            dout.close();
            e.close();
            if (n >= 0) {
                return this.createProxy(n, classname);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ObjectNotFoundException(name, exception);
        }

        throw new ObjectNotFoundException(name);
    }

    private Object createProxy(int oid, String classname) throws Exception {
        Class c = Class.forName(classname);
        Constructor cons = c.getConstructor(ObjectImporter.proxyConstructorParamTypes);

        return cons.newInstance(new Object[] { this, new Integer(oid)});
    }

    public Object call(int objectid, int methodid, Object[] args) throws RemoteException {
        boolean result;
        Object rvalue;
        String errmsg;

        try {
            Socket e = new Socket(this.servername, this.port);
            BufferedOutputStream out = new BufferedOutputStream(e.getOutputStream());

            out.write(this.rmiCommand);
            out.write(this.endofline);
            out.write(this.endofline);
            ObjectOutputStream dout = new ObjectOutputStream(out);

            dout.writeInt(objectid);
            dout.writeInt(methodid);
            this.writeParameters(dout, args);
            dout.flush();
            BufferedInputStream ins = new BufferedInputStream(e.getInputStream());

            this.skipHeader(ins);
            ObjectInputStream din = new ObjectInputStream(ins);

            result = din.readBoolean();
            rvalue = null;
            errmsg = null;
            if (result) {
                rvalue = din.readObject();
            } else {
                errmsg = din.readUTF();
            }

            din.close();
            dout.close();
            e.close();
            if (rvalue instanceof RemoteRef) {
                RemoteRef ref = (RemoteRef) rvalue;

                rvalue = this.createProxy(ref.oid, ref.classname);
            }
        } catch (ClassNotFoundException classnotfoundexception) {
            throw new RemoteException(classnotfoundexception);
        } catch (IOException ioexception) {
            throw new RemoteException(ioexception);
        } catch (Exception exception) {
            throw new RemoteException(exception);
        }

        if (result) {
            return rvalue;
        } else {
            throw new RemoteException(errmsg);
        }
    }

    private void skipHeader(InputStream in) throws IOException {
        int len;

        do {
            int c;

            for (len = 0; (c = in.read()) >= 0 && c != 13; ++len) {
                ;
            }

            in.read();
        } while (len > 0);

    }

    private void writeParameters(ObjectOutputStream dout, Object[] names) throws IOException {
        int n = names.length;

        dout.writeInt(n);

        for (int i = 0; i < n; ++i) {
            if (names[i] instanceof Proxy) {
                Proxy p = (Proxy) names[i];

                dout.writeObject(new RemoteRef(p._getObjectId()));
            } else {
                dout.writeObject(names[i]);
            }
        }

    }
}
