package org.meveo.admin.action.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseCrudBean;
import org.meveo.admin.action.admin.custom.CustomFieldDataEntryBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.storage.RepositoryApi;
import org.meveo.api.storage.RepositoryDto;
import org.meveo.elresolver.ELException;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.hierarchy.HierarchyLevel;
import org.meveo.model.hierarchy.UserHierarchyLevel;
import org.meveo.model.storage.Repository;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.hierarchy.impl.UserHierarchyLevelService;
import org.meveo.service.storage.RepositoryService;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 * Controller for managing {@link Repository} CRUD operations.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 **/
@Named
@ViewScoped
public class RepositoryBean extends BaseCrudBean<Repository, RepositoryDto> {

	private static final long serialVersionUID = 8661265102557481231L;
	
    @Inject
    protected CustomFieldDataEntryBean customFieldDataEntryBean;

	@Inject
	private RepositoryService repositoryService;

	@Inject
	private RepositoryApi repositoryApi;

	@Inject
	private UserHierarchyLevelService userHierarchyLevelService;

	private Boolean forceDelete;

	private TreeNode userGroupRootNode;

	private TreeNode userGroupSelectedNode;

	public RepositoryBean() {
		super(Repository.class);
	}

	@Override
	public String getEditViewName() {
		return "repositoryDetail";
	}

	@Override
	protected String getListViewName() {
		return "repositories";
	}

	@Override
	protected IPersistenceService<Repository> getPersistenceService() {
		return repositoryService;
	}

	@Override
	public BaseCrudApi<Repository, RepositoryDto> getBaseCrudApi() {
		return repositoryApi;
	}

	public Boolean getForceDelete() {
		return forceDelete;
	}

	public void setForceDelete(Boolean forceDelete) {
		this.forceDelete = forceDelete;
	}

	@ActionMethod
	public String saveOrUpdate() throws BusinessException, ELException {
		String message = entity.isTransient() ? "save.successful" : "update.successful";

		if (getUserGroupSelectedNode() != null) {
			UserHierarchyLevel userHierarchyLevel = (UserHierarchyLevel) this.getUserGroupSelectedNode().getData();
			getEntity().setUserHierarchyLevel(userHierarchyLevel);
		}
		
		customFieldDataEntryBean.saveCustomFieldsToEntity((ICustomFieldEntity) entity, entity.isTransient());

		if (entity.isTransient()) {
			repositoryService.create(entity);

		} else {
			repositoryService.update(entity);
		}

		messages.info(new BundleKey("messages", message));
		return back();
	}

	/**
	 * Deletes a given repository and it's children when value of forceDelete is
	 * true.
	 * 
	 * @param entity      record to be deleted
	 * @param forceDelete if true, delete the children
	 * @throws BusinessException failed entity deletion
	 */
	@ActionMethod
	public void delete(Repository entity, Boolean forceDelete) throws BusinessException {
		repositoryService.remove(entity, forceDelete);
	}

	@ActionMethod
	public String deleteAndNavigate() throws BusinessException {
		repositoryService.remove(entity);
		return getListViewName();
	}

	public List<Repository> listWithSqlConnection() {
		return repositoryService.listWithSqlConnection();
	}

	public void setUserGroupRootNode(TreeNode rootNode) {
		this.userGroupRootNode = rootNode;
	}

	public TreeNode getUserGroupRootNode() {

		if (userGroupRootNode == null) {
			userGroupRootNode = new DefaultTreeNode("Root", null);
			List<UserHierarchyLevel> roots;
			roots = userHierarchyLevelService.findRoots();
			UserHierarchyLevel userHierarchyLevel = getEntity().getUserHierarchyLevel();
			if (CollectionUtils.isNotEmpty(roots)) {
				Collections.sort(roots);
				for (UserHierarchyLevel userGroupTree : roots) {
					createTree(userGroupTree, userGroupRootNode, userHierarchyLevel);
				}
			}
		}
		return userGroupRootNode;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	private TreeNode createTree(HierarchyLevel hierarchyLevel, TreeNode rootNode, UserHierarchyLevel selectedHierarchyLevel) {

		TreeNode newNode = new DefaultTreeNode(hierarchyLevel, rootNode);
		newNode.setExpanded(true);

		if (hierarchyLevel.getChildLevels() != null) {
			List<UserHierarchyLevel> subTree = new ArrayList<UserHierarchyLevel>(hierarchyLevel.getChildLevels());
			if (selectedHierarchyLevel != null && selectedHierarchyLevel.getId().equals(hierarchyLevel.getId())) {
				newNode.setSelected(true);
			}

			if (CollectionUtils.isNotEmpty(subTree)) {
				Collections.sort(subTree);
				for (HierarchyLevel userGroupTree : subTree) {
					createTree(userGroupTree, newNode, selectedHierarchyLevel);
				}
			}
		}

		return newNode;
	}

	public TreeNode getUserGroupSelectedNode() {
		return userGroupSelectedNode;
	}

	public void setUserGroupSelectedNode(TreeNode userGroupSelectedNode) {
		this.userGroupSelectedNode = userGroupSelectedNode;
	}

	public void clearUserHierarchyLevel() {
		entity.setUserHierarchyLevel(null);
		userGroupSelectedNode = null;
		userGroupRootNode = null;
	}
}
