package org.reflections.scanners;

import com.google.common.base.Joiner;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.reflections.adapters.MetadataAdapter;

public class MethodParameterNamesScanner extends AbstractScanner {

    public void scan(Object cls) {
        MetadataAdapter md = this.getMetadataAdapter();
        Iterator iterator = md.getMethods(cls).iterator();

        while (iterator.hasNext()) {
            Object method = iterator.next();
            String key = md.getMethodFullKey(cls, method);

            if (this.acceptResult(key)) {
                LocalVariableAttribute table = (LocalVariableAttribute) ((MethodInfo) method).getCodeAttribute().getAttribute("LocalVariableTable");
                int length = table.tableLength();
                int i = Modifier.isStatic(((MethodInfo) method).getAccessFlags()) ? 0 : 1;

                if (i < length) {
                    ArrayList names = new ArrayList(length - i);

                    while (i < length) {
                        names.add(((MethodInfo) method).getConstPool().getUtf8Info(table.nameIndex(i++)));
                    }

                    this.getStore().put(key, Joiner.on(", ").join(names));
                }
            }
        }

    }
}
