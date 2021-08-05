package org.reflections.scanners;

import com.google.common.base.Joiner;
import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.MethodInfo;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import org.reflections.ReflectionsException;
import org.reflections.util.ClasspathHelper;

public class MemberUsageScanner extends AbstractScanner {

    private ClassPool classPool;

    public void scan(Object cls) {
        try {
            CtClass e = this.getClassPool().get(this.getMetadataAdapter().getClassName(cls));
            CtConstructor[] actconstructor = e.getDeclaredConstructors();
            int i = actconstructor.length;

            int j;

            for (j = 0; j < i; ++j) {
                CtConstructor member = actconstructor[j];

                this.scanMember(member);
            }

            CtMethod[] actmethod = e.getDeclaredMethods();

            i = actmethod.length;

            for (j = 0; j < i; ++j) {
                CtMethod ctmethod = actmethod[j];

                this.scanMember(ctmethod);
            }

            e.detach();
        } catch (Exception exception) {
            throw new ReflectionsException("Could not scan method usage for " + this.getMetadataAdapter().getClassName(cls), exception);
        }
    }

    void scanMember(CtBehavior member) throws CannotCompileException {
        final String key = member.getDeclaringClass().getName() + "." + member.getMethodInfo().getName() + "(" + this.nameeterNames(member.getMethodInfo()) + ")";

        member.instrument(new ExprEditor() {
            public void edit(NewExpr e) throws CannotCompileException {
                try {
                    MemberUsageScanner.this.put(e.getConstructor().getDeclaringClass().getName() + ".<init>(" + MemberUsageScanner.this.nameeterNames(e.getConstructor().getMethodInfo()) + ")", e.getLineNumber(), key);
                } catch (NotFoundException notfoundexception) {
                    throw new ReflectionsException("Could not find new instance usage in " + key, notfoundexception);
                }
            }

            public void edit(MethodCall m) throws CannotCompileException {
                try {
                    MemberUsageScanner.this.put(m.getMethod().getDeclaringClass().getName() + "." + m.getMethodName() + "(" + MemberUsageScanner.this.nameeterNames(m.getMethod().getMethodInfo()) + ")", m.getLineNumber(), key);
                } catch (NotFoundException notfoundexception) {
                    throw new ReflectionsException("Could not find member " + m.getClassName() + " in " + key, notfoundexception);
                }
            }

            public void edit(ConstructorCall c) throws CannotCompileException {
                try {
                    MemberUsageScanner.this.put(c.getConstructor().getDeclaringClass().getName() + ".<init>(" + MemberUsageScanner.this.nameeterNames(c.getConstructor().getMethodInfo()) + ")", c.getLineNumber(), key);
                } catch (NotFoundException notfoundexception) {
                    throw new ReflectionsException("Could not find member " + c.getClassName() + " in " + key, notfoundexception);
                }
            }

            public void edit(FieldAccess f) throws CannotCompileException {
                try {
                    MemberUsageScanner.this.put(f.getField().getDeclaringClass().getName() + "." + f.getFieldName(), f.getLineNumber(), key);
                } catch (NotFoundException notfoundexception) {
                    throw new ReflectionsException("Could not find member " + f.getFieldName() + " in " + key, notfoundexception);
                }
            }
        });
    }

    private void put(String key, int lineNumber, String value) {
        if (this.acceptResult(key)) {
            this.getStore().put(key, value + " #" + lineNumber);
        }

    }

    String nameeterNames(MethodInfo info) {
        return Joiner.on(", ").join(this.getMetadataAdapter().getParameterNames(info));
    }

    private ClassPool getClassPool() {
        if (this.classPool == null) {
            synchronized (this) {
                this.classPool = new ClassPool();
                ClassLoader[] classLoaders = this.getConfiguration().getClassLoaders();

                if (classLoaders == null) {
                    classLoaders = ClasspathHelper.classLoaders(new ClassLoader[0]);
                }

                ClassLoader[] aclassloader = classLoaders;
                int i = classLoaders.length;

                for (int j = 0; j < i; ++j) {
                    ClassLoader classLoader = aclassloader[j];

                    this.classPool.appendClassPath((ClassPath) (new LoaderClassPath(classLoader)));
                }
            }
        }

        return this.classPool;
    }
}
