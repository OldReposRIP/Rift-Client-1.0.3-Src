package org.spongepowered.tools.obfuscation.mapping.mcp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.ObfuscationType;
import org.spongepowered.tools.obfuscation.mapping.IMappingConsumer;
import org.spongepowered.tools.obfuscation.mapping.common.MappingWriter;

public class MappingWriterSrg extends MappingWriter {

    public MappingWriterSrg(Messager messager, Filer filer) {
        super(messager, filer);
    }

    public void write(String output, ObfuscationType type, IMappingConsumer.MappingSet fields, IMappingConsumer.MappingSet methods) {
        if (output != null) {
            PrintWriter writer = null;

            try {
                writer = this.openFileWriter(output, type + " output SRGs");
                this.writeFieldMappings(writer, fields);
                this.writeMethodMappings(writer, methods);
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Exception exception) {
                        ;
                    }
                }

            }

        }
    }

    protected void writeFieldMappings(PrintWriter writer, IMappingConsumer.MappingSet fields) {
        Iterator iterator = fields.iterator();

        while (iterator.hasNext()) {
            IMappingConsumer.MappingSet.Pair field = (IMappingConsumer.MappingSet.Pair) iterator.next();

            writer.println(this.formatFieldMapping(field));
        }

    }

    protected void writeMethodMappings(PrintWriter writer, IMappingConsumer.MappingSet methods) {
        Iterator iterator = methods.iterator();

        while (iterator.hasNext()) {
            IMappingConsumer.MappingSet.Pair method = (IMappingConsumer.MappingSet.Pair) iterator.next();

            writer.println(this.formatMethodMapping(method));
        }

    }

    protected String formatFieldMapping(IMappingConsumer.MappingSet.Pair mapping) {
        return String.format("FD: %s/%s %s/%s", new Object[] { ((MappingField) mapping.from).getOwner(), ((MappingField) mapping.from).getName(), ((MappingField) mapping.to).getOwner(), ((MappingField) mapping.to).getName()});
    }

    protected String formatMethodMapping(IMappingConsumer.MappingSet.Pair mapping) {
        return String.format("MD: %s %s %s %s", new Object[] { ((MappingMethod) mapping.from).getName(), ((MappingMethod) mapping.from).getDesc(), ((MappingMethod) mapping.to).getName(), ((MappingMethod) mapping.to).getDesc()});
    }
}
