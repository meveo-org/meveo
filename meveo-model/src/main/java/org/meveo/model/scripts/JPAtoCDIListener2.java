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
public class JPAtoCDIListener2 {

	@Inject
	@BeforeAnyUpdate
	protected Event<Object> beforeAnyUpdate;
	
	@Inject
	@AfterAnyUpdate
	protected Event<Object> afterAnyUpdate;
	
	@PrePersist
	@PreUpdate
	@PreRemove
	public void beforeAnyUpdate(Object d) {
		beforeAnyUpdate.fire(d);
	}
	
	@PostUpdate
	@PostPersist
	@PostRemove
	public void afterAnyUpdate(Object d) {
		afterAnyUpdate.fire(d);
	}
	
}
