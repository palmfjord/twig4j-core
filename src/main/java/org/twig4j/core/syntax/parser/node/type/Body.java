package org.twig4j.core.syntax.parser.node.type;

import org.twig4j.core.syntax.parser.node.Node;

import java.util.ArrayList;
import java.util.HashMap;

public class Body extends Node {
    public Body(ArrayList<Node> nodes, HashMap<String, Object> attributes, Integer line, String tag) {
        super(nodes, attributes, line, tag);
    }

    public Body(Integer line) {
        super(line);
    }
}
