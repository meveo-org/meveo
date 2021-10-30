/**
 * 
 */
package org.meveo.admin.web.navigation;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.util.ResourceBundle;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.DynamicMenuModel;

@Named
@ViewScoped	// TODO: Session scoped ?
public class MenuBean implements Serializable {

	private static final long serialVersionUID = 2232511683891649553L;
	
	@Inject
	@CurrentUser
	private transient MeveoUser currentUser;
	
	@Inject
	private transient ResourceBundle resourceBundle;
	
	private transient DynamicMenuModel menu;
	
	@PostConstruct
	public void init() {
		menu = new DynamicMenuModel();
		
		addAdminMenu(menu);
	}
	
	public DynamicMenuModel getMenu() {
		return menu;
	}
	
	private void addAdminMenu(DynamicMenuModel menu) {
		if(!currentUser.hasAnyRole("administrationVisualization", "catalogVisualization", "userVisualization", "superAdminManagement")) {
			return;
		}
		
		DefaultSubMenu admin = new DefaultSubMenu(resourceBundle.getString("menu.configuration"));
		admin.setId("admin");
		menu.addElement(admin);
		
		DefaultMenuItem meveoModules = new DefaultMenuItem(resourceBundle.getString("menu.meveoModules"));
		meveoModules.setOutcome("meveoModules");
		admin.addElement(meveoModules);
		
		// Storages sub menu
		if (currentUser.hasAnyRole("superAdminManagement", "administrationVisualization")) {
			DefaultSubMenu storages = new DefaultSubMenu(resourceBundle.getString("menu.storages"));
			storages.setId("storages");
			admin.addElement(storages);
			
			if (currentUser.hasRole("administrationVisualization")) {
				DefaultMenuItem repositories = new DefaultMenuItem(resourceBundle.getString("repository.title"));
				repositories.setOutcome("repositories");
				repositories.setId("repositories");
				storages.addElement(repositories);
		
				DefaultMenuItem binaryStorageConfigurations = new DefaultMenuItem(resourceBundle.getString("binaryStorageConfiguration.title"));
				binaryStorageConfigurations.setOutcome("binaryStorageConfigurations");
				binaryStorageConfigurations.setId("binaryStorageConfigurations");
				storages.addElement(binaryStorageConfigurations);
				
				DefaultMenuItem sqlConfigurations = new DefaultMenuItem(resourceBundle.getString("repository.sqlConfiguration"));
				sqlConfigurations.setOutcome("sqlConfigurations");
				sqlConfigurations.setId("sqlConfigurations");
				storages.addElement(sqlConfigurations);
				
				DefaultMenuItem neo4jConfigurations = new DefaultMenuItem(resourceBundle.getString("repository.neo4jConfiguration"));
				neo4jConfigurations.setOutcome("neo4jConfigurations");
				neo4jConfigurations.setId("neo4jConfigurations");
				storages.addElement(neo4jConfigurations);
				
				DefaultMenuItem gitRepositories = new DefaultMenuItem(resourceBundle.getString("repository.neo4jConfiguration"));
				gitRepositories.setOutcome("gitRepositories");
				gitRepositories.setId("gitRepositories");
				storages.addElement(gitRepositories);
			}
			
			if (currentUser.hasRole("superAdminManagement")) {
				DefaultMenuItem mavenConfiguration = new DefaultMenuItem(resourceBundle.getString("maven.configuration"));
				mavenConfiguration.setOutcome("mavenConfiguration");
				mavenConfiguration.setId("mavenConfiguration");
				storages.addElement(mavenConfiguration);
			}
		}
		
		
		
	}

}
