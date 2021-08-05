package me.zero.alpine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;

public class EventManager implements EventBus {

    private final Map SUBSCRIPTION_CACHE = new HashMap();
    private final Map SUBSCRIPTION_MAP = new HashMap();
    private final List ATTACHED_BUSES = new ArrayList();

    public void subscribe(Object object) {
        List listeners = (List) this.SUBSCRIPTION_CACHE.computeIfAbsent(object, (o) -> {
            return (List) Arrays.stream(o.getClass().getDeclaredFields()).filter(EventManager::isValidField).map((field) -> {
                return asListener(o, field);
            }).filter(Objects::nonNull).collect(Collectors.toList());
        });

        listeners.forEach(this::subscribe);
        if (!this.ATTACHED_BUSES.isEmpty()) {
            this.ATTACHED_BUSES.forEach((bus) -> {
                bus.subscribe(object);
            });
        }

    }

    public void subscribe(Object... objects) {
        Arrays.stream(objects).forEach(this::subscribe);
    }

    public void subscribe(Iterable objects) {
        objects.forEach(this::subscribe);
    }

    public void unsubscribe(Object object) {
        List objectListeners = (List) this.SUBSCRIPTION_CACHE.get(object);

        if (objectListeners != null) {
            this.SUBSCRIPTION_MAP.values().forEach((listeners) -> {
                Objects.requireNonNull(objectListeners);
                listeners.removeIf(objectListeners::contains);
            });
            if (!this.ATTACHED_BUSES.isEmpty()) {
                this.ATTACHED_BUSES.forEach((bus) -> {
                    bus.unsubscribe(object);
                });
            }

        }
    }

    public void unsubscribe(Object... objects) {
        Arrays.stream(objects).forEach(this::unsubscribe);
    }

    public void unsubscribe(Iterable objects) {
        objects.forEach(this::unsubscribe);
    }

    public void post(Object event) {
        List listeners = (List) this.SUBSCRIPTION_MAP.get(event.getClass());

        if (listeners != null) {
            listeners.forEach((listener) -> {
                listener.invoke(event);
            });
        }

        if (!this.ATTACHED_BUSES.isEmpty()) {
            this.ATTACHED_BUSES.forEach((bus) -> {
                bus.post(event);
            });
        }

    }

    public void attach(EventBus bus) {
        if (!this.ATTACHED_BUSES.contains(bus)) {
            this.ATTACHED_BUSES.add(bus);
        }

    }

    public void detach(EventBus bus) {
        if (this.ATTACHED_BUSES.contains(bus)) {
            this.ATTACHED_BUSES.remove(bus);
        }

    }

    private static boolean isValidField(Field field) {
        return field.isAnnotationPresent(EventHandler.class) && Listener.class.isAssignableFrom(field.getType());
    }

    private static Listener asListener(Object object, Field field) {
        try {
            boolean e = field.isAccessible();

            field.setAccessible(true);
            Listener listener = (Listener) field.get(object);

            field.setAccessible(e);
            if (listener == null) {
                return null;
            } else if (listener.getPriority() <= 5 && listener.getPriority() >= 1) {
                return listener;
            } else {
                throw new RuntimeException("Event Priority out of bounds! %s");
            }
        } catch (IllegalAccessException illegalaccessexception) {
            return null;
        }
    }

    private void subscribe(Listener listener) {
        List listeners = (List) this.SUBSCRIPTION_MAP.computeIfAbsent(listener.getTarget(), (target) -> {
            return new ArrayList();
        });

        int index;

        for (index = 0; index < listeners.size() && listener.getPriority() >= ((Listener) listeners.get(index)).getPriority(); ++index) {
            ;
        }

        listeners.add(index, listener);
    }
}
