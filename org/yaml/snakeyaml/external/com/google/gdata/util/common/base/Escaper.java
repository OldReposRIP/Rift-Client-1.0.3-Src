package org.yaml.snakeyaml.external.com.google.gdata.util.common.base;

public interface Escaper {

    String escape(String s);

    Appendable escape(Appendable appendable);
}
