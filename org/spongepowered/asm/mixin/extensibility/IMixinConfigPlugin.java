package org.spongepowered.asm.mixin.extensibility;

import java.util.List;
import java.util.Set;
import org.spongepowered.asm.lib.tree.ClassNode;

public interface IMixinConfigPlugin {

    void onLoad(String s);

    String getRefMapperConfig();

    boolean shouldApplyMixin(String s, String s1);

    void acceptTargets(Set set, Set set1);

    List getMixins();

    void preApply(String s, ClassNode classnode, String s1, IMixinInfo imixininfo);

    void postApply(String s, ClassNode classnode, String s1, IMixinInfo imixininfo);
}
