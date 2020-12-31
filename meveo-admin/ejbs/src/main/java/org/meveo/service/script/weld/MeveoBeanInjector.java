/**
 * 
 */
package org.meveo.service.script.weld;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.producer.ResourceInjector;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class MeveoBeanInjector<T> extends ResourceInjector<T> {

	private MeveoBeanManager manager;
	
	/**
	 * Instantiates a new MeveoBeanInjector
	 *
	 * @param type
	 * @param bean
	 * @param beanManager
	 */
	protected MeveoBeanInjector(EnhancedAnnotatedType<T> type, Bean<T> bean, MeveoBeanManager beanManager) {
		super(type, bean, beanManager.getDelegate());
		this.manager = manager;
	}

    @Override
    public void inject(final T instance, final CreationalContext<T> ctx, final BeanManagerImpl manager, final SlimAnnotatedType<T> type, final InjectionTarget<T> injectionTarget) {
//        if(instance instanceof ScriptInterface) {
//        	// Injection of meveo managed bean
//        	new InjectionContextImpl<T>(manager, injectionTarget, type, instance) {
//	            @Override
//	            public void proceed() {
//	                Beans.injectFieldsAndInitializers(instance, ctx, manager, getInjectableFields(), getInitializerMethods());
//	            }
//	        }.run();
//        } else {
        	super.inject(instance, ctx, manager, type, injectionTarget);
//
//        }
    }
	
//    public static <T> void injectFieldsAndInitializers(T instance, CreationalContext<T> ctx, MeveoBeanManager beanManager,
//            List<? extends Iterable<? extends FieldInjectionPoint<?, ?>>> injectableFields,
//            List<? extends Iterable<? extends MethodInjectionPoint<?, ?>>> initializerMethods) {
//    	
//        if (injectableFields.size() != initializerMethods.size()) {
//            throw UtilLogger.LOG.invalidQuantityInjectableFieldsAndInitializerMethods(injectableFields, initializerMethods);
//        }
//        
//        for (int i = 0; i < injectableFields.size(); i++) {
//            injectBoundFields(instance, ctx, beanManager, injectableFields.get(i));
//            callInitializers(instance, ctx, beanManager, initializerMethods.get(i));
//        }
//    }
    
    
    /**
     * Injects bound fields
     *
     * @param instance The instance to inject into
     */
//    public static <T> void injectBoundFields(T instance, CreationalContext<T> creationalContext, MeveoBeanManager manager,
//            Iterable<? extends FieldInjectionPoint<?, ?>> injectableFields) {
//    	
//        for (FieldInjectionPoint<?, ?> injectableField : injectableFields) {
//            injectableField.inject(instance, manager, creationalContext);
//        }
//        
//    }
    
    /**
     * Calls all initializers of the bean
     *
     * @param instance The bean instance
     */
//    public static <T> void callInitializers(T instance, CreationalContext<T> creationalContext, MeveoBeanManager manager,
//            Iterable<? extends MethodInjectionPoint<?, ?>> initializerMethods) {
//        for (MethodInjectionPoint<?, ?> initializer : initializerMethods) {
//            initializer.invoke(instance, null, manager, creationalContext, CreationException.class);
//        }
//    }
	
	

}
