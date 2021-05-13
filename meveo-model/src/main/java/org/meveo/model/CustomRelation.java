/**
 * 
 */
package org.meveo.model;

import java.util.List;

/**
 * Interface that the classes generated for each CET implements
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 * @param <Source> source type of the relation
 * @param <Target> target type of the relation
 */
public interface CustomRelation<Source extends CustomEntity, Target extends CustomEntity> {
	
	/** @return the code of the corresponding CET */
	String getCrtCode();
	
	/** @return the uuid of the entity */
	String getUuid();
	
	/**
	 * @return the source of the relation
	 */
	Source getSource();
	
	/**
	 * @return the target of the relation
	 */
	Target getTarget();
	
	void setSource(Source source);
	
	void setTarget(Target target);
	
}
