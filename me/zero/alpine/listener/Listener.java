package me.zero.alpine.listener;

import java.util.function.Predicate;
import net.jodah.typetools.TypeResolver;

public final class Listener implements EventHook {

    private final Class target;
    private final EventHook hook;
    private final Predicate[] filters;
    private final byte priority;

    @SafeVarargs
    public Listener(EventHook hook, Predicate... filters) {
        this(hook, (byte) 3, filters);
    }

    @SafeVarargs
    public Listener(EventHook hook, byte priority, Predicate... filters) {
        this.hook = hook;
        this.priority = priority;
        this.target = TypeResolver.resolveRawArgument(EventHook.class, hook.getClass());
        this.filters = filters;
    }

    public final Class getTarget() {
        return this.target;
    }

    public final byte getPriority() {
        return this.priority;
    }

    public final void invoke(Object event) {
        if (this.filters.length > 0) {
            Predicate[] apredicate = this.filters;
            int i = apredicate.length;

            for (int j = 0; j < i; ++j) {
                Predicate filter = apredicate[j];

                if (!filter.test(event)) {
                    return;
                }
            }
        }

        this.hook.invoke(event);
    }
}
