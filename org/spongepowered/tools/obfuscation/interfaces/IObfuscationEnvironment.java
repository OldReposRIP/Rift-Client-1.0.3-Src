package org.spongepowered.tools.obfuscation.interfaces;

import java.util.Collection;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;

public interface IObfuscationEnvironment {

    MappingMethod getObfMethod(MemberInfo memberinfo);

    MappingMethod getObfMethod(MappingMethod mappingmethod);

    MappingMethod getObfMethod(MappingMethod mappingmethod, boolean flag);

    MappingField getObfField(MemberInfo memberinfo);

    MappingField getObfField(MappingField mappingfield);

    MappingField getObfField(MappingField mappingfield, boolean flag);

    String getObfClass(String s);

    MemberInfo remapDescriptor(MemberInfo memberinfo);

    String remapDescriptor(String s);

    void writeMappings(Collection collection);
}
