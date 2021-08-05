package org.spongepowered.asm.launch.platform;

import java.net.URI;

public class MixinPlatformAgentDefault extends MixinPlatformAgentAbstract {

    public MixinPlatformAgentDefault(MixinPlatformManager manager, URI uri) {
        super(manager, uri);
    }

    public void prepare() {
        String compatibilityLevel = this.attributes.get("MixinCompatibilityLevel");

        if (compatibilityLevel != null) {
            this.manager.setCompatibilityLevel(compatibilityLevel);
        }

        String mixinConfigs = this.attributes.get("MixinConfigs");
        int i;

        if (mixinConfigs != null) {
            String[] tokenProviders = mixinConfigs.split(",");
            int j = tokenProviders.length;

            for (i = 0; i < j; ++i) {
                String config = tokenProviders[i];

                this.manager.addConfig(config.trim());
            }
        }

        String s = this.attributes.get("MixinTokenProviders");

        if (s != null) {
            String[] astring = s.split(",");

            i = astring.length;

            for (int k = 0; k < i; ++k) {
                String provider = astring[k];

                this.manager.addTokenProvider(provider.trim());
            }
        }

    }

    public void initPrimaryContainer() {}

    public void inject() {}

    public String getLaunchTarget() {
        return this.attributes.get("Main-Class");
    }
}
