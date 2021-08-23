package org.meveo.elresolver;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.el.FunctionMapper;

import org.apache.commons.lang3.StringUtils;

public class MeveoDefaultFunctionMapper extends FunctionMapper {

    private Map<String, Method> functionMap = new HashMap<>();
    
    private static final Pattern PATTERN_STRIP_ACCENTS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");


    public MeveoDefaultFunctionMapper(){

        try {
            addFunction("string","contains", getClass().getMethod("contains", String.class, String.class));
            addFunction("string","startsWith", getClass().getMethod("startsWith", String.class, String.class));
            addFunction("string","endsWith", getClass().getMethod("endsWith", String.class, String.class));
            addFunction("string","normalizeCA", getClass().getMethod("normalizeCA", String.class));
                       

            addFunction("array", "first", getClass().getMethod("getFirstItem", Collection.class));
            addFunction("array", "contains", getClass().getMethod("contains", Collection.class, Object.class));
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
        if (prefix == null || localName == null) {
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

    /**
     * First item of the collection
     */
    public static Object getFirstItem(Collection collection){
        return collection.iterator().next();
    }

    public static boolean contains(Collection collection, Object object){
        return collection.contains(object);
    }

    /**
     * normalize Case And Accents of an input string 
     *  
     * @param input : text to normalize
     * @return replace accent characters by equivalent without accents change to lowerCase
     */
    public static String normalizeCA(String input) {
    	if (StringUtils.isEmpty(input)) {
            return input;
        }
    	input = Normalizer.normalize(input, Normalizer.Form.NFD);
    	input = PATTERN_STRIP_ACCENTS.matcher(input).replaceAll(StringUtils.EMPTY);
    	return input.toLowerCase();
    }

}
