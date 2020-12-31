/**
 * 
 */
package org.meveo.service.script.weld;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.Validator;
import org.jboss.weld.injection.producer.InjectionTargetInitializationContext;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
//public class MeveoInjectionTargetService extends InjectionTargetService {
//
//	/**
//	 * Instantiates a new MeveoInjectionTargetService
//	 *
//	 * @param beanManager
//	 */
//	public MeveoInjectionTargetService(MeveoBeanManager meveoManager, MeveoBeanValidator validator) {
//	       this.validator = beanManager.getServices().get(Validator.class);
//	        this.producersToValidate = new ConcurrentLinkedQueue<Producer<?>>();
//	        this.injectionTargetsToInitialize = new ConcurrentLinkedQueue<InjectionTargetInitializationContext<?>>();
//	        this.container = Container.instance(beanManager);
//	        this.beanManager = beanManager;
//	}
//
//}
