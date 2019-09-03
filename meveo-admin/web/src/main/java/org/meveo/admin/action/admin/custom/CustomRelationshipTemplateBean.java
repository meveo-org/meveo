package org.meveo.admin.action.admin.custom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.exception.BusinessException;
import org.meveo.elresolver.ELException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.CustomFieldTemplate.GroupedCustomFieldTreeItemType;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.custom.CustomizedEntity;
import org.meveo.service.job.Job;
import org.meveo.util.EntityCustomizationUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DualListModel;
import org.primefaces.model.TreeNode;

@Named
@ViewScoped
public class CustomRelationshipTemplateBean extends BackingCustomBean<CustomRelationshipTemplate> {

    private static final long serialVersionUID = 1187554162639618526L;

    /**
     * Prefix to apply to custom field templates (appliesTo value)
     */
    private String crtPrefix;

    private CustomizedEntity customizedEntity;

    private SortedTreeNode groupedFields;

    private List<CustomEntityTemplate> customEntityTemplates;
    
    private List<CustomRelationshipTemplate> customRelationshipTemplates;

    protected TreeNode selectedFieldGrouping;

    private TranslatableLabel selectedFieldGroupingLabel = new TranslatableLabel();

    private DualListModel<DBStorageType> availableStoragesDM;

    private EntityCustomAction selectedEntityAction;

    private boolean displayNeo4j;

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    public CustomRelationshipTemplateBean() {
        super(CustomRelationshipTemplate.class);
        entityClass = CustomRelationshipTemplate.class;
    }

    @PostConstruct
    public void init(){
        customRelationshipTemplates = customRelationshipTemplateService.list();
        customEntityTemplates = customEntityTemplateService.list();
    }

    @Override
    public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {
        String returnView =  super.saveOrUpdate(killConversation);
        customRelationshipTemplateService.synchronizeStorages(getEntity());
        return returnView;
    }

    public void onChangeAvailableStorages() {
        displayNeo4j = false;
        if (CollectionUtils.isNotEmpty(getEntity().getAvailableStorages())) {
            getEntity().getAvailableStorages().clear();
            getEntity().getAvailableStorages().addAll(availableStoragesDM.getTarget());
        } else {
            getEntity().setAvailableStorages(availableStoragesDM.getTarget());
        }
    }

    public boolean isDisplayNeo4j() {
        if (CollectionUtils.isNotEmpty(getEntity().getAvailableStorages())) {
            if (getEntity().getAvailableStorages().contains(DBStorageType.NEO4J)) {
                return true;
            }
        }
        return false;
    }

    public void setDisplayNeo4j(boolean displayNeo4j) {
        this.displayNeo4j = displayNeo4j;
    }

    public List<CustomEntityTemplate> getCustomEntityTemplates() {
        return customEntityTemplates;
    }

    public List<CustomRelationshipTemplate> getCustomRelationshipTemplates() {
    	return customRelationshipTemplates;
	}

	@Override
    protected CustomRelationshipTemplateService getPersistenceService() {
        return customRelationshipTemplateService;
    }

    public boolean isCustomRelationshipTemplate() {
        return entityClassName == null || CustomRelationshipTemplate.class.getName().equals(entityClassName);
    }

    /**
     * Prepare to show entity customization for a particular class - To be used from
     * GUI action button/link
     *
     * @param entityClassName
     *
     */
    public void initCustomization(String entityClassName) {
        customizedEntity = null;
        this.entityClassName = entityClassName;
        try {
            getCustomizedEntity();
        } catch (ClassNotFoundException e) {
            log.error("Failed to initialize entity customization for a class {}", entityClassName);
        }
    }

    /**
     * Construct customizedEntity instance which is a representation of customizable
     * class (e.g. Customer)
     *
     * @return
     * @throws ClassNotFoundException
     */
    public CustomizedEntity getCustomizedEntity() throws ClassNotFoundException {

        // Convert appliesTo parameter to a entityClassName of objectId value in case of
        // customEntity template
        if (customizedEntity == null && appliesTo != null) {

            CustomizedEntity customizedEntityMatched = customizedEntityService.getCustomizedEntity(appliesTo);
            if (customizedEntityMatched != null) {
                setEntityClassName(customizedEntityMatched.getEntityClass().getName());
                if (customizedEntityMatched.getCustomEntityId() != null) {
                    initEntity(customizedEntityMatched.getCustomEntityId());
                }
            }
        }

        if (customizedEntity == null && entityClassName != null && !CustomEntityTemplate.class.getName().equals(entityClassName)) {
            entityClass = Class.forName(entityClassName);
            customizedEntity = new CustomizedEntity(entityClass.getSimpleName(), entityClass, null, null);
            crtPrefix = EntityCustomizationUtils.getAppliesTo(entityClass, null);

            if (Job.class.isAssignableFrom(entityClass)) {

                // Check and instantiate missing custom field templates for a given job
                Job job = jobInstanceService.getJobByName(entityClass.getSimpleName());
                Map<String, CustomFieldTemplate> jobCustomFields = job.getCustomFields();

                // Create missing custom field templates if needed
                try {
                    customFieldTemplateService.createMissingTemplates(crtPrefix, jobCustomFields.values());

                } catch (BusinessException e) {
                    log.error("Failed to construct customized entity", e);
                    messages.error(new BundleKey("messages", "error.unexpected"));
                }
            }
        }

        return customizedEntity;
    }

    public TreeNode getFields() {
        if (groupedFields != null || crtPrefix == null) {
            return groupedFields;
        }

        Map<String, CustomFieldTemplate> fields = customFieldTemplateService.findByAppliesToNoCache(crtPrefix);

        GroupedCustomField groupedCFT = new GroupedCustomField(fields.values(), CustomRelationshipTemplate.class.isAssignableFrom(entityClass) ? entity.getName() : "Custom fields", true);
 
        groupedFields = new SortedTreeNode(groupedCFT.getType(), groupedCFT.getData(), null, true);
        groupedFields.setExpanded(true);

        // Create through tabs
        for (GroupedCustomField level1 : groupedCFT.getChildren()) { 
            SortedTreeNode level1Node = new SortedTreeNode(level1.getType(), level1.getData(), groupedFields, level1.getType() == GroupedCustomFieldTreeItemType.tab);
            level1Node.setExpanded(true);

            // Create fields of field groups
            for (GroupedCustomField level2 : level1.getChildren()) { 
                SortedTreeNode level2Node = new SortedTreeNode(level2.getType(), level2.getData(), level1Node, level2.getType() == GroupedCustomFieldTreeItemType.fieldGroup);
                if (level2.getType().equals(GroupedCustomFieldTreeItemType.fieldGroup)) {
                    level2Node.setExpanded(true);
                }
                // Create fields
                for (GroupedCustomField level3 : level2.getChildren()) {
                    new SortedTreeNode(level3.getType(), level3.getData(), level2Node,null);
                }
            }
        }
        if (cachedTreeNodes != null && !cachedTreeNodes.isEmpty()) {
            for (TreeNode tabNode : cachedTreeNodes) {// tab
                TreeNode existedTab = getChildNodeByValue(groupedFields, tabNode.getData().toString());
                if (existedTab == null) {// check tab
                    existedTab = new SortedTreeNode(GroupedCustomFieldTreeItemType.tab, tabNode.getData(), groupedFields, true);
                    existedTab.setExpanded(true);
                }
                for (TreeNode fieldGroupNode : tabNode.getChildren()) {// field groups of tab
                    TreeNode existedFieldGroup = getChildNodeByValue(existedTab, fieldGroupNode.getData().toString());
                    if (existedFieldGroup == null) {
                        existedFieldGroup = new SortedTreeNode(GroupedCustomFieldTreeItemType.fieldGroup, fieldGroupNode.getData(), existedTab, null);
                    }
                }
            }
        }

        return groupedFields;
    }

    public List<EntityCustomAction> getEntityActions() {

        if (entityActions != null || crtPrefix == null) {
            return entityActions;
        }

        Map<String, EntityCustomAction> scripts = entityActionScriptService.findByAppliesTo(crtPrefix);

        entityActions = new ArrayList<EntityCustomAction>();
        entityActions.addAll(scripts.values());

        return entityActions;
    }

    @Override
    public void refreshFields() {
        if (groupedFields != null) {
            cachedTreeNodes = new ArrayList<TreeNode>();
            for (TreeNode tabNode : groupedFields.getChildren()) {
                SortedTreeNode tab = new SortedTreeNode(GroupedCustomFieldTreeItemType.tab, tabNode.getData(), null, null);
                cachedTreeNodes.add(tab);
                if (tabNode.getChildCount() != 0) {
                    for (TreeNode fieldGroupNode : tabNode.getChildren()) {// fieldgroup
                        SortedTreeNode fieldGroup = (SortedTreeNode) fieldGroupNode;
                        if (fieldGroup.getType().equals(GroupedCustomFieldTreeItemType.fieldGroup) && fieldGroupNode.getChildCount() == 0) {
                            new SortedTreeNode(GroupedCustomFieldTreeItemType.fieldGroup, fieldGroupNode.getData(), tab, null);
                        }
                    }
                }
            }
        }
        groupedFields = null;
    }

    public void editFieldGrouping(TreeNode selectedFieldGrouping) {
        setSelectedFieldGrouping(selectedFieldGrouping);
        // this.selectedFieldGroupingLabel = (TranslatableLabel)
        // selectedFieldGrouping.getData();
    }

    public void setSelectedFieldGrouping(TreeNode selectedFieldGrouping) {
        this.selectedFieldGrouping = selectedFieldGrouping;
        this.selectedFieldGroupingLabel = (TranslatableLabel) selectedFieldGrouping.getData();
    }

    public TreeNode getSelectedFieldGrouping() {
        return selectedFieldGrouping;
    }

    public TranslatableLabel getSelectedFieldGroupingLabel() {
        return selectedFieldGroupingLabel;
    }

    public void setSelectedFieldGroupingLabel(TranslatableLabel selectedFieldGroupingLabel) {
        this.selectedFieldGroupingLabel = selectedFieldGroupingLabel;
    }

    public void setSelectedEntityAction(EntityCustomAction selectedEntityAction) {
        this.selectedEntityAction = selectedEntityAction;
    }

    public EntityCustomAction getSelectedEntityAction() {
        return selectedEntityAction;
    }


    @Override
    public String getEditViewName() {
        return "customizedRelationship";
    }

    @Override
    public String getListViewName() {
        return "customizedRelationships";
    }

    public String getCrtPrefix() {
        if (crtPrefix != null) {
            return crtPrefix;

        } else if (entity != null && entity.getCode() != null) {
            crtPrefix = entity.getAppliesTo();
            return crtPrefix;
        }
        return null;
    }

    @Override
    public void newTab() { 
        setSelectedFieldGrouping(new SortedTreeNode(GroupedCustomFieldTreeItemType.tab, new TranslatableLabel(""), groupedFields, true));
        selectedFieldGrouping.setExpanded(true);
    }

    @Override
    public void newFieldGroup(TreeNode parentNode) {
        setSelectedFieldGrouping(new SortedTreeNode(GroupedCustomFieldTreeItemType.fieldGroup, new TranslatableLabel(""), parentNode, true));
        selectedFieldGrouping.setExpanded(true);
    }

    @Override
    public void saveUpdateFieldGrouping() {
        try {
            updateFieldGuiPositionValue((SortedTreeNode) selectedFieldGrouping);
        } catch (BusinessException e) {
            log.error("Failed to update field grouping {}", selectedFieldGrouping, e);
            messages.error(new BundleKey("messages", "error.unexpected"));
        }
    }

    @Override
    public void cancelFieldGrouping() {
        if (StringUtils.isBlank((String) selectedFieldGrouping.getData())) {
            selectedFieldGrouping.getParent().getChildren().remove(selectedFieldGrouping);
        }
    }

    public void moveUp(SortedTreeNode node) {

        int currentIndex = node.getIndexInParent();

        // Move a position up within the same branch
        if (currentIndex > 0) {
            TreeNode parent = node.getParent();
            parent.getChildren().remove(currentIndex);
            parent.getChildren().add(currentIndex - 1, node);

            // Move a position up outside the branch
        } else if (currentIndex == 0 && node.canMoveUp()) {
            TreeNode parentSibling = node.getParentSiblingUp();
            node.getParent().getChildren().remove(currentIndex);
            parentSibling.getChildren().add(node);
        }

        try {
            updateFieldGuiPositionValue((SortedTreeNode) node.getParent());

        } catch (BusinessException e) {
            log.error("Failed to move up {}", node, e);
            messages.error(new BundleKey("messages", "error.unexpected"));
        }
    }

    public void moveDown(SortedTreeNode node) {

        int currentIndex = node.getIndexInParent();
        boolean isLast = node.isLast();

        // Move a position down within the same branch
        if (!isLast) {
            TreeNode parent = node.getParent();
            if (node.getType().equals(GroupedCustomFieldTreeItemType.field.name())) {
                SortedTreeNode siblingDown = node.getSiblingDown();
                if (siblingDown != null && !siblingDown.getType().equals(GroupedCustomFieldTreeItemType.field.name())) {
                    parent.getChildren().remove(currentIndex);
                    siblingDown.getChildren().add(0, node);
                    return;
                }
            }
            parent.getChildren().remove(currentIndex);
            parent.getChildren().add(currentIndex + 1, node);

            // Move a position down outside the branch
        } else if (isLast && node.canMoveDown()) {
            SortedTreeNode parentSibling = node.getParentSiblingDown();
            if (parentSibling != null) {
                node.getParent().getChildren().remove(currentIndex);

                if (parentSibling.getType().equals(GroupedCustomFieldTreeItemType.field.name())) {
                    parentSibling.getParent().getChildren().add(parentSibling.getIndexInParent(), node);
                } else {
                    parentSibling.getChildren().add(0, node);
                }
            }
        }

        try {
            updateFieldGuiPositionValue((SortedTreeNode) node.getParent());

        } catch (BusinessException e) {
            log.error("Failed to move down {}", node, e);
            messages.error(new BundleKey("messages", "error.unexpected"));
        }
    }

    public boolean validateUniqueFields(CustomRelationshipTemplate customRelationshipTemplate) {
        if (entity.isTransient()) {
            CustomRelationshipTemplate customRelationshipTemplateNew = customRelationshipTemplateService.findByCode(customRelationshipTemplate.getCode());
            if (customRelationshipTemplateNew != null) {
                messages.error(new BundleKey("messages", "customRelationshipEntity.unqueFields"), customRelationshipTemplate.getCode(), customRelationshipTemplate.getStartNode().getName(), customRelationshipTemplate.getEndNode().getName());
                FacesContext.getCurrentInstance().validationFailed();
                return false;
            }
        }

        return true;
    }

    public class RuleNameValue implements Serializable {

        private static final long serialVersionUID = 3694377290046737073L;
        private String name;
        private String value;

        public RuleNameValue() {
        }

        public RuleNameValue(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            RuleNameValue nameValue = (RuleNameValue) o;

            if (!name.equals(nameValue.name))
                return false;
            return value.equals(nameValue.value);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }
    }

    private void updateFieldGuiPositionValue(SortedTreeNode nodeToUpdate) throws BusinessException {

        // Re-position current and child nodes
        List<TreeNode> nodes = nodeToUpdate.getChildren(); 
        if (!nodeToUpdate.getType().equals(GroupedCustomFieldTreeItemType.root.name())) {
            nodes = new ArrayList<TreeNode>();
            nodes.add(nodeToUpdate);
        }

        for (TreeNode treeNode : nodes) {
            SortedTreeNode sortedNode = (SortedTreeNode) treeNode;

            String currentPosition = null;
            if (sortedNode.getType().equals(GroupedCustomFieldTreeItemType.tab.name())) { 
                currentPosition = GroupedCustomFieldTreeItemType.tab.positionTag + ":" + sortedNode.getData() + ":" + sortedNode.getIndexInParent();

            } else if (sortedNode.getType().equals(GroupedCustomFieldTreeItemType.fieldGroup.name())) {
                if (!sortedNode.getParent().getType().equals(GroupedCustomFieldTreeItemType.root.name())) {
                    currentPosition = GroupedCustomFieldTreeItemType.tab.positionTag + ":" + sortedNode.getParent().getData() + ":"
                            + ((SortedTreeNode) sortedNode.getParent()).getIndexInParent() + ";";
                }
                currentPosition = currentPosition + GroupedCustomFieldTreeItemType.fieldGroup.positionTag + ":" + sortedNode.getData() + ":" + sortedNode.getIndexInParent();

            }
            for (TreeNode node : sortedNode.getChildren()) {
                SortedTreeNode sortedChildNode = (SortedTreeNode) node;
                if (sortedChildNode.getType().equals(GroupedCustomFieldTreeItemType.field.name())) {
                    String guiPosition = currentPosition + ";" + GroupedCustomFieldTreeItemType.fieldGroup.positionTag + ":" + sortedChildNode.getIndexInParent();
                    CustomFieldTemplate cft = (CustomFieldTemplate) sortedChildNode.getData();
                    cft = customFieldTemplateService.refreshOrRetrieve(cft);
                    if (!guiPosition.equals(cft.getGuiPosition())) {
                        cft.setGuiPosition(guiPosition);
                        cft = customFieldTemplateService.update(cft);
                        sortedChildNode.setData(cft);
                    }
                    
                } else if (sortedChildNode.getType().equals(GroupedCustomFieldTreeItemType.fieldGroup.name())) {
                    String childGroupPosition = currentPosition + ";" + GroupedCustomFieldTreeItemType.fieldGroup.name() + ":" + sortedChildNode.getData() + ":"
                            + sortedChildNode.getIndexInParent();
                    for (TreeNode childNode : sortedChildNode.getChildren()) {
                        SortedTreeNode sortedChildChildNode = (SortedTreeNode) childNode;
                        String guiPosition = childGroupPosition + ";" + GroupedCustomFieldTreeItemType.field.positionTag + ":" + sortedChildChildNode.getIndexInParent();
                        CustomFieldTemplate cft = (CustomFieldTemplate) sortedChildChildNode.getData();
                        cft = customFieldTemplateService.refreshOrRetrieve(cft);
                        if (!guiPosition.equals(cft.getGuiPosition())) {
                            cft.setGuiPosition(guiPosition);
                            cft = customFieldTemplateService.update(cft);
                            sortedChildChildNode.setData(cft);
                        }
                    }
                }
            }
        }
    }

    public class SortedTreeNode extends DefaultTreeNode {

        private static final long serialVersionUID = 3694377290046737073L;

        public SortedTreeNode() {
            super();
        }

        public SortedTreeNode(String type, Object data, TreeNode parent) {
            super(type, data, parent);
        }
        
        public SortedTreeNode(GroupedCustomFieldTreeItemType type, Object data, TreeNode parent, Boolean expanded) {
            super(type.name(), data, parent);
            if (expanded != null && expanded) {
                this.setExpanded(true);
            }
        }
 
        
        public String getGuiPositionForField() {

            if (getType().equals(GroupedCustomFieldTreeItemType.tab.name())) {
                return GroupedCustomFieldTreeItemType.tab.positionTag + ":" + getData() + ":" + getParent().getChildren().indexOf(this) + ";"
                        + "field:" + getChildCount();

            } else if (getType().equals(GroupedCustomFieldTreeItemType.fieldGroup.name())) {
                String guiPosition = GroupedCustomFieldTreeItemType.fieldGroup.positionTag + ":" + getData() + ":" + getParent().getChildren().indexOf(this) + "field:" + getChildCount();
                if (getParent().getType().equals(GroupedCustomFieldTreeItemType.tab.name())) {
                    guiPosition = GroupedCustomFieldTreeItemType.tab.positionTag + ":" + getParent().getData() + ":" + getParent().getParent().getChildren().indexOf(getParent())
                            + ";" + guiPosition;
                }
                return guiPosition;
            }
            return null;
        }
        
         
        
        
        public boolean canMoveUp() {
            // Can not move if its is a first item in a tree and nowhere to move
            return !(getIndexInParent() == 0 && (this.getType().equals(GroupedCustomFieldTreeItemType.tab.name())
                    || (this.getType().equals(GroupedCustomFieldTreeItemType.fieldGroup.name()) && ((SortedTreeNode) this.getParent()).getIndexInParent() == 0) || (this.getType().equals(
                    		GroupedCustomFieldTreeItemType.field.name())
                    && this.getParent().getType().equals(GroupedCustomFieldTreeItemType.tab.name()) && ((SortedTreeNode) this.getParent()).getIndexInParent() == 0)));

        }

        public boolean canMoveDown() {

            return !(isLast() && (this.getType().equals(GroupedCustomFieldTreeItemType.tab.name())
                    || (this.getType().equals(GroupedCustomFieldTreeItemType.field.name()) && ((SortedTreeNode) this.getParent()).isLast())
                    || (this.getType().equals(GroupedCustomFieldTreeItemType.field.name()) && this.getParent().getType().equals(GroupedCustomFieldTreeItemType.tab.name()) && ((SortedTreeNode) this
                        .getParent()).isLast()) || (this.getType().equals(GroupedCustomFieldTreeItemType.field.name()) && this.getParent().getType().equals(GroupedCustomFieldTreeItemType.fieldGroup.name()) && !((SortedTreeNode) this
                .getParent()).canMoveDown())));

        }

        public TreeNode getSelectedFieldGrouping() {
            return selectedFieldGrouping;
        }


        
        
        protected int getIndexInParent() {
            return getParent().getChildren().indexOf(this);
        }

        protected boolean isLast() {
            return getIndexInParent() == this.getParent().getChildCount() - 1;
        }

        public SortedTreeNode getParentSiblingDown() {

            SortedTreeNode parent = (SortedTreeNode) this.getParent();
            while (parent.getParent() != null) {
                int parentIndex = parent.getIndexInParent();
                if (parent.getParent().getChildCount() > parentIndex + 1) {
                    SortedTreeNode sibling = (SortedTreeNode) parent.getParent().getChildren().get(parentIndex + 1);
                    return sibling;
                }
                parent = (SortedTreeNode) parent.getParent();
            }

            return null;
        }

        public SortedTreeNode getSiblingDown() {
            int currentIndex = this.getIndexInParent();
            if (getParent().getChildCount() > currentIndex + 1) {
                return (SortedTreeNode) getParent().getChildren().get(currentIndex + 1);
            }

            return null;
        }

        public SortedTreeNode getParentSiblingUp() {

            SortedTreeNode parent = (SortedTreeNode) this.getParent();
            while (parent.getParent() != null) {
                int parentIndex = parent.getIndexInParent();
                if (parentIndex > 0) {
                    SortedTreeNode sibling = (SortedTreeNode) parent.getParent().getChildren().get(parentIndex - 1);
                    return sibling;
                }
                parent = (SortedTreeNode) parent.getParent();
            }

            return null;
        }
    }
    public DualListModel<DBStorageType> getAvailableStoragesDM() {
        if (availableStoragesDM == null) {
            List<DBStorageType> perksSource = new ArrayList<>();
            for (DBStorageType dbStorageType : DBStorageType.values()) {
                perksSource.add(dbStorageType);
            }
            List<DBStorageType> perksTarget = new ArrayList<DBStorageType>();
            if (getEntity().getAvailableStorages() != null) {
                perksTarget.addAll(getEntity().getAvailableStorages());
            }
            perksSource.removeAll(perksTarget);
            availableStoragesDM = new DualListModel<DBStorageType>(perksSource, perksTarget);
        }
        return availableStoragesDM;
    }
    public void setAvailableStoragesDM(DualListModel<DBStorageType> availableStoragesDM) {
        this.availableStoragesDM = availableStoragesDM;
    }
    public List<DBStorageType> getStorageTypesList(){
        ArrayList<DBStorageType> arrayList = new ArrayList<>(availableStoragesDM.getSource());
        arrayList.addAll(availableStoragesDM.getTarget());
        return arrayList;
    }
}