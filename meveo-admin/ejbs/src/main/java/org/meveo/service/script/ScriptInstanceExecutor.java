/**
 * 
 */
package org.meveo.service.script;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;

import org.meveo.admin.exception.ScriptExecutionException;

/**
 * Transactional-aware script engine executor
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ScriptInstanceExecutor implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Resource
	private transient UserTransaction userTx;
	
	public void executeNoTx(ScriptInterface engine, Map<String, Object> context) throws ScriptExecutionException {
        try {
			engine.execute(context);
		} catch (Throwable e) {
			throw new ScriptExecutionException(engine.getClass().getName(), "execute", e);
		}
	}
	
	public void executeManualTx(ScriptInterface engine, Map<String, Object> context) throws ScriptExecutionException {
        try {
        	context.put("userTx", userTx);
			engine.execute(context);
		} catch (Throwable e) {
			throw new ScriptExecutionException(engine.getClass().getName(), "execute", e);
		}
	}
	
	public void executeInNewTx(ScriptInterface engine, Map<String, Object> context) throws ScriptExecutionException {
        
		try {
			userTx.begin();
			engine.execute(context);
			userTx.commit();
		} catch (Throwable e) {
			throw new ScriptExecutionException(engine.getClass().getName(), "execute", e);
		}
		
	}
	
}
