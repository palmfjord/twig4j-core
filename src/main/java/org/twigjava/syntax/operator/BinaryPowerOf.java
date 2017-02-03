package org.twigjava.syntax.operator;

public class BinaryPowerOf implements Operator {
    @Override
    public Integer getPrecedence() {
        return 200;
    }

    @Override
    public Class getNodeClass() {
        return org.twigjava.syntax.parser.node.type.expression.BinaryPowerOf.class;
    }

    @Override
    public Associativity getAssociativity() {
        return Associativity.RIGHT;
    }
}