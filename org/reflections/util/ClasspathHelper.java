package org.reflections.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import javax.servlet.ServletContext;
import org.reflections.Reflections;

public abstract class ClasspathHelper {

    public static ClassLoader contextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static ClassLoader staticClassLoader() {
        return Reflections.class.getClassLoader();
    }

    public static ClassLoader[] classLoaders(ClassLoader... classLoaders) {
        if (classLoaders != null && classLoaders.length != 0) {
            return classLoaders;
        } else {
            ClassLoader contextClassLoader = contextClassLoader();
            ClassLoader staticClassLoader = staticClassLoader();

            return contextClassLoader != null ? (staticClassLoader != null && contextClassLoader != staticClassLoader ? new ClassLoader[] { contextClassLoader, staticClassLoader} : new ClassLoader[] { contextClassLoader}) : new ClassLoader[0];
        }
    }

    public static Collection forPackage(String name, ClassLoader... classLoaders) {
        return forResource(resourceName(name), classLoaders);
    }

    public static Collection forResource(String resourceName, ClassLoader... classLoaders) {
        ArrayList result = new ArrayList();
        ClassLoader[] loaders = classLoaders(classLoaders);
        ClassLoader[] aclassloader = loaders;
        int i = loaders.length;

        for (int j = 0; j < i; ++j) {
            ClassLoader classLoader = aclassloader[j];

            try {
                Enumeration e = classLoader.getResources(resourceName);

                while (e.hasMoreElements()) {
                    URL url = (URL) e.nextElement();
                    int index = url.toExternalForm().lastIndexOf(resourceName);

                    if (index != -1) {
                        result.add(new URL(url, url.toExternalForm().substring(0, index)));
                    } else {
                        result.add(url);
                    }
                }
            } catch (IOException ioexception) {
                if (Reflections.log != null) {
                    Reflections.log.error("error getting resources for " + resourceName, ioexception);
                }
            }
        }

        return distinctUrls(result);
    }

    public static URL forClass(Class aClass, ClassLoader... classLoaders) {
        ClassLoader[] loaders = classLoaders(classLoaders);
        String resourceName = aClass.getName().replace(".", "/") + ".class";
        ClassLoader[] aclassloader = loaders;
        int i = loaders.length;

        for (int j = 0; j < i; ++j) {
            ClassLoader classLoader = aclassloader[j];

            try {
                URL e = classLoader.getResource(resourceName);

                if (e != null) {
                    String normalizedUrl = e.toExternalForm().substring(0, e.toExternalForm().lastIndexOf(aClass.getPackage().getName().replace(".", "/")));

                    return new URL(normalizedUrl);
                }
            } catch (MalformedURLException malformedurlexception) {
                if (Reflections.log != null) {
                    Reflections.log.warn("Could not get URL", malformedurlexception);
                }
            }
        }

        return null;
    }

    public static Collection forClassLoader() {
        return forClassLoader(classLoaders(new ClassLoader[0]));
    }

    public static Collection forClassLoader(ClassLoader... classLoaders) {
        ArrayList result = new ArrayList();
        ClassLoader[] loaders = classLoaders(classLoaders);
        ClassLoader[] aclassloader = loaders;
        int i = loaders.length;

        for (int j = 0; j < i; ++j) {
            for (ClassLoader classLoader = aclassloader[j]; classLoader != null; classLoader = classLoader.getParent()) {
                if (classLoader instanceof URLClassLoader) {
                    URL[] urls = ((URLClassLoader) classLoader).getURLs();

                    if (urls != null) {
                        result.addAll(Arrays.asList(urls));
                    }
                }
            }
        }

        return distinctUrls(result);
    }

    public static Collection forJavaClassPath() {
        ArrayList urls = new ArrayList();
        String javaClassPath = System.getProperty("java.class.path");

        if (javaClassPath != null) {
            String[] astring = javaClassPath.split(File.pathSeparator);
            int i = astring.length;

            for (int j = 0; j < i; ++j) {
                String path = astring[j];

                try {
                    urls.add((new File(path)).toURI().toURL());
                } catch (Exception exception) {
                    if (Reflections.log != null) {
                        Reflections.log.warn("Could not get URL", exception);
                    }
                }
            }
        }

        return distinctUrls(urls);
    }

    public static Collection forWebInfLib(ServletContext servletContext) {
        ArrayList urls = new ArrayList();
        Set resourcePaths = servletContext.getResourcePaths("/WEB-INF/lib");

        if (resourcePaths == null) {
            return urls;
        } else {
            Iterator iterator = resourcePaths.iterator();

            while (iterator.hasNext()) {
                Object urlString = iterator.next();

                try {
                    urls.add(servletContext.getResource((String) urlString));
                } catch (MalformedURLException malformedurlexception) {
                    ;
                }
            }

            return distinctUrls(urls);
        }
    }

    public static URL forWebInfClasses(ServletContext servletContext) {
        try {
            String path = servletContext.getRealPath("/WEB-INF/classes");

            if (path == null) {
                return servletContext.getResource("/WEB-INF/classes");
            }

            File file = new File(path);

            if (file.exists()) {
                return file.toURL();
            }
        } catch (MalformedURLException malformedurlexception) {
            ;
        }

        return null;
    }

    public static Collection forManifest() {
        return forManifest((Iterable) forClassLoader());
    }

    public static Collection forManifest(URL url) {
        ArrayList result = new ArrayList();

        result.add(url);

        try {
            String part = cleanPath(url);
            File jarFile = new File(part);
            JarFile myJar = new JarFile(part);
            URL validUrl = tryToGetValidUrl(jarFile.getPath(), (new File(part)).getParent(), part);

            if (validUrl != null) {
                result.add(validUrl);
            }

            Manifest manifest = myJar.getManifest();

            if (manifest != null) {
                String classPath = manifest.getMainAttributes().getValue(new Name("Class-Path"));

                if (classPath != null) {
                    String[] astring = classPath.split(" ");
                    int i = astring.length;

                    for (int j = 0; j < i; ++j) {
                        String jar = astring[j];

                        validUrl = tryToGetValidUrl(jarFile.getPath(), (new File(part)).getParent(), jar);
                        if (validUrl != null) {
                            result.add(validUrl);
                        }
                    }
                }
            }
        } catch (IOException ioexception) {
            ;
        }

        return distinctUrls(result);
    }

    public static Collection forManifest(Iterable urls) {
        ArrayList result = new ArrayList();
        Iterator iterator = urls.iterator();

        while (iterator.hasNext()) {
            URL url = (URL) iterator.next();

            result.addAll(forManifest(url));
        }

        return distinctUrls(result);
    }

    static URL tryToGetValidUrl(String workingDir, String path, String filename) {
        try {
            if ((new File(filename)).exists()) {
                return (new File(filename)).toURI().toURL();
            }

            if ((new File(path + File.separator + filename)).exists()) {
                return (new File(path + File.separator + filename)).toURI().toURL();
            }

            if ((new File(workingDir + File.separator + filename)).exists()) {
                return (new File(workingDir + File.separator + filename)).toURI().toURL();
            }

            if ((new File((new URL(filename)).getFile())).exists()) {
                return (new File((new URL(filename)).getFile())).toURI().toURL();
            }
        } catch (MalformedURLException malformedurlexception) {
            ;
        }

        return null;
    }

    public static String cleanPath(URL url) {
        String path = url.getPath();

        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException unsupportedencodingexception) {
            ;
        }

        if (path.startsWith("jar:")) {
            path = path.substring("jar:".length());
        }

        if (path.startsWith("file:")) {
            path = path.substring("file:".length());
        }

        if (path.endsWith("!/")) {
            path = path.substring(0, path.lastIndexOf("!/")) + "/";
        }

        return path;
    }

    private static String resourceName(String name) {
        if (name != null) {
            String resourceName = name.replace(".", "/");

            resourceName = resourceName.replace("\\", "/");
            if (resourceName.startsWith("/")) {
                resourceName = resourceName.substring(1);
            }

            return resourceName;
        } else {
            return null;
        }
    }

    private static Collection distinctUrls(Collection urls) {
        LinkedHashMap distinct = new LinkedHashMap(urls.size());
        Iterator iterator = urls.iterator();

        while (iterator.hasNext()) {
            URL url = (URL) iterator.next();

            distinct.put(url.toExternalForm(), url);
        }

        return distinct.values();
    }
}
