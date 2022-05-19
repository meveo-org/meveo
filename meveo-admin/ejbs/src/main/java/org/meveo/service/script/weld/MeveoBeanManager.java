/**
 * 
 */
package org.meveo.service.script.weld;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.enterprise.inject.spi.InterceptionFactory;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProducerFactory;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.bean.proxy.ClientProxyProvider;
import org.jboss.weld.bootstrap.Validator;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.enablement.ModuleEnablement;
import org.jboss.weld.bootstrap.events.ContainerLifecycleEvents;
import org.jboss.weld.context.WeldAlterableContext;
import org.jboss.weld.contexts.AbstractSharedContext;
import org.jboss.weld.contexts.WeldCreationalContext;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.event.ObserverNotifier;
import org.jboss.weld.events.WeldEvent;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.injection.attributes.FieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ParameterInjectionPointAttributes;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.interceptor.reader.InterceptorMetadataReader;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.FieldProducerFactory;
import org.jboss.weld.manager.InjectionTargetFactoryImpl;
import org.jboss.weld.manager.MethodProducerFactory;
import org.jboss.weld.manager.api.WeldInjectionTargetBuilder;
import org.jboss.weld.manager.api.WeldInjectionTargetFactory;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.resolution.AbstractTypeSafeBeanResolver;
import org.jboss.weld.resolution.NameBasedResolver;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeBeanResolver;
import org.jboss.weld.resolution.TypeSafeDecoratorResolver;
import org.jboss.weld.resolution.TypeSafeInterceptorResolver;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.LazyValueHolder;
import org.meveo.service.script.ScriptInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for {@link BeanManagerImpl} used to instantiate scripts
 * 
 * @author clement.bareth
 * @since 6.14.0
 * @version 6.14.0
 */
@Vetoed
public class MeveoBeanManager implements WeldManager {
	
	public static MeveoBeanManager INSTANCE;
	
	private static Logger LOGGER = LoggerFactory.getLogger(MeveoBeanManager.class);
	
	private Instance<ScriptInstanceService> scriptInstanceService = CDI.current().select(ScriptInstanceService.class);
	
	private BeanManagerImpl beanManager;
	private List<Bean<?>> enabledBeans;
	private Set<Bean<?>> beanSet;
	private List<Bean<?>> sharedBeans;
	private Map<Type, ArrayList<Bean<?>>> beansByType;
	private LazyValueHolder<Map<Type, ArrayList<Bean<?>>>> beansByTypeHolder;
	private Map<String, List<Bean<?>>> meveoBeans = new HashMap<>();
	private MeveoBeanResolver meveoBeanResolver;
	private MeveoProxyProvider clientProxyProvider;
	
	private ConcurrentHashMap<Class<?>, Future<Bean<?>>> beanDefinitionTasks = new ConcurrentHashMap<>();
	
	public static MeveoBeanManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new MeveoBeanManager();
		}
		
		return INSTANCE;
	}
	/**
	 * @return the {@link #meveoBeans}
	 */
	public Map<String, List<Bean<?>>> getMeveoBeans() {
		return meveoBeans;
	}

	public BeanManagerImpl getDelegate() {
		return beanManager;
	}
	
	/**
	 * @return
	 * @see org.jboss.weld.manager.api.WeldManager#getActiveContexts()
	 */
	public Collection<Context> getActiveContexts() {
		return beanManager.getActiveContexts();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.api.WeldManager#getActiveWeldAlterableContexts()
	 */
	public Collection<WeldAlterableContext> getActiveWeldAlterableContexts() {
		return beanManager.getActiveWeldAlterableContexts();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getContextId()
	 */
	public String getContextId() {
		return beanManager.getContextId();
	}

	/**
	 * @param accessibleBeanManager
	 * @see org.jboss.weld.manager.BeanManagerImpl#addAccessibleBeanManager(org.jboss.weld.manager.BeanManagerImpl)
	 */
	public void addAccessibleBeanManager(BeanManagerImpl accessibleBeanManager) {
		beanManager.addAccessibleBeanManager(accessibleBeanManager);
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getAccessibleManagers()
	 */
	public HashSet<BeanManagerImpl> getAccessibleManagers() {
		return beanManager.getAccessibleManagers();
	}

	/**
	 * @param beans
	 * @see org.jboss.weld.manager.BeanManagerImpl#addBeans(java.util.Collection)
	 */
	public void addBeans(Collection<? extends Bean<?>> beans) {
		beanManager.addBeans(beans);
	}

	/**
	 * @param bean
	 * @see org.jboss.weld.manager.BeanManagerImpl#addDecorator(javax.enterprise.inject.spi.Decorator)
	 */
	public void addDecorator(Decorator<?> bean) {
		beanManager.addDecorator(bean);
	}

	/**
	 * @param <T>
	 * @param event
	 * @param bindings
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#resolveObserverMethods(java.lang.Object, java.lang.annotation.Annotation[])
	 */
	public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... bindings) {
		return beanManager.resolveObserverMethods(event, bindings);
	}

	/**
	 * @param bean
	 * @see org.jboss.weld.manager.BeanManagerImpl#addInterceptor(javax.enterprise.inject.spi.Interceptor)
	 */
	public void addInterceptor(Interceptor<?> bean) {
		beanManager.addInterceptor(bean);
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getEnabled()
	 */
	public ModuleEnablement getEnabled() {
		return beanManager.getEnabled();
	}

	/**
	 * @param enabled
	 * @see org.jboss.weld.manager.BeanManagerImpl#setEnabled(org.jboss.weld.bootstrap.enablement.ModuleEnablement)
	 */
	public void setEnabled(ModuleEnablement enabled) {
		beanManager.setEnabled(enabled);
	}

	/**
	 * @param bean
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#isBeanEnabled(javax.enterprise.inject.spi.Bean)
	 */
	public boolean isBeanEnabled(Bean<?> bean) {
		return beanManager.isBeanEnabled(bean);
	}

	/**
	 * @param beanType
	 * @param qualifiers
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getBeans(java.lang.reflect.Type, java.lang.annotation.Annotation[])
	 */
	public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
		return beanManager.getBeans(beanType, qualifiers);
	}

	/**
	 * @param beanType
	 * @param qualifiers
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getBeans(java.lang.reflect.Type, java.util.Set)
	 */
	public Set<Bean<?>> getBeans(Type beanType, Set<Annotation> qualifiers) {
		return beanManager.getBeans(beanType, qualifiers);
	}

	/**
	 * @param injectionPoint
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getBeans(javax.enterprise.inject.spi.InjectionPoint)
	 */
	public Set<Bean<?>> getBeans(InjectionPoint injectionPoint) {
		var result = beanManager.getBeans(injectionPoint);
		if(result == null || result.isEmpty()) {
	        result = meveoBeanResolver.resolve(new ResolvableBuilder(injectionPoint, beanManager).create(), false);
		}
		return result;
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getEnterpriseBeans()
	 */
	public Map<EjbDescriptor<?>, SessionBean<?>> getEnterpriseBeans() {
		return beanManager.getEnterpriseBeans();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getBeans()
	 */
	public List<Bean<?>> getBeans() {
		return beanManager.getBeans();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getDecorators()
	 */
	public List<Decorator<?>> getDecorators() {
		return beanManager.getDecorators();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getInterceptors()
	 */
	public List<Interceptor<?>> getInterceptors() {
		return beanManager.getInterceptors();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getDynamicAccessibleBeans()
	 */
	public Iterable<Bean<?>> getDynamicAccessibleBeans() {
		return beanManager.getDynamicAccessibleBeans();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getAccessibleBeans()
	 */
	public Set<Bean<?>> getAccessibleBeans() {
		return beanManager.getAccessibleBeans();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getDynamicAccessibleInterceptors()
	 */
	public Iterable<Interceptor<?>> getDynamicAccessibleInterceptors() {
		return beanManager.getDynamicAccessibleInterceptors();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getDynamicAccessibleDecorators()
	 */
	public Iterable<Decorator<?>> getDynamicAccessibleDecorators() {
		return beanManager.getDynamicAccessibleDecorators();
	}

	/**
	 * @param context
	 * @see org.jboss.weld.manager.BeanManagerImpl#addContext(javax.enterprise.context.spi.Context)
	 */
	public void addContext(Context context) {
		beanManager.addContext(context);
	}

	/**
	 * @param observer
	 * @see org.jboss.weld.manager.BeanManagerImpl#addObserver(javax.enterprise.inject.spi.ObserverMethod)
	 */
	public void addObserver(ObserverMethod<?> observer) {
		beanManager.addObserver(observer);
	}

	/**
	 * @param event
	 * @param qualifiers
	 * @see org.jboss.weld.manager.BeanManagerImpl#fireEvent(java.lang.Object, java.lang.annotation.Annotation[])
	 */
	public void fireEvent(Object event, Annotation... qualifiers) {
		beanManager.fireEvent(event, qualifiers);
	}

	/**
	 * @param scopeType
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getContext(java.lang.Class)
	 */
	public Context getContext(Class<? extends Annotation> scopeType) {
		return beanManager.getContext(scopeType);
	}

	/**
	 * @param scopeType
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getUnwrappedContext(java.lang.Class)
	 */
	public Context getUnwrappedContext(Class<? extends Annotation> scopeType) {
		return beanManager.getUnwrappedContext(scopeType);
	}

	/**
	 * @param scopeType
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#isContextActive(java.lang.Class)
	 */
	public boolean isContextActive(Class<? extends Annotation> scopeType) {
		return beanManager.isContextActive(scopeType);
	}

	/**
	 * @param bean
	 * @param requestedType
	 * @param creationalContext
	 * @param noProxy
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getReference(javax.enterprise.inject.spi.Bean, java.lang.reflect.Type, javax.enterprise.context.spi.CreationalContext, boolean)
	 */
	public Object getReference(Bean<?> bean, Type requestedType, CreationalContext<?> creationalContext, boolean noProxy) {
		return beanManager.getReference(bean, requestedType, creationalContext, noProxy);
	}

	/**
	 * @param bean
	 * @param requestedType
	 * @param creationalContext
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getReference(javax.enterprise.inject.spi.Bean, java.lang.reflect.Type, javax.enterprise.context.spi.CreationalContext)
	 */
	public Object getReference(Bean<?> bean, Type requestedType, CreationalContext<?> creationalContext) {
		return beanManager.getReference(bean, requestedType, creationalContext);
	}

	/**
	 * @param injectionPoint
	 * @param resolvedBean
	 * @param creationalContext
	 * @return
	 * @deprecated
	 * @see org.jboss.weld.manager.BeanManagerImpl#getReference(javax.enterprise.inject.spi.InjectionPoint, javax.enterprise.inject.spi.Bean, javax.enterprise.context.spi.CreationalContext)
	 */
	public Object getReference(InjectionPoint injectionPoint, Bean<?> resolvedBean, CreationalContext<?> creationalContext) {
		return beanManager.getReference(injectionPoint, resolvedBean, creationalContext);
	}

	/**
	 * @param injectionPoint
	 * @param resolvedBean
	 * @param creationalContext
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getInjectableReference(javax.enterprise.inject.spi.InjectionPoint, javax.enterprise.inject.spi.Bean, javax.enterprise.context.spi.CreationalContext)
	 */
	public Object getInjectableReference(InjectionPoint injectionPoint, Bean<?> resolvedBean, CreationalContext<?> creationalContext) {
		return beanManager.getInjectableReference(injectionPoint, resolvedBean, creationalContext);
	}

	/**
	 * @param injectionPoint
	 * @param creationalContext
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getInjectableReference(javax.enterprise.inject.spi.InjectionPoint, javax.enterprise.context.spi.CreationalContext)
	 */
	public Object getInjectableReference(InjectionPoint injectionPoint, CreationalContext<?> creationalContext) {
		return beanManager.getInjectableReference(injectionPoint, creationalContext);
	}

	/**
	 * @param <T>
	 * @param resolvable
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getBean(org.jboss.weld.resolution.Resolvable)
	 */
	public <T> Bean<T> getBean(Resolvable resolvable) {
		return beanManager.getBean(resolvable);
	}

	/**
	 * @param name
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getBeans(java.lang.String)
	 */
	public Set<Bean<?>> getBeans(String name) {
		return beanManager.getBeans(name);
	}

	/**
	 * @param types
	 * @param qualifiers
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#resolveDecorators(java.util.Set, java.lang.annotation.Annotation[])
	 */
	public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers) {
		return beanManager.resolveDecorators(types, qualifiers);
	}

	/**
	 * @param types
	 * @param qualifiers
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#resolveDecorators(java.util.Set, java.util.Set)
	 */
	public List<Decorator<?>> resolveDecorators(Set<Type> types, Set<Annotation> qualifiers) {
		return beanManager.resolveDecorators(types, qualifiers);
	}

	/**
	 * @param type
	 * @param interceptorBindings
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#resolveInterceptors(javax.enterprise.inject.spi.InterceptionType, java.lang.annotation.Annotation[])
	 */
	public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings) {
		return beanManager.resolveInterceptors(type, interceptorBindings);
	}

	/**
	 * @param type
	 * @param interceptorBindings
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#resolveInterceptors(javax.enterprise.inject.spi.InterceptionType, java.util.Collection)
	 */
	public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Collection<Annotation> interceptorBindings) {
		return beanManager.resolveInterceptors(type, interceptorBindings);
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getBeanResolver()
	 */
	public TypeSafeBeanResolver getBeanResolver() {
		return meveoBeanResolver;
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getDecoratorResolver()
	 */
	public TypeSafeDecoratorResolver getDecoratorResolver() {
		return beanManager.getDecoratorResolver();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getInterceptorResolver()
	 */
	public TypeSafeInterceptorResolver getInterceptorResolver() {
		return beanManager.getInterceptorResolver();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getNameBasedResolver()
	 */
	public NameBasedResolver getNameBasedResolver() {
		return beanManager.getNameBasedResolver();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getAccessibleLenientObserverNotifier()
	 */
	public ObserverNotifier getAccessibleLenientObserverNotifier() {
		return beanManager.getAccessibleLenientObserverNotifier();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getGlobalLenientObserverNotifier()
	 */
	public ObserverNotifier getGlobalLenientObserverNotifier() {
		return beanManager.getGlobalLenientObserverNotifier();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getGlobalStrictObserverNotifier()
	 */
	public ObserverNotifier getGlobalStrictObserverNotifier() {
		return beanManager.getGlobalStrictObserverNotifier();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#toString()
	 */
	public String toString() {
		return beanManager.toString();
	}

	/**
	 * @param obj
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return beanManager.equals(obj);
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#hashCode()
	 */
	public int hashCode() {
		return beanManager.hashCode();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getServices()
	 */
	public ServiceRegistry getServices() {
		return beanManager.getServices();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getClientProxyProvider()
	 */
	public ClientProxyProvider getClientProxyProvider() {
		return clientProxyProvider;
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getDynamicAccessibleNamespaces()
	 */
	public Iterable<String> getDynamicAccessibleNamespaces() {
		return beanManager.getDynamicAccessibleNamespaces();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getAccessibleNamespaces()
	 */
	public List<String> getAccessibleNamespaces() {
		return beanManager.getAccessibleNamespaces();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getId()
	 */
	public String getId() {
		return beanManager.getId();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getObservers()
	 */
	public List<ObserverMethod<?>> getObservers() {
		return beanManager.getObservers();
	}

	/**
	 * @param <T>
	 * @param type
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createInjectionTarget(javax.enterprise.inject.spi.AnnotatedType)
	 */
	public <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> type) {
		return beanManager.createInjectionTarget(type);
	}

	/**
	 * @param <T>
	 * @param descriptor
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createInjectionTarget(org.jboss.weld.ejb.spi.EjbDescriptor)
	 */
	public <T> InjectionTarget<T> createInjectionTarget(EjbDescriptor<T> descriptor) {
		return beanManager.createInjectionTarget(descriptor);
	}

	/**
	 * @param ij
	 * @see org.jboss.weld.manager.BeanManagerImpl#validate(javax.enterprise.inject.spi.InjectionPoint)
	 */
	public void validate(InjectionPoint ij) {
		beanManager.validate(ij);
	}

	/**
	 * @param bindingType
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getInterceptorBindingDefinition(java.lang.Class)
	 */
	public Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> bindingType) {
		return beanManager.getInterceptorBindingDefinition(bindingType);
	}

	/**
	 * @param id
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getPassivationCapableBean(java.lang.String)
	 */
	public Bean<?> getPassivationCapableBean(String id) {
		return beanManager.getPassivationCapableBean(id);
	}

	/**
	 * @param identifier
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getPassivationCapableBean(org.jboss.weld.serialization.spi.BeanIdentifier)
	 */
	public Bean<?> getPassivationCapableBean(BeanIdentifier identifier) {
		return beanManager.getPassivationCapableBean(identifier);
	}

	/**
	 * @param stereotype
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getStereotypeDefinition(java.lang.Class)
	 */
	public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype) {
		return beanManager.getStereotypeDefinition(stereotype);
	}

	/**
	 * @param annotationType
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#isQualifier(java.lang.Class)
	 */
	public boolean isQualifier(Class<? extends Annotation> annotationType) {
		return beanManager.isQualifier(annotationType);
	}

	/**
	 * @param annotationType
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#isInterceptorBinding(java.lang.Class)
	 */
	public boolean isInterceptorBinding(Class<? extends Annotation> annotationType) {
		return beanManager.isInterceptorBinding(annotationType);
	}

	/**
	 * @param annotationType
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#isNormalScope(java.lang.Class)
	 */
	public boolean isNormalScope(Class<? extends Annotation> annotationType) {
		return beanManager.isNormalScope(annotationType);
	}

	/**
	 * @param annotationType
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#isPassivatingScope(java.lang.Class)
	 */
	public boolean isPassivatingScope(Class<? extends Annotation> annotationType) {
		return beanManager.isPassivatingScope(annotationType);
	}

	/**
	 * @param annotationType
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#isScope(java.lang.Class)
	 */
	public boolean isScope(Class<? extends Annotation> annotationType) {
		return beanManager.isScope(annotationType);
	}

	/**
	 * @param annotationType
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#isStereotype(java.lang.Class)
	 */
	public boolean isStereotype(Class<? extends Annotation> annotationType) {
		return beanManager.isStereotype(annotationType);
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getELResolver()
	 */
	public ELResolver getELResolver() {
		return beanManager.getELResolver();
	}

	/**
	 * @param expressionFactory
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#wrapExpressionFactory(javax.el.ExpressionFactory)
	 */
	public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory) {
		return beanManager.wrapExpressionFactory(expressionFactory);
	}

	/**
	 * @param <T>
	 * @param contextual
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createCreationalContext(javax.enterprise.context.spi.Contextual)
	 */
	public <T> WeldCreationalContext<T> createCreationalContext(Contextual<T> contextual) {
		return beanManager.createCreationalContext(contextual);
	}

	/**
	 * @param <T>
	 * @param type
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createAnnotatedType(java.lang.Class)
	 */
	public <T> AnnotatedType<T> createAnnotatedType(Class<T> type) {
		return beanManager.createAnnotatedType(type);
	}

	/**
	 * @param <T>
	 * @param type
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createEnhancedAnnotatedType(java.lang.Class)
	 */
	public <T> EnhancedAnnotatedType<T> createEnhancedAnnotatedType(Class<T> type) {
		return beanManager.createEnhancedAnnotatedType(type);
	}

	/**
	 * @param <T>
	 * @param type
	 * @param id
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createAnnotatedType(java.lang.Class, java.lang.String)
	 */
	public <T> AnnotatedType<T> createAnnotatedType(Class<T> type, String id) {
		return beanManager.createAnnotatedType(type, id);
	}

	/**
	 * @param <T>
	 * @param type
	 * @param id
	 * @see org.jboss.weld.manager.BeanManagerImpl#disposeAnnotatedType(java.lang.Class, java.lang.String)
	 */
	public <T> void disposeAnnotatedType(Class<T> type, String id) {
		beanManager.disposeAnnotatedType(type, id);
	}

	/**
	 * @param <X>
	 * @param beans
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#resolve(java.util.Set)
	 */
	public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
		return beanManager.resolve(beans);
	}

	/**
	 * @param <T>
	 * @param beanName
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getEjbDescriptor(java.lang.String)
	 */
	public <T> EjbDescriptor<T> getEjbDescriptor(String beanName) {
		return beanManager.getEjbDescriptor(beanName);
	}

	/**
	 * @param <T>
	 * @param descriptor
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getBean(org.jboss.weld.ejb.spi.EjbDescriptor)
	 */
	public <T> SessionBean<T> getBean(EjbDescriptor<T> descriptor) {
		return beanManager.getBean(descriptor);
	}

	/**
	 * 
	 * @see org.jboss.weld.manager.BeanManagerImpl#cleanup()
	 */
	public void cleanup() {
		beanManager.cleanup();
	}

	/**
	 * 
	 * @see org.jboss.weld.manager.BeanManagerImpl#cleanupAfterBoot()
	 */
	public void cleanupAfterBoot() {
		beanManager.cleanupAfterBoot();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getInterceptorModelRegistry()
	 */
	public ConcurrentMap<SlimAnnotatedType<?>, InterceptionModel> getInterceptorModelRegistry() {
		return beanManager.getInterceptorModelRegistry();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getInterceptorMetadataReader()
	 */
	public InterceptorMetadataReader getInterceptorMetadataReader() {
		return beanManager.getInterceptorMetadataReader();
	}

	/**
	 * @param <X>
	 * @param annotatedType
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#fireProcessInjectionTarget(javax.enterprise.inject.spi.AnnotatedType)
	 */
	public <X> InjectionTarget<X> fireProcessInjectionTarget(AnnotatedType<X> annotatedType) {
		return beanManager.fireProcessInjectionTarget(annotatedType);
	}

	/**
	 * @param <X>
	 * @param annotatedType
	 * @param injectionTarget
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#fireProcessInjectionTarget(javax.enterprise.inject.spi.AnnotatedType, javax.enterprise.inject.spi.InjectionTarget)
	 */
	public <X> InjectionTarget<X> fireProcessInjectionTarget(AnnotatedType<X> annotatedType, InjectionTarget<X> injectionTarget) {
		return beanManager.fireProcessInjectionTarget(annotatedType, injectionTarget);
	}

	/**
	 * @param annotations
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#extractInterceptorBindingsForQualifierInstance(java.lang.Iterable)
	 */
	public Set<QualifierInstance> extractInterceptorBindingsForQualifierInstance(Iterable<QualifierInstance> annotations) {
		return beanManager.extractInterceptorBindingsForQualifierInstance(annotations);
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#instance()
	 */
	public Instance<Object> instance() {
		return beanManager.instance();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#event()
	 */
	public WeldEvent<Object> event() {
		return beanManager.event();
	}

	/**
	 * @param <T>
	 * @param ctx
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getInstance(javax.enterprise.context.spi.CreationalContext)
	 */
	public <T> WeldInstance<Object> getInstance(CreationalContext<?> ctx) {
		return beanManager.getInstance(ctx);
	}

	/**
	 * @param <T>
	 * @param type
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createBeanAttributes(javax.enterprise.inject.spi.AnnotatedType)
	 */
	public <T> BeanAttributes<T> createBeanAttributes(AnnotatedType<T> type) {
		return beanManager.createBeanAttributes(type);
	}

	/**
	 * @param member
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createBeanAttributes(javax.enterprise.inject.spi.AnnotatedMember)
	 */
	public BeanAttributes<?> createBeanAttributes(AnnotatedMember<?> member) {
		return beanManager.createBeanAttributes(member);
	}

	/**
	 * @param <X>
	 * @param member
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#internalCreateBeanAttributes(javax.enterprise.inject.spi.AnnotatedMember)
	 */
	public <X> BeanAttributes<?> internalCreateBeanAttributes(AnnotatedMember<X> member) {
		return beanManager.internalCreateBeanAttributes(member);
	}

	/**
	 * @param <T>
	 * @param attributes
	 * @param beanClass
	 * @param injectionTargetFactory
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createBean(javax.enterprise.inject.spi.BeanAttributes, java.lang.Class, javax.enterprise.inject.spi.InjectionTargetFactory)
	 */
	public <T> Bean<T> createBean(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTargetFactory<T> injectionTargetFactory) {
		return beanManager.createBean(attributes, beanClass, injectionTargetFactory);
	}

	/**
	 * @param <T>
	 * @param <X>
	 * @param attributes
	 * @param beanClass
	 * @param producerFactory
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createBean(javax.enterprise.inject.spi.BeanAttributes, java.lang.Class, javax.enterprise.inject.spi.ProducerFactory)
	 */
	public <T, X> Bean<T> createBean(BeanAttributes<T> attributes, Class<X> beanClass, ProducerFactory<X> producerFactory) {
		return beanManager.createBean(attributes, beanClass, producerFactory);
	}

	/**
	 * @param field
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createInjectionPoint(javax.enterprise.inject.spi.AnnotatedField)
	 */
	public FieldInjectionPointAttributes<?, ?> createInjectionPoint(AnnotatedField<?> field) {
		return beanManager.createInjectionPoint(field);
	}

	/**
	 * @param parameter
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createInjectionPoint(javax.enterprise.inject.spi.AnnotatedParameter)
	 */
	public ParameterInjectionPointAttributes<?, ?> createInjectionPoint(AnnotatedParameter<?> parameter) {
		return beanManager.createInjectionPoint(parameter);
	}

	/**
	 * @param <T>
	 * @param extensionClass
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getExtension(java.lang.Class)
	 */
	public <T extends Extension> T getExtension(Class<T> extensionClass) {
		return beanManager.getExtension(extensionClass);
	}

	/**
	 * @param <T>
	 * @param ctx
	 * @param clazz
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createInterceptionFactory(javax.enterprise.context.spi.CreationalContext, java.lang.Class)
	 */
	public <T> InterceptionFactory<T> createInterceptionFactory(CreationalContext<T> ctx, Class<T> clazz) {
		return beanManager.createInterceptionFactory(ctx, clazz);
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getEvent()
	 */
	public Event<Object> getEvent() {
		return beanManager.getEvent();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getContainerLifecycleEvents()
	 */
	public ContainerLifecycleEvents getContainerLifecycleEvents() {
		return beanManager.getContainerLifecycleEvents();
	}

	/**
	 * @param qualifier1
	 * @param qualifier2
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#areQualifiersEquivalent(java.lang.annotation.Annotation, java.lang.annotation.Annotation)
	 */
	public boolean areQualifiersEquivalent(Annotation qualifier1, Annotation qualifier2) {
		return beanManager.areQualifiersEquivalent(qualifier1, qualifier2);
	}

	/**
	 * @param interceptorBinding1
	 * @param interceptorBinding2
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#areInterceptorBindingsEquivalent(java.lang.annotation.Annotation, java.lang.annotation.Annotation)
	 */
	public boolean areInterceptorBindingsEquivalent(Annotation interceptorBinding1, Annotation interceptorBinding2) {
		return beanManager.areInterceptorBindingsEquivalent(interceptorBinding1, interceptorBinding2);
	}

	/**
	 * @param qualifier
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getQualifierHashCode(java.lang.annotation.Annotation)
	 */
	public int getQualifierHashCode(Annotation qualifier) {
		return beanManager.getQualifierHashCode(qualifier);
	}

	/**
	 * @param interceptorBinding
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getInterceptorBindingHashCode(java.lang.annotation.Annotation)
	 */
	public int getInterceptorBindingHashCode(Annotation interceptorBinding) {
		return beanManager.getInterceptorBindingHashCode(interceptorBinding);
	}

	/**
	 * @param <T>
	 * @param type
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getLocalInjectionTargetFactory(javax.enterprise.inject.spi.AnnotatedType)
	 */
	public <T> InjectionTargetFactoryImpl<T> getLocalInjectionTargetFactory(AnnotatedType<T> type) {
		return beanManager.getLocalInjectionTargetFactory(type);
	}

	/**
	 * @param <T>
	 * @param type
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getInjectionTargetFactory(javax.enterprise.inject.spi.AnnotatedType)
	 */
    @Override
    public <T> WeldInjectionTargetFactory<T> getInjectionTargetFactory(AnnotatedType<T> type) {
        return beanManager.getInjectionTargetFactory(type);
    }

	/**
	 * @param <X>
	 * @param field
	 * @param declaringBean
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getProducerFactory(javax.enterprise.inject.spi.AnnotatedField, javax.enterprise.inject.spi.Bean)
	 */
	public <X> FieldProducerFactory<X> getProducerFactory(AnnotatedField<? super X> field, Bean<X> declaringBean) {
		return beanManager.getProducerFactory(field, declaringBean);
	}

	/**
	 * @param <X>
	 * @param method
	 * @param declaringBean
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getProducerFactory(javax.enterprise.inject.spi.AnnotatedMethod, javax.enterprise.inject.spi.Bean)
	 */
	public <X> MethodProducerFactory<X> getProducerFactory(AnnotatedMethod<? super X> method, Bean<X> declaringBean) {
		return beanManager.getProducerFactory(method, declaringBean);
	}

	/**
	 * @param <T>
	 * @param type
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createInjectionTargetBuilder(javax.enterprise.inject.spi.AnnotatedType)
	 */
	public <T> WeldInjectionTargetBuilder<T> createInjectionTargetBuilder(AnnotatedType<T> type) {
		return beanManager.createInjectionTargetBuilder(type);
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#createInstance()
	 */
	public WeldInstance<Object> createInstance() {
		return beanManager.createInstance();
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#unwrap()
	 */
	public BeanManagerImpl unwrap() {
		return beanManager.unwrap();
	}

	/**
	 * @param payload
	 * @see org.jboss.weld.manager.BeanManagerImpl#fireRequestContextInitialized(java.lang.Object)
	 */
	public void fireRequestContextInitialized(Object payload) {
		beanManager.fireRequestContextInitialized(payload);
	}

	/**
	 * @param payload
	 * @see org.jboss.weld.manager.BeanManagerImpl#fireRequestContextBeforeDestroyed(java.lang.Object)
	 */
	public void fireRequestContextBeforeDestroyed(Object payload) {
		beanManager.fireRequestContextBeforeDestroyed(payload);
	}

	/**
	 * @param payload
	 * @see org.jboss.weld.manager.BeanManagerImpl#fireRequestContextDestroyed(java.lang.Object)
	 */
	public void fireRequestContextDestroyed(Object payload) {
		beanManager.fireRequestContextDestroyed(payload);
	}

	/**
	 * @param callback
	 * @see org.jboss.weld.manager.BeanManagerImpl#addValidationFailureCallback(java.util.function.BiConsumer)
	 */
	public void addValidationFailureCallback(BiConsumer<Exception, Environment> callback) {
		beanManager.addValidationFailureCallback(callback);
	}

	/**
	 * @param failure
	 * @param environment
	 * @see org.jboss.weld.manager.BeanManagerImpl#validationFailed(java.lang.Exception, org.jboss.weld.bootstrap.api.Environment)
	 */
	public void validationFailed(Exception failure, Environment environment) {
		beanManager.validationFailed(failure, environment);
	}

	/**
	 * @return
	 * @see org.jboss.weld.manager.BeanManagerImpl#getScopes()
	 */
	public Collection<Class<? extends Annotation>> getScopes() {
		return beanManager.getScopes();
	}

	@SuppressWarnings("unchecked")
	private MeveoBeanManager() {
        try {
        	BeanManagerProxy managerProxy = (BeanManagerProxy) CDI.current().getBeanManager();
        	BeanManagerImpl manager = managerProxy.unwrap();
        	
        	var services = manager.getServices();
            var validator = services.get(Validator.class);
            services.add(Validator.class, new MeveoBeanValidator(validator, this));
            services.add(InjectionTargetService.class, new InjectionTargetService(manager));
            
            beanManager = BeanManagerImpl.newManager(manager, "MeveoBeanManager", services);
            beanManager.addAccessibleBeanManager(manager);
            
            clientProxyProvider = new MeveoProxyProvider(beanManager.getContextId());
            
            Field clientProxyProviderField = BeanManagerImpl.class.getDeclaredField("clientProxyProvider");
            clientProxyProviderField.setAccessible(true);
            clientProxyProviderField.set(beanManager, clientProxyProvider);
            
            Field enabledBeansField = BeanManagerImpl.class.getDeclaredField("enabledBeans");
            enabledBeansField.setAccessible(true);
            enabledBeans = (List<Bean<?>>) enabledBeansField.get(beanManager);
            
            Field sharedBeansField = BeanManagerImpl.class.getDeclaredField("sharedBeans");
            sharedBeansField.setAccessible(true);
            sharedBeans = (List<Bean<?>>) sharedBeansField.get(beanManager);
            
            Field beanSetField = BeanManagerImpl.class.getDeclaredField("beanSet");
            beanSetField.setAccessible(true);
            beanSet = (Set<Bean<?>>) beanSetField.get(beanManager);
            
            Field beanResolverField = BeanManagerImpl.class.getDeclaredField("beanResolver");
            beanResolverField.setAccessible(true);
            var beanResolver = (TypeSafeBeanResolver) beanResolverField.get(beanManager);

            Field beansByTypeField = AbstractTypeSafeBeanResolver.class.getDeclaredField("beansByType");
            beansByTypeField.setAccessible(true);
            beansByTypeHolder = (LazyValueHolder<Map<Type, ArrayList<Bean<?>>>>) beansByTypeField.get(beanResolver);
            beansByType = beansByTypeHolder.get();
            
            meveoBeanResolver = new MeveoBeanResolver(this, scriptInstanceService.get());
            beanResolverField.set(beanManager, meveoBeanResolver);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
        
	}
	
	/**
	 * @param code
	 */
	public List<Bean<?>> removeBean(String code) {
		enabledBeans.removeIf(b -> b.getBeanClass().getName().equals(code));
		beanSet.removeIf(b -> b.getBeanClass().getName().equals(code));
		sharedBeans.removeIf(b -> b.getBeanClass().getName().equals(code));
        List<Bean<?>> beans = meveoBeans.computeIfAbsent(code, clazz -> new ArrayList<>());
		beans.clear();
		meveoBeanResolver.clear();
		return beans;
	}
	
	/**
	 * Add the bean to the bean manager
	 * 
	 * @param <T> type of the bean
	 * @param bean the bean to add
	 */
	private <T> void addBean(Bean<T> bean) {
		// Need to drop previous instance of bean
        List<Bean<?>> beans = removeBean(bean.getBeanClass().getName());
        beans.add(bean);
		beanManager.addBean(bean);
        
        // Clean application context
        Context context = beanManager.getContext(bean.getScope());
        if(context instanceof AbstractSharedContext) {
        	var sharedContext = (AbstractSharedContext) context;
        	sharedContext.destroy(bean);
        }
        
        beansByTypeHolder.clear();
        beansByTypeHolder.get();
	}
	
	/**
	 * Create a bean definition for the given type
	 * 
	 * @param <T> Bean type
	 * @param type Script class
	 * @return a bean definition for the given type
	 */
	@SuppressWarnings("unchecked")
	public <T> Bean<T> createBean(Class<T> type) {
		try {
			Bean<T> returnedBean = (Bean<T>) beanDefinitionTasks.computeIfAbsent(type, clazz -> {
				return CompletableFuture.supplyAsync(() -> {
					AnnotatedType<T> oat = createAnnotatedType(type);
					var classTransformer = ClassTransformer.instance(beanManager);
					// Drop the type definition so we can reload it later if the script is re-compiled
					classTransformer.disposeBackedAnnotatedType(type, beanManager.getId(), null);
					
					BeanAttributes<T> oa = createBeanAttributes(oat);
					InjectionTargetFactory<T> factory = getInjectionTargetFactory(oat);
					Bean<T> bean = createBean(oa, type, factory);
					
					addBean(bean);

					return bean;
				});
			}).get();
			
			beanDefinitionTasks.remove(type);
			
			return returnedBean;
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("Failed to define bean for {}", type, e);
			throw new RuntimeException(e);
		}
	}
	
	public <T> T getInstance(Bean<T> bean, Class<T> scriptClass) {
		WeldInstance<T> weldInstance = getWeldInstance(bean, scriptClass);
		T instance = weldInstance.get();
		
		if (bean.getScope().equals(Dependent.class)) {
			BeanSynchronizer beanSynchronizer = CDI.current().select(BeanSynchronizer.class).get();
			beanSynchronizer.addDependentInstance(weldInstance, instance);
		}
		
		return instance;
	}
	
	public <T> WeldInstance<T> getWeldInstance(Bean<T> bean, Class<T> scriptClass) {
		WeldCreationalContext<T> createCreationalContext = createCreationalContext(bean);
		return getInstance(createCreationalContext).select(scriptClass);
	}
	
	public <T> T getInstance(Bean<T> bean) {
		Context context = getContext(bean.getScope());
		WeldCreationalContext<T> createCreationalContext = createCreationalContext(bean);
		return context.get(bean, createCreationalContext);
	}
		
}
