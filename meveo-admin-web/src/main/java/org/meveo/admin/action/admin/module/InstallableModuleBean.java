/**
 * 
 */
package org.meveo.admin.action.admin.module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.seam.international.status.Messages;
import org.meveo.admin.exception.BusinessException;
import org.meveo.model.module.InstallableModule;
import org.meveo.service.admin.impl.MeveoModuleProviderService;
import org.meveo.util.view.MessagesHelper;

@Named
@ViewScoped
public class InstallableModuleBean implements Serializable {

	private static final long serialVersionUID = -3755599942886216780L;

	private transient List<InstallableModule> modules;
	
    @Inject
    protected Messages messages;
    
    private AtomicBoolean refresh = new AtomicBoolean(false);
    
	@Inject
	private transient MeveoModuleProviderService providerService;
	
	public void refresh() {
		refresh.set(true);
		modules = null;
		getModules();
		refresh.set(false);
	}
	
	public List<InstallableModule> getModules() {
		if (modules == null) {
			modules = new ArrayList<>();
			for (var provider : providerService.list()) {
				try {
					var providerModules = providerService.listInstallableModules(provider, refresh.get());
					modules.addAll(providerModules);
				} catch (BusinessException e) {
					MessagesHelper.error(messages, "Cannot retrieve modules from " + provider.getCode(), e);
				}
			}
		}
		return modules;
	}
}
