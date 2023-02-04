/**
 * 
 */
package org.meveo.modules.infinispan;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;

import org.hibernate.search.annotations.Field;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.QueryFactory;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.CustomEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.storage.IStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.PersistenceActionResult;
import org.meveo.persistence.StorageImpl;
import org.meveo.persistence.StorageQuery;
import org.meveo.service.base.QueryBuilderHelper;
import org.meveo.service.custom.CustomEntityTemplateCompiler;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.script.Script;
import org.slf4j.Logger;

import com.github.javaparser.JavaParser;

/**
 * 
 * @author heros
 * @since 
 * @version
 */
public class InfinispanStorage extends Script implements StorageImpl  {

	private static Logger log = org.slf4j.LoggerFactory.getLogger(InfinispanStorage.class);
	
	private EmbeddedCacheManager cacheContainer;
	
	private CustomEntityTemplateCompiler cetCompiler = getCDIBean(CustomEntityTemplateCompiler.class);
	private CustomEntityTemplateService cetService = getCDIBean(CustomEntityTemplateService.class);
	
	@Override
	public boolean exists(IStorageConfiguration repository, CustomEntityTemplate cet, String uuid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String findEntityIdByValues(Repository repository, IStorageConfiguration conf, CustomEntityInstance cei) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> findById(IStorageConfiguration repository, CustomEntityTemplate cet, String uuid, Map<String, CustomFieldTemplate> cfts, Collection<String> fetchFields, boolean withEntityReferences) {
		var pojo = getCache(cet.getCode()).get(uuid);
		try {
			return JacksonUtil.toMap(pojo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<Map<String, Object>> find(StorageQuery query) throws EntityDoesNotExistsException {
		query.getPaginationConfiguration();
		QueryBuilder queryBuilder = QueryBuilderHelper.getQuery(null, getClass());
		return null;
	}

	@Override
	public PersistenceActionResult createOrUpdate(Repository repository, IStorageConfiguration storageConf, CustomEntityInstance cei, Map<String, CustomFieldTemplate> customFieldTemplates, String foundUuid) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistenceActionResult addCRTByUuids(IStorageConfiguration repository, CustomRelationshipTemplate crt, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Repository repository, IStorageConfiguration conf, CustomEntityInstance cei) throws BusinessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBinaries(IStorageConfiguration repository, CustomEntityTemplate cet, CustomFieldTemplate cft, String uuid, List<File> binaries) throws BusinessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(IStorageConfiguration repository, CustomEntityTemplate cet, String uuid) throws BusinessException {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer count(IStorageConfiguration repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Create the persisted cache
	 */
	@Override
	public void cetCreated(CustomEntityTemplate cet) {
        if (!cacheContainer.cacheExists(cet.getCode())) {
        	PersistenceConfigurationBuilder confBuilder = new ConfigurationBuilder()
        			.persistence()
                    .passivation(false);
            Configuration persistentFileConfig = confBuilder.addSingleFileStore()
                    .location(cet.getCode())
                    .preload(true)
                    .purgeOnStartup(false)
                    .build();
            cacheContainer.defineConfiguration(cet.getCode(), persistentFileConfig);
        }
        updateJavaFileWithAnnotations(cet);
	}

	@Override
	public void crtCreated(CustomRelationshipTemplate crt) throws BusinessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void cftCreated(CustomModelObject template, CustomFieldTemplate cft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cetUpdated(CustomEntityTemplate oldCet, CustomEntityTemplate cet) {
		updateJavaFileWithAnnotations(cet);
	}

	@Override
	public void crtUpdated(CustomRelationshipTemplate cet) throws BusinessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void cftUpdated(CustomModelObject template, CustomFieldTemplate oldCft, CustomFieldTemplate cft) {
		updateJavaFileWithAnnotations((CustomEntityTemplate) template);
	}

	@Override
	public void removeCft(CustomModelObject template, CustomFieldTemplate cft) {
		updateJavaFileWithAnnotations((CustomEntityTemplate) template);
	}

	@Override
	public void removeCet(CustomEntityTemplate cet) {
		
	}

	@Override
	public void removeCrt(CustomRelationshipTemplate crt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		if (cacheContainer != null) {
	    	try {
				InitialContext initialContext = new InitialContext();
				cacheContainer = (EmbeddedCacheManager) initialContext.lookup("java:jboss/infinispan/container/meveo");
			} catch (Exception e) {
				log.error("Cannot instantiate cache container", e);
			}
		}
	}

	@Override
	public <T> T beginTransaction(IStorageConfiguration repository, int stackedCalls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void commitTransaction(IStorageConfiguration repository) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollbackTransaction(int stackedCalls) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
	
	private Cache<String, CustomEntity> getCache(String code) {
		return cacheContainer.getCache(code);
	}
	
    public org.infinispan.query.dsl.Query getQuery(QueryBuilder queryBuilder, String code) {
    	QueryFactory queryFactory = Search.getQueryFactory(getCache(code));
    	queryBuilder.applyPagination(queryBuilder.getPaginationSortAlias());

    	org.infinispan.query.dsl.Query query = queryFactory.create(queryBuilder.toString());

    	if (queryBuilder.getPaginationConfiguration() != null) {
    		if (queryBuilder.getPaginationConfiguration().getFirstRow() != null) {
    			query.startOffset(queryBuilder.getPaginationConfiguration().getFirstRow());
    		}
    		if (queryBuilder.getPaginationConfiguration().getNumberOfRows() != null) {
    			query.maxResults(queryBuilder.getPaginationConfiguration().getNumberOfRows());
    		}
    	}
        
        for (Map.Entry<String, Object> e : queryBuilder.getParams().entrySet()) {
        	query.setParameter(e.getKey(), e.getValue());
        }
        return query;
    }
	
	private void updateJavaFileWithAnnotations(CustomEntityTemplate cet) {
		final File cetJavaDir = cetCompiler.getJavaCetDir(cet, cetService.findModuleOf(cet));
		final File javaFile = new File(cetJavaDir, cet.getCode() + ".java");
		try {
			var compilationUnit = JavaParser.parse(javaFile);
			var cetClass = compilationUnit.getClassByName(cet.getCode()).get();
			cetClass.getFields()
				.forEach(field -> field.addAnnotation(Field.class));
			MeveoFileUtils.writeAndPreserveCharset(compilationUnit.toString(), javaFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
