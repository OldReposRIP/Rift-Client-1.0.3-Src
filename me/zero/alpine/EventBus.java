package me.zero.alpine;

public interface EventBus {

    void subscribe(Object object);

    void subscribe(Object... aobject);

    void subscribe(Iterable iterable);

    void unsubscribe(Object object);

    void unsubscribe(Object... aobject);

    void unsubscribe(Iterable iterable);

    void post(Object object);

    void attach(EventBus eventbus);

    void detach(EventBus eventbus);
}
