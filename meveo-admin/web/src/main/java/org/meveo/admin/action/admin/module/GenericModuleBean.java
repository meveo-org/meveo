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
package org.meveo.admin.action.admin.module;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseCrudBean;
import org.meveo.admin.action.admin.ViewBean;
import org.meveo.admin.action.catalog.ScriptInstanceBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ModuleUtil;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.CustomEntityTemplateDto;
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.module.MeveoModuleItemDto;
import org.meveo.api.module.MeveoModuleApi;
import org.meveo.api.module.OnDuplicate;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.elresolver.ELException;
import org.meveo.model.BusinessEntity;
import org.meveo.model.communication.MeveoInstance;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleDependency;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.storage.Repository;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.admin.impl.MeveoModuleUtils;
import org.meveo.service.admin.impl.ModuleUninstall;
import org.meveo.service.admin.impl.ModuleUninstall.ModuleUninstallBuilder;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.util.view.ServiceBasedLazyDataModel;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.CroppedImage;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DualListModel;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.TreeNode;

/**
 * Meveo module bean
 *
 * @author Clément Bareth
 * @author Tyshan Shi(tyshan@manaty.net)
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @lastModifiedVersion 6.9.0
 */

public abstract class GenericModuleBean<T extends MeveoModule> extends BaseCrudBean<T, MeveoModuleDto> {

    private static final long serialVersionUID = 8332852624069548417L;

    @Inject
    protected MeveoModuleService meveoModuleService;

    @Inject
    private MeveoModuleApi moduleApi;

    @Inject
    @ViewBean
    protected ScriptInstanceBean scriptInstanceBean;

    @Inject
    protected CustomFieldTemplateService customFieldTemplateService;

    private BusinessEntity moduleItemEntity;
    private MeveoModule moduleDependencyEntity;
    private TreeNode root;
    protected MeveoInstance meveoInstance;
    private CroppedImage croppedImage;
    private String tmpPicture;
    private boolean remove = true;
    private boolean deleteFiles;
    private OnDuplicate onDuplicate = OnDuplicate.SKIP;
    
    // Properties used to force user to reload page between installation and uninstallation
    private boolean showInstallBtn;
	private boolean showUninstallBtn;
	
	protected DualListModel<String> repositoriesDM;
	
	protected ModuleUninstallBuilder moduleUninstall = ModuleUninstall.builder();
	

    public GenericModuleBean() {

    }

    public GenericModuleBean(Class<T> clazz) {
        super(clazz);
    }

    @Override
    @PostConstruct
    public void init() {
        root = new DefaultTreeNode("Root");
        repositoriesDM = new DualListModel<>(repositoryService.list().stream()
    			.map(Repository::getCode)
    			.collect(Collectors.toList()), new ArrayList<>());
    }
    
    /**
	 * @return the {@link #repository}
	 */
	public DualListModel<String> getRepositoriesDM() {
		return repositoriesDM;
	}
	
	/**
	 * @param repositories the repositories to set
	 */
	public void setRepositoriesDM(DualListModel<String> repositories) {
		this.repositoriesDM = repositories;
	}

	/**
	 * @return the {@link #moduleUninstall}
	 */
	public ModuleUninstallBuilder getModuleUninstall() {
		return moduleUninstall;
	}

	/**
	 * @param moduleUninstall the moduleUninstall to set
	 */
	public void setModuleUninstall(ModuleUninstallBuilder moduleUninstall) {
		this.moduleUninstall = moduleUninstall;
	}

	public OnDuplicate getOnDuplicate() {
		return onDuplicate;
	}

	public void setOnDuplicate(OnDuplicate onDuplicate) {
		this.onDuplicate = onDuplicate;
	}

	public MeveoInstance getMeveoInstance() {
        return meveoInstance;
    }

    public void setMeveoInstance(MeveoInstance meveoInstance) {
        this.meveoInstance = meveoInstance;
    }

    @SuppressWarnings("unchecked")
	@Override
    public T initEntity() {
        T module = super.initEntity();

        // If module is in being developed, show module items from meveoModule.moduleItems()
        if (!module.isDownloaded() || module.isInstalled()) {
            if (module.getModuleItems() == null) {
                return module;
            }

            List<MeveoModuleItem> itemsToRemove = new ArrayList<>();

            createTree(module, itemsToRemove);
            
            // If module was downloaded, show module items from meveoModule.moduleSource
        } else {
            try {
                MeveoModuleDto dto = MeveoModuleUtils.moduleSourceToDto(module);

                if (dto.getModuleItems() == null) {
                    return module;
                }

				for (MeveoModuleItemDto moduleItemDto : dto.getModuleItems()) {
					Class<? extends BaseEntityDto> dtoClass = (Class<? extends BaseEntityDto>) Class.forName(moduleItemDto.getDtoClassName());
					BaseEntityDto itemDto = JacksonUtil.convert(moduleItemDto.getDtoData(), dtoClass);

					if (itemDto instanceof CustomFieldTemplateDto) {
						CustomFieldTemplateDto customFieldTemplateDto = (CustomFieldTemplateDto) itemDto;
						TreeNode classNode = getOrCreateNodeByAppliesTo(customFieldTemplateDto.getAppliesTo(), itemDto.getClass().getName());
						new DefaultTreeNode("item", itemDto, classNode);

					} else if (itemDto instanceof CustomEntityTemplateDto) {
						// include the cft as well
						TreeNode classNode = getOrCreateNodeByClass(itemDto.getClass().getName());
						new DefaultTreeNode("item", itemDto, classNode);
						CustomEntityTemplateDto customEntityTemplateDto = (CustomEntityTemplateDto) itemDto;

						if (customEntityTemplateDto.getFields() != null && !customEntityTemplateDto.getFields().isEmpty()) {
							String cftClassName = CustomFieldTemplate.class.getName();
							TreeNode cftNode = new DefaultTreeNode(cftClassName, ReflectionUtils.getHumanClassName(cftClassName), classNode);
							cftNode.setExpanded(true);

							for (CustomFieldTemplateDto cftDto : customEntityTemplateDto.getFields()) {
								new DefaultTreeNode("item", cftDto, cftNode);
							}
						}

					} else {
						TreeNode classNode = getOrCreateNodeByClass(itemDto.getClass().getName());
						new DefaultTreeNode("item", itemDto, classNode);
					}
				}

            } catch (Exception e) {
                log.error("Failed to load module source {}", module.getCode(), e);
            }

        }

        this.showInstallBtn = !module.isInstalled();
        this.showUninstallBtn = module.isInstalled();
        return module;
    }
    
	public boolean isShowInstallBtn() {
		return showInstallBtn;
	}

	public boolean isShowUninstallBtn() {
		return showUninstallBtn;
	}

	/**
	 * @param module
	 * @param notLoadedItems
	 */
	private void createTree(T module, List<MeveoModuleItem> notLoadedItems) {
		for (MeveoModuleItem item : module.getModuleItems()) {

		    // Load an entity related to a module item. If it was not been able to load (e.g. was deleted), mark it to be deleted and delete
		    try {
		    	if(item.getItemEntity() == null) {
		    		meveoModuleService.loadModuleItem(item);
		    	}
		        
		        if (item.getItemEntity() instanceof CustomFieldTemplate) {
		            TreeNode classNode = getOrCreateNodeByAppliesTo(item.getAppliesTo(), item.getItemClass());
		            new DefaultTreeNode("item", item, classNode);
		            
		        } else {
		            TreeNode classNode = getOrCreateNodeByClass(item.getItemClass());
		            new DefaultTreeNode("item", item, classNode);
		        }
		        
			} catch (BusinessException e) {
		        log.error("Failed to load module source {}", module.getCode(), e);
			}

		}
	}

    public BusinessEntity getModuleItemEntity() {
        return moduleItemEntity;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void setModuleItemEntity(BusinessEntity itemEntity) throws BusinessException {
        if (itemEntity != null && !entity.equals(itemEntity)) {
            List<String> uuidList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(entity.getModuleItems())) {
                for (MeveoModuleItem moduleItem : entity.getModuleItems()) {
                    if (moduleItem.getItemEntity() instanceof CustomEntityInstance) {
                        uuidList.add(((CustomEntityInstance) moduleItem.getItemEntity()).getUuid());
                    }
                }
            }
            if (itemEntity instanceof CustomEntityInstance && uuidList.contains(((CustomEntityInstance) itemEntity).getUuid())) {
                messages.error(new BundleKey("messages", "meveoModule.error.ceiExisted"), itemEntity.getCode());
                return;
            }
            MeveoModuleItem item = new MeveoModuleItem(itemEntity);
            if (itemEntity instanceof CustomEntityInstance && itemEntity.getId() == null) {
                item.setAppliesTo(((CustomEntityInstance) itemEntity).getCetCode());
            }
            if (!entity.getModuleItems().contains(item)) {
            	try {
            		meveoModuleService.addModuleItem(item, entity);
            	} catch (BusinessException e) {
            		throw new BusinessException("Entity cannot be add or remove from the module", e);
            	}
                
            } else {
                messages.error(new BundleKey("messages", "meveoModule.error.moduleItemExisted"), itemEntity.getCode(), entity.getCode());
                return;
            }

            moduleItemEntity = itemEntity;
            
        	root = new DefaultTreeNode("Root");
    		createTree(entity, null);
        }
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    @SuppressWarnings("serial")
	public LazyDataModel<MeveoModule> getSubModules() {
        HashMap<String, Object> filters = new HashMap<>();

        if (!getEntity().isTransient()) {
            filters.put("ne id", entity.getId());
        }

        final Map<String, Object> finalFilters = filters;

        return new ServiceBasedLazyDataModel<MeveoModule>() {

            @Override
            protected IPersistenceService<MeveoModule> getPersistenceServiceImpl() {
                return meveoModuleService;
            }

            @Override
            protected Map<String, Object> getSearchCriteria() {

                // Omit empty or null values
                Map<String, Object> cleanFilters = new HashMap<>();

                for (Map.Entry<String, Object> filterEntry : finalFilters.entrySet()) {
                    if (filterEntry.getValue() == null) {
                        continue;
                    }
                    if (filterEntry.getValue() instanceof String) {
                        if (StringUtils.isBlank((String) filterEntry.getValue())) {
                            continue;
                        }
                    }
                    cleanFilters.put(filterEntry.getKey(), filterEntry.getValue());
                }

                return GenericModuleBean.this.supplementSearchCriteria(cleanFilters);
            }

            @Override
            protected String getDefaultSortImpl() {
                return getDefaultSort();
            }

            @Override
            protected SortOrder getDefaultSortOrderImpl() {
                return getDefaultSortOrder();
            }

            @Override
            protected List<String> getListFieldsToFetchImpl() {
                return getListFieldsToFetch();
            }

        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void removeTreeNode(TreeNode node) throws BusinessException {
        MeveoModuleItem item = (MeveoModuleItem) node.getData();
        TreeNode parent = node.getParent();
        parent.getChildren().remove(node);
        if (parent.getChildCount() == 0) {
            parent.getParent().getChildren().remove(parent);
        }
        entity.removeItem(item);
    }

    public void publishModule() {

        if (meveoInstance != null) {
            log.debug("export module {} to remote instance {}", entity.getCode(), meveoInstance.getCode());
            try {
                meveoModuleService.publishModule2MeveoInstance(entity, meveoInstance);
                messages.info(new BundleKey("messages", "meveoModule.publishSuccess"), entity.getCode(), meveoInstance.getCode());
            } catch (Exception e) {
                log.error("Error when export module {} to {}", entity.getCode(), meveoInstance, e);
                messages.error(new BundleKey("messages", "meveoModule.publishFailed"), entity.getCode(), meveoInstance.getCode(),
                    (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
            }
        }
    }

    public void cropLogo() {
        try {
            String originFilename = croppedImage.getOriginalFilename();
            String formatname = originFilename.substring(originFilename.lastIndexOf(".") + 1);
            String filename = String.format("%s.%s", entity.getCode(), formatname);
            filename.replaceAll(" ", "_");
            log.debug("crop module picture to {}", filename);
            String dest = ModuleUtil.getModulePicturePath(currentUser.getProviderCode()) + File.separator + filename;
            ModuleUtil.cropPicture(dest, croppedImage);
            entity.setLogoPicture(filename);
            messages.info(new BundleKey("messages", "meveoModule.cropPictureSuccess"));
        } catch (Exception e) {
            log.error("error when crop a module picture {}, info {}!", croppedImage.getOriginalFilename(), e.getMessage());
            messages.error(new BundleKey("messages", "meveoModule.cropPictureFailed"), (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        log.debug("upload file={}", event.getFile().getFileName());
        String originFilename = event.getFile().getFileName();
        int formatPosition = originFilename.lastIndexOf(".");
        String formatname = null;
        if (formatPosition > 0) {
            formatname = originFilename.substring(formatPosition + 1);
        }
        if (!"JPEG".equalsIgnoreCase(formatname) && !"JPG".equalsIgnoreCase(formatname) && !"PNG".equalsIgnoreCase(formatname) && !"GIF".equalsIgnoreCase(formatname)) {
            log.debug("error picture format name for origin file {}!", originFilename);
            return;
        }
        String filename = String.format("%s.%s", getTmpFilePrefix(), formatname);
        this.tmpPicture = filename;
        InputStream in = null;
        try {
            String tmpFolder = ModuleUtil.getTmpRootPath(currentUser.getProviderCode());
            String dest = tmpFolder + File.separator + filename;
            log.debug("output original module picture file to {}", dest);
            in = event.getFile().getInputstream();
            BufferedImage src = ImageIO.read(in);
            ImageIO.write(src, formatname, new File(dest));
            messages.info(new BundleKey("messages", "meveoModule.uploadPictureSuccess"), originFilename);
        } catch (Exception e) {
            log.error("Failed to upload a picture {} for module {}, info {}", filename, entity.getCode(), e.getMessage(), e);
            messages.error(new BundleKey("messages", "meveoModule.uploadPictureFailed"), (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public CroppedImage getCroppedImage() {
        return croppedImage;
    }

    public void setCroppedImage(CroppedImage croppedImage) {
        this.croppedImage = croppedImage;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    @Override
    @ActionMethod
    public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {

        MeveoModule moduleDuplicate = meveoModuleService.findByCodeWithFetchEntities(entity.getCode());
        if (moduleDuplicate != null && !moduleDuplicate.getId().equals(entity.getId())) {
            messages.error(new BundleKey("messages", "commons.uniqueField.code"), entity.getCode());
            return null;
        }

        boolean isNew = entity.isTransient();

        super.saveOrUpdate(killConversation);

        if (isNew) {
            return getEditViewName();
        } else {
            return back();
        }
    }

    private void removeModulePicture(String filename) {
        if (filename == null) {
            return;
        }
        try {
            ModuleUtil.removeModulePicture(currentUser.getProviderCode(), filename);
        } catch (Exception e) {
            log.error("failed to remove module picture {}, info {}", filename, e.getMessage(), e);
        }
    }

    @ActionMethod
    public String deleteModuleFile() throws BusinessException, IOException {
        MeveoModule module = meveoModuleService.findById(entity.getId(), getListFieldsToFetch());
        List<String> pathFiles = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(module.getModuleFiles())) {
            for (String moduleFile : module.getModuleFiles()) {
                pathFiles.add(moduleFile);
            }
        }
        delete();
        if (CollectionUtils.isNotEmpty(pathFiles) && deleteFiles) {
            meveoModuleService.removeFilesIfModuleIsDeleted(pathFiles);
        }
        return getListViewName();
    }

    /**
     * clean uploaded picture
     */
    @ActionMethod
    @Override
    public void delete() throws BusinessException {

        String source = entity.getLogoPicture();
        super.delete();
        if (source != null) {
            removeModulePicture(source);
        }
    }

    /**
     * clean uploaded pictures for multi delete
     */
    @ActionMethod
    @Override
    public void deleteMany() throws Exception {
        List<String> files = new ArrayList<>();
        String source;

        for (MeveoModule entity : getSelectedEntities()) {
            source = entity.getLogoPicture();
            if (source != null) {
                files.add(source);
            }
        }

        super.deleteMany();

        for (String file : files) {
            removeModulePicture(file);
        }
    }

    private static String getTmpFilePrefix() {
        return UUID.randomUUID().toString();
    }

    public String getTmpPicture() {
        return tmpPicture;
    }

    public void setTmpPicture(String tmpPicture) {
        this.tmpPicture = tmpPicture;
    }

    private TreeNode getOrCreateNodeByClass(String classname) {

        classname = classname.replaceAll("Dto", "");
        classname = classname.replaceAll("DTO", "");
        for (TreeNode node : root.getChildren()) {
            if (classname.equals(node.getType())) {
                return node;
            }
        }

        TreeNode node = new DefaultTreeNode(classname, ReflectionUtils.getHumanClassName(classname), root);
        node.setExpanded(true);
        return node;
    }

    private TreeNode getOrCreateNodeByAppliesTo(String appliesTo, String classname) {
        TreeNode appliesToNode = getOrCreateNodeByClass(classname);
        String code = null;
        if (appliesTo.contains("_"))
        	code = appliesTo.split("_", 2)[1];
        else code = appliesTo;
        for (TreeNode node : appliesToNode.getChildren()) {
            if (code.equals(node.getData())) {
                return node;
            }
        }

        TreeNode node = new DefaultTreeNode(appliesTo, code, getOrCreateNodeByClass(classname));
        node.setExpanded(true);
        return node;
    }

    public void refreshScript() {
        entity.setScript(scriptInstanceBean.getEntity());
    }

    /**
     * Prepare to show a popup to view or edit script
     */
    public void viewEditScript() {
        if (entity.getScript() != null) {
            scriptInstanceBean.initEntity(entity.getScript().getId());
        } else {
            scriptInstanceBean.newEntity();
        }
        scriptInstanceBean.setBackViewSave(this.getEditViewName());
    }

    /**
     * Prepare to show a popup to enter new script
     */
    public void newScript() {
        scriptInstanceBean.newEntity();
        scriptInstanceBean.setBackViewSave(this.getEditViewName());
    }

    @SuppressWarnings("unchecked")
    public String install() {
    	
    	try {
	        entity = (T) install(entity, onDuplicate);
	        return "moduleDetail.xhtml?faces-redirect=true&meveoModuleId=" + entity.getId() + "&edit=true";
        
    	} catch (Exception e) {
            log.error("Failed to install meveo module {} ", entity.getCode(), e);
            
            Throwable rootCause = e;
            while(rootCause.getCause() != null) {
            	rootCause = rootCause.getCause();
            }
            
            messages.error(new BundleKey("messages", "meveoModule.installFailed"), entity.getCode(), (rootCause.getMessage() == null ? rootCause.getClass().getSimpleName() : rootCause.getMessage()));
            return null;
    	}
    }
    
	public void onRepositoryChange() {
		log.info("test", repositoriesDM.getTarget());
	}

	public MeveoModule install(MeveoModule module, OnDuplicate onDuplicate) throws Exception {

		if (!module.isDownloaded()) {
			return module;

		} else if (module.isInstalled()) {
			messages.warn(new BundleKey("messages", "meveoModule.installedAlready"));
			return module;
		}

		MeveoModuleDto moduleDto = MeveoModuleUtils.moduleSourceToDto(module);
		if (module.getScript() != null) {
			boolean checkTestSuits = meveoModuleService.checkTestSuites(module.getScript().getCode());
			if (!checkTestSuits && module.getIsInDraft()) {
				messages.error(new BundleKey("messages", "meveoModule.warningWhenInstallingModule"));
			}
			if (!checkTestSuits && !module.getIsInDraft()) {
				return null;
			}
		}

		var result = moduleApi.install(repositoriesDM.getTarget(), moduleDto, onDuplicate);
		messages.info(new BundleKey("messages", "meveoModule.installSuccess"), moduleDto.getCode());
		messages.info(result.toString());

		module = result.getInstalledModule();

		return module;
    }

    @SuppressWarnings("unchecked")	
    @Transactional(TxType.REQUIRES_NEW)
    public void uninstall() {
        try {

            if (!entity.isInstalled()) {
                messages.warn(new BundleKey("messages", "meveoModule.notInstalled"));
                return;
            }

            moduleUninstall.module(entity);
            List<MeveoModule> uninstalledModules = moduleApi.uninstall(MeveoModule.class, moduleUninstall.build());
            entity = (T) uninstalledModules.get(0);
            messages.info(new BundleKey("messages", "meveoModule.uninstallSuccess"), entity.getCode());

        } catch (Exception e) {
            log.error("Failed to uninstall meveo module {} ", entity.getCode(), e);
            messages.error(new BundleKey("messages", "meveoModule.uninstallFailed"), entity.getCode(), (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
	public void removeFileFromModule(String item) {
        try {
            entity.removeModuleFile(item);
            entity = (T) meveoModuleService.update(entity);
        } catch (BusinessException e) {
            log.error("Failed to remove module file {}", item, e);
        }
    }
    
    @SuppressWarnings("unchecked")
	public void removeModuleDependency(MeveoModuleDependency dependency) {
        try {
            entity.removeModuleDependency(dependency);
            entity = (T) meveoModuleService.update(entity);
        } catch (BusinessException e) {
            log.error("Failed to remove module dependency {}", dependency, e);
        }
    }

	public void setMeveoModule(MeveoModule meveoModule) {

	}
 
	public MeveoModule getModuleDependencyEntity() {
		return moduleDependencyEntity;
	}

	@SuppressWarnings("unchecked")
	public void setModuleDependencyEntity(MeveoModule dependencyEntity) throws BusinessException {
		if (dependencyEntity != null && !entity.equals(dependencyEntity)) {
			dependencyEntity = meveoModuleService.findById(dependencyEntity.getId(), Arrays.asList("patches", "releases", "moduleDependencies"));
			MeveoModuleDependency meveoDependency = new MeveoModuleDependency(dependencyEntity.getCode(), dependencyEntity.getDescription(), dependencyEntity.getCurrentVersion());
			if (!entity.getModuleDependencies().contains(meveoDependency)) {
				entity.addModuleDependency(meveoDependency);
			}
			moduleDependencyEntity = dependencyEntity;
			entity = (T) meveoModuleService.update(entity);
		}
	}

    public boolean isDeleteFiles() {
       return deleteFiles;
    }

    public void setDeleteFiles(boolean deleteFiles) {
        this.deleteFiles = deleteFiles;
    }
    
}