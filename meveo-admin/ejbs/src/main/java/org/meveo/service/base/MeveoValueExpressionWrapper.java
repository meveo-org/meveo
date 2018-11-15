package org.meveo.service.base;

import org.meveo.commons.utils.EjbUtils;
import org.meveo.elresolver.ValueExpressionWrapper;
import org.meveo.model.crm.Provider;
import org.meveo.service.crm.impl.ProviderService;

import javax.el.*;
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
}
