/**
 * 
 */
package org.meveo.persistence.graphql;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author clement.bareth
 * @since 6.11.0
 * @version 6.11.0
 */
public class GraphQLQueryBuilderTest {
	
	@Test
	public void test() {
		String str = GraphQLQueryBuilder.create("Test")
			.filter("toto", 45)
			.field("field1")
			.field("field2", GraphQLQueryBuilder.create("SubTest")
					.field("subField1")
					.field("subField2")
					.filter("filter2", "testFilter")
			).toString();
				
		System.out.println(str);
	}
}
