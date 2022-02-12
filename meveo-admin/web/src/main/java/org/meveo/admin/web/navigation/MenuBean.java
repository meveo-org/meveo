/**
 * 
 */
package org.meveo.admin.web.navigation;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.meveo.admin.util.ResourceBundle;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.custom.CustomEntityCategoryService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.DynamicMenuModel;

@Named
@SessionScoped
public class MenuBean implements Serializable {

	private static final long serialVersionUID = 2232511683891649553L;
	
	@Inject
	@CurrentUser
	private transient MeveoUser currentUser;
	
	@Inject
	private transient ResourceBundle resourceBundle;
	
    @Inject
    private transient CustomEntityTemplateService customEntityTemplateService;
    
    @Inject
    private transient CustomEntityCategoryService customEntityCategoryService;
	
	private DynamicMenuModel menu;
	
	@PostConstruct
	public void init() {
		menu = new DynamicMenuModel();
		addAdminMenu(menu);
		addExecutionMenu(menu);
		addOntologyMenu(menu);
		addServicesMenu(menu);
		addReportingMenu(menu);
		addCETMenues(menu);
	}
	
	public DynamicMenuModel getMenu() {
		return menu;
	}
	
	private void addReportingMenu(DynamicMenuModel menu) {
		if (!currentUser.hasRole("reportingVisualization")) {
			return;
		}
		
		DefaultSubMenu reporting = new DefaultSubMenu(resourceBundle.getString("menu.reporting"));
		reporting.setId("reporting");
		menu.addElement(reporting);
		
		addItem(reporting, "measurableQuantities", "menu.measurableQuantities");
		addItem(reporting, "measuredValueDetail", "menu.measuredValues", "measuredValues");
		addItem(reporting, "charts", "menu.charts");
	}
	
	private void addServicesMenu(DynamicMenuModel menu) {
		if (!currentUser.hasAnyRole("administrationVisualization", "superAdminManagement")) {
			return;
		}
		
		DefaultSubMenu services = new DefaultSubMenu(resourceBundle.getString("menu.services"));
		services.setId("services");
		menu.addElement(services);
		
		addItem(services, "functionCategories", "menu.functionCategories");
		addItem(services, "scriptInstances", "menu.functions");
		
		DefaultSubMenu endpoints = addSubMenu(services, "endpoint", "menu.endpoint");
		addItem(endpoints, "endpoint", "menu.endpoint.rest", "restEndpoint");
		addItem(endpoints, "webSocketEndpoints", "menu.endpoint.webSocket", "webSocketEndpoint");
	
		DefaultSubMenu jobSubMenu = addSubMenu(services, "jobSubMenu", "menu.jobs");
		addItem(jobSubMenu, "jobInstances", "menu.jobInstances", "jobs");
		addItem(jobSubMenu, "timerEntities", "menu.timerEntities", "timers");
		
		DefaultSubMenu tests = addSubMenu(services, "tests", "menu.tests");
		addItem(tests, "testResults", "menu.function.tests", "allTests");
		addItem(tests, "categoryTests", "menu.function.category.tests", "testsByCategory");
		
		addItem(services, "workflows", "menu.workflow");
		addItem(services, "filters", "menu.filters");
		
		DefaultSubMenu notifications = addSubMenu(services, "notifications", "menu.notifications");
		addItem(notifications, "notifications", "menu.notifications", "internalNotifications");
		addItem(notifications, "webHooks", "menu.webHooks", "webHooks");
		addItem(notifications, "emailNotifications", "menu.emailNotifications", "emailNotifications");
		addItem(notifications, "jobTriggers", "menu.jobTriggers", "jobTriggers");
		addItem(notifications, "webNotifications", "menu.webNotifications", "webNotifications");
		addItem(notifications, "notificationHistories", "menu.notificationHistories", "notificationHistory");
		addItem(notifications, "inboundRequests", "menu.inboundRequests", "inboundRequests");

	}
	
	private void addOntologyMenu(DynamicMenuModel menu) {
		if(!currentUser.hasAnyRole("administrationVisualization", "superAdminManagement")) {
			return;
		}
		
		DefaultSubMenu onto = new DefaultSubMenu(resourceBundle.getString("menu.ontology"));
		onto.setId("ontology");
		menu.addElement(onto);
		
		addItem(onto, "customEntityCategorys", "menu.customEntityCategories", "customEntityCategories");
		addItem(onto, "customizedEntities", "menu.customizedEntities");
		addItem(onto, "customizedRelationships", "menu.customizedRelationships");
	}
	
	private void addAdminMenu(DynamicMenuModel menu) {
		if(!currentUser.hasAnyRole("administrationVisualization", "catalogVisualization", "userVisualization", "superAdminManagement")) {
			return;
		}
		
		DefaultSubMenu admin = new DefaultSubMenu(resourceBundle.getString("menu.configuration"));
		admin.setId("admin");
		menu.addElement(admin);
		
		// Storages sub menu
		if (currentUser.hasAnyRole("superAdminManagement", "administrationVisualization")) {
			DefaultSubMenu storages = addSubMenu(admin, "storages", "menu.storages");
			
			if (currentUser.hasRole("administrationVisualization")) {
				addItem(storages, "repositories", "repository.title");
				addItem(storages, "binaryStorageConfigurations", "binaryStorageConfiguration.title");
				addItem(storages, "sqlConfigurations", "repository.sqlConfiguration");
				addItem(storages, "neo4jConfigurations", "repository.neo4jConfiguration");
				addItem(storages, "gitRepositories", "menu.gitRepositories");
			}
			
			if (currentUser.hasRole("superAdminManagement")) {
				addItem(storages, "mavenConfiguration", "maven.configuration");
			}
		}
		
		// Settings sub-menu
		if (currentUser.hasAnyRole("superAdminManagement", "administrationVisualization")) {
			DefaultSubMenu settings = addSubMenu(admin, "settings", "menu.settings");
			
			if (currentUser.hasRole("administrationVisualization")) {
				addItem(settings, "mailerConfiguration", "menu.mailServer");
				addItem(settings, "emailTemplates", "menu.emailTemplates");
			}
			
			if (currentUser.hasRole("superAdminManagement")) {
				addItem(settings, "meveoProperties", "menu.properties");
				addItem(settings, "auditConfiguration", "menu.auditConfiguration");
			}
		}
		
		// Users sub-menu
		if (currentUser.hasAnyRole("userVisualization", "administrationVisualization")) {
			DefaultSubMenu usersMenu = addSubMenu(admin, "usersMenu", "menu.users");
			
			if (currentUser.hasRole("administrationVisualization")) {
				addItem(usersMenu, "userGroupHierarchy", "menu.userGroupHierarchy");
				addItem(usersMenu, "roles", "menu.userRoles", "userRoles");
			}
			
			if (currentUser.hasRole("userVisualization")) {
				addItem(usersMenu, "users", "menu.users");
			}
		}
		
		addItem(admin, "meveoModules", "menu.meveoModules", "MeveoModules");
		addItem(admin, "meveoInstances", "menu.meveoInstance", "MeveoInstances");
		
	}

	private void addExecutionMenu(DynamicMenuModel menu) {
		if(!currentUser.hasAnyRole("administrationVisualization", "superAdminManagement")) {
			return;
		}
		
		DefaultSubMenu execution = new DefaultSubMenu(resourceBundle.getString("menu.execution"));
		execution.setId("execution");
		menu.addElement(execution);
		
		DefaultSubMenu tools = addSubMenu(execution, "tools", "menu.tools");
		addItem(tools, "import", "menu.import");
		addItem(tools, "export", "menu.export");
		
		DefaultSubMenu logs = addSubMenu(execution, null, "menu.logs");
		addItem(logs, "logs", "menu.logExplorer");
		addItem(logs, "auditLog", "menu.auditLogs");
		
		DefaultSubMenu esSearch = addSubMenu(execution, null, "menu.search");
		if (currentUser.hasRole("administrationVisualization")) {
			addItem(esSearch, "fullTextSearch", "menu.fullTextSearch", "ESSearch");
		}
		if (currentUser.hasRole("superAdminManagement")) {
			addItem(esSearch, "indexing", "menu.fullTextSearch.index", "reindexES");
		}
		
		if (currentUser.hasRole("administrationVisualization")) {
			addItem(execution, "fileExplorer", "menu.fileExplorer", "fileExplorerMenu");
			addItem(execution, "caches", "menu.cacheInfo", "cache");
		}
		addItem(execution, "workflowHistory", "menu.workflowHistory");
	}
	
	private DefaultMenuItem addItem(DefaultSubMenu subMenu, String outcome, String translation) {
		return addItem(subMenu, outcome, translation, outcome);
	}
	
	private DefaultMenuItem addItem(DefaultSubMenu subMenu, String outcome, String translation, String id) {
		DefaultMenuItem item = new DefaultMenuItem(resourceBundle.getString(translation));
		item.setOutcome(outcome);
		item.setId(id);
		item.setTitle(resourceBundle.getString(translation));
		subMenu.addElement(item);
		return item;
	}
	
	private DefaultSubMenu addSubMenu(DefaultSubMenu menu, String id, String translation) {
		DefaultSubMenu subMenu = new DefaultSubMenu(resourceBundle.getString(translation));
		menu.setId(id);
		menu.addElement(subMenu);
		return subMenu;
	}
	
	private void addCETMenues(DynamicMenuModel menu) {
		PaginationConfiguration confCec = new PaginationConfiguration();
		confCec.setFetchFields(List.of("customEntityTemplates"));
		
		Optional.ofNullable(customEntityCategoryService.list(confCec))
			.stream()
			.flatMap(Collection::stream)
			.distinct()
			.forEach(category -> {
				DefaultSubMenu firstSubmenu = new DefaultSubMenu(category.getName());
				
				List<CustomEntityTemplate> firstLevelCets = Optional.ofNullable(category.getCustomEntityTemplates())
						.orElse(List.of())
						.stream()
						.filter(cet -> cet.getSuperTemplate() == null || !category.getCustomEntityTemplates().contains(cet.getSuperTemplate()))
						.collect(Collectors.toList());
				
				if (!firstLevelCets.isEmpty()) {
					addItems(firstSubmenu, firstLevelCets);
					menu.addElement(firstSubmenu);
				}
			});
		
		PaginationConfiguration confCet = new PaginationConfiguration();
		confCet.setFetchFields(List.of("subTemplates"));
		
		List<CustomEntityTemplate> cetsWithoutCategories = customEntityTemplateService.list(confCet).stream()
			.filter(cet -> cet.getCustomEntityCategory() == null && cet.getSuperTemplate() == null)
			.collect(Collectors.toList());
		
		if (!cetsWithoutCategories.isEmpty()) {
			DefaultSubMenu subMenu = new DefaultSubMenu("Others");
			addItems(subMenu, cetsWithoutCategories);
			menu.addElement(subMenu);
		}
	}
	
	private void addItems(DefaultSubMenu subMenu, Collection<CustomEntityTemplate> templates) {
		for (CustomEntityTemplate cet : templates) {
			if (cet.getSubTemplates() == null || cet.getSubTemplates().isEmpty()) {
				DefaultMenuItem item = new DefaultMenuItem(cet.getName());
				item.setOutcome("customEntities");
				item.setParam("cet", cet.getCode());
				subMenu.addElement(item);
			} else {
				DefaultSubMenu cetSubMenu = new DefaultSubMenu(cet.getName());
				cetSubMenu.setStyleClass("cet-" + cet.getCode());
				addItems(cetSubMenu, cet.getSubTemplates());
				subMenu.addElement(cetSubMenu);
			}
		}
	}
	
	@Transactional(value = TxType.REQUIRES_NEW)
	private void cetChange(@Observes(during = TransactionPhase.AFTER_SUCCESS, notifyObserver = Reception.IF_EXISTS) CustomEntityTemplate cet) {
		init();
	}

}
