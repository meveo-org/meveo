package org.meveo.model.scripts;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.BaseEntity;

import java.io.Serializable;

/**
 * A hacky class that will turn JPA events into CDI events so that meveo-ejb module can access them
 * @author Clement Bareth
 */
public class JPAtoCDIListener {

    @Inject
    @Created
    protected Event<Object> entityCreatedEventProducer;

	@Inject
	@Updated
	protected Event<Object> entityUpdatedEventProducer;

	@Inject
	@Removed
	protected Event<Object> entityRemovedEventProducer;

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

}
