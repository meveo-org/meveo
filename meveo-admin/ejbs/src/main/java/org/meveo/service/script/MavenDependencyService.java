package org.meveo.service.script;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.service.script.maven.MavenClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class MavenDependencyService {
    private static final Logger log = LoggerFactory.getLogger(MavenDependencyService.class);

    @Inject
    @MeveoJpa
    private EntityManagerWrapper emWrapper;
    
    private Object[] listAllCoordinates(Collection<MavenDependency> dependencies) {
	return dependencies.stream().map(MavenDependency::getCoordinates).toArray();
    }

    public MavenDependency find(String coordinates) {
	String queryString = "from MavenDependency " + "where lower(coordinates) = :coordinates ";

	TypedQuery<MavenDependency> query = emWrapper.getEntityManager().createQuery(queryString, MavenDependency.class)
		.setParameter("coordinates", coordinates.toLowerCase());

	try {
	    return query.getSingleResult();
	} catch (NoResultException e) {
	    return null;
	}
    }

    public boolean validateUniqueFields(String version, String groupId, String artifactId) {

	String queryString = "select 1 " + "from MavenDependency " + "where lower(groupId)=:groupId "
		+ "and lower(artifactId)=:artifactId " + "and lower(version) != :version";

	Query query = emWrapper.getEntityManager().createQuery(queryString)
		.setParameter("groupId", groupId.toLowerCase()).setParameter("artifactId", artifactId.toLowerCase())
		.setParameter("version", version.toLowerCase());

	return query.getResultList().size() == 0;
    }

    /**
     * Removes maven dependencies that are not linked to any script
     */
    public void removeOrphans() {
	String query = "DELETE FROM maven_dependency md\n" + "WHERE NOT EXISTS (\n" + "	SELECT 1 \n"
		+ "	FROM adm_script_maven_dependency asmd\n" + "	WHERE asmd.maven_coordinates = md.coordinates\n"
		+ ")";

	emWrapper.getEntityManager().createNativeQuery(query).executeUpdate();
    }

    /**
     * Find related maven dependencies.
     */
    public List<MavenDependency> findScriptRelatedDependencies(ScriptInstance scriptInstance) {
	Set<MavenDependency> dependencies = scriptInstance.getMavenDependenciesNullSafe();
	long scriptId = scriptInstance.getId();
	log.debug("scriptInstance.id: {}", scriptId);
	log.debug("scriptInstance.mavenDependencies: {}", listAllCoordinates(dependencies));

	if (!dependencies.isEmpty()) {
	    List<MavenDependency> dependenciesToRemove = dependencies.stream().filter((dependency) -> {
		return ((BigInteger) emWrapper.getEntityManager()
			.createNamedQuery("MavenDependency.countOtherDependencies").setParameter("scriptId", scriptId)
			.setParameter("coordinates", dependency.getCoordinates()).getSingleResult()).intValue() == 0;
	    }).collect(Collectors.toList());
	    if (!dependenciesToRemove.isEmpty()) {
		return dependenciesToRemove;
	    }
	}
	return new ArrayList<MavenDependency>();
    }

    /**
     * Removes maven dependencies related to a specified script instance.
     */
    public void removeScriptRelatedDependencies(List<MavenDependency> dependenciesToRemove) {
	if (!dependenciesToRemove.isEmpty()) {
	    log.debug("dependenciesToRemove: {}", listAllCoordinates(dependenciesToRemove));
	    emWrapper.getEntityManager().createNamedQuery("MavenDependency.removeDependencies")
		    .setParameter("dependencies", dependenciesToRemove).executeUpdate();
	    MavenClassLoader mavenClassLoader = MavenClassLoader.getInstance();
	    mavenClassLoader.removeLibraries(dependenciesToRemove);
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
}
