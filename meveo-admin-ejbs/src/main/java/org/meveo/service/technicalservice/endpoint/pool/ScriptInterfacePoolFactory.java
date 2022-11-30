/**
 * 
 */
package org.meveo.service.technicalservice.endpoint.pool;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.inject.Instance;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.service.script.ScriptInterface;

public class ScriptInterfacePoolFactory implements PooledObjectFactory<ScriptInterface> {

	private final Instance<ScriptInterface> instance;
	private final Function function;
	private final Class<ScriptInterface> scriptClass;

	public ScriptInterfacePoolFactory(Function function, Class<ScriptInterface> scriptClass, Instance<ScriptInterface> instance) {
		this.instance = instance;
		this.function = function;
		this.scriptClass = scriptClass;
	}

	@Override
	public void activateObject(PooledObject<ScriptInterface> p) throws Exception {
		p.getObject().resetState();
	}

	@Override
	public void destroyObject(PooledObject<ScriptInterface> p) throws Exception {
		p.getObject().cancel();
		p.getObject().finalize(null);
		instance.destroy(p.getObject());
	}

	@Override
	public PooledObject<ScriptInterface> makeObject() throws Exception {
		ScriptInterface scriptInterface = instance.get();
		scriptInterface.init(null);
		return new DefaultPooledObject<ScriptInterface>(scriptInterface);
	}

	@Override
	public void passivateObject(PooledObject<ScriptInterface> p) throws Exception {
//		if (function instanceof ScriptInstance) {
//			Object[] nullArray = { null };
//			ScriptInstance script = (ScriptInstance) function;
//			script.getSettersNullSafe().forEach(setter -> {
//				ReflectionUtils.getSetterByNameAndSimpleClassName(scriptClass, setter.getMethodName(), setter.getType())
//					.ifPresent(method -> {
//						try {
//							method.invoke(p.getObject(), nullArray);
//						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//							// NOOP
//						}
//					});
//			});
//		}
	}

	@Override
	public boolean validateObject(PooledObject<ScriptInterface> p) {
		return true;
	}

}
