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
package org.meveo.admin.action.catalog;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.action.admin.ViewBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.elresolver.ELException;
import org.meveo.model.scripts.Accessor;
import org.meveo.model.scripts.CustomScript;
import org.meveo.model.scripts.ScriptIO;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.scripts.ScriptInstanceNode;
import org.meveo.model.scripts.ScriptSourceTypeEnum;
import org.meveo.model.security.Role;
import org.meveo.service.admin.impl.RoleService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.script.CustomScriptService;
import org.meveo.service.script.ScriptInstanceService;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DualListModel;
import org.primefaces.model.TreeNode;

/**
 * Standard backing bean for {@link org.meveo.model.scripts.ScriptInstance} (extends {@link org.meveo.admin.action.BaseBean} that provides almost all common methods to handle entities filtering/sorting in datatable, their
 * create, edit, view, delete operations). It works with Manaty custom JSF components.
 */
@Named
@ViewScoped
@ViewBean
public class ScriptInstanceBean extends BaseBean<ScriptInstance> {
    private static final long serialVersionUID = 1L;
    private static final String JAVA  = "java";
    private static final String ES5  = "es5";

    /**
     * Injected @{link ScriptInstance} service. Extends {@link org.meveo.service.base.PersistenceService}.
     */
    @Inject
    private ScriptInstanceService scriptInstanceService;

    @Inject
    private RoleService roleService;

    private DualListModel<Role> execRolesDM;
    private DualListModel<Role> sourcRolesDM;

    private List<ScriptIO> inputs = new ArrayList<>();
    private List<ScriptIO> outputs = new ArrayList<>();

    private TreeNode rootNode;

    private TreeNode selectedNode;

    public void initCompilationErrors() {
        if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) {
            return;
        }
        if (getObjectId() == null) {
            return;
        }

        if (entity == null) {
            initEntity();
        }

        if (entity.isError()) {
            scriptInstanceService.compileScript(entity, true);
        }
    }

    public DualListModel<Role> getExecRolesDM() {

        if (execRolesDM == null) {
            List<Role> perksSource = roleService.getAllRoles();
            List<Role> perksTarget = new ArrayList<>();
            if (getEntity().getExecutionRoles() != null) {
                perksTarget.addAll(getEntity().getExecutionRoles());
            }
            perksSource.removeAll(perksTarget);
            execRolesDM = new DualListModel<>(perksSource, perksTarget);
        }
        return execRolesDM;
    }

    public DualListModel<Role> getSourcRolesDM() {

        if (sourcRolesDM == null) {
            List<Role> perksSource = roleService.getAllRoles();
            List<Role> perksTarget = new ArrayList<>();
            if (getEntity().getSourcingRoles() != null) {
                perksTarget.addAll(getEntity().getSourcingRoles());
            }
            perksSource.removeAll(perksTarget);
            sourcRolesDM = new DualListModel<>(perksSource, perksTarget);
        }
        return sourcRolesDM;
    }

    public void setExecRolesDM(DualListModel<Role> perks) {
        this.execRolesDM = perks;
    }

    public void setSourcRolesDM(DualListModel<Role> perks) {
        this.sourcRolesDM = perks;
    }

    /**
     * Constructor. Invokes super constructor and provides class type of this bean for {@link org.meveo.admin.action.BaseBean}.
     */
    public ScriptInstanceBean() {
        super(ScriptInstance.class);

    }

    /**
     * @see org.meveo.admin.action.BaseBean#getPersistenceService()
     */
    @Override
    protected IPersistenceService<ScriptInstance> getPersistenceService() {
        return scriptInstanceService;
    }

    @Override
    protected String getListViewName() {
        return "scriptInstances";
    }

    /**
     * Fetch customer field so no LazyInitialize exception is thrown when we access it from account edit view.
     *
     */
    @Override
    protected List<String> getFormFieldsToFetch() {
        return Arrays.asList("executionRoles", "sourcingRoles");
    }

    @Override
    protected String getDefaultSort() {
        return "code";
    }

    @Override
    @ActionMethod
    public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {
        String code = entity.getCode();
        if (entity.getSourceTypeEnum() == ScriptSourceTypeEnum.JAVA) {
            code = CustomScriptService.getFullClassname(entity.getScript());

            // check script existed full class name in class path
            if (CustomScriptService.isOverwritesJavaClass(code)) {
                messages.error(new BundleKey("messages", "message.scriptInstance.classInvalid"), code);
                return null;
            }
        }

        // check duplicate script
        CustomScript scriptDuplicate = scriptInstanceService.findByCode(code); // genericScriptService
        if (scriptDuplicate != null && !scriptDuplicate.getId().equals(entity.getId())) {
            messages.error(new BundleKey("messages", "scriptInstance.scriptAlreadyExists"), code);
            return null;
        }

        // Update roles
        getEntity().getExecutionRoles().clear();
        if (execRolesDM != null) {
            getEntity().getExecutionRoles().addAll(roleService.refreshOrRetrieve(execRolesDM.getTarget()));
        }

        // Update roles
        getEntity().getSourcingRoles().clear();
        if (sourcRolesDM != null) {
            getEntity().getSourcingRoles().addAll(roleService.refreshOrRetrieve(sourcRolesDM.getTarget()));
        }
        if (CollectionUtils.isNotEmpty(inputs)) {
            List<String> scriptInputs = new ArrayList<>();
            for (ScriptIO scriptIO : inputs) {
                if (scriptIO.isEditable()) {
                    scriptInputs.add(scriptIO.getName());
                }
                getEntity().getScriptInputs().clear();
                getEntity().getScriptInputs().addAll(scriptInputs);
            }
        }

        if (CollectionUtils.isNotEmpty(outputs)) {
            List<String> scriptOutputs = new ArrayList<>();
            for (ScriptIO scriptIO : outputs) {
                if (scriptIO.isEditable()) {
                    scriptOutputs.add(scriptIO.getName());
                }
                getEntity().getScriptOutputs().clear();
                getEntity().getScriptOutputs().addAll(scriptOutputs);
            }
        }

        String result = super.saveOrUpdate(false);

        if (entity.isError()) {
            result = "scriptInstanceDetail.xhtml?faces-redirect=true&objectId=" + getObjectId() + "&edit=true&cid=" + conversation.getId();
        }else {
            if (killConversation) {
                endConversation();
            }
        }

        return result;
    }

    public String execute() {
        scriptInstanceService.test(entity.getCode(), null);
        return null;
    }

    @Override
    public void runListFilter() {
        rootNode = computeRootNode();
        super.runListFilter();
    }

    @Override
    public void search() {
        super.search();
        rootNode = computeRootNode();
    }

    public List<String> getLogs() {
        return scriptInstanceService.getLogs(entity.getCode());
    }

    public boolean isUserHasSourcingRole(ScriptInstance scriptInstance) {
        return scriptInstanceService.isUserHasSourcingRole(scriptInstance);
    }

    public void testCompilation() {
        String code = entity.getCode();
        // check script existed full class name in class path
        if (entity.getSourceTypeEnum() == ScriptSourceTypeEnum.JAVA) {
            code = CustomScriptService.getFullClassname(entity.getScript());
            if (CustomScriptService.isOverwritesJavaClass(code)) {
                messages.error(new BundleKey("messages", "message.scriptInstance.classInvalid"), code);
                return;
            }
        }

        // check duplicate script
        CustomScript scriptDuplicate = scriptInstanceService.findByCode(code); // genericScriptService
        if (scriptDuplicate != null && !scriptDuplicate.getId().equals(entity.getId())) {
            messages.error(new BundleKey("messages", "scriptInstance.scriptAlreadyExists"), code);
            return;
        }

        scriptInstanceService.compileScript(entity, true);
        if (!entity.isError()) {
            messages.info(new BundleKey("messages", "scriptInstance.compilationSuccessfull"));
        }
    }

    /**
     * Autocomplete method for selecting a class that implement ICustomFieldEntity. Return a human readable class name. Used in conjunction with CustomFieldAppliesToConverter
     *
     * @param query Partial class name to match
     * @return
     */
    public List<ScriptInstance> autocompleteScriptsNames(String query) {
        return scriptInstanceService.findByCodeLike(query);
    }

    public List<ScriptIO> getInputs() {
        if (CollectionUtils.isEmpty(inputs)) {
            if (entity.getId() != null && entity.getSourceTypeEnum() == ScriptSourceTypeEnum.JAVA) {
                final List<Accessor> setters = entity.getSetters();
                if (CollectionUtils.isNotEmpty(setters)) {
                    for (Accessor accessor : setters) {
                        inputs.add(createIO(accessor.getName()));
                    }
                }
            }
            if (entity.getId() != null && CollectionUtils.isNotEmpty(entity.getScriptInputs())) {
                ScriptIO input;
                for (String item : entity.getScriptInputs()) {
                    input = new ScriptIO();
                    input.setName(item);
                    inputs.add(input);
                }
            }
        }
        return inputs;
    }

    public List<ScriptIO> getOutputs() {
        if (CollectionUtils.isEmpty(outputs)) {
            if (entity.getId() != null && entity.getSourceTypeEnum() == ScriptSourceTypeEnum.JAVA) {
                final List<Accessor> getters = entity.getGetters();
                if (CollectionUtils.isNotEmpty(getters)) {
                    for (Accessor accessor : getters) {
                        outputs.add(createIO(accessor.getName()));
                    }
                }
            }

            if (entity.getId() != null && CollectionUtils.isNotEmpty(entity.getScriptOutputs())) {
                ScriptIO output;
                for (String item : entity.getScriptOutputs()) {
                    output = new ScriptIO();
                    output.setName(item);
                    outputs.add(output);
                }
            }
        }
        return outputs;
    }

    public void addNewInput() {
        ScriptIO scriptIO = new ScriptIO();
        scriptIO.setEditable(true);
        inputs.add(scriptIO);
    }

    public void addNewOutput() {
        ScriptIO scriptIO = new ScriptIO();
        scriptIO.setEditable(true);
        outputs.add(scriptIO);
    }

    public void removeScriptInput(ScriptIO scriptIO) {
        inputs.remove(scriptIO);
    }

    public void removeScriptOutput(ScriptIO scriptIO) {
        outputs.remove(scriptIO);
    }

    public TreeNode getRootNode() {
        if(rootNode == null) {
            rootNode = computeRootNode();
        }
        return rootNode;
    }

    public TreeNode computeRootNode() {
        log.info("Computing root node");

        Map<String, Object> filters = this.getFilters();
        String code = "";
        boolean isExpand = false;
        if (this.filters.containsKey("code")) {
            code = (String) filters.get("code");
        }

        List<ScriptInstance> scriptInstances = new ArrayList<>();
        if (!org.meveo.commons.utils.StringUtils.isBlank(code)) {
            scriptInstances = scriptInstanceService.findByCodeLike(code);
            isExpand = true;
        } else {
            scriptInstances = scriptInstanceService.list();
        }
        rootNode = new DefaultTreeNode("document", new ScriptInstanceNode("", ""), null);
        rootNode.setExpanded(isExpand);
        List<ScriptInstance> javaScriptInstances = null;
        List<ScriptInstance> es5ScriptInstances = null;
        if (CollectionUtils.isNotEmpty(scriptInstances)) {
            javaScriptInstances = scriptInstances.stream()
                    .filter(e -> e.getSourceTypeEnum() == ScriptSourceTypeEnum.JAVA)
                    .collect(Collectors.toList());
            es5ScriptInstances = scriptInstances.stream()
                    .filter(e -> e.getSourceTypeEnum() == ScriptSourceTypeEnum.ES5)
                    .collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(javaScriptInstances)) {
            TreeNode rootJava = new DefaultTreeNode("document", new ScriptInstanceNode(JAVA, JAVA), rootNode);
            rootJava.setExpanded(isExpand);
            for (ScriptInstance scriptInstance : javaScriptInstances) {
                String[] fullNames = scriptInstance.getCode().split("\\.");
                List<String> nodes = new LinkedList<>(Arrays.asList(fullNames));
                createTree(JAVA, nodes, rootJava, scriptInstance.getCode(), scriptInstance.getId(), scriptInstance.getError(), isExpand);
            }
        }
        if (CollectionUtils.isNotEmpty(es5ScriptInstances)) {
            TreeNode rootEs5 = new DefaultTreeNode("document", new ScriptInstanceNode(ES5, ES5), rootNode);
            rootEs5.setExpanded(isExpand);
            for (ScriptInstance scriptInstance : es5ScriptInstances) {
                String[] fullNames = scriptInstance.getCode().split("\\.");
                List<String> nodes = new LinkedList<>(Arrays.asList(fullNames));
                createTree(ES5, nodes, rootEs5, scriptInstance.getCode(), scriptInstance.getId(), scriptInstance.getError(), isExpand);
            }
        }

        return rootNode;
    }

    public void setRootNode(TreeNode rootNode) {
        this.rootNode = rootNode;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public void viewScript(NodeSelectEvent event) throws IOException{
        ScriptInstanceNode scriptInstanceNode = (ScriptInstanceNode) event.getTreeNode().getData();
        if (scriptInstanceNode.getId() != null) {
            String url = "scriptInstanceDetail.jsf?objectId=" + scriptInstanceNode.getId() +"&edit=true&cid=" + conversation.getId();
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        }
    }

    private ScriptIO createIO(String name) {
        ScriptIO scriptIO = new ScriptIO();
        scriptIO.setName(name);
        scriptIO.setEditable(false);
        return scriptIO;
    }

    private void createTree(String scripType, List<String> packages, TreeNode rootNode, String fullName, Long id, Boolean error, boolean isExpand) {
        if (CollectionUtils.isNotEmpty(packages)) {
            String nodeName = packages.get(0);
            TreeNode newNode = findNode(rootNode, nodeName);
            if (newNode == null) {
                if (fullName.endsWith(nodeName)) {
                    newNode = new DefaultTreeNode("document", new ScriptInstanceNode(nodeName, scripType, fullName, error, id), rootNode);
                } else {
                    newNode = new DefaultTreeNode("document", new ScriptInstanceNode(nodeName, scripType), rootNode);
                }
                newNode.setExpanded(isExpand);
            }
            packages.remove(0);
            createTree(scripType, packages, newNode, fullName, id, error, isExpand);
        }
    }

    private TreeNode findNode(TreeNode node, String searchNodeName){
        List<TreeNode> subChild = node.getChildren();
        TreeNode foundNode = null;
        for (TreeNode treeNode : subChild) {
            ScriptInstanceNode scriptInstanceNode = (ScriptInstanceNode) treeNode.getData();
            if (scriptInstanceNode.getName().equals(searchNodeName)){
                return treeNode;
            }
            foundNode = findNode(treeNode, searchNodeName);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return foundNode;
    }
}