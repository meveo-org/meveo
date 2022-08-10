package org.meveo.service.script;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;

import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.EjbUtils;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.11
 */
public abstract class Script implements ScriptInterface {
	
	private Map<Object, Instance<Object>> instancesToDestroy = new HashMap<>();

	/**
	 * Base URL of the application. Maps to app setting with key meveo.admin.baseUrl.
	 */
	public static String APP_BASE_URL = "APP_BASE_URL";
	
	/**
	 * Current provider/tenant
	 */
	public static String CONTEXT_APP_PROVIDER = "CONTEXT_APP_PROVIDER";

	/**
	 * Current user
	 */
	public static String CONTEXT_CURRENT_USER = "CONTEXT_CURRENT_USER";

	/**
	 * GUI redirection after entity custom action execution
	 */
	public static String RESULT_GUI_OUTCOME = "GUI_OUTCOME";

	/**
	 * A key of a message to show after entity custom action execution
	 */
	public static String RESULT_GUI_MESSAGE_KEY = "GUI_MESSAGE_KEY";

	/**
	 * A message to show after entity custom action execution
	 */
	public static String RESULT_GUI_MESSAGE = "GUI_MESSAGE";

	/**
	 * Script return value
	 */
	public static String RESULT_VALUE = "RESULT_VALUE";

	/**
	 * Entity, on which script acts on
	 */
	public static String CONTEXT_ENTITY = "CONTEXT_ENTITY";

	/**
	 * Parent entity of an entity, on which script acts on
	 */
	public static String CONTEXT_PARENT_ENTITY = "CONTEXT_PARENT_ENTITY";

	/**
	 * Entity custom action's code
	 */
	public static String CONTEXT_ACTION = "CONTEXT_ACTION";

	/**
	 * Nb of ok when script is executed by a Job
	 */
	public static String JOB_RESULT_NB_OK = "RESULT_NB_OK";

	/**
	 * Nb of ko when script is executed by a Job
	 */
	public static String JOB_RESULT_NB_KO = "RESULT_NB_KO";

	/**
	 * Nb of warn when script is executed by a Job
	 */
	public static String JOB_RESULT_NB_WARN = "RESULT_NB_WARN";

	/**
	 * Report when script is executed by a Job
	 */
	public static String JOB_RESULT_REPORT = "RESULT_REPORT";

	/**
	 * Summary when script is executed by a Job
	 */
	public static String JOB_RESULT_SUMMARY = "RESULT_SUMMARY";

	/**
	 * Nb of result to process when script is executed by a Job
	 */
	public static String JOB_RESULT_TO_PROCESS = "RESULT_TO_PROCESS";
	
	public static final String REPOSITORY = "REPOSITORY";

	protected Object getServiceInterface(String serviceInterfaceName) {
		return EjbUtils.getServiceInterface(serviceInterfaceName);
	}

	@SuppressWarnings("unchecked")
	protected <T> T getCDIBean(Class<T> cdiBeanClass) {
		Instance<T> instance = CDI.current().select(cdiBeanClass);
		T object = instance.get();
		
		instancesToDestroy.put((Object) object, (Instance<Object>) instance);
		return object;
	}

	@Override
	public void init(Map<String, Object> methodContext) throws BusinessException {

	}

	@Override
	public void execute(Map<String, Object> methodContext) throws BusinessException {

	}

	@Override
	public void finalize(Map<String, Object> methodContext) throws BusinessException {

	}

	/**
	 * Executed after the main {@link #execute(Map)} method.
	 * 
	 * @param methodContext Method variables in a form of a map where
	 *                      CONTEXT_ENTITY=entity to process
	 * @throws BusinessException when post transaction fails
	 */
	public void postCommit(Map<String, Object> methodContext) throws BusinessException {

	}

	/**
	 * Executed when an error is thrown in the {@link #execute(Map)} method.
	 * 
	 * @param methodContext Method variables in a form of a map where
	 *                      CONTEXT_ENTITY=entity to process
	 * @throws BusinessException when rollback fails
	 */
	public void postRollback(Map<String, Object> methodContext) throws BusinessException {

	}
	
	@PreDestroy
	public void preDestroy() {
		var beanManager = CDI.current().getBeanManager();

		try {
			instancesToDestroy.forEach((object, instance) -> {
				var annotatedType = beanManager.createAnnotatedType(object.getClass());
				Set<Bean<?>> beans = beanManager.getBeans(annotatedType.getBaseType());
				beans.stream()
					.filter(bean -> bean.getScope().equals(Dependent.class))
					.findFirst()
					.ifPresent(bean -> {
						instance.destroy(object);
					});
			});
		} catch (Exception e) {
			e.printStackTrace(); // Should not happen
		}
	}
}