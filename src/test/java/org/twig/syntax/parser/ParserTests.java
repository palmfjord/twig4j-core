package org.twig.syntax.parser;

import org.junit.Assert;
import org.junit.Test;
import org.twig.exception.SyntaxErrorException;
import org.twig.syntax.Token;
import org.twig.syntax.TokenStream;
import org.twig.syntax.parser.node.Module;
import org.twig.syntax.parser.node.Node;
import org.twig.syntax.parser.node.type.PrintExpression;
import org.twig.syntax.parser.node.type.expression.Constant;
import org.twig.syntax.parser.node.type.expression.Name;

import static org.mockito.Mockito.*;

public class ParserTests {
    @Test
    public void canParseText() throws SyntaxErrorException {
        TokenStream tokenStream = new TokenStream("aFile");
        tokenStream.add(new Token(Token.Type.TEXT, "Hello world!", 1));
        tokenStream.add(new Token(Token.Type.EOF, null, 2));

        Parser parser = new Parser();
        Module module = parser.parse(tokenStream);

        Node bodyNode = module.getBodyNode();
        Assert.assertEquals("Module node 1 should be of type text", "Hello world!", bodyNode.getAttribute("data"));
    }

    @Test
    public void canParseStringPrint() throws SyntaxErrorException {
        TokenStream tokenStream = new TokenStream("aFile");
        tokenStream.add(new Token(Token.Type.VAR_START, null, 1));
        tokenStream.add(new Token(Token.Type.STRING, "foo", 1));
        tokenStream.add(new Token(Token.Type.VAR_END, null, 1));
        tokenStream.add(new Token(Token.Type.EOF, null, 1));

        Parser parser = new Parser();

        Constant stringConstant = new Constant("foo", 1);
        Module module = parser.parse(tokenStream);

        Assert.assertEquals(
                "First node should be of type PrintExpression",
                PrintExpression.class,
                module.getBodyNode().getClass()
        );
        Assert.assertEquals(
                "PrintExpession should contain constant",
                stringConstant.getAttribute("data"),
                module.getBodyNode().getNode(0).getAttribute("data")
        );
    }

    @Test
    public void canParseVariable() throws SyntaxErrorException {
        TokenStream tokenStream = new TokenStream("aFile");
        tokenStream.add(new Token(Token.Type.VAR_START, null, 1));
        tokenStream.add(new Token(Token.Type.NAME, "foo", 1));
        tokenStream.add(new Token(Token.Type.VAR_END, null, 1));
        tokenStream.add(new Token(Token.Type.EOF, null, 1));

        Parser parser = new Parser();

        Name name = new Name("foo", 1);
        Module module = parser.parse(tokenStream);

        Assert.assertEquals(
                "Body should be af type PrintExpression",
                PrintExpression.class,
                module.getBodyNode().getClass()
        );
        Assert.assertEquals(
                "Variable should be of type Name",
                Name.class,
                module.getBodyNode().getNode(0).getClass()
        );
        Assert.assertEquals(
                "Variable name should be correct",
                name.getAttribute("name"),
                module.getBodyNode().getNode(0).getAttribute("name")
        );
    }
}
