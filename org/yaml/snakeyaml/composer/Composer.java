package org.yaml.snakeyaml.composer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.events.AliasEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.NodeEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.SequenceStartEvent;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.parser.Parser;
import org.yaml.snakeyaml.resolver.Resolver;

public class Composer {

    protected final Parser parser;
    private final Resolver resolver;
    private final Map anchors;
    private final Set recursiveNodes;

    public Composer(Parser parser, Resolver resolver) {
        this.parser = parser;
        this.resolver = resolver;
        this.anchors = new HashMap();
        this.recursiveNodes = new HashSet();
    }

    public boolean checkNode() {
        if (this.parser.checkEvent(Event.ID.StreamStart)) {
            this.parser.getEvent();
        }

        return !this.parser.checkEvent(Event.ID.StreamEnd);
    }

    public Node getNode() {
        return !this.parser.checkEvent(Event.ID.StreamEnd) ? this.composeDocument() : null;
    }

    public Node getSingleNode() {
        this.parser.getEvent();
        Node document = null;

        if (!this.parser.checkEvent(Event.ID.StreamEnd)) {
            document = this.composeDocument();
        }

        if (!this.parser.checkEvent(Event.ID.StreamEnd)) {
            Event event = this.parser.getEvent();

            throw new ComposerException("expected a single document in the stream", document.getStartMark(), "but found another document", event.getStartMark());
        } else {
            this.parser.getEvent();
            return document;
        }
    }

    private Node composeDocument() {
        this.parser.getEvent();
        Node node = this.composeNode((Node) null);

        this.parser.getEvent();
        this.anchors.clear();
        this.recursiveNodes.clear();
        return node;
    }

    private Node composeNode(Node parent) {
        this.recursiveNodes.add(parent);
        Node node = null;
        String anchor;

        if (this.parser.checkEvent(Event.ID.Alias)) {
            AliasEvent event = (AliasEvent) this.parser.getEvent();

            anchor = event.getAnchor();
            if (!this.anchors.containsKey(anchor)) {
                throw new ComposerException((String) null, (Mark) null, "found undefined alias " + anchor, event.getStartMark());
            }

            node = (Node) this.anchors.get(anchor);
            if (this.recursiveNodes.remove(node)) {
                node.setTwoStepsConstruction(true);
            }
        } else {
            NodeEvent event1 = (NodeEvent) this.parser.peekEvent();

            anchor = null;
            anchor = event1.getAnchor();
            if (this.parser.checkEvent(Event.ID.Scalar)) {
                node = this.composeScalarNode(anchor);
            } else if (this.parser.checkEvent(Event.ID.SequenceStart)) {
                node = this.composeSequenceNode(anchor);
            } else {
                node = this.composeMappingNode(anchor);
            }
        }

        this.recursiveNodes.remove(parent);
        return node;
    }

    protected Node composeScalarNode(String anchor) {
        ScalarEvent ev = (ScalarEvent) this.parser.getEvent();
        String tag = ev.getTag();
        boolean resolved = false;
        Tag nodeTag;

        if (tag != null && !tag.equals("!")) {
            nodeTag = new Tag(tag);
        } else {
            nodeTag = this.resolver.resolve(NodeId.scalar, ev.getValue(), ev.getImplicit().canOmitTagInPlainScalar());
            resolved = true;
        }

        ScalarNode node = new ScalarNode(nodeTag, resolved, ev.getValue(), ev.getStartMark(), ev.getEndMark(), ev.getStyle());

        if (anchor != null) {
            node.setAnchor(anchor);
            this.anchors.put(anchor, node);
        }

        return node;
    }

    protected Node composeSequenceNode(String anchor) {
        SequenceStartEvent startEvent = (SequenceStartEvent) this.parser.getEvent();
        String tag = startEvent.getTag();
        boolean resolved = false;
        Tag nodeTag;

        if (tag != null && !tag.equals("!")) {
            nodeTag = new Tag(tag);
        } else {
            nodeTag = this.resolver.resolve(NodeId.sequence, (String) null, startEvent.getImplicit());
            resolved = true;
        }

        ArrayList children = new ArrayList();
        SequenceNode node = new SequenceNode(nodeTag, resolved, children, startEvent.getStartMark(), (Mark) null, startEvent.getFlowStyle());

        if (anchor != null) {
            node.setAnchor(anchor);
            this.anchors.put(anchor, node);
        }

        while (!this.parser.checkEvent(Event.ID.SequenceEnd)) {
            children.add(this.composeNode(node));
        }

        Event endEvent = this.parser.getEvent();

        node.setEndMark(endEvent.getEndMark());
        return node;
    }

    protected Node composeMappingNode(String anchor) {
        MappingStartEvent startEvent = (MappingStartEvent) this.parser.getEvent();
        String tag = startEvent.getTag();
        boolean resolved = false;
        Tag nodeTag;

        if (tag != null && !tag.equals("!")) {
            nodeTag = new Tag(tag);
        } else {
            nodeTag = this.resolver.resolve(NodeId.mapping, (String) null, startEvent.getImplicit());
            resolved = true;
        }

        ArrayList children = new ArrayList();
        MappingNode node = new MappingNode(nodeTag, resolved, children, startEvent.getStartMark(), (Mark) null, startEvent.getFlowStyle());

        if (anchor != null) {
            node.setAnchor(anchor);
            this.anchors.put(anchor, node);
        }

        while (!this.parser.checkEvent(Event.ID.MappingEnd)) {
            this.composeMappingChildren(children, node);
        }

        Event endEvent = this.parser.getEvent();

        node.setEndMark(endEvent.getEndMark());
        return node;
    }

    protected void composeMappingChildren(List children, MappingNode node) {
        Node itemKey = this.composeKeyNode(node);

        if (itemKey.getTag().equals(Tag.MERGE)) {
            node.setMerged(true);
        }

        Node itemValue = this.composeValueNode(node);

        children.add(new NodeTuple(itemKey, itemValue));
    }

    protected Node composeKeyNode(MappingNode node) {
        return this.composeNode(node);
    }

    protected Node composeValueNode(MappingNode node) {
        return this.composeNode(node);
    }
}
