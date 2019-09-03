package org.meveo.admin.action.admin.custom;

import org.meveo.admin.action.BaseBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.elresolver.ELException;
import org.meveo.model.BusinessEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityCategoryService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomizedEntityService;
import org.meveo.service.custom.EntityCustomActionService;
import org.meveo.service.job.JobInstanceService;
import org.primefaces.model.TreeNode;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author Clément Bareth
 */
@Named
@ViewScoped
public abstract class BackingCustomBean <T extends BusinessEntity> extends BaseBean<T> {

    private static final long serialVersionUID = 1187554162639618526L;

    @Inject
    protected CustomFieldTemplateService customFieldTemplateService;

    @Inject
    protected CustomEntityTemplateService customEntityTemplateService;
    
    @Inject
    protected CustomEntityCategoryService customEntityCategoryService;

    @Inject
    protected EntityCustomActionService entityActionScriptService;

    @Inject
    protected JobInstanceService jobInstanceService;

    @Inject
    protected CustomizedEntityService customizedEntityService;

    /**
     * Class corresponding to a entityClassName value of CustomRelationshipTemplate class if null
     */
    protected String entityClassName;

    /**
     * Request parameter
     */
    protected String appliesTo;

    /**
     * Class corresponding to a entityClassName value or CustomEntityTemplate class if null
     */
    protected Class entityClass;

    protected TreeNode selectedFieldGrouping;

    /**
     * Remember tree nodes before refreshing them - that way tabs and fieldgroups are not lost after refresh if they had no children
     */
    protected List<TreeNode> cachedTreeNodes;

    protected List<EntityCustomAction> entityActions;

    protected BackingCustomBean(Class<T> clazz){
        super(clazz);
    }

    protected TreeNode getChildNodeByValue(TreeNode parentTreeNode, String value) {
        for (TreeNode childNode : parentTreeNode.getChildren()) {
            if (childNode.getData().equals(value)) {
                return childNode;
            }
        }
        return null;
    }

    @Override
    @ActionMethod
    public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {
        super.saveOrUpdate(killConversation);
        return getEditViewName();
    }

    public void removeField(TreeNode currentNode) {
        currentNode.getParent().getChildren().remove(currentNode);
        refreshFields();
    }

    public void removeFieldGrouping() {
        try {
            for (TreeNode childNode : selectedFieldGrouping.getChildren()) {
                if (childNode.getType().equals(CustomFieldTemplate.GroupedCustomFieldTreeItemType.field.name())) {
                    customFieldTemplateService.remove(((CustomFieldTemplate) childNode.getData()).getId());
                } else if (childNode.getType().equals(CustomFieldTemplate.GroupedCustomFieldTreeItemType.fieldGroup.name())) {
                    for (TreeNode childChildNode : childNode.getChildren()) {
                        customFieldTemplateService.remove(((CustomFieldTemplate) childChildNode.getData()).getId());
                    }
                }
            }
            selectedFieldGrouping.getParent().getChildren().remove(selectedFieldGrouping);
        } catch (BusinessException e) {
            log.error("Failed to remove field grouping", e);
        }
        refreshFields();
    }

    public abstract void refreshFields();

    public abstract void newTab();

    public abstract void newFieldGroup(TreeNode parentNode);

    public abstract void saveUpdateFieldGrouping();

    public abstract void cancelFieldGrouping();

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        if (entityClassName != null) {
            entityClassName = ReflectionUtils.getCleanClassName(entityClassName);
        }
        this.entityClassName = entityClassName;
    }

    public String getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

}
