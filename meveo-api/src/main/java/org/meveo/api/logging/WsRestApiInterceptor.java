package org.meveo.api.logging;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Logs the calls to the REST and WS interfaces. Sets up logging MDC context.
 * 
 * @author Edward P. Legaspi
 * 
 **/
public class WsRestApiInterceptor {

    @Inject
    @CurrentUser
    private MeveoUser currentUser;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {

        if (currentUser.getProviderCode() == null) {
            MDC.remove("providerCode");
        } else {
            MDC.put("providerCode", currentUser.getProviderCode());
        }

        if (log.isDebugEnabled()) {
            log.debug("\r\n\r\n===========================================================");
            log.debug("Entering method {}.{}", invocationContext.getMethod().getDeclaringClass().getName(), invocationContext.getMethod().getName());

            if (invocationContext.getParameters() != null) {
                for (Object obj : invocationContext.getParameters()) {
                    log.debug("Parameter {}", obj == null ? null : obj.toString());
                }
            }
        }

        // Call the actual REST/WS method
        Object apiResult = null;
        apiResult = invocationContext.proceed();
        log.debug("Finished method {}.{}", invocationContext.getMethod().getDeclaringClass().getName(), invocationContext.getMethod().getName());
        MDC.remove("providerCode");
        return apiResult;
    }

}
