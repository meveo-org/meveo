package org.meveo.persistence.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.service.base.BusinessService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.custom.CustomTableCreatorService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.8.0
 * @since 6.6.0
 */
@Stateless
public class SqlConfigurationService extends BusinessService<SqlConfiguration> {

	@Inject
	private SQLConnectionProvider sqlConnectionProvider;

	@Inject
	@Updated
	private Event<SqlConfiguration> sqlConfigurationUpdatedEvent;

	@Inject
	@Created
	private Event<SqlConfiguration> sqlConfigurationCreatedEvent;

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	@Inject
	private CustomFieldTemplateService customFieldTemplateService;

	@Inject
	private CustomTableCreatorService customTableCreatorService;

	@Inject
	private SqlConfigurationService sqlConfigurationService;
	
	@Inject
	private CustomRelationshipTemplateService customRelationshipTemplateService;
	
	/**
	 * Gets the {@link SQLStorageConfiguration#schema}
	 * 
	 * @param sqlConfigurationCode Code of the {@link SqlConfiguration}
	 * @return
	 */
	public String getSchema(String sqlConfigurationCode) {
		String sql = "SELECT schema FROM " + SqlConfiguration.class.getName() + " WHERE code =:code";
		return getEntityManager().createQuery(sql, String.class) //
				.setParameter("code", sqlConfigurationCode) //
				.setHint("org.hibernate.cacheable", true) //
				.getSingleResult();
	}

	@Override
	public void create(SqlConfiguration entity) throws BusinessException {
		setDbSchema(entity);

		sqlConfigurationService.createInNewTx(entity);
		
		if(!entity.getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION)) {
			sqlConfigurationService.initializeCet(entity);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void createInNewTx(SqlConfiguration entity) throws BusinessException {
		if (entity.getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION) || testConnection(entity)) {
			super.create(entity);

		} else {
			throw new BusinessException("SqlConfiguration with code " + entity.getCode() + " is not valid.");
		}
	}

	@Override
	public SqlConfiguration update(SqlConfiguration entity) throws BusinessException {
		setDbSchema(entity);

		if (testConnection(entity)) {
			entity = super.update(entity);
			sqlConfigurationUpdatedEvent.fire(entity);

		} else {
			throw new BusinessException("SqlConfiguration with code " + entity.getCode() + " is not valid.");
		}

		return entity;
	}

	/**
	 * Check if the given connection code is valid.
	 * 
	 * @param sqlConfigurationCode the sqlConfiguration stored in the database
	 * @return true if the connection is valid, false otherwise
	 */
	public boolean testConnection(SqlConfiguration sqlConfiguration) {


		return sqlConnectionProvider.testSession(sqlConfiguration);
	}

	/**
	 * Check if the given connection code is valid.
	 * 
	 * @param sqlConfigurationCode the sqlConfiguration stored in the database
	 * @return true if the connection is valid, false otherwise
	 */
	public boolean testConnection(String sqlConfigurationCode) {

		boolean result = false;
		try (Session session = sqlConnectionProvider.getSession(sqlConfigurationCode)) {
			if (session != null) {
				result = true;
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<SqlConfiguration> listActiveAndInitialized() {

		QueryBuilder qb = new QueryBuilder(SqlConfiguration.class, "s");
		qb.addCriterion("initialized", "=", true, false);
		qb.addCriterion("disabled", "=", false, true);

		return qb.getQuery(getEntityManager()).getResultList();
	}
	
	public List<CustomModelObject> initializeModuleDatabase(String moduleCode, String sqlConfCode) throws BusinessException {
		List<CustomModelObject> initializedItems = new ArrayList<>();
		
		MeveoModule module = meveoModuleService.findByCode(moduleCode);
		Set<MeveoModuleItem> moduleItems = module.getModuleItems();
		
		List<String> moduleCrtCodes = moduleItems.stream()
				.filter(item -> item.getItemClass().equals(CustomRelationshipTemplate.class.getName()))
				.distinct()
				.map(item -> item.getItemCode())
				.collect(Collectors.toList());
		
		List<String> moduleCetCodes = moduleItems.stream()
				.filter(item -> item.getItemClass().equals(CustomEntityTemplate.class.getName()))
				.distinct()
				.map(item -> item.getItemCode())
				.collect(Collectors.toList());
		
		List<CustomEntityTemplate> moduleCets = new ArrayList<CustomEntityTemplate>();
		moduleCetCodes.forEach(moduleCetCode -> {
			moduleCets.add(customEntityTemplateService.findByCode(moduleCetCode));
		});
		
		List<CustomRelationshipTemplate> moduleCrts = new ArrayList<CustomRelationshipTemplate>();
		moduleCrtCodes.forEach(moduleCrtCode -> {
			moduleCrts.add(customRelationshipTemplateService.findByCode(moduleCrtCode));
		});
		
		initializedItems.addAll(moduleCets);
		initializedItems.addAll(moduleCrts);

		for(CustomEntityTemplate moduleCet : moduleCets) {			
			customTableCreatorService.createTable(sqlConfCode, moduleCet);
		}
	
		for(CustomRelationshipTemplate moduleCrt : moduleCrts) {				
			try {
				customTableCreatorService.createCrtTable(sqlConfCode, moduleCrt);
			} catch (BusinessException e) {
				log.error("Couldn't create CRT table", e);
				throw e;
			}
		}
		
		return initializedItems;
	}

	/**
	 * Initialize the schema of this SqlConfiguration from the default datasource.
	 * 
	 * @param entity target data source
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void initializeCet(SqlConfiguration entity) {

		// commented for now as it requires an admin role for the database user
		// if (entity.getDriverClass().toLowerCase().contains("postgresql")) {
		// customTableCreatorService.executePostgreSqlExtension(entity.getCode());
		// }
		
		boolean initialized = true;

		// get all cet
		List<CustomEntityTemplate> cets = new ArrayList<>(customEntityTemplateService.listNoCache());
		
		Map<String, Boolean> jpaReferenceMatrix = new HashMap<>();
		
		// Sort cet by references
		Collections.sort(cets, (c1, c2) -> {
			Map<String, CustomFieldTemplate> cftsC1 = customFieldTemplateService.findByAppliesToNoCache(c1.getAppliesTo());
			Map<String, CustomFieldTemplate> cftsC2 = customFieldTemplateService.findByAppliesToNoCache(c2.getAppliesTo());

			// Put cets with no references in top of the list
			var c1HasReferences = cftsC1.values()
				.stream()
				.anyMatch(cft -> cft.getFieldType() == CustomFieldTypeEnum.ENTITY);
			
			if(c1HasReferences) {
				jpaReferenceMatrix.computeIfAbsent(c1.getCode(), k -> cftsC1.values()
					.stream()
					.filter(e -> e.getFieldType().equals(CustomFieldTypeEnum.ENTITY))
					.anyMatch(e -> customFieldTemplateService.isReferenceJpaEntity(e.getEntityClazzCetCode())));
			}
			
			var c2HasReferences = cftsC2.values()
				.stream()
				.anyMatch(cft -> cft.getFieldType() == CustomFieldTypeEnum.ENTITY);
			
			if(c2HasReferences) {
				jpaReferenceMatrix.computeIfAbsent(c2.getCode(), k -> cftsC2.values()
					.stream()
					.filter(e -> e.getFieldType().equals(CustomFieldTypeEnum.ENTITY))
					.anyMatch(e -> customFieldTemplateService.isReferenceJpaEntity(e.getEntityClazzCetCode())));
			}
			
			if(!c1HasReferences && !c2HasReferences) {
				return 0;
			} else if(c1HasReferences && !c2HasReferences) {
				return 1;
			} else if(!c1HasReferences && c2HasReferences) {
				return -1;
			}
			
			boolean c1RefersC2 = cftsC1.values()
					.stream()
					.anyMatch(cft -> c2.getCode().equals(cft.getEntityClazzCetCode()));
			
			if(c1RefersC2) {
				return 1;
			}
			
			boolean c2RefersC1 = cftsC2.values()
					.stream()
					.anyMatch(cft -> c1.getCode().equals(cft.getEntityClazzCetCode()));
			
			if(c2RefersC1) {
				return -1;
			}
			
			return 0;
		});
		
		List<String> skippedCets = new ArrayList<>();
		for (CustomEntityTemplate cet : cets) {
			if(!cet.isStoreAsTable()) {
				continue;
			}
			
			// Skip CET if it has a reference to a JPA entity
			if(!entity.getCode().equals(SqlConfiguration.DEFAULT_SQL_CONNECTION) && 
					jpaReferenceMatrix.getOrDefault(cet.getCode(), false)) {
				skippedCets.add(cet.getCode());
				continue;
			}
			
			Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesToNoCache(cet.getAppliesTo());
			
			// Skip CET if it has reference to a skipped entity
			boolean skip = cfts.values()
				.stream()
				.filter(e -> e.getFieldType().equals(CustomFieldTypeEnum.ENTITY))
				.anyMatch(e -> skippedCets.contains(e.getEntityClazzCetCode()));
			if(skip) {
				skippedCets.add(cet.getCode());
				continue;
			}
			
			String tableName = SQLStorageConfiguration.getCetDbTablename(cet.getCode());
			customTableCreatorService.createTable(entity.getCode(), cet);

			try {
				Thread.sleep(250);
				for (Entry<String, CustomFieldTemplate> cftEntry : cfts.entrySet()) {
					if(cftEntry.getValue().isSqlStorage()) {
						customTableCreatorService.addField(entity.getCode(), tableName, cftEntry.getValue());
						Thread.sleep(250);
					}
				}
			} catch (InterruptedException e) {
				log.error("Interrupted creating table for {}", cet);
			}
		}

		entity = findByCode(entity.getCode());
		entity.setInitialized(initialized);
	}
	
	private void setDbSchema(SqlConfiguration entity) {
		if(!StringUtils.isBlank(entity.getSchema())) {
			if(!entity.getUrl().contains("currentSchema=" + entity.getSchema())) {
				if(entity.getUrl().contains("?")) {
					entity.setUrl(entity.getUrl() + "&currentSchema=" + entity.getSchema());
				} else {
					entity.setUrl(entity.getUrl() + "?currentSchema=" + entity.getSchema());
				}
			}
		}
	}
}
