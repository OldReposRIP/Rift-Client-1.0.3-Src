package javassist;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class JarClassPath implements ClassPath {

    JarFile jarfile;
    String jarfileURL;

    JarClassPath(String pathname) throws NotFoundException {
        try {
            this.jarfile = new JarFile(pathname);
            this.jarfileURL = (new File(pathname)).getCanonicalFile().toURI().toURL().toString();
        } catch (IOException ioexception) {
            throw new NotFoundException(pathname);
        }
    }

    public InputStream openClassfile(String classname) throws NotFoundException {
        try {
            String jarname = classname.replace('.', '/') + ".class";
            JarEntry je = this.jarfile.getJarEntry(jarname);

            return je != null ? this.jarfile.getInputStream(je) : null;
        } catch (IOException ioexception) {
            throw new NotFoundException("broken jar file?: " + this.jarfile.getName());
        }
    }

    public URL find(String classname) {
        String jarname = classname.replace('.', '/') + ".class";
        JarEntry je = this.jarfile.getJarEntry(jarname);

        if (je != null) {
            try {
                return new URL("jar:" + this.jarfileURL + "!/" + jarname);
            } catch (MalformedURLException malformedurlexception) {
                ;
            }
        }

        return null;
    }

    public void close() {
        try {
            this.jarfile.close();
            this.jarfile = null;
        } catch (IOException ioexception) {
            ;
        }

    }

    public String toString() {
        return this.jarfile == null ? "<null>" : this.jarfile.toString();
    }
}
