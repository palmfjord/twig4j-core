package org.twigjava.syntax.parser.node.type.expression;

import org.junit.Assert;
import org.junit.Test;
import org.twigjava.Environment;
import org.twigjava.compiler.ClassCompiler;
import org.twigjava.exception.LoaderException;
import org.twigjava.exception.TwigRuntimeException;
import org.twigjava.syntax.parser.node.Node;

public class BinaryConcatTests {
    @Test
    public void testCompile() throws LoaderException, TwigRuntimeException {
        ClassCompiler compiler = new ClassCompiler(new Environment());
        Node left = new Constant(1, 1);
        Node right = new Constant(2, 2);
        BinaryConcat concatNode = new BinaryConcat(left, right, 1);

        concatNode.compile(compiler);

        Assert.assertEquals(
                "Code should be compiled correctly",
                "(String.valueOf(1).concat(String.valueOf(2)))",
                compiler.getSourceCode()
        );
    }
}