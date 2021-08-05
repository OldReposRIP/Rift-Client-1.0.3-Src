package org.spongepowered.asm.mixin.transformer;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.Label;
import org.spongepowered.asm.lib.Type;
import org.spongepowered.asm.lib.signature.SignatureReader;
import org.spongepowered.asm.lib.signature.SignatureVisitor;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.AnnotationNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FieldInsnNode;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.spongepowered.asm.lib.tree.JumpInsnNode;
import org.spongepowered.asm.lib.tree.LabelNode;
import org.spongepowered.asm.lib.tree.LineNumberNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionClassExporter;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import org.spongepowered.asm.mixin.transformer.meta.MixinRenamed;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;
import org.spongepowered.asm.util.Annotations;
import org.spongepowered.asm.util.Bytecode;
import org.spongepowered.asm.util.ConstraintParser;
import org.spongepowered.asm.util.perf.Profiler;
import org.spongepowered.asm.util.throwables.ConstraintViolationException;
import org.spongepowered.asm.util.throwables.InvalidConstraintException;

class MixinApplicatorStandard {

    protected static final List CONSTRAINED_ANNOTATIONS = ImmutableList.of(Overwrite.class, Inject.class, ModifyArg.class, ModifyArgs.class, Redirect.class, ModifyVariable.class, ModifyConstant.class);
    protected static final int[] INITIALISER_OPCODE_BLACKLIST = new int[] { 177, 21, 22, 23, 24, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 79, 80, 81, 82, 83, 84, 85, 86};
    protected final Logger logger = LogManager.getLogger("mixin");
    protected final TargetClassContext context;
    protected final String targetName;
    protected final ClassNode targetClass;
    protected final Profiler profiler = MixinEnvironment.getProfiler();
    protected final boolean mergeSignatures;

    MixinApplicatorStandard(TargetClassContext context) {
        this.context = context;
        this.targetName = context.getClassName();
        this.targetClass = context.getClassNode();
        ExtensionClassExporter exporter = (ExtensionClassExporter) context.getExtensions().getExtension(ExtensionClassExporter.class);

        this.mergeSignatures = exporter.isDecompilerActive() && MixinEnvironment.getCurrentEnvironment().getOption(MixinEnvironment.Option.DEBUG_EXPORT_DECOMPILE_MERGESIGNATURES);
    }

    void apply(SortedSet mixins) {
        ArrayList mixinContexts = new ArrayList();
        Iterator current = mixins.iterator();

        while (current.hasNext()) {
            MixinInfo ex = (MixinInfo) current.next();

            this.logger.log(ex.getLoggingLevel(), "Mixing {} from {} into {}", new Object[] { ex.getName(), ex.getParent(), this.targetName});
            mixinContexts.add(ex.createContextFor(this.context));
        }

        current = null;

        try {
            Iterator iterator = mixinContexts.iterator();

            label46:
            while (true) {
                MixinTargetContext context;

                if (!iterator.hasNext()) {
                    MixinApplicatorStandard.ApplicatorPass[] amixinapplicatorstandard_applicatorpass = MixinApplicatorStandard.ApplicatorPass.values();
                    int i = amixinapplicatorstandard_applicatorpass.length;

                    for (int j = 0; j < i; ++j) {
                        MixinApplicatorStandard.ApplicatorPass pass = amixinapplicatorstandard_applicatorpass[j];
                        Profiler.Section timer = this.profiler.begin(new String[] { "pass", pass.name().toLowerCase()});
                        Iterator iterator1 = mixinContexts.iterator();

                        while (iterator1.hasNext()) {
                            MixinTargetContext context1 = (MixinTargetContext) iterator1.next();

                            this.applyMixin(context1, pass);
                        }

                        timer.end();
                    }

                    iterator = mixinContexts.iterator();

                    while (true) {
                        if (!iterator.hasNext()) {
                            break label46;
                        }

                        context = (MixinTargetContext) iterator.next();
                        context.postApply(this.targetName, this.targetClass);
                    }
                }

                context = (MixinTargetContext) iterator.next();
                context.preApply(this.targetName, this.targetClass);
            }
        } catch (InvalidMixinException invalidmixinexception) {
            throw invalidmixinexception;
        } catch (Exception exception) {
            throw new InvalidMixinException(current, "Unexpecteded " + exception.getClass().getSimpleName() + " whilst applying the mixin class: " + exception.getMessage(), exception);
        }

        this.applySourceMap(this.context);
        this.context.processDebugTasks();
    }

    protected final void applyMixin(MixinTargetContext mixin, MixinApplicatorStandard.ApplicatorPass pass) {
        switch (pass) {
        case MAIN:
            this.applySignature(mixin);
            this.applyInterfaces(mixin);
            this.applyAttributes(mixin);
            this.applyAnnotations(mixin);
            this.applyFields(mixin);
            this.applyMethods(mixin);
            this.applyInitialisers(mixin);
            break;

        case PREINJECT:
            this.prepareInjections(mixin);
            break;

        case INJECT:
            this.applyAccessors(mixin);
            this.applyInjections(mixin);
            break;

        default:
            throw new IllegalStateException("Invalid pass specified " + pass);
        }

    }

    protected void applySignature(MixinTargetContext mixin) {
        if (this.mergeSignatures) {
            this.context.mergeSignature(mixin.getSignature());
        }

    }

    protected void applyInterfaces(MixinTargetContext mixin) {
        Iterator iterator = mixin.getInterfaces().iterator();

        while (iterator.hasNext()) {
            String interfaceName = (String) iterator.next();

            if (!this.targetClass.interfaces.contains(interfaceName)) {
                this.targetClass.interfaces.add(interfaceName);
                mixin.getTargetClassInfo().addInterface(interfaceName);
            }
        }

    }

    protected void applyAttributes(MixinTargetContext mixin) {
        if (mixin.shouldSetSourceFile()) {
            this.targetClass.sourceFile = mixin.getSourceFile();
        }

        this.targetClass.version = Math.max(this.targetClass.version, mixin.getMinRequiredClassVersion());
    }

    protected void applyAnnotations(MixinTargetContext mixin) {
        ClassNode sourceClass = mixin.getClassNode();

        Bytecode.mergeAnnotations(sourceClass, this.targetClass);
    }

    protected void applyFields(MixinTargetContext mixin) {
        this.mergeShadowFields(mixin);
        this.mergeNewFields(mixin);
    }

    protected void mergeShadowFields(MixinTargetContext mixin) {
        Iterator iterator = mixin.getShadowFields().iterator();

        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            FieldNode shadow = (FieldNode) entry.getKey();
            FieldNode target = this.findTargetField(shadow);

            if (target != null) {
                Bytecode.mergeAnnotations(shadow, target);
                if (((ClassInfo.Field) entry.getValue()).isDecoratedMutable() && !Bytecode.hasFlag(target, 2)) {
                    target.access &= -17;
                }
            }
        }

    }

    protected void mergeNewFields(MixinTargetContext mixin) {
        Iterator iterator = mixin.getFields().iterator();

        while (iterator.hasNext()) {
            FieldNode field = (FieldNode) iterator.next();
            FieldNode target = this.findTargetField(field);

            if (target == null) {
                this.targetClass.fields.add(field);
                if (field.signature != null) {
                    if (this.mergeSignatures) {
                        SignatureVisitor sv = mixin.getSignature().getRemapper();

                        (new SignatureReader(field.signature)).accept(sv);
                        field.signature = sv.toString();
                    } else {
                        field.signature = null;
                    }
                }
            }
        }

    }

    protected void applyMethods(MixinTargetContext mixin) {
        Iterator iterator = mixin.getShadowMethods().iterator();

        MethodNode mixinMethod;

        while (iterator.hasNext()) {
            mixinMethod = (MethodNode) iterator.next();
            this.applyShadowMethod(mixin, mixinMethod);
        }

        iterator = mixin.getMethods().iterator();

        while (iterator.hasNext()) {
            mixinMethod = (MethodNode) iterator.next();
            this.applyNormalMethod(mixin, mixinMethod);
        }

    }

    protected void applyShadowMethod(MixinTargetContext mixin, MethodNode shadow) {
        MethodNode target = this.findTargetMethod(shadow);

        if (target != null) {
            Bytecode.mergeAnnotations(shadow, target);
        }

    }

    protected void applyNormalMethod(MixinTargetContext mixin, MethodNode mixinMethod) {
        mixin.transformMethod(mixinMethod);
        if (!mixinMethod.name.startsWith("<")) {
            this.checkMethodVisibility(mixin, mixinMethod);
            this.checkMethodConstraints(mixin, mixinMethod);
            this.mergeMethod(mixin, mixinMethod);
        } else if ("<clinit>".equals(mixinMethod.name)) {
            this.appendInsns(mixin, mixinMethod);
        }

    }

    protected void mergeMethod(MixinTargetContext mixin, MethodNode method) {
        boolean isOverwrite = Annotations.getVisible(method, Overwrite.class) != null;
        MethodNode target = this.findTargetMethod(method);

        if (target != null) {
            if (this.isAlreadyMerged(mixin, method, isOverwrite, target)) {
                return;
            }

            AnnotationNode sv = Annotations.getInvisible(method, Intrinsic.class);

            if (sv != null) {
                if (this.mergeIntrinsic(mixin, method, isOverwrite, target, sv)) {
                    mixin.getTarget().methodMerged(method);
                    return;
                }
            } else {
                if (mixin.requireOverwriteAnnotations() && !isOverwrite) {
                    throw new InvalidMixinException(mixin, String.format("%s%s in %s cannot overwrite method in %s because @Overwrite is required by the parent configuration", new Object[] { method.name, method.desc, mixin, mixin.getTarget().getClassName()}));
                }

                this.targetClass.methods.remove(target);
            }
        } else if (isOverwrite) {
            throw new InvalidMixinException(mixin, String.format("Overwrite target \"%s\" was not located in target class %s", new Object[] { method.name, mixin.getTargetClassRef()}));
        }

        this.targetClass.methods.add(method);
        mixin.methodMerged(method);
        if (method.signature != null) {
            if (this.mergeSignatures) {
                SignatureVisitor sv1 = mixin.getSignature().getRemapper();

                (new SignatureReader(method.signature)).accept(sv1);
                method.signature = sv1.toString();
            } else {
                method.signature = null;
            }
        }

    }

    protected boolean isAlreadyMerged(MixinTargetContext mixin, MethodNode method, boolean isOverwrite, MethodNode target) {
        AnnotationNode merged = Annotations.getVisible(target, MixinMerged.class);

        if (merged == null) {
            if (Annotations.getVisible(target, Final.class) != null) {
                this.logger.warn("Overwrite prohibited for @Final method {} in {}. Skipping method.", new Object[] { method.name, mixin});
                return true;
            } else {
                return false;
            }
        } else {
            String sessionId = (String) Annotations.getValue(merged, "sessionId");

            if (!this.context.getSessionId().equals(sessionId)) {
                throw new ClassFormatError("Invalid @MixinMerged annotation found in" + mixin + " at " + method.name + " in " + this.targetClass.name);
            } else if (Bytecode.hasFlag(target, 4160) && Bytecode.hasFlag(method, 4160)) {
                if (mixin.getEnvironment().getOption(MixinEnvironment.Option.DEBUG_VERBOSE)) {
                    this.logger.warn("Synthetic bridge method clash for {} in {}", new Object[] { method.name, mixin});
                }

                return true;
            } else {
                String owner = (String) Annotations.getValue(merged, "mixin");
                int priority = ((Integer) Annotations.getValue(merged, "priority")).intValue();

                if (priority >= mixin.getPriority() && !owner.equals(mixin.getClassName())) {
                    this.logger.warn("Method overwrite conflict for {} in {}, previously written by {}. Skipping method.", new Object[] { method.name, mixin, owner});
                    return true;
                } else if (Annotations.getVisible(target, Final.class) != null) {
                    this.logger.warn("Method overwrite conflict for @Final method {} in {} declared by {}. Skipping method.", new Object[] { method.name, mixin, owner});
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    protected boolean mergeIntrinsic(MixinTargetContext mixin, MethodNode method, boolean isOverwrite, MethodNode target, AnnotationNode intrinsic) {
        if (isOverwrite) {
            throw new InvalidMixinException(mixin, "@Intrinsic is not compatible with @Overwrite, remove one of these annotations on " + method.name + " in " + mixin);
        } else {
            String methodName = method.name + method.desc;

            if (Bytecode.hasFlag(method, 8)) {
                throw new InvalidMixinException(mixin, "@Intrinsic method cannot be static, found " + methodName + " in " + mixin);
            } else {
                if (!Bytecode.hasFlag(method, 4096)) {
                    AnnotationNode renamed = Annotations.getVisible(method, MixinRenamed.class);

                    if (renamed == null || !((Boolean) Annotations.getValue(renamed, "isInterfaceMember", (Object) Boolean.FALSE)).booleanValue()) {
                        throw new InvalidMixinException(mixin, "@Intrinsic method must be prefixed interface method, no rename encountered on " + methodName + " in " + mixin);
                    }
                }

                if (!((Boolean) Annotations.getValue(intrinsic, "displace", (Object) Boolean.FALSE)).booleanValue()) {
                    this.logger.log(mixin.getLoggingLevel(), "Skipping Intrinsic mixin method {} for {}", new Object[] { methodName, mixin.getTargetClassRef()});
                    return true;
                } else {
                    this.displaceIntrinsic(mixin, method, target);
                    return false;
                }
            }
        }
    }

    protected void displaceIntrinsic(MixinTargetContext mixin, MethodNode method, MethodNode target) {
        String proxyName = "proxy+" + target.name;
        ListIterator iter = method.instructions.iterator();

        while (iter.hasNext()) {
            AbstractInsnNode insn = (AbstractInsnNode) iter.next();

            if (insn instanceof MethodInsnNode && insn.getOpcode() != 184) {
                MethodInsnNode methodNode = (MethodInsnNode) insn;

                if (methodNode.owner.equals(this.targetClass.name) && methodNode.name.equals(target.name) && methodNode.desc.equals(target.desc)) {
                    methodNode.name = proxyName;
                }
            }
        }

        target.name = proxyName;
    }

    protected final void appendInsns(MixinTargetContext mixin, MethodNode method) {
        if (Type.getReturnType(method.desc) != Type.VOID_TYPE) {
            throw new IllegalArgumentException("Attempted to merge insns from a method which does not return void");
        } else {
            MethodNode target = this.findTargetMethod(method);

            if (target == null) {
                this.targetClass.methods.add(method);
            } else {
                AbstractInsnNode returnNode = Bytecode.findInsn(target, 177);

                if (returnNode != null) {
                    ListIterator injectIter = method.instructions.iterator();

                    while (injectIter.hasNext()) {
                        AbstractInsnNode insn = (AbstractInsnNode) injectIter.next();

                        if (!(insn instanceof LineNumberNode) && insn.getOpcode() != 177) {
                            target.instructions.insertBefore(returnNode, insn);
                        }
                    }

                    target.maxLocals = Math.max(target.maxLocals, method.maxLocals);
                    target.maxStack = Math.max(target.maxStack, method.maxStack);
                }

            }
        }
    }

    protected void applyInitialisers(MixinTargetContext mixin) {
        MethodNode ctor = this.getConstructor(mixin);

        if (ctor != null) {
            Deque initialiser = this.getInitialiser(mixin, ctor);

            if (initialiser != null && initialiser.size() != 0) {
                Iterator iterator = this.targetClass.methods.iterator();

                while (iterator.hasNext()) {
                    MethodNode method = (MethodNode) iterator.next();

                    if ("<init>".equals(method.name)) {
                        method.maxStack = Math.max(method.maxStack, ctor.maxStack);
                        this.injectInitialiser(mixin, method, initialiser);
                    }
                }

            }
        }
    }

    protected MethodNode getConstructor(MixinTargetContext mixin) {
        MethodNode ctor = null;
        Iterator iterator = mixin.getMethods().iterator();

        while (iterator.hasNext()) {
            MethodNode mixinMethod = (MethodNode) iterator.next();

            if ("<init>".equals(mixinMethod.name) && Bytecode.methodHasLineNumbers(mixinMethod)) {
                if (ctor == null) {
                    ctor = mixinMethod;
                } else {
                    this.logger.warn(String.format("Mixin %s has multiple constructors, %s was selected\n", new Object[] { mixin, ctor.desc}));
                }
            }
        }

        return ctor;
    }

    private MixinApplicatorStandard.Range getConstructorRange(MethodNode ctor) {
        boolean lineNumberIsValid = false;
        AbstractInsnNode endReturn = null;
        int line = 0;
        int start = 0;
        int end = 0;
        int superIndex = -1;
        ListIterator label = ctor.instructions.iterator();

        while (label.hasNext()) {
            AbstractInsnNode insn = (AbstractInsnNode) label.next();

            if (insn instanceof LineNumberNode) {
                line = ((LineNumberNode) insn).line;
                lineNumberIsValid = true;
            } else if (insn instanceof MethodInsnNode) {
                if (insn.getOpcode() == 183 && "<init>".equals(((MethodInsnNode) insn).name) && superIndex == -1) {
                    superIndex = ctor.instructions.indexOf(insn);
                    start = line;
                }
            } else if (insn.getOpcode() == 181) {
                lineNumberIsValid = false;
            } else if (insn.getOpcode() == 177) {
                if (lineNumberIsValid) {
                    end = line;
                } else {
                    end = start;
                    endReturn = insn;
                }
            }
        }

        if (endReturn != null) {
            LabelNode label1 = new LabelNode(new Label());

            ctor.instructions.insertBefore(endReturn, (AbstractInsnNode) label1);
            ctor.instructions.insertBefore(endReturn, (AbstractInsnNode) (new LineNumberNode(start, label1)));
        }

        return new MixinApplicatorStandard.Range(start, end, superIndex);
    }

    protected final Deque getInitialiser(MixinTargetContext mixin, MethodNode ctor) {
        MixinApplicatorStandard.Range init = this.getConstructorRange(ctor);

        if (!init.isValid()) {
            return null;
        } else {
            boolean line = false;
            ArrayDeque initialiser = new ArrayDeque();
            boolean gatherNodes = false;
            short trimAtOpcode = -1;
            LabelNode optionalInsn = null;
            ListIterator last = ctor.instructions.iterator(init.marker);

            while (last.hasNext()) {
                AbstractInsnNode insn = (AbstractInsnNode) last.next();

                if (insn instanceof LineNumberNode) {
                    int i = ((LineNumberNode) insn).line;
                    AbstractInsnNode abstractinsnnode = ctor.instructions.get(ctor.instructions.indexOf(insn) + 1);

                    if (i == init.end && abstractinsnnode.getOpcode() != 177) {
                        gatherNodes = true;
                        trimAtOpcode = 177;
                    } else {
                        gatherNodes = init.excludes(i);
                        trimAtOpcode = -1;
                    }
                } else if (gatherNodes) {
                    if (optionalInsn != null) {
                        initialiser.add(optionalInsn);
                        optionalInsn = null;
                    }

                    if (insn instanceof LabelNode) {
                        optionalInsn = (LabelNode) insn;
                    } else {
                        int opcode = insn.getOpcode();

                        if (opcode == trimAtOpcode) {
                            trimAtOpcode = -1;
                        } else {
                            int[] aint = MixinApplicatorStandard.INITIALISER_OPCODE_BLACKLIST;
                            int j = aint.length;

                            for (int k = 0; k < j; ++k) {
                                int ivalidOp = aint[k];

                                if (opcode == ivalidOp) {
                                    throw new InvalidMixinException(mixin, "Cannot handle " + Bytecode.getOpcodeName(opcode) + " opcode (0x" + Integer.toHexString(opcode).toUpperCase() + ") in class initialiser");
                                }
                            }

                            initialiser.add(insn);
                        }
                    }
                }
            }

            AbstractInsnNode abstractinsnnode1 = (AbstractInsnNode) initialiser.peekLast();

            if (abstractinsnnode1 != null && abstractinsnnode1.getOpcode() != 181) {
                throw new InvalidMixinException(mixin, "Could not parse initialiser, expected 0xB5, found 0x" + Integer.toHexString(abstractinsnnode1.getOpcode()) + " in " + mixin);
            } else {
                return initialiser;
            }
        }
    }

    protected final void injectInitialiser(MixinTargetContext mixin, MethodNode ctor, Deque initialiser) {
        Map labels = Bytecode.cloneLabels(ctor.instructions);
        AbstractInsnNode insn = this.findInitialiserInjectionPoint(mixin, ctor, initialiser);

        if (insn == null) {
            this.logger.warn("Failed to locate initialiser injection point in <init>{}, initialiser was not mixed in.", new Object[] { ctor.desc});
        } else {
            Iterator iterator = initialiser.iterator();

            while (iterator.hasNext()) {
                AbstractInsnNode node = (AbstractInsnNode) iterator.next();

                if (!(node instanceof LabelNode)) {
                    if (node instanceof JumpInsnNode) {
                        throw new InvalidMixinException(mixin, "Unsupported JUMP opcode in initialiser in " + mixin);
                    }

                    AbstractInsnNode imACloneNow = node.clone(labels);

                    ctor.instructions.insert(insn, imACloneNow);
                    insn = imACloneNow;
                }
            }

        }
    }

    protected AbstractInsnNode findInitialiserInjectionPoint(MixinTargetContext mixin, MethodNode ctor, Deque initialiser) {
        HashSet initialisedFields = new HashSet();
        Iterator mode = initialiser.iterator();

        while (mode.hasNext()) {
            AbstractInsnNode targetName = (AbstractInsnNode) mode.next();

            if (targetName.getOpcode() == 181) {
                initialisedFields.add(fieldKey((FieldInsnNode) targetName));
            }
        }

        MixinApplicatorStandard.InitialiserInjectionMode mode1 = this.getInitialiserInjectionMode(mixin.getEnvironment());
        String targetName1 = mixin.getTargetClassInfo().getName();
        String targetSuperName = mixin.getTargetClassInfo().getSuperName();
        AbstractInsnNode targetInsn = null;
        ListIterator iter = ctor.instructions.iterator();

        while (iter.hasNext()) {
            AbstractInsnNode insn = (AbstractInsnNode) iter.next();
            String key;

            if (insn.getOpcode() == 183 && "<init>".equals(((MethodInsnNode) insn).name)) {
                key = ((MethodInsnNode) insn).owner;
                if (key.equals(targetName1) || key.equals(targetSuperName)) {
                    targetInsn = insn;
                    if (mode1 == MixinApplicatorStandard.InitialiserInjectionMode.SAFE) {
                        break;
                    }
                }
            } else if (insn.getOpcode() == 181 && mode1 == MixinApplicatorStandard.InitialiserInjectionMode.DEFAULT) {
                key = fieldKey((FieldInsnNode) insn);
                if (initialisedFields.contains(key)) {
                    targetInsn = insn;
                }
            }
        }

        return targetInsn;
    }

    private MixinApplicatorStandard.InitialiserInjectionMode getInitialiserInjectionMode(MixinEnvironment environment) {
        String strMode = environment.getOptionValue(MixinEnvironment.Option.INITIALISER_INJECTION_MODE);

        if (strMode == null) {
            return MixinApplicatorStandard.InitialiserInjectionMode.DEFAULT;
        } else {
            try {
                return MixinApplicatorStandard.InitialiserInjectionMode.valueOf(strMode.toUpperCase());
            } catch (Exception exception) {
                this.logger.warn("Could not parse unexpected value \"{}\" for mixin.initialiserInjectionMode, reverting to DEFAULT", new Object[] { strMode});
                return MixinApplicatorStandard.InitialiserInjectionMode.DEFAULT;
            }
        }
    }

    private static String fieldKey(FieldInsnNode fieldNode) {
        return String.format("%s:%s", new Object[] { fieldNode.desc, fieldNode.name});
    }

    protected void prepareInjections(MixinTargetContext mixin) {
        mixin.prepareInjections();
    }

    protected void applyInjections(MixinTargetContext mixin) {
        mixin.applyInjections();
    }

    protected void applyAccessors(MixinTargetContext mixin) {
        List accessorMethods = mixin.generateAccessors();
        Iterator iterator = accessorMethods.iterator();

        while (iterator.hasNext()) {
            MethodNode method = (MethodNode) iterator.next();

            if (!method.name.startsWith("<")) {
                this.mergeMethod(mixin, method);
            }
        }

    }

    protected void checkMethodVisibility(MixinTargetContext mixin, MethodNode mixinMethod) {
        if (Bytecode.hasFlag(mixinMethod, 8) && !Bytecode.hasFlag(mixinMethod, 2) && !Bytecode.hasFlag(mixinMethod, 4096) && Annotations.getVisible(mixinMethod, Overwrite.class) == null) {
            throw new InvalidMixinException(mixin, String.format("Mixin %s contains non-private static method %s", new Object[] { mixin, mixinMethod}));
        }
    }

    protected void applySourceMap(TargetClassContext context) {
        this.targetClass.sourceDebug = context.getSourceMap().toString();
    }

    protected void checkMethodConstraints(MixinTargetContext mixin, MethodNode method) {
        Iterator iterator = MixinApplicatorStandard.CONSTRAINED_ANNOTATIONS.iterator();

        while (iterator.hasNext()) {
            Class annotationType = (Class) iterator.next();
            AnnotationNode annotation = Annotations.getVisible(method, annotationType);

            if (annotation != null) {
                this.checkConstraints(mixin, method, annotation);
            }
        }

    }

    protected final void checkConstraints(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
        try {
            ConstraintParser.Constraint ex = ConstraintParser.parse(annotation);

            try {
                ex.check(mixin.getEnvironment());
            } catch (ConstraintViolationException constraintviolationexception) {
                String message = String.format("Constraint violation: %s on %s in %s", new Object[] { constraintviolationexception.getMessage(), method, mixin});

                this.logger.warn(message);
                if (!mixin.getEnvironment().getOption(MixinEnvironment.Option.IGNORE_CONSTRAINTS)) {
                    throw new InvalidMixinException(mixin, message, constraintviolationexception);
                }
            }

        } catch (InvalidConstraintException invalidconstraintexception) {
            throw new InvalidMixinException(mixin, invalidconstraintexception.getMessage());
        }
    }

    protected final MethodNode findTargetMethod(MethodNode searchFor) {
        Iterator iterator = this.targetClass.methods.iterator();

        MethodNode target;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            target = (MethodNode) iterator.next();
        } while (!target.name.equals(searchFor.name) || !target.desc.equals(searchFor.desc));

        return target;
    }

    protected final FieldNode findTargetField(FieldNode searchFor) {
        Iterator iterator = this.targetClass.fields.iterator();

        FieldNode target;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            target = (FieldNode) iterator.next();
        } while (!target.name.equals(searchFor.name));

        return target;
    }

    class Range {

        final int start;
        final int end;
        final int marker;

        Range(int start, int end, int marker) {
            this.start = start;
            this.end = end;
            this.marker = marker;
        }

        boolean isValid() {
            return this.start != 0 && this.end != 0 && this.end >= this.start;
        }

        boolean contains(int value) {
            return value >= this.start && value <= this.end;
        }

        boolean excludes(int value) {
            return value < this.start || value > this.end;
        }

        public String toString() {
            return String.format("Range[%d-%d,%d,valid=%s)", new Object[] { Integer.valueOf(this.start), Integer.valueOf(this.end), Integer.valueOf(this.marker), Boolean.valueOf(this.isValid())});
        }
    }

    static enum InitialiserInjectionMode {

        DEFAULT, SAFE;
    }

    static enum ApplicatorPass {

        MAIN, PREINJECT, INJECT;
    }
}
