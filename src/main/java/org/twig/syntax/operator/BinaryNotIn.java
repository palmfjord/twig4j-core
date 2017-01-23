package org.twig.syntax.operator;

public class BinaryNotIn implements Operator {
    @Override
    public Integer getPrecedence() {
        return 20;
    }

    @Override
    public Class getNodeClass() {
        return org.twig.syntax.parser.node.type.expression.BinaryNotIn.class;
    }

    @Override
    public Associativity getAssociativity() {
        return Associativity.LEFT;
    }
}