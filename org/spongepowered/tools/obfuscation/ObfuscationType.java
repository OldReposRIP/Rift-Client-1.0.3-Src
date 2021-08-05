package org.spongepowered.tools.obfuscation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;
import org.spongepowered.tools.obfuscation.interfaces.IOptionProvider;
import org.spongepowered.tools.obfuscation.service.ObfuscationTypeDescriptor;

public final class ObfuscationType {

    private static final Map types = new LinkedHashMap();
    private final String key;
    private final ObfuscationTypeDescriptor descriptor;
    private final IMixinAnnotationProcessor ap;
    private final IOptionProvider options;

    private ObfuscationType(ObfuscationTypeDescriptor descriptor, IMixinAnnotationProcessor ap) {
        this.key = descriptor.getKey();
        this.descriptor = descriptor;
        this.ap = ap;
        this.options = ap;
    }

    public final ObfuscationEnvironment createEnvironment() {
        try {
            Class ex = this.descriptor.getEnvironmentType();
            Constructor ctor = ex.getDeclaredConstructor(new Class[] { ObfuscationType.class});

            ctor.setAccessible(true);
            return (ObfuscationEnvironment) ctor.newInstance(new Object[] { this});
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public String toString() {
        return this.key;
    }

    public String getKey() {
        return this.key;
    }

    public ObfuscationTypeDescriptor getConfig() {
        return this.descriptor;
    }

    public IMixinAnnotationProcessor getAnnotationProcessor() {
        return this.ap;
    }

    public boolean isDefault() {
        String defaultEnv = this.options.getOption("defaultObfuscationEnv");

        return defaultEnv == null && this.key.equals("name") || defaultEnv != null && this.key.equals(defaultEnv.toLowerCase());
    }

    public boolean isSupported() {
        return this.getInputFileNames().size() > 0;
    }

    public List getInputFileNames() {
        Builder builder = ImmutableList.builder();
        String inputFile = this.options.getOption(this.descriptor.getInputFileOption());

        if (inputFile != null) {
            builder.add(inputFile);
        }

        String extraInputFiles = this.options.getOption(this.descriptor.getExtraInputFilesOption());

        if (extraInputFiles != null) {
            String[] astring = extraInputFiles.split(";");
            int i = astring.length;

            for (int j = 0; j < i; ++j) {
                String extraInputFile = astring[j];

                builder.add(extraInputFile.trim());
            }
        }

        return builder.build();
    }

    public String getOutputFileName() {
        return this.options.getOption(this.descriptor.getOutputFileOption());
    }

    public static Iterable types() {
        return ObfuscationType.types.values();
    }

    public static ObfuscationType create(ObfuscationTypeDescriptor descriptor, IMixinAnnotationProcessor ap) {
        String key = descriptor.getKey();

        if (ObfuscationType.types.containsKey(key)) {
            throw new IllegalArgumentException("Obfuscation type with key " + key + " was already registered");
        } else {
            ObfuscationType type = new ObfuscationType(descriptor, ap);

            ObfuscationType.types.put(key, type);
            return type;
        }
    }

    public static ObfuscationType get(String key) {
        ObfuscationType type = (ObfuscationType) ObfuscationType.types.get(key);

        if (type == null) {
            throw new IllegalArgumentException("Obfuscation type with key " + key + " was not registered");
        } else {
            return type;
        }
    }
}
