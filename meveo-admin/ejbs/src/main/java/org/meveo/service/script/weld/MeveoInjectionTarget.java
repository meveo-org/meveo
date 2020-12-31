/**
 * 
 */
package org.meveo.service.script.weld;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class MeveoInjectionTarget<T> extends BasicInjectionTarget<T> {

	private MeveoBeanManager manager;
	
	/**
	 * Instantiates a new MeveoInjectionTarget
	 *
	 * @param type
	 * @param bean
	 * @param beanManager
	 * @param instantiator
	 */
	protected MeveoInjectionTarget(BasicInjectionTarget bit, MeveoBeanManager manager) {
		super((EnhancedAnnotatedType<T>) bit.getAnnotatedType(), bit.getBean(), manager.getDelegate(), getInjector(bit, manager), bit.getLifecycleCallbackInvoker(), bit.getInstantiator());
		this.manager = manager;
	}
	
	private static MeveoBeanInjector getInjector(BasicInjectionTarget bit, MeveoBeanManager manager) {
		return new MeveoBeanInjector((EnhancedAnnotatedType) bit.getAnnotatedType(), bit.getBean(), manager);
	}

}
