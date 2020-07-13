package org.meveo.service.script;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.model.scripts.ScriptInstance;
import org.slf4j.Logger;

@Stateless
public class MavenDependencyService {

	@Inject
	private Logger log;
	
    @Inject
    @MeveoJpa
    private EntityManagerWrapper emWrapper;
    
    /**
     * Remove orphan maven dependencies
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) // Need a new transaction
    public void onScriptUpdated(@Observes(during = TransactionPhase.AFTER_COMPLETION) @Updated ScriptInstance si) {
    	log.debug("[CDI event]  Trigger onScriptUpdated script instance with id={}", si.getId());
    	removeOrphans();
    }
    
    /**
     * Remove orphan maven dependencies
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) // Need a new transaction
    public void onScriptDeleted(@Observes(during = TransactionPhase.AFTER_COMPLETION) @Removed ScriptInstance si) {
    	removeOrphans();
    }
    
    /**
     * Removes maven dependencies that are not linked to any script
     */
    public void removeOrphans() {
    	String query = "DELETE FROM maven_dependency md\n" + 
		    			"WHERE NOT EXISTS (\n" + 
		    			"	SELECT 1 \n" + 
		    			"	FROM adm_script_maven_dependency asmd\n" + 
		    			"	WHERE asmd.maven_coordinates = md.coordinates\n" + 
		    			")";
    
    	emWrapper.getEntityManager()
    		.createNativeQuery(query)
    		.executeUpdate();
    }
    
    public MavenDependency find(String coordinates) {
        String queryString = "from MavenDependency "
				+ "where lower(coordinates) = :coordinates ";

		TypedQuery<MavenDependency> query = emWrapper.getEntityManager()
			.createQuery(queryString, MavenDependency.class)
			.setParameter("coordinates", coordinates.toLowerCase());
		
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
    }

    public boolean validateUniqueFields(String version, String groupId, String artifactId) {

        String queryString = "select 1 "
        					+ "from MavenDependency "
        					+ "where lower(groupId)=:groupId "
        					+ "and lower(artifactId)=:artifactId "
        					+ "and lower(version) != :version";
        
        Query query = emWrapper.getEntityManager()
        		.createQuery(queryString)
        		.setParameter("groupId", groupId.toLowerCase())
        		.setParameter("artifactId", artifactId.toLowerCase())
        		.setParameter("version", version.toLowerCase());
        
        return query.getResultList().size() == 0;
    }
    

}
