package org.twigjava.extension;

import org.twigjava.exception.TwigRuntimeException;
import org.twigjava.filter.Filter;
import org.twigjava.syntax.operator.Operator;
import org.twigjava.syntax.parser.tokenparser.AbstractTokenParser;

import java.util.List;
import java.util.Map;

public interface Extension {
    /**
     * Get all token parsers
     * @return A hashmap of token parsers
     */
    public List<AbstractTokenParser> getTokenParsers();

    /**
     * Get unary operators of this extension
     * @return A hashmap of operators indexed by their operator string
     */
    public Map<String, Operator> getUnaryOperators();

    /**
     * Get binary operators of this extension
     * @return A hashmap of operators indexed by their operator string
     */
    public Map<String, Operator> getBinaryOperators();

    /**
     * Get the filters beloning to this extension
     * @return The filters
     */
    public Map<String, Filter> getFilters() throws TwigRuntimeException;

    /**
     * Get the name of the extension
     * @return The name
     */
    public String getName();
}