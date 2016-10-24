package org.twig.syntax.parser.node;

import org.junit.Test;
import org.twig.Environment;
import org.twig.compiler.ClassCompiler;
import org.twig.exception.LoaderException;
import org.twig.syntax.parser.node.type.Body;

import static org.mockito.Mockito.*;

public class ModuleTests {
    @Test
    public void canCompileHeaderAndFooter() throws LoaderException {
        ClassCompiler classCompilerStub = mock(ClassCompiler.class);
        Environment environmentStub = mock(Environment.class);

        Module module = new Module(new Body(1));
        module.setFileName("foo");

        setupClassCompilerStubWhens(classCompilerStub, environmentStub);

        module.compile(classCompilerStub);

        verify(classCompilerStub).writeLine("package org.twig.template;\n");
        verify(classCompilerStub).writeLine("/**");
        verify(classCompilerStub).writeLine(" * foo");
        verify(classCompilerStub).writeLine(" */");
        verify(classCompilerStub).write("public class hash_0");
        verify(classCompilerStub).writeLine(" extends BaseClass {");
        verify(classCompilerStub).indent();
        verify(classCompilerStub, times(2)).unIndent();
        verify(classCompilerStub, times(2)).writeLine("}");
    }

    @Test
    public void canCompileBody() throws LoaderException {
        ClassCompiler classCompilerStub = mock(ClassCompiler.class);
        Environment environmentStub = mock(Environment.class);

        Body bodyNodeStub = mock(Body.class);

        Module module = new Module(bodyNodeStub);
        module.setFileName("foo");

        setupClassCompilerStubWhens(classCompilerStub, environmentStub);

        module.compile(classCompilerStub);

        verify(classCompilerStub).subCompile(bodyNodeStub);
        verify(classCompilerStub).writeLine("protected String doRender(HashMap<String, String> context) {");
        verify(classCompilerStub, times(2)).writeLine("}");
    }

    private void setupClassCompilerStubWhens(ClassCompiler classCompilerStub, Environment environmentStub) throws LoaderException {
        when(classCompilerStub.writeLine(anyString())).thenReturn(classCompilerStub);
        when(classCompilerStub.write(anyString())).thenReturn(classCompilerStub);
        when(classCompilerStub.unIndent()).thenReturn(classCompilerStub);
        when(classCompilerStub.subCompile(anyObject())).thenReturn(classCompilerStub);
        when(classCompilerStub.getEnvironment()).thenReturn(environmentStub);

        when(environmentStub.getTemplateClass("foo")).thenReturn("hash_0");
        when(environmentStub.getTemplateBaseClass()).thenReturn("BaseClass");
    }
}