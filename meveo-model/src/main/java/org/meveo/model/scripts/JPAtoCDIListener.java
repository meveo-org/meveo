package org.meveo.model.scripts;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.meveo.event.qualifier.AfterAnyUpdate;
import org.meveo.event.qualifier.BeforeAnyUpdate;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.CreatedAfterTx;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.event.qualifier.UpdatedAfterTx;
import org.meveo.model.BaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A hacky class that will turn JPA events into CDI events so that meveo-ejb
 * module can access them
 * 
 * @author Clement Bareth
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 */
public class JPAtoCDIListener {

	private Logger log = LoggerFactory.getLogger(JPAtoCDIListener.class);

	@Inject
	@Created
	protected Event<Object> entityCreatedEventProducer;

	@Inject
	@Updated
	protected Event<Object> entityUpdatedEventProducer;

	@Inject
	@Removed
	protected Event<Object> entityRemovedEventProducer;
	
	@Inject
	@CreatedAfterTx
	protected Event<Object> entityCreatedAfterTxEventProducer;

	@Inject
	@UpdatedAfterTx
	protected Event<Object> entityUpdatedAfterTxEventProducer;
	
	@Inject
	@org.meveo.event.qualifier.PostRemoved
	protected Event<Object> entityRemovedAfterTxEventProducer;
	

	@PrePersist
	public void created(Object d) {
		entityCreatedEventProducer.fire(d);
	}

	@PreUpdate
	public void updated(Object d) {
		entityUpdatedEventProducer.fire(d);
	}
	
	@PreRemove
	public void removed(Object d) {
		entityRemovedEventProducer.fire(d);
	}
	
	@PostPersist
	public void createdAfterTx(Object d) {
		entityCreatedAfterTxEventProducer.fire(d);
	}

	@PostUpdate
	public void updatedAfterTx(Object d) {
		entityUpdatedAfterTxEventProducer.fire(d);
	}

	@PostRemove
	public void removedAfterTx(Object d) {
		entityRemovedAfterTxEventProducer.fire(d);
	}
	
	
}
