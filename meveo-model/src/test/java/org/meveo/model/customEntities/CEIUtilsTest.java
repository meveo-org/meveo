/**
 * 
 */
package org.meveo.model.customEntities;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.meveo.interfaces.Entity;
import org.meveo.interfaces.EntityGraph;
import org.meveo.interfaces.EntityRelation;
import org.meveo.model.CustomEntity;
import org.meveo.model.customEntities.annotations.Relation;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.typereferences.GenericTypeReferences;

import com.fasterxml.jackson.core.type.TypeReference;

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
	public void testListPropertyMerge() {
		CustomEntityC c1 = new CustomEntityC();
		c1.setValue("toto");
		c1.getList().add("A");
		
		CustomEntityC c2 = new CustomEntityC();
		c2.setValue("toto");
		c2.getList().add("B");
		
		var graph = CEIUtils.toEntityGraph(List.of(c1, c2));
		assert graph.getEntities().size() == 1;
		
		var col = (Collection) graph.getEntities().get(0).getProperties().get("list");
		assert col.size() == 2;
	}
	
	@Test
	public void testCircularReferencesSameLevel() {
		CustomEntityA entityA = new CustomEntityA();
		entityA.setValue("A");
		
		CustomEntityB entityB1 = new CustomEntityB();
		entityB1.setValue("B1");
		entityB1.setCircularRef(new BtoA(entityB1, entityA));
		entityA.setaToBRelation(new AtoB(entityA, entityB1));
		
		CustomEntityC entityC = new CustomEntityC();
		entityC.setValue("C");
		entityC.setCircularRef(new CtoA(entityC, entityA));
		entityB1.setTarget(entityC);
		
		EntityGraph graph = CEIUtils.toEntityGraph(List.of(entityA, entityB1, entityC));
		
		assert graph.getEntities().size() == 3;
		assert graph.getRelations().size() == 4;
	}
	
	@Test
	public void testCircularReferencesSecondLevel() {
		CustomEntityA entityA = new CustomEntityA();
		entityA.setValue("A");
		
		CustomEntityB entityB1 = new CustomEntityB();
		entityB1.setValue("B1");
		entityA.setaToBRelation(new AtoB(entityA, entityB1));
		
		CustomEntityC entityC = new CustomEntityC();
		entityC.setValue("C");
		entityC.setCircularRef(new CtoA(entityC, entityA));
		entityB1.setTarget(entityC);
		
		EntityGraph graph = CEIUtils.toEntityGraph(List.of(entityA));
		
		assert graph.getEntities().size() == 3;
		assert graph.getRelations().size() == 3;
	}
	
	@Test
	public void testCircularReferencesFirstLevel() {
		CustomEntityA entityA = new CustomEntityA();
		entityA.setValue("A");
		
		CustomEntityB entityB1 = new CustomEntityB();
		entityB1.setValue("B1");
		entityB1.setCircularRef(new BtoA(entityB1, entityA));
		entityA.setaToBRelation(new AtoB(entityA, entityB1));
		
		EntityGraph graph = CEIUtils.toEntityGraph(List.of(entityA));
		
		assert graph.getEntities().size() == 2;
		assert graph.getRelations().size() == 2;
	}
	
	@Test
	public void targetIsEmbeddedSerialization() {
		Entity entity = new Entity.Builder()
				.properties(Map.of(
						"value", "source", 
						"aToBRelation", Map.of("value", "target4")
					))
				.type("CustomEntityA")
				.name("source")
				.build();
		
		var entities = CEIUtils.fromEntityGraph(new EntityGraph(List.of(entity), List.of()));
		
		assert entities.size() == 1;
		
		entities.forEach(e -> {
			var customEntityA = (CustomEntityA) e;
			assert customEntityA.getaToBRelation() != null;
			assert customEntityA.getaToBRelation().getTarget() != null;
		});
	}
	
	@Test
	public void testListRelationshipSerialization() {
		CustomEntityA entityA = new CustomEntityA();
		entityA.setValue("A");
		
		CustomEntityB entityB1 = new CustomEntityB();
		entityB1.setValue("B1");
		
		CustomEntityB entityB2 = new CustomEntityB();
		entityB2.setValue("B2");
		
		AtoBMulti rel1 = new AtoBMulti(entityA, entityB1);
		rel1.setTest("rel1");
		entityA.getaToBmulti().add(rel1);
		
		AtoBMulti rel2 = new AtoBMulti(entityA, entityB2);
		rel2.setTest("rel2");
		entityA.getaToBmulti().add(rel2);
		
		EntityGraph graph = CEIUtils.toEntityGraph(List.of(entityA));
		
		assert graph.getEntities().size() == 3;
		assert graph.getRelations().size() == 2;
	}
	
	@Test
	public void testSingleValueDeserialization() {
		TypeReference<Set<Long>> typeRef = new TypeReference<Set<Long>>() {};
		Integer value = 789;
		Set<Long> collection = JacksonUtil.convert(value, typeRef);
		assert collection.size() == 1;
	}
	
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
		
		Entity targetTarget = new Entity.Builder()
				.properties(Map.of("value", "targetTarget"))
				.type("CustomEntityC")
				.name("targetTarget")
				.build();
		
		EntityRelation relation1 = new EntityRelation.Builder()
				.properties(Map.of())
				.type("HasTarget")
				.name("source-target1")
				.source(source)
				.target(target1)
				.build();
		
		EntityRelation transitiveRelation = new EntityRelation.Builder()
				.properties(Map.of())
				.type("HasCTarget")
				.name("target1-targetTarget")
				.source(target1)
				.target(targetTarget)
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
				List.of(source, target1, target2, target3, target4, target5, target6, targetTarget), 
				List.of(relation1, relation2, relation3, relation4, relation5, relation6, transitiveRelation)
			);
		
		var entities = CEIUtils.fromEntityGraph(graph);
		
		assert entities.size() == 1;

		var sourceEntity = (CustomEntityA) entities.iterator().next();
		
		assert sourceEntity.getTarget() != null;
		assert sourceEntity.getTargets().size() == 2;
		
		assert sourceEntity.getaToBRelation() != null;
		assert sourceEntity.getaToBRelation().getTarget() != null;

		assert sourceEntity.getaToBmulti().size() == 2;
		sourceEntity.getaToBmulti().forEach(rel -> {
			assert rel.getTarget() != null;
		});
		
		assert sourceEntity.getTarget().getTarget() != null;
		
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
