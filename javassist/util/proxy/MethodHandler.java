package javassist.util.proxy;

import java.lang.reflect.Method;

public interface MethodHandler {

    Object invoke(Object object, Method method, Method method1, Object[] aobject) throws Throwable;
}
