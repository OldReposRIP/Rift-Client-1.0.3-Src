package org.spongepowered.tools.obfuscation;

import java.lang.reflect.Method;
import java.util.Iterator;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic.Kind;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;

class AnnotatedMixinElementHandlerOverwrite extends AnnotatedMixinElementHandler {

    AnnotatedMixinElementHandlerOverwrite(IMixinAnnotationProcessor ap, AnnotatedMixin mixin) {
        super(ap, mixin);
    }

    public void registerMerge(ExecutableElement method) {
        this.validateTargetMethod(method, (AnnotationHandle) null, new AnnotatedMixinElementHandler.AliasedElementName(method, AnnotationHandle.MISSING), "overwrite", true, true);
    }

    public void registerOverwrite(AnnotatedMixinElementHandlerOverwrite.AnnotatedElementOverwrite elem) {
        AnnotatedMixinElementHandler.AliasedElementName name = new AnnotatedMixinElementHandler.AliasedElementName(elem.getElement(), elem.getAnnotation());

        this.validateTargetMethod((ExecutableElement) elem.getElement(), elem.getAnnotation(), name, "@Overwrite", true, false);
        this.checkConstraints((ExecutableElement) elem.getElement(), elem.getAnnotation());
        if (elem.shouldRemap()) {
            Iterator overwriteErrorKind = this.mixin.getTargets().iterator();

            while (overwriteErrorKind.hasNext()) {
                TypeHandle javadoc = (TypeHandle) overwriteErrorKind.next();

                if (!this.registerOverwriteForTarget(elem, javadoc)) {
                    return;
                }
            }
        }

        if (!"true".equalsIgnoreCase(this.ap.getOption("disableOverwriteChecker"))) {
            Kind overwriteErrorKind1 = "error".equalsIgnoreCase(this.ap.getOption("overwriteErrorLevel")) ? Kind.ERROR : Kind.WARNING;
            String javadoc1 = this.ap.getJavadocProvider().getJavadoc(elem.getElement());

            if (javadoc1 == null) {
                this.ap.printMessage(overwriteErrorKind1, "@Overwrite is missing javadoc comment", elem.getElement());
                return;
            }

            if (!javadoc1.toLowerCase().contains("@author")) {
                this.ap.printMessage(overwriteErrorKind1, "@Overwrite is missing an @author tag", elem.getElement());
            }

            if (!javadoc1.toLowerCase().contains("@reason")) {
                this.ap.printMessage(overwriteErrorKind1, "@Overwrite is missing an @reason tag", elem.getElement());
            }
        }

    }

    private boolean registerOverwriteForTarget(AnnotatedMixinElementHandlerOverwrite.AnnotatedElementOverwrite elem, TypeHandle target) {
        MappingMethod targetMethod = target.getMappingMethod(elem.getSimpleName(), elem.getDesc());
        ObfuscationData obfData = this.obf.getDataProvider().getObfMethod(targetMethod);

        if (obfData.isEmpty()) {
            Kind ex = Kind.ERROR;

            try {
                Method md = ((ExecutableElement) elem.getElement()).getClass().getMethod("isStatic", new Class[0]);

                if (((Boolean) md.invoke(elem.getElement(), new Object[0])).booleanValue()) {
                    ex = Kind.WARNING;
                }
            } catch (Exception exception) {
                ;
            }

            this.ap.printMessage(ex, "No obfuscation mapping for @Overwrite method", elem.getElement());
            return false;
        } else {
            try {
                this.addMethodMappings(elem.getSimpleName(), elem.getDesc(), obfData);
                return true;
            } catch (Mappings.MappingConflictException mappings_mappingconflictexception) {
                elem.printMessage(this.ap, Kind.ERROR, "Mapping conflict for @Overwrite method: " + mappings_mappingconflictexception.getNew().getSimpleName() + " for target " + target + " conflicts with existing mapping " + mappings_mappingconflictexception.getOld().getSimpleName());
                return false;
            }
        }
    }

    static class AnnotatedElementOverwrite extends AnnotatedMixinElementHandler.AnnotatedElement {

        private final boolean shouldRemap;

        public AnnotatedElementOverwrite(ExecutableElement element, AnnotationHandle annotation, boolean shouldRemap) {
            super(element, annotation);
            this.shouldRemap = shouldRemap;
        }

        public boolean shouldRemap() {
            return this.shouldRemap;
        }
    }
}
