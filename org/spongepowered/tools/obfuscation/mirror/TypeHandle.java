package org.spongepowered.tools.obfuscation.mirror;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.mirror.mapping.ResolvableMappingMethod;

public class TypeHandle {

    private final String name;
    private final PackageElement pkg;
    private final TypeElement element;
    private TypeReference reference;

    public TypeHandle(PackageElement pkg, String name) {
        this.name = name.replace('.', '/');
        this.pkg = pkg;
        this.element = null;
    }

    public TypeHandle(TypeElement element) {
        this.pkg = TypeUtils.getPackage(element);
        this.name = TypeUtils.getInternalName(element);
        this.element = element;
    }

    public TypeHandle(DeclaredType type) {
        this((TypeElement) type.asElement());
    }

    public final String toString() {
        return this.name.replace('/', '.');
    }

    public final String getName() {
        return this.name;
    }

    public final PackageElement getPackage() {
        return this.pkg;
    }

    public final TypeElement getElement() {
        return this.element;
    }

    protected TypeElement getTargetElement() {
        return this.element;
    }

    public AnnotationHandle getAnnotation(Class annotationClass) {
        return AnnotationHandle.of(this.getTargetElement(), annotationClass);
    }

    public final List getEnclosedElements() {
        return getEnclosedElements(this.getTargetElement());
    }

    public List getEnclosedElements(ElementKind... kind) {
        return getEnclosedElements(this.getTargetElement(), kind);
    }

    public TypeMirror getType() {
        return this.getTargetElement() != null ? this.getTargetElement().asType() : null;
    }

    public TypeHandle getSuperclass() {
        TypeElement targetElement = this.getTargetElement();

        if (targetElement == null) {
            return null;
        } else {
            TypeMirror superClass = targetElement.getSuperclass();

            return superClass != null && superClass.getKind() != TypeKind.NONE ? new TypeHandle((DeclaredType) superClass) : null;
        }
    }

    public List getInterfaces() {
        if (this.getTargetElement() == null) {
            return Collections.emptyList();
        } else {
            Builder list = ImmutableList.builder();
            Iterator iterator = this.getTargetElement().getInterfaces().iterator();

            while (iterator.hasNext()) {
                TypeMirror iface = (TypeMirror) iterator.next();

                list.add(new TypeHandle((DeclaredType) iface));
            }

            return list.build();
        }
    }

    public boolean isPublic() {
        return this.getTargetElement() != null && this.getTargetElement().getModifiers().contains(Modifier.PUBLIC);
    }

    public boolean isImaginary() {
        return this.getTargetElement() == null;
    }

    public boolean isSimulated() {
        return false;
    }

    public final TypeReference getReference() {
        if (this.reference == null) {
            this.reference = new TypeReference(this);
        }

        return this.reference;
    }

    public MappingMethod getMappingMethod(String name, String desc) {
        return new ResolvableMappingMethod(this, name, desc);
    }

    public String findDescriptor(MemberInfo memberInfo) {
        String desc = memberInfo.desc;

        if (desc == null) {
            Iterator iterator = this.getEnclosedElements(new ElementKind[] { ElementKind.METHOD}).iterator();

            while (iterator.hasNext()) {
                ExecutableElement method = (ExecutableElement) iterator.next();

                if (method.getSimpleName().toString().equals(memberInfo.name)) {
                    desc = TypeUtils.getDescriptor(method);
                    break;
                }
            }
        }

        return desc;
    }

    public final FieldHandle findField(VariableElement element) {
        return this.findField(element, true);
    }

    public final FieldHandle findField(VariableElement element, boolean caseSensitive) {
        return this.findField(element.getSimpleName().toString(), TypeUtils.getTypeName(element.asType()), caseSensitive);
    }

    public final FieldHandle findField(String name, String type) {
        return this.findField(name, type, true);
    }

    public FieldHandle findField(String name, String type, boolean caseSensitive) {
        String rawType = TypeUtils.stripGenerics(type);
        Iterator iterator = this.getEnclosedElements(new ElementKind[] { ElementKind.FIELD}).iterator();

        VariableElement field;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            field = (VariableElement) iterator.next();
            if (compareElement(field, name, type, caseSensitive)) {
                return new FieldHandle(this.getTargetElement(), field);
            }
        } while (!compareElement(field, name, rawType, caseSensitive));

        return new FieldHandle(this.getTargetElement(), field, true);
    }

    public final MethodHandle findMethod(ExecutableElement element) {
        return this.findMethod(element, true);
    }

    public final MethodHandle findMethod(ExecutableElement element, boolean caseSensitive) {
        return this.findMethod(element.getSimpleName().toString(), TypeUtils.getJavaSignature((Element) element), caseSensitive);
    }

    public final MethodHandle findMethod(String name, String signature) {
        return this.findMethod(name, signature, true);
    }

    public MethodHandle findMethod(String name, String signature, boolean matchCase) {
        String rawSignature = TypeUtils.stripGenerics(signature);

        return findMethod(this, name, signature, rawSignature, matchCase);
    }

    protected static MethodHandle findMethod(TypeHandle target, String name, String signature, String rawSignature, boolean matchCase) {
        Iterator iterator = getEnclosedElements(target.getTargetElement(), new ElementKind[] { ElementKind.CONSTRUCTOR, ElementKind.METHOD}).iterator();

        ExecutableElement method;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            method = (ExecutableElement) iterator.next();
        } while (!compareElement(method, name, signature, matchCase) && !compareElement(method, name, rawSignature, matchCase));

        return new MethodHandle(target, method);
    }

    protected static boolean compareElement(Element elem, String name, String type, boolean matchCase) {
        try {
            String ex = elem.getSimpleName().toString();
            String elementType = TypeUtils.getJavaSignature(elem);
            String rawElementType = TypeUtils.stripGenerics(elementType);
            boolean compared = matchCase ? name.equals(ex) : name.equalsIgnoreCase(ex);

            return compared && (type.length() == 0 || type.equals(elementType) || type.equals(rawElementType));
        } catch (NullPointerException nullpointerexception) {
            return false;
        }
    }

    protected static List getEnclosedElements(TypeElement targetElement, ElementKind... kind) {
        if (kind != null && kind.length >= 1) {
            if (targetElement == null) {
                return Collections.emptyList();
            } else {
                Builder list = ImmutableList.builder();
                Iterator iterator = targetElement.getEnclosedElements().iterator();

                while (iterator.hasNext()) {
                    Element elem = (Element) iterator.next();
                    ElementKind[] aelementkind = kind;
                    int i = kind.length;

                    for (int j = 0; j < i; ++j) {
                        ElementKind ek = aelementkind[j];

                        if (elem.getKind() == ek) {
                            list.add(elem);
                            break;
                        }
                    }
                }

                return list.build();
            }
        } else {
            return getEnclosedElements(targetElement);
        }
    }

    protected static List getEnclosedElements(TypeElement targetElement) {
        return targetElement != null ? targetElement.getEnclosedElements() : Collections.emptyList();
    }
}
