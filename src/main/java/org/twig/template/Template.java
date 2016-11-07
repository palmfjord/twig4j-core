package org.twig.template;

import org.twig.Environment;
import org.twig.exception.TwigRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class Template {
    protected Environment environment;

    public Template() {
    }

    public Template(Environment environment) {
        this.environment = environment;
    }

    public String render() throws TwigRuntimeException {
        return render(new HashMap<>());
    }

    public String render(Map<String, ?> context) throws TwigRuntimeException {
        return doRender(context);
    }

    abstract protected String doRender(Map<String, ?> context) throws TwigRuntimeException;

    /**
     * Get the template file name
     *
     * @return The file name
     */
    abstract public String getTemplateName();

    /**
     * Get a variable from the provided context
     *
     * @param context The context to get the variable from
     * @param item The variable name
     * @param ignoreStrictChecks Whether to throw an error or just fail silently and return empty string
     * @return
     */
    protected Object getContext(Map<String, ?> context, String item, boolean ignoreStrictChecks, Integer line) throws TwigRuntimeException {
        if (!context.containsKey(item)) {
            if (ignoreStrictChecks || !environment.isStrictVariables()) {
                return "";
            } else {
                throw TwigRuntimeException.variableDoesNotExist(item, getTemplateName(), line);
            }
        }

        return context.get(item);
    }

    protected Object getAttribute(Object object, Object item, List<Object> arguments, String type) throws TwigRuntimeException {
        return getAttribute(object, item, arguments, type, false, false);
    }

    protected Object getAttribute(Object object, Object item, List<Object> arguments, String type, boolean isDefinedTest, boolean ignoreStrictChecks) throws TwigRuntimeException {
        if (!type.equals("method")) {

            // If this is a regular array list
            if (object instanceof List) {
                Integer arrayItem = null;
                // Cast bools and floats/doubles to integers
                if (item.getClass() == boolean.class) {
                    arrayItem = (boolean)item ? 1 : 0;
                } else if (item.getClass() == Float.class || item.getClass() == Double.class || item.getClass() == Integer.class) {
                    arrayItem = (Integer) item;
                }

                try {
                    return ((List) object).get(arrayItem);
                } catch (IndexOutOfBoundsException e) {
                    // Just continue
                } catch (NullPointerException e) {
                    // Just continue
                }
            }

            // If this is an array
            if (type.equals("array")) { // TODO !is_object(object)
                if (isDefinedTest) {
                    return false;
                }

                if (ignoreStrictChecks || !environment.isStrictVariables()) {
                    return null;
                }

                String message;
                if (object instanceof List) {
                    if (((List) object).size() == 0) {
                        message = String.format("Key \"%s\" does not exist as the array is empty", String.valueOf(item));
                    } else {
                        message = String.format("Key \"%s\" for array with keys \"%s\" does not exist", String.valueOf(item), String.join(", ", (List) object));
                    }
                } else {
                    if (object == null) {
                        message = String.format("Impossible to access a key (\"%s\") on a null variable", String.valueOf(item));
                    } else {
                        message = String.format("Impossible to access an attribute (\"%s\") on a %s variable", String.valueOf(item), object.getClass().getName());
                    }
                }

                throw new TwigRuntimeException(message, getTemplateName(), -1);
            }
        }

        // Can't work with null variables
        if (object == null) {
            if (ignoreStrictChecks || !environment.isStrictVariables() ) {
                return null;
            } else {
                throw new TwigRuntimeException("Impossible to invoke a method (\"" + String.valueOf(item) + "\") on a null variable", getTemplateName(), -1);
            }
        }

        // Can't work with primitive types
        if (
            object.getClass() == Integer.class
            || object.getClass() == String.class
            || object.getClass() == Float.class
            || object.getClass() == Double.class
            || object.getClass() == Boolean.class
        ) {
            if (ignoreStrictChecks || !environment.isStrictVariables()) {
                return null;
            } else {
                throw new TwigRuntimeException("Impossible to invoke a method (\"" + String.valueOf(item) + "\") on a " + object.getClass().getName() + " variable", getTemplateName(), -1);
            }
        }

        // Get map attribute or "regular" property
        if (!type.equals("method") && !(object instanceof Template)) { // Template does not have public properties, and we don't want to allow access to internal ones
            // Get map attribute
            if (object instanceof Map) {
                if (((Map) object).containsKey(item)) {
                    return ((Map) object).get(item);
                }
            }

            // Try to get the property
            try {
                Field field = object.getClass().getField((String) item);
                return field.get(object);
            } catch (Exception e) {
                // Just continue, there might be a getter
            }
        }

        // Only methods left, setup the arguments
        List<Class> argumentClasses = new ArrayList<>();
        for (Object argument : arguments) {
            argumentClasses.add(argument.getClass());
        }

        // Used for (get|is|has)Property
        String propertyNameWithUpperFirst = ((String) item).substring(0, 1).toUpperCase() + ((String) item).substring(1);

        // 1. Try to invoke the "method" directly (ie `property()`)
        try {
            Method methodToInvoke = object.getClass().getDeclaredMethod(String.valueOf(item), argumentClasses.toArray(new Class[argumentClasses.size()]));

            return methodToInvoke.invoke(object, arguments.toArray());
        } catch (NoSuchMethodException e) {
            // 2. Try a getter (ie `getProperty()`)
            try {
                Method methodToInvoke = object.getClass().getDeclaredMethod("get" + propertyNameWithUpperFirst);

                return methodToInvoke.invoke(object);
            } catch (NoSuchMethodException getterException) {
                // 3. Try a "haser' (ie `hasProperty()`)
                try {
                    Method methodToInvoke = object.getClass().getDeclaredMethod("is" + propertyNameWithUpperFirst);

                    return methodToInvoke.invoke(object);
                } catch (NoSuchMethodException iserException) {
                    // 4. Try an "iser" (ie `isProperty()`)
                    try {
                        Method methodToInvoke = object.getClass().getDeclaredMethod("has" + propertyNameWithUpperFirst);

                        return methodToInvoke.invoke(object);
                    } catch (NoSuchMethodException haserException) {
                        // Property was not a map attribute, class  field, method, getter, haser or iser - we have nothing more to try
                        throw new TwigRuntimeException(
                                "No such method \"" + String.valueOf(item) + "\" on object of type \"" + object.getClass().getName() + "\"",
                                getTemplateName(),
                                -1,
                                e
                        );
                    } catch (IllegalAccessException haserException) {
                        throw TwigRuntimeException.illegalAccessToMethod("has" + propertyNameWithUpperFirst, object.getClass().getName(), getTemplateName(), haserException);
                    } catch (InvocationTargetException haserException) {
                        throw TwigRuntimeException.invocationTargetException("has" + propertyNameWithUpperFirst, object.getClass().getName(), getTemplateName(), haserException);
                    }

                // is
                } catch (IllegalAccessException iserException) {
                    throw TwigRuntimeException.illegalAccessToMethod("is" + propertyNameWithUpperFirst, object.getClass().getName(), getTemplateName(), iserException);
                } catch (InvocationTargetException iserException) {
                    throw TwigRuntimeException.invocationTargetException("is" + propertyNameWithUpperFirst, object.getClass().getName(), getTemplateName(), iserException);
                }

            // get
            } catch (IllegalAccessException getterException) {
                throw TwigRuntimeException.illegalAccessToMethod("get" + propertyNameWithUpperFirst, object.getClass().getName(), getTemplateName(), getterException);
            } catch (InvocationTargetException getterException) {
                throw TwigRuntimeException.invocationTargetException("get" + propertyNameWithUpperFirst, object.getClass().getName(), getTemplateName(), getterException);
            }

        // The method
        } catch (IllegalAccessException e) {
            throw TwigRuntimeException.illegalAccessToMethod(String.valueOf(item), object.getClass().getName(), getTemplateName(), e);
        } catch (InvocationTargetException e) {
            throw TwigRuntimeException.invocationTargetException(String.valueOf(item), object.getClass().getName(), getTemplateName(), e);
        }
    }

    /**
     * Compare 2 variables or throw exception if of different types
     *
     * @param a Object 1
     * @param b Object 2
     * @return Whether they are equal
     * @throws TwigRuntimeException On type errors
     */
    protected boolean compare(Object a, Object b) throws TwigRuntimeException {
        if (!a.getClass().equals(b.getClass())) {
            if (environment.isStrictTypes()) {
                throw new TwigRuntimeException(
                        String.format("Cannot compare different types (tried to compare \"%s\" with \"%s\")", a.getClass().getName(), b.getClass().getName()),
                        getTemplateName(),
                        -1
                );
            } else {
                return false;
            }

        }

        return a.equals(b);
    }

    /**
     * Set the environment
     * @param environment The environment
     */
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
