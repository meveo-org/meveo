/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
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
package org.meveo.service.admin.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.commons.io.FileUtils;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.weld.contexts.ContextNotActiveException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.EntityAlreadyLinkedToModule;
import org.meveo.admin.listener.CommitMessageBean;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.ApiService;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.module.ModuleDependencyDto;
import org.meveo.api.dto.module.ModuleReleaseDto;
import org.meveo.api.dto.response.module.MeveoModuleDtosResponse;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.commons.utils.EjbUtils;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.export.RemoteAuthenticationException;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModulePostInstall;
import org.meveo.model.communication.MeveoInstance;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.git.GitRepository;
import org.meveo.model.jobs.JobExecutionResultImpl;
import org.meveo.model.jobs.JobInstance;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleDependency;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.module.MeveoModulePatch;
import org.meveo.model.module.ModuleRelease;
import org.meveo.model.module.ModuleReleaseItem;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.security.PasswordUtils;
import org.meveo.service.base.BusinessService;
import org.meveo.service.base.BusinessServiceFinder;
import org.meveo.service.communication.impl.MeveoInstanceService;
import org.meveo.service.config.impl.MavenConfigurationService;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.GitRepositoryService;
import org.meveo.service.job.JobExecutionService;
import org.meveo.service.job.JobInstanceService;
import org.meveo.service.script.ScriptInstanceService;

/**
 * EJB for managing MeveoModule entities
 * @author Clément Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @lastModifiedVersion 6.10
 */
@Stateless
public class MeveoModuleService extends GenericModuleService<MeveoModule> {

    @Inject
    private MeveoInstanceService meveoInstanceService;
    
    @Inject
    private MavenConfigurationService mavenConfigurationService;
    
    @Inject
    private JobInstanceService jobInstanceService;

    @Inject
    private JobExecutionService jobExecutionService;
    
    @Inject
    private GitRepositoryService gitRepositoryService;
    
    @Inject
    private GitClient gitClient;
    
    @Inject
    private BusinessServiceFinder businessServiceFinder;
    
    @Inject
    private ScriptInstanceService scriptInstanceService;

    @Inject
    CommitMessageBean commitMessageBean;

    /**
     * Add missing dependencies of each module item
     * 
     * @param moduleCode Code of the module to synchronize
     * @return the total number of items added to the module
     * @throws BusinessException if the module can't be updated
     */
	public int synchronizeLinkedItems(String moduleCode) throws BusinessException {
		MeveoModule meveoModule = findByCode(moduleCode, List.of("moduleItems"));
		List<MeveoModuleItem> newItems = getMissingItems(meveoModule);
		int totalCount = 0;
		
		while (!newItems.isEmpty()) {
			totalCount += newItems.size();
			meveoModule.getModuleItems().addAll(newItems);
			newItems = getMissingItems(meveoModule);
		}
		
		if(totalCount > 0) {
			meveoModule.getModuleItems().forEach(m -> m.setMeveoModule(meveoModule));
			update(meveoModule);
		}
		
		return totalCount;
	}

	/**
	 * @param MeveoModule meveoModule the module to get missing items froms
	 * @return the missing dependencies of each module item
	 */
	private List<MeveoModuleItem> getMissingItems(MeveoModule meveoModule) {
		List<MeveoModuleItem> newItems = new ArrayList<>();
		
		for (MeveoModuleItem item : meveoModule.getModuleItems()) {
			try {
				// Check if the module item has possible dependencies
				Class<?> clazz = Class.forName(item.getItemClass());
				List<Field> fields = ReflectionUtils.getAllFields(new ArrayList<>(), clazz)
						.stream()
						.filter(f -> f.getType().getAnnotation(ModuleItem.class) != null)
						.collect(Collectors.toList());
				
				// Load each dependency and add them as module item if they are not present
				if(!fields.isEmpty()) {
					Object loadedItem = getItemEntity(item, clazz);
					
					for(Field field : fields) {
						boolean canAccess = field.canAccess(loadedItem);
						field.setAccessible(true);
						BusinessEntity fieldValue = (BusinessEntity) field.get(loadedItem);
						
						if(fieldValue instanceof HibernateProxy) {
							Hibernate.initialize(fieldValue); 
							fieldValue = (BusinessEntity) ((HibernateProxy) fieldValue)
					                  .getHibernateLazyInitializer()
					                  .getImplementation();
						}
						
						if(fieldValue != null) {
							MeveoModuleItem newItem = new MeveoModuleItem(fieldValue);
							if(!meveoModule.getModuleItems().contains(newItem)) {
								newItems.add(newItem);
							}
						}
						field.setAccessible(canAccess);
					}
				}
			} catch (Exception e) {
				log.error("Cannot retrieve dependencies for module item {}", item, e);
			}
		}
		

		return newItems;
	}

    /**
     * import module from remote meveo instance.
     * 
     * @param meveoInstance meveo instance
     * @return list of meveo module
     * @throws BusinessException business exception.
     */
    public List<MeveoModuleDto> downloadModulesFromMeveoInstance(MeveoInstance meveoInstance) throws BusinessException {
        List<MeveoModuleDto> result;
        try {
            String url = "api/rest/module/list";
            String baseurl = meveoInstance.getUrl().endsWith("/") ? meveoInstance.getUrl() : meveoInstance.getUrl() + "/";
            String username = meveoInstance.getAuthUsername() != null ? meveoInstance.getAuthUsername() : "";
            String password = meveoInstance.getAuthPassword() != null ? meveoInstance.getAuthPassword() : "";
    		if(password != null) {
    			password = PasswordUtils.decrypt(meveoInstance.getSalt(), password);
    		}
            
            ResteasyClient client = meveoInstanceService.getRestEasyClient();
            ResteasyWebTarget target = client.target(baseurl + url);
            BasicAuthentication basicAuthentication = new BasicAuthentication(username, password);
            target.register(basicAuthentication);

            Response response = target.request().get();
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED || response.getStatus() == HttpURLConnection.HTTP_FORBIDDEN) {
                    throw new RemoteAuthenticationException("Http status " + response.getStatus() + ", info " + response.getStatusInfo().getReasonPhrase());
                } else {
                    throw new BusinessException("Http status " + response.getStatus() + ", info " + response.getStatusInfo().getReasonPhrase());
                }
            }

            MeveoModuleDtosResponse resultDto = response.readEntity(MeveoModuleDtosResponse.class);
            if (resultDto == null) {
                throw new BusinessException("No response body from meveo instance " + meveoInstance.getCode());
            }

            if (ActionStatusEnum.SUCCESS != resultDto.getActionStatus().getStatus()) {
                throw new BusinessException("Code " + resultDto.getActionStatus().getErrorCode() + ", info " + resultDto.getActionStatus().getMessage());
            }

            result = resultDto.getModules();
            if (result != null) {
                result.sort(Comparator.comparing(BusinessEntityDto::getCode));
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to communicate {}. Reason {}", meveoInstance.getCode(), (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
            throw new BusinessException("Fail to communicate " + meveoInstance.getCode() + ". Error " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

	/**
	 * Publish meveo module with DTO items to remote meveo instance.
	 * 
	 * @param module        meveo module
	 * @param meveoInstance meveo instance.
	 * @throws BusinessException business exception.
	 */
	@SuppressWarnings("unchecked")
	public void publishModule2MeveoInstance(MeveoModule module, MeveoInstance meveoInstance) throws BusinessException {
		log.debug("export module {} to {}", module, meveoInstance);
		final String url = "api/rest/module/createOrUpdate";

		// check if module is installed
		if (!module.isInstalled()) {
			throw new BusinessException("Only installed modules are allowed to be publish");
		}

		try {
			ApiService<MeveoModule, MeveoModuleDto> moduleApi = (ApiService<MeveoModule, MeveoModuleDto>) EjbUtils.getServiceInterface("MeveoModuleApi");
			if (moduleApi == null) {
				throw new IllegalArgumentException("Cannot find api MeveoModuleApi bean");
			}

			MeveoModuleDto moduleDto = moduleApi.find(module.getCode());

			Response response = meveoInstanceService.publishDto2MeveoInstance(url, meveoInstance, moduleDto);
			ActionStatus actionStatus = response.readEntity(ActionStatus.class);
			if (actionStatus == null) {
				throw new BusinessException("Cannot read response status");
			}

			if (ActionStatusEnum.SUCCESS != actionStatus.getStatus()) {
				throw new BusinessException("Code " + actionStatus.getErrorCode() + ", info " + actionStatus.getMessage());
			}
		} catch (Exception e) {
			log.error("Error when export module {} to {}. Reason {}", module.getCode(), meveoInstance.getCode(),
					(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
			throw new BusinessException("Fail to communicate " + meveoInstance.getCode() + ". Error " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
		}
	}

    @SuppressWarnings("unchecked")
    public List<MeveoModuleItem> findByCodeAndItemType(String code, String className) {
        QueryBuilder qb = new QueryBuilder(MeveoModuleItem.class, "m");
        qb.addCriterion("itemCode", "=", code, true);
        qb.addCriterion("itemClass", "=", className, true);
        try {
            return (List<MeveoModuleItem>) qb.getQuery(getEntityManager()).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<MeveoModuleItem> findModuleItem(String code, String className, String appliesTo) {
    	List<MeveoModuleItem> res;
    	QueryBuilder qb = new QueryBuilder(MeveoModuleItem.class, "m");
    	qb.addCriterion("itemCode", "=", code, true);
    	qb.addCriterion("itemClass", "=", className, true);
    	qb.addCriterion("appliesTo", "=", appliesTo, true);
    	
    	try {
    		res = (List<MeveoModuleItem>) qb.getQuery(getEntityManager()).getResultList();
    	} catch (NoResultException e) {
    		res = null;
    	}
    	
    	return res;
    }

    
    /**
     * Add module item with differentiate if appliesTo is null or not
     * 
     * @param meveoModuleItem Module item
     * @throws BusinessException 
     * @throws IOException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void addModuleItem(@Valid MeveoModuleItem meveoModuleItem, MeveoModule module) throws BusinessException{
    	// Check if the module already contains the module item
    	if(module.getModuleItems().contains(meveoModuleItem)) {
    		return;
    	}
    	
    	List<MeveoModuleItem> existingItems = new ArrayList<MeveoModuleItem>();
    	if (meveoModuleItem.getAppliesTo() == null) {
    		existingItems = this.findByCodeAndItemType(meveoModuleItem.getItemCode(), meveoModuleItem.getItemClass());
    	}else {
    		existingItems = this.findModuleItem(meveoModuleItem.getItemCode(), meveoModuleItem.getItemClass(), meveoModuleItem.getAppliesTo());
    	}
    	
    	if (meveoModuleItem.getItemEntity() == null) {
    		loadModuleItem(meveoModuleItem);
    	}
    	BusinessService businessService = businessServiceFinder.find(meveoModuleItem.getItemEntity());
    	
    	// Throw an error if the item belongs to another module other than the Meveo module
    	boolean belongsToModule = existingItems != null && existingItems
    			.stream()
    			.filter(item -> item.getMeveoModule() != null)
    			.anyMatch(item -> !item.getMeveoModule().getCode().equals("Meveo") && !item.getMeveoModule().getCode().equals(module.getCode()));
    	if (belongsToModule) {
    		throw new EntityAlreadyLinkedToModule(meveoModuleItem, existingItems.get(0).getMeveoModule());
    	}
    	
    	MeveoModule meveoModule = this.findByCode("Meveo");
    	
    	// FIXME: Seems that the module item is added elsewhere in the process so we need the second check (only happens for CFT)
    	if (existingItems == null || existingItems.isEmpty() || existingItems.get(0).getMeveoModule() == null || existingItems.get(0).getMeveoModule().getCode().equals(module.getCode())) {
    		try {
    		    businessService.moveFilesToModule(meveoModuleItem.getItemEntity(), meveoModule, module);
    			module.getModuleItems().add(meveoModuleItem);
    			meveoModuleItem.setMeveoModule(module);
    			businessService.onAddToModule(meveoModuleItem.getItemEntity(), module);
    		} catch (BusinessException | IOException e2) {
				throw new BusinessException("Entity cannot be add or remove from the module", e2);
    		}
    	} else {
    		try {
    		    businessService.moveFilesToModule(meveoModuleItem.getItemEntity(), meveoModule, module);
    		    MeveoModule moduleToRemove = businessService.findModuleOf(meveoModuleItem.getItemEntity());
    		    moduleToRemove.removeItem(meveoModuleItem);
    		    module.getModuleItems().add(meveoModuleItem);
				meveoModuleItem.setMeveoModule(module);
				businessService.onAddToModule(meveoModuleItem.getItemEntity(), module);
    		} catch (BusinessException | IOException e) {
				throw new BusinessException("Entity cannot be add or remove from the module", e);
			}
    	}
    }
    
    /**
     * Check whether the given item is a child of an other active module
     *
     * @param moduleItemCode Module item code
     * @param itemClass      Class of the module item
     * @return <code>true</code> if the given item exists in an other module
     */
    public boolean isChildOfOtherActiveModule(String moduleItemCode, String itemClass) {
        QueryBuilder qb = new QueryBuilder(MeveoModuleItem.class, "i", null, Collections.singletonList("meveoModule"));
        qb.addCriterion("i.itemCode", "=", moduleItemCode, true);
        qb.addCriterion("i.itemClass", "=", itemClass, false);
        qb.addBooleanCriterion("meveoModule.disabled", false);
        return qb.count(getEntityManager()) > 1;
    }

    @SuppressWarnings("unchecked")
    public String getRelatedModulesAsString(String itemCode, String itemClazz, String appliesTo) {
        QueryBuilder qb = new QueryBuilder(MeveoModule.class, "m", Collections.singletonList("moduleItems as i"));
        qb.addCriterion("i.itemCode", "=", itemCode, true);
        qb.addCriterion("i.itemClass", "=", itemClazz, false);
        qb.addCriterion("i.appliesTo", "=", appliesTo, false);
        List<MeveoModule> modules = qb.getQuery(getEntityManager()).getResultList();

        if (modules != null) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (MeveoModule module : modules) {
                if (i != 0) {
                    sb.append(";");
                }
                sb.append(module.getCode());
                i++;
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * Find entity by code - match the beginning of code.
     *
     * @param code Beginning of code
     * @return A list of entities which code starts with a given value
     */
    @SuppressWarnings("unchecked")
    public List<MeveoModule> findLikeWithCode(String code) {
        try {
            QueryBuilder qb = new QueryBuilder(getEntityClass(), "be");
            if (!StringUtils.isBlank(code)) {
                qb.like("be.code", code, QueryBuilder.QueryLikeStyleEnum.MATCH_ANYWHERE, false);
            }

            return (List<MeveoModule>) qb.getQuery(getEntityManager()).getResultList();

        } catch (NoResultException | NonUniqueResultException ne) {
            return null;
        }
    }

    public List<MeveoModule> list(MeveoModuleFilters filters) {
        return list(filters, "*", MeveoModule.class);
    }

    public List<String> listCodesOnly(MeveoModuleFilters filters) {
        return list(filters, "code", String.class);
    }

    private <T> List<T> list(MeveoModuleFilters filters, String projection, Class<T> returnedClass) {
    	if (projection == "*") {
			projection = "";

		} else {
			projection = "." + projection;
		}

    	StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT m" + projection + " FROM " + entityClass.getName() + " m");
        if(filters.getItemType() !=null && filters.getItemClass() != null){
            queryBuilder.append(" INNER JOIN m.moduleItems i");
        }

        Map<String, Object> parameters = new HashMap<>();

        queryBuilder.append(" WHERE 1 = 1 \n");

        if(filters.getActive() != null){
            if(filters.getActive()) {
                queryBuilder.append(" AND m.disabled = 0 ");
            } else {
                queryBuilder.append(" AND m.disabled = 1 ");
            }
        }
        
        if(StringUtils.isNoBlank(filters.getCode())) {
        	queryBuilder.append(" AND code like '%:code%' ");
        	parameters.put("code", filters.getCode());
        }

        if(filters.getDownloaded() != null){
            if(!filters.getDownloaded()) {
                queryBuilder.append(" AND m.moduleSource = '' OR m.moduleSource = null ");
            } else {
                queryBuilder.append(" AND m.moduleSource != null AND m.moduleSource != '' ");
            }
        }

        if(filters.getItemCode() != null && filters.getItemClass() != null){
            queryBuilder.append(" AND i.itemCode = :itemCode AND i.itemClass = :itemClass");
            parameters.put("itemCode", filters.getItemCode());
            parameters.put("itemClass", filters.getItemClass());
        }

        final TypedQuery<T> query = getEntityManager().createQuery(queryBuilder.toString(), returnedClass);
        parameters.forEach(query::setParameter);

        return query.getResultList();
    }

    public void onCftDeleted(@Observes @Removed CustomFieldTemplate cft) {
		Query q = getEntityManager().createNamedQuery("MeveoModuleItem.synchronizeCftDelete");
		q = q.setParameter("itemCode", cft.getCode());
		q = q.setParameter("itemClass", CustomEntityTemplate.class.getName());
		q.executeUpdate();
	}

	public void releaseModule(MeveoModule entity, String nextVersion) throws BusinessException {
        entity = findById(entity.getId(), Arrays.asList("moduleItems", "patches", "moduleDependencies", "moduleFiles"));
        ModuleRelease moduleRelease = new ModuleRelease();
        moduleRelease.setCode(entity.getCode());
        moduleRelease.setDescription(entity.getDescription());
        moduleRelease.setLicense(entity.getLicense());
        moduleRelease.setLogoPicture(entity.getLogoPicture());
        moduleRelease.setScript(entity.getScript());
        moduleRelease.setCurrentVersion(entity.getCurrentVersion());
        moduleRelease.setMeveoVersionBase(entity.getMeveoVersionBase());
        moduleRelease.setMeveoVersionCeiling(entity.getMeveoVersionCeiling());
        moduleRelease.setModuleSource(entity.getModuleSource());
        moduleRelease.setInDraft(false);
        if (CollectionUtils.isNotEmpty(entity.getModuleFiles())) {
            Set<String> moduleFiles = new HashSet<>();
            for (String moduleFile : entity.getModuleFiles()) {
                moduleFiles.add(moduleFile);
            }
            moduleRelease.setModuleFiles(moduleFiles);
        }
        if (CollectionUtils.isNotEmpty(entity.getModuleDependencies())) {
            List<MeveoModuleDependency> dependencies = new ArrayList<>();
            for (MeveoModuleDependency moduleDependency : entity.getModuleDependencies()) {
                dependencies.add(moduleDependency);
            }
            moduleRelease.setModuleDependencies(dependencies);
        }
        if (CollectionUtils.isNotEmpty(entity.getModuleItems())) {
            List<ModuleReleaseItem> moduleReleaseItems = new ArrayList<>();
            for (MeveoModuleItem meveoModuleItem : entity.getModuleItems()) {
                ModuleReleaseItem moduleReleaseItem = new ModuleReleaseItem();
                moduleReleaseItem.setAppliesTo(meveoModuleItem.getAppliesTo());
                moduleReleaseItem.setItemClass(meveoModuleItem.getItemClass());
                moduleReleaseItem.setItemEntity(meveoModuleItem.getItemEntity());
                moduleReleaseItem.setItemCode(meveoModuleItem.getItemCode());
                moduleReleaseItem.setModuleRelease(moduleRelease);
                moduleReleaseItems.add(moduleReleaseItem);
            }
            moduleRelease.setModuleItems(moduleReleaseItems);
        } else if (!StringUtils.isBlank(entity.getModuleSource())) {
            ModuleReleaseDto moduleReleaseDto = JacksonUtil.fromString(entity.getModuleSource(), ModuleReleaseDto.class);
            moduleReleaseDto.setCurrentVersion(entity.getCurrentVersion());
            moduleReleaseDto.setInDraft(false);

            if (CollectionUtils.isNotEmpty(moduleReleaseDto.getModuleFiles())) {
                moduleReleaseDto.getModuleFiles().clear();
            }
            if (CollectionUtils.isNotEmpty(entity.getModuleFiles())) {
                for (String moduleFile : entity.getModuleFiles()) {
                    moduleReleaseDto.getModuleFiles().add(moduleFile);
                }
            }

            if (CollectionUtils.isNotEmpty(moduleReleaseDto.getModuleDependencies())) {
                moduleReleaseDto.getModuleDependencies().clear();
            }
            if (CollectionUtils.isNotEmpty(entity.getModuleDependencies())) {
                for (MeveoModuleDependency dependency : entity.getModuleDependencies()) {
                    moduleReleaseDto.addModuleDependency(dependency);
                }
            }
            moduleRelease.setModuleSource(JacksonUtil.toString(moduleReleaseDto));
        }
        
        // Before setting the next version, release the maven module inside the local .m2 repo
        Path jarPath = Paths.get(mavenConfigurationService.getM2FolderPath(), "org", "meveo", entity.getCode(), entity.getCurrentVersion(), entity.getCode() + "-" + entity.getCurrentVersion() + ".jar");
        File jarFile = jarPath.toFile();
        
        try {
            File artifactFolder = jarFile.getParentFile();
            if (!artifactFolder.exists()) {
            	artifactFolder.mkdirs();
            }
			
	        try (FileOutputStream fos = new FileOutputStream(jarFile)) {
	        	try (JarOutputStream jos = this.buildJar(entity, fos)) {
	    	        File pomFile = new File(artifactFolder, entity.getCode() + "-" + entity.getCurrentVersion() + ".pom");
	    	        FileUtils.copyFile(this.findPom(entity), pomFile);
	        	}
	        }
	        
		} catch (IOException e) {
			throw new BusinessException("Can't install artifact to .m2", e);
		}
        
        entity.setCurrentVersion(nextVersion);
        moduleRelease.setMeveoModule(entity);
        entity.getReleases().add(moduleRelease);
        this.update(entity);
    }

	/**
	 * Retrieves if a function JMeter tests run successfully.
	 * 
	 * @param codeScript code of a function
	 * @return true if there are no error
	 */
	public boolean checkTestSuites(String codeScript) {

		JobInstance jobInstance = jobInstanceService.findByCode("FunctionTestJob_" + codeScript);
		JobExecutionResultImpl result = null;
		if (jobInstance != null)
			result = jobExecutionService.findLastExecutionByInstance(jobInstance);
		if (result != null && result.getNbItemsProcessedWithError() > Long.valueOf("0")) {
			return false;
		}

		return true;
	}

    public MeveoModule mergeModule(MeveoModule entity) {
	    MeveoModule meveoModule = updateModule(entity);
	    return getEntityManager().merge(meveoModule);
    }

    private MeveoModule updateModule(MeveoModule entity) {
        MeveoModule meveoModule = findById(entity.getId());
        Set<MeveoModuleDependency> moduleDependencies = new HashSet<>();
        if (CollectionUtils.isNotEmpty(entity.getModuleDependencies())) {
            for (MeveoModuleDependency meveoModuleDependency : entity.getModuleDependencies()) {
                moduleDependencies.add(meveoModuleDependency);
            }
        }

        // Update module files
        if(meveoModule.getModuleDependencies() != null) {
            meveoModule.getModuleDependencies().clear();
        }
        
        Set<String> moduleFiles = new HashSet<>();
        if (CollectionUtils.isNotEmpty(entity.getModuleFiles())) {
            for (String moduleFile : entity.getModuleFiles()) {
                moduleFiles.add(moduleFile);
            }
        }

        if(meveoModule.getModuleFiles() != null) {
            meveoModule.getModuleFiles().clear();
        }
        
        if (CollectionUtils.isNotEmpty(moduleFiles)) {
            for (String moduleFile : moduleFiles) {
                meveoModule.getModuleFiles().add(moduleFile);
            }
        }
        
        Set<MeveoModuleItem> moduleItems = new HashSet<>();
        if (CollectionUtils.isNotEmpty(entity.getModuleItems())) {
            for (MeveoModuleItem meveoModuleItem : entity.getModuleItems()) {
                moduleItems.add(meveoModuleItem);
            }
        }

        if(meveoModule.getModuleItems() != null) {
            meveoModule.getModuleItems().clear();
        }

        Set<MeveoModulePatch> modulePatches = new HashSet<>();
        if (CollectionUtils.isNotEmpty(entity.getPatches())) {
            for (MeveoModulePatch meveoModulePatch : entity.getPatches()) {
                modulePatches.add(meveoModulePatch);
            }
        }

        if(meveoModule.getPatches() != null) {
           meveoModule.getPatches().clear();
        }

	    meveoModule.setDescription(entity.getDescription());
        meveoModule.setLicense(entity.getLicense());
        meveoModule.setLogoPicture(entity.getLogoPicture());
	    meveoModule.setCurrentVersion(entity.getCurrentVersion());
        meveoModule.setMeveoVersionBase(entity.getMeveoVersionBase());
        meveoModule.setMeveoVersionCeiling(entity.getMeveoVersionCeiling());
        meveoModule.setModuleSource(entity.getModuleSource());
        if (CollectionUtils.isNotEmpty(moduleItems)) {
            for (MeveoModuleItem meveoModuleItem : moduleItems) {
                meveoModule.getModuleItems().add(meveoModuleItem);
            }
        }

        if (CollectionUtils.isNotEmpty(moduleDependencies)) {
            for (MeveoModuleDependency moduleDependency : moduleDependencies) {
                meveoModule.getModuleDependencies().add(moduleDependency);
            }
        }
        if (CollectionUtils.isNotEmpty(modulePatches)) {
            for (MeveoModulePatch modulePatch : modulePatches) {
                meveoModule.getPatches().add(modulePatch);
            }
        }
        return meveoModule;
    }

    public MeveoModule getMeveoModuleByVersionModule(String code, String currentVersion) throws EntityDoesNotExistsException {
        MeveoModule meveoModule = findByCode(code);
        if (meveoModule == null) {
            throw new EntityDoesNotExistsException("The module file cannot be imported because module dependency "+ code +" doesn't exists locally.");
        }
        if (currentVersion.equals(meveoModule.getCurrentVersion())) {
            return meveoModule;
        }
        if (CollectionUtils.isNotEmpty(meveoModule.getReleases())) {
            for (ModuleRelease moduleRelease : meveoModule.getReleases()) {
                if (currentVersion.equals(moduleRelease.getCurrentVersion())) {
                    MeveoModule module = new MeveoModule();
                    module.setCode(moduleRelease.getCode());
                    module.setDescription(moduleRelease.getDescription());
                    module.setScript(moduleRelease.getScript());
                    module.setCurrentVersion(moduleRelease.getCurrentVersion());
                    module.setMeveoVersionBase(moduleRelease.getMeveoVersionBase());
                    module.setMeveoVersionCeiling(moduleRelease.getMeveoVersionCeiling());
                    module.setModuleSource(moduleRelease.getModuleSource());
                    if (CollectionUtils.isNotEmpty(moduleRelease.getModuleFiles())) {
                        Set<String> moduleFiles = new HashSet<>();
                        for (String moduleFile : moduleRelease.getModuleFiles()) {
                            moduleFiles.add(moduleFile);
                        }
                        module.setModuleFiles(moduleFiles);
                    }
                    if (CollectionUtils.isNotEmpty(moduleRelease.getModuleDependencies())) {
                        Set<MeveoModuleDependency> dependencies = new HashSet<>();
                        for (MeveoModuleDependency moduleDependency : moduleRelease.getModuleDependencies()) {
                            dependencies.add(moduleDependency);
                        }
                        module.setModuleDependencies(dependencies);
                    }
                    if (CollectionUtils.isNotEmpty(moduleRelease.getModuleItems())) {
                        Set<MeveoModuleItem> meveoModuleItems = new HashSet<>();
                        for (ModuleReleaseItem releaseItem : moduleRelease.getModuleItems()) {
                            MeveoModuleItem item = new MeveoModuleItem();
                            item.setAppliesTo(releaseItem.getAppliesTo());
                            item.setItemClass(releaseItem.getItemClass());
                            item.setItemEntity(releaseItem.getItemEntity());
                            item.setItemCode(releaseItem.getItemCode());
                            item.setMeveoModule(module);
                            meveoModuleItems.add(item);
                        }
                        module.setModuleItems(meveoModuleItems);
                    }
                    return module;
                }
            }
        }
        return null;
    }

    public Integer findLaterNearestVersion(List<Integer> versions, Integer version) {

	    List<Integer> versionList = new ArrayList<>();
	    for (Integer versionRelease : versions) {
	        if (versionRelease > version) {
	            versionList.add(versionRelease);
            }
        }

        Integer laterVersion = getClosestVersion(versionList, version);
	    return laterVersion;
    }

    public Integer findEarlierNearestVersion(List<Integer> versions, Integer version) {

        List<Integer> versionList = new ArrayList<>();
        for (Integer versionRelease : versions) {
            if (versionRelease < version) {
                versionList.add(versionRelease);
            }
        }

        Integer earlierVersion = getClosestVersion(versionList, version);
        return earlierVersion;
    }

    public Integer getClosestVersion(List<Integer> versions, Integer version) {

        if (versions.size() < 1)
            return null;
        if (versions.size() == 1) {
            return versions.get(0);
        }
        Integer closestValue = versions.get(0);
        Integer leastDistance = Math.abs(versions.get(0) - version);
        for (int i = 0; i < versions.size(); i++) {
            int currentDistance = Math.abs(versions.get(i) - version);
            if (currentDistance < leastDistance) {
                closestValue = versions.get(i);
                leastDistance = currentDistance;
            }
        }
        return closestValue;
    }

    public void removeFilesIfModuleIsDeleted(List<String> moduleFiles) throws IOException {
        for (String moduleFile : moduleFiles) {
            String path = paramBeanFactory.getInstance().getChrootDir(currentUser.getProviderCode()) + File.separator + moduleFile;
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                FileUtils.deleteDirectory(file);
            } else if (file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    public MeveoModule findById(Long id, List<String> fetchFields, boolean refresh) {
	    MeveoModule meveoModule = findById(id);
	    if (getEntityManager().contains(meveoModule)) {
	        getEntityManager().detach(meveoModule);
        }
        return super.findById(id, fetchFields, refresh);
    }

    @Override
    public List<MeveoModule> list(PaginationConfiguration config) {
    	return super.list(config);
    }
    
    public List<String> getLazyLoadedProperties() {
    	return List.of("gitRepository", "patches");
    }
    
    /**
     * Create a GitRepository when a MeveoModule is created
     * 
     * @param meveoModule meveo module created
     * @throws BusinessException business exception
     */
    public void onMeveoModuleCreated(@Observes @Created MeveoModule meveoModule) throws BusinessException {
    	var repo = new GitRepository();
    	repo.setCode(meveoModule.getCode());
    	repo.setDescription(meveoModule.getDescription());
    	
    	if (this.gitRepositoryService.findByCode(meveoModule.getCode()) == null) {
			this.gitRepositoryService.create(repo);
			this.gitClient.checkout(repo, "master", true);
			meveoModule.setGitRepository(repo);
    		
    	} else {
    		meveoModule.setGitRepository(this.gitRepositoryService.findByCode(meveoModule.getCode()));
    	}
            
        if (!meveoModule.isDownloaded()) {
    		mavenConfigurationService.generatePom("Create pom", meveoModule,repo);
    	}
    
    }
    
    
	public void postModuleInstall(@Observes @ModulePostInstall MeveoModule module) throws BusinessException {
//    	MeveoModule thinModule;
//    	
//    	// Generate module.json file
//		try {
//			thinModule = (MeveoModule) BeanUtilsBean.getInstance().cloneBean(module);
//			thinModule.setCode(module.getCode());
//			thinModule.setModuleItems(null);
//			
//			addFilesToModule(thinModule, module);
//		} catch (Exception e) {
//			throw new BusinessException(e);
//		}
//		
//		// Generate maven facet if file does not exists yet
//		mavenConfigurationService.createDefaultPomFile(module.getCode());
	}
	
	
    
    @Override
	public void remove(MeveoModule meveoModule) throws BusinessException {
		super.remove(meveoModule);
		
    	if (meveoModule.getGitRepository() != null) {
			this.gitRepositoryService.remove(meveoModule.getGitRepository());
    	}
	}

	public MeveoModule findByCodeWithFetchEntities(String code) {
		return super.findByCode(code,Arrays.asList("moduleItems", "patches", "releases", "moduleDependencies", "moduleFiles", "repositories"));
	}
	
	/**
	 * Copy the module files into the git directory if they are not present yet
	 * 
	 * @param module the installed module
	 * @throws IOException if a file / directory can't be copied
	 */
	public void copyFilesToGitDirectory(@Observes @ModulePostInstall MeveoModule module) throws IOException {
		if(!CollectionUtils.isEmpty(module.getModuleFiles())) {
			String chrootDir = paramBeanFactory.getInstance().getChrootDir(currentUser.getProviderCode());
			for (String filePath : module.getModuleFiles()) {
				Path source = Paths.get(chrootDir, filePath);
				Path target = Paths.get(GitHelper.getRepositoryDir(currentUser, module.getCode()).getAbsolutePath(), filePath);
				if(!Files.exists(target) && Files.exists(source)) {
					Files.createDirectories(target);
					if (Files.isDirectory(target)) {
						FileUtils.copyDirectory(source.toFile(), target.toFile());
					} else {
						Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
		}
	}
	
	/**
	 * Build a jar output stream containing the maven artifact view of a module
	 * 
	 * @param module the input module
	 * @param os the output stream which will be written into
	 * @return the output stream built
	 * @throws IOException if the output stream can't be accessed
	 */
	public JarOutputStream buildJar(MeveoModule module, OutputStream os) throws IOException {
		JarOutputStream jos = new JarOutputStream(os);
		File javaDir = GitHelper.getRepositoryDir(null, module.getCode())
				.toPath()
				.resolve("facets")
				.resolve("java")
				.toFile();
		for (File f : javaDir.listFiles()) {
			if (f.isDirectory()) {
				org.meveo.commons.utils.FileUtils.addDirectoryToZip(f, jos, null);
			} else {
				org.meveo.commons.utils.FileUtils.addFileToZip(f, jos, null);
			}
		}
		return jos;
	}
	
	/**
	 * Retrieve the pom file of a given module
	 * 
	 * @param module the input module
	 * @return the pom file
	 */
	public File findPom(MeveoModule module) {
		return GitHelper.getRepositoryDir(null, module.getCode())
				.toPath()
				.resolve("facets")
				.resolve("maven")
				.resolve("pom.xml")
				.toFile();
	}

	@Override
	public void addFilesToModule(MeveoModule entity, MeveoModule module) throws BusinessException {
		// Fetch entities for special serialization
		MeveoModule newModule = findByCodeWithFetchEntities(entity.getCode());
		getEntityManager().detach(newModule);
		if (newModule.getScript() != null) {
			newModule.setScript(scriptInstanceService.findById(newModule.getScript().getId(), List.of("sourcingRoles", "executionRoles")));
		}
		
		MeveoModuleDto dto = new MeveoModuleDto(newModule);
		dto.setModuleItems(null);
		
		Stream.ofNullable(newModule.getModuleDependencies())
			.flatMap(Collection::stream)
			.map(MeveoModuleDependency::getCode)
			.map(code -> findByCode(code, List.of("gitRepository")))
			.forEach(dto::addDependency);
		
    	String businessEntityDtoSerialize = JacksonUtil.toStringPrettyPrinted(dto);
    	
    	File gitDirectory = GitHelper.getRepositoryDir(currentUser, module.getCode());
    	
    	File newJsonFile = new File(gitDirectory, "module.json");
    	try {
    		MeveoFileUtils.writeAndPreserveCharset(businessEntityDtoSerialize, newJsonFile);
    	} catch (IOException e) {
    		throw new BusinessException("File cannot be updated or created", e);
    	}
    	
    	GitRepository gitRepository = gitRepositoryService.findByCode(module.getCode());

        String message = "Add module descriptor file";
        try {
            message+=" "+commitMessageBean.getCommitMessage();
        } catch (ContextNotActiveException e) {
            log.warn("No active session found for getting commit message when adding module.json to "+module.getCode());
        }

        gitClient.commitFiles(gitRepository, Collections.singletonList(newJsonFile), message);
	
	}
	
	
}
