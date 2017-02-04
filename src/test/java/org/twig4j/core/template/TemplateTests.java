package org.twig4j.core.template;

import org.junit.Assert;
import org.junit.Test;
import org.twig4j.core.Environment;
import org.twig4j.core.exception.Twig4jException;
import org.twig4j.core.exception.Twig4jRuntimeException;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Map;

public class TemplateTests {
    @Test
    public void testCanRenderString() throws Twig4jException {
        Template template = new TestStringTemplate();
        Assert.assertEquals("Rendered string should be returned", "foo", template.render());
    }

    @Test
    public void testCanRenderVariable() throws Twig4jException {
        Context context = new Context();
        context.put("foo", "bar");
        Template template = new TestVariableTemplate();

        Assert.assertEquals("Rendered variable contents should be returned", "bar", template.render(context));
    }

    @Test(expected = Twig4jRuntimeException.class)
    public void testAccessUndefinedVariableThrowsRuntimeException() throws Twig4jException {
        Template template = new TestVariableTemplate();
        Environment environment = mock(Environment.class);

        when(environment.isStrictVariables()).thenReturn(true);

        template.setEnvironment(environment);

        template.render(new Context());
    }

    @Test
    public void canCallMethodOnContextObject() throws Twig4jException {
        Context context = new Context();
        context.put("foo", new ClassWithBarMethod());
        Template template = new TestMethodCallTemplate();

        Assert.assertEquals(
                "Method contents should be returned",
                "some argument",
                template.render(context)
        );
    }

    @Test
    public void canGetObjectProperty() throws Twig4jException {

    }

    @Test
    public void canCompareDifferentConstants() throws Twig4jException {
        Template template = new TestCompareDifferentConstantsTemplate(new Environment());

        Assert.assertEquals(
                "Method compare should return false when comparing 2 different constants",
                "false",
                template.render()
        );
    }

    @Test
    public void canCompareEqualConstants() throws Twig4jException {
        Template template = new TestCompareSameConstantsTemplate(new Environment());

        Assert.assertEquals(
                "Method compare should return true when comparing 2 equal constants",
                "true",
                template.render()
        );
    }

    @Test(expected = Twig4jRuntimeException.class)
    public void throwsExceptionWhenComparingDifferentTypes() throws Twig4jException {
        Environment environment = new Environment();
        environment.enableStrictTypes();
        Template template = new TestCompareDifferentTypesConstantsTemplate(environment);

        template.render();
    }

    @Test
    public void failsSilentlyWhenComparingDifferentTypesWithStrictTypesDisabled() throws Twig4jException {
        Environment environment = new Environment();
        environment.disableStrictTypes();
        Template template = new TestCompareDifferentTypesConstantsTemplate(environment);

        Assert.assertEquals(
                "Method compare should return false when comparing constants of different types",
                "false",
                template.render()
        );
    }

    @Test
    public void canGetPropertyGetterIserHaser() throws Twig4jException {
        Context ctx = new Context();
        ctx.put("foo", new TestClassWithGettersAndProperty());
        Environment env = new Environment();

        env.enableStrictVariables();

        Template fooProperty = new TestGetFooPropertyTemplate(env);
        Assert.assertEquals("foo property should be fetched", "foo contents", fooProperty.render(ctx));

        Template barGetter = new TestGetBarGetterTemplate(env);
        Assert.assertEquals("bar getter should be called", "bar contents", barGetter.render(ctx));

        Template bazIser = new TestGetBazIserTemplate(env);
        Assert.assertEquals("baz iser should be called", "true", bazIser.render(ctx));

        Template quxHaser = new TestGetQuxHaserTemplate(env);
        Assert.assertEquals("qux haser should be called", "false", quxHaser.render(ctx));

        Template quuxMethod = new TestGetQuuxMethodTemplate(env);
        Assert.assertEquals("quux should be called", "quux contents", quuxMethod.render(ctx));
    }

    @Test
    public void canLoadOtherTemplate() throws Twig4jException {
        Environment environment = mock(Environment.class);
        Template barTwig = new TestStringTemplate();

        when(environment.resolveTemplate("bar.twig4j")).thenReturn(barTwig);

        Template template = new TestLoadTemplateTemplate();
        template.setEnvironment(environment);

        Assert.assertEquals(
            "Contents of template that only loads other template should be the same as content of the loaded template",
            barTwig.render(),
            template.render()
        );
    }

    @Test
    public void canDisplayBlock() throws Twig4jException {
        Environment environment = mock(Environment.class);
        Template template = new TestDisplayBlockTemplate(environment);

        Assert.assertEquals(
            "Contents of rendered template should be what's in block a",
            "foo",
            template.render()
        );
    }
    protected class TestStringTemplate extends Template {
        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return "foo";
        }

        @Override
        public String getTemplateName() {
            return "foo";
        }
    }

    protected class TestVariableTemplate extends Template {
        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return getContext(context, "foo", false, 1).toString();
        }

        @Override
        public String getTemplateName() {
            return "foo";
        }
    }

    protected class TestMethodCallTemplate extends Template {
        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return String.valueOf(getAttribute(context.get("foo"), "bar", Arrays.asList("some ", "argument"), "method"));
        }

        @Override
        public String getTemplateName() {
            return "foo";
        }
    }

    protected class ClassWithBarMethod {
        public String bar(String anArgument, String anotherArgument) {
            return anArgument + anotherArgument;
        }
    }

    protected class TestCompareDifferentConstantsTemplate extends Template {
        public TestCompareDifferentConstantsTemplate(Environment environment) throws Twig4jException {
            super(environment);
        }

        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return String.valueOf(compare("foo", "bar"));
        }

        @Override
        public String getTemplateName() {
            return "foo";
        }
    }

    protected class TestCompareSameConstantsTemplate extends Template {
        public TestCompareSameConstantsTemplate(Environment environment) throws Twig4jException {
            super(environment);
        }

        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return String.valueOf(compare("foo", "foo"));
        }

        @Override
        public String getTemplateName() {
            return "foo";
        }
    }

    protected class TestCompareDifferentTypesConstantsTemplate extends Template {
        public TestCompareDifferentTypesConstantsTemplate(Environment environment) throws Twig4jException {
            super(environment);
        }

        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return String.valueOf(compare("true", true));
        }

        @Override
        public String getTemplateName() {
            return "foo";
        }
    }

    protected class TestGetFooPropertyTemplate extends Template {
        public TestGetFooPropertyTemplate(Environment environment) throws Twig4jException {
            super(environment);
        }

        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return String.valueOf(getAttribute(getContext(context, "foo", false, 1), "foo", Arrays.asList(), "any"));
        }

        @Override
        public String getTemplateName() {
            return "foo";
        }
    }

    protected class TestGetBarGetterTemplate extends Template {
        public TestGetBarGetterTemplate(Environment environment) throws Twig4jException {
            super(environment);
        }

        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return String.valueOf(getAttribute(getContext(context, "foo", false, 1), "bar", Arrays.asList(), "any"));
        }

        @Override
        public String getTemplateName() {
            return "foo";
        }
    }

    protected class TestGetBazIserTemplate extends Template {
        public TestGetBazIserTemplate(Environment environment) throws Twig4jException {
            super(environment);
        }

        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return String.valueOf(getAttribute(getContext(context, "foo", false, 1), "baz", Arrays.asList(), "any"));
        }

        @Override
        public String getTemplateName() {
            return "foo";
        }
    }

    protected class TestGetQuxHaserTemplate extends Template {
        public TestGetQuxHaserTemplate(Environment environment) throws Twig4jException {
            super(environment);
        }

        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return String.valueOf(getAttribute(getContext(context, "foo", false, 1), "qux", Arrays.asList(), "any"));
        }

        @Override
        public String getTemplateName() {
            return "foo";
        }
    }

    protected class TestGetQuuxMethodTemplate extends Template {
        public TestGetQuuxMethodTemplate(Environment environment) throws Twig4jException {
            super(environment);
        }

        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return String.valueOf(getAttribute(getContext(context, "foo", false, 1), "quux", Arrays.asList(), "any"));
        }

        @Override
        public String getTemplateName() {
            return "foo";
        }
    }

    protected class TestClassWithGettersAndProperty {
        public String foo = "foo contents";

        public String getBar() {
            return "bar contents";
        }

        public boolean isBaz() {
            return true;
        }

        public boolean hasQux() {
            return false;
        }

        public String quux() {
            return "quux contents";
        }
    }

    protected class TestLoadTemplateTemplate extends Template {
        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return loadTemplate("bar.twig4j", "bar.twig4j", 1, null).render();
        }

        @Override
        public String getTemplateName() {
            return "foo.twig4j";
        }
    }

    protected class TestDisplayBlockTemplate extends Template {
        public TestDisplayBlockTemplate(Environment environment) throws Twig4jException {
            try {
                blocks.put("a", new TemplateBlockMethodSet(this, getClass().getMethod("block_a", Context.class, Map.class)));
            } catch (NoSuchMethodException e) {
                throw new Twig4jRuntimeException("Could not find method for block.", getTemplateName(), -1, e);
            }
        }

        @Override
        protected String doDisplay(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return displayBlock("a", context, blocks, true);
        }

        public String block_a(Context context, Map<String, TemplateBlockMethodSet> blocks) throws Twig4jException {
            return "foo";
        }

        @Override
        public String getTemplateName() {
            return "foo.twig4j";
        }
    }
}