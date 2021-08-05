package org.spongepowered.asm.launch.platform;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.service.MixinService;

public class MixinContainer {

    private static final List agentClasses = new ArrayList();
    private final Logger logger = LogManager.getLogger("mixin");
    private final URI uri;
    private final List agents = new ArrayList();

    public MixinContainer(MixinPlatformManager manager, URI uri) {
        this.uri = uri;
        Iterator iterator = MixinContainer.agentClasses.iterator();

        while (iterator.hasNext()) {
            String agentClass = (String) iterator.next();

            try {
                Class ex = Class.forName(agentClass);
                Constructor ctor = ex.getDeclaredConstructor(new Class[] { MixinPlatformManager.class, URI.class});

                this.logger.debug("Instancing new {} for {}", new Object[] { ex.getSimpleName(), this.uri});
                IMixinPlatformAgent agent = (IMixinPlatformAgent) ctor.newInstance(new Object[] { manager, uri});

                this.agents.add(agent);
            } catch (Exception exception) {
                this.logger.catching(exception);
            }
        }

    }

    public URI getURI() {
        return this.uri;
    }

    public Collection getPhaseProviders() {
        ArrayList phaseProviders = new ArrayList();
        Iterator iterator = this.agents.iterator();

        while (iterator.hasNext()) {
            IMixinPlatformAgent agent = (IMixinPlatformAgent) iterator.next();
            String phaseProvider = agent.getPhaseProvider();

            if (phaseProvider != null) {
                phaseProviders.add(phaseProvider);
            }
        }

        return phaseProviders;
    }

    public void prepare() {
        Iterator iterator = this.agents.iterator();

        while (iterator.hasNext()) {
            IMixinPlatformAgent agent = (IMixinPlatformAgent) iterator.next();

            this.logger.debug("Processing prepare() for {}", new Object[] { agent});
            agent.prepare();
        }

    }

    public void initPrimaryContainer() {
        Iterator iterator = this.agents.iterator();

        while (iterator.hasNext()) {
            IMixinPlatformAgent agent = (IMixinPlatformAgent) iterator.next();

            this.logger.debug("Processing launch tasks for {}", new Object[] { agent});
            agent.initPrimaryContainer();
        }

    }

    public void inject() {
        Iterator iterator = this.agents.iterator();

        while (iterator.hasNext()) {
            IMixinPlatformAgent agent = (IMixinPlatformAgent) iterator.next();

            this.logger.debug("Processing inject() for {}", new Object[] { agent});
            agent.inject();
        }

    }

    public String getLaunchTarget() {
        Iterator iterator = this.agents.iterator();

        String launchTarget;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            IMixinPlatformAgent agent = (IMixinPlatformAgent) iterator.next();

            launchTarget = agent.getLaunchTarget();
        } while (launchTarget == null);

        return launchTarget;
    }

    static {
        GlobalProperties.put("mixin.agents", MixinContainer.agentClasses);
        Iterator iterator = MixinService.getService().getPlatformAgents().iterator();

        while (iterator.hasNext()) {
            String agent = (String) iterator.next();

            MixinContainer.agentClasses.add(agent);
        }

        MixinContainer.agentClasses.add("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
    }
}
