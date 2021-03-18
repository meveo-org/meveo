/**
 * 
 */
package org.meveo.model.persistence;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.meveo.model.CustomEntity;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.annotations.Relation;
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
	public void testEntitiesAreMerged() {
		var a1 = new CustomEntityA();
		var a2 = new CustomEntityA();
		var b1 = new CustomEntityB();
		var b2 = new CustomEntityB();
		
		a1.setValue("a1");
		a2.setValue("a2");
		b1.setValue("b");
		b2.setValue("b");
		b2.setOtherValue("b2");
		a1.setTarget(b1);
		a2.setTarget(b2);
		
		var graph = CEIUtils.toEntityGraph(List.of(a1, a2));
		
		assert graph.getEntities().size() == 3;
		assert graph.getRelations().size() == 2;
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
