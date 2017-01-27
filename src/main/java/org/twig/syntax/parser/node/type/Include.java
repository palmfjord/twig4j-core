package org.twig.syntax.parser.node.type;

import org.twig.compiler.ClassCompiler;
import org.twig.exception.LoaderException;
import org.twig.exception.TwigRuntimeException;
import org.twig.syntax.parser.node.Node;

import java.util.Arrays;
import java.util.List;

public class Include extends Node {
    /**
     * Constructor
     * @param template The template file name to load (usually a string constant)
     * @param variables Variables to pass to the template
     * @param only Whether to "sandbox" the template so it doesn't access this template's context
     * @param ignoreMissing Ignore if the template is missing and fail silently?
     * @param lineNumber The line which this tag is on
     * @param tag Always pass "include"
     */
    public Include(Node template, Node variables, Boolean only, Boolean ignoreMissing, Integer lineNumber, String tag) {
        super(lineNumber);
        setTag(tag);
        nodes = Arrays.asList(template, variables);
        putAttribute("only", only);
        putAttribute("ignore_missing", ignoreMissing);
    }

    @Override
    public void compile(ClassCompiler compiler) throws LoaderException, TwigRuntimeException {
        
    }
}