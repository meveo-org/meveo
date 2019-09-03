package org.meveo.jpa.event;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.FlushEvent;
import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.EjbUtils;
import org.meveo.service.index.ElasticClient;

/**
 * JPA flush event listener. Flushes pending changes to Elastic Search
 * 
 * @author Andrius Karpavicius
 * @author clement.bareth
 */
public class FlushEventListener implements org.hibernate.event.spi.FlushEventListener {

    private static final long serialVersionUID = -9043373325952642047L;

	@Override
	public void onFlush(FlushEvent event) throws HibernateException {
		ElasticClient elasticClient = (ElasticClient) EjbUtils.getServiceInterface("ElasticClient");
        try {
			elasticClient.flushChanges();
		} catch (BusinessException e) {
			throw new RuntimeException(e);
		}		
	}
	
}