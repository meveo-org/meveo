package org.meveo.service.crm.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.meveo.jpa.EntityManagerProvider;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.security.keycloak.CurrentUserProvider;
import org.slf4j.Logger;

@Stateless
public class CurrentUserProducer {

    @Inject
    private CurrentUserProvider currentUserProvider;

    @Inject
    EntityManagerProvider entityManagerProvider;

    /**
     * produce a current user
     * 
     * @return MeveoUser
     */
    @Produces
    @RequestScoped
    @Named("currentUser")
    @CurrentUser
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) //this method is used in "roll-back processes", so we need to use a separate transaction
    public MeveoUser getCurrentUser() {
        String providerCode = currentUserProvider.getCurrentUserProviderCode();
        EntityManager em = entityManagerProvider.getEntityManager(providerCode);

        return currentUserProvider.getCurrentUser(providerCode, em);
    }
}