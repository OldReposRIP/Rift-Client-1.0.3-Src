package org.reflections.vfs;

import com.google.common.base.Predicate;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;

public class UrlTypeVFS implements Vfs.UrlType {

    public static final String[] REPLACE_EXTENSION = new String[] { ".ear/", ".jar/", ".war/", ".sar/", ".har/", ".par/"};
    final String VFSZIP = "vfszip";
    final String VFSFILE = "vfsfile";
    Predicate realFile = new Predicate() {
        public boolean apply(File file) {
            return file.exists() && file.isFile();
        }
    };

    public boolean matches(URL url) {
        return "vfszip".equals(url.getProtocol()) || "vfsfile".equals(url.getProtocol());
    }

    public Vfs.Dir createDir(URL url) {
        try {
            URL e = this.adaptURL(url);

            return new ZipDir(new JarFile(e.getFile()));
        } catch (Exception exception) {
            try {
                return new ZipDir(new JarFile(url.getFile()));
            } catch (IOException ioexception) {
                if (Reflections.log != null) {
                    Reflections.log.warn("Could not get URL", exception);
                    Reflections.log.warn("Could not get URL", ioexception);
                }

                return null;
            }
        }
    }

    public URL adaptURL(URL url) throws MalformedURLException {
        return "vfszip".equals(url.getProtocol()) ? this.replaceZipSeparators(url.getPath(), this.realFile) : ("vfsfile".equals(url.getProtocol()) ? new URL(url.toString().replace("vfsfile", "file")) : url);
    }

    URL replaceZipSeparators(String path, Predicate acceptFile) throws MalformedURLException {
        int pos = 0;

        while (pos != -1) {
            pos = this.findFirstMatchOfDeployableExtention(path, pos);
            if (pos > 0) {
                File file = new File(path.substring(0, pos - 1));

                if (acceptFile.apply(file)) {
                    return this.replaceZipSeparatorStartingFrom(path, pos);
                }
            }
        }

        throw new ReflectionsException("Unable to identify the real zip file in path \'" + path + "\'.");
    }

    int findFirstMatchOfDeployableExtention(String path, int pos) {
        Pattern p = Pattern.compile("\\.[ejprw]ar/");
        Matcher m = p.matcher(path);

        return m.find(pos) ? m.end() : -1;
    }

    URL replaceZipSeparatorStartingFrom(String path, int pos) throws MalformedURLException {
        String zipFile = path.substring(0, pos - 1);
        String zipPath = path.substring(pos);
        int numSubs = 1;
        String[] prefix = UrlTypeVFS.REPLACE_EXTENSION;
        int i = prefix.length;

        for (int i = 0; i < i; ++i) {
            for (String ext = prefix[i]; zipPath.contains(ext); ++numSubs) {
                zipPath = zipPath.replace(ext, ext.substring(0, 4) + "!");
            }
        }

        String s = "";

        for (i = 0; i < numSubs; ++i) {
            s = s + "zip:";
        }

        if (zipPath.trim().length() == 0) {
            return new URL(s + "/" + zipFile);
        } else {
            return new URL(s + "/" + zipFile + "!" + zipPath);
        }
    }
}
