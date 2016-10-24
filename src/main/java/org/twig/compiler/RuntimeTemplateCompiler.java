package org.twig.compiler;

import net.openhft.compiler.CachedCompiler;
import net.openhft.compiler.CompilerUtils;
import org.twig.template.Template;

public class RuntimeTemplateCompiler {
    private CachedCompiler cachedCompiler = CompilerUtils.CACHED_COMPILER;

    public RuntimeTemplateCompiler() {
    }

    public RuntimeTemplateCompiler(CachedCompiler cachedCompiler) {
        this.cachedCompiler = cachedCompiler;
    }

    /**
     * Compile a template class from java source code
     * @param sourceCode The java code to compile
     * @param name The name of the template class INCLUDING package name (ie org.twig.template.02ntueh0k2b20rckb9940ntqb_0)
     * @return
     */
    public Template compile(String sourceCode, String name) {
        try {
            Template template = (Template) cachedCompiler.loadFromJava(name, sourceCode).newInstance();

            return template;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Mismatching class name and template class name when runtime compiling template");
        } catch (InstantiationException e) {
            throw new RuntimeException("InstantiationException: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccessException: " + e.getMessage());
        }
    }

    public void setCachedCompiler(CachedCompiler cachedCompiler) {
        this.cachedCompiler = cachedCompiler;
    }
}
