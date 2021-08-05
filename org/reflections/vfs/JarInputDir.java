package org.reflections.vfs;

import com.google.common.collect.AbstractIterator;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.reflections.ReflectionsException;
import org.reflections.util.Utils;

public class JarInputDir implements Vfs.Dir {

    private final URL url;
    JarInputStream jarInputStream;
    long cursor = 0L;
    long nextCursor = 0L;

    public JarInputDir(URL url) {
        this.url = url;
    }

    public String getPath() {
        return this.url.getPath();
    }

    public Iterable getFiles() {
        return new Iterable() {
            public Iterator iterator() {
                return new AbstractIterator() {
                    {
                        try {
                            JarInputDir.this.jarInputStream = new JarInputStream(JarInputDir.this.url.openConnection().getInputStream());
                        } catch (Exception exception) {
                            throw new ReflectionsException("Could not open url connection", exception);
                        }
                    }

                    protected Vfs.File computeNext() {
                        while (true) {
                            try {
                                JarEntry e = JarInputDir.this.jarInputStream.getNextJarEntry();

                                if (e == null) {
                                    return (Vfs.File) this.endOfData();
                                }

                                long size = e.getSize();

                                if (size < 0L) {
                                    size += 4294967295L;
                                }

                                JarInputDir.this.nextCursor += size;
                                if (!e.isDirectory()) {
                                    return new JarInputFile(e, JarInputDir.this, JarInputDir.this.cursor, JarInputDir.this.nextCursor);
                                }
                            } catch (IOException ioexception) {
                                throw new ReflectionsException("could not get next zip entry", ioexception);
                            }
                        }
                    }
                };
            }
        };
    }

    public void close() {
        Utils.close(this.jarInputStream);
    }
}
