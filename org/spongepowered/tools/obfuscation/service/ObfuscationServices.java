package org.spongepowered.tools.obfuscation.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import javax.tools.Diagnostic.Kind;
import org.spongepowered.tools.obfuscation.ObfuscationType;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;

public final class ObfuscationServices {

    private static ObfuscationServices instance;
    private final ServiceLoader serviceLoader = ServiceLoader.load(IObfuscationService.class, this.getClass().getClassLoader());
    private final Set services = new HashSet();

    public static ObfuscationServices getInstance() {
        if (ObfuscationServices.instance == null) {
            ObfuscationServices.instance = new ObfuscationServices();
        }

        return ObfuscationServices.instance;
    }

    public void initProviders(IMixinAnnotationProcessor ap) {
        try {
            Iterator serviceError = this.serviceLoader.iterator();

            while (serviceError.hasNext()) {
                IObfuscationService service = (IObfuscationService) serviceError.next();

                if (!this.services.contains(service)) {
                    this.services.add(service);
                    String serviceName = service.getClass().getSimpleName();
                    Collection obfTypes = service.getObfuscationTypes();

                    if (obfTypes != null) {
                        Iterator iterator = obfTypes.iterator();

                        while (iterator.hasNext()) {
                            ObfuscationTypeDescriptor obfType = (ObfuscationTypeDescriptor) iterator.next();

                            try {
                                ObfuscationType ex = ObfuscationType.create(obfType, ap);

                                ap.printMessage(Kind.NOTE, serviceName + " supports type: \"" + ex + "\"");
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (ServiceConfigurationError serviceconfigurationerror) {
            ap.printMessage(Kind.ERROR, serviceconfigurationerror.getClass().getSimpleName() + ": " + serviceconfigurationerror.getMessage());
            serviceconfigurationerror.printStackTrace();
        }

    }

    public Set getSupportedOptions() {
        HashSet supportedOptions = new HashSet();
        Iterator iterator = this.serviceLoader.iterator();

        while (iterator.hasNext()) {
            IObfuscationService provider = (IObfuscationService) iterator.next();
            Set options = provider.getSupportedOptions();

            if (options != null) {
                supportedOptions.addAll(options);
            }
        }

        return supportedOptions;
    }

    public IObfuscationService getService(Class serviceClass) {
        Iterator iterator = this.serviceLoader.iterator();

        IObfuscationService service;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            service = (IObfuscationService) iterator.next();
        } while (!serviceClass.getName().equals(service.getClass().getName()));

        return service;
    }
}
