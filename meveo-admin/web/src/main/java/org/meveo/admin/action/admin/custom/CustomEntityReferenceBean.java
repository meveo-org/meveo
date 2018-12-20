package org.meveo.admin.action.admin.custom;

import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityReference;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.custom.CustomEntityReferenceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hien Bach
 */
@Named
@ViewScoped
public class CustomEntityReferenceBean extends BaseBean<CustomEntityReference> {

    private List<CustomEntityTemplate> customEntityTemplates;

    @Inject
    private CustomEntityReferenceService referenceService;

    public CustomEntityReferenceBean() {
        super(CustomEntityReference.class);
    }

    @Override
    protected IPersistenceService<CustomEntityReference> getPersistenceService() {
        return referenceService;
    }

    @PostConstruct
    public void init(){
        customEntityTemplates = referenceService.getCETFromReference();
    }

    @Override
    public String getEditViewName() {
        return "menuConfigurationDetail";
    }

    @Override
    protected String getListViewName() {
        return "menuConfigurations";
    }

    @Override
    public String getNewViewName() {
        return "menuConfigurationDetail";
    }

    public List<CustomEntityTemplate> getCustomEntityTemplates() {
        return customEntityTemplates;
    }

    public void setCustomEntityTemplates(List<CustomEntityTemplate> customEntityTemplates) {
        this.customEntityTemplates = customEntityTemplates;
    }

   /* public String save() throws BusinessException {
        referenceService.
    }*/
}
