package org.yaml.snakeyaml.constructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

public class Constructor extends SafeConstructor {

    public Constructor() {
        this(Object.class);
    }

    public Constructor(Class theRoot) {
        this(new TypeDescription(checkRoot(theRoot)));
    }

    private static Class checkRoot(Class theRoot) {
        if (theRoot == null) {
            throw new NullPointerException("Root class must be provided.");
        } else {
            return theRoot;
        }
    }

    public Constructor(TypeDescription theRoot) {
        this(theRoot, (Collection) null);
    }

    public Constructor(TypeDescription theRoot, Collection moreTDs) {
        if (theRoot == null) {
            throw new NullPointerException("Root type must be provided.");
        } else {
            this.yamlConstructors.put((Object) null, new Constructor.ConstructYamlObject());
            if (!Object.class.equals(theRoot.getType())) {
                this.rootTag = new Tag(theRoot.getType());
            }

            this.yamlClassConstructors.put(NodeId.scalar, new Constructor.ConstructScalar());
            this.yamlClassConstructors.put(NodeId.mapping, new Constructor.ConstructMapping());
            this.yamlClassConstructors.put(NodeId.sequence, new Constructor.ConstructSequence());
            this.addTypeDescription(theRoot);
            if (moreTDs != null) {
                Iterator iterator = moreTDs.iterator();

                while (iterator.hasNext()) {
                    TypeDescription td = (TypeDescription) iterator.next();

                    this.addTypeDescription(td);
                }
            }

        }
    }

    public Constructor(String theRoot) throws ClassNotFoundException {
        this(Class.forName(check(theRoot)));
    }

    private static final String check(String s) {
        if (s == null) {
            throw new NullPointerException("Root type must be provided.");
        } else if (s.trim().length() == 0) {
            throw new YAMLException("Root type must be provided.");
        } else {
            return s;
        }
    }

    protected Class getClassForNode(Node node) {
        Class classForTag = (Class) this.typeTags.get(node.getTag());

        if (classForTag == null) {
            String name = node.getTag().getClassName();

            Class cl;

            try {
                cl = this.getClassForName(name);
            } catch (ClassNotFoundException classnotfoundexception) {
                throw new YAMLException("Class not found: " + name);
            }

            this.typeTags.put(node.getTag(), cl);
            return cl;
        } else {
            return classForTag;
        }
    }

    protected Class getClassForName(String name) throws ClassNotFoundException {
        try {
            return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException classnotfoundexception) {
            return Class.forName(name);
        }
    }

    protected class ConstructSequence implements Construct {

        public Object construct(Node node) {
            SequenceNode snode = (SequenceNode) node;

            if (Set.class.isAssignableFrom(node.getType())) {
                if (node.isTwoStepsConstruction()) {
                    throw new YAMLException("Set cannot be recursive.");
                } else {
                    return Constructor.this.constructSet(snode);
                }
            } else if (Collection.class.isAssignableFrom(node.getType())) {
                return node.isTwoStepsConstruction() ? Constructor.this.newList(snode) : Constructor.this.constructSequence(snode);
            } else if (node.getType().isArray()) {
                return node.isTwoStepsConstruction() ? Constructor.this.createArray(node.getType(), snode.getValue().size()) : Constructor.this.constructArray(snode);
            } else {
                ArrayList possibleConstructors = new ArrayList(snode.getValue().size());
                java.lang.reflect.Constructor[] argumentList = node.getType().getDeclaredConstructors();
                int nameeterTypes = argumentList.length;

                int index;

                for (index = 0; index < nameeterTypes; ++index) {
                    java.lang.reflect.Constructor e = argumentList[index];

                    if (snode.getValue().size() == e.getParameterTypes().length) {
                        possibleConstructors.add(e);
                    }
                }

                if (!possibleConstructors.isEmpty()) {
                    Iterator iterator;

                    if (possibleConstructors.size() == 1) {
                        Object[] aobject = new Object[snode.getValue().size()];
                        java.lang.reflect.Constructor java_lang_reflect_constructor = (java.lang.reflect.Constructor) possibleConstructors.get(0);

                        index = 0;

                        Node node;

                        for (iterator = snode.getValue().iterator(); iterator.hasNext(); aobject[index++] = Constructor.this.constructObject(node)) {
                            node = (Node) iterator.next();
                            Class oclass = java_lang_reflect_constructor.getParameterTypes()[index];

                            node.setType(oclass);
                        }

                        try {
                            java_lang_reflect_constructor.setAccessible(true);
                            return java_lang_reflect_constructor.newInstance(aobject);
                        } catch (Exception exception) {
                            throw new YAMLException(exception);
                        }
                    }

                    List list = Constructor.this.constructSequence(snode);
                    Class[] aclass = new Class[list.size()];

                    index = 0;

                    for (iterator = list.iterator(); iterator.hasNext(); ++index) {
                        Object c = iterator.next();

                        aclass[index] = c.getClass();
                    }

                    iterator = possibleConstructors.iterator();

                    while (iterator.hasNext()) {
                        java.lang.reflect.Constructor java_lang_reflect_constructor1 = (java.lang.reflect.Constructor) iterator.next();
                        Class[] argTypes = java_lang_reflect_constructor1.getParameterTypes();
                        boolean foundConstructor = true;

                        for (int e1 = 0; e1 < argTypes.length; ++e1) {
                            if (!this.wrapIfPrimitive(argTypes[e1]).isAssignableFrom(aclass[e1])) {
                                foundConstructor = false;
                                break;
                            }
                        }

                        if (foundConstructor) {
                            try {
                                java_lang_reflect_constructor1.setAccessible(true);
                                return java_lang_reflect_constructor1.newInstance(list.toArray());
                            } catch (Exception exception1) {
                                throw new YAMLException(exception1);
                            }
                        }
                    }
                }

                throw new YAMLException("No suitable constructor with " + String.valueOf(snode.getValue().size()) + " arguments found for " + node.getType());
            }
        }

        private final Class wrapIfPrimitive(Class clazz) {
            if (!clazz.isPrimitive()) {
                return clazz;
            } else if (clazz == Integer.TYPE) {
                return Integer.class;
            } else if (clazz == Float.TYPE) {
                return Float.class;
            } else if (clazz == Double.TYPE) {
                return Double.class;
            } else if (clazz == Boolean.TYPE) {
                return Boolean.class;
            } else if (clazz == Long.TYPE) {
                return Long.class;
            } else if (clazz == Character.TYPE) {
                return Character.class;
            } else if (clazz == Short.TYPE) {
                return Short.class;
            } else if (clazz == Byte.TYPE) {
                return Byte.class;
            } else {
                throw new YAMLException("Unexpected primitive " + clazz);
            }
        }

        public void construct2ndStep(Node node, Object object) {
            SequenceNode snode = (SequenceNode) node;

            if (List.class.isAssignableFrom(node.getType())) {
                List list = (List) object;

                Constructor.this.constructSequenceStep2(snode, list);
            } else {
                if (!node.getType().isArray()) {
                    throw new YAMLException("Immutable objects cannot be recursive.");
                }

                Constructor.this.constructArrayStep2(snode, object);
            }

        }
    }

    protected class ConstructScalar extends AbstractConstruct {

        public Object construct(Node nnode) {
            ScalarNode node = (ScalarNode) nnode;
            Class type = node.getType();

            try {
                return Constructor.this.newInstance(type, node, false);
            } catch (InstantiationException instantiationexception) {
                Object result;

                if (!type.isPrimitive() && type != String.class && !Number.class.isAssignableFrom(type) && type != Boolean.class && !Date.class.isAssignableFrom(type) && type != Character.class && type != BigInteger.class && type != BigDecimal.class && !Enum.class.isAssignableFrom(type) && !Tag.BINARY.equals(node.getTag()) && !Calendar.class.isAssignableFrom(type) && type != UUID.class) {
                    java.lang.reflect.Constructor[] javaConstructors = type.getDeclaredConstructors();
                    int oneArgCount = 0;
                    java.lang.reflect.Constructor javaConstructor = null;
                    java.lang.reflect.Constructor[] argument = javaConstructors;
                    int e = javaConstructors.length;

                    for (int i = 0; i < e; ++i) {
                        java.lang.reflect.Constructor c = argument[i];

                        if (c.getParameterTypes().length == 1) {
                            ++oneArgCount;
                            javaConstructor = c;
                        }
                    }

                    if (javaConstructor == null) {
                        try {
                            return Constructor.this.newInstance(type, node, false);
                        } catch (InstantiationException instantiationexception1) {
                            throw new YAMLException("No single argument constructor found for " + type + " : " + instantiationexception1.getMessage());
                        }
                    }

                    Object object;

                    if (oneArgCount == 1) {
                        object = this.constructStandardJavaInstance(javaConstructor.getParameterTypes()[0], node);
                    } else {
                        object = Constructor.this.constructScalar(node);

                        try {
                            javaConstructor = type.getDeclaredConstructor(new Class[] { String.class});
                        } catch (Exception exception) {
                            throw new YAMLException("Can\'t construct a java object for scalar " + node.getTag() + "; No String constructor found. Exception=" + exception.getMessage(), exception);
                        }
                    }

                    try {
                        javaConstructor.setAccessible(true);
                        result = javaConstructor.newInstance(new Object[] { object});
                    } catch (Exception exception1) {
                        throw new ConstructorException((String) null, (Mark) null, "Can\'t construct a java object for scalar " + node.getTag() + "; exception=" + exception1.getMessage(), node.getStartMark(), exception1);
                    }
                } else {
                    result = this.constructStandardJavaInstance(type, node);
                }

                return result;
            }
        }

        private Object constructStandardJavaInstance(Class type, ScalarNode node) {
            Object result;
            Construct contr;

            if (type == String.class) {
                contr = (Construct) Constructor.this.yamlConstructors.get(Tag.STR);
                result = contr.construct(node);
            } else if (type != Boolean.class && type != Boolean.TYPE) {
                if (type != Character.class && type != Character.TYPE) {
                    if (Date.class.isAssignableFrom(type)) {
                        contr = (Construct) Constructor.this.yamlConstructors.get(Tag.TIMESTAMP);
                        Date ex1 = (Date) contr.construct(node);

                        if (type == Date.class) {
                            result = ex1;
                        } else {
                            try {
                                java.lang.reflect.Constructor e = type.getConstructor(new Class[] { Long.TYPE});

                                result = e.newInstance(new Object[] { Long.valueOf(ex1.getTime())});
                            } catch (RuntimeException runtimeexception) {
                                throw runtimeexception;
                            } catch (Exception exception) {
                                throw new YAMLException("Cannot construct: \'" + type + "\'");
                            }
                        }
                    } else if (type != Float.class && type != Double.class && type != Float.TYPE && type != Double.TYPE && type != BigDecimal.class) {
                        if (type != Byte.class && type != Short.class && type != Integer.class && type != Long.class && type != BigInteger.class && type != Byte.TYPE && type != Short.TYPE && type != Integer.TYPE && type != Long.TYPE) {
                            if (Enum.class.isAssignableFrom(type)) {
                                String contr1 = node.getValue();

                                try {
                                    result = Enum.valueOf(type, contr1);
                                } catch (Exception exception1) {
                                    throw new YAMLException("Unable to find enum value \'" + contr1 + "\' for enum class: " + type.getName());
                                }
                            } else if (Calendar.class.isAssignableFrom(type)) {
                                SafeConstructor.ConstructYamlTimestamp contr2 = new SafeConstructor.ConstructYamlTimestamp();

                                contr2.construct(node);
                                result = contr2.getCalendar();
                            } else if (Number.class.isAssignableFrom(type)) {
                                SafeConstructor.ConstructYamlFloat contr3 = Constructor.this.new ConstructYamlFloat();

                                result = contr3.construct(node);
                            } else if (UUID.class == type) {
                                result = UUID.fromString(node.getValue());
                            } else {
                                if (!Constructor.this.yamlConstructors.containsKey(node.getTag())) {
                                    throw new YAMLException("Unsupported class: " + type);
                                }

                                result = ((Construct) Constructor.this.yamlConstructors.get(node.getTag())).construct(node);
                            }
                        } else {
                            contr = (Construct) Constructor.this.yamlConstructors.get(Tag.INT);
                            result = contr.construct(node);
                            if (type != Byte.class && type != Byte.TYPE) {
                                if (type != Short.class && type != Short.TYPE) {
                                    if (type != Integer.class && type != Integer.TYPE) {
                                        if (type != Long.class && type != Long.TYPE) {
                                            result = new BigInteger(result.toString());
                                        } else {
                                            result = Long.valueOf(result.toString());
                                        }
                                    } else {
                                        result = Integer.valueOf(Integer.parseInt(result.toString()));
                                    }
                                } else {
                                    result = Short.valueOf(result.toString());
                                }
                            } else {
                                result = Byte.valueOf(result.toString());
                            }
                        }
                    } else if (type == BigDecimal.class) {
                        result = new BigDecimal(node.getValue());
                    } else {
                        contr = (Construct) Constructor.this.yamlConstructors.get(Tag.FLOAT);
                        result = contr.construct(node);
                        if (type == Float.class || type == Float.TYPE) {
                            result = new Float(((Double) result).doubleValue());
                        }
                    }
                } else {
                    contr = (Construct) Constructor.this.yamlConstructors.get(Tag.STR);
                    String ex = (String) contr.construct(node);

                    if (ex.length() == 0) {
                        result = null;
                    } else {
                        if (ex.length() != 1) {
                            throw new YAMLException("Invalid node Character: \'" + ex + "\'; length: " + ex.length());
                        }

                        result = Character.valueOf(ex.charAt(0));
                    }
                }
            } else {
                contr = (Construct) Constructor.this.yamlConstructors.get(Tag.BOOL);
                result = contr.construct(node);
            }

            return result;
        }
    }

    protected class ConstructYamlObject implements Construct {

        private Construct getConstructor(Node node) {
            Class cl = Constructor.this.getClassForNode(node);

            node.setType(cl);
            Construct constructor = (Construct) Constructor.this.yamlClassConstructors.get(node.getNodeId());

            return constructor;
        }

        public Object construct(Node node) {
            Object result = null;

            try {
                result = this.getConstructor(node).construct(node);
                return result;
            } catch (ConstructorException constructorexception) {
                throw constructorexception;
            } catch (Exception exception) {
                throw new ConstructorException((String) null, (Mark) null, "Can\'t construct a java object for " + node.getTag() + "; exception=" + exception.getMessage(), node.getStartMark(), exception);
            }
        }

        public void construct2ndStep(Node node, Object object) {
            try {
                this.getConstructor(node).construct2ndStep(node, object);
            } catch (Exception exception) {
                throw new ConstructorException((String) null, (Mark) null, "Can\'t construct a second step for a java object for " + node.getTag() + "; exception=" + exception.getMessage(), node.getStartMark(), exception);
            }
        }
    }

    protected class ConstructMapping implements Construct {

        public Object construct(Node node) {
            MappingNode mnode = (MappingNode) node;

            if (Map.class.isAssignableFrom(node.getType())) {
                return node.isTwoStepsConstruction() ? Constructor.this.newMap(mnode) : Constructor.this.constructMapping(mnode);
            } else if (Collection.class.isAssignableFrom(node.getType())) {
                return node.isTwoStepsConstruction() ? Constructor.this.newSet(mnode) : Constructor.this.constructSet(mnode);
            } else {
                Object obj = Constructor.this.newInstance(mnode);

                return node.isTwoStepsConstruction() ? obj : this.constructJavaBean2ndStep(mnode, obj);
            }
        }

        public void construct2ndStep(Node node, Object object) {
            if (Map.class.isAssignableFrom(node.getType())) {
                Constructor.this.constructMapping2ndStep((MappingNode) node, (Map) object);
            } else if (Set.class.isAssignableFrom(node.getType())) {
                Constructor.this.constructSet2ndStep((MappingNode) node, (Set) object);
            } else {
                this.constructJavaBean2ndStep((MappingNode) node, object);
            }

        }

        protected Object constructJavaBean2ndStep(MappingNode node, Object object) {
            Constructor.this.flattenMapping(node);
            Class beanType = node.getType();
            List nodeValue = node.getValue();
            Iterator iterator = nodeValue.iterator();

            while (iterator.hasNext()) {
                NodeTuple tuple = (NodeTuple) iterator.next();

                if (!(tuple.getKeyNode() instanceof ScalarNode)) {
                    throw new YAMLException("Keys must be scalars but found: " + tuple.getKeyNode());
                }

                ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
                Node valueNode = tuple.getValueNode();

                keyNode.setType(String.class);
                String key = (String) Constructor.this.constructObject(keyNode);

                try {
                    TypeDescription e = (TypeDescription) Constructor.this.typeDefinitions.get(beanType);
                    Property property = e == null ? this.getProperty(beanType, key) : e.getProperty(key);

                    if (!property.isWritable()) {
                        throw new YAMLException("No writable property \'" + key + "\' on class: " + beanType.getName());
                    }

                    valueNode.setType(property.getType());
                    boolean typeDetected = e != null ? e.setupPropertyType(key, valueNode) : false;

                    if (!typeDetected && valueNode.getNodeId() != NodeId.scalar) {
                        Class[] value = property.getActualTypeArguments();

                        if (value != null && value.length > 0) {
                            Class ketType;

                            if (valueNode.getNodeId() == NodeId.sequence) {
                                ketType = value[0];
                                SequenceNode valueType = (SequenceNode) valueNode;

                                valueType.setListType(ketType);
                            } else if (Set.class.isAssignableFrom(valueNode.getType())) {
                                ketType = value[0];
                                MappingNode valueType1 = (MappingNode) valueNode;

                                valueType1.setOnlyKeyType(ketType);
                                valueType1.setUseClassConstructor(Boolean.valueOf(true));
                            } else if (Map.class.isAssignableFrom(valueNode.getType())) {
                                ketType = value[0];
                                Class valueType2 = value[1];
                                MappingNode mnode = (MappingNode) valueNode;

                                mnode.setTypes(ketType, valueType2);
                                mnode.setUseClassConstructor(Boolean.valueOf(true));
                            }
                        }
                    }

                    Object value1 = e != null ? this.newInstance(e, key, valueNode) : Constructor.this.constructObject(valueNode);

                    if ((property.getType() == Float.TYPE || property.getType() == Float.class) && value1 instanceof Double) {
                        value1 = Float.valueOf(((Double) value1).floatValue());
                    }

                    if (property.getType() == String.class && Tag.BINARY.equals(valueNode.getTag()) && value1 instanceof byte[]) {
                        value1 = new String((byte[]) ((byte[]) value1));
                    }

                    if (e == null || !e.setProperty(object, key, value1)) {
                        property.set(object, value1);
                    }
                } catch (Exception exception) {
                    throw new ConstructorException("Cannot create property=" + key + " for JavaBean=" + object, node.getStartMark(), exception.getMessage(), valueNode.getStartMark(), exception);
                }
            }

            return object;
        }

        private Object newInstance(TypeDescription memberDescription, String propertyName, Node node) {
            Object newInstance = memberDescription.newInstance(propertyName, node);

            if (newInstance != null) {
                Constructor.this.constructedObjects.put(node, newInstance);
                return Constructor.this.constructObjectNoCheck(node);
            } else {
                return Constructor.this.constructObject(node);
            }
        }

        protected Property getProperty(Class type, String name) {
            return Constructor.this.getPropertyUtils().getProperty(type, name);
        }
    }
}
