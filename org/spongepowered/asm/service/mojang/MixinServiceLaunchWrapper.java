package org.spongepowered.asm.service.mojang;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.ILegacyClassTransformer;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.ITransformer;
import org.spongepowered.asm.util.ReEntranceLock;
import org.spongepowered.asm.util.perf.Profiler;

public class MixinServiceLaunchWrapper implements IMixinService, IClassProvider, IClassBytecodeProvider {

    public static final String BLACKBOARD_KEY_TWEAKCLASSES = "TweakClasses";
    public static final String BLACKBOARD_KEY_TWEAKS = "Tweaks";
    private static final String LAUNCH_PACKAGE = "org.spongepowered.asm.launch.";
    private static final String MIXIN_PACKAGE = "org.spongepowered.asm.mixin.";
    private static final String STATE_TWEAKER = "org.spongepowered.asm.mixin.EnvironmentStateTweaker";
    private static final String TRANSFORMER_PROXY_CLASS = "org.spongepowered.asm.mixin.transformer.Proxy";
    private static final Logger logger = LogManager.getLogger("mixin");
    private final LaunchClassLoaderUtil classLoaderUtil;
    private final ReEntranceLock lock;
    private IClassNameTransformer nameTransformer;

    public MixinServiceLaunchWrapper() {
        this.classLoaderUtil = new LaunchClassLoaderUtil(Launch.classLoader);
        this.lock = new ReEntranceLock(1);
    }

    public String getName() {
        return "LaunchWrapper";
    }

    public boolean isValid() {
        try {
            Launch.classLoader.hashCode();
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    public void prepare() {
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.asm.launch.");
    }

    public MixinEnvironment.Phase getInitialPhase() {
        return findInStackTrace("net.minecraft.launchwrapper.Launch", "launch") > 132 ? MixinEnvironment.Phase.DEFAULT : MixinEnvironment.Phase.PREINIT;
    }

    public void init() {
        if (findInStackTrace("net.minecraft.launchwrapper.Launch", "launch") < 4) {
            MixinServiceLaunchWrapper.logger.error("MixinBootstrap.doInit() called during a tweak constructor!");
        }

        List tweakClasses = (List) GlobalProperties.get("TweakClasses");

        if (tweakClasses != null) {
            tweakClasses.add("org.spongepowered.asm.mixin.EnvironmentStateTweaker");
        }

    }

    public ReEntranceLock getReEntranceLock() {
        return this.lock;
    }

    public Collection getPlatformAgents() {
        return ImmutableList.of("org.spongepowered.asm.launch.platform.MixinPlatformAgentFML");
    }

    public IClassProvider getClassProvider() {
        return this;
    }

    public IClassBytecodeProvider getBytecodeProvider() {
        return this;
    }

    public Class findClass(String name) throws ClassNotFoundException {
        return Launch.classLoader.findClass(name);
    }

    public Class findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Launch.classLoader);
    }

    public Class findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Launch.class.getClassLoader());
    }

    public void beginPhase() {
        Launch.classLoader.registerTransformer("org.spongepowered.asm.mixin.transformer.Proxy");
    }

    public void checkEnv(Object bootSource) {
        if (bootSource.getClass().getClassLoader() != Launch.class.getClassLoader()) {
            throw new MixinException("Attempted to init the mixin environment in the wrong classloader");
        }
    }

    public InputStream getResourceAsStream(String name) {
        return Launch.classLoader.getResourceAsStream(name);
    }

    public void registerInvalidClass(String className) {
        this.classLoaderUtil.registerInvalidClass(className);
    }

    public boolean isClassLoaded(String className) {
        return this.classLoaderUtil.isClassLoaded(className);
    }

    public String getClassRestrictions(String className) {
        String restrictions = "";

        if (this.classLoaderUtil.isClassClassLoaderExcluded(className, (String) null)) {
            restrictions = "PACKAGE_CLASSLOADER_EXCLUSION";
        }

        if (this.classLoaderUtil.isClassTransformerExcluded(className, (String) null)) {
            restrictions = (restrictions.length() > 0 ? restrictions + "," : "") + "PACKAGE_TRANSFORMER_EXCLUSION";
        }

        return restrictions;
    }

    public URL[] getClassPath() {
        return (URL[]) Launch.classLoader.getSources().toArray(new URL[0]);
    }

    public Collection getTransformers() {
        List transformers = Launch.classLoader.getTransformers();
        ArrayList wrapped = new ArrayList(transformers.size());
        Iterator iterator = transformers.iterator();

        while (iterator.hasNext()) {
            IClassTransformer transformer = (IClassTransformer) iterator.next();

            if (transformer instanceof ITransformer) {
                wrapped.add((ITransformer) transformer);
            } else {
                wrapped.add(new LegacyTransformerHandle(transformer));
            }

            if (transformer instanceof IClassNameTransformer) {
                MixinServiceLaunchWrapper.logger.debug("Found name transformer: {}", new Object[] { transformer.getClass().getName()});
                this.nameTransformer = (IClassNameTransformer) transformer;
            }
        }

        return wrapped;
    }

    public byte[] getClassBytes(String name, String transformedName) throws IOException {
        byte[] classBytes = Launch.classLoader.getClassBytes(name);

        if (classBytes != null) {
            return classBytes;
        } else {
            URLClassLoader appClassLoader = (URLClassLoader) Launch.class.getClassLoader();
            InputStream classStream = null;

            Object object;

            try {
                String ex = transformedName.replace('.', '/').concat(".class");

                classStream = appClassLoader.getResourceAsStream(ex);
                byte[] abyte = IOUtils.toByteArray(classStream);

                return abyte;
            } catch (Exception exception) {
                object = null;
            } finally {
                IOUtils.closeQuietly(classStream);
            }

            return (byte[]) object;
        }
    }

    public byte[] getClassBytes(String className, boolean runTransformers) throws ClassNotFoundException, IOException {
        String transformedName = className.replace('/', '.');
        String name = this.unmapClassName(transformedName);
        Profiler profiler = MixinEnvironment.getProfiler();
        Profiler.Section loadTime = profiler.begin(1, "class.load");
        byte[] classBytes = this.getClassBytes(name, transformedName);

        loadTime.end();
        if (runTransformers) {
            Profiler.Section transformTime = profiler.begin(1, "class.transform");

            classBytes = this.applyTransformers(name, transformedName, classBytes, profiler);
            transformTime.end();
        }

        if (classBytes == null) {
            throw new ClassNotFoundException(String.format("The specified class \'%s\' was not found", new Object[] { transformedName}));
        } else {
            return classBytes;
        }
    }

    private byte[] applyTransformers(String name, String transformedName, byte[] basicClass, Profiler profiler) {
        if (this.classLoaderUtil.isClassExcluded(name, transformedName)) {
            return basicClass;
        } else {
            MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
            Iterator iterator = environment.getTransformers().iterator();

            while (iterator.hasNext()) {
                ILegacyClassTransformer transformer = (ILegacyClassTransformer) iterator.next();

                this.lock.clear();
                int pos = transformer.getName().lastIndexOf(46);
                String simpleName = transformer.getName().substring(pos + 1);
                Profiler.Section transformTime = profiler.begin(2, simpleName.toLowerCase());

                transformTime.setInfo(transformer.getName());
                basicClass = transformer.transformClassBytes(name, transformedName, basicClass);
                transformTime.end();
                if (this.lock.isSet()) {
                    environment.addTransformerExclusion(transformer.getName());
                    this.lock.clear();
                    MixinServiceLaunchWrapper.logger.info("A re-entrant transformer \'{}\' was detected and will no longer process meta class data", new Object[] { transformer.getName()});
                }
            }

            return basicClass;
        }
    }

    private String unmapClassName(String className) {
        if (this.nameTransformer == null) {
            this.findNameTransformer();
        }

        return this.nameTransformer != null ? this.nameTransformer.unmapClassName(className) : className;
    }

    private void findNameTransformer() {
        List transformers = Launch.classLoader.getTransformers();
        Iterator iterator = transformers.iterator();

        while (iterator.hasNext()) {
            IClassTransformer transformer = (IClassTransformer) iterator.next();

            if (transformer instanceof IClassNameTransformer) {
                MixinServiceLaunchWrapper.logger.debug("Found name transformer: {}", new Object[] { transformer.getClass().getName()});
                this.nameTransformer = (IClassNameTransformer) transformer;
            }
        }

    }

    public ClassNode getClassNode(String className) throws ClassNotFoundException, IOException {
        return this.getClassNode(this.getClassBytes(className, true), 0);
    }

    private ClassNode getClassNode(byte[] classBytes, int flags) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(classBytes);

        classReader.accept(classNode, flags);
        return classNode;
    }

    public final String getSideName() {
        Iterator name = ((List) GlobalProperties.get("Tweaks")).iterator();

        ITweaker tweaker;

        do {
            if (!name.hasNext()) {
                String name1 = this.getSideName("net.minecraftforge.fml.relauncher.FMLLaunchHandler", "side");

                if (name1 != null) {
                    return name1;
                }

                name1 = this.getSideName("cpw.mods.fml.relauncher.FMLLaunchHandler", "side");
                if (name1 != null) {
                    return name1;
                }

                name1 = this.getSideName("com.mumfrey.liteloader.launch.LiteLoaderTweaker", "getEnvironmentType");
                if (name1 != null) {
                    return name1;
                }

                return "UNKNOWN";
            }

            tweaker = (ITweaker) name.next();
            if (tweaker.getClass().getName().endsWith(".common.launcher.FMLServerTweaker")) {
                return "SERVER";
            }
        } while (!tweaker.getClass().getName().endsWith(".common.launcher.FMLTweaker"));

        return "CLIENT";
    }

    private String getSideName(String className, String methodName) {
        try {
            Class ex = Class.forName(className, false, Launch.classLoader);
            Method method = ex.getDeclaredMethod(methodName, new Class[0]);

            return ((Enum) method.invoke((Object) null, new Object[0])).name();
        } catch (Exception exception) {
            return null;
        }
    }

    private static int findInStackTrace(String className, String methodName) {
        Thread currentThread = Thread.currentThread();

        if (!"main".equals(currentThread.getName())) {
            return 0;
        } else {
            StackTraceElement[] stackTrace = currentThread.getStackTrace();
            StackTraceElement[] astacktraceelement = stackTrace;
            int i = stackTrace.length;

            for (int j = 0; j < i; ++j) {
                StackTraceElement s = astacktraceelement[j];

                if (className.equals(s.getClassName()) && methodName.equals(s.getMethodName())) {
                    return s.getLineNumber();
                }
            }

            return 0;
        }
    }
}
