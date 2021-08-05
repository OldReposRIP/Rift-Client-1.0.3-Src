package org.spongepowered.asm.mixin.transformer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.ArgsClassGenerator;
import org.spongepowered.asm.mixin.throwables.ClassAlreadyLoadedException;
import org.spongepowered.asm.mixin.throwables.MixinApplyError;
import org.spongepowered.asm.mixin.throwables.MixinException;
import org.spongepowered.asm.mixin.throwables.MixinPrepareError;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IClassGenerator;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.IHotSwap;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionCheckClass;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionCheckInterfaces;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionClassExporter;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.spongepowered.asm.mixin.transformer.throwables.MixinTransformerError;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.ITransformer;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.transformers.TreeTransformer;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.asm.util.ReEntranceLock;
import org.spongepowered.asm.util.perf.Profiler;

public class MixinTransformer extends TreeTransformer {

    private static final String MIXIN_AGENT_CLASS = "org.spongepowered.tools.agent.MixinAgent";
    private static final String METRONOME_AGENT_CLASS = "org.spongepowered.metronome.Agent";
    static final Logger logger = LogManager.getLogger("mixin");
    private final IMixinService service = MixinService.getService();
    private final List configs = new ArrayList();
    private final List pendingConfigs = new ArrayList();
    private final ReEntranceLock lock;
    private final String sessionId = UUID.randomUUID().toString();
    private final Extensions extensions;
    private final IHotSwap hotSwapper;
    private final MixinPostProcessor postProcessor;
    private final Profiler profiler;
    private MixinEnvironment currentEnvironment;
    private Level verboseLoggingLevel;
    private boolean errorState;
    private int transformedCount;

    MixinTransformer() {
        this.verboseLoggingLevel = Level.DEBUG;
        this.errorState = false;
        this.transformedCount = 0;
        MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
        Object globalMixinTransformer = environment.getActiveTransformer();

        if (globalMixinTransformer instanceof ITransformer) {
            throw new MixinException("Terminating MixinTransformer instance " + this);
        } else {
            environment.setActiveTransformer(this);
            this.lock = this.service.getReEntranceLock();
            this.extensions = new Extensions(this);
            this.hotSwapper = this.initHotSwapper(environment);
            this.postProcessor = new MixinPostProcessor();
            this.extensions.add((IClassGenerator) (new ArgsClassGenerator()));
            this.extensions.add((IClassGenerator) (new InnerClassGenerator()));
            this.extensions.add((IExtension) (new ExtensionClassExporter(environment)));
            this.extensions.add((IExtension) (new ExtensionCheckClass()));
            this.extensions.add((IExtension) (new ExtensionCheckInterfaces()));
            this.profiler = MixinEnvironment.getProfiler();
        }
    }

    private IHotSwap initHotSwapper(MixinEnvironment environment) {
        if (!environment.getOption(MixinEnvironment.Option.HOT_SWAP)) {
            return null;
        } else {
            try {
                MixinTransformer.logger.info("Attempting to load Hot-Swap agent");
                Class th = Class.forName("org.spongepowered.tools.agent.MixinAgent");
                Constructor ctor = th.getDeclaredConstructor(new Class[] { MixinTransformer.class});

                return (IHotSwap) ctor.newInstance(new Object[] { this});
            } catch (Throwable throwable) {
                MixinTransformer.logger.info("Hot-swap agent could not be loaded, hot swapping of mixins won\'t work. {}: {}", new Object[] { throwable.getClass().getSimpleName(), throwable.getMessage()});
                return null;
            }
        }
    }

    public void audit(MixinEnvironment environment) {
        HashSet unhandled = new HashSet();
        Iterator auditLogger = this.configs.iterator();

        while (auditLogger.hasNext()) {
            MixinConfig config = (MixinConfig) auditLogger.next();

            unhandled.addAll(config.getUnhandledTargets());
        }

        Logger auditLogger1 = LogManager.getLogger("mixin/audit");
        Iterator config2 = unhandled.iterator();

        while (config2.hasNext()) {
            String config1 = (String) config2.next();

            try {
                auditLogger1.info("Force-loading class {}", new Object[] { config1});
                this.service.getClassProvider().findClass(config1, true);
            } catch (ClassNotFoundException classnotfoundexception) {
                auditLogger1.error("Could not force-load " + config1, classnotfoundexception);
            }
        }

        config2 = this.configs.iterator();

        while (config2.hasNext()) {
            MixinConfig config3 = (MixinConfig) config2.next();
            Iterator ex = config3.getUnhandledTargets().iterator();

            while (ex.hasNext()) {
                String target = (String) ex.next();
                ClassAlreadyLoadedException ex1 = new ClassAlreadyLoadedException(target + " was already classloaded");

                auditLogger1.error("Could not force-load " + target, ex1);
            }
        }

        if (environment.getOption(MixinEnvironment.Option.DEBUG_PROFILER)) {
            this.printProfilerSummary();
        }

    }

    private void printProfilerSummary() {
        DecimalFormat threedp = new DecimalFormat("(###0.000");
        DecimalFormat onedp = new DecimalFormat("(###0.0");
        PrettyPrinter printer = this.profiler.printer(false, false);
        long prepareTime = this.profiler.get("mixin.prepare").getTotalTime();
        long readTime = this.profiler.get("mixin.read").getTotalTime();
        long applyTime = this.profiler.get("mixin.apply").getTotalTime();
        long writeTime = this.profiler.get("mixin.write").getTotalTime();
        long totalMixinTime = this.profiler.get("mixin").getTotalTime();
        long loadTime = this.profiler.get("class.load").getTotalTime();
        long transformTime = this.profiler.get("class.transform").getTotalTime();
        long exportTime = this.profiler.get("mixin.debug.export").getTotalTime();
        long actualTime = totalMixinTime - loadTime - transformTime - exportTime;
        double timeSliceMixin = (double) actualTime / (double) totalMixinTime * 100.0D;
        double timeSliceLoad = (double) loadTime / (double) totalMixinTime * 100.0D;
        double timeSliceTransform = (double) transformTime / (double) totalMixinTime * 100.0D;
        double timeSliceExport = (double) exportTime / (double) totalMixinTime * 100.0D;
        long worstTransformerTime = 0L;
        Profiler.Section worstTransformer = null;
        Iterator format = this.profiler.getSections().iterator();

        while (format.hasNext()) {
            Profiler.Section agent = (Profiler.Section) format.next();
            long mdGetTimes = agent.getName().startsWith("class.transform.") ? agent.getTotalTime() : 0L;

            if (mdGetTimes > worstTransformerTime) {
                worstTransformerTime = mdGetTimes;
                worstTransformer = agent;
            }
        }

        printer.hr().add("Summary").hr().add();
        String format1 = "%9d ms %12s seconds)";

        printer.kv("Total mixin time", format1, new Object[] { Long.valueOf(totalMixinTime), threedp.format((double) totalMixinTime * 0.001D)}).add();
        printer.kv("Preparing mixins", format1, new Object[] { Long.valueOf(prepareTime), threedp.format((double) prepareTime * 0.001D)});
        printer.kv("Reading input", format1, new Object[] { Long.valueOf(readTime), threedp.format((double) readTime * 0.001D)});
        printer.kv("Applying mixins", format1, new Object[] { Long.valueOf(applyTime), threedp.format((double) applyTime * 0.001D)});
        printer.kv("Writing output", format1, new Object[] { Long.valueOf(writeTime), threedp.format((double) writeTime * 0.001D)}).add();
        printer.kv("of which", "");
        printer.kv("Time spent loading from disk", format1, new Object[] { Long.valueOf(loadTime), threedp.format((double) loadTime * 0.001D)});
        printer.kv("Time spent transforming classes", format1, new Object[] { Long.valueOf(transformTime), threedp.format((double) transformTime * 0.001D)}).add();
        if (worstTransformer != null) {
            printer.kv("Worst transformer", worstTransformer.getName());
            printer.kv("Class", worstTransformer.getInfo());
            printer.kv("Time spent", "%s seconds", new Object[] { Double.valueOf(worstTransformer.getTotalSeconds())});
            printer.kv("called", "%d times", new Object[] { Integer.valueOf(worstTransformer.getTotalCount())}).add();
        }

        printer.kv("   Time allocation:     Processing mixins", "%9d ms %10s%% of total)", new Object[] { Long.valueOf(actualTime), onedp.format(timeSliceMixin)});
        printer.kv("Loading classes", "%9d ms %10s%% of total)", new Object[] { Long.valueOf(loadTime), onedp.format(timeSliceLoad)});
        printer.kv("Running transformers", "%9d ms %10s%% of total)", new Object[] { Long.valueOf(transformTime), onedp.format(timeSliceTransform)});
        if (exportTime > 0L) {
            printer.kv("Exporting classes (debug)", "%9d ms %10s%% of total)", new Object[] { Long.valueOf(exportTime), onedp.format(timeSliceExport)});
        }

        printer.add();

        try {
            Class agent1 = this.service.getClassProvider().findAgentClass("org.spongepowered.metronome.Agent", false);
            Method mdGetTimes1 = agent1.getDeclaredMethod("getTimes", new Class[0]);
            Map times = (Map) mdGetTimes1.invoke((Object) null, new Object[0]);

            printer.hr().add("Transformer Times").hr().add();
            int longest = 10;

            Iterator iterator;
            Entry entry;

            for (iterator = times.entrySet().iterator(); iterator.hasNext(); longest = Math.max(longest, ((String) entry.getKey()).length())) {
                entry = (Entry) iterator.next();
            }

            iterator = times.entrySet().iterator();

            while (iterator.hasNext()) {
                entry = (Entry) iterator.next();
                String name = (String) entry.getKey();
                long mixinTime = 0L;
                Iterator iterator1 = this.profiler.getSections().iterator();

                while (iterator1.hasNext()) {
                    Profiler.Section section = (Profiler.Section) iterator1.next();

                    if (name.equals(section.getInfo())) {
                        mixinTime = section.getTotalTime();
                        break;
                    }
                }

                if (mixinTime > 0L) {
                    printer.add("%-" + longest + "s %8s ms %8s ms in mixin)", new Object[] { name, Long.valueOf(((Long) entry.getValue()).longValue() + mixinTime), "(" + mixinTime});
                } else {
                    printer.add("%-" + longest + "s %8s ms", new Object[] { name, entry.getValue()});
                }
            }

            printer.add();
        } catch (Throwable throwable) {
            ;
        }

        printer.print();
    }

    public String getName() {
        return this.getClass().getName();
    }

    public boolean isDelegationExcluded() {
        return true;
    }

    public synchronized byte[] transformClassBytes(String name, String transformedName, byte[] basicClass) {
        if (transformedName != null && !this.errorState) {
            MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
            Profiler.Section th;

            if (basicClass == null) {
                Iterator iterator = this.extensions.getGenerators().iterator();

                do {
                    if (!iterator.hasNext()) {
                        return basicClass;
                    }

                    IClassGenerator iclassgenerator = (IClassGenerator) iterator.next();

                    th = this.profiler.begin(new String[] { "generator", iclassgenerator.getClass().getSimpleName().toLowerCase()});
                    basicClass = iclassgenerator.generate(transformedName);
                    th.end();
                } while (basicClass == null);

                this.extensions.export(environment, transformedName.replace('.', '/'), false, basicClass);
                return basicClass;
            } else {
                boolean locked = this.lock.push().check();
                Profiler.Section mixinTimer = this.profiler.begin("mixin");

                if (!locked) {
                    try {
                        this.checkSelect(environment);
                    } catch (Exception exception) {
                        this.lock.pop();
                        mixinTimer.end();
                        throw new MixinException(exception);
                    }
                }

                byte[] th1;

                try {
                    if (!this.postProcessor.canTransform(transformedName)) {
                        TreeSet treeset = null;
                        boolean flag = false;
                        Iterator iterator1 = this.configs.iterator();

                        while (iterator1.hasNext()) {
                            MixinConfig targetClassNode = (MixinConfig) iterator1.next();

                            if (targetClassNode.packageMatch(transformedName)) {
                                flag = true;
                            } else if (targetClassNode.hasMixinsFor(transformedName)) {
                                if (treeset == null) {
                                    treeset = new TreeSet();
                                }

                                treeset.addAll(targetClassNode.getMixinsFor(transformedName));
                            }
                        }

                        if (flag) {
                            throw new NoClassDefFoundError(String.format("%s is a mixin class and cannot be referenced directly", new Object[] { transformedName}));
                        }

                        if (treeset != null) {
                            if (locked) {
                                MixinTransformer.logger.warn("Re-entrance detected, this will cause serious problems.", new MixinException());
                                throw new MixinApplyError("Re-entrance error.");
                            }

                            if (this.hotSwapper != null) {
                                this.hotSwapper.registerTargetClass(transformedName, basicClass);
                            }

                            try {
                                Profiler.Section profiler_section = this.profiler.begin("read");
                                ClassNode classnode = this.readClass(basicClass, true);
                                TargetClassContext context = new TargetClassContext(environment, this.extensions, this.sessionId, transformedName, classnode, treeset);

                                profiler_section.end();
                                basicClass = this.applyMixins(environment, context);
                                ++this.transformedCount;
                            } catch (InvalidMixinException invalidmixinexception) {
                                this.dumpClassOnFailure(transformedName, basicClass, environment);
                                this.handleMixinApplyError(transformedName, invalidmixinexception, environment);
                            }
                        }

                        th1 = basicClass;
                        return th1;
                    }

                    th = this.profiler.begin("postprocessor");
                    byte[] invalidRef = this.postProcessor.transformClassBytes(name, transformedName, basicClass);

                    th.end();
                    this.extensions.export(environment, transformedName, false, invalidRef);
                    th1 = invalidRef;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    this.dumpClassOnFailure(transformedName, basicClass, environment);
                    throw new MixinTransformerError("An unexpected critical error was encountered", throwable);
                } finally {
                    this.lock.pop();
                    mixinTimer.end();
                }

                return th1;
            }
        } else {
            return basicClass;
        }
    }

    public List reload(String mixinClass, byte[] bytes) {
        if (this.lock.getDepth() > 0) {
            throw new MixinApplyError("Cannot reload mixin if re-entrant lock entered");
        } else {
            ArrayList targets = new ArrayList();
            Iterator iterator = this.configs.iterator();

            while (iterator.hasNext()) {
                MixinConfig config = (MixinConfig) iterator.next();

                targets.addAll(config.reloadMixin(mixinClass, bytes));
            }

            return targets;
        }
    }

    private void checkSelect(MixinEnvironment environment) {
        if (this.currentEnvironment != environment) {
            this.select(environment);
        } else {
            int unvisitedCount = Mixins.getUnvisitedCount();

            if (unvisitedCount > 0 && this.transformedCount == 0) {
                this.select(environment);
            }

        }
    }

    private void select(MixinEnvironment environment) {
        this.verboseLoggingLevel = environment.getOption(MixinEnvironment.Option.DEBUG_VERBOSE) ? Level.INFO : Level.DEBUG;
        if (this.transformedCount > 0) {
            MixinTransformer.logger.log(this.verboseLoggingLevel, "Ending {}, applied {} mixins", new Object[] { this.currentEnvironment, Integer.valueOf(this.transformedCount)});
        }

        String action = this.currentEnvironment == environment ? "Checking for additional" : "Preparing";

        MixinTransformer.logger.log(this.verboseLoggingLevel, "{} mixins for {}", new Object[] { action, environment});
        this.profiler.setActive(true);
        this.profiler.mark(environment.getPhase().toString() + ":prepare");
        Profiler.Section prepareTimer = this.profiler.begin("prepare");

        this.selectConfigs(environment);
        this.extensions.select(environment);
        int totalMixins = this.prepareConfigs(environment);

        this.currentEnvironment = environment;
        this.transformedCount = 0;
        prepareTimer.end();
        long elapsedMs = prepareTimer.getTime();
        double elapsedTime = prepareTimer.getSeconds();

        if (elapsedTime > 0.25D) {
            long loadTime = this.profiler.get("class.load").getTime();
            long transformTime = this.profiler.get("class.transform").getTime();
            long pluginTime = this.profiler.get("mixin.plugin").getTime();
            String elapsed = (new DecimalFormat("###0.000")).format(elapsedTime);
            String perMixinTime = (new DecimalFormat("###0.0")).format((double) elapsedMs / (double) totalMixins);

            MixinTransformer.logger.log(this.verboseLoggingLevel, "Prepared {} mixins in {} sec ({}ms avg) ({}ms load, {}ms transform, {}ms plugin)", new Object[] { Integer.valueOf(totalMixins), elapsed, perMixinTime, Long.valueOf(loadTime), Long.valueOf(transformTime), Long.valueOf(pluginTime)});
        }

        this.profiler.mark(environment.getPhase().toString() + ":apply");
        this.profiler.setActive(environment.getOption(MixinEnvironment.Option.DEBUG_PROFILER));
    }

    private void selectConfigs(MixinEnvironment environment) {
        Iterator iter = Mixins.getConfigs().iterator();

        while (iter.hasNext()) {
            Config handle = (Config) iter.next();

            try {
                MixinConfig ex = handle.get();

                if (ex.select(environment)) {
                    iter.remove();
                    MixinTransformer.logger.log(this.verboseLoggingLevel, "Selecting config {}", new Object[] { ex});
                    ex.onSelect();
                    this.pendingConfigs.add(ex);
                }
            } catch (Exception exception) {
                MixinTransformer.logger.warn(String.format("Failed to select mixin config: %s", new Object[] { handle}), exception);
            }
        }

        Collections.sort(this.pendingConfigs);
    }

    private int prepareConfigs(MixinEnvironment environment) {
        int totalMixins = 0;
        final IHotSwap hotSwapper = this.hotSwapper;
        Iterator iterator = this.pendingConfigs.iterator();

        MixinConfig config;

        while (iterator.hasNext()) {
            config = (MixinConfig) iterator.next();
            config.addListener(this.postProcessor);
            if (hotSwapper != null) {
                config.addListener(new MixinConfig.IListener() {
                    public void onPrepare(MixinInfo mixin) {
                        hotSwapper.registerMixinClass(mixin.getClassName());
                    }

                    public void onInit(MixinInfo mixin) {}
                });
            }
        }

        iterator = this.pendingConfigs.iterator();

        String message;

        while (iterator.hasNext()) {
            config = (MixinConfig) iterator.next();

            try {
                MixinTransformer.logger.log(this.verboseLoggingLevel, "Preparing {} ({})", new Object[] { config, Integer.valueOf(config.getDeclaredMixinCount())});
                config.prepare();
                totalMixins += config.getMixinCount();
            } catch (InvalidMixinException invalidmixinexception) {
                this.handleMixinPrepareError(config, invalidmixinexception, environment);
            } catch (Exception exception) {
                message = exception.getMessage();
                MixinTransformer.logger.error("Error encountered whilst initialising mixin config \'" + config.getName() + "\': " + message, exception);
            }
        }

        iterator = this.pendingConfigs.iterator();

        while (iterator.hasNext()) {
            config = (MixinConfig) iterator.next();
            IMixinConfigPlugin ex = config.getPlugin();

            if (ex != null) {
                HashSet message1 = new HashSet();
                Iterator iterator1 = this.pendingConfigs.iterator();

                while (iterator1.hasNext()) {
                    MixinConfig otherConfig = (MixinConfig) iterator1.next();

                    if (!otherConfig.equals(config)) {
                        message1.addAll(otherConfig.getTargets());
                    }
                }

                ex.acceptTargets(config.getTargets(), Collections.unmodifiableSet(message1));
            }
        }

        iterator = this.pendingConfigs.iterator();

        while (iterator.hasNext()) {
            config = (MixinConfig) iterator.next();

            try {
                config.postInitialise();
            } catch (InvalidMixinException invalidmixinexception1) {
                this.handleMixinPrepareError(config, invalidmixinexception1, environment);
            } catch (Exception exception1) {
                message = exception1.getMessage();
                MixinTransformer.logger.error("Error encountered during mixin config postInit step\'" + config.getName() + "\': " + message, exception1);
            }
        }

        this.configs.addAll(this.pendingConfigs);
        Collections.sort(this.configs);
        this.pendingConfigs.clear();
        return totalMixins;
    }

    private byte[] applyMixins(MixinEnvironment environment, TargetClassContext context) {
        Profiler.Section timer = this.profiler.begin("preapply");

        this.extensions.preApply(context);
        timer = timer.next("apply");
        this.apply(context);
        timer = timer.next("postapply");

        try {
            this.extensions.postApply(context);
        } catch (ExtensionCheckClass.ValidationFailedException extensioncheckclass_validationfailedexception) {
            MixinTransformer.logger.info(extensioncheckclass_validationfailedexception.getMessage());
            if (context.isExportForced() || environment.getOption(MixinEnvironment.Option.DEBUG_EXPORT)) {
                this.writeClass(context);
            }
        }

        timer.end();
        return this.writeClass(context);
    }

    private void apply(TargetClassContext context) {
        context.applyMixins();
    }

    private void handleMixinPrepareError(MixinConfig config, InvalidMixinException ex, MixinEnvironment environment) throws MixinPrepareError {
        this.handleMixinError(config.getName(), ex, environment, MixinTransformer.ErrorPhase.PREPARE);
    }

    private void handleMixinApplyError(String targetClass, InvalidMixinException ex, MixinEnvironment environment) throws MixinApplyError {
        this.handleMixinError(targetClass, ex, environment, MixinTransformer.ErrorPhase.APPLY);
    }

    private void handleMixinError(String context, InvalidMixinException ex, MixinEnvironment environment, MixinTransformer.ErrorPhase errorPhase) throws Error {
        this.errorState = true;
        IMixinInfo mixin = ex.getMixin();

        if (mixin == null) {
            MixinTransformer.logger.error("InvalidMixinException has no mixin!", ex);
            throw ex;
        } else {
            IMixinConfig config = mixin.getConfig();
            MixinEnvironment.Phase phase = mixin.getPhase();
            IMixinErrorHandler.ErrorAction action = config.isRequired() ? IMixinErrorHandler.ErrorAction.ERROR : IMixinErrorHandler.ErrorAction.WARN;

            if (environment.getOption(MixinEnvironment.Option.DEBUG_VERBOSE)) {
                (new PrettyPrinter()).add("Invalid Mixin").centre().hr('-').kvWidth(10).kv("Action", errorPhase.name()).kv("Mixin", mixin.getClassName()).kv("Config", config.getName()).kv("Phase", phase).hr('-').add("    %s", new Object[] { ex.getClass().getName()}).hr('-').addWrapped("    %s", new Object[] { ex.getMessage()}).hr('-').add((Throwable) ex, 8).trace(action.logLevel);
            }

            Iterator iterator = this.getErrorHandlers(mixin.getPhase()).iterator();

            while (iterator.hasNext()) {
                IMixinErrorHandler handler = (IMixinErrorHandler) iterator.next();
                IMixinErrorHandler.ErrorAction newAction = errorPhase.onError(handler, context, ex, mixin, action);

                if (newAction != null) {
                    action = newAction;
                }
            }

            MixinTransformer.logger.log(action.logLevel, errorPhase.getLogMessage(context, ex, mixin), ex);
            this.errorState = false;
            if (action == IMixinErrorHandler.ErrorAction.ERROR) {
                throw new MixinApplyError(errorPhase.getErrorMessage(mixin, config, phase), ex);
            }
        }
    }

    private List getErrorHandlers(MixinEnvironment.Phase phase) {
        ArrayList handlers = new ArrayList();
        Iterator iterator = Mixins.getErrorHandlerClasses().iterator();

        while (iterator.hasNext()) {
            String handlerClassName = (String) iterator.next();

            try {
                MixinTransformer.logger.info("Instancing error handler class {}", new Object[] { handlerClassName});
                Class handlerClass = this.service.getClassProvider().findClass(handlerClassName, true);
                IMixinErrorHandler handler = (IMixinErrorHandler) handlerClass.newInstance();

                if (handler != null) {
                    handlers.add(handler);
                }
            } catch (Throwable throwable) {
                ;
            }
        }

        return handlers;
    }

    private byte[] writeClass(TargetClassContext context) {
        return this.writeClass(context.getClassName(), context.getClassNode(), context.isExportForced());
    }

    private byte[] writeClass(String transformedName, ClassNode targetClass, boolean forceExport) {
        Profiler.Section writeTimer = this.profiler.begin("write");
        byte[] bytes = this.writeClass(targetClass);

        writeTimer.end();
        this.extensions.export(this.currentEnvironment, transformedName, forceExport, bytes);
        return bytes;
    }

    private void dumpClassOnFailure(String className, byte[] bytes, MixinEnvironment env) {
        if (env.getOption(MixinEnvironment.Option.DUMP_TARGET_ON_FAILURE)) {
            ExtensionClassExporter exporter = (ExtensionClassExporter) this.extensions.getExtension(ExtensionClassExporter.class);

            exporter.dumpClass(className.replace('.', '/') + ".target", bytes);
        }

    }

    static enum ErrorPhase {

        PREPARE {;
            IMixinErrorHandler.ErrorAction onError(IMixinErrorHandler handler, String context, InvalidMixinException ex, IMixinInfo mixin, IMixinErrorHandler.ErrorAction action) {
                try {
                    return handler.onPrepareError(mixin.getConfig(), ex, mixin, action);
                } catch (AbstractMethodError abstractmethoderror) {
                    return action;
                }
            }

            protected String getContext(IMixinInfo mixin, String context) {
                return String.format("preparing %s in %s", new Object[] { mixin.getName(), context});
            }
        }, APPLY {;
    IMixinErrorHandler.ErrorAction onError(IMixinErrorHandler handler, String context, InvalidMixinException ex, IMixinInfo mixin, IMixinErrorHandler.ErrorAction action) {
        try {
            return handler.onApplyError(context, ex, mixin, action);
        } catch (AbstractMethodError abstractmethoderror) {
            return action;
        }
    }

    protected String getContext(IMixinInfo mixin, String context) {
        return String.format("%s -> %s", new Object[] { mixin, context});
    }
};

        private final String text;

        private ErrorPhase() {
            this.text = this.name().toLowerCase();
        }

        abstract IMixinErrorHandler.ErrorAction onError(IMixinErrorHandler imixinerrorhandler, String s, InvalidMixinException invalidmixinexception, IMixinInfo imixininfo, IMixinErrorHandler.ErrorAction imixinerrorhandler_erroraction);

        protected abstract String getContext(IMixinInfo imixininfo, String s);

        public String getLogMessage(String context, InvalidMixinException ex, IMixinInfo mixin) {
            return String.format("Mixin %s failed %s: %s %s", new Object[] { this.text, this.getContext(mixin, context), ex.getClass().getName(), ex.getMessage()});
        }

        public String getErrorMessage(IMixinInfo mixin, IMixinConfig config, MixinEnvironment.Phase phase) {
            return String.format("Mixin [%s] from phase [%s] in config [%s] FAILED during %s", new Object[] { mixin, phase, config, this.name()});
        }

        ErrorPhase(Object x2) {
            this();
        }
    }
}
