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
            queryString = String.format("select count(*) from MavenDependency where lower(groupId)='%s' and lower(artifactId)='%s'",
                    groupId.toLowerCase().replaceAll("'", "''"),
                    artifactId.toLowerCase().replaceAll("'", "''"));
        } else {
            queryString = String.format("select count(*) from MavenDependency where lower(groupId)='%s' and lower(artifactId)='%s' and script.id = %d",
                    groupId.toLowerCase().replaceAll("'", "''"),
                    artifactId.toLowerCase().replaceAll("'", "''"), id);
        }
        Query query = emWrapper.getEntityManager().createQuery(queryString);
        long count = (Long) query.getSingleResult();
        return count == 0L;
    }


}
