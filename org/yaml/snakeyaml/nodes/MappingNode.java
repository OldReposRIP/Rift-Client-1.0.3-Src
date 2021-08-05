package org.yaml.snakeyaml.nodes;

import java.util.Iterator;
import java.util.List;
import org.yaml.snakeyaml.error.Mark;

public class MappingNode extends CollectionNode {

    private List value;
    private boolean merged;

    public MappingNode(Tag tag, boolean resolved, List value, Mark startMark, Mark endMark, Boolean flowStyle) {
        super(tag, startMark, endMark, flowStyle);
        this.merged = false;
        if (value == null) {
            throw new NullPointerException("value in a Node is required.");
        } else {
            this.value = value;
            this.resolved = resolved;
        }
    }

    public MappingNode(Tag tag, List value, Boolean flowStyle) {
        this(tag, true, value, (Mark) null, (Mark) null, flowStyle);
    }

    public NodeId getNodeId() {
        return NodeId.mapping;
    }

    public List getValue() {
        return this.value;
    }

    public void setValue(List merge) {
        this.value = merge;
    }

    public void setOnlyKeyType(Class keyType) {
        Iterator iterator = this.value.iterator();

        while (iterator.hasNext()) {
            NodeTuple nodes = (NodeTuple) iterator.next();

            nodes.getKeyNode().setType(keyType);
        }

    }

    public void setTypes(Class keyType, Class valueType) {
        Iterator iterator = this.value.iterator();

        while (iterator.hasNext()) {
            NodeTuple nodes = (NodeTuple) iterator.next();

            nodes.getValueNode().setType(valueType);
            nodes.getKeyNode().setType(keyType);
        }

    }

    public String toString() {
        StringBuilder buf = new StringBuilder();

        for (Iterator iterator = this.getValue().iterator(); iterator.hasNext(); buf.append(" }")) {
            NodeTuple node = (NodeTuple) iterator.next();

            buf.append("{ key=");
            buf.append(node.getKeyNode());
            buf.append("; value=");
            if (node.getValueNode() instanceof CollectionNode) {
                buf.append(System.identityHashCode(node.getValueNode()));
            } else {
                buf.append(node.toString());
            }
        }

        String values = buf.toString();

        return "<" + this.getClass().getName() + " (tag=" + this.getTag() + ", values=" + values + ")>";
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public boolean isMerged() {
        return this.merged;
    }
}
