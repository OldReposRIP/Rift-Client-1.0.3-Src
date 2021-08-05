package org.spongepowered.tools.obfuscation;

import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic.Kind;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import org.spongepowered.tools.obfuscation.mirror.MethodHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeUtils;

public class AnnotatedMixinElementHandlerSoftImplements extends AnnotatedMixinElementHandler {

    AnnotatedMixinElementHandlerSoftImplements(IMixinAnnotationProcessor ap, AnnotatedMixin mixin) {
        super(ap, mixin);
    }

    public void process(AnnotationHandle implementsAnnotation) {
        if (this.mixin.remap()) {
            List interfaces = implementsAnnotation.getAnnotationList("value");

            if (interfaces.size() < 1) {
                this.ap.printMessage(Kind.WARNING, "Empty @Implements annotation", this.mixin.getMixin(), implementsAnnotation.asMirror());
            } else {
                Iterator iterator = interfaces.iterator();

                while (iterator.hasNext()) {
                    AnnotationHandle interfaceAnnotation = (AnnotationHandle) iterator.next();
                    Interface.Remap remap = (Interface.Remap) interfaceAnnotation.getValue("remap", Interface.Remap.ALL);

                    if (remap != Interface.Remap.NONE) {
                        try {
                            TypeHandle ex = new TypeHandle((DeclaredType) interfaceAnnotation.getValue("iface"));
                            String prefix = (String) interfaceAnnotation.getValue("prefix");

                            this.processSoftImplements(remap, ex, prefix);
                        } catch (Exception exception) {
                            this.ap.printMessage(Kind.ERROR, "Unexpected error: " + exception.getClass().getName() + ": " + exception.getMessage(), this.mixin.getMixin(), interfaceAnnotation.asMirror());
                        }
                    }
                }

            }
        }
    }

    private void processSoftImplements(Interface.Remap remap, TypeHandle iface, String prefix) {
        Iterator iterator = iface.getEnclosedElements(new ElementKind[] { ElementKind.METHOD}).iterator();

        while (iterator.hasNext()) {
            ExecutableElement superInterface = (ExecutableElement) iterator.next();

            this.processMethod(remap, iface, prefix, superInterface);
        }

        iterator = iface.getInterfaces().iterator();

        while (iterator.hasNext()) {
            TypeHandle superInterface1 = (TypeHandle) iterator.next();

            this.processSoftImplements(remap, superInterface1, prefix);
        }

    }

    private void processMethod(Interface.Remap remap, TypeHandle iface, String prefix, ExecutableElement method) {
        String name = method.getSimpleName().toString();
        String sig = TypeUtils.getJavaSignature((Element) method);
        String desc = TypeUtils.getDescriptor(method);
        MethodHandle prefixedMixinMethod;

        if (remap != Interface.Remap.ONLY_PREFIXED) {
            prefixedMixinMethod = this.mixin.getHandle().findMethod(name, sig);
            if (prefixedMixinMethod != null) {
                this.addInterfaceMethodMapping(remap, iface, (String) null, prefixedMixinMethod, name, desc);
            }
        }

        if (prefix != null) {
            prefixedMixinMethod = this.mixin.getHandle().findMethod(prefix + name, sig);
            if (prefixedMixinMethod != null) {
                this.addInterfaceMethodMapping(remap, iface, prefix, prefixedMixinMethod, name, desc);
            }
        }

    }

    private void addInterfaceMethodMapping(Interface.Remap remap, TypeHandle iface, String prefix, MethodHandle method, String name, String desc) {
        MappingMethod mapping = new MappingMethod(iface.getName(), name, desc);
        ObfuscationData obfData = this.obf.getDataProvider().getObfMethod(mapping);

        if (obfData.isEmpty()) {
            if (remap.forceRemap()) {
                this.ap.printMessage(Kind.ERROR, "No obfuscation mapping for soft-implementing method", method.getElement());
            }

        } else {
            this.addMethodMappings(method.getName(), desc, this.applyPrefix(obfData, prefix));
        }
    }

    private ObfuscationData applyPrefix(ObfuscationData data, String prefix) {
        if (prefix == null) {
            return data;
        } else {
            ObfuscationData prefixed = new ObfuscationData();
            Iterator iterator = data.iterator();

            while (iterator.hasNext()) {
                ObfuscationType type = (ObfuscationType) iterator.next();
                MappingMethod mapping = (MappingMethod) data.get(type);

                prefixed.put(type, mapping.addPrefix(prefix));
            }

            return prefixed;
        }
    }
}
