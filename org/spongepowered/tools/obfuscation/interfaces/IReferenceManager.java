package org.spongepowered.tools.obfuscation.interfaces;

import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.mixin.refmap.ReferenceMapper;
import org.spongepowered.tools.obfuscation.ObfuscationData;

public interface IReferenceManager {

    void setAllowConflicts(boolean flag);

    boolean getAllowConflicts();

    void write();

    ReferenceMapper getMapper();

    void addMethodMapping(String s, String s1, ObfuscationData obfuscationdata);

    void addMethodMapping(String s, String s1, MemberInfo memberinfo, ObfuscationData obfuscationdata);

    void addFieldMapping(String s, String s1, MemberInfo memberinfo, ObfuscationData obfuscationdata);

    void addClassMapping(String s, String s1, ObfuscationData obfuscationdata);
}
