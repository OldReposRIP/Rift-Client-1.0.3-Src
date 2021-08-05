package org.spongepowered.asm.mixin.transformer;

import java.util.Iterator;
import java.util.Map.Entry;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidInterfaceMixinException;

class MixinApplicatorInterface extends MixinApplicatorStandard {

    MixinApplicatorInterface(TargetClassContext context) {
        super(context);
    }

    protected void applyInterfaces(MixinTargetContext mixin) {
        Iterator iterator = mixin.getInterfaces().iterator();

        while (iterator.hasNext()) {
            String interfaceName = (String) iterator.next();

            if (!this.targetClass.name.equals(interfaceName) && !this.targetClass.interfaces.contains(interfaceName)) {
                this.targetClass.interfaces.add(interfaceName);
                mixin.getTargetClassInfo().addInterface(interfaceName);
            }
        }

    }

    protected void applyFields(MixinTargetContext mixin) {
        Iterator iterator = mixin.getShadowFields().iterator();

        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            FieldNode shadow = (FieldNode) entry.getKey();

            this.logger.error("Ignoring redundant @Shadow field {}:{} in {}", new Object[] { shadow.name, shadow.desc, mixin});
        }

        this.mergeNewFields(mixin);
    }

    protected void applyInitialisers(MixinTargetContext mixin) {}

    protected void prepareInjections(MixinTargetContext mixin) {
        Iterator iterator = this.targetClass.methods.iterator();

        while (iterator.hasNext()) {
            MethodNode method = (MethodNode) iterator.next();

            try {
                InjectionInfo ex = InjectionInfo.parse(mixin, method);

                if (ex != null) {
                    throw new InvalidInterfaceMixinException(mixin, ex + " is not supported on interface mixin method " + method.name);
                }
            } catch (InvalidInjectionException invalidinjectionexception) {
                String description = invalidinjectionexception.getInjectionInfo() != null ? invalidinjectionexception.getInjectionInfo().toString() : "Injection";

                throw new InvalidInterfaceMixinException(mixin, description + " is not supported in interface mixin");
            }
        }

    }

    protected void applyInjections(MixinTargetContext mixin) {}
}
