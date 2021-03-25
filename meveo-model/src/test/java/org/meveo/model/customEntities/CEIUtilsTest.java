/**
 * 
 */
package org.meveo.model.customEntities;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.meveo.interfaces.Entity;
import org.meveo.interfaces.EntityGraph;
import org.meveo.interfaces.EntityRelation;
import org.meveo.model.CustomEntity;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.annotations.Relation;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.typereferences.GenericTypeReferences;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class CEIUtilsTest {
	
	private static String inputStr = "{\r\n" + 
			"    \"investCode\": \"test\",\r\n" + 
			"    \"investName\": \"test\",\r\n" + 
			"    \"businessPerimeter\": {},\r\n" + 
			"    \"myTemplateInputOne\": {\r\n" + 
			"        \"lastName\" : \"test crud name\",\r\n" + 
			"        \"firstNames\" : [\"test crud my firsName\"]\r\n" + 
			"    },\r\n" + 
			"    \"myTemplateInputTwo\": [\r\n" + 
			"        \"test crud string input\"\r\n" + 
			"    ]\r\n" + 
			"    ,\r\n" + 
			"    \"technicalConfiguration\": {\r\n" + 
			"        \"endDate\": \"2020-11-09T12:22:47.452Z\",\r\n" + 
			"        \"investigatorLocality\": \"FR\"\r\n" + 
			"    },\r\n" + 
			"    \"templateCode\": \"testCrud\"\r\n" + 
			"}";
	
	private static Map<String, Object> inputMap = JacksonUtil.fromString(inputStr, GenericTypeReferences.MAP_STRING_OBJECT);
	
	@Test
	public void testConvertGraphToEntities() {
		Entity target1 = new Entity.Builder()
				.properties(Map.of("value", "target1"))
				.type("CustomEntityB")
				.name("target1")
				.build();
		
		Entity target2 = new Entity.Builder()
				.properties(Map.of("value", "target2"))
				.type("CustomEntityB")
				.name("target2")
				.build();
		
		Entity target3 = new Entity.Builder()
				.properties(Map.of("value", "target3"))
				.type("CustomEntityB")
				.name("target3")
				.build();
		
		Entity target4 = new Entity.Builder()
				.properties(Map.of("value", "target4"))
				.type("CustomEntityB")
				.name("target4")
				.build();
		
		Entity target5 = new Entity.Builder()
				.properties(Map.of("value", "target5"))
				.type("CustomEntityB")
				.name("target5")
				.build();
		
		Entity target6 = new Entity.Builder()
				.properties(Map.of("value", "target6"))
				.type("CustomEntityB")
				.name("target6")
				.build();
		
		Entity source = new Entity.Builder()
				.properties(Map.of("value", "source"))
				.type("CustomEntityA")
				.name("source")
				.build();
		
		EntityRelation relation1 = new EntityRelation.Builder()
				.properties(Map.of())
				.type("HasTarget")
				.name("source-target1")
				.source(source)
				.target(target1)
				.build();
		
		EntityRelation relation2 = new EntityRelation.Builder()
				.properties(Map.of())
				.type("HasTargets")
				.name("source-target2")
				.source(source)
				.target(target2)
				.build();
		
		EntityRelation relation3 = new EntityRelation.Builder()
				.properties(Map.of())
				.type("HasTargets")
				.name("source-target3")
				.source(source)
				.target(target3)
				.build();
		
		EntityRelation relation4 = new EntityRelation.Builder()
				.properties(Map.of())
				.type("AtoB")
				.properties(Map.of("test", "test4"))
				.name("source-target4")
				.source(source)
				.target(target4)
				.build();
		
		EntityRelation relation5 = new EntityRelation.Builder()
				.properties(Map.of())
				.type("AtoBMulti")
				.properties(Map.of("test", "relation5"))
				.name("source-target5")
				.source(source)
				.target(target5)
				.build();
		
		EntityRelation relation6 = new EntityRelation.Builder()
				.properties(Map.of())
				.type("AtoBMulti")
				.properties(Map.of("test", "relation6"))
				.name("source-target6")
				.source(source)
				.target(target6)
				.build();
		
		EntityGraph graph = new EntityGraph(
				List.of(source, target1, target2, target3, target4, target5, target6), 
				List.of(relation1, relation2, relation3, relation4, relation5, relation6)
			);
		
		var entities = CEIUtils.fromEntityGraph(graph);
		
		assert entities.size() == 1;

		var sourceEntity = (CustomEntityA) entities.iterator().next();
		
		assert sourceEntity.getTarget() != null;
		assert sourceEntity.getTargets().size() == 2;
		
		assert sourceEntity.getaToBRelation() != null;
		assert sourceEntity.getaToBmulti().size() == 2;
		
		// TODO: test transitive relations
		
	}
	
	@Test
	public void testEntitiesAreMerged() {
		var a1 = new CustomEntityA();
		a1.setValue("a1");
		
		var a2 = new CustomEntityA();
		a2.setValue("a2");

		var b1 = new CustomEntityB();
		b1.setValue("b");
		a1.setTarget(b1);

		var b2 = new CustomEntityB();
		b2.setValue("b");
		b2.setOtherValue("b2");
		a2.setTarget(b2);

		var b3 = new CustomEntityB();
		b3.setValue("b3");
		a1.setaToBRelation(new AtoB(a1, b3));
		
		var graph = CEIUtils.toEntityGraph(List.of(a1, a2));
		
		assert graph.getEntities().size() == 4;
		assert graph.getRelations().size() == 3;
	}
	
	@Test
	public void convertEntitiesToGraph() {
		var subTargetEntity = new CustomEntity() {
			
			private String uuid;
			
			@Override
			public String getUuid() {
				return uuid;
			}
			
			public void setUuid(String uuid) {
				this.uuid = uuid;
			}

			private String value = "I'm a target";
			
			private String getValue() {
				return value;
			}
			
			@Override
			public String getCetCode() {
				return "SubTargetEntity";
			}
			
		};
		
		var targetEntity = new CustomEntity() {
			
			@Relation("BaseToTarget")
			private CustomEntity target = subTargetEntity;
			
			/**
			 * @return the {@link #target}
			 */
			public CustomEntity getTarget() {
				return target;
			}
			
			private String uuid;
			
			@Override
			public String getUuid() {
				return uuid;
			}
			
			public void setUuid(String uuid) {
				this.uuid = uuid;
			}

			private String value = "I'm a target";
			
			private String getValue() {
				return value;
			}
			
			@Override
			public String getCetCode() {
				return "TargetEntity";
			}
			
		};
		
		var baseEntity = new CustomEntity() {
			
			private String uuid;
			
			@Override
			public String getUuid() {
				return uuid;
			}
			
			public void setUuid(String uuid) {
				this.uuid = uuid;
			}
			
			private String toto = "toto";
			
			@Relation("BaseToTarget")
			private CustomEntity target = targetEntity;
			
			
			
			/**
			 * @return the {@link #target}
			 */
			public CustomEntity getTarget() {
				return target;
			}

			/**
			 * @param target the target to set
			 */
			public void setTarget(CustomEntity target) {
				this.target = target;
			}

			/**
			 * @param toto the toto to set
			 */
			public void setToto(String toto) {
				this.toto = toto;
			}

			private String getToto() {
				return toto;
			}

			@Override
			public String getCetCode() {
				return "TestA";
			}

		};
		
		var graph = CEIUtils.toEntityGraph(List.of(baseEntity));
		
		assert graph.getEntities().size() == 3;
		assert graph.getRelations().size() == 2;
	}
	
	@Test
	public void deserializeNestedList() {
		CustomEntityTemplate cet = new CustomEntityTemplate();
		cet.setCode("test");
		
		CustomEntityInstance cei = CEIUtils.fromMap(inputMap, cet);
		Map<String, Object> input = cei.get("myTemplateInputOne");
		var listVal = input.get("firstNames");
		
		assert listVal instanceof List;
	}
}