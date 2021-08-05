package org.reflections.serializers;

import java.io.File;
import java.io.InputStream;
import org.reflections.Reflections;

public interface Serializer {

    Reflections read(InputStream inputstream);

    File save(Reflections reflections, String s);

    String toString(Reflections reflections);
}
