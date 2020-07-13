/**
 * 
 */
package org.meveo.event.model;

/**
 * 
 * @author clement.bareth
 * @since
 * @version
 */
public class AttributeUpdateEvent<T, A> {

	private T object;
	private A oldValue;
	private A newValue;

	public AttributeUpdateEvent(T object, A oldValue, A newValue) {
		super();
		this.object = object;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public T getObject() {
		return object;
	}

	public A getOldValue() {
		return oldValue;
	}

	public A getNewValue() {
		return newValue;
	}

}
