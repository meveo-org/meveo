package org.meveo.service.custom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import org.jboss.weld.contexts.ContextNotActiveException;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.listener.CommitMessageBean;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.service.base.BusinessService;
import org.meveo.service.crm.impl.CustomFieldException;
import org.meveo.service.crm.impl.CustomFieldTemplateUtils;
import org.meveo.service.git.GitHelper;

@Stateless
public class EntityCustomActionService extends BusinessService<EntityCustomAction> {

    @Inject
    CommitMessageBean commitMessageBean;

    /**
     * Find a list of entity actions/scripts corresponding to a given entity
     *
     * @param entity Entity that entity actions/scripts apply to

     * @return A map of entity actions/scripts mapped by a action code
     */
    public Map<String, EntityCustomAction> findByAppliesTo(ICustomFieldEntity entity) {
        try {
            return findByAppliesTo(CustomFieldTemplateUtils.calculateAppliesToValue(entity));

        } catch (CustomFieldException e) {
            // Its ok, handles cases when value that is part of CFT.AppliesTo calculation is not set yet on entity
            return new HashMap<String, EntityCustomAction>();
        }
    }

    /**
     * Find a list of entity actions/scripts corresponding to a given entity
     *
     * @param appliesTo Entity (CFT appliesTo code) that entity actions/scripts apply to

     * @return A map of entity actions/scripts mapped by a action code
     */
    @SuppressWarnings("unchecked")
    public Map<String, EntityCustomAction> findByAppliesTo(String appliesTo) {

        QueryBuilder qb = new QueryBuilder(EntityCustomAction.class, "s", null);
        qb.addCriterion("s.appliesTo", "=", appliesTo, true);

        List<EntityCustomAction> actions = (List<EntityCustomAction>) qb.getQuery(getEntityManager()).getResultList();

        Map<String, EntityCustomAction> actionMap = new HashMap<String, EntityCustomAction>();
        for (EntityCustomAction action : actions) {
            actionMap.put(action.getCode(), action);
        }
        return actionMap;
    }

    /**
     * Find a specific entity action/script by a code
     *
     * @param code Entity action/script code. MUST be in a format of &lt;localCode&gt;|&lt;appliesTo&gt;
     * @param entity Entity that entity actions/scripts apply to

     * @return Entity action/script
     * @throws CustomFieldException An exception when AppliesTo value can not be calculated
     */
    public EntityCustomAction findByCodeAndAppliesTo(String code, ICustomFieldEntity entity) throws CustomFieldException {
        return findByCodeAndAppliesTo(code, CustomFieldTemplateUtils.calculateAppliesToValue(entity));
    }

    /**
     * Find a specific entity action/script by a code
     *
     * @param code Entity action/script code. MUST be in a format of &lt;localCode&gt;|&lt;appliesTo&gt;
     * @param appliesTo Entity (CFT appliesTo code) that entity actions/scripts apply to

     * @return Entity action/script
     */
    public EntityCustomAction findByCodeAndAppliesTo(String code, String appliesTo) {

        QueryBuilder qb = new QueryBuilder(EntityCustomAction.class, "s", null);
        qb.addCriterion("s.code", "=", code, true);
        qb.addCriterion("s.appliesTo", "=", appliesTo, true);
        try {
            return (EntityCustomAction) qb.getQuery(getEntityManager()).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public void addFilesToModule(EntityCustomAction entity, MeveoModule module) throws BusinessException {
        BaseEntityDto businessEntityDto = businessEntitySerializer.serialize(entity);
        String businessEntityDtoSerialize = JacksonUtil.toString(businessEntityDto);

        File gitDirectory = GitHelper.getRepositoryDir(currentUser, module.getCode());
        String cetCode = CustomEntityTemplate.getCodeFromAppliesTo(entity.getAppliesTo());
        if(cetCode == null) {
            cetCode = CustomRelationshipTemplate.getCodeFromAppliesTo(entity.getAppliesTo());
        }

        String path = entity.getClass().getAnnotation(ModuleItem.class).path() + "/" + cetCode;

        File newDir = new File (gitDirectory, path);
        newDir.mkdirs();

        File newJsonFile = new File(newDir, entity.getCode() +".json");
        try {
            MeveoFileUtils.writeAndPreserveCharset(businessEntityDtoSerialize, newJsonFile);
        } catch (IOException e) {
            throw new BusinessException("File cannot be updated or created", e);
        }

        GitRepository gitRepository = gitRepositoryService.findByCode(module.getCode());
        String message = "Add JSON file for custom action " + cetCode + "." + entity.getCode();
        try {
            message+=" "+commitMessageBean.getCommitMessage();
        } catch (ContextNotActiveException e) {
            log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
        }
        gitClient.commitFiles(gitRepository, Collections.singletonList(newDir), message);
    }
}