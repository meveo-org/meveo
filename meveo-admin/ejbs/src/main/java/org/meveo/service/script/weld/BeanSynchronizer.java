/**
 * 
 */
package org.meveo.service.script.weld;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;

import org.jboss.weld.inject.WeldInstance;

@RequestScoped
public class BeanSynchronizer {
	
	private List<Runnable> removeInstances = new ArrayList<>();
	
	public <T> void addDependentInstance(WeldInstance<T> weldInstance, T instance) {
		removeInstances.add(() -> { 
			weldInstance.destroy(instance);
		});
	}
	
	@PreDestroy
	public void preDestroy() {
		removeInstances.forEach(Runnable::run);
	}
	
	
}
