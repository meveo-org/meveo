package org.meveo.service.script;

import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.annotations.QueryHints;
import org.meveo.event.qualifier.Removed;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.service.config.impl.MavenConfigurationService;

@Stateless
public class MavenDependencyService {

	@Inject
	@MeveoJpa
	private EntityManagerWrapper emWrapper;
	
	@Inject
	protected MavenConfigurationService mavenConfigurationService;
	
	public List<MavenDependency> findModuleDependencies(String moduleCode) {
		String queryString = 
				  "SELECT DISTINCT md "
				+ "FROM MeveoModuleItem mmi "
				+ "		INNER JOIN mmi.meveoModule as module, "
				+ "ScriptInstance si "
				+ "		INNER JOIN si.mavenDependencies as md "
				+ "WHERE module.code = :code \n"
				+ "AND mmi.itemCode = si.code";
		
		return emWrapper.getEntityManager()
				.createQuery(queryString, MavenDependency.class)
				.setHint(QueryHints.READ_ONLY, true)
				.setParameter("code", moduleCode)
				.getResultList();
	}
	
	public List<ScriptInstance> findRelatedScripts(MavenDependency dependency) {
		String queryString = 
				  "SELECT DISTINCT si "
				+ "FROM ScriptInstance si "
				+ "		INNER JOIN si.mavenDependencies as md \n"
				+ "WHERE md.coordinates = :coordinates";
		
		return emWrapper.getEntityManager()
				.createQuery(queryString, ScriptInstance.class)
				.setParameter("coordinates", dependency.getCoordinates())
				.getResultList();
	}

	public MavenDependency find(String coordinates) {
		String queryString = "from MavenDependency where lower(coordinates) = :coordinates ";

		TypedQuery<MavenDependency> query = emWrapper.getEntityManager().createQuery(queryString, MavenDependency.class).setParameter("coordinates", coordinates.toLowerCase());

		try {
			return query.getSingleResult();
			//FIXME: @ClementBareth  this always fail when installing a module with script having maven dependency because the script is not saved
		} catch (Exception e) {
			return null;
		}
	}

	public boolean validateUniqueFields(String version, String groupId, String artifactId) {

		String queryString = "select 1 " + "from MavenDependency " + "where lower(groupId)=:groupId " + "and lower(artifactId)=:artifactId " + "and lower(version) != :version";

		Query query = emWrapper.getEntityManager().createQuery(queryString).setParameter("groupId", groupId.toLowerCase()).setParameter("artifactId", artifactId.toLowerCase())
				.setParameter("version", version.toLowerCase());

		return query.getResultList().size() == 0;
	}

	/**
	 * Removes maven dependencies that are not linked to any script
	 * @param script The updated / deleted script
	 */
	public void removeOrphans(ScriptInstance script) {
		
		String query = "SELECT md FROM MavenDependency md \n" 
				+ "WHERE md.scriptInstances IS EMPTY";
		
		EntityManager entityManager = emWrapper.getEntityManager();
		List<MavenDependency> dependencies = entityManager.createQuery(query, MavenDependency.class)
			.getResultList();
		
		dependencies.forEach(entityManager::remove);
		dependencies.forEach(d -> mavenConfigurationService.onDependencyRemoved(d, script));
	}

}
