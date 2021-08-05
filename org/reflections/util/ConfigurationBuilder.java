package org.reflections.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.adapters.JavaReflectionAdapter;
import org.reflections.adapters.JavassistAdapter;
import org.reflections.adapters.MetadataAdapter;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.serializers.Serializer;
import org.reflections.serializers.XmlSerializer;

public class ConfigurationBuilder implements Configuration {

    @Nonnull
    private Set scanners = Sets.newHashSet(new Scanner[] { new TypeAnnotationsScanner(), new SubTypesScanner()});
    @Nonnull
    private Set urls = Sets.newHashSet();
    protected MetadataAdapter metadataAdapter;
    @Nullable
    private Predicate inputsFilter;
    private Serializer serializer;
    @Nullable
    private ExecutorService executorService;
    @Nullable
    private ClassLoader[] classLoaders;
    private boolean expandSuperTypes = true;

    public static ConfigurationBuilder build(@Nullable Object... names) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        ArrayList nameeters = Lists.newArrayList();
        Iterator iterator;
        Object object;

        if (names != null) {
            Object[] loaders = names;
            int classLoaders = names.length;

            for (int filter = 0; filter < classLoaders; ++filter) {
                Object scanners = loaders[filter];

                if (scanners != null) {
                    if (scanners.getClass().isArray()) {
                        Object[] aobject = (Object[]) ((Object[]) scanners);
                        int name = aobject.length;

                        for (int i = 0; i < name; ++i) {
                            Object p = aobject[i];

                            if (p != null) {
                                nameeters.add(p);
                            }
                        }
                    } else if (scanners instanceof Iterable) {
                        iterator = ((Iterable) scanners).iterator();

                        while (iterator.hasNext()) {
                            object = iterator.next();
                            if (object != null) {
                                nameeters.add(object);
                            }
                        }
                    } else {
                        nameeters.add(scanners);
                    }
                }
            }
        }

        ArrayList arraylist = Lists.newArrayList();
        Iterator iterator1 = nameeters.iterator();

        while (iterator1.hasNext()) {
            Object object1 = iterator1.next();

            if (object1 instanceof ClassLoader) {
                arraylist.add((ClassLoader) object1);
            }
        }

        ClassLoader[] aclassloader = arraylist.isEmpty() ? null : (ClassLoader[]) arraylist.toArray(new ClassLoader[arraylist.size()]);
        FilterBuilder filterbuilder = new FilterBuilder();
        ArrayList arraylist1 = Lists.newArrayList();

        iterator = nameeters.iterator();

        while (iterator.hasNext()) {
            object = iterator.next();
            if (object instanceof String) {
                builder.addUrls(ClasspathHelper.forPackage((String) object, aclassloader));
                filterbuilder.includePackage(new String[] { (String) object});
            } else if (object instanceof Class) {
                if (Scanner.class.isAssignableFrom((Class) object)) {
                    try {
                        builder.addScanners(new Scanner[] { (Scanner) ((Class) object).newInstance()});
                    } catch (Exception exception) {
                        ;
                    }
                }

                builder.addUrls(new URL[] { ClasspathHelper.forClass((Class) object, aclassloader)});
                filterbuilder.includePackage((Class) object);
            } else if (object instanceof Scanner) {
                arraylist1.add((Scanner) object);
            } else if (object instanceof URL) {
                builder.addUrls(new URL[] { (URL) object});
            } else if (!(object instanceof ClassLoader)) {
                if (object instanceof Predicate) {
                    filterbuilder.add((Predicate) object);
                } else if (object instanceof ExecutorService) {
                    builder.setExecutorService((ExecutorService) object);
                } else if (Reflections.log != null) {
                    throw new ReflectionsException("could not use name " + object);
                }
            }
        }

        if (builder.getUrls().isEmpty()) {
            if (aclassloader != null) {
                builder.addUrls(ClasspathHelper.forClassLoader(aclassloader));
            } else {
                builder.addUrls(ClasspathHelper.forClassLoader());
            }
        }

        builder.filterInputsBy(filterbuilder);
        if (!arraylist1.isEmpty()) {
            builder.setScanners((Scanner[]) arraylist1.toArray(new Scanner[arraylist1.size()]));
        }

        if (!arraylist.isEmpty()) {
            builder.addClassLoaders((Collection) arraylist);
        }

        return builder;
    }

    public ConfigurationBuilder forPackages(String... packages) {
        String[] astring = packages;
        int i = packages.length;

        for (int j = 0; j < i; ++j) {
            String pkg = astring[j];

            this.addUrls(ClasspathHelper.forPackage(pkg, new ClassLoader[0]));
        }

        return this;
    }

    @Nonnull
    public Set getScanners() {
        return this.scanners;
    }

    public ConfigurationBuilder setScanners(@Nonnull Scanner... scanners) {
        this.scanners.clear();
        return this.addScanners(scanners);
    }

    public ConfigurationBuilder addScanners(Scanner... scanners) {
        this.scanners.addAll(Sets.newHashSet(scanners));
        return this;
    }

    @Nonnull
    public Set getUrls() {
        return this.urls;
    }

    public ConfigurationBuilder setUrls(@Nonnull Collection urls) {
        this.urls = Sets.newHashSet(urls);
        return this;
    }

    public ConfigurationBuilder setUrls(URL... urls) {
        this.urls = Sets.newHashSet(urls);
        return this;
    }

    public ConfigurationBuilder addUrls(Collection urls) {
        this.urls.addAll(urls);
        return this;
    }

    public ConfigurationBuilder addUrls(URL... urls) {
        this.urls.addAll(Sets.newHashSet(urls));
        return this;
    }

    public MetadataAdapter getMetadataAdapter() {
        if (this.metadataAdapter != null) {
            return this.metadataAdapter;
        } else {
            try {
                return this.metadataAdapter = new JavassistAdapter();
            } catch (Throwable throwable) {
                if (Reflections.log != null) {
                    Reflections.log.warn("could not create JavassistAdapter, using JavaReflectionAdapter", throwable);
                }

                return this.metadataAdapter = new JavaReflectionAdapter();
            }
        }
    }

    public ConfigurationBuilder setMetadataAdapter(MetadataAdapter metadataAdapter) {
        this.metadataAdapter = metadataAdapter;
        return this;
    }

    @Nullable
    public Predicate getInputsFilter() {
        return this.inputsFilter;
    }

    public void setInputsFilter(@Nullable Predicate inputsFilter) {
        this.inputsFilter = inputsFilter;
    }

    public ConfigurationBuilder filterInputsBy(Predicate inputsFilter) {
        this.inputsFilter = inputsFilter;
        return this;
    }

    @Nullable
    public ExecutorService getExecutorService() {
        return this.executorService;
    }

    public ConfigurationBuilder setExecutorService(@Nullable ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    public ConfigurationBuilder useParallelExecutor() {
        return this.useParallelExecutor(Runtime.getRuntime().availableProcessors());
    }

    public ConfigurationBuilder useParallelExecutor(int availableProcessors) {
        ThreadFactory factory = (new ThreadFactoryBuilder()).setDaemon(true).setNameFormat("org.reflections-scanner-%d").build();

        this.setExecutorService(Executors.newFixedThreadPool(availableProcessors, factory));
        return this;
    }

    public Serializer getSerializer() {
        return this.serializer != null ? this.serializer : (this.serializer = new XmlSerializer());
    }

    public ConfigurationBuilder setSerializer(Serializer serializer) {
        this.serializer = serializer;
        return this;
    }

    @Nullable
    public ClassLoader[] getClassLoaders() {
        return this.classLoaders;
    }

    public boolean shouldExpandSuperTypes() {
        return this.expandSuperTypes;
    }

    public ConfigurationBuilder setExpandSuperTypes(boolean expandSuperTypes) {
        this.expandSuperTypes = expandSuperTypes;
        return this;
    }

    public void setClassLoaders(@Nullable ClassLoader[] classLoaders) {
        this.classLoaders = classLoaders;
    }

    public ConfigurationBuilder addClassLoader(ClassLoader classLoader) {
        return this.addClassLoaders(new ClassLoader[] { classLoader});
    }

    public ConfigurationBuilder addClassLoaders(ClassLoader... classLoaders) {
        this.classLoaders = this.classLoaders == null ? classLoaders : (ClassLoader[]) ObjectArrays.concat(this.classLoaders, classLoaders, ClassLoader.class);
        return this;
    }

    public ConfigurationBuilder addClassLoaders(Collection classLoaders) {
        return this.addClassLoaders((ClassLoader[]) classLoaders.toArray(new ClassLoader[classLoaders.size()]));
    }
}
