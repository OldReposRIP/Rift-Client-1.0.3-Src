package org.yaml.snakeyaml;

public class LoaderOptions {

    private boolean allowDuplicateKeys = true;

    public boolean isAllowDuplicateKeys() {
        return this.allowDuplicateKeys;
    }

    public void setAllowDuplicateKeys(boolean allowDuplicateKeys) {
        this.allowDuplicateKeys = allowDuplicateKeys;
    }
}
