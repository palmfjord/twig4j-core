package org.twig4j.core.syntax.parser;

import org.junit.Assert;
import org.junit.Test;
import org.twig4j.core.Environment;
import org.twig4j.core.exception.SyntaxErrorException;
import org.twig4j.core.exception.Twig4jException;
import org.twig4j.core.exception.Twig4jRuntimeException;
import org.twig4j.core.syntax.Token;
import org.twig4j.core.syntax.TokenStream;
import org.twig4j.core.syntax.operator.UnaryNot;
import org.twig4j.core.syntax.parser.node.Module;
import org.twig4j.core.syntax.parser.node.Node;
import org.twig4j.core.syntax.parser.node.type.PrintExpression;
import org.twig4j.core.syntax.parser.node.type.expression.*;
import org.twig4j.core.syntax.parser.node.type.expression.BinaryAdd;
import org.twig4j.core.syntax.parser.node.type.expression.BinaryConcat;
import org.twig4j.core.syntax.parser.node.type.expression.BinaryMultiply;
import org.twig4j.core.syntax.parser.node.type.expression.Filter;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class ExpressionParserTests {
    @Test
    public void testParsePrimaryExpressionBool() throws SyntaxErrorException, Twig4jRuntimeException {
        Token trueToken = new Token(Token.Type.NAME, "true", 1);
        TokenStream tokenStream = new TokenStream();
        tokenStream.add(trueToken);
        tokenStream.add(new Token(Token.Type.EOF, null, 1));

        Parser parserStub = mock(Parser.class);
        when(parserStub.getCurrentToken()).thenReturn(trueToken);
        when(parserStub.getTokenStream()).thenReturn(tokenStream);

        ExpressionParser expressionParser = new ExpressionParser(parserStub);
        Node boolConstant = expressionParser.parsePrimaryExpression();

        Assert.assertEquals(
                "Returned node should be a Constant",
                Constant.class.toString(),
                boolConstant.getClass().toString()
        );
        Assert.assertEquals(
                "Returned node should have value true",
                true,
                boolConstant.getAttribute("data")
        );
    }

    @Test
    public void testParsePrimaryExpressionNull() throws SyntaxErrorException, Twig4jRuntimeException {
        Token nullToken = new Token(Token.Type.NAME, "null", 1);
        TokenStream tokenStream = new TokenStream();
        tokenStream.add(nullToken);
        tokenStream.add(new Token(Token.Type.EOF, null, 1));

        Parser parserStub = mock(Parser.class);
        when(parserStub.getCurrentToken()).thenReturn(nullToken);
        when(parserStub.getTokenStream()).thenReturn(tokenStream);

        ExpressionParser expressionParser = new ExpressionParser(parserStub);
        Node boolConstant = expressionParser.parsePrimaryExpression();

        Assert.assertEquals(
                "Returned node should be a Constant",
                Constant.class.toString(),
                boolConstant.getClass().toString()
        );
        Assert.assertEquals(
                "Returned node should have value null",
                null,
                boolConstant.getAttribute("data")
        );
    }

    @Test
    public void testParseSimpleString() throws SyntaxErrorException, Twig4jRuntimeException {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.Type.STRING, "foo", 1));
        tokens.add(new Token(Token.Type.EOF, null, 1));
        TokenStream tokenStream = new TokenStream(tokens);
        Parser parser = mock(Parser.class);

        ExpressionParser expressionParser = new ExpressionParser(parser);

        when(parser.getTokenStream()).thenReturn(tokenStream);
        when(parser.getCurrentToken()).thenReturn(tokens.get(0));

        Node parsedString = expressionParser.parseStringExpression();

        Node expectedNode = new StringConstant("foo", 1);

        Assert.assertEquals(
                "Type of returned node should be constant",
                expectedNode.getClass(),
                parsedString.getClass()
        );
        Assert.assertEquals(
                "Value should be \"foo\"",
                expectedNode.getAttribute("data"),
                parsedString.getAttribute("data")
        );
    }

    @Test
    public void testParseInterpolatedString() throws SyntaxErrorException, Twig4jRuntimeException {
        ArrayList<Token> tokens = new ArrayList<>();
        // Token stream for "foo#{"bar"}"
        //tokens.add(new Token(Token.Type.VAR_START, null, 1));
        tokens.add(new Token(Token.Type.STRING, "foo", 1));
        tokens.add(new Token(Token.Type.INTERPLATION_START, null, 1));
        tokens.add(new Token(Token.Type.STRING, "bar", 1));
        tokens.add(new Token(Token.Type.INTERPOLATION_END, null, 1));
        tokens.add(new Token(Token.Type.EOF, null, 1));
        TokenStream tokenStream = new TokenStream(tokens);

        Parser parser = new Parser(new Environment());
        ExpressionParser expressionParser = new ExpressionParser(parser);
        parser
                .setExpressionParser(expressionParser)
                .setTokenStream(tokenStream);

        Node parsedString = expressionParser.parseStringExpression();

        BinaryConcat expectedNode = new BinaryConcat(new StringConstant("foo", 1), new StringConstant("bar", 1), 1);

        Assert.assertEquals(
                "Type of returned node should be BinaryConcat",
                expectedNode.getClass(),
                parsedString.getClass()
        );
        Assert.assertEquals(
                "Type of left should be Constant",
                expectedNode.getLeftNode().getClass(),
                parsedString.getNode(0).getClass()
        );
        Assert.assertEquals(
                "Value of left should be \"foo\"",
                expectedNode.getLeftNode().getAttribute("data"),
                parsedString.getNode(0).getAttribute("data")
        );
        Assert.assertEquals(
                "Type of right should be Constant",
                expectedNode.getRightNode().getClass(),
                parsedString.getNode(1).getClass()
        );
        Assert.assertEquals(
                "Value of right should be \"bar\"",
                expectedNode.getRightNode().getAttribute("data"),
                parsedString.getNode(1).getAttribute("data")
        );
    }

    @Test
    public void testParsePrimaryExpressionString() throws SyntaxErrorException, Twig4jRuntimeException {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.Type.STRING, "foo", 1));
        tokens.add(new Token(Token.Type.EOF, null, 1));
        TokenStream tokenStream = new TokenStream(tokens);
        Parser parser = mock(Parser.class);

        ExpressionParser expressionParser = new ExpressionParser(parser);

        when(parser.getTokenStream()).thenReturn(tokenStream);
        when(parser.getCurrentToken()).thenReturn(tokens.get(0));

        Node parsedString = expressionParser.parsePrimaryExpression();
        Node expectedNode = new StringConstant("foo", 1);

        Assert.assertEquals(
                "Type of returned node should be Constant",
                expectedNode.getClass(),
                parsedString.getClass()
        );
        Assert.assertEquals(
                "Value should be \"foo\"",
                expectedNode.getAttribute("data"),
                parsedString.getAttribute("data")
        );
    }

    @Test
    public void testParsePrimaryExpressionVariable() throws SyntaxErrorException, Twig4jRuntimeException {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.Type.NAME, "foo", 1));
        tokens.add(new Token(Token.Type.EOF, null, 1));
        TokenStream tokenStream = new TokenStream(tokens);
        Parser parser = mock(Parser.class);

        ExpressionParser expressionParser = new ExpressionParser(parser);

        when(parser.getTokenStream()).thenReturn(tokenStream);
        when(parser.getCurrentToken()).thenReturn(tokens.get(0));

        Node parsedString = expressionParser.parsePrimaryExpression();
        Node expectedNode = new Name("foo", 1);

        Assert.assertEquals(
                "Type of returned node should be Name",
                expectedNode.getClass(),
                parsedString.getClass()
        );
        Assert.assertEquals(
                "Value should be \"foo\"",
                expectedNode.getAttribute("name"),
                parsedString.getAttribute("name")
        );
    }

    @Test
    public void testParsePrimaryExpressionInverseBool() throws SyntaxErrorException, Twig4jRuntimeException {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.Type.NAME, "not", 1));
        tokens.add(new Token(Token.Type.NAME, "true", 1));
        tokens.add(new Token(Token.Type.EOF, null, 1));
        TokenStream tokenStream = new TokenStream(tokens);
        Environment env = new Environment();
        Parser parser = new Parser(env);
        env.addUnaryOperator("not", new UnaryNot());
        parser.setTokenStream(tokenStream);

        ExpressionParser expressionParser = new ExpressionParser(parser);

        Node parsedUnaryExpression = expressionParser.parseExpression();
        Node expectedNode = new org.twig4j.core.syntax.parser.node.type.expression.UnaryNot(new Constant(true, 1), 1);

        Assert.assertEquals(
                "Type of returned node should be UnaryNot",
                expectedNode.getClass(),
                parsedUnaryExpression.getClass()
        );
        Assert.assertEquals(
                "Contents should be the constant",
                expectedNode.getNode(0).getClass(),
                parsedUnaryExpression.getNode(0).getClass()
        );
    }

    @Test
    public void canParseAddition() throws SyntaxErrorException, Twig4jRuntimeException {
        TokenStream tokenStream = new TokenStream("aFile");
        tokenStream.add(new Token(Token.Type.VAR_START, null, 1));
        tokenStream.add(new Token(Token.Type.NUMBER, "1", 1));
        tokenStream.add(new Token(Token.Type.OPERATOR, "+", 1));
        tokenStream.add(new Token(Token.Type.NUMBER, "2", 1));
        tokenStream.add(new Token(Token.Type.VAR_END, null, 1));
        tokenStream.add(new Token(Token.Type.EOF, null, 1));

        Parser parser = new Parser(new Environment());
        Module module = parser.parse(tokenStream);

        Assert.assertEquals(
                "Body node should be a PrintExpression",
                PrintExpression.class,
                module.getBodyNode().getClass()
        );
        Assert.assertEquals(
                "Printed expression should be a binary add",
                BinaryAdd.class,
                module.getBodyNode().getNode(0).getClass()
        );
        Assert.assertEquals(
                "Left item should be number 1",
                1,
                module.getBodyNode().getNode(0).getNode(0).getAttribute("data")
        );
        Assert.assertEquals(
                "Right item should be number 2",
                2,
                module.getBodyNode().getNode(0).getNode(1).getAttribute("data")
        );
    }

    @Test
    public void canParseMathsWithParenthesis() throws SyntaxErrorException, Twig4jRuntimeException {
        TokenStream tokenStream = new TokenStream("aFile");
        tokenStream.add(new Token(Token.Type.VAR_START, null, 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, "(", 1));
        tokenStream.add(new Token(Token.Type.NUMBER, "1", 1));
        tokenStream.add(new Token(Token.Type.OPERATOR, "+", 1));
        tokenStream.add(new Token(Token.Type.NUMBER, "2", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, ")", 1));
        tokenStream.add(new Token(Token.Type.OPERATOR, "*", 1));
        tokenStream.add(new Token(Token.Type.NUMBER, "2", 1));
        tokenStream.add(new Token(Token.Type.VAR_END, null, 1));
        tokenStream.add(new Token(Token.Type.EOF, null, 1));

        Parser parser = new Parser(new Environment());
        Module module = parser.parse(tokenStream);

        Assert.assertEquals(
                "First node should be multiplication (since 1 + 1 is wrapped in parenthesises)",
                BinaryMultiply.class,
                module.getBodyNode().getNode(0).getClass()
        );
        Assert.assertEquals(
                "Multiplication node should have left expression binary addition",
                BinaryAdd.class,
                module.getBodyNode().getNode(0).getNode(0).getClass()
        );
        Assert.assertEquals(
                "Multiplication node should have right expression constant",
                Constant.class,
                module.getBodyNode().getNode(0).getNode(1).getClass()
        );
    }

    @Test(expected = SyntaxErrorException.class)
    public void cantParseUnclosedParenthesis() throws SyntaxErrorException, Twig4jRuntimeException {
        TokenStream tokenStream = new TokenStream("aFile");
        tokenStream.add(new Token(Token.Type.VAR_START, null, 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, "(", 1));
        tokenStream.add(new Token(Token.Type.NUMBER, "1", 1));
        tokenStream.add(new Token(Token.Type.VAR_END, null, 1));
        tokenStream.add(new Token(Token.Type.EOF, null, 1));

        Parser parser = new Parser(new Environment());
        parser.parse(tokenStream);
    }

    @Test
    public void canParseArguments() throws Twig4jException {
        TokenStream tokenStream = new TokenStream("aFile");
        tokenStream.add(new Token(Token.Type.PUNCTUATION, "(", 1));
        tokenStream.add(new Token(Token.Type.NAME, "foo", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, ",", 1));
        tokenStream.add(new Token(Token.Type.NUMBER, "1", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, ")", 1));
        tokenStream.add(new Token(Token.Type.EOF, null, 1));
        Parser parserStub = new Parser(new Environment());
        parserStub.setTokenStream(tokenStream);

        ExpressionParser expressionParser = new ExpressionParser(parserStub);
        Node arguments = expressionParser.parseArguments();

        Assert.assertEquals(
                "First argument should be of type name",
                Name.class,
                arguments.getNode(0).getClass()
        );
        Assert.assertEquals(
                "First argument should have value \"foo\"",
                "foo",
                arguments.getNode(0).getAttribute("name")
        );
        Assert.assertEquals(
                "2nd argument should be of type constant",
                Constant.class,
                arguments.getNode(1).getClass()
        );
        Assert.assertEquals(
                "2nd argument should have value 1",
                1,
                arguments.getNode(1).getAttribute("data")
        );
    }

    @Test
    public void canParseMethod() throws Twig4jException {
        TokenStream tokenStream = new TokenStream("aFile");
        tokenStream.add(new Token(Token.Type.NAME, "foo", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, ".", 1));
        tokenStream.add(new Token(Token.Type.NAME, "bar", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, "(", 1));
        tokenStream.add(new Token(Token.Type.STRING, "baz", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, ")", 1));
        tokenStream.add(new Token(Token.Type.EOF, null, 1));
        Parser parserStub = new Parser(new Environment());
        parserStub.setTokenStream(tokenStream);

        ExpressionParser expressionParser = new ExpressionParser(parserStub);

        Node parsedExpression = expressionParser.parseExpression();

        Assert.assertEquals(
                "Returned node should be of type GetAttr",
                GetAttr.class,
                parsedExpression.getClass()
        );
        Assert.assertEquals(
                "1st getattr node (the node) should be of type name",
                Name.class,
                parsedExpression.getNode(0).getClass()
        );
        Assert.assertEquals(
                "1st getattr node (the node should be \"foo\"",
                "foo",
                parsedExpression.getNode(0).getAttribute("name")
        );
        Assert.assertEquals(
                "2nd getattr node (the attribute) should be of type constant",
                Constant.class,
                parsedExpression.getNode(1).getClass()
        );
        Assert.assertEquals(
                "2nd getattr node (the attribute) should be \"bar\"",
                "bar",
                parsedExpression.getNode(1).getAttribute("data")
        );
        Assert.assertEquals(
                "3rd getattr node (the arguments) should be of type array",
                Array.class,
                parsedExpression.getNode(2).getClass()
        );
        Assert.assertEquals(
                "3rd getattr node (the attribute) should contain 1 sub node (=1 argument)",
                1,
                parsedExpression.getNode(2).getNodes().size()
        );
    }

    @Test
    public void canParseArray() throws Twig4jException {
        TokenStream tokenStream = new TokenStream("aFile");
        tokenStream.add(new Token(Token.Type.PUNCTUATION, "[", 1));
        tokenStream.add(new Token(Token.Type.STRING, "foo", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, ",", 1));
        tokenStream.add(new Token(Token.Type.STRING, "bar", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, "]", 1));
        tokenStream.add(new Token(Token.Type.VAR_END, null, 1));
        tokenStream.add(new Token(Token.Type.EOF, null, 1));
        Parser parser = new Parser(new Environment());
        parser.setTokenStream(tokenStream);

        ExpressionParser expressionParser = new ExpressionParser(parser);

        Expression expression = expressionParser.parseExpression();

        Assert.assertEquals(
                "Returned type should be of type array",
                Array.class,
                expression.getClass()
        );
        Assert.assertEquals(
                "First element of array should be foo",
                "foo",
                expression.getNode(0).getAttribute("data")
        );
        Assert.assertEquals(
                "2nd element of array should be bar",
                "bar",
                expression.getNode(1).getAttribute("data")
        );
    }

    @Test
    public void canParseArraySubscript() throws Twig4jException {
        TokenStream tokenStream = new TokenStream("aFile");
        tokenStream.add(new Token(Token.Type.PUNCTUATION, "[", 1));
        tokenStream.add(new Token(Token.Type.STRING, "foo", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, ",", 1));
        tokenStream.add(new Token(Token.Type.STRING, "bar", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, "]", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, "[", 1));
        tokenStream.add(new Token(Token.Type.NUMBER, "0", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, "]", 1));
        tokenStream.add(new Token(Token.Type.VAR_END, null, 1));
        tokenStream.add(new Token(Token.Type.EOF, null, 1));
        Parser parser = new Parser(new Environment());
        parser.setTokenStream(tokenStream);

        ExpressionParser expressionParser = new ExpressionParser(parser);

        Expression expression = expressionParser.parseExpression();

        Assert.assertEquals(
                "Returned type should be of type getAttr",
                GetAttr.class,
                expression.getClass()
        );
        Assert.assertEquals(
                "First element of GetAttr should be array",
                Array.class,
                expression.getNode(0).getClass()
        );
        Assert.assertEquals(
                "2nd element of GetAttr should be scalar int (=accessed array key)",
                0,
                expression.getNode(1).getAttribute("data")
        );
    }


    @Test
    public void canParseHash() throws Twig4jException {
        TokenStream tokenStream = new TokenStream("aFile");
        tokenStream.add(new Token(Token.Type.PUNCTUATION, "{", 1));
        tokenStream.add(new Token(Token.Type.NAME, "foo", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, ":", 1));
        tokenStream.add(new Token(Token.Type.STRING, "bar", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, ",", 1));
        tokenStream.add(new Token(Token.Type.STRING, "baz", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, ":", 1));
        tokenStream.add(new Token(Token.Type.STRING, "qux", 1));
        tokenStream.add(new Token(Token.Type.PUNCTUATION, "}", 1));
        tokenStream.add(new Token(Token.Type.VAR_END, null, 1));
        tokenStream.add(new Token(Token.Type.EOF, null, 1));
        Parser parser = new Parser(new Environment());
        parser.setTokenStream(tokenStream);

        ExpressionParser expressionParser = new ExpressionParser(parser);

        Expression expression = expressionParser.parseExpression();

        Assert.assertEquals(
                "Returned type should be of type Hash",
                Hash.class,
                expression.getClass()
        );
        Assert.assertEquals(
                "Attribute foo should be foo contents",
                "bar",
                ((Constant) expression.getAttribute("foo")).getAttribute("data")
        );
        Assert.assertEquals(
                "Attribute baz should be baz contents",
                "qux",
                ((Constant) expression.getAttribute("baz")).getAttribute("data")
        );
    }

    @Test
    public void testParseAssignments() throws SyntaxErrorException, Twig4jRuntimeException {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.Type.NAME, "foo", 1));
        tokens.add(new Token(Token.Type.PUNCTUATION, ",", 1));
        tokens.add(new Token(Token.Type.NAME, "bar", 1));
        tokens.add(new Token(Token.Type.EOF, null, 1));
        TokenStream tokenStream = new TokenStream(tokens);

        Parser parser = new Parser(new Environment());
        parser.setTokenStream(tokenStream);
        ExpressionParser expressionParser = new ExpressionParser(parser);

        List<String> names = expressionParser.parseAssignmentExpression();

        Assert.assertEquals("Number of items in list should be correct", 2, names.size());

        Assert.assertEquals("First item should be of correct value", "foo", names.get(0));
        Assert.assertEquals("Second item should be of correct value", "bar", names.get(1));
    }

    @Test
    public void testParseMultitargetStrings() throws SyntaxErrorException, Twig4jRuntimeException {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.Type.STRING, "foo", 1));
        tokens.add(new Token(Token.Type.PUNCTUATION, ",", 1));
        tokens.add(new Token(Token.Type.STRING, "bar", 1));
        tokens.add(new Token(Token.Type.EOF, null, 1));
        TokenStream tokenStream = new TokenStream(tokens);

        Parser parser = new Parser(new Environment());
        parser.setTokenStream(tokenStream);
        ExpressionParser expressionParser = new ExpressionParser(parser);

        Node parsedExpressions = expressionParser.parseMultitargetExpression();

        Assert.assertEquals("Number of items in list should be correct", 2, parsedExpressions.getNodes().size());

        Assert.assertEquals("First item should be of correct type", StringConstant.class, parsedExpressions.getNode(0).getClass());
        Assert.assertEquals("Second item should be of correct type", StringConstant.class, parsedExpressions.getNode(1).getClass());
    }

    @Test
    public void testParseMultitargetString() throws SyntaxErrorException, Twig4jRuntimeException {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.Type.STRING, "foo", 1));
        tokens.add(new Token(Token.Type.EOF, null, 1));
        TokenStream tokenStream = new TokenStream(tokens);

        Parser parser = new Parser(new Environment());
        parser.setTokenStream(tokenStream);
        ExpressionParser expressionParser = new ExpressionParser(parser);

        Node parsedExpressions = expressionParser.parseMultitargetExpression();

        Assert.assertEquals("Number of items in list should be correct", 1, parsedExpressions.getNodes().size());

        Assert.assertEquals("First item should be of correct type", StringConstant.class, parsedExpressions.getNode(0).getClass());
    }

    @Test
    public void canParseFilters() throws SyntaxErrorException, Twig4jRuntimeException {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.Type.PUNCTUATION, "|", 1));
        tokens.add(new Token(Token.Type.NAME, "upper", 1));
        tokens.add(new Token(Token.Type.PUNCTUATION, "(", 1));
        tokens.add(new Token(Token.Type.STRING, "bar", 1));
        tokens.add(new Token(Token.Type.PUNCTUATION, ")", 1));
        tokens.add(new Token(Token.Type.EOF, null, 1));
        TokenStream tokenStream = new TokenStream(tokens);

        Environment environment = mock(Environment.class);
        Parser parser = new Parser(environment);
        parser.setTokenStream(tokenStream);
        ExpressionParser expressionParser = new ExpressionParser(parser);

        try {
            when(environment.getFilter("upper")).thenReturn(new org.twig4j.core.filter.Filter("upper", getClass().getMethod("activateUpperFilter", String.class)));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Logic is dead! The declared method to use does not exist in test.");
        }

        Expression expectedBodyNode = new Constant("foo", 1);
        Expression parsedExpression = expressionParser.parseFilterExpression(expectedBodyNode);

        Assert.assertEquals("Should be of type filter", Filter.class, parsedExpression.getClass());
        Assert.assertEquals("Filter name node should be filter name", "upper", parsedExpression.getNode(1).getAttribute("data"));
        Assert.assertSame("Body node should be passed node", expectedBodyNode, parsedExpression.getNode(0));
        Assert.assertEquals("Number of arguments should be 1", 1, parsedExpression.getNode(2).getNodes().size());
        Assert.assertEquals("Argument passed should be of correct type", StringConstant.class, parsedExpression.getNode(2).getNode(0).getClass());
    }

    @Test
    public void canParseParentFunction() throws SyntaxErrorException, Twig4jRuntimeException {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.Type.NAME, "parent", 1));
        tokens.add(new Token(Token.Type.PUNCTUATION, "(", 1));
        tokens.add(new Token(Token.Type.PUNCTUATION, ")", 1));
        tokens.add(new Token(Token.Type.EOF, null, 1));
        TokenStream tokenStream = new TokenStream(tokens);

        Environment environment = mock(Environment.class);
        Parser parser = new Parser(environment);
        parser.getBlockStack().push("foo");
        parser.setParent(new StringConstant("foo.twig4j", 1));
        parser.setTokenStream(tokenStream);
        ExpressionParser expressionParser = new ExpressionParser(parser);

        Expression parsedExpression = expressionParser.parsePrimaryExpression();

        Assert.assertEquals("Should be of type parent", Parent.class, parsedExpression.getClass());
        Assert.assertEquals("Node name should be block name", "foo", parsedExpression.getAttribute("name"));
    }

    @Test
    public void canParseBlockFunction() throws SyntaxErrorException, Twig4jRuntimeException {
        ArrayList<Token> tokens = new ArrayList<>();
        tokens.add(new Token(Token.Type.NAME, "block", 1));
        tokens.add(new Token(Token.Type.PUNCTUATION, "(", 1));
        tokens.add(new Token(Token.Type.STRING, "foo", 1));
        tokens.add(new Token(Token.Type.PUNCTUATION, ")", 1));
        tokens.add(new Token(Token.Type.EOF, null, 1));
        TokenStream tokenStream = new TokenStream(tokens);

        Environment environment = mock(Environment.class);
        Parser parser = new Parser(environment);
        parser.getBlockStack().push("foo");
        parser.setParent(new StringConstant("foo.twig4j", 1));
        parser.setTokenStream(tokenStream);
        ExpressionParser expressionParser = new ExpressionParser(parser);

        Expression parsedExpression = expressionParser.parsePrimaryExpression();

        Assert.assertEquals("Should be of type block reference", BlockReferenceExpression.class, parsedExpression.getClass());
        Assert.assertEquals("Node name should be a string constant", StringConstant.class, parsedExpression.getNode(0).getClass());
        Assert.assertEquals("Node name should be 'foo'", "foo", parsedExpression.getNode(0).getAttribute("data"));
    }

    public String activateUpperFilter(String foo) {
        return foo;
    }
}
