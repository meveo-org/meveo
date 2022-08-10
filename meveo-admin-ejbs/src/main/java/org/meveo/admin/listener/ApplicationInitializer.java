package org.meveo.admin.listener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.cache.*;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.jpa.EntityManagerProvider;
import org.meveo.model.crm.Provider;
import org.meveo.security.keycloak.CurrentUserProvider;
import org.meveo.service.crm.impl.ProviderService;
import org.meveo.service.job.JobInstanceService;
import org.meveo.service.script.ScriptInstanceService;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;

/**
 * Takes care of initializing/loading various application services/data
 * 
 * @author Andrius Karpavicius
 */
@Stateless
public class ApplicationInitializer {

	public static final String APPLICATION_INITIALIZER = "applicationInitializer";

	@EJB
    private ApplicationInitializer multitenantAppInitializer;

    @Inject
    private ProviderService providerService;

    @Inject
    private CurrentUserProvider currentUserProvider;

    @Inject
    private JobInstanceService jobInstanceService;

    @Inject
    private ScriptInstanceService scriptInstanceService;

    @Inject
    private EntityManagerProvider entityManagerProvider;

    @Inject
    private Logger log;

    @Inject
    private NotificationCacheContainerProvider notifCache;

    @Inject
    private UserMessageCacheContainerProvider userMessageCache;

    @Inject
    private CustomFieldsCacheContainerProvider cftCache;

    @Inject
    private JobCacheContainerProvider jobCache;

    @Inject
    protected ParamBeanFactory paramBeanFactory;

    public void init() {

        final List<Provider> providers = providerService.list(new PaginationConfiguration("id", SortOrder.ASCENDING));

        int i = 0;

        // Wait for each provider to initialize.
        for (Provider provider : providers) {

            Future<Boolean> initProvider;

            try {
                initProvider = multitenantAppInitializer.initializeTenant(provider, i == 0, false);

                initProvider.get();

            } catch (InterruptedException | ExecutionException | BusinessException e) {
                log.error("Failed to initialize a provider {}", provider.getCode());
            }
            i++;
        }
    }

    /**
     * Initialize tenant information: establish EMF for secondary tenants/providers, schedule jobs, compile scripts, preload caches
     * 
     * @param provider Tenant/provider to initialize
     * @param isMainProvider Is it a main tenant/provider.
     * @param createESIndex boolean that determines whether to create or not the index
     * @return A future with value of True
     * @throws BusinessException Business exception
     */
//    @Asynchronous
    @Transactional(Transactional.TxType.REQUIRED)
    public Future<Boolean> initializeTenant(Provider provider, boolean isMainProvider, boolean createESIndex) throws BusinessException {

        log.debug("Will initialize application for provider {}", provider.getCode());

        if (!isMainProvider) {
            entityManagerProvider.registerEntityManagerFactory(provider.getCode());
        }

        currentUserProvider.forceAuthentication(APPLICATION_INITIALIZER, isMainProvider ? null : provider.getCode());

        // Ensure that provider code in secondary provider schema matches the tenant/provider code as it was listed in main provider's secondary tenant/provider record
        if (!isMainProvider) {
            providerService.updateProviderCode(provider.getCode());
        }

        // Register jobs
        jobInstanceService.registerJobs();

        try {
            scriptInstanceService.constructClassPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("Initialized application for provider {}", provider.getCode());

        return new AsyncResult<Boolean>(Boolean.TRUE);
    }

}