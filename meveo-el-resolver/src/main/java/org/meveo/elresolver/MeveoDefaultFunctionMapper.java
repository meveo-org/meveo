package org.meveo.elresolver;


import org.apache.commons.lang3.StringUtils;

import javax.el.FunctionMapper;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class MeveoDefaultFunctionMapper extends FunctionMapper {

    private Map<String, Method> functionMap = new HashMap<String, Method>();

    public MeveoDefaultFunctionMapper(){

        try {
            addFunction("kb","contains", getClass().getMethod("contains", String.class, String.class));
            addFunction("kb","startsWith", getClass().getMethod("startsWith", String.class, String.class));
            addFunction("kb","endsWith", getClass().getMethod("endsWith", String.class, String.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Method resolveFunction(String prefix, String localName) {
        String key = prefix + ":" + localName;
        return functionMap.get(key);
    }

    public void addFunction(String prefix, String localName, Method method) {
        int modifiers = method.getModifiers();
        if (prefix == null || localName == null || method == null) {
            throw new NullPointerException();
        }
        if (!Modifier.isPublic(modifiers)) {
            throw new IllegalArgumentException("method not public");
        }
        if (!Modifier.isStatic(modifiers)) {
            throw new IllegalArgumentException("method not static");
        }
        Class<?> retType = method.getReturnType();
        if (retType == Void.TYPE) {
            throw new IllegalArgumentException("method returns void");
        }
        String key = prefix + ":" + localName;
        functionMap.put(key, method);
    }

    /**
     * string1 contains string2
     *
     * @param string1 Left operand to compare
     * @param string2 Right operand to compare
     * @return true if string1 contains string2
     */
    public static boolean contains(String string1, String string2){
        return string1.contains(string2);
    }

    /**
     * string1 starts with string2
     *
     * @param string1 Left operand to compare
     * @param string2 Right operand to compare
     * @return true if string1 starts with string2
     */
    public static boolean startsWith(String string1, String string2){
        return string1.startsWith(string2);
    }

    /**
     * string1 starts with string2
     *
     * @param string1 Left operand to compare
     * @param string2 Right operand to compare
     * @return true if string1 starts with string2
     */
    public static boolean endsWith(String string1, String string2){
        return string1.endsWith(string2);
    }


}
