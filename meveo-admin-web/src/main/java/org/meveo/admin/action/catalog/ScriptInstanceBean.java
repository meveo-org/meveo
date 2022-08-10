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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.action.admin.ViewBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.ScriptInstanceApi;
import org.meveo.api.dto.ScriptInstanceDto;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.elresolver.ELException;
import org.meveo.jpa.JpaAmpNewTx;
import org.meveo.model.scripts.Accessor;
import org.meveo.model.scripts.CustomScript;
import org.meveo.model.scripts.FunctionIO;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.model.scripts.ScriptIO;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.scripts.ScriptInstanceError;
import org.meveo.model.scripts.ScriptInstanceNode;
import org.meveo.model.scripts.ScriptSourceTypeEnum;
import org.meveo.model.security.Role;
import org.meveo.model.wf.WFAction;
import org.meveo.service.admin.impl.RoleService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.script.CustomScriptService;
import org.meveo.service.script.MavenDependencyService;
import org.meveo.service.script.ScriptInstanceService;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DualListModel;
import org.primefaces.model.TreeNode;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

/**
 * Standard backing bean for {@link ScriptInstance} (extends {@link BaseBean}
 * that provides almost all common methods to handle entities filtering/sorting
 * in datatable, their create, edit, view, delete operations). It works with
 * Manaty custom JSF components.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.11
 */
@Named
@ViewScoped
@ViewBean
public class ScriptInstanceBean extends BaseBean<ScriptInstance> {
	private static final long serialVersionUID = 1L;
	private static final String JAVA = "java";
	private static final String ES5 = "es5";
	private static final String PYTHON = "python";

	/**
	 * Injected @{link ScriptInstance} service. Extends
	 * {@link org.meveo.service.base.PersistenceService}.
	 */
	@Inject
	private ScriptInstanceService scriptInstanceService;

	@Inject
	private RoleService roleService;

	@Inject
	private ScriptInstanceApi scriptInstanceApi;

	@Inject
	private MavenDependencyService mavenDependencyService;

	private DualListModel<Role> execRolesDM;
	private DualListModel<Role> sourcRolesDM;

	private List<ScriptIO> inputs = new ArrayList<>();
	private List<ScriptIO> outputs = new ArrayList<>();

	private List<MavenDependency> mavenDependencies;
	private MavenDependency mavenDependency = new MavenDependency();

	private TreeNode rootNode;

    private TreeNode selectedNode;

    public void initialize() {
        rootNode = computeRootNode();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void organizeImports() {
    	// Don't need to re-compile if compilation already has errors
    	if(entity.getScriptErrors() == null || entity.getScriptErrors().isEmpty()) {
    		scriptInstanceService.compileScript(entity, true);
    	}
    	
    	if(entity.getScriptErrors() == null) {
    		return;
    	}
    	
    	List<Class<?>> classesToImport = new ArrayList<>();
    	List<ScriptInstanceError> resolvedErrors = new ArrayList<>();
    	
        for(ScriptInstanceError error : entity.getScriptErrors()) {
        	Pattern shouldMatch = Pattern.compile("cannot find symbol.*class.*", Pattern.DOTALL);
        	Pattern pattern = Pattern.compile("symbol.*class (.*)");
        	Matcher matcher = pattern.matcher(error.getMessage());
        	if(shouldMatch.matcher(error.getMessage()).matches() && matcher.find()) {
        		String className = matcher.group(1);
        		try {
					ReflectionUtils.getClasses("")
						.stream()
		        		.filter(e -> e.getSimpleName().equals(className)).findFirst()
	        			.ifPresent(e -> {
	        	        	resolvedErrors.add(error);
	        	        	classesToImport.add(e);
	        			});					
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		}

		String sourceCode = entity.getScript();
		Matcher matcher = Pattern.compile("package.*;").matcher(sourceCode);
		if (matcher.find() && !classesToImport.isEmpty()) {
			String packageLine = matcher.group(0);
			String body = sourceCode.replace(packageLine, "");
			sourceCode = packageLine + "\n\n";
			for (Class<?> classToImport : classesToImport) {
				sourceCode = "\n" + sourceCode + "import " + classToImport.getName() + ";";
			}
			sourceCode = sourceCode + body;
		}

		entity.setScript(sourceCode);
		resolvedErrors.forEach(entity.getScriptErrors()::remove);
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void formatSourceCode() {
		String sourceCode = entity.getScript();
		CompilationUnit compilationUnit;

		try {
			compilationUnit = JavaParser.parse(sourceCode);
			entity.setScript(compilationUnit.toString());
		} catch (Exception e) {
			// Skip getter and setters parsing. We don't need to log errors as they will be
			// logged later in code.
			return;
		}
	}

	@SuppressWarnings("deprecation")
	public String getSourceCode() throws IOException {
		if (!StringUtils.isBlank(entity.getScript())) {
			return entity.getScript();
		}

		if (entity.getSourceTypeEnum() == ScriptSourceTypeEnum.JAVA) {
			return IOUtils.toString(this.getClass().getResourceAsStream("/templates/DefaultScript.java"));
		}

		return null;
	}

	public void setSourceCode(String sourceCode) {
		this.getEntity().setScript(sourceCode);
	}

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
			if (getEntity().getExecutionRolesNullSafe() != null) {
				perksTarget.addAll(getEntity().getExecutionRolesNullSafe());
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
			if (getEntity().getSourcingRolesNullSafe() != null) {
				perksTarget.addAll(getEntity().getSourcingRolesNullSafe());
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
	 * Constructor. Invokes super constructor and provides class type of this bean
	 * for {@link BaseBean}.
	 */
	public ScriptInstanceBean() {
		super(ScriptInstance.class);

	}

	/**
	 * @see BaseBean#getPersistenceService()
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
	 * Fetch customer field so no LazyInitialize exception is thrown when we access
	 * it from account edit view.
	 *
	 */
	@Override
	protected List<String> getFormFieldsToFetch() {
		return Arrays.asList("executionRoles", "sourcingRoles", "mavenDependencies", "importScriptInstances", "scriptInputs", "scriptOutputs");
	}

	@Override
	protected List<String> getListFieldsToFetch() {
		return Arrays.asList("mavenDependencies", "importScriptInstances", "scriptInputs", "scriptOutputs");
	}

	@Override
	protected String getDefaultSort() {
		return "code";
	}

	@Override
	@ActionMethod
	public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {
		// Make sure we don't work on a persistent entity
		getPersistenceService().detach(entity);

		String code = entity.getCode();

		if (entity.getSourceTypeEnum() == ScriptSourceTypeEnum.JAVA) {
			code = CustomScriptService.getFullClassname(entity.getScript());

			// check script existed full class name in class path
			if (CustomScriptService.isOverwritesJavaClass(code)) {
				messages.error(new BundleKey("messages", "message.scriptInstance.classInvalid"), code);
				return null;
			}
		}

		// Update roles
		getEntity().getExecutionRolesNullSafe().clear();
		if (execRolesDM != null) {
			getEntity().getExecutionRolesNullSafe().addAll(roleService.refreshOrRetrieve(execRolesDM.getTarget()));
		}

		// Update roles
		getEntity().getSourcingRolesNullSafe().clear();
		if (sourcRolesDM != null) {
			getEntity().getSourcingRolesNullSafe().addAll(roleService.refreshOrRetrieve(sourcRolesDM.getTarget()));
		}

		// Update inputs
		if (CollectionUtils.isNotEmpty(inputs)) {
			List<String> scriptInputs = new ArrayList<>();
			for (ScriptIO scriptIO : inputs) {
				if (scriptIO.isEditable()) {
					scriptInputs.add(scriptIO.getName());
				}
				getEntity().getScriptInputsNullSafe().clear();
				getEntity().getScriptInputsNullSafe().addAll(scriptInputs);
			}
		}

		// Update outputs
		if (CollectionUtils.isNotEmpty(outputs)) {
			List<String> scriptOutputs = new ArrayList<>();
			for (ScriptIO scriptIO : outputs) {
				if (scriptIO.isEditable()) {
					scriptOutputs.add(scriptIO.getName());
				}
				getEntity().getScriptOutputsNullSafe().clear();
				getEntity().getScriptOutputsNullSafe().addAll(scriptOutputs);
			}
		}

		// Update maven dependencies
		if (CollectionUtils.isNotEmpty(mavenDependencies)) {
			Set<MavenDependency> scriptMavens = new HashSet<>();
			for (MavenDependency mavenDependency : mavenDependencies) {
				if (mavenDependency != null) {
					scriptMavens.add(mavenDependency);
				}
			}
			getEntity().getMavenDependenciesNullSafe().clear();
			getEntity().getMavenDependenciesNullSafe().addAll(scriptMavens);
		} else {
			getEntity().getMavenDependenciesNullSafe().clear();
		}

		boolean isUnique = false;
		if (mavenDependencies != null) {
			Integer line = 1;
			Map<String, Integer> map = new HashMap<>();
			for (MavenDependency maven : mavenDependencies) {
				String key = maven.getGroupId() + maven.getArtifactId();
				if (map.containsKey(key)) {
					Integer position = map.get(key);
					messages.error(new BundleKey("messages", "scriptInstance.error.duplicate"), line, position);
					isUnique = true;
				} else {
					map.put(key, line);
				}
				line++;
			}
			line = 1;

			for (MavenDependency maven : mavenDependencies) {
				if (!mavenDependencyService.validateUniqueFields(maven.getVersion(), maven.getGroupId(), maven.getArtifactId())) {
					messages.error(new BundleKey("messages", "scriptInstance.error.unique"), line);
					isUnique = true;
				}
				line++;

			}

		}

		if (isUnique) {
			log.info("Not updating script {} because a maven dependency already exists with another version");
			return null;
		}

		// Update script references
		List<ScriptInstance> importedScripts = scriptInstanceService.populateImportScriptInstance(getEntity().getScript());
		getEntity().getImportScriptInstancesNullSafe().clear();
		if (CollectionUtils.isNotEmpty(importedScripts)) {
			getEntity().getImportScriptInstancesNullSafe().addAll(importedScripts);
		}

		// Manage entity
		super.saveOrUpdate(false);
	
//		var dto = scriptInstanceApi.toDto(entity);
//		try {
//			scriptInstanceApi.createOrUpdate(dto);
//
//			if (entity.isTransient()) {
//				entity = scriptInstanceService.findByCode(dto.getCode());
//				setObjectId(entity.getId());
//			}
//
//		} catch (MeveoApiException e) {
//			messages.error("Entity can't be saved. Please retry.");
//			throw new BusinessException(e);
//		}
//		String message = entity.isTransient() ? "save.successful" : "update.successful";
//        messages.info(new BundleKey("messages", message));

		String result = "scriptInstanceDetail.xhtml?faces-redirect=true&objectId=" + entity.getId() + "&edit=true";
		return "";
	}

    @Override
    public String deleteWithBack() throws BusinessException {
        entity = scriptInstanceService.findById(getEntity().getId());
        return super.deleteWithBack();
    }

	@ActionMethod
	@JpaAmpNewTx
	public String execute() {
		scriptInstanceService.test(entity.getCode(), null);
		endConversation();
        messages.info(new BundleKey("messages", "info.entity.executed"));
		//return "scriptInstanceDetail.xhtml?faces-redirect=true&objectId=" + getObjectId() + "&edit=true&";
        return "";
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
			initEntity(entity.getId());
		}
	}

	/**
	 * Autocomplete method for selecting a class that implement ICustomFieldEntity.
	 * Return a human readable class name. Used in conjunction with
	 * CustomFieldAppliesToConverter
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
				final List<Accessor> setters = entity.getSettersNullSafe();
				if (CollectionUtils.isNotEmpty(setters)) {
					for (Accessor accessor : setters) {
						inputs.add(createIO(accessor.getName()));
					}
				}
			}
			if (entity.getId() != null && CollectionUtils.isNotEmpty(entity.getScriptInputsNullSafe())) {
				ScriptIO input;
				for (String item : entity.getScriptInputsNullSafe()) {
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
				final List<Accessor> getters = entity.getGettersNullSafe();
				if (CollectionUtils.isNotEmpty(getters)) {
					for (Accessor accessor : getters) {
						outputs.add(createIO(accessor.getName()));
					}
				}
			}

			if (entity.getId() != null && CollectionUtils.isNotEmpty(entity.getScriptOutputsNullSafe())) {
				ScriptIO output;
				for (String item : entity.getScriptOutputsNullSafe()) {
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
		if (rootNode == null) {
			rootNode = computeRootNode();
		}
		return rootNode;
	}

	public TreeNode computeRootNode() {
		Map<String, Object> filters = this.getFilters();
		String code = "";
		boolean isExpand = true;
		if (this.filters.containsKey("code")) {
			code = (String) filters.get("code");
		}

		List<ScriptInstanceDto> scriptInstances = new ArrayList<>();
		if (!org.meveo.commons.utils.StringUtils.isBlank(code)) {
			scriptInstances = scriptInstanceApi.getScriptsForTreeView(code);
			isExpand = true;
		} else {
			scriptInstances = scriptInstanceApi.getScriptsForTreeView(null);
		}
		rootNode = new DefaultTreeNode("document", new ScriptInstanceNode("", ""), null);
		rootNode.setExpanded(isExpand);
		List<ScriptInstanceDto> javaScriptInstances = null;
		List<ScriptInstanceDto> es5ScriptInstances = null;
		List<ScriptInstanceDto> pythonScriptInstances = null;
		if (CollectionUtils.isNotEmpty(scriptInstances)) {
			javaScriptInstances = scriptInstances.stream().filter(e -> e.getType() == ScriptSourceTypeEnum.JAVA).collect(Collectors.toList());
			es5ScriptInstances = scriptInstances.stream().filter(e -> e.getType() == ScriptSourceTypeEnum.ES5).collect(Collectors.toList());
			pythonScriptInstances = scriptInstances.stream().filter(e -> e.getType() == ScriptSourceTypeEnum.PYTHON).collect(Collectors.toList());
		}
		
		if (CollectionUtils.isNotEmpty(javaScriptInstances)) {
			TreeNode rootJava = new DefaultTreeNode("document", new ScriptInstanceNode(JAVA, JAVA), rootNode);
			rootJava.setExpanded(isExpand);
			for (ScriptInstanceDto scriptInstance : javaScriptInstances) {
				String[] fullNames = scriptInstance.getCode().split("\\.");
				List<String> nodes = new LinkedList<>(Arrays.asList(fullNames));
				createTree(JAVA, nodes, rootJava, scriptInstance.getCode(), scriptInstance.getId(), scriptInstance.getError(), isExpand);
			}
		}
		
		if (CollectionUtils.isNotEmpty(es5ScriptInstances)) {
			TreeNode rootEs5 = new DefaultTreeNode("document", new ScriptInstanceNode(ES5, ES5), rootNode);
			rootEs5.setExpanded(isExpand);
			for (ScriptInstanceDto scriptInstance : es5ScriptInstances) {
				String[] fullNames = scriptInstance.getCode().split("\\.");
				List<String> nodes = new LinkedList<>(Arrays.asList(fullNames));
				createTree(ES5, nodes, rootEs5, scriptInstance.getCode(), scriptInstance.getId(), scriptInstance.getError(), isExpand);
			}
		}
		
		if (CollectionUtils.isNotEmpty(pythonScriptInstances)) {
			TreeNode rootPython = new DefaultTreeNode("document", new ScriptInstanceNode(PYTHON, PYTHON), rootNode);
			rootPython.setExpanded(isExpand);
			for (ScriptInstanceDto scriptInstance : pythonScriptInstances) {
				String[] fullNames = scriptInstance.getCode().split("\\.");
				List<String> nodes = new LinkedList<>(Arrays.asList(fullNames));
				createTree(PYTHON, nodes, rootPython, scriptInstance.getCode(), scriptInstance.getId(), scriptInstance.getError(), isExpand);
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

	public void viewScript(NodeSelectEvent event) throws IOException {
		ScriptInstanceNode scriptInstanceNode = (ScriptInstanceNode) event.getTreeNode().getData();
		if (scriptInstanceNode.getId() != null) {
			String url = "scriptInstanceDetail.jsf?objectId=" + scriptInstanceNode.getId() + "&edit=true&cid=" + conversation.getId();
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

	private TreeNode findNode(TreeNode node, String searchNodeName) {
		List<TreeNode> subChild = node.getChildren();
		TreeNode foundNode = null;
		for (TreeNode treeNode : subChild) {
			ScriptInstanceNode scriptInstanceNode = (ScriptInstanceNode) treeNode.getData();
			if (scriptInstanceNode.getName().equals(searchNodeName)) {
				return treeNode;
			}
			foundNode = findNode(treeNode, searchNodeName);
			if (foundNode != null) {
				return foundNode;
			}
		}
		return foundNode;
	}

	public List<MavenDependency> getMavenDependencies() {
		if (mavenDependencies == null) {
			if (entity.getMavenDependenciesNullSafe() != null) {
				mavenDependencies = new ArrayList<>(entity.getMavenDependenciesNullSafe());
				return mavenDependencies;
			} else {
				return new ArrayList<>();
			}
		}
		return mavenDependencies;
	}

	public void setMavenDependencies(List<MavenDependency> mavenDependencies) {
		this.mavenDependencies = mavenDependencies;
	}

	public MavenDependency getMavenDependency() {
		return mavenDependency;
	}

	public void removeMavenDependency(MavenDependency selectedMavenDependency) {
		mavenDependencies.remove(selectedMavenDependency);
	}

	public void addMavenDependency() {
		if (mavenDependencies == null) {
			mavenDependencies = new ArrayList<>();
			entity.setMavenDependencies(new HashSet<>());
		}

		mavenDependency = new MavenDependency();
		
		if (entity.getSourceTypeEnum() == ScriptSourceTypeEnum.ES5) {
			mavenDependency.setGroupId("org.webjars.npm");
		}
		mavenDependencies.add(mavenDependency);
	}
	
    public List<FunctionIO> getScriptInputs(ScriptInstance script) {
    	if(script == null) {
    		return List.of();
    	}
    	
    	try {
			return scriptInstanceService.getInputs(script);
		} catch (BusinessException e) {
			log.error("Failed to get inputs of script {}", script);
			return List.of();
		}
    }
}