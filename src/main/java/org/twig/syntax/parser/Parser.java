package org.twig.syntax.parser;

import org.twig.Environment;
import org.twig.exception.SyntaxErrorException;
import org.twig.exception.TwigRuntimeException;
import org.twig.syntax.Token;
import org.twig.syntax.TokenStream;
import org.twig.syntax.parser.node.Module;
import org.twig.syntax.parser.node.Node;
import org.twig.syntax.parser.node.type.PrintExpression;
import org.twig.syntax.parser.node.type.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
    // The token stream to parse
    private TokenStream tokenStream;
    // The expression parser
    private ExpressionParser expressionParser = new ExpressionParser(this);
    // The Twig environment
    private Environment environment;

    public Parser(Environment environment) {
        this.environment = environment;
    }

    /**
     * Parses a template
     * @param tokenStream The token stream to parse
     * @return The Module node (which represents a twig file)
     * @throws SyntaxErrorException
     */
    public Module parse(TokenStream tokenStream) throws SyntaxErrorException, TwigRuntimeException {
        // TODO: Find out wth line 64-82 does

        // TODO: Create node visitors

        this.tokenStream = tokenStream;
        // TODO Find out what all the other properties does

        Node body;
        try {
            body = subparse();
        } catch (SyntaxErrorException e) {
            if (e.getTemplateName() == null) {
                e.setTemplateName(getFilename());
            }

            if (e.getLineNumber() == null) {
                e.setLineNumber(tokenStream.getCurrent().getLine());
            }

            throw e;
        }

        Module node = new Module(body, tokenStream.getFilename());

        return node;
    }

    /**
     * Does the hard work parsing
     * @return The body nodes
     * @throws SyntaxErrorException
     */
    public Node subparse() throws SyntaxErrorException, TwigRuntimeException {
        Integer lineno = tokenStream.getCurrent().getLine();
        ArrayList<Node> rv = new ArrayList<>();

        while (!tokenStream.isEOF()) {
            switch (tokenStream.getCurrent().getType()) {
                case TEXT:
                    Token textToken = tokenStream.next();

                    // Create attribute
                    HashMap<String, Object> attributes = new HashMap<>();
                    attributes.put("data", textToken.getValue());

                    rv.add(new Text(new ArrayList<Node>(), attributes, textToken.getLine(), null));
                    break;

                case VAR_START:
                    Token varStartToken = tokenStream.next();
                    Node expr = expressionParser.parseExpression();
                    tokenStream.expect(Token.Type.VAR_END);
                    rv.add(new PrintExpression(expr, varStartToken.getLine()));

                case EOF:
                    break;

                default:
                    throw new SyntaxErrorException(
                            "Lexer or parser ended up in unsupported state.",
                            tokenStream.getFilename(),
                            tokenStream.getCurrent().getLine()
                    );
            }
        }

        if (rv.size() == 1) {
            return rv.get(0);
        }

        return new Node(rv, new HashMap<>(), lineno, null);
    }

    /**
     * Get the current token the token stream points to
     *
     * @return The token
     */
    public Token getCurrentToken() {
        return tokenStream.getCurrent();
    }

    /**
     * Get the file name of the token stream currently being parsed
     * @return The file/template name
     */
    public String getFilename() {
        return tokenStream.getFilename();
    }

    public TokenStream getTokenStream() {
        return tokenStream;
    }

    public Parser setTokenStream(TokenStream tokenStream) {
        this.tokenStream = tokenStream;

        return this;
    }

    public ExpressionParser getExpressionParser() {
        return expressionParser;
    }

    public Parser setExpressionParser(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;

        return this;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Parser setEnvironment(Environment environment) {
        this.environment = environment;

        return this;
    }
}
