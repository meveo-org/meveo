/**
 * 
 */
package org.meveo.service.script.weld;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.manager.InjectionTargetFactoryImpl;
import org.jboss.weld.manager.api.WeldInjectionTarget;
/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class MeveoInjectionTargetFactory<T> extends InjectionTargetFactoryImpl<T> {

	private MeveoBeanManager manager;
	
    protected MeveoInjectionTargetFactory(AnnotatedType<T> type, MeveoBeanManager manager) {
    	super(type, manager.getDelegate());
    	this.manager = manager;
    }

	@Override
	public WeldInjectionTarget<T> createInjectionTarget(Bean<T> bean) {
		return super.createInjectionTarget(bean);
//		WeldInjectionTarget<T> target = super.createInjectionTarget(bean);
//		if(target instanceof BasicInjectionTarget) {
//			return new MeveoInjectionTarget((BasicInjectionTarget) target, manager);
//		} else {
//			throw new UnsupportedOperationException();
//		}
	}
    
    

}

