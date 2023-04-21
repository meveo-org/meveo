/**
 * 
 */
package org.meveo.persistence.graphql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.meveo.commons.utils.CamelCaseUtils;
import org.meveo.model.neo4j.Neo4JConfiguration;
import org.meveo.util.Inflector;

/**
 * 
 * @author clement.bareth
 * @since 6.11.0
 * @version 6.15.0
 */
public class GraphQLQueryBuilder {
	
	private final String type;
	private final Map<String, Object> filters = new HashMap<>();
	private final List<String> fields = new ArrayList<>();
	private final Map<String, GraphQLQueryBuilder> subQueries = new HashMap<>();
	private Integer limit;
	private Integer offset;
	private GraphQLQueryBuilder parent;
	private boolean isV3 = true;
	
	public static GraphQLQueryBuilder create(Neo4JConfiguration configuration, String type) {
		return new GraphQLQueryBuilder(configuration, type);
	}
	
	private GraphQLQueryBuilder (Neo4JConfiguration configuration, String type) {
		this.isV3 = configuration.getDbVersion().startsWith("3");
		if (!this.isV3 && type != null) {
			this.type = GraphQLQueryBuilder.toV4QueryType(type);
		} else {
			this.type = type;
		}
	}
	
	public GraphQLQueryBuilder filter(String name, Object value) {
		filters.put(name, value);
		return this;
	}
	
	//TODO: Implement "like" operator where value starts and ends with *
	
	public GraphQLQueryBuilder field(String name) {
		fields.add(name);
		return this;
	}
	
	public GraphQLQueryBuilder field(String name, GraphQLQueryBuilder value) {
		subQueries.put(name, value);
		value.parent = this;
		return this;
	}
	
	public GraphQLQueryBuilder limit(int limit) {
		this.limit = limit;
		return this;
	}
	
	public GraphQLQueryBuilder offset(int offset) {
		this.offset = offset;
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder tabs = new StringBuilder("\t\t");
		StringBuilder tabsMinus = new StringBuilder("\t");

		for(var i = parent; i != null; i = i.parent) {
			tabs.append("\t");
			tabsMinus.append("\t");
		}
		
		String filtersStr;
		if(filters.isEmpty() && limit == null && offset == null) {
			filtersStr = "";
		} else {
		
			String prefixFilter = "(";
			if ((limit != null || offset != null) && !isV3) {
				prefixFilter += "options: { ";
			}
			
			if(limit != null) {
				if (isV3) {
					prefixFilter += "first: " + limit + ", ";
				} else {
					prefixFilter += "limit: " + limit + ", ";
				}
			}
			
			if(offset != null) {
				prefixFilter += "offset: " + offset;
				if (isV3) {
					prefixFilter += ", ";
				} else {
					prefixFilter += " }";
				}
			}
			
			if (filters.isEmpty()) {
				filtersStr = prefixFilter + ")";
			} else {
				if (!isV3) {
					if (offset != null || limit != null) {
						prefixFilter += ", ";
					}
					prefixFilter += "where: {";
				}
				
				filtersStr = filters.entrySet()
						.stream()
						.map(e -> { 
							if(e.getValue() instanceof String) {
								return e.getKey() + ":\"" + e.getValue() + "\"";
							} else {
								return e.getKey() + ":" + e.getValue();
							}
						}).collect(Collectors.joining(",", prefixFilter, ""));
				
				if (!isV3) {
					filtersStr += "}";
				}
				
				filtersStr += ")";
			}
			
		}
		
		List<String> fieldsList = new ArrayList<>(fields);
		fieldsList.addAll(
			subQueries.entrySet()
				.stream()
				.map(e -> e.getKey() + e.getValue().toString())
				.collect(Collectors.toList())
		);
		
		String fieldStr = fieldsList.stream()
				.collect(Collectors.joining("\n" + tabs.toString(), tabs.toString(), "\n" + tabsMinus.toString()));

		if(parent == null) {
			return " {\n\t" + type + " " + filtersStr + " {\n" + fieldStr + "}" + "\n}";
		} else {
			return " " + filtersStr + " {\n" + fieldStr  + "}";
		}
	}
	
	/**
	 * Transform an entity type into a graphql query type.
	 * <br>
	 * Exemple: User -> users
	 * 
	 * @param type the type to transform
	 * @return the transformed type
	 */
	public static String toV4QueryType(String type) {
		String queryType = CamelCaseUtils.camelCase(type, true);
		return Inflector.getInstance().pluralize(queryType);
	}
	
	public static void main (String...strings) {
		System.out.println(toV4QueryType("SPInvestigation"));
		System.out.println(toV4QueryType("Media"));
		System.out.println(toV4QueryType("Criterion"));
	}
	
}
