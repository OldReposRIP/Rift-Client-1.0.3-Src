package me.zero.alpine.listener;

@FunctionalInterface
public interface EventHook {

    void invoke(Object object);
}
