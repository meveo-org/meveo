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
package org.meveo.service.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.io.FileUtils;
import org.hibernate.LockOptions;
import org.hibernate.NaturalIdLoadAccess;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jboss.weld.contexts.ContextNotActiveException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.listener.CommitMessageBean;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.commons.utils.QueryBuilder.QueryLikeStyleEnum;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.admin.impl.ModuleInstallationContext;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.GitRepositoryService;
import org.meveo.service.git.MeveoRepository;

/**
 * @author phung
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @author Clément Bareth
 * @param <P> extension of Business Entity.
 */
public abstract class BusinessService<P extends BusinessEntity> extends PersistenceService<P> {
	
	@Inject
	protected EntitySerializer businessEntitySerializer;
	
	@Inject
	protected GitClient gitClient;
	
	@Inject
	protected GitRepositoryService gitRepositoryService;
	
	@Inject
	protected MeveoModuleService meveoModuleService;
	
	@Inject
	private ModuleInstallationContext installationContext;
	
	@Inject
	@MeveoRepository
	protected GitRepository meveoRepository;

	@Inject
	CommitMessageBean commitMessageBean;
	
    /**
     * Find entity by code - strict match.
     * 
     * @param code Code to match
     * @return A single entity matching code
     */
    @SuppressWarnings("unchecked")
	public P findByCode(String code) {

        if (code == null) {
            return null;
        }

//        TypedQuery<P> query = getEntityManager().createQuery("select be from " + entityClass.getName() + " be where upper(code)=:code", entityClass)
//            .setParameter("code", code.toUpperCase()).setMaxResults(1);
		return (P) getEntityManager().unwrap(Session.class).byNaturalId(entityClass.getName()).using("code", code).load();

        // if (entityClass.isAnnotationPresent(Cacheable.class)) {
        // query.setHint("org.hibernate.cacheable", true);
        // }

//        try {
//            return query.getSingleResult();
//        } catch (NoResultException e) {
//            log.debug("No {} of code {} found", getEntityClass().getSimpleName(), code);
//            return null;
//        }
    }

    /**
     * Find entity by code - strict match.
     * 
     * @param code Code to match
     * @param fetchFields Fields to fetch
     * @return A single entity matching code
     */
    public P findByCode(String code, List<String> fetchFields) {
        return findByCode(code, fetchFields, null, null, null);
    }
    
    @Transactional(value = TxType.REQUIRES_NEW)
    @TransactionAttribute(value = TransactionAttributeType.REQUIRES_NEW)
    public P findByCodeNewTx(String code, List<String> fetchFields) {
        return findByCode(code, fetchFields, null, null, null);
    }

    /**
     * Find entity by code - strict match.
     * 
     * @param code Code to match
     * @param fetchFields Fields to fetch
     * @param additionalSql Additional sql to append to the find clause
     * @param additionalParameters An array of Parameter names and values for additional sql
     * @return A single entity matching code
     */
    @SuppressWarnings("unchecked")
    protected P findByCode(String code, List<String> fetchFields, String additionalSql, Object... additionalParameters) {
        
        if (StringUtils.isBlank(code)) {
            return null;
        }
        
        QueryBuilder qb = new QueryBuilder(getEntityClass(), "be", fetchFields);
        qb.addCriterion("be.code", "=", code, true);
        if (additionalSql != null) {
            qb.addSqlCriterionMultiple(additionalSql, additionalParameters);
        }

        try {
            return (P) qb.getQuery(getEntityManager()).getSingleResult();

        } catch (NoResultException e) {
            log.debug("No {} of code {} found", getEntityClass().getSimpleName(), code);
            return null;
        } catch (NonUniqueResultException e) {
            log.error("More than one entity of type {} with code {} found. A first entry is returned.", entityClass, code);
            return (P) qb.getQuery(getEntityManager()).getResultList().get(0);
        }
    }

    /**
     * Find entity by code - match the beginning of code.
     * 
     * @param codePrefix Beginning of code
     * @return A list of entities which code starts with a given value
     */
    @SuppressWarnings("unchecked")
    public List<P> findStartsWithCode(String codePrefix) {
        try {
            QueryBuilder qb = new QueryBuilder(getEntityClass(), "be");
            qb.like("be.code", codePrefix, QueryLikeStyleEnum.MATCH_BEGINNING, false);

            return (List<P>) qb.getQuery(getEntityManager()).getResultList();
        } catch (NoResultException ne) {
            return null;
        } catch (NonUniqueResultException nre) {
            return null;
        }
    }

    public String findDuplicateCode(BusinessEntity entity) {
        return findDuplicateCode(entity, "-Copy");
    }

    public String findDuplicateCode(BusinessEntity entity, String suffix) {
        String code = entity.getCode() + suffix;
        int id = 1;
        String criteria = code;
        BusinessEntity temp = null;
        while (true) {
            temp = findByCode(criteria);
            if (temp == null) {
                break;
            }
            id++;
            criteria = code + "-" + id;
        }
        return criteria;
    }

    public BusinessEntity findByEntityClassAndCode(Class<?> clazz, String code) {
        QueryBuilder qb = new QueryBuilder(clazz, "be", null);
        qb.addCriterion("be.code", "=", code, true);

        try {
            return (BusinessEntity) qb.getQuery(getEntityManager()).getSingleResult();
        } catch (NoResultException e) {
            log.debug("No {} of code {} found", getEntityClass().getSimpleName(), code);
            return null;
        } catch (NonUniqueResultException e) {
            log.error("More than one entity of type {} with code {} found", entityClass, code);
            return null;
        }
    }

    /** 
     * Find which the module the entity belongs to 

     * @param entity for which you are looking for the module
     * @return MeveoModule, or null otherwise
     */
    @SuppressWarnings({ "rawtypes" })
	public MeveoModule findModuleOf(P entity) {
    	if (!entity.getClass().isAnnotationPresent(ModuleItem.class)) {
    		return null;
    	}
    	
    	//XXX: This part of code will break the sub-modules mechanism, but it seems it isn't used anymore
    	if (entity instanceof MeveoModule) {
    		return (MeveoModule) entity;
    	}
    	
    	String appliesTo = null;
    	if (entity instanceof CustomFieldTemplate) {
    		appliesTo = ((CustomFieldTemplate) entity).getAppliesTo();
    	} else if (entity instanceof EntityCustomAction) {
    		appliesTo = ((EntityCustomAction) entity).getAppliesTo();
    	} else if (entity instanceof CustomEntityInstance) {
    		appliesTo = ((CustomEntityInstance) entity).getCetCode();
    	}
    	
    	String code;
    	if (entity instanceof CustomEntityInstance) {
    		code = ((CustomEntityInstance) entity).getUuid();
    	} else {
    		code = entity.getCode();
    	}
    	
    	
    	MeveoModule module = null;
    	if (entity != null) {
			Session session = this.getEntityManager().unwrap(Session.class);
			String query = "SELECT mi.meveoModule "
    				+ "FROM MeveoModuleItem mi "
    				+ "	WHERE mi.itemCode = :code "
    				+ "	AND mi.itemClass = :itemClass "
    				+ (appliesTo != null ? "	AND mi.appliesTo = :appliesTo" : "");
			
    		Query<MeveoModule> q = session.createQuery(
    				query,
    				MeveoModule.class);
    		q.setParameter("code", code);
    		q.setParameter("itemClass", entity.getClass().getName());
    		if (appliesTo != null) q.setParameter("appliesTo", appliesTo);
    		module = q.getResultStream().findFirst().orElse(null);
		}
    	
    	if (module == null && !(entity instanceof CustomEntityInstance)) {
    		module = meveoModuleService.findByCode("Meveo");
    	}
    	
    	return module;
    }
    
    /**
     * Remove the entity that belongs to the module
     * 
     * @param entity belonging to the module
     * @param module corresponding to the entity
     * @throws BusinessException if the folder is not deleted
     */
    public void removeFilesFromModule(P entity, MeveoModule module) throws BusinessException {
    	if (module == null) {
    		return;
    	}
    	File gitDirectory = GitHelper.getRepositoryDir(currentUser, module.getGitRepository().getCode());
    	String path = entity.getClass().getAnnotation(ModuleItem.class).path() + "/" + entity.getCode() + ".json";
    	File directoryToRemove = new File(gitDirectory, path);
    	if (directoryToRemove.exists()) {
    		try {
    			FileUtils.forceDelete(directoryToRemove);
    	    	List<String> pattern = new ArrayList<String>();
    	    	pattern.add(GitHelper.computeRelativePath(gitDirectory, directoryToRemove));
    	    	gitClient.commit(module.getGitRepository(), pattern, "Remove directory " + directoryToRemove.getPath());
    		} catch (IOException e) {
    			throw new BusinessException("Folder unsuccessful deleted : " + directoryToRemove.getPath() + ". " + e.getMessage(), e);
    		}
    	}

    }

    @Override
    public void afterUpdateOrCreate(P entity) throws BusinessException {
    	MeveoModule module;
    	if (!installationContext.isActive()) {
    		module = findModuleOf(entity);
    	} else {
    		module = installationContext.getModule();
    	}
    	
    	if(module != null && entity.getClass().getAnnotation(ModuleItem.class) != null) {
    		addFilesToModule(entity,  module);
    	} else if (module != null) {
    		log.info("Ignore module file, as this entity is not a moduleItem " + entity.getClass());
    	}
	}

    public void addFilesToModule(P entity, MeveoModule module) throws BusinessException {
    	BaseEntityDto businessEntityDto = getDto(entity);
    	String businessEntityDtoSerialize = JacksonUtil.toStringPrettyPrinted(businessEntityDto);
    	
    	File gitDirectory = GitHelper.getRepositoryDir(currentUser, module.getCode());
    	String path;
		try {
			path = entity.getClass().getAnnotation(ModuleItem.class).path() + "/";
		} catch (Exception e1) {
			throw new BusinessException("failed to build path of entity {} ({}) of module {}", e1, entity.getCode(), entity.getClass(), module.getCode());
		}
    	
    	File newDir = new File (gitDirectory, path);
    	newDir.mkdirs();
    	
    	File newJsonFile = new File(gitDirectory, path+"/"+entity.getCode()+".json");
    	try {
	    	MeveoFileUtils.writeAndPreserveCharset(businessEntityDtoSerialize, newJsonFile);
    	} catch (IOException e) {
    		throw new BusinessException("File cannot be updated or created", e);
    	}
    	
    	GitRepository gitRepository = gitRepositoryService.findByCode(module.getCode());
	String message = "Add JSON file for entity " + entity.getCode();
        try {
            message+=" "+commitMessageBean.getCommitMessage();
        } catch (ContextNotActiveException e) {
            log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
        }
	gitClient.commitFiles(gitRepository, Collections.singletonList(newDir), message);
    }
    
    /**
     * Retrieve the current module of the entity, ignore if none
     * Remove the files from old Module
     * Add the files to the new module
     * 
     * @param entity belonging to the module
     * @param module corresponding to the entity
     * @throws BusinessException 
     * @throws IOException BusinessException
     */
    public void moveFilesToModule(P entity, MeveoModule oldModule, MeveoModule newModule) throws BusinessException, IOException {
		removeFilesFromModule(entity, oldModule);
	    addFilesToModule(entity, newModule);
    }
    
    public void onAddToModule(P entity, MeveoModule module) throws BusinessException {
    	
    }
    
    // ------------------------------- Methods that retrieves lazy loaded objects ----------------------------------- //

    public P findByCodeLazy(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        
		NaturalIdLoadAccess<P> query = getEntityManager().
				unwrap(org.hibernate.Session.class)
				.byNaturalId(getEntityClass())
				.with(LockOptions.READ)
				.using("code", code);
		
		return query.getReference();
    }
    
    protected BaseEntityDto getDto(P entity) throws BusinessException {
    	return businessEntitySerializer.serialize(entity);
    }

}
