package org.twig.functional;

import org.junit.Assert;
import org.junit.Test;
import org.twig.Environment;
import org.twig.exception.TwigException;
import org.twig.loader.HashMapLoader;

import java.util.HashMap;

public class RenderMathsTests {
    private Environment environment;

    @Test
    public void canRenderNumber() throws TwigException {
        HashMap<String, String> templates = new HashMap<>();
        templates.put("foo.twig", "{{ 1134.12341 }}");
        setupEnvironment(templates);

        Assert.assertEquals("1134.12341 should be rendered", "1134.12341", environment.render("foo.twig"));
    }

    @Test
    public void canDoAddition() throws TwigException {
        HashMap<String, String> templates = new HashMap<>();
        templates.put("foo.twig", "{{ 1 + 1 }}");
        setupEnvironment(templates);

        Assert.assertEquals("1 + 1 should be 2", "2", environment.render("foo.twig"));
    }

    private void setupEnvironment(HashMap<String, String> templates) {
        environment = new Environment(new HashMapLoader(templates));

        environment.enableDebug();
        environment.enableStrictVariables();
    }
}