package live.rift.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import org.apache.commons.lang3.reflect.MethodUtils;

public class ReflectUtils {

    public static Object callMethod(Object target, Object[] names, String... names) {
        int len = names.length;
        int i = 0;

        while (i < len) {
            String s = names[i];

            try {
                return MethodUtils.invokeMethod(target, true, s, names);
            } catch (Exception exception) {
                ++i;
            }
        }

        System.out.println("Invalid Method: " + Arrays.asList(names));
        return null;
    }

    public static Field getField(Class c, String... names) {
        int l = names.length;
        int fern = 0;

        while (fern < l) {
            String b = names[fern];

            try {
                Field e = Field.class.getDeclaredField("modifiers");

                e.setAccessible(true);
                Field f = c.getDeclaredField(b);

                f.setAccessible(true);
                e.setInt(f, f.getModifiers() & -17);
                return f;
            } catch (Exception exception) {
                ++fern;
            }
        }

        System.out.println("Invalid Fields: " + Arrays.asList(names) + " For Class: " + c.getName());
        return null;
    }
}
