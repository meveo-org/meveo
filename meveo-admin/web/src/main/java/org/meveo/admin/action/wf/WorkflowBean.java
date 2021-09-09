/*
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
package org.meveo.admin.action.wf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.action.admin.ViewBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.elresolver.ELException;
import org.meveo.exceptions.EntityAlreadyExistsException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.scripts.FunctionIO;
import org.meveo.model.wf.WFAction;
import org.meveo.model.wf.WFTransition;
import org.meveo.model.wf.Workflow;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.wf.WFActionService;
import org.meveo.service.wf.WFTransitionService;
import org.meveo.service.wf.WorkflowService;
import org.omnifaces.cdi.Param;

/**
 * Standard backing bean for {@link Workflow} (extends {@link BaseBean} that provides almost all common methods to handle entities filtering/sorting in datatable, their create,
 * edit, view, delete operations). It works with Manaty custom JSF components .
 * 
 * @author Wassim Drira
 * @lastModifiedVersion 5.0
 * 
 */
@Named
@ViewScoped
@ViewBean
public class WorkflowBean extends BaseBean<Workflow> {

    private static final long serialVersionUID = 1L;
    private static final String LESS_SEPARATOR = " < ";
    private static final String LESS_SEPARATOR_NO_SPACE_LEFT = "< ";
    private static final String LESS_SEPARATOR_NO_SPACE_RIGHT = " <";

    /**
     * Injected @{link Workflow} service. Extends {@link PersistenceService}.
     */
    @Inject
    private WorkflowService workflowService;

    @Inject
    private WFTransitionService wFTransitionService;

    @Inject
    private WFActionService wfActionService;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;
    
    @Inject
    private ScriptInstanceService scriptService;

    private List<WFAction> wfActions = new ArrayList<>();

    private boolean showDetailPage = false;

    private String oldCetCode = null;

    private String oldWFType = null;

    // @Produces
    // @Named
    private transient WFTransition wfTransition = new WFTransition();

    @Inject
    @Param
    private String isDunning;

    /**
     * Constructor. Invokes super constructor and provides class type of this bean for {@link BaseBean}.
     */
    public WorkflowBean() {
        super(Workflow.class);
    }

    @Override
    public Workflow initEntity() {
        super.initEntity();
        if (entity != null) {
            oldCetCode = entity.getCetCode();
            oldWFType = entity.getWfType();
        }
        // PersistenceUtils.initializeAndUnproxy(entity.getActions());
        return entity;
    }

    public WFTransition getWfTransition() {
        return wfTransition;
    }

    public void setWfTransition(WFTransition wfTransition) {
        this.wfTransition = wfTransition;
    }

    public String getOldCetCode() {
        return oldCetCode;
    }

    public void setOldCetCode(String oldCetCode) {
        this.oldCetCode = oldCetCode;
    }

    public String getOldWFType() {
        return oldWFType;
    }

    public void setOldWFType(String oldWFType) {
        this.oldWFType = oldWFType;
    }

    public void cancelTransitionDetail() {
        this.wfTransition = new WFTransition();
        showDetailPage = false;
    }

    @Override
    @ActionMethod
    public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {
        if ((oldCetCode != null && !entity.getCetCode().equals(oldCetCode)) || (oldWFType != null && !entity.getWfType().equals(oldWFType))) {
            Workflow workflow = workflowService.findByCetCodeAndWFType(entity.getCetCode(), entity.getWfType());
            if (workflow != null) {
                messages.error(new BundleKey("messages", "workflow.uniqueField.error"));
                return null;
            }
            List<WFTransition> wfTransitions = entity.getTransitions();
            if (CollectionUtils.isNotEmpty(wfTransitions)) {
                for (WFTransition wfTransition: wfTransitions) {
                    if (wfTransition != null) {
                        wfTransition = wFTransitionService.findById(wfTransition.getId(), Arrays.asList("wfDecisionRules", "wfActions"));
                        if (CollectionUtils.isNotEmpty(wfTransition.getWfActions())) {
                            for (WFAction wfAction : wfTransition.getWfActions()) {
                                wfActionService.remove(wfAction);
                            }
                        }
                    }
                }
                for (WFTransition wfTransition: wfTransitions) {
                    wFTransitionService.remove(wfTransition);
                }
            }
            entity.setTransitions(new ArrayList<>());
        }
        try {
            String message = entity.isTransient() ? "save.successful" : "update.successful";
            saveOrUpdate(entity);
            messages.info(new BundleKey("messages", message));
            if (killConversation) {
                endConversation();
            }
        } catch (EntityAlreadyExistsException e) {
            messages.error(new BundleKey("messages", "workflow.uniqueField.error"));
            return null;
        }
        return "workflowDetail";
    }

    public void saveWfTransition() throws BusinessException {

        if (wfTransition.getId() != null) {
            WFTransition wfTrs = wFTransitionService.findById(wfTransition.getId(), Arrays.asList("wfDecisionRules", "wfActions"));
            wfTrs.setFromStatus(wfTransition.getFromStatus());
            wfTrs.setToStatus(wfTransition.getToStatus());
            wfTrs.setConditionEl(wfTransition.getConditionEl());
            wfTrs.setDescription(wfTransition.getDescription());

            wFTransitionService.update(wfTrs);

            addOrUpdateOrDeleteActions(wfTrs, wfActions, true);

            entity.getTransitions().remove(wfTransition);
            entity.getTransitions().add(wfTrs);

            messages.info(new BundleKey("messages", "update.successful"));
        } else {

            wfTransition.setWorkflow(entity);
            wFTransitionService.create(wfTransition);

            addOrUpdateOrDeleteActions(wfTransition, wfActions, false);

            entity.getTransitions().add(wfTransition);
            messages.info(new BundleKey("messages", "save.successful"));
        }

        wfActions.clear();
        showDetailPage = false;
        wfTransition = new WFTransition();
    }

    @ActionMethod
    public void deleteWfTransition(WFTransition transitionToDelete) {
        try {
            wFTransitionService.remove(transitionToDelete.getId());
            entity = workflowService.refreshOrRetrieve(entity);
            wfActions.clear();
            showDetailPage = false;
            messages.info(new BundleKey("messages", "delete.successful"));

        } catch (Exception e) {
            log.info("Failed to delete!", e);
            messages.error(new BundleKey("messages", "error.delete.unexpected"));
        }
    }

    @ActionMethod
    public void duplicateWfTransition(WFTransition wfTransition) {
        try {
            this.wfTransition = wFTransitionService.duplicate(wfTransition, entity);

            // Set max priority +1
            int priority = 1;
            if (entity.getTransitions().size() > 0) {
                for (WFTransition wfTransitionInList : entity.getTransitions()) {
                    if (WfTransitionBean.CATCH_ALL_PRIORITY != wfTransitionInList.getPriority() && priority <= wfTransitionInList.getPriority()) {
                        priority = wfTransitionInList.getPriority() + 1;
                    }
                }
            }
            this.wfTransition.setPriority(priority);
            editWfTransition(this.wfTransition);

        } catch (Exception e) {
            log.error("Failed to duplicate WF transition!", e);
            messages.error(new BundleKey("messages", "error.duplicate.unexpected"));
        }
    }

    @ActionMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void editWfTransition(WFTransition transitionToEdit) {
        this.wfTransition = transitionToEdit;
        WFTransition wfTransition1 = wFTransitionService.findById(this.wfTransition.getId(), Arrays.asList("wfDecisionRules", "wfActions"));
        if (wfTransition1 != null && wfTransition1.getWfActions() != null) {
            wfActions.clear();
            wfActions.addAll(wfTransition1.getWfActions());
        }
        showDetailPage = true;
    }

    /**
     * Autocomplete method for class filter field - search entity type classes with @ObservableEntity annotation
     * 
     * @param query A partial class name (including a package)
     * @return A list of classnames
     */
    public List<String> autocompleteClassNames(String query) {
        List<Class<?>> allWFType = workflowService.getAllWFTypes();
        List<String> classNames = new ArrayList<String>();
        for (Class<?> clazz : allWFType) {
            if (!"org.meveo.service.script.wf.WFTypeScript".equals(clazz.getName())) {
                if (StringUtils.isBlank(query)) {
                    classNames.add(clazz.getName());
                } else if (clazz.getName().toLowerCase().contains(query.toLowerCase())) {
                    classNames.add(clazz.getName());
                }
            }
        }
        Collections.sort(classNames);
        return classNames;
    }

    public List<String> autocompleteCETName(String query) {
        List<CustomEntityTemplate> allCET = customEntityTemplateService.list();
        List<String> cetNames = new ArrayList<String>();
        for (CustomEntityTemplate customEntityTemplate: allCET) {
           cetNames.add(customEntityTemplate.getCode());
        }
        Collections.sort(cetNames);
        return cetNames;
    }

    public List<String> autocompleteCFTName(String query) {
        Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo("CE_" + entity.getCetCode());
        List<String> cftNames = new ArrayList<String>();
        if (cfts != null && CollectionUtils.isNotEmpty(cfts.values()))
        for (CustomFieldTemplate cft: cfts.values()) {
            if (cft.getFieldType() == CustomFieldTypeEnum.LIST && cft.getStorageType() == CustomFieldStorageTypeEnum.SINGLE && cft.getListValues() != null) {
                cftNames.add(cft.getCode());
            }
        }
        Collections.sort(cftNames);
        return cftNames;
    }

    /**
     * @see org.meveo.admin.action.BaseBean#getPersistenceService()
     */
    @Override
    protected IPersistenceService<Workflow> getPersistenceService() {
        return workflowService;
    }

    @Override
    protected List<String> getFormFieldsToFetch() {
        return Arrays.asList("transitions");
    }

    @SuppressWarnings({ "unchecked" })
    public Map<String, String> getTransitionStatusFromWorkflowType() {
        Map<String, String> statusMap = new TreeMap<>();
        if (entity.getCetCode() != null && entity.getWfType() != null) {
            CustomFieldTemplate customFieldTemplate = customFieldTemplateService.findByCodeAndAppliesTo(entity.getWfType(), "CE_" + entity.getCetCode());
            Map<String, String> listValue = customFieldTemplate.getListValues();
            statusMap.putAll(listValue);
        }
        return statusMap;
    }

    public void addNewAction() {
        WFAction newInstance = new WFAction();
        if (CollectionUtils.isNotEmpty(wfActions)) {
            WFAction lastAction = wfActions.get(wfActions.size() - 1);
            newInstance.setPriority(lastAction.getPriority() + 1);
        } else {
            newInstance.setPriority(1);
        }
        wfActions.add(newInstance);
    }

    public void deleteWfAction(int indexAction) {
        if (wfActions.size() > indexAction && wfActions.get(indexAction) != null) {
            wfActions.remove(indexAction);
        }
    }

    public boolean isShowDetailPage() {
        return showDetailPage;
    }

    public void setShowDetailPage(boolean showDetailPage) {
        this.showDetailPage = showDetailPage;
    }

    public void newTransition() {
        showDetailPage = true;
        wfActions.clear();
        List<WFTransition> wfTransitionList = entity.getTransitions();
        if (CollectionUtils.isNotEmpty(wfTransitionList)) {
            WFTransition lastWFTransition = wfTransitionList.get(wfTransitionList.size() - 1);
            wfTransition.setPriority(lastWFTransition.getPriority() + 1);
        } else {
            wfTransition.setPriority(1);
        }
    }

    public List<WFAction> getWfActions() {
        return wfActions;
    }

    public void setWfActions(List<WFAction> wfActions) {
        this.wfActions = wfActions;
    }

    private void addOrUpdateOrDeleteActions(WFTransition wfTransition, List<WFAction> wfActionList, boolean isUpdate) throws BusinessException {

        List<WFAction> updatedActions = new ArrayList<>();
        List<WFAction> newActions = new ArrayList<>();
        for (WFAction wfAction : wfActionList) {
            if (wfAction.getId() != null) {
                updatedActions.add(wfAction);
            } else {
                newActions.add(wfAction);
            }
        }

        if (isUpdate && this.wfTransition != null) {
            WFTransition currentTransition = wFTransitionService.findById(this.wfTransition.getId(), Arrays.asList("wfActions"));
            List<WFAction> deletedActions = currentTransition.getWfActions();
            if (CollectionUtils.isNotEmpty(deletedActions)) {
                deletedActions.removeAll(updatedActions);
            }
            for (WFAction wfAction : deletedActions) {
                wfActionService.remove(wfAction);
            }

            if (CollectionUtils.isNotEmpty(wfTransition.getWfActions())) {
                wfTransition.getWfActions().clear();
            }
            for (WFAction wfAction : updatedActions) {
                wfActionService.update(wfAction);
                wfTransition.getWfActions().add(wfAction);
            }
        }
        for (WFAction wfAction : newActions) {
            wfAction.setWfTransition(wfTransition);
            wfActionService.create(wfAction);
            wfTransition.getWfActions().add(wfAction);
        }

        updatedActions.clear();
        newActions.clear();
    }

    public void moveUpTransition(WFTransition selectedWfTransition) throws BusinessException {
        cancelTransitionDetail();
        int index = entity.getTransitions().indexOf(selectedWfTransition);
        if (index > 0) {
            WFTransition upWfTransition = entity.getTransitions().get(index);
            int priorityUp = upWfTransition.getPriority();
            WFTransition downWfTransition = entity.getTransitions().get(index - 1);
            WFTransition needUpdate = wFTransitionService.refreshOrRetrieve(upWfTransition);
            needUpdate.setPriority(downWfTransition.getPriority());
            wFTransitionService.update(needUpdate);
            needUpdate = wFTransitionService.refreshOrRetrieve(downWfTransition);
            needUpdate.setPriority(priorityUp);
            wFTransitionService.update(needUpdate);
            entity.getTransitions().get(index).setPriority(downWfTransition.getPriority());
            entity.getTransitions().get(index - 1).setPriority(priorityUp);
            Collections.swap(entity.getTransitions(), index, index - 1);
            messages.info(new BundleKey("messages", "update.successful"));
        }
    }

    public void moveDownTransition(WFTransition selectedWfTransition) throws BusinessException {
        cancelTransitionDetail();
        int index = entity.getTransitions().indexOf(selectedWfTransition);
        if (index < entity.getTransitions().size() - 1) {
            WFTransition upWfTransition = entity.getTransitions().get(index);
            int priorityUp = upWfTransition.getPriority();
            WFTransition downWfTransition = entity.getTransitions().get(index + 1);
            WFTransition needUpdate = wFTransitionService.findById(upWfTransition.getId(), true);
            needUpdate.setPriority(downWfTransition.getPriority());
            wFTransitionService.update(needUpdate);
            needUpdate = wFTransitionService.findById(downWfTransition.getId(), true);
            needUpdate.setPriority(priorityUp);
            wFTransitionService.update(needUpdate);
            entity.getTransitions().get(index).setPriority(downWfTransition.getPriority());
            entity.getTransitions().get(index + 1).setPriority(priorityUp);
            Collections.swap(entity.getTransitions(), index, index + 1);
            messages.info(new BundleKey("messages", "update.successful"));
        }
    }

    public void moveUpAction(int index) {
        if (index > 0) {
            WFAction upWfAction = wfActions.get(index);
            int priorityUp = upWfAction.getPriority();
            WFAction downWfAction = wfActions.get(index - 1);
            wfActions.get(index).setPriority(downWfAction.getPriority());
            wfActions.get(index - 1).setPriority(priorityUp);
            Collections.swap(wfActions, index, index - 1);
        }
    }

    public void moveDownAction(int index) {
        if (index < wfActions.size() - 1) {
            WFAction upWfAction = wfActions.get(index);
            int priorityUp = upWfAction.getPriority();
            WFAction downWfAction = wfActions.get(index + 1);
            wfActions.get(index).setPriority(downWfAction.getPriority());
            wfActions.get(index + 1).setPriority(priorityUp);
            Collections.swap(wfActions, index, index + 1);
        }
    }

    @ActionMethod
    public void duplicate() {
        if (entity != null && entity.getId() != null) {
            try {
                workflowService.duplicate(entity);
                messages.info(new BundleKey("messages", "duplicate.successfull"));
            } catch (BusinessException e) {
                log.error("Error encountered duplicating {} entity: {}", new Object[] { entity.getClass().getSimpleName(), entity.getCode(), e });
                messages.error(new BundleKey("messages", "error.duplicate.unexpected"));
            }
        }
    }

    @Override
    public Map<String, Object> getFilters() {
        if (filters == null) {
            filters = new HashMap<String, Object>();
        }
        if (isDunning != null && "true".equals(isDunning)) {
            filters.put("wfType", "org.meveo.admin.wf.types.DunningWF");
        }
        return filters;
    }    

    public void resetValueForWFType() {
        if (oldCetCode != null && !entity.getCetCode().equals(oldCetCode)) {
            entity.setWfType(null);
        }
    }
}