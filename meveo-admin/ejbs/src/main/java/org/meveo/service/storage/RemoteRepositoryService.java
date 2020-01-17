package org.meveo.service.storage;

import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.storage.RemoteRepository;
import org.meveo.service.base.PersistenceService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import java.io.Serializable;

@Stateless
public class RemoteRepositoryService extends PersistenceService<RemoteRepository> implements Serializable {

    @Inject
    @MeveoJpa
    private EntityManagerWrapper emWrapper;

    public boolean validateUniqueCode(String code) {

        String queryString = null;

        queryString = "select count(*) from RemoteRepository where lower(code)=:code";

        Query query = emWrapper.getEntityManager().createQuery(queryString).setParameter("code", code);
        long count = (Long) query.getSingleResult();
        return count == 0L;
    }



}
