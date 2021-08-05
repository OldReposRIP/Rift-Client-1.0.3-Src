package org.spongepowered.asm.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MixinService {

    private static final Logger logger = LogManager.getLogger("mixin");
    private static MixinService instance;
    private ServiceLoader bootstrapServiceLoader;
    private final Set bootedServices = new HashSet();
    private ServiceLoader serviceLoader;
    private IMixinService service = null;

    private MixinService() {
        this.runBootServices();
    }

    private void runBootServices() {
        this.bootstrapServiceLoader = ServiceLoader.load(IMixinServiceBootstrap.class, this.getClass().getClassLoader());
        Iterator iterator = this.bootstrapServiceLoader.iterator();

        while (iterator.hasNext()) {
            IMixinServiceBootstrap bootService = (IMixinServiceBootstrap) iterator.next();

            try {
                bootService.bootstrap();
                this.bootedServices.add(bootService.getServiceClassName());
            } catch (Throwable throwable) {
                MixinService.logger.catching(throwable);
            }
        }

    }

    private static MixinService getInstance() {
        if (MixinService.instance == null) {
            MixinService.instance = new MixinService();
        }

        return MixinService.instance;
    }

    public static void boot() {
        getInstance();
    }

    public static IMixinService getService() {
        return getInstance().getServiceInstance();
    }

    private synchronized IMixinService getServiceInstance() {
        if (this.service == null) {
            this.service = this.initService();
            if (this.service == null) {
                throw new ServiceNotAvailableError("No mixin host service is available");
            }
        }

        return this.service;
    }

    private IMixinService initService() {
        this.serviceLoader = ServiceLoader.load(IMixinService.class, this.getClass().getClassLoader());
        Iterator iter = this.serviceLoader.iterator();

        while (iter.hasNext()) {
            try {
                IMixinService th = (IMixinService) iter.next();

                if (this.bootedServices.contains(th.getClass().getName())) {
                    MixinService.logger.debug("MixinService [{}] was successfully booted in {}", new Object[] { th.getName(), this.getClass().getClassLoader()});
                }

                if (th.isValid()) {
                    return th;
                }
            } catch (ServiceConfigurationError serviceconfigurationerror) {
                serviceconfigurationerror.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        return null;
    }
}
