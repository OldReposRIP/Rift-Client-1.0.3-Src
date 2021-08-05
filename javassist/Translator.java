package javassist;

public interface Translator {

    void start(ClassPool classpool) throws NotFoundException, CannotCompileException;

    void onLoad(ClassPool classpool, String s) throws NotFoundException, CannotCompileException;
}
