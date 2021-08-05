package org.spongepowered.tools.obfuscation.mapping;

import java.io.File;
import java.io.IOException;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;

public interface IMappingProvider {

    void clear();

    boolean isEmpty();

    void read(File file) throws IOException;

    MappingMethod getMethodMapping(MappingMethod mappingmethod);

    MappingField getFieldMapping(MappingField mappingfield);

    String getClassMapping(String s);

    String getPackageMapping(String s);
}
