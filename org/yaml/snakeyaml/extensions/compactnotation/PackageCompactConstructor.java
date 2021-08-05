package org.yaml.snakeyaml.extensions.compactnotation;

public class PackageCompactConstructor extends CompactConstructor {

    private String packageName;

    public PackageCompactConstructor(String packageName) {
        this.packageName = packageName;
    }

    protected Class getClassForName(String name) throws ClassNotFoundException {
        if (name.indexOf(46) < 0) {
            try {
                Class clazz = Class.forName(this.packageName + "." + name);

                return clazz;
            } catch (ClassNotFoundException classnotfoundexception) {
                ;
            }
        }

        return super.getClassForName(name);
    }
}
