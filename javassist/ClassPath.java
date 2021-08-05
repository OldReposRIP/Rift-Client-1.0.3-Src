package javassist;

import java.io.InputStream;
import java.net.URL;

public interface ClassPath {

    InputStream openClassfile(String s) throws NotFoundException;

    URL find(String s);

    void close();
}
