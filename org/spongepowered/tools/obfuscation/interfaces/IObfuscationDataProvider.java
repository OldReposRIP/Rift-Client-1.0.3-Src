package org.spongepowered.tools.obfuscation.interfaces;

import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.obfuscation.mapping.IMapping;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.ObfuscationData;
import org.spongepowered.tools.obfuscation.mirror.TypeHandle;

public interface IObfuscationDataProvider {

    ObfuscationData getObfEntryRecursive(MemberInfo memberinfo);

    ObfuscationData getObfEntry(MemberInfo memberinfo);

    ObfuscationData getObfEntry(IMapping imapping);

    ObfuscationData getObfMethodRecursive(MemberInfo memberinfo);

    ObfuscationData getObfMethod(MemberInfo memberinfo);

    ObfuscationData getRemappedMethod(MemberInfo memberinfo);

    ObfuscationData getObfMethod(MappingMethod mappingmethod);

    ObfuscationData getRemappedMethod(MappingMethod mappingmethod);

    ObfuscationData getObfFieldRecursive(MemberInfo memberinfo);

    ObfuscationData getObfField(MemberInfo memberinfo);

    ObfuscationData getObfField(MappingField mappingfield);

    ObfuscationData getObfClass(TypeHandle typehandle);

    ObfuscationData getObfClass(String s);
}
