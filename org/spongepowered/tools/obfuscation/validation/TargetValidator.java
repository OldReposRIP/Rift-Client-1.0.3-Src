package org.spongepowered.tools.obfuscation.validation;

import java.util.Collection;
import java.util.Iterator;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.tools.obfuscation.MixinValidator;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;
import org.spongepowered.tools.obfuscation.interfaces.IMixinValidator;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;
import org.spongepowered.tools.obfuscation.mirror.TypeUtils;

public class TargetValidator extends MixinValidator {

    public TargetValidator(IMixinAnnotationProcessor ap) {
        super(ap, IMixinValidator.ValidationPass.LATE);
    }

    public boolean validate(TypeElement mixin, AnnotationHandle annotation, Collection targets) {
        if ("true".equalsIgnoreCase(this.options.getOption("disableTargetValidator"))) {
            return true;
        } else {
            if (mixin.getKind() == ElementKind.INTERFACE) {
                this.validateInterfaceMixin(mixin, targets);
            } else {
                this.validateClassMixin(mixin, targets);
            }

            return true;
        }
    }

    private void validateInterfaceMixin(TypeElement mixin, Collection targets) {
        boolean containsNonAccessorMethod = false;
        Iterator iterator = mixin.getEnclosedElements().iterator();

        while (iterator.hasNext()) {
            Element target = (Element) iterator.next();

            if (target.getKind() == ElementKind.METHOD) {
                boolean targetType = AnnotationHandle.of(target, Accessor.class).exists();
                boolean isInvoker = AnnotationHandle.of(target, Invoker.class).exists();

                containsNonAccessorMethod |= !targetType && !isInvoker;
            }
        }

        if (containsNonAccessorMethod) {
            iterator = targets.iterator();

            while (iterator.hasNext()) {
                TypeHandle target1 = (TypeHandle) iterator.next();
                TypeElement targetType1 = target1.getElement();

                if (targetType1 != null && targetType1.getKind() != ElementKind.INTERFACE) {
                    this.error("Targetted type \'" + target1 + " of " + mixin + " is not an interface", mixin);
                }
            }

        }
    }

    private void validateClassMixin(TypeElement mixin, Collection targets) {
        TypeMirror superClass = mixin.getSuperclass();
        Iterator iterator = targets.iterator();

        while (iterator.hasNext()) {
            TypeHandle target = (TypeHandle) iterator.next();
            TypeMirror targetType = target.getType();

            if (targetType != null && !this.validateSuperClass(targetType, superClass)) {
                this.error("Superclass " + superClass + " of " + mixin + " was not found in the hierarchy of target class " + targetType, mixin);
            }
        }

    }

    private boolean validateSuperClass(TypeMirror targetType, TypeMirror superClass) {
        return TypeUtils.isAssignable(this.processingEnv, targetType, superClass) ? true : this.validateSuperClassRecursive(targetType, superClass);
    }

    private boolean validateSuperClassRecursive(TypeMirror targetType, TypeMirror superClass) {
        if (!(targetType instanceof DeclaredType)) {
            return false;
        } else if (TypeUtils.isAssignable(this.processingEnv, targetType, superClass)) {
            return true;
        } else {
            TypeElement targetElement = (TypeElement) ((DeclaredType) targetType).asElement();
            TypeMirror targetSuper = targetElement.getSuperclass();

            return targetSuper.getKind() == TypeKind.NONE ? false : (this.checkMixinsFor(targetSuper, superClass) ? true : this.validateSuperClassRecursive(targetSuper, superClass));
        }
    }

    private boolean checkMixinsFor(TypeMirror targetType, TypeMirror superClass) {
        Iterator iterator = this.getMixinsTargeting(targetType).iterator();

        TypeMirror mixinType;

        do {
            if (!iterator.hasNext()) {
                return false;
            }

            mixinType = (TypeMirror) iterator.next();
        } while (!TypeUtils.isAssignable(this.processingEnv, mixinType, superClass));

        return true;
    }
}
