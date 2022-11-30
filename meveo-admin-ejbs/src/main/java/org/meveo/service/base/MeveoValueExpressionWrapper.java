package org.meveo.service.base;

import org.meveo.commons.utils.EjbUtils;
import org.meveo.elresolver.*;
import org.meveo.model.crm.Provider;
import org.meveo.service.crm.impl.ProviderService;

import javax.el.*;
import javax.el.ELException;
import java.util.Map;

public class MeveoValueExpressionWrapper extends ValueExpressionWrapper {


    protected MeveoValueExpressionWrapper(String expression, Map<Object, Object> userMap, Class resultClass) {
        super(expression, userMap, resultClass);
    }

    @Override
    protected Map<String, Object> getAdditionalSources(String expression, Map<Object, Object> userMap) {
        final Map<String, Object> additionalSources = super.getAdditionalSources(expression, userMap);
        if (userMap != null && expression.contains("appProvider")) {
            Provider appProvider = ((ProviderService) EjbUtils.getServiceInterface("ProviderService")).getProvider();
            additionalSources.put("appProvider", appProvider);
        }
        return additionalSources;
    }

    @Override
    protected FunctionMapper getMapper() {
        return new MeveoFunctionMapper();
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
    public static boolean evaluateToBooleanOneVariable(String expression, String variableName, Object variable) throws ELException, org.meveo.elresolver.ELException {

        boolean result = evaluateToBooleanMultiVariable(expression, variableName, variable);
        return result;
    }
}
