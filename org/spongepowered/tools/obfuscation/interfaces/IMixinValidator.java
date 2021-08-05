package org.spongepowered.tools.obfuscation.interfaces;

import java.util.Collection;
import javax.lang.model.element.TypeElement;
import org.spongepowered.tools.obfuscation.mirror.AnnotationHandle;

public interface IMixinValidator {

    boolean validate(IMixinValidator.ValidationPass imixinvalidator_validationpass, TypeElement typeelement, AnnotationHandle annotationhandle, Collection collection);

    public static enum ValidationPass {

        EARLY, LATE, FINAL;
    }
}
