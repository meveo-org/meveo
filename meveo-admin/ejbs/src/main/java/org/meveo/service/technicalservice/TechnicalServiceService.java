/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.service.technicalservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.technicalservice.TechnicalServiceFilters;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.scripts.FunctionIO;
import org.meveo.model.technicalservice.Description;
import org.meveo.model.technicalservice.InputMeveoProperty;
import org.meveo.model.technicalservice.OutputMeveoProperty;
import org.meveo.model.technicalservice.TechnicalService;
import org.meveo.service.script.FunctionService;
import org.meveo.service.script.technicalservice.TechnicalServiceEngine;

/**
 * Technical service persistence service.
 *
 * @author Clément Bareth
 * @param <T> the generic type of service
 */
public abstract class TechnicalServiceService<T extends TechnicalService> extends FunctionService<T, TechnicalServiceEngine<T>> {

    @Override
	public List<FunctionIO> getInputs(T function) throws BusinessException {
    	try {
    		Hibernate.initialize(function.getDescriptions());
    	} catch (LazyInitializationException e) {
    		function.setDescriptions(description(function.getCode()));
    	}
    	
		return super.getInputs(function);
	}

	@Override
	public List<FunctionIO> getOutputs(T function) throws BusinessException {
    	try {
    		Hibernate.initialize(function.getDescriptions());
    	} catch (LazyInitializationException e) {
    		function.setDescriptions(description(function.getCode()));
    	}
    	
		return super.getOutputs(function);
	}

	/**
	 * Removes the description.
	 *
	 * @param serviceId id of the service to remove description
	 */
    public void removeDescription(long serviceId){
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Description> query = cb.createQuery(Description.class);
        Root<Description> service = query.from(Description.class);
        query.where(cb.equal(service.get("service"), serviceId));
        List<Description> descriptions = getEntityManager().createQuery(query).getResultList();
        descriptions.forEach(description -> getEntityManager().remove(description));
    }

    /**
	 * Retrieve the last version of the technical service with the specified name.
	 *
	 * @param name Name of the technical service to retrieve
	 * @return The last version number or empty if the technical service does not exists
	 */
    public Optional<Integer> latestVersionNumber(String name) {
        String queryString = "Select max(service.functionVersion) from "+getEntityClass().getName()+" service \n" +
                "where service.name = :name";
        Query q = getEntityManager().createQuery(queryString)
                .setParameter("name", name);
        try {
            return Optional.of((Integer) q.getSingleResult());
        } catch (NoResultException ignored) {
        }
        return Optional.empty();
    }

    /**
	 * Retrieve all the version of the technical services that have the specified name.
	 *
	 * @param name Name of the technical services to retrieve
	 * @return The list of technical service's version
	 */
    @SuppressWarnings("unchecked")
    public List<T> findByName(String name) {
        QueryBuilder qb = new QueryBuilder(getEntityClass(), "service", null);
        qb.addCriterion("service.name", "=", name, true);
        try {
            return (List<T>) qb.getQuery(getEntityManager()).getResultList();
        } catch (NoResultException e) {
            log.warn("No Technical service by name {} found", name);
        }
        return new ArrayList<>();
    }

    /**
	 * Retrieve the latest version of the technical service.
	 *
	 * @param name Name of the technical service to retrieve
	 * @return The last version of the technical service
	 */
    @SuppressWarnings("unchecked")
    public Optional<T> findLatestByName(String name) {
        try {
            QueryBuilder qb = new QueryBuilder(getEntityClass(), "service", null);
            qb.addCriterion("service.name", "=", name, true);
            qb.addSql("service.functionVersion = (select max(ci.functionVersion) from org.meveo.model.technicalservice.TechnicalService ci where "
                    + "ci.name = service.name)");
            return Optional.of((T) qb.getQuery(getEntityManager()).getSingleResult());
        } catch (NoResultException e) {
            log.warn("No Technical service by name {} found", name);
        }
        return Optional.empty();
    }

    /**
	 * Retrieve a technical service based on name and version.
	 *
	 * @param name    Name of the technical service to retrieve
	 * @param version Version of the technical service to retrieve
	 * @return The retrieved technical service or empty if not found
	 */
    @SuppressWarnings("unchecked")
    public Optional<T> findByNameAndVersion(String name, Integer version) {
        QueryBuilder qb = new QueryBuilder(getEntityClass(), "service", null);
        qb.addCriterion("service.name", "=", name, true);
        qb.addCriterion("service.functionVersion", "=", version, true);
        try {
            return Optional.of((T) qb.getQuery(getEntityManager()).getSingleResult());
        } catch (NoResultException e) {
            log.warn("No Technical service by name {} and version {} found", name, version);
        }
        return Optional.empty();
    }
    
    public TechnicalService findServiceByCode(String code) throws NoResultException {
    	QueryBuilder qb = new QueryBuilder(TechnicalService.class, "service", null);
    	qb.addCriterion("service.code", "=", code, true);
		return qb.getTypedQuery(getEntityManager(), TechnicalService.class)
				.getSingleResult();
    }

    /**
	 * Retrieves a filtered list of all services.
	 *
	 * @param filters Filter to apply
	 * @return The services corresponding to the specified filters
	 */
    public List<T> list(TechnicalServiceFilters filters) {
        QueryBuilder qb = filteredQueryBuilder(filters);
        TypedQuery<T> query = qb.getTypedQuery(getEntityManager(), getEntityClass());
        query.setHint("org.hibernate.readOnly", true);
        return query.getResultList();
    }

    /**
	 * Retrieves the names of all the technical services.
	 *
	 * @return The names of all the technical services
	 */
    public List<String> names(){
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<T> root = query.from(getEntityClass());
        query.select(root.get("name"));
        query.distinct(true);
        query.where(cb.equal(root.type(), getEntityClass()));
        return getEntityManager().createQuery(query).getResultList();
    }
    
    /**
     * Retrieves the extended services for the given service
     * 
     * @param serviceCode the service to retrieve extended services from
     * @return the services extended by the given service
     */
    @SuppressWarnings("unchecked")
	public Set<TechnicalService> getExtendedServices(String serviceCode) {
    	String query = "SELECT service.extendedServices FROM " + TechnicalService.class.getName() + " service \n"
    				 + "WHERE service.code = :serviceCode";
    	
    	try {
    		var result = getEntityManager().createQuery(query)
				.setParameter("serviceCode", serviceCode)
    			.getResultList();
			return new HashSet<>(result);
    	} catch(NoResultException e) {
    		return new HashSet<>();
    	}
    }

    /**
	 * Retrieve the description for a particular technical service.
	 *
	 * @param code Code of the service
	 * @return The description of the service with given code
	 */
	public List<Description> description(String code) {
    	String serviceQuery = "FROM " + TechnicalService.class.getName() + " service \n"
    						+ "LEFT JOIN FETCH service.extendedServices \n"
    						+ "WHERE service.code = :code \n";
    	
    	TechnicalService service = getEntityManager()
				.createQuery(serviceQuery, TechnicalService.class)
				.setParameter("code", code)
    			.getSingleResult();
		
		String descriptionQuery = "FROM " + Description.class.getName() + "\n"
								+ "WHERE service = :service";
    	
        List<Description> resultList = getEntityManager()
        		.createQuery(descriptionQuery, Description.class)
        		.setParameter("service", service)
        		.getResultList();
        
        if(!service.getExtendedServices().isEmpty()) {
	        // Retrieve inherited descriptions
	        List<Description> inheritedDescriptions = service.getExtendedServices()
	        		.stream()
	        		.map(TechnicalService::getCode)
	        		.map(this::description)
	        		.flatMap(List::stream)
	        		.collect(Collectors.toList());
	        
	        inheritedDescriptions.forEach(d -> { 
	        	d.setInherited(true);
	        	d.getInputProperties().forEach(p -> p.setInherited(true));
	        	d.getOutputProperties().forEach(p -> p.setInherited(true));
	        });
	        
	        inheritedDescriptions.forEach(inheritedDescription -> {
	        	Optional<Description> descriptionWithSameName = resultList.stream()
	        			.filter(d -> d.getName().equals(inheritedDescription.getName()))
	        			.findFirst();
	        	
	        	if(!descriptionWithSameName.isPresent()) {
	        		resultList.add(inheritedDescription);
	        	} else {
	        		// Merge the descriptions
	        		descriptionWithSameName.get().setInherited(true);
	        		descriptionWithSameName.get().getInputProperties().addAll(inheritedDescription.getInputProperties());
	        		descriptionWithSameName.get().getOutputProperties().addAll(inheritedDescription.getOutputProperties());
	        		descriptionWithSameName.get().setInput(descriptionWithSameName.get().isInput() || inheritedDescription.isInput());
	        		descriptionWithSameName.get().setOutput(descriptionWithSameName.get().isOutput() || inheritedDescription.isOutput());
	        	}
	        });
        }
        
        for(Description desc : resultList) {
        	Hibernate.initialize(desc.getInputProperties());
        	Hibernate.initialize(desc.getOutputProperties());
        }
        
		return resultList;
    }

    /**
	 * Retrieves the different versions number for a technical service.
	 *
	 * @param name Name of the service
	 * @return The versions numbers for the technical service with the given name
	 */
    public List<Integer> versions(String name){
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
        Root<T> service = query.from(getEntityClass());
        query.select(service.get("functionVersion"));
        query.where(cb.equal(service.get("name"), name));
        return getEntityManager().createQuery(query).getResultList();
    }

    /**
	 * Count the technical services.
	 *
	 * @param filters Filters to apply
	 * @return The count of technical services corresponding to the filters
	 */
    /**
     * @param filters
     * @return
     */
    public long count(TechnicalServiceFilters filters){
        QueryBuilder qb = filteredQueryBuilder(filters);
        return qb.count(getEntityManager());
    }

    /**
	 * Find by newer than.
	 *
	 * @param filters   the filters
	 * @param sinceDate the since date
	 * @return the list of result
	 */
    public List<T> findByNewerThan(TechnicalServiceFilters filters, Date sinceDate) {
        QueryBuilder qb = queryBuilder(filters,sinceDate);
        return qb.getTypedQuery(getEntityManager(), getEntityClass())
        		.getResultList();
    }
    
    @Override
	public void create(T executable) throws BusinessException {
    	retainInheritedDescriptions(executable, executable.getDescriptions());
		super.create(executable);
	}

	@Override
	public T update(T executable) throws BusinessException {
		removeDescription(executable.getId());
    	retainInheritedDescriptions(executable, executable.getDescriptions());
    	
    	if(getEntityManager().contains(executable)) {
    		super.updateNoMerge(executable);
    		return executable;
    	} else {
    		return super.update(executable);
    	}
	}

	@Override
	public void remove(T executable) throws BusinessException {
		// Check that there are no other technical service inheriting from it
		List<String> children = getChildrenServices(executable);
		if(!children.isEmpty()) {
			throw new IllegalArgumentException("Can't remove service " + executable.getCode()
					+ " because following services inherit from it: " + children);
		}
		
		super.remove(executable);
	}

	@Override
    public void afterUpdateOrCreate(T executable) {}

    @Override
    protected void validate(T executable) {}

    @Override
    protected String getCode(T executable) {
        return executable.getName() + "." + executable.getFunctionVersion();
    }
    
    /**
     * Remove the inherited descriptions from persisted object
     * 
     * @param executable target Technical service
     */
    public Map<String, Description> retainInheritedDescriptions(T executable, Map<String, Description> descriptions) {
    	for(Description description : descriptions.values()) {
    		if(description.isInherited()) {
    			description.getInputProperties().removeIf(InputMeveoProperty::isInherited);
    			description.getOutputProperties().removeIf(OutputMeveoProperty::isInherited);
    		}
    	}
    	
    	Map<String, Description> inheritedDecriptions = executable.getExtendedServices()
    			.stream()
    			.map(service -> description(service.getCode()))
    			.flatMap(Collection::stream)
    			.collect(Collectors.toMap(Description::getName, Function.identity()));
    	
    	List<String> toRemove = new ArrayList<>();
    	
    	for(Description description : descriptions.values()) {
    		if(description.isInherited()) {
    			// Compute difference between inherited and actual description
    			Description inheritedDescription = inheritedDecriptions.get(description.getName());
    			
    			if(inheritedDescription != null) {
	    			// Check if input / ouput is different
	    			if(description.isInput() != inheritedDescription.isInput()) {
	    				continue;
	    			}
	    			
	    			if(description.isOutput() != inheritedDescription.isOutput()) {
	    				continue;
	    			}
	    			
	    			if(!description.getInputProperties().isEmpty()) {
	    				continue;
	    			}
	    			
	    			if(!description.getOutputProperties().isEmpty()) {
	    				continue;
	    			}
    			}
    			
    			toRemove.add(description.getName());    			
    		}
    	}
    	
    	descriptions.values().removeIf(d -> {
    		boolean remove = toRemove.contains(d.getName());
    		if(remove) {
    			getEntityManager().remove(d);
    		}
    		return remove;
    	});
    	return descriptions;
//    	toRemove.forEach(executable.getDescriptions()::remove);
    }

    private QueryBuilder filteredQueryBuilder(TechnicalServiceFilters filters) {
        QueryBuilder qb = new QueryBuilder(getEntityClass(), "service", null);
        if(filters.getName() != null){
            qb.addCriterion("service.name", "=", filters.getName(), true);
        }else if(filters.getLikeName() != null){
            qb.addCriterion("service.name", "like", filters.getName(), true);
        }

        if(filters.isOnlyActive()){
            qb.addCriterion("service.disabled", "=", false, false);
        }

        return qb;
    }

    private QueryBuilder queryBuilder(TechnicalServiceFilters filters, Date sinceDate) {
        QueryBuilder qb = new QueryBuilder(getEntityClass(), "service", null);
        if(filters.getName() != null){
            qb.addCriterion("service.name", "=", filters.getName(), true);
        }else if(filters.getLikeName() != null){
            qb.addCriterion("service.name", "like", filters.getName(), true);
        }
        if (sinceDate != null) {
            qb.addCriterion("service.auditable.created", ">=", sinceDate, true);
        }
        return qb;
    }
    
    /**
     * Return the services that inherit from the given service
     * 
     * @param parentService the parent service
     * @return the children services
     */
    public List<String> getChildrenServices(T parentService) {
    	final String query = "SELECT service.code FROM " + TechnicalService.class.getName() + " as service \n"
    			+ "WHERE :parentService MEMBER OF service.extendedServices";
    	
    	return getEntityManager().createQuery(query, String.class)
    			.setParameter("parentService", parentService)
    			.getResultList();
    	
    }
}
