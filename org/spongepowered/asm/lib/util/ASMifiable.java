package org.spongepowered.asm.lib.util;

import java.util.Map;

public interface ASMifiable {

    void asmify(StringBuffer stringbuffer, String s, Map map);
}
