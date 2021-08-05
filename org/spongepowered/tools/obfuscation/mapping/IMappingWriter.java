package org.spongepowered.tools.obfuscation.mapping;

import org.spongepowered.tools.obfuscation.ObfuscationType;

public interface IMappingWriter {

    void write(String s, ObfuscationType obfuscationtype, IMappingConsumer.MappingSet imappingconsumer_mappingset, IMappingConsumer.MappingSet imappingconsumer_mappingset1);
}
