package org.spongepowered.asm.mixin;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.mixin.transformer.Config;

public final class Mixins {

    private static final Logger logger = LogManager.getLogger("mixin");
    private static final String CONFIGS_KEY = "mixin.configs.queue";
    private static final Set errorHandlers = new LinkedHashSet();

    public static void addConfigurations(String... configFiles) {
        MixinEnvironment fallback = MixinEnvironment.getDefaultEnvironment();
        String[] astring = configFiles;
        int i = configFiles.length;

        for (int j = 0; j < i; ++j) {
            String configFile = astring[j];

            createConfiguration(configFile, fallback);
        }

    }

    public static void addConfiguration(String configFile) {
        createConfiguration(configFile, MixinEnvironment.getDefaultEnvironment());
    }

    /** @deprecated */
    @Deprecated
    static void addConfiguration(String configFile, MixinEnvironment fallback) {
        createConfiguration(configFile, fallback);
    }

    private static void createConfiguration(String configFile, MixinEnvironment fallback) {
        Config config = null;

        try {
            config = Config.create(configFile, fallback);
        } catch (Exception exception) {
            Mixins.logger.error("Error encountered reading mixin config " + configFile + ": " + exception.getClass().getName() + " " + exception.getMessage(), exception);
        }

        registerConfiguration(config);
    }

    private static void registerConfiguration(Config config) {
        if (config != null) {
            MixinEnvironment env = config.getEnvironment();

            if (env != null) {
                env.registerConfig(config.getName());
            }

            getConfigs().add(config);
        }
    }

    public static int getUnvisitedCount() {
        int count = 0;
        Iterator iterator = getConfigs().iterator();

        while (iterator.hasNext()) {
            Config config = (Config) iterator.next();

            if (!config.isVisited()) {
                ++count;
            }
        }

        return count;
    }

    public static Set getConfigs() {
        Object mixinConfigs = (Set) GlobalProperties.get("mixin.configs.queue");

        if (mixinConfigs == null) {
            mixinConfigs = new LinkedHashSet();
            GlobalProperties.put("mixin.configs.queue", mixinConfigs);
        }

        return (Set) mixinConfigs;
    }

    public static void registerErrorHandlerClass(String handlerName) {
        if (handlerName != null) {
            Mixins.errorHandlers.add(handlerName);
        }

    }

    public static Set getErrorHandlerClasses() {
        return Collections.unmodifiableSet(Mixins.errorHandlers);
    }
}
