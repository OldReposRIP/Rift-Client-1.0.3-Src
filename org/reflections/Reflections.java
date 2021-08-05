package org.reflections;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.Sets.SetView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MemberUsageScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterNamesScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.serializers.Serializer;
import org.reflections.serializers.XmlSerializer;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.reflections.util.Utils;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;

public class Reflections {

    @Nullable
    public static Logger log = Utils.findLogger(Reflections.class);
    protected final transient Configuration configuration;
    protected Store store;

    public Reflections(Configuration configuration) {
        this.configuration = configuration;
        this.store = new Store(configuration);
        if (configuration.getScanners() != null && !configuration.getScanners().isEmpty()) {
            Iterator iterator = configuration.getScanners().iterator();

            while (iterator.hasNext()) {
                Scanner scanner = (Scanner) iterator.next();

                scanner.setConfiguration(configuration);
                scanner.setStore(this.store.getOrCreate(scanner.getClass().getSimpleName()));
            }

            this.scan();
            if (configuration.shouldExpandSuperTypes()) {
                this.expandSuperTypes();
            }
        }

    }

    public Reflections(String prefix, @Nullable Scanner... scanners) {
        this(new Object[] { prefix, scanners});
    }

    public Reflections(Object... names) {
        this((Configuration) ConfigurationBuilder.build(names));
    }

    protected Reflections() {
        this.configuration = new ConfigurationBuilder();
        this.store = new Store(this.configuration);
    }

    protected void scan() {
        if (this.configuration.getUrls() != null && !this.configuration.getUrls().isEmpty()) {
            if (Reflections.log != null && Reflections.log.isDebugEnabled()) {
                Reflections.log.debug("going to scan these urls:\n" + Joiner.on("\n").join(this.configuration.getUrls()));
            }

            long time = System.currentTimeMillis();
            int scannedUrls = 0;
            ExecutorService executorService = this.configuration.getExecutorService();
            ArrayList futures = Lists.newArrayList();
            Iterator keys = this.configuration.getUrls().iterator();

            while (keys.hasNext()) {
                final URL values = (URL) keys.next();

                try {
                    if (executorService != null) {
                        futures.add(executorService.submit(new Runnable() {
                            public void run() {
                                if (Reflections.log != null && Reflections.log.isDebugEnabled()) {
                                    Reflections.log.debug("[" + Thread.currentThread().toString() + "] scanning " + values);
                                }

                                Reflections.this.scan(values);
                            }
                        }));
                    } else {
                        this.scan(values);
                    }

                    ++scannedUrls;
                } catch (ReflectionsException reflectionsexception) {
                    if (Reflections.log != null && Reflections.log.isWarnEnabled()) {
                        Reflections.log.warn("could not create Vfs.Dir from url. ignoring the exception and continuing", reflectionsexception);
                    }
                }
            }

            if (executorService != null) {
                keys = futures.iterator();

                while (keys.hasNext()) {
                    Future future = (Future) keys.next();

                    try {
                        future.get();
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                }
            }

            time = System.currentTimeMillis() - time;
            if (executorService != null) {
                executorService.shutdown();
            }

            if (Reflections.log != null) {
                int i = 0;
                int j = 0;

                String index;

                for (Iterator e = this.store.keySet().iterator(); e.hasNext(); j += this.store.get(index).size()) {
                    index = (String) e.next();
                    i += this.store.get(index).keySet().size();
                }

                Reflections.log.info(String.format("Reflections took %d ms to scan %d urls, producing %d keys and %d values %s", new Object[] { Long.valueOf(time), Integer.valueOf(scannedUrls), Integer.valueOf(i), Integer.valueOf(j), executorService != null && executorService instanceof ThreadPoolExecutor ? String.format("[using %d cores]", new Object[] { Integer.valueOf(((ThreadPoolExecutor) executorService).getMaximumPoolSize())}) : ""}));
            }

        } else {
            if (Reflections.log != null) {
                Reflections.log.warn("given scan urls are empty. set urls in the configuration");
            }

        }
    }

    protected void scan(URL url) {
        Vfs.Dir dir = Vfs.fromURL(url);

        try {
            Iterator iterator = dir.getFiles().iterator();

            while (iterator.hasNext()) {
                Vfs.File file = (Vfs.File) iterator.next();
                Predicate inputsFilter = this.configuration.getInputsFilter();
                String path = file.getRelativePath();
                String fqn = path.replace('/', '.');

                if (inputsFilter == null || inputsFilter.apply(path) || inputsFilter.apply(fqn)) {
                    Object classObject = null;
                    Iterator iterator1 = this.configuration.getScanners().iterator();

                    while (iterator1.hasNext()) {
                        Scanner scanner = (Scanner) iterator1.next();

                        try {
                            if (scanner.acceptsInput(path) || scanner.acceptResult(fqn)) {
                                classObject = scanner.scan(file, classObject);
                            }
                        } catch (Exception exception) {
                            if (Reflections.log != null && Reflections.log.isDebugEnabled()) {
                                Reflections.log.debug("could not scan file " + file.getRelativePath() + " in url " + url.toExternalForm() + " with scanner " + scanner.getClass().getSimpleName(), exception);
                            }
                        }
                    }
                }
            }
        } finally {
            dir.close();
        }

    }

    public static Reflections collect() {
        return collect("META-INF/reflections/", (new FilterBuilder()).include(".*-reflections.xml"), new Serializer[0]);
    }

    public static Reflections collect(String packagePrefix, Predicate resourceNameFilter, @Nullable Serializer... optionalSerializer) {
        Object serializer = optionalSerializer != null && optionalSerializer.length == 1 ? optionalSerializer[0] : new XmlSerializer();
        Collection urls = ClasspathHelper.forPackage(packagePrefix, new ClassLoader[0]);

        if (urls.isEmpty()) {
            return null;
        } else {
            long start = System.currentTimeMillis();
            Reflections reflections = new Reflections();
            Iterable files = Vfs.findFiles(urls, packagePrefix, resourceNameFilter);
            Iterator store = files.iterator();

            while (store.hasNext()) {
                Vfs.File keys = (Vfs.File) store.next();
                InputStream values = null;

                try {
                    values = keys.openInputStream();
                    reflections.merge(((Serializer) serializer).read(values));
                } catch (IOException ioexception) {
                    throw new ReflectionsException("could not merge " + keys, ioexception);
                } finally {
                    Utils.close(values);
                }
            }

            if (Reflections.log != null) {
                Store store1 = reflections.getStore();
                int keys1 = 0;
                int values1 = 0;

                String index;

                for (Iterator e = store1.keySet().iterator(); e.hasNext(); values1 += store1.get(index).size()) {
                    index = (String) e.next();
                    keys1 += store1.get(index).keySet().size();
                }

                Reflections.log.info(String.format("Reflections took %d ms to collect %d url%s, producing %d keys and %d values [%s]", new Object[] { Long.valueOf(System.currentTimeMillis() - start), Integer.valueOf(urls.size()), urls.size() > 1 ? "s" : "", Integer.valueOf(keys1), Integer.valueOf(values1), Joiner.on(", ").join(urls)}));
            }

            return reflections;
        }
    }

    public Reflections collect(InputStream inputStream) {
        try {
            this.merge(this.configuration.getSerializer().read(inputStream));
            if (Reflections.log != null) {
                Reflections.log.info("Reflections collected metadata from input stream using serializer " + this.configuration.getSerializer().getClass().getName());
            }

            return this;
        } catch (Exception exception) {
            throw new ReflectionsException("could not merge input stream", exception);
        }
    }

    public Reflections collect(File file) {
        FileInputStream inputStream = null;

        Reflections e;

        try {
            inputStream = new FileInputStream(file);
            e = this.collect((InputStream) inputStream);
        } catch (FileNotFoundException filenotfoundexception) {
            throw new ReflectionsException("could not obtain input stream from file " + file, filenotfoundexception);
        } finally {
            Utils.close(inputStream);
        }

        return e;
    }

    public Reflections merge(Reflections reflections) {
        if (reflections.store != null) {
            Iterator iterator = reflections.store.keySet().iterator();

            while (iterator.hasNext()) {
                String indexName = (String) iterator.next();
                Multimap index = reflections.store.get(indexName);
                Iterator iterator1 = index.keySet().iterator();

                while (iterator1.hasNext()) {
                    String key = (String) iterator1.next();
                    Iterator iterator2 = index.get(key).iterator();

                    while (iterator2.hasNext()) {
                        String string = (String) iterator2.next();

                        this.store.getOrCreate(indexName).put(key, string);
                    }
                }
            }
        }

        return this;
    }

    public void expandSuperTypes() {
        if (this.store.keySet().contains(index(SubTypesScanner.class))) {
            Multimap mmap = this.store.get(index(SubTypesScanner.class));
            SetView keys = Sets.difference(mmap.keySet(), Sets.newHashSet(mmap.values()));
            HashMultimap expand = HashMultimap.create();
            UnmodifiableIterator unmodifiableiterator = keys.iterator();

            while (unmodifiableiterator.hasNext()) {
                String key = (String) unmodifiableiterator.next();
                Class type = ReflectionUtils.forName(key, new ClassLoader[0]);

                if (type != null) {
                    this.expandSupertypes(expand, key, type);
                }
            }

            mmap.putAll(expand);
        }

    }

    private void expandSupertypes(Multimap mmap, String key, Class type) {
        Iterator iterator = ReflectionUtils.getSuperTypes(type).iterator();

        while (iterator.hasNext()) {
            Class supertype = (Class) iterator.next();

            if (mmap.put(supertype.getName(), key)) {
                if (Reflections.log != null) {
                    Reflections.log.debug("expanded subtype {} -> {}", supertype.getName(), key);
                }

                this.expandSupertypes(mmap, supertype.getName(), supertype);
            }
        }

    }

    public Set getSubTypesOf(Class type) {
        return Sets.newHashSet(ReflectionUtils.forNames(this.store.getAll(index(SubTypesScanner.class), (Iterable) Arrays.asList(new String[] { type.getName()})), this.loaders()));
    }

    public Set getTypesAnnotatedWith(Class annotation) {
        return this.getTypesAnnotatedWith(annotation, false);
    }

    public Set getTypesAnnotatedWith(Class annotation, boolean honorInherited) {
        Iterable annotated = this.store.get(index(TypeAnnotationsScanner.class), new String[] { annotation.getName()});
        Iterable classes = this.getAllAnnotated(annotated, annotation.isAnnotationPresent(Inherited.class), honorInherited);

        return Sets.newHashSet(Iterables.concat(ReflectionUtils.forNames(annotated, this.loaders()), ReflectionUtils.forNames(classes, this.loaders())));
    }

    public Set getTypesAnnotatedWith(Annotation annotation) {
        return this.getTypesAnnotatedWith(annotation, false);
    }

    public Set getTypesAnnotatedWith(Annotation annotation, boolean honorInherited) {
        Iterable annotated = this.store.get(index(TypeAnnotationsScanner.class), new String[] { annotation.annotationType().getName()});
        Set filter = ReflectionUtils.filter((Iterable) ReflectionUtils.forNames(annotated, this.loaders()), new Predicate[] { ReflectionUtils.withAnnotation(annotation)});
        Iterable classes = this.getAllAnnotated(Utils.names((Iterable) filter), annotation.annotationType().isAnnotationPresent(Inherited.class), honorInherited);

        return Sets.newHashSet(Iterables.concat(filter, ReflectionUtils.forNames(ReflectionUtils.filter(classes, new Predicate[] { Predicates.not(Predicates.in(Sets.newHashSet(annotated)))}), this.loaders())));
    }

    protected Iterable getAllAnnotated(Iterable annotated, boolean inherited, boolean honorInherited) {
        Iterable subTypes;

        if (honorInherited) {
            if (inherited) {
                subTypes = this.store.get(index(SubTypesScanner.class), (Iterable) ReflectionUtils.filter(annotated, new Predicate[] { new Predicate() {
                    public boolean apply(@Nullable String input) {
                        Class type = ReflectionUtils.forName(input, Reflections.this.loaders());

                        return type != null && !type.isInterface();
                    }
                }}));
                return Iterables.concat(subTypes, this.store.getAll(index(SubTypesScanner.class), subTypes));
            } else {
                return annotated;
            }
        } else {
            subTypes = Iterables.concat(annotated, this.store.getAll(index(TypeAnnotationsScanner.class), annotated));
            return Iterables.concat(subTypes, this.store.getAll(index(SubTypesScanner.class), subTypes));
        }
    }

    public Set getMethodsAnnotatedWith(Class annotation) {
        Iterable methods = this.store.get(index(MethodAnnotationsScanner.class), new String[] { annotation.getName()});

        return Utils.getMethodsFromDescriptors(methods, this.loaders());
    }

    public Set getMethodsAnnotatedWith(Annotation annotation) {
        return ReflectionUtils.filter((Iterable) this.getMethodsAnnotatedWith(annotation.annotationType()), new Predicate[] { ReflectionUtils.withAnnotation(annotation)});
    }

    public Set getMethodsMatchParams(Class... types) {
        return Utils.getMethodsFromDescriptors(this.store.get(index(MethodParameterScanner.class), new String[] { Utils.names(types).toString()}), this.loaders());
    }

    public Set getMethodsReturn(Class returnType) {
        return Utils.getMethodsFromDescriptors(this.store.get(index(MethodParameterScanner.class), (Iterable) Utils.names(new Class[] { returnType})), this.loaders());
    }

    public Set getMethodsWithAnyParamAnnotated(Class annotation) {
        return Utils.getMethodsFromDescriptors(this.store.get(index(MethodParameterScanner.class), new String[] { annotation.getName()}), this.loaders());
    }

    public Set getMethodsWithAnyParamAnnotated(Annotation annotation) {
        return ReflectionUtils.filter((Iterable) this.getMethodsWithAnyParamAnnotated(annotation.annotationType()), new Predicate[] { ReflectionUtils.withAnyParameterAnnotation(annotation)});
    }

    public Set getConstructorsAnnotatedWith(Class annotation) {
        Iterable methods = this.store.get(index(MethodAnnotationsScanner.class), new String[] { annotation.getName()});

        return Utils.getConstructorsFromDescriptors(methods, this.loaders());
    }

    public Set getConstructorsAnnotatedWith(Annotation annotation) {
        return ReflectionUtils.filter((Iterable) this.getConstructorsAnnotatedWith(annotation.annotationType()), new Predicate[] { ReflectionUtils.withAnnotation(annotation)});
    }

    public Set getConstructorsMatchParams(Class... types) {
        return Utils.getConstructorsFromDescriptors(this.store.get(index(MethodParameterScanner.class), new String[] { Utils.names(types).toString()}), this.loaders());
    }

    public Set getConstructorsWithAnyParamAnnotated(Class annotation) {
        return Utils.getConstructorsFromDescriptors(this.store.get(index(MethodParameterScanner.class), new String[] { annotation.getName()}), this.loaders());
    }

    public Set getConstructorsWithAnyParamAnnotated(Annotation annotation) {
        return ReflectionUtils.filter((Iterable) this.getConstructorsWithAnyParamAnnotated(annotation.annotationType()), new Predicate[] { ReflectionUtils.withAnyParameterAnnotation(annotation)});
    }

    public Set getFieldsAnnotatedWith(Class annotation) {
        HashSet result = Sets.newHashSet();
        Iterator iterator = this.store.get(index(FieldAnnotationsScanner.class), new String[] { annotation.getName()}).iterator();

        while (iterator.hasNext()) {
            String annotated = (String) iterator.next();

            result.add(Utils.getFieldFromString(annotated, this.loaders()));
        }

        return result;
    }

    public Set getFieldsAnnotatedWith(Annotation annotation) {
        return ReflectionUtils.filter((Iterable) this.getFieldsAnnotatedWith(annotation.annotationType()), new Predicate[] { ReflectionUtils.withAnnotation(annotation)});
    }

    public Set getResources(Predicate namePredicate) {
        Iterable resources = Iterables.filter(this.store.get(index(ResourcesScanner.class)).keySet(), namePredicate);

        return Sets.newHashSet(this.store.get(index(ResourcesScanner.class), resources));
    }

    public Set getResources(final Pattern pattern) {
        return this.getResources(new Predicate() {
            public boolean apply(String input) {
                return pattern.matcher(input).matches();
            }
        });
    }

    public List getMethodParamNames(Method method) {
        Iterable names = this.store.get(index(MethodParameterNamesScanner.class), new String[] { Utils.name(method)});

        return !Iterables.isEmpty(names) ? Arrays.asList(((String) Iterables.getOnlyElement(names)).split(", ")) : Arrays.asList(new String[0]);
    }

    public List getConstructorParamNames(Constructor constructor) {
        Iterable names = this.store.get(index(MethodParameterNamesScanner.class), new String[] { Utils.name(constructor)});

        return !Iterables.isEmpty(names) ? Arrays.asList(((String) Iterables.getOnlyElement(names)).split(", ")) : Arrays.asList(new String[0]);
    }

    public Set getFieldUsage(Field field) {
        return Utils.getMembersFromDescriptors(this.store.get(index(MemberUsageScanner.class), new String[] { Utils.name(field)}), new ClassLoader[0]);
    }

    public Set getMethodUsage(Method method) {
        return Utils.getMembersFromDescriptors(this.store.get(index(MemberUsageScanner.class), new String[] { Utils.name(method)}), new ClassLoader[0]);
    }

    public Set getConstructorUsage(Constructor constructor) {
        return Utils.getMembersFromDescriptors(this.store.get(index(MemberUsageScanner.class), new String[] { Utils.name(constructor)}), new ClassLoader[0]);
    }

    public Set getAllTypes() {
        HashSet allTypes = Sets.newHashSet(this.store.getAll(index(SubTypesScanner.class), Object.class.getName()));

        if (allTypes.isEmpty()) {
            throw new ReflectionsException("Couldn\'t find subtypes of Object. Make sure SubTypesScanner initialized to include Object class - new SubTypesScanner(false)");
        } else {
            return allTypes;
        }
    }

    public Store getStore() {
        return this.store;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public File save(String filename) {
        return this.save(filename, this.configuration.getSerializer());
    }

    public File save(String filename, Serializer serializer) {
        File file = serializer.save(this, filename);

        if (Reflections.log != null) {
            Reflections.log.info("Reflections successfully saved in " + file.getAbsolutePath() + " using " + serializer.getClass().getSimpleName());
        }

        return file;
    }

    private static String index(Class scannerClass) {
        return scannerClass.getSimpleName();
    }

    private ClassLoader[] loaders() {
        return this.configuration.getClassLoaders();
    }
}
