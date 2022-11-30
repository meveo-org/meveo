package org.meveo.elresolver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.el.ExpressionFactoryImpl;

public class ValueExpressionWrapper {

    private static final String MAPPER_CLASS_NAME = "mapper.class.name";
    
    protected static Logger log = LoggerFactory.getLogger(ValueExpressionWrapper.class);

    static ExpressionFactory expressionFactory;

    private SimpleELResolver simpleELResolver;

    private ELContext context;

    private ValueExpression ve;

    private static FunctionMapper functionMapper = null;
    
    static {
//    	while (expressionFactory == null) {
//    		try {
//    			expressionFactory = ExpressionFactoryImpl.newInstance();
//    		} catch (Exception e) {
//    			e.printStackTrace();
//    			try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//    			// throw new RuntimeException(e);
//    		}
//    	}
    }

    /**
     * Evaluate expression.
     * 
     * @param expression Expression to evaluate
     * @param contextMap Context of values
     * @return A value that expression evaluated to
     * @throws ELException business exception.
     */
    public static boolean evaluateToBoolean(String expression, Map<Object, Object> contextMap) throws ELException {

        Object value = evaluateExpression(expression, contextMap, Boolean.class);
        if (value instanceof Boolean) {
            return (boolean) value;
        } else {
            return false;
        }
    }
    
    
    /**
     * Evaluate expression to a boolean value ignoring exceptions
     * 
     * @param expression Expression to evaluate
     * @param contextMap Context of values
     * @return A boolean value expression evaluates to. An empty expression evaluates to true. Failure to evaluate, return false;
     */
    public static boolean evaluateToBooleanIgnoreErrors(String expression, Map<Object, Object> contextMap) {
        try {
            return evaluateToBoolean(expression, contextMap);
        } catch (ELException e) {
            log.error("Failed to evaluate expression {} on variable {}", expression, contextMap, e);
            return false;
        }
    }

    /**
     * Evaluate expression to a boolean value. Note: method needs to have a unique name as is evaluated from JSF pages.
     * 
     * @param expression Expression to evaluate
     * @param variableName Variable name to give to a variable in context
     * @param variable Variable to make available in context
     * @return A boolean value expression evaluates to. An empty expression evaluates to true;
     * @throws ELException business exception.
     */
    public static boolean evaluateToBooleanOneVariable(String expression, String variableName, Object variable) throws ELException {

        boolean result = evaluateToBooleanMultiVariable(expression, variableName, variable);
        return result;
    }

    /**
     * Evaluate expression to a boolean value ignoring exceptions
     * 
     * @param expression Expression to evaluate
     * @param variableName Variable name to give to a variable in context
     * @param variable Variable to make available in context
     * @return A boolean value expression evaluates to. An empty expression evaluates to true. Failure to evaluate, return false;
     */
    public static boolean evaluateToBooleanIgnoreErrors(String expression, String variableName, Object variable) {
        try {
            return evaluateToBooleanMultiVariable(expression, variableName, variable);
        } catch (ELException e) {
            log.error("Failed to evaluate expression {} on variable {}/{}", expression, variableName, variable, e);
            return false;
        }
    }

    /**
     * Evaluate expression to a boolean value.
     * 
     * @param expression Expression to evaluate
     * @param contextVarNameAndValue An array of context variables and their names in the following order: variable 1 name, variable 1, variable 2 name, variable2, etc..
     * @return A boolean value expression evaluates to. An empty expression evaluates to true;
     * @throws ELException business exception.
     */
    public static boolean evaluateToBooleanMultiVariable(String expression, Object... contextVarNameAndValue) throws ELException {
        if (StringUtils.isBlank(expression)) {
            return true;
        }

        Map<Object, Object> contextMap = new HashMap<Object, Object>();
        if (contextVarNameAndValue != null) {
            for (int i = 0; i < contextVarNameAndValue.length; i = i + 2) {
                contextMap.put(contextVarNameAndValue[i], contextVarNameAndValue[i + 1]);
            }
        }
        Object value = evaluateExpression(expression, contextMap, Boolean.class);
        if (value instanceof Boolean) {
            return (boolean) value;
        } else {
            return false;
        }
    }

    /**
     * Evaluate expression to a String value ignoring exceptions. Converting to string if necessary.
     * 
     * @param expression Expression to evaluate
     * @param variableName Variable name to give to a variable in context
     * @param variable Variable to make available in context
     * @return A boolean value expression evaluates to. An empty expression evaluates to true. Failure to evaluate, return false;
     */
    public static String evaluateToStringIgnoreErrors(String expression, String variableName, Object variable) {
        try {
            return evaluateToStringMultiVariable(expression, variableName, variable);
        } catch (ELException e) {
            log.error("Failed to evaluate expression {} on variable {}/{}", expression, variableName, variable, e);
            return null;
        }
    }

    /**
     * Evaluate expression to a string value, converting to string if necessary.
     * 
     * @param expression Expression to evaluate
     * @param contextVarNameAndValue An array of context variables and their names in the following order: variable 1 name, variable 1, variable 2 name, variable2, etc..
     * @return A boolean value expression evaluates to. An empty expression evaluates to true;
     * @throws ELException business exception
     */
    public static String evaluateToStringMultiVariable(String expression, Object... contextVarNameAndValue) throws ELException {
        if (StringUtils.isBlank(expression)) {
            return null;
        }

        Map<Object, Object> contextMap = new HashMap<Object, Object>();
        if (contextVarNameAndValue != null) {
            for (int i = 0; i < contextVarNameAndValue.length; i = i + 2) {
                contextMap.put(contextVarNameAndValue[i], contextVarNameAndValue[i + 1]);
            }
        }
        Object value = evaluateExpression(expression, contextMap, String.class);
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    /**
     * Evaluate expression.
     * 
     * @param expression Expression to evaluate
     * @param userMap Context of values
     * @param resultClass An expected result class
     * @return A value that expression evaluated to
     * @throws ELException business exception.
     */
    @SuppressWarnings("unchecked")
	public static <T> T evaluateExpression(String expression, Map<Object, Object> userMap, Class<T> resultClass) throws ELException {
        Object result = null;
        if (StringUtils.isBlank(expression)) {
            return null;
        }
        expression = StringUtils.trim(expression);

        if (!expression.contains("#{") && !expression.contains("${")) {
            log.trace("the expression '{}' doesn't contain any EL", expression);
            if (resultClass.equals(String.class)) {
                return (T) expression;
            } else if (resultClass.equals(Double.class)) {
                return (T) (Double) Double.parseDouble(expression);
            } else if (resultClass.equals(Boolean.class)) {
                if ("true".equalsIgnoreCase(expression)) {
                    return (T) Boolean.TRUE;
                } else {
                    return (T) Boolean.FALSE;
                }
            }
        }
        try {
            result = ValueExpressionWrapper.getValue(expression, userMap, resultClass);
            log.trace("EL {} => {}", expression, result);

        } catch (Exception e) {
            log.warn("EL {} throw error with variables {}", expression, userMap, e);
            throw new ELException("Error while evaluating expression " + expression, e);
        }
        return (T) result;
    }

    private static Object getValue(String expression, Map<Object, Object> userMap, @SuppressWarnings("rawtypes") Class resultClass) {
        return new ValueExpressionWrapper(expression, userMap, resultClass).getValue(userMap);
    }

    protected Map<String,Object> getAdditionalSources(String expression, Map<Object, Object> userMap){
        return new HashMap<>();
    }

    protected FunctionMapper getMapper(){
        final String mapperClassName = System.getProperty(MAPPER_CLASS_NAME);
        if(mapperClassName != null) {
            try {
                Class<?> mapperClass = Class.forName(mapperClassName);
                final Object mapper = mapperClass.newInstance();
                if (mapper instanceof MeveoDefaultFunctionMapper) {
                    log.info("Using {} as function mapper", mapperClassName);
                    return (FunctionMapper) mapper;
                }
                log.warn("Class {} is not a sub-class of MeveoDefaultFunctionMapper, using default function mapper", mapperClassName);
            } catch (Exception e) {
                log.warn("{}\nUsing default function mapper", e.toString());
            }
        }
        return new MeveoDefaultFunctionMapper();
    }

    protected ValueExpressionWrapper(String expression, Map<Object, Object> userMap, @SuppressWarnings("rawtypes") Class resultClass) {
        try {
        	Map<String, Object> additionalSources = getAdditionalSources(expression, userMap);
        	if (!additionalSources.isEmpty())
            	userMap.putAll(additionalSources);
            simpleELResolver = new SimpleELResolver(userMap);
            final VariableMapper variableMapper = new SimpleVariableMapper();
            if(functionMapper == null) {
                functionMapper = getMapper();
            }
            final CompositeELResolver compositeELResolver = new CompositeELResolver();
            compositeELResolver.add(simpleELResolver);
            compositeELResolver.add(new ArrayELResolver());
            compositeELResolver.add(new ListELResolver());
            compositeELResolver.add(new BeanELResolver());
            compositeELResolver.add(new MapELResolver());
            context = new ELContext() {
                @Override
                public ELResolver getELResolver() {
                    return compositeELResolver;
                }

                @Override
                public FunctionMapper getFunctionMapper() {
                    return functionMapper;
                }

                @Override
                public VariableMapper getVariableMapper() {
                    return variableMapper;
                }
            };
            
            if (expressionFactory == null) {
            	expressionFactory = new ExpressionFactoryImpl();//ExpressionFactoryImpl.newInstance();
            }
            
            ve = expressionFactory.createValueExpression(context, expression, resultClass);
        } catch (Exception e) {
        	throw new RuntimeException("Failed to init ValueExpressionWrapper", e);
        }
        
    }

    private Object getValue(Map<Object, Object> userMap) {
        simpleELResolver.setUserMap(userMap);
        return ve.getValue(context);
    }

    public static boolean collectionContains(String[] collection, String key) {
        return Arrays.asList(collection).contains(key);
    }
}