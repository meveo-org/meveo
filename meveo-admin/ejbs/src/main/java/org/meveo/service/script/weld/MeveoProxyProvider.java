/**
 * 
 */
package org.meveo.service.script.weld;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.bean.proxy.ClientProxyProvider;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class MeveoProxyProvider extends ClientProxyProvider {

	/**
	 * Instantiates a new MeveoProxyProvider
	 *
	 * @param contextId
	 */
	public MeveoProxyProvider(String contextId) {
		super(contextId);
	}

	@Override
	public <T> T getClientProxy(Bean<T> bean, Type requestedType) {
		// Don't use a CDI proxy
		return MeveoBeanManager.getInstance().getInstance(bean);
	}

}
