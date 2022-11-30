/**
 * 
 */
package org.meveo.service.script.test;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.IEntity;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.model.tests.TestResultDto;
import org.meveo.service.base.local.IPersistenceService;
import org.primefaces.model.SortOrder;

/**
 * Not a real DAO service. It is used to compute a specific view based on 
 * {@link JobInstance} and {@link JobExecutionResultImpl} records
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
@Transactional
public class TestResultService implements IPersistenceService<TestResultDto>{
	
	@Inject
	@MeveoJpa
	private EntityManagerWrapper emWrapper;
	
	@Override
	public List<TestResultDto> list() {
		return list(null);
	}

	/**
	 * For this method, we use a native query as we need to do a sub-query with limit, 
	 * which is not possible with HQL
	 * 
	 * @see {@link IPersistenceService#list(PaginationConfiguration)}
	 */
	@Override
	public List<TestResultDto> list(PaginationConfiguration config) {
		int history = 1;
		
		if(config.getFilters().get("history") != null) {
			history = Integer.parseInt((String) config.getFilters().get("history"));
		}
		
		String query = "SELECT jeri.nb_success as nb_success, " +
				"ji.id as id," +
				"jeri.nb_error as nb_error, " + 
				"jeri.nb_warning as nb_warning, " + 
				"fn.code as function_code, " + 
				"jeri.end_date as end_date " + 
				"FROM job_execution jeri " + 
				"INNER JOIN meveo_job_instance ji " + 
				"ON ji.id = jeri.job_instance_id, " + 
				"meveo_function fn  \r\n" + 
				"    LEFT JOIN function_category fnCategory \r\n" + 
				"        ON fnCategory.id = fn.category_id\r\n" + 
				"\r\n" + 
				"WHERE jeri.id IN ( \r\n" + 
				"    SELECT jeri1.id " +
				"    FROM job_execution jeri1\r\n" + 
				"    WHERE jeri1.job_instance_id = ji.id\r\n" + 
				"    ORDER BY jeri1.end_date DESC  \r\n" + 
				"    LIMIT " + history +
				")\r\n" + 
				"AND ji.job_category = 'TEST'  \r\n" + 
				"AND fn.code = ji.parametres ";
		
		//  Filters 
		if(config != null && config.getFilters().containsKey("category")) {
			query += "AND fnCategory.code = :code ";
		}
		
		if(config != null && config.getFilters().get("active") != null) {
			boolean active = (boolean) config.getFilters().get("active");
			if(active) {
				query += "AND ji.disabled = 0 ";
			}
		}
		
		/*******************  Ordering ******************************/
		String sortField = config.getSortField();
		String order = null;
		if(config.getOrdering() != SortOrder.ASCENDING) {
			order = "ASC";
		} else if(config.getOrdering() != SortOrder.DESCENDING) {
			order = "DESC";
		}

		// Convert field
		switch(sortField) {
			case "endDate" : 
				sortField = "end_date";
				break;
			case "functionCode":
				sortField = "fn.code";
				break;
			case "stable" : 
			case "nbKo" : 
				sortField = "jeri.nb_error";
				break;
			case "nbWarnings":
				sortField = "jeri.nb_warning";
				break;
			case "nbOk" : 
				sortField = "jeri.nb_success";
				break;
			default:
				sortField = null;
		}
		
		if(sortField != null && order != null) {
			query += "\nORDER BY " + sortField + " " + order;
		}
		
		Query typedQuery = emWrapper.getEntityManager()
				.createNativeQuery(query, Tuple.class);
		
		//  Filters values 
		if(config != null && config.getFilters().containsKey("category")) {
			typedQuery.setParameter("code", config.getFilters().get("category"));
		}
		
		typedQuery.setFirstResult(config.getFirstRow());
		typedQuery.setMaxResults(config.getNumberOfRows());
		
		Stream<Tuple> resultList = (Stream<Tuple>) typedQuery.getResultStream();
		
		return resultList.map(t -> {
			return new TestResultDto(
					((BigInteger) t.get("id")).longValue(),
					((BigInteger) t.get("nb_success")).longValue(),
					((BigInteger) t.get("nb_error")).longValue(),
					((BigInteger) t.get("nb_warning")).longValue(),
					(String) t.get("function_code"),
					(Date) t.get("end_date"));
		}).collect(Collectors.toList());
	}

	@Override
	public long count() {
		return count(null);
	}

	@Override
	public long count(PaginationConfiguration config) {
		String query = "SELECT COUNT(jeri) FROM JobExecutionResultImpl jeri \r\n" + 
				"    JOIN jeri.jobInstance ji, \r\n" + 

				"    Function fn\r\n" + 
				"    LEFT JOIN fn.category fnCategory \r\n" + 
				"\r\n" + 
				"WHERE jeri.endDate = (\r\n" + 
				"    SELECT max(jeri1.endDate)\r\n" + 
				"    FROM JobExecutionResultImpl jeri1 \r\n" + 
				"    WHERE jeri1.jobInstance.id = ji.id\r\n" + 
				")\r\n" + 
				"AND ji.jobCategoryEnum = 'TEST'\r\n" + 
				"AND fn.code = ji.parametres\r\n";
		
		//  Filters 
		if(config != null && config.getFilters().containsKey("category")) {
			query += "AND fnCategory.code = :code ";
		}
		
		if(config != null && config.getFilters().get("active") != null) {
			boolean active = (boolean) config.getFilters().get("active");
			if(active) {
				query += "AND ji.disabled = false";
			}
		}
		
		TypedQuery<Long> typedQuery = emWrapper.getEntityManager()
				.createQuery(query, Long.class);
		
		//  Filters values 
		if(config != null && config.getFilters().containsKey("category")) {
			typedQuery.setParameter("code", config.getFilters().get("category"));
		}
		
		return typedQuery.getSingleResult();
	}
	
	@Override
	public List<TestResultDto> listActive() {
		return null;
	}

	@Override
	public TestResultDto findById(Long id) {
		return null;
	}

	@Override
	public TestResultDto findById(Long id, List<String> fetchFields) {
		return null;
	}

	@Override
	public TestResultDto findById(Long id, boolean refresh) {
		return null;
	}

	@Override
	public TestResultDto findById(Long id, List<String> fetchFields, boolean refresh) {
		return null;
	}

	@Override
	public void create(TestResultDto e) throws BusinessException {
	}

	@Override
	public TestResultDto update(TestResultDto e) throws BusinessException {
		return null;
	}

	@Override
	public TestResultDto disable(Long id) throws BusinessException {
		return null;
	}

	@Override
	public TestResultDto disable(TestResultDto e) throws BusinessException {
		return null;
	}

	@Override
	public TestResultDto enable(Long id) throws BusinessException {
		return null;
	}

	@Override
	public TestResultDto enable(TestResultDto e) throws BusinessException {
		return null;
	}

	@Override
	public void remove(Long id) throws BusinessException {
		
	}

	@Override
	public void remove(TestResultDto e) throws BusinessException {
	}

	@Override
	public void remove(Set<Long> ids) throws BusinessException {
	}

	@Override
	public Class<TestResultDto> getEntityClass() {
		return null;
	}

	@Override
	public void detach(TestResultDto entity) {
	}

	@Override
	public void refresh(IEntity entity) {
	}

	@Override
	public TestResultDto refreshOrRetrieve(TestResultDto entity) {
		return null;
	}

	@Override
	public List<TestResultDto> refreshOrRetrieve(List<TestResultDto> entities) {
		return null;
	}

	@Override
	public Set<TestResultDto> refreshOrRetrieve(Set<TestResultDto> entities) {
		return null;
	}

	@Override
	public TestResultDto retrieveIfNotManaged(TestResultDto entity) {
		return null;
	}

	@Override
	public List<TestResultDto> retrieveIfNotManaged(List<TestResultDto> entities) {
		return null;
	}

	@Override
	public Set<TestResultDto> retrieveIfNotManaged(Set<TestResultDto> entities) {
		return null;
	}

	@Override
	public void flush() {
	}

	@Override
	public EntityManager getEntityManager() {
		return emWrapper.getEntityManager();
	}

	@Override
	public TestResultDto update(TestResultDto entity, boolean asyncEvent) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TestResultDto findByCode(String code) {
		// TODO Auto-generated method stub
		return null;
	}

}