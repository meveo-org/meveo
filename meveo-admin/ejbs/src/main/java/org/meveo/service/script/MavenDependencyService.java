package org.meveo.service.script;

import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;

@Stateless
public class MavenDependencyService {

    @Inject
    @MeveoJpa
    private EntityManagerWrapper emWrapper;

    public boolean validateUniqueFields(Long id, String groupId, String artifactId) {

        String queryString = null;
        if (id == null) {
            queryString = "select count(*) from MavenDependency where lower(groupId)=:groupId and lower(artifactId)=:artifactId";
        } else {
            queryString = "select count(*) from MavenDependency where lower(groupId)=:groupId and lower(artifactId)=:artifactId and script.id <>:id";
        }
        Query query = emWrapper.getEntityManager().createQuery(queryString).setParameter("groupId", groupId.toLowerCase()).setParameter("artifactId", artifactId.toLowerCase());
        if (id != null) {
            query.setParameter("id", id);
        }
        long count = (Long) query.getSingleResult();
        return count == 0L;
    }


}
