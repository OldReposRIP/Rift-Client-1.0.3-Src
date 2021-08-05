package org.reflections;

import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Store {

    private transient boolean concurrent;
    private final Map storeMap = new HashMap();

    protected Store() {
        this.concurrent = false;
    }

    public Store(Configuration configuration) {
        this.concurrent = configuration.getExecutorService() != null;
    }

    public Set keySet() {
        return this.storeMap.keySet();
    }

    public Multimap getOrCreate(String index) {
        Object mmap = (Multimap) this.storeMap.get(index);

        if (mmap == null) {
            SetMultimap multimap = Multimaps.newSetMultimap(new HashMap(), new Supplier() {
                public Set get() {
                    return Sets.newSetFromMap(new ConcurrentHashMap());
                }
            });

            mmap = this.concurrent ? Multimaps.synchronizedSetMultimap(multimap) : multimap;
            this.storeMap.put(index, mmap);
        }

        return (Multimap) mmap;
    }

    public Multimap get(String index) {
        Multimap mmap = (Multimap) this.storeMap.get(index);

        if (mmap == null) {
            throw new ReflectionsException("Scanner " + index + " was not configured");
        } else {
            return mmap;
        }
    }

    public Iterable get(String index, String... keys) {
        return this.get(index, (Iterable) Arrays.asList(keys));
    }

    public Iterable get(String index, Iterable keys) {
        Multimap mmap = this.get(index);
        Store.IterableChain result = new Store.IterableChain(null);
        Iterator iterator = keys.iterator();

        while (iterator.hasNext()) {
            String key = (String) iterator.next();

            result.addAll(mmap.get(key));
        }

        return result;
    }

    private Iterable getAllIncluding(String index, Iterable keys, Store.IterableChain result) {
        result.addAll(keys);
        Iterator iterator = keys.iterator();

        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            Iterable values = this.get(index, new String[] { key});

            if (values.iterator().hasNext()) {
                this.getAllIncluding(index, values, result);
            }
        }

        return result;
    }

    public Iterable getAll(String index, String key) {
        return this.getAllIncluding(index, this.get(index, new String[] { key}), new Store.IterableChain(null));
    }

    public Iterable getAll(String index, Iterable keys) {
        return this.getAllIncluding(index, this.get(index, keys), new Store.IterableChain(null));
    }

    private static class IterableChain implements Iterable {

        private final List chain;

        private IterableChain() {
            this.chain = Lists.newArrayList();
        }

        private void addAll(Iterable iterable) {
            this.chain.add(iterable);
        }

        public Iterator iterator() {
            return Iterables.concat(this.chain).iterator();
        }

        IterableChain(Object x0) {
            this();
        }
    }
}
