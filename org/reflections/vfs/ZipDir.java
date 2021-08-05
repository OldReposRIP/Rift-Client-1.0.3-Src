package org.reflections.vfs;

import com.google.common.collect.AbstractIterator;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.reflections.Reflections;

public class ZipDir implements Vfs.Dir {

    final java.util.zip.ZipFile jarFile;

    public ZipDir(JarFile jarFile) {
        this.jarFile = jarFile;
    }

    public String getPath() {
        return this.jarFile.getName();
    }

    public Iterable getFiles() {
        return new Iterable() {
            public Iterator iterator() {
                return new AbstractIterator() {
                    final Enumeration entries;

                    {
                        this.entries = ZipDir.this.jarFile.entries();
                    }

                    protected Vfs.File computeNext() {
                        while (true) {
                            if (this.entries.hasMoreElements()) {
                                ZipEntry entry = (ZipEntry) this.entries.nextElement();

                                if (entry.isDirectory()) {
                                    continue;
                                }

                                return new ZipFile(ZipDir.this, entry);
                            }

                            return (Vfs.File) this.endOfData();
                        }
                    }
                };
            }
        };
    }

    public void close() {
        try {
            this.jarFile.close();
        } catch (IOException ioexception) {
            if (Reflections.log != null) {
                Reflections.log.warn("Could not close JarFile", ioexception);
            }
        }

    }

    public String toString() {
        return this.jarFile.getName();
    }
}
