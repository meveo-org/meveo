package org.meveo.service.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.meveo.event.qualifier.RemovedAfterTx;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.service.script.maven.MavenClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class MavenDependencyService {
	private static final Logger log = LoggerFactory.getLogger(MavenDependencyService.class);

	@Inject
	@MeveoJpa
	private EntityManagerWrapper emWrapper;

	@Inject
	@RemovedAfterTx
	protected Event<MavenDependency> afterDeleteEvent;

	private Object[] listAllCoordinates(Collection<MavenDependency> dependencies) {
		return dependencies.stream().map(MavenDependency::getCoordinates).toArray();
	}

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

		Query query = emWrapper.getEntityManager().createQuery(queryString).setParameter("groupId", groupId.toLowerCase()).setParameter("artifactId", artifactId.toLowerCase()).setParameter("version", version.toLowerCase());

		return query.getResultList().size() == 0;
	}

	/**
	 * Removes maven dependencies that are not linked to any script
	 */
	public void removeOrphans() {
		List<MavenDependency> dependenciesToRemove = findOrphans();
		removeDependencies(dependenciesToRemove);
	}

	/**
	 * Find maven dependencies that are not related to any script instances.
	 */
	public List<MavenDependency> findOrphans() {
		List<MavenDependency> dependenciesToRemove = emWrapper.getEntityManager().createNamedQuery("MavenDependency.getOrphans", MavenDependency.class).getResultList();

		if (!dependenciesToRemove.isEmpty()) {
			return dependenciesToRemove;
		}
		return new ArrayList<MavenDependency>();
	}

	/**
	 * Removes the specified maven dependencies passed in.
	 */
	public void removeDependencies(List<MavenDependency> dependenciesToRemove) {
		if (!dependenciesToRemove.isEmpty()) {
			log.debug("dependenciesToRemove: {}", listAllCoordinates(dependenciesToRemove));
			emWrapper.getEntityManager().createNamedQuery("MavenDependency.removeDependencies").setParameter("dependencies", dependenciesToRemove).executeUpdate();
			dependenciesToRemove.stream().forEach((mavenDependency) -> {
				afterDeleteEvent.fire(mavenDependency);
			});
		}
	}

	/**
	 * Removes maven dependencies from maven class loader.
	 */
	public void removeDependenciesFromClassLoader(List<MavenDependency> dependenciesToRemove) {
		if (!dependenciesToRemove.isEmpty()) {
			MavenClassLoader mavenClassLoader = MavenClassLoader.getInstance();
			mavenClassLoader.removeLibraries(dependenciesToRemove);
		}
	}

	/**
	 * Removes a single maven dependency from maven class loader.
	 */
	public void removeDependencyFromClassLoader(MavenDependency dependencyToRemove) {
		MavenClassLoader mavenClassLoader = MavenClassLoader.getInstance();
		mavenClassLoader.removeLibrary(dependencyToRemove);
	}
}
