package org.yaml.snakeyaml.serializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.emitter.Emitable;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.events.AliasEvent;
import org.yaml.snakeyaml.events.DocumentEndEvent;
import org.yaml.snakeyaml.events.DocumentStartEvent;
import org.yaml.snakeyaml.events.ImplicitTuple;
import org.yaml.snakeyaml.events.MappingEndEvent;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.SequenceEndEvent;
import org.yaml.snakeyaml.events.SequenceStartEvent;
import org.yaml.snakeyaml.events.StreamEndEvent;
import org.yaml.snakeyaml.events.StreamStartEvent;
import org.yaml.snakeyaml.nodes.AnchorNode;
import org.yaml.snakeyaml.nodes.CollectionNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.resolver.Resolver;

public final class Serializer {

    private final Emitable emitter;
    private final Resolver resolver;
    private boolean explicitStart;
    private boolean explicitEnd;
    private DumperOptions.Version useVersion;
    private Map useTags;
    private Set serializedNodes;
    private Map anchors;
    private AnchorGenerator anchorGenerator;
    private Boolean closed;
    private Tag explicitRoot;

    public Serializer(Emitable emitter, Resolver resolver, DumperOptions opts, Tag rootTag) {
        this.emitter = emitter;
        this.resolver = resolver;
        this.explicitStart = opts.isExplicitStart();
        this.explicitEnd = opts.isExplicitEnd();
        if (opts.getVersion() != null) {
            this.useVersion = opts.getVersion();
        }

        this.useTags = opts.getTags();
        this.serializedNodes = new HashSet();
        this.anchors = new HashMap();
        this.anchorGenerator = opts.getAnchorGenerator();
        this.closed = null;
        this.explicitRoot = rootTag;
    }

    public void open() throws IOException {
        if (this.closed == null) {
            this.emitter.emit(new StreamStartEvent((Mark) null, (Mark) null));
            this.closed = Boolean.FALSE;
        } else if (Boolean.TRUE.equals(this.closed)) {
            throw new SerializerException("serializer is closed");
        } else {
            throw new SerializerException("serializer is already opened");
        }
    }

    public void close() throws IOException {
        if (this.closed == null) {
            throw new SerializerException("serializer is not opened");
        } else {
            if (!Boolean.TRUE.equals(this.closed)) {
                this.emitter.emit(new StreamEndEvent((Mark) null, (Mark) null));
                this.closed = Boolean.TRUE;
            }

        }
    }

    public void serialize(Node node) throws IOException {
        if (this.closed == null) {
            throw new SerializerException("serializer is not opened");
        } else if (this.closed.booleanValue()) {
            throw new SerializerException("serializer is closed");
        } else {
            this.emitter.emit(new DocumentStartEvent((Mark) null, (Mark) null, this.explicitStart, this.useVersion, this.useTags));
            this.anchorNode(node);
            if (this.explicitRoot != null) {
                node.setTag(this.explicitRoot);
            }

            this.serializeNode(node, (Node) null);
            this.emitter.emit(new DocumentEndEvent((Mark) null, (Mark) null, this.explicitEnd));
            this.serializedNodes.clear();
            this.anchors.clear();
        }
    }

    private void anchorNode(Node node) {
        if (node.getNodeId() == NodeId.anchor) {
            node = ((AnchorNode) node).getRealNode();
        }

        if (this.anchors.containsKey(node)) {
            String seqNode = (String) this.anchors.get(node);

            if (null == seqNode) {
                seqNode = this.anchorGenerator.nextAnchor(node);
                this.anchors.put(node, seqNode);
            }
        } else {
            this.anchors.put(node, (Object) null);
            switch (node.getNodeId()) {
            case sequence:
                SequenceNode seqNode1 = (SequenceNode) node;
                List list = seqNode1.getValue();
                Iterator mnode1 = list.iterator();

                while (mnode1.hasNext()) {
                    Node map1 = (Node) mnode1.next();

                    this.anchorNode(map1);
                }

                return;

            case mapping:
                MappingNode mnode = (MappingNode) node;
                List map = mnode.getValue();
                Iterator iterator = map.iterator();

                while (iterator.hasNext()) {
                    NodeTuple object = (NodeTuple) iterator.next();
                    Node key = object.getKeyNode();
                    Node value = object.getValueNode();

                    this.anchorNode(key);
                    this.anchorNode(value);
                }
            }
        }

    }

    private void serializeNode(Node node, Node parent) throws IOException {
        if (node.getNodeId() == NodeId.anchor) {
            node = ((AnchorNode) node).getRealNode();
        }

        String tAlias = (String) this.anchors.get(node);

        if (this.serializedNodes.contains(node)) {
            this.emitter.emit(new AliasEvent(tAlias, (Mark) null, (Mark) null));
        } else {
            this.serializedNodes.add(node);
            switch (node.getNodeId()) {
            case sequence:
                SequenceNode seqNode = (SequenceNode) node;
                boolean implicitS = node.getTag().equals(this.resolver.resolve(NodeId.sequence, (String) null, true));

                this.emitter.emit(new SequenceStartEvent(tAlias, node.getTag().getValue(), implicitS, (Mark) null, (Mark) null, seqNode.getFlowStyle()));
                List list = seqNode.getValue();
                Iterator implicitTag = list.iterator();

                while (implicitTag.hasNext()) {
                    Node implicitM = (Node) implicitTag.next();

                    this.serializeNode(implicitM, node);
                }

                this.emitter.emit(new SequenceEndEvent((Mark) null, (Mark) null));
                break;

            case scalar:
                ScalarNode scalarNode = (ScalarNode) node;
                Tag detectedTag = this.resolver.resolve(NodeId.scalar, scalarNode.getValue(), true);
                Tag defaultTag = this.resolver.resolve(NodeId.scalar, scalarNode.getValue(), false);
                ImplicitTuple tuple = new ImplicitTuple(node.getTag().equals(detectedTag), node.getTag().equals(defaultTag));
                ScalarEvent event = new ScalarEvent(tAlias, node.getTag().getValue(), tuple, scalarNode.getValue(), (Mark) null, (Mark) null, scalarNode.getStyle());

                this.emitter.emit(event);
                break;

            default:
                Tag implicitTag1 = this.resolver.resolve(NodeId.mapping, (String) null, true);
                boolean implicitM1 = node.getTag().equals(implicitTag1);

                this.emitter.emit(new MappingStartEvent(tAlias, node.getTag().getValue(), implicitM1, (Mark) null, (Mark) null, ((CollectionNode) node).getFlowStyle()));
                MappingNode mnode = (MappingNode) node;
                List map = mnode.getValue();
                Iterator iterator = map.iterator();

                while (iterator.hasNext()) {
                    NodeTuple row = (NodeTuple) iterator.next();
                    Node key = row.getKeyNode();
                    Node value = row.getValueNode();

                    this.serializeNode(key, mnode);
                    this.serializeNode(value, mnode);
                }

                this.emitter.emit(new MappingEndEvent((Mark) null, (Mark) null));
            }
        }

    }
}
