/**
 * 
 */
package org.meveo.service.script.weld;

import java.util.ArrayList;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.TypeSafeBeanResolver;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.service.script.ScriptInstanceService;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class MeveoBeanResolver extends TypeSafeBeanResolver {

	private MeveoBeanManager meveoBeanManager;
	private ScriptInstanceService scriptService;
	
	/**
	 * Instantiates a new MeveoBeanResolver
	 *
	 * @param beanManager
	 * @param beans
	 */
	public MeveoBeanResolver(MeveoBeanManager meveoBeanManager, ScriptInstanceService scriptService) {
		super(meveoBeanManager.getDelegate(), meveoBeanManager.getDynamicAccessibleBeans());
		this.meveoBeanManager = meveoBeanManager;
		this.scriptService = scriptService;
	}

	@Override
	protected Iterable<? extends Bean<?>> getAllBeans(Resolvable resolvable) {
		var result = super.getAllBeans(resolvable);
		if(result == null || !result.iterator().hasNext()) {
			result = meveoBeanManager.getMeveoBeans().get(resolvable.getJavaClass().getName());
			if(result == null || !result.iterator().hasNext()) {
				ScriptInstance scriptInstance = scriptService.findByCode(resolvable.getJavaClass().getName());
				if(scriptInstance == null) {
					throw new NullPointerException("Script instance with code " + resolvable.getJavaClass().getName() + " does not exists");
				}
				scriptService.loadClassInCache(scriptInstance.getCode());
				result = meveoBeanManager.getMeveoBeans().get(resolvable.getJavaClass().getName());
			}
		}
		
		if(result == null) {
			result = new ArrayList<>();
		}
		
		return result;
	}

	@Override
	protected boolean matches(Resolvable resolvable, Bean<?> bean) {
		if(resolvable.getJavaClass().getName().equals(bean.getBeanClass().getName())) {
			return true;
		}
		
		return super.matches(resolvable, bean);
	}
	
	
	

}
