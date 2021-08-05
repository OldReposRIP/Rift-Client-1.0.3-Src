package org.reflections.util;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.reflections.ReflectionsException;

public class FilterBuilder implements Predicate {

    private final List chain;

    public FilterBuilder() {
        this.chain = Lists.newArrayList();
    }

    private FilterBuilder(Iterable filters) {
        this.chain = Lists.newArrayList(filters);
    }

    public FilterBuilder include(String regex) {
        return this.add(new FilterBuilder.Include(regex));
    }

    public FilterBuilder exclude(String regex) {
        this.add(new FilterBuilder.Exclude(regex));
        return this;
    }

    public FilterBuilder add(Predicate filter) {
        this.chain.add(filter);
        return this;
    }

    public FilterBuilder includePackage(Class aClass) {
        return this.add(new FilterBuilder.Include(packageNameRegex(aClass)));
    }

    public FilterBuilder excludePackage(Class aClass) {
        return this.add(new FilterBuilder.Exclude(packageNameRegex(aClass)));
    }

    public FilterBuilder includePackage(String... prefixes) {
        String[] astring = prefixes;
        int i = prefixes.length;

        for (int j = 0; j < i; ++j) {
            String prefix = astring[j];

            this.add(new FilterBuilder.Include(prefix(prefix)));
        }

        return this;
    }

    public FilterBuilder excludePackage(String prefix) {
        return this.add(new FilterBuilder.Exclude(prefix(prefix)));
    }

    private static String packageNameRegex(Class aClass) {
        return prefix(aClass.getPackage().getName() + ".");
    }

    public static String prefix(String qualifiedName) {
        return qualifiedName.replace(".", "\\.") + ".*";
    }

    public String toString() {
        return Joiner.on(", ").join(this.chain);
    }

    public boolean apply(String regex) {
        boolean accept = this.chain == null || this.chain.isEmpty() || this.chain.get(0) instanceof FilterBuilder.Exclude;

        if (this.chain != null) {
            Iterator iterator = this.chain.iterator();

            while (iterator.hasNext()) {
                Predicate filter = (Predicate) iterator.next();

                if ((!accept || !(filter instanceof FilterBuilder.Include)) && (accept || !(filter instanceof FilterBuilder.Exclude))) {
                    accept = filter.apply(regex);
                    if (!accept && filter instanceof FilterBuilder.Exclude) {
                        break;
                    }
                }
            }
        }

        return accept;
    }

    public static FilterBuilder parse(String includeExcludeString) {
        ArrayList filters = new ArrayList();

        if (!Utils.isEmpty(includeExcludeString)) {
            String[] astring = includeExcludeString.split(",");
            int i = astring.length;

            for (int j = 0; j < i; ++j) {
                String string = astring[j];
                String trimmed = string.trim();
                char prefix = trimmed.charAt(0);
                String pattern = trimmed.substring(1);
                Object filter;

                switch (prefix) {
                case '+':
                    filter = new FilterBuilder.Include(pattern);
                    break;

                case '-':
                    filter = new FilterBuilder.Exclude(pattern);
                    break;

                default:
                    throw new ReflectionsException("includeExclude should start with either + or -");
                }

                filters.add(filter);
            }

            return new FilterBuilder(filters);
        } else {
            return new FilterBuilder();
        }
    }

    public static FilterBuilder parsePackages(String includeExcludeString) {
        ArrayList filters = new ArrayList();

        if (!Utils.isEmpty(includeExcludeString)) {
            String[] astring = includeExcludeString.split(",");
            int i = astring.length;

            for (int j = 0; j < i; ++j) {
                String string = astring[j];
                String trimmed = string.trim();
                char prefix = trimmed.charAt(0);
                String pattern = trimmed.substring(1);

                if (!pattern.endsWith(".")) {
                    pattern = pattern + ".";
                }

                pattern = prefix(pattern);
                Object filter;

                switch (prefix) {
                case '+':
                    filter = new FilterBuilder.Include(pattern);
                    break;

                case '-':
                    filter = new FilterBuilder.Exclude(pattern);
                    break;

                default:
                    throw new ReflectionsException("includeExclude should start with either + or -");
                }

                filters.add(filter);
            }

            return new FilterBuilder(filters);
        } else {
            return new FilterBuilder();
        }
    }

    public static class Exclude extends FilterBuilder.Matcher {

        public Exclude(String patternString) {
            super(patternString);
        }

        public boolean apply(String regex) {
            return !this.pattern.matcher(regex).matches();
        }

        public String toString() {
            return "-" + super.toString();
        }
    }

    public static class Include extends FilterBuilder.Matcher {

        public Include(String patternString) {
            super(patternString);
        }

        public boolean apply(String regex) {
            return this.pattern.matcher(regex).matches();
        }

        public String toString() {
            return "+" + super.toString();
        }
    }

    public abstract static class Matcher implements Predicate {

        final Pattern pattern;

        public Matcher(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        public abstract boolean apply(String s);

        public String toString() {
            return this.pattern.pattern();
        }
    }
}
