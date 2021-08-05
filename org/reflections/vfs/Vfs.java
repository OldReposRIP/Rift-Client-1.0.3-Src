package org.reflections.vfs;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import javax.annotation.Nullable;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.Utils;

public abstract class Vfs {

    private static List defaultUrlTypes = Lists.newArrayList(Vfs.DefaultUrlTypes.values());

    public static List getDefaultUrlTypes() {
        return Vfs.defaultUrlTypes;
    }

    public static void setDefaultURLTypes(List urlTypes) {
        Vfs.defaultUrlTypes = urlTypes;
    }

    public static void addDefaultURLTypes(Vfs.UrlType urlType) {
        Vfs.defaultUrlTypes.add(0, urlType);
    }

    public static Vfs.Dir fromURL(URL url) {
        return fromURL(url, Vfs.defaultUrlTypes);
    }

    public static Vfs.Dir fromURL(URL url, List urlTypes) {
        Iterator iterator = urlTypes.iterator();

        while (iterator.hasNext()) {
            Vfs.UrlType type = (Vfs.UrlType) iterator.next();

            try {
                if (type.matches(url)) {
                    Vfs.Dir e = type.createDir(url);

                    if (e != null) {
                        return e;
                    }
                }
            } catch (Throwable throwable) {
                if (Reflections.log != null) {
                    Reflections.log.warn("could not create Dir using " + type + " from url " + url.toExternalForm() + ". skipping.", throwable);
                }
            }
        }

        throw new ReflectionsException("could not create Vfs.Dir from url, no matching UrlType was found [" + url.toExternalForm() + "]\neither use fromURL(final URL url, final List<UrlType> urlTypes) or use the static setDefaultURLTypes(final List<UrlType> urlTypes) or addDefaultURLTypes(UrlType urlType) with your specialized UrlType.");
    }

    public static Vfs.Dir fromURL(URL url, Vfs.UrlType... urlTypes) {
        return fromURL(url, (List) Lists.newArrayList(urlTypes));
    }

    public static Iterable findFiles(Collection inUrls, final String packagePrefix, final Predicate nameFilter) {
        Predicate fileNamePredicate = new Predicate() {
            public boolean apply(Vfs.File file) {
                String path = file.getRelativePath();

                if (!path.startsWith(packagePrefix)) {
                    return false;
                } else {
                    String filename = path.substring(path.indexOf(packagePrefix) + packagePrefix.length());

                    return !Utils.isEmpty(filename) && nameFilter.apply(filename.substring(1));
                }
            }
        };

        return findFiles(inUrls, fileNamePredicate);
    }

    public static Iterable findFiles(Collection inUrls, Predicate filePredicate) {
        Object result = new ArrayList();
        Iterator iterator = inUrls.iterator();

        while (iterator.hasNext()) {
            final URL url = (URL) iterator.next();

            try {
                result = Iterables.concat((Iterable) result, Iterables.filter(new Iterable() {
                    public Iterator iterator() {
                        return Vfs.fromURL(url).getFiles().iterator();
                    }
                }, filePredicate));
            } catch (Throwable throwable) {
                if (Reflections.log != null) {
                    Reflections.log.error("could not findFiles for url. continuing. [" + url + "]", throwable);
                }
            }
        }

        return (Iterable) result;
    }

    @Nullable
    public static java.io.File getFile(URL url) {
        java.io.File file;
        String path;

        try {
            path = url.toURI().getSchemeSpecificPart();
            if ((file = new java.io.File(path)).exists()) {
                return file;
            }
        } catch (URISyntaxException urisyntaxexception) {
            ;
        }

        try {
            path = URLDecoder.decode(url.getPath(), "UTF-8");
            if (path.contains(".jar!")) {
                path = path.substring(0, path.lastIndexOf(".jar!") + ".jar".length());
            }

            if ((file = new java.io.File(path)).exists()) {
                return file;
            }
        } catch (UnsupportedEncodingException unsupportedencodingexception) {
            ;
        }

        try {
            path = url.toExternalForm();
            if (path.startsWith("jar:")) {
                path = path.substring("jar:".length());
            }

            if (path.startsWith("wsjar:")) {
                path = path.substring("wsjar:".length());
            }

            if (path.startsWith("file:")) {
                path = path.substring("file:".length());
            }

            if (path.contains(".jar!")) {
                path = path.substring(0, path.indexOf(".jar!") + ".jar".length());
            }

            if ((file = new java.io.File(path)).exists()) {
                return file;
            }

            path = path.replace("%20", " ");
            if ((file = new java.io.File(path)).exists()) {
                return file;
            }
        } catch (Exception exception) {
            ;
        }

        return null;
    }

    private static boolean hasJarFileInPath(URL url) {
        return url.toExternalForm().matches(".*\\.jar(\\!.*|$)");
    }

    public static enum DefaultUrlTypes implements Vfs.UrlType {

        jarFile {;
            public boolean matches(URL url) {
                return url.getProtocol().equals("file") && Vfs.hasJarFileInPath(url);
            }

            public Vfs.Dir createDir(URL url) throws Exception {
                return new ZipDir(new JarFile(Vfs.getFile(url)));
            }
        }, jarUrl {;
    public boolean matches(URL url) {
        return "jar".equals(url.getProtocol()) || "zip".equals(url.getProtocol()) || "wsjar".equals(url.getProtocol());
    }

    public Vfs.Dir createDir(URL url) throws Exception {
        try {
            URLConnection file = url.openConnection();

            if (file instanceof JarURLConnection) {
                return new ZipDir(((JarURLConnection) file).getJarFile());
            }
        } catch (Throwable throwable) {
            ;
        }

        java.io.File file1 = Vfs.getFile(url);

        return file1 != null ? new ZipDir(new JarFile(file1)) : null;
    }
}, directory {;
    public boolean matches(URL url) {
        if (url.getProtocol().equals("file") && !Vfs.hasJarFileInPath(url)) {
            java.io.File file = Vfs.getFile(url);

            return file != null && file.isDirectory();
        } else {
            return false;
        }
    }

    public Vfs.Dir createDir(URL url) throws Exception {
        return new SystemDir(Vfs.getFile(url));
    }
}, jboss_vfs {;
    public boolean matches(URL url) {
        return url.getProtocol().equals("vfs");
    }

    public Vfs.Dir createDir(URL url) throws Exception {
        Object content = url.openConnection().getContent();
        Class virtualFile = ClasspathHelper.contextClassLoader().loadClass("org.jboss.vfs.VirtualFile");
        java.io.File physicalFile = (java.io.File) virtualFile.getMethod("getPhysicalFile", new Class[0]).invoke(content, new Object[0]);
        String name = (String) virtualFile.getMethod("getName", new Class[0]).invoke(content, new Object[0]);
        java.io.File file = new java.io.File(physicalFile.getParentFile(), name);

        if (!file.exists() || !file.canRead()) {
            file = physicalFile;
        }

        return (Vfs.Dir) (file.isDirectory() ? new SystemDir(file) : new ZipDir(new JarFile(file)));
    }
}, jboss_vfsfile {;
    public boolean matches(URL url) throws Exception {
        return "vfszip".equals(url.getProtocol()) || "vfsfile".equals(url.getProtocol());
    }

    public Vfs.Dir createDir(URL url) throws Exception {
        return (new UrlTypeVFS()).createDir(url);
    }
}, bundle {;
    public boolean matches(URL url) throws Exception {
        return url.getProtocol().startsWith("bundle");
    }

    public Vfs.Dir createDir(URL url) throws Exception {
        return Vfs.fromURL((URL) ClasspathHelper.contextClassLoader().loadClass("org.eclipse.core.runtime.FileLocator").getMethod("resolve", new Class[] { URL.class}).invoke((Object) null, new Object[] { url}));
    }
}, jarInputStream {;
    public boolean matches(URL url) throws Exception {
        return url.toExternalForm().contains(".jar");
    }

    public Vfs.Dir createDir(URL url) throws Exception {
        return new JarInputDir(url);
    }
};

        private DefaultUrlTypes() {}

        DefaultUrlTypes(Object x2) {
            this();
        }
    }

    public interface UrlType {

        boolean matches(URL url) throws Exception;

        Vfs.Dir createDir(URL url) throws Exception;
    }

    public interface File {

        String getName();

        String getRelativePath();

        InputStream openInputStream() throws IOException;
    }

    public interface Dir {

        String getPath();

        Iterable getFiles();

        void close();
    }
}
