/**
 * 
 */
package org.meveo.service.script.weld;

import java.util.Collection;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.Validator;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.logging.MessageCallback;
import org.jboss.weld.logging.ValidatorLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.PlugableValidator;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.InjectionPoints;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.collections.WeldCollections;
import org.jboss.weld.util.reflection.Formats;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class MeveoBeanValidator extends Validator {
	
	private Validator validator;
	private MeveoBeanManager meveoBeanManager;

	public MeveoBeanValidator(Validator validator, MeveoBeanManager meveoBeanManager) {
		super(null, null);
		this.validator = validator;
		this.meveoBeanManager = meveoBeanManager;
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return validator.hashCode();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return validator.equals(obj);
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return validator.toString();
	}

    /**
     * Validate an injection point
     *
     * @param ij          the injection point to validate
     * @param beanManager the bean manager
     */
    public void validateInjectionPoint(InjectionPoint ij, BeanManagerImpl beanManager) {
        validateInjectionPointForDefinitionErrors(ij, ij.getBean(), beanManager);
        validateMetadataInjectionPoint(ij, ij.getBean(), ValidatorLogger.INJECTION_INTO_NON_BEAN);
        validateEventMetadataInjectionPoint(ij);
        validateInjectionPointForDeploymentProblems(ij, ij.getBean(), beanManager);
    }

	/**
	 * @param ij
	 * @param bean
	 * @param beanManager
	 * @see org.jboss.weld.bootstrap.Validator#validateInjectionPointForDefinitionErrors(javax.enterprise.inject.spi.InjectionPoint, javax.enterprise.inject.spi.Bean, org.jboss.weld.manager.BeanManagerImpl)
	 */
	public void validateInjectionPointForDefinitionErrors(InjectionPoint ij, Bean<?> bean, BeanManagerImpl beanManager) {
		validator.validateInjectionPointForDefinitionErrors(ij, bean, beanManager);
	}

	/**
	 * @param ij
	 * @param bean
	 * @param messageCallback
	 * @see org.jboss.weld.bootstrap.Validator#validateMetadataInjectionPoint(javax.enterprise.inject.spi.InjectionPoint, javax.enterprise.inject.spi.Bean, org.jboss.weld.logging.MessageCallback)
	 */
	public void validateMetadataInjectionPoint(InjectionPoint ij, Bean<?> bean, MessageCallback<DefinitionException> messageCallback) {
		validator.validateMetadataInjectionPoint(ij, bean, messageCallback);
	}

	/**
	 * @param ip
	 * @see org.jboss.weld.bootstrap.Validator#validateEventMetadataInjectionPoint(javax.enterprise.inject.spi.InjectionPoint)
	 */
	public void validateEventMetadataInjectionPoint(InjectionPoint ip) {
		validator.validateEventMetadataInjectionPoint(ip);
	}
	
	/**
	 * @param producer
	 * @param beanManager
	 * @see org.jboss.weld.bootstrap.Validator#validateProducer(javax.enterprise.inject.spi.Producer, org.jboss.weld.manager.BeanManagerImpl)
	 */
    public void validateProducer(Producer<?> producer, BeanManagerImpl beanManager) {
        for (InjectionPoint injectionPoint : producer.getInjectionPoints()) {
            validateInjectionPoint(injectionPoint, beanManager);
        }
    }

	/**
	 * @param ij
	 * @param bean
	 * @param beanManager
	 * @see org.jboss.weld.bootstrap.Validator#validateInjectionPointForDeploymentProblems(javax.enterprise.inject.spi.InjectionPoint, javax.enterprise.inject.spi.Bean, org.jboss.weld.manager.BeanManagerImpl)
	 */
	public void validateInjectionPointForDeploymentProblems(InjectionPoint ij, Bean<?> bean, BeanManagerImpl beanManager) {
        Set<Bean<?>> rootResolvedBeans = beanManager.getBeanResolver().resolve(beanManager.getBeans(ij));
        if(rootResolvedBeans.isEmpty()) {
    		// Look in meveo beans
    		Set<Bean<?>> resolvedBeans = meveoBeanManager.getBeanResolver().resolve(meveoBeanManager.getBeans(ij));
            if (!isInjectionPointSatisfied(ij, resolvedBeans, meveoBeanManager)) {
                throw ValidatorLogger.LOG.injectionPointHasUnsatisfiedDependencies(
                        ij,
                        Formats.formatAnnotations(ij.getQualifiers()),
                        Formats.formatInjectionPointType(ij.getType()),
                        Formats.formatAsStackTraceElement(ij),
                        InjectionPoints.getUnsatisfiedDependenciesAdditionalInfo(ij, beanManager));
            }
            if (resolvedBeans.size() > 1) {
                throw ValidatorLogger.LOG.injectionPointHasAmbiguousDependencies(
                    ij,
                    Formats.formatAnnotations(ij.getQualifiers()),
                    Formats.formatInjectionPointType(ij.getType()),
                    Formats.formatAsStackTraceElement(ij),
                    WeldCollections.toMultiRowString(resolvedBeans));
            }
        } else {
            // Call delegate
    		validator.validateInjectionPointForDeploymentProblems(ij, bean, beanManager);
        }

	}
	
    private static boolean isInjectionPointSatisfied(InjectionPoint ij, Set<?> resolvedBeans, MeveoBeanManager beanManager) {
        if (ij.getBean() instanceof Decorator<?>) {
            if (beanManager.getEnabled().isDecoratorEnabled(ij.getBean().getBeanClass())) {
                return resolvedBeans.size() > 0;
            } else {
                return true;
            }
        } else {
            return resolvedBeans.size() > 0;
        }
    }

	/**
	 * @param producers
	 * @param beanManager
	 * @see org.jboss.weld.bootstrap.Validator#validateProducers(java.util.Collection, org.jboss.weld.manager.BeanManagerImpl)
	 */
	public void validateProducers(Collection<Producer<?>> producers, BeanManagerImpl beanManager) {
		validator.validateProducers(producers, beanManager);
	}

	/**
	 * @param ij
	 * @param resolvedBean
	 * @param beanManager
	 * @see org.jboss.weld.bootstrap.Validator#validateInjectionPointPassivationCapable(javax.enterprise.inject.spi.InjectionPoint, javax.enterprise.inject.spi.Bean, org.jboss.weld.manager.BeanManagerImpl)
	 */
	public void validateInjectionPointPassivationCapable(InjectionPoint ij, Bean<?> resolvedBean, BeanManagerImpl beanManager) {
		validator.validateInjectionPointPassivationCapable(ij, resolvedBean, beanManager);
	}

	/**
	 * @param ij
	 * @param resolvedBean
	 * @param beanManager
	 * @param bean
	 * @see org.jboss.weld.bootstrap.Validator#validateInterceptorDecoratorInjectionPointPassivationCapable(javax.enterprise.inject.spi.InjectionPoint, javax.enterprise.inject.spi.Bean, org.jboss.weld.manager.BeanManagerImpl, javax.enterprise.inject.spi.Bean)
	 */
	public void validateInterceptorDecoratorInjectionPointPassivationCapable(InjectionPoint ij, Bean<?> resolvedBean, BeanManagerImpl beanManager, Bean<?> bean) {
		validator.validateInterceptorDecoratorInjectionPointPassivationCapable(ij, resolvedBean, beanManager, bean);
	}

	/**
	 * @param manager
	 * @param deployment
	 * @see org.jboss.weld.bootstrap.Validator#validateDeployment(org.jboss.weld.manager.BeanManagerImpl, org.jboss.weld.bootstrap.BeanDeployment)
	 */
	public void validateDeployment(BeanManagerImpl manager, BeanDeployment deployment) {
		validator.validateDeployment(manager, deployment);
	}

	/**
	 * @param manager
	 * @see org.jboss.weld.bootstrap.Validator#validateSpecialization(org.jboss.weld.manager.BeanManagerImpl)
	 */
	public void validateSpecialization(BeanManagerImpl manager) {
		validator.validateSpecialization(manager);
	}

	/**
	 * @param beans
	 * @param manager
	 * @see org.jboss.weld.bootstrap.Validator#validateBeans(java.util.Collection, org.jboss.weld.manager.BeanManagerImpl)
	 */
	public void validateBeans(Collection<? extends Bean<?>> beans, BeanManagerImpl manager) {
		validator.validateBeans(beans, manager);
	}

	/**
	 * @param interceptors
	 * @param manager
	 * @see org.jboss.weld.bootstrap.Validator#validateInterceptors(java.util.Collection, org.jboss.weld.manager.BeanManagerImpl)
	 */
	public void validateInterceptors(Collection<? extends Interceptor<?>> interceptors, BeanManagerImpl manager) {
		validator.validateInterceptors(interceptors, manager);
	}

	/**
	 * @param decorators
	 * @param manager
	 * @see org.jboss.weld.bootstrap.Validator#validateDecorators(java.util.Collection, org.jboss.weld.manager.BeanManagerImpl)
	 */
	public void validateDecorators(Collection<? extends Decorator<?>> decorators, BeanManagerImpl manager) {
		validator.validateDecorators(decorators, manager);
	}

	/**
	 * @param beanManager
	 * @see org.jboss.weld.bootstrap.Validator#validateBeanNames(org.jboss.weld.manager.BeanManagerImpl)
	 */
	public void validateBeanNames(BeanManagerImpl beanManager) {
		validator.validateBeanNames(beanManager);
	}

	/**
	 * 
	 * @see org.jboss.weld.bootstrap.Validator#cleanup()
	 */
	public void cleanup() {
		validator.cleanup();
	}

	/**
	 * @param bean
	 * @return
	 * @see org.jboss.weld.bootstrap.Validator#isResolved(javax.enterprise.inject.spi.Bean)
	 */
	public boolean isResolved(Bean<?> bean) {
		return validator.isResolved(bean);
	}

	/**
	 * 
	 * @see org.jboss.weld.bootstrap.Validator#clearResolved()
	 */
	public void clearResolved() {
		validator.clearResolved();
	}
	
	

}
