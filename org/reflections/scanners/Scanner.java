package org.reflections.scanners;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import javax.annotation.Nullable;
import org.reflections.Configuration;
import org.reflections.vfs.Vfs;

public interface Scanner {

    void setConfiguration(Configuration configuration);

    Multimap getStore();

    void setStore(Multimap multimap);

    Scanner filterResultsBy(Predicate predicate);

    boolean acceptsInput(String s);

    Object scan(Vfs.File vfs_file, @Nullable Object object);

    boolean acceptResult(String s);
}
