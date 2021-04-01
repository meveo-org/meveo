/**
 * 
 */
package org.meveo.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 
 * @author clement.bareth
 * @since 6.14.0
 * @version 6.14.0
 */
public class EntityGraph {

	private List<Entity> entities;
	private List<EntityRelation> relations;

	/**
	 * Instantiates a new EntityGraph
	 *
	 * @param entities entities of the graph
	 * @param relations relations of the graph
	 */
	public EntityGraph(List<Entity> entities, List<EntityRelation> relations) {
		super();
		this.entities = entities;
		this.relations = relations;
	}
	
	/**
	 * Instantiates a new EntityGraph
	 *
	 */
	public EntityGraph() {
		super();
		this.entities = new ArrayList<>();
		this.relations = new ArrayList<>();
	}



	/**
	 * @return the {@link #entities}
	 */
	public List<Entity> getEntities() {
		return entities;
	}

	/**
	 * @param entities the entities to set
	 */
	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}

	/**
	 * @return the {@link #relations}
	 */
	public List<EntityRelation> getRelations() {
		return relations;
	}

	/**
	 * @param relations the relations to set
	 */
	public void setRelations(List<EntityRelation> relations) {
		this.relations = relations;
	}
	
	/**
	 * @return the concatenated list of entities and relations
	 */
	public List<EntityOrRelation> getAll() {
		return Stream.concat(this.entities.stream(), this.relations.stream())
			.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return "EntityGraph [entities=" + entities + ", relations=" + relations + "]";
	}

}
