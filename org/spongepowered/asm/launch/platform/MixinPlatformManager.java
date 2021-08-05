package org.spongepowered.asm.launch.platform;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.MixinService;

public class MixinPlatformManager {

    private static final String DEFAULT_MAIN_CLASS = "net.minecraft.client.main.Main";
    private static final String MIXIN_TWEAKER_CLASS = "org.spongepowered.asm.launch.MixinTweaker";
    private static final Logger logger = LogManager.getLogger("mixin");
    private final Map containers = new LinkedHashMap();
    private MixinContainer primaryContainer;
    private boolean prepared = false;
    private boolean injected;

    public void init() {
        MixinPlatformManager.logger.debug("Initialising Mixin Platform Manager");
        URI uri = null;

        try {
            uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            if (uri != null) {
                MixinPlatformManager.logger.debug("Mixin platform: primary container is {}", new Object[] { uri});
                this.primaryContainer = this.addContainer(uri);
            }
        } catch (URISyntaxException urisyntaxexception) {
            urisyntaxexception.printStackTrace();
        }

        this.scanClasspath();
    }

    public Collection getPhaseProviderClasses() {
        Collection phaseProviders = this.primaryContainer.getPhaseProviders();

        return (Collection) (phaseProviders != null ? Collections.unmodifiableCollection(phaseProviders) : Collections.emptyList());
    }

    public final MixinContainer addContainer(URI uri) {
        MixinContainer existingContainer = (MixinContainer) this.containers.get(uri);

        if (existingContainer != null) {
            return existingContainer;
        } else {
            MixinPlatformManager.logger.debug("Adding mixin platform agents for container {}", new Object[] { uri});
            MixinContainer container = new MixinContainer(this, uri);

            this.containers.put(uri, container);
            if (this.prepared) {
                container.prepare();
            }

            return container;
        }
    }

    public final void prepare(List args) {
        this.prepared = true;
        Iterator argv = this.containers.values().iterator();

        while (argv.hasNext()) {
            MixinContainer container = (MixinContainer) argv.next();

            container.prepare();
        }

        if (args != null) {
            this.parseArgs(args);
        } else {
            String argv1 = System.getProperty("sun.java.command");

            if (argv1 != null) {
                this.parseArgs(Arrays.asList(argv1.split(" ")));
            }
        }

    }

    private void parseArgs(List args) {
        boolean captureNext = false;

        String arg;

        for (Iterator iterator = args.iterator(); iterator.hasNext(); captureNext = "--mixin".equals(arg)) {
            arg = (String) iterator.next();
            if (captureNext) {
                this.addConfig(arg);
            }
        }

    }

    public final void inject() {
        if (!this.injected) {
            this.injected = true;
            if (this.primaryContainer != null) {
                this.primaryContainer.initPrimaryContainer();
            }

            this.scanClasspath();
            MixinPlatformManager.logger.debug("inject() running with {} agents", new Object[] { Integer.valueOf(this.containers.size())});
            Iterator iterator = this.containers.values().iterator();

            while (iterator.hasNext()) {
                MixinContainer container = (MixinContainer) iterator.next();

                try {
                    container.inject();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

        }
    }

    private void scanClasspath() {
        URL[] sources = MixinService.getService().getClassProvider().getClassPath();
        URL[] aurl = sources;
        int i = sources.length;

        for (int j = 0; j < i; ++j) {
            URL url = aurl[j];

            try {
                URI ex = url.toURI();

                if (!this.containers.containsKey(ex)) {
                    MixinPlatformManager.logger.debug("Scanning {} for mixin tweaker", new Object[] { ex});
                    if ("file".equals(ex.getScheme()) && (new File(ex)).exists()) {
                        MainAttributes attributes = MainAttributes.of(ex);
                        String tweaker = attributes.get("TweakClass");

                        if ("org.spongepowered.asm.launch.MixinTweaker".equals(tweaker)) {
                            MixinPlatformManager.logger.debug("{} contains a mixin tweaker, adding agents", new Object[] { ex});
                            this.addContainer(ex);
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

    }

    public String getLaunchTarget() {
        Iterator iterator = this.containers.values().iterator();

        String mainClass;

        do {
            if (!iterator.hasNext()) {
                return "net.minecraft.client.main.Main";
            }

            MixinContainer container = (MixinContainer) iterator.next();

            mainClass = container.getLaunchTarget();
        } while (mainClass == null);

        return mainClass;
    }

    final void setCompatibilityLevel(String level) {
        try {
            MixinEnvironment.CompatibilityLevel ex = MixinEnvironment.CompatibilityLevel.valueOf(level.toUpperCase());

            MixinPlatformManager.logger.debug("Setting mixin compatibility level: {}", new Object[] { ex});
            MixinEnvironment.setCompatibilityLevel(ex);
        } catch (IllegalArgumentException illegalargumentexception) {
            MixinPlatformManager.logger.warn("Invalid compatibility level specified: {}", new Object[] { level});
        }

    }

    final void addConfig(String config) {
        if (config.endsWith(".json")) {
            MixinPlatformManager.logger.debug("Registering mixin config: {}", new Object[] { config});
            Mixins.addConfiguration(config);
        } else if (config.contains(".json@")) {
            int pos = config.indexOf(".json@");
            String phaseName = config.substring(pos + 6);

            config = config.substring(0, pos + 5);
            MixinEnvironment.Phase phase = MixinEnvironment.Phase.forName(phaseName);

            if (phase != null) {
                MixinPlatformManager.logger.warn("Setting config phase via manifest is deprecated: {}. Specify target in config instead", new Object[] { config});
                MixinPlatformManager.logger.debug("Registering mixin config: {}", new Object[] { config});
                MixinEnvironment.getEnvironment(phase).addConfiguration(config);
            }
        }

    }

    final void addTokenProvider(String provider) {
        if (provider.contains("@")) {
            String[] parts = provider.split("@", 2);
            MixinEnvironment.Phase phase = MixinEnvironment.Phase.forName(parts[1]);

            if (phase != null) {
                MixinPlatformManager.logger.debug("Registering token provider class: {}", new Object[] { parts[0]});
                MixinEnvironment.getEnvironment(phase).registerTokenProviderClass(parts[0]);
            }

        } else {
            MixinEnvironment.getDefaultEnvironment().registerTokenProviderClass(provider);
        }
    }
}
