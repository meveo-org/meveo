package org.meveo.service.script;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.scripts.MavenDependency;

@Stateless
public class MavenDependencyService {

	@Inject
	@MeveoJpa
	private EntityManagerWrapper emWrapper;

	public MavenDependency find(String coordinates) {
		String queryString = "from MavenDependency " + "where lower(coordinates) = :coordinates ";

		TypedQuery<MavenDependency> query = emWrapper.getEntityManager().createQuery(queryString, MavenDependency.class).setParameter("coordinates", coordinates.toLowerCase());

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
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
	 */
	public void removeOrphans() {
		String query = "DELETE FROM maven_dependency md\n" + "WHERE NOT EXISTS (\n" + "	SELECT 1 \n" + "	FROM adm_script_maven_dependency asmd\n"
				+ "	WHERE asmd.maven_coordinates = md.coordinates\n" + ")";

		emWrapper.getEntityManager().createNativeQuery(query).executeUpdate();
	}

}
