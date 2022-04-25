/**
 * 
 */
package org.meveo.model.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.interfaces.EntityGraph;
import org.meveo.interfaces.EntityRelation;
import org.meveo.model.CustomEntity;
import org.meveo.model.CustomRelation;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.EntityReferenceWrapper;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.annotations.Relation;
import org.meveo.model.typereferences.GenericTypeReferences;
import org.meveo.security.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Utilitary class for manipulating {@link CustomEntityInstance}
 * 
 * @author clement.bareth
 * @since 6.8.0
 * @version 6.12.0
 */
public class CEIUtils {

	private static Logger LOG = LoggerFactory.getLogger(CEIUtils.class);

	/**
	 * @param cei  The entity to hash
	 * @param cfts Custom fields of the entity template
	 * @return a hash of the cei based on the uuid and the clear fields
	 */
	public static String getHash(CustomEntityInstance cei, Map<String, CustomFieldTemplate> cfts) {
		List<Object> objectsToHash = new ArrayList<>();
		if (cei.getUuid() == null) {
			throw new IllegalArgumentException("Can't hash a CEI without uuid");
		}
		objectsToHash.add(cei.getUuid());

		var values = cei.getCfValuesAsValues();
		values.entrySet().forEach(e -> {
			if (e.getValue() instanceof Instant) {
				e.setValue(((Instant) e.getValue()).toEpochMilli());
			}
		});
		cfts.values().stream()
			.sorted((cft1, cft2) -> cft1.getCode().compareTo(cft2.getCode()))
			.filter(cft -> !cft.getFieldType().equals(CustomFieldTypeEnum.SECRET))
			.map(cft -> values.get(cft.getCode()))
			.filter(java.util.Objects::nonNull)
			.forEach(objectsToHash::add);

		return PasswordUtils.getSalt(objectsToHash.toArray());
	}

	/**
	 * @param entityGraph Initial graph to be converted
	 * @return the entities extracted from the graph
	 */
	@SuppressWarnings("unchecked")
	public static Collection<CustomEntity> fromEntityGraph(EntityGraph entityGraph) {
		Map<String, CustomEntity> entities = new HashMap<>();
		List<CustomEntity> targetEntities = new ArrayList<>();

		// Create entities
		for(org.meveo.interfaces.Entity entity : entityGraph.getEntities()) {
			var customEntity = convertToCustomEntity(entity);
			if(customEntity != null) {
				entities.put(entity.getUID(), customEntity);
			}
		}

		// Create relations
		for(EntityRelation relation : entityGraph.getRelations()) {
			ClassNotFoundException classNotFoundException = null;

			var sourceEntity = entities.get(relation.getSource().getUID());
			var targetEntity = entities.get(relation.getTarget().getUID());
			targetEntities.add(targetEntity);

			// Look-up the source entity fields
			var fields = ReflectionUtils.getAllFields(new ArrayList<>(), sourceEntity.getClass());

			try {
				var crtClass = (Class<? extends CustomRelation<CustomEntity,CustomEntity>>) Class.forName("org.meveo.model.customEntities." + relation.getType());
				var customRelation = JacksonUtil.convert(relation.getProperties(), crtClass);
				setUUIDField(customRelation, relation.getUID());
				customRelation.setSource(sourceEntity);
				customRelation.setTarget(targetEntity);

				// Case where the relation has data and is single valued
				Optional<Field> singleRelationField = fields.stream()
						.filter(f -> f.getType().equals(crtClass))
						.findFirst();
				if(singleRelationField.isPresent()) {
					singleRelationField.ifPresent(field -> {
						var setter = findSetter(field.getName(), sourceEntity.getClass());
						if(setter != null) { 
							try {
								setter.invoke(sourceEntity, customRelation);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								LOG.error("Can't set value", e);
							}
						}
					});
					continue;
				}

				// Case where the relation has data and is multi valued
				Optional<Field> multivaluedRelationField = fields.stream()
						.filter(f -> f.getGenericType() instanceof ParameterizedType)
						.filter(f -> f.getGenericType().getTypeName().equals("java.util.List<org.meveo.model.customEntities." + relation.getType() + ">"))
						.findFirst();
				if(multivaluedRelationField.isPresent()) {
					multivaluedRelationField.ifPresent(field -> {
						ReflectionUtils.findValueWithGetter(sourceEntity, field.getName())
							.map(value -> (Collection<CustomRelation<CustomEntity, CustomEntity>>) value)
							.ifPresent(values -> values.add(customRelation));
					});
					continue;
				}
			} catch (ClassNotFoundException e) {
				classNotFoundException = e;
			}

			// Case where the relation has no data and is annotated
			Optional<Field> annotatedField = fields.stream()
					.filter(f -> f.isAnnotationPresent(Relation.class))
					.filter(f -> f.getAnnotation(Relation.class).value().equals(relation.getType()))
					.findFirst();
			if(annotatedField.isPresent()) {
				annotatedField.ifPresent(field -> {
					if(field.getType().equals(List.class)) {
						// Relation is multi valued
						ReflectionUtils.findValueWithGetter(sourceEntity, field.getName())
						.map(value -> (Collection<CustomEntity>) value)
						.ifPresent(values -> values.add(targetEntity));
					} else {
						// Relation is mono valued
						var setter = findSetter(field.getName(), sourceEntity.getClass());
						if(setter != null) {
							try {
								setter.invoke(sourceEntity, targetEntity);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								LOG.error("Can't set value", e);
							}
						}
					}

				});
				continue;
			}

			// If process failed, log the error
			if(classNotFoundException != null) {
				LOG.error("Can't find class", classNotFoundException);
			}
		}

		// Remove target entities, as they are embedded in other objects
		targetEntities.forEach(entities.values()::remove);
		return entities.values();
	}

	/**
	 * @param entity entity to convert
	 * @return a corresponding instance of {@link CustomEntity}
	 * @throws ClassNotFoundException if the type of the entity does not exists in class path
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static CustomEntity convertToCustomEntity(org.meveo.interfaces.Entity entity) {
		Class<? extends CustomEntity> cetClass;
		
		try {
			cetClass = (Class<? extends CustomEntity>) Class.forName("org.meveo.model.customEntities." + entity.getType());
		} catch (ClassNotFoundException e) {
			LOG.error("Can't find class", e);
			return null;
		}
		
		var customEntity = JacksonUtil.convert(entity.getProperties(), cetClass);
		setUUIDField(customEntity, entity.getUID());

		// Handle the case where the entity has the target of a relation embedded
		for(var property : entity.getProperties().entrySet()) {
			if(property.getValue() instanceof Map) {
				var mapValue = (Map<String, Object>) property.getValue();
				var optPropertyValue = ReflectionUtils.findValueWithGetter(customEntity, property.getKey());
				optPropertyValue.ifPresent(propValue -> {
					if(propValue instanceof CustomRelation) {
						var customRelation = (CustomRelation) propValue;
						if(customRelation.getSource() == null) {
							customRelation.setSource(customEntity);
						}
						if(customRelation.getTarget() == null) {
							var targetField = ReflectionUtils.getField(customRelation.getClass(), "target");
							var targetEntity = new org.meveo.interfaces.Entity.Builder()
									.name(UUID.randomUUID().toString())
									.properties(mapValue)
									.type(targetField.getType().getSimpleName())
									.build();
							
							var target = convertToCustomEntity(targetEntity);
							customRelation.setTarget(target);
						}
					}
				});
			}

		}
		return customEntity;
	}

	/**
	 * Convert a collection of {@link CustomEntity} to an {@link EntityGraph}, merging duplicated entities
	 * 
	 * @param mainEntities The entities that will compose the graph
	 * @return the corresponding entity graph
	 */
	public static EntityGraph toEntityGraph(Collection<CustomEntity> mainEntities) {
		List<org.meveo.interfaces.Entity> entities = new ArrayList<>();
		List<EntityRelation> relations = new ArrayList<>();
		List<CustomEntity> subEntities = new ArrayList<>();
		Set<CustomEntity> customEntities = new HashSet<>(mainEntities);
		EntityGraph entityGraph = new EntityGraph(entities, relations);
		
		populateGraph(entityGraph, customEntities, subEntities, new HashSet<>());
	
		mergeEntities(subEntities, customEntities, entityGraph);
		
		return entityGraph;
	}

	private static void mergeEntities(List<CustomEntity> subEntities, Set<CustomEntity> customEntities, EntityGraph entityGraph) {
		// Build a map of entities by uuid
		Map<String, CustomEntity> customEntitiesByUuid = Stream.concat(customEntities.stream(), subEntities.stream())
				.distinct()
				.collect(Collectors.toMap(CustomEntity::getUuid, Function.identity()));

		// Merge entities that are equals
		List<org.meveo.interfaces.Entity> entitiesToRemove = new ArrayList<>();
		Map<String, List<org.meveo.interfaces.Entity>> sameTypeEntities = entityGraph.getEntities()
				.stream()
				.collect(Collectors.groupingBy(org.meveo.interfaces.Entity::getType, Collectors.toList()));
		
		sameTypeEntities.forEach((type, entityList) -> {
			int size = entityList.size();
			for(int i = 0; i < size; i ++) {
				org.meveo.interfaces.Entity entity1 = entityList.get(i);
				CustomEntity cetInstance1 = customEntitiesByUuid.get(entity1.getName());
				if(cetInstance1 == null) {
					continue;
				}
				for(int j = i+1; j < size; j++) {
					org.meveo.interfaces.Entity entity2 = entityList.get(j);
					CustomEntity cetInstance2 = customEntitiesByUuid.get(entity2.getName());
					if(cetInstance2 != null && cetInstance1.isEqual(cetInstance2)) {
						entitiesToRemove.add(entity2);
						entity1.merge(entity2);
						entityGraph.getRelations().stream()
						.forEach(r -> {
							if(r.getSource().equals(entity2)) {
								r.setSource(entity1);
							}
							if(r.getTarget().equals(entity2)) {
								r.setTarget(entity1);
							}
						});
					}
				}
			}
		});
		entityGraph.getEntities().removeAll(entitiesToRemove);
	}

	@SuppressWarnings("unchecked")
	private static void populateGraph(EntityGraph entityGraph, Collection<CustomEntity> customEntities, Collection<CustomEntity> subEntities, Collection<CustomEntity> entitiesToSkip) {
		for(var customEntity : customEntities) {
			// Avoid circular loops
			if(entitiesToSkip.contains(customEntity)) {
				continue;
			} else {
				entitiesToSkip.add(customEntity);
			}
			
			if(customEntity.getUuid() == null) {
				setUUIDField(customEntity, UUID.randomUUID().toString());
			}

			Class<? extends CustomEntity> customClass = customEntity.getClass();

			var fields = JacksonUtil.convert(customEntity, GenericTypeReferences.MAP_STRING_OBJECT);
			var relationshipsFields = new HashMap<String, Object>();

			// Retain all relationships fields annotated with @Relation
			for(var field : Map.copyOf(fields).entrySet()) {
				var fieldDef = ReflectionUtils.getField(customClass, field.getKey());
				boolean isRelationshipField = fieldDef.isAnnotationPresent(Relation.class) || CustomRelation.class.isAssignableFrom(fieldDef.getType());
				
				if(!isRelationshipField && Collection.class.isAssignableFrom(fieldDef.getType())) {
					Type actualType = ((ParameterizedType) fieldDef.getGenericType()).getActualTypeArguments()[0];
					if(actualType instanceof Class && CustomRelation.class.isAssignableFrom((Class<?>) actualType)) {
						isRelationshipField = true;
					} else {
						try {
							var actualClass = Class.forName(actualType.getTypeName());
							isRelationshipField = CustomRelation.class.isAssignableFrom(actualClass);
						} catch (ClassNotFoundException e) {
							isRelationshipField = false;
						}
					}
				}
				
				if(isRelationshipField) {
					relationshipsFields.put(field.getKey(), field.getValue());
					fields.remove(field.getKey());
				}
			}

			var entity = new org.meveo.interfaces.Entity.Builder()
					.name(customEntity.getUuid())
					.type(customEntity.getCetCode())
					.properties(fields)
					.build();

			entityGraph.getEntities().add(entity);

			// Extract sub entities and relations
			for(var field : relationshipsFields.keySet()) {
				var fieldDef = ReflectionUtils.getField(customClass, field);
				var value = ReflectionUtils.findValueWithGetter(customEntity, field);
				if(value.isPresent() && (value.get() instanceof Collection)) {
					if(!isCollectionOfCustomRelation(fieldDef)) {
						// Case where the field is a collection of custom entity annotated with @Relation
						
						Collection<CustomEntity> customEntitiesValue = (Collection<CustomEntity>) value.get();
						 
						subEntities.addAll(customEntitiesValue);
						populateGraph(entityGraph, List.copyOf(customEntitiesValue), subEntities, entitiesToSkip);
						
						for(var relatedCustomEntity : customEntitiesValue) {
							entityGraph.getEntities().stream()
								.filter(e -> e.getName().equals(relatedCustomEntity.getUuid()))
								.findFirst()
								.ifPresentOrElse(e -> {
									var relation = new org.meveo.interfaces.EntityRelation.Builder()
											.name(UUID.randomUUID().toString())
											.source(entity)
											.target(e)
											.type(fieldDef.getAnnotation(Relation.class).value())
											.properties(Map.of())
											.build();
									entityGraph.getRelations().add(relation);
								}, () -> { 
									LOG.warn("Target not found for {}", fieldDef.getAnnotation(Relation.class).value());
								});
						}
					} else {
						// Case where the field is a lsit of custom relations
						Collection<CustomRelation<?,?>> customRelations = (Collection<CustomRelation<?,?>>) value.get();
						for(CustomRelation<?,?> customRelation : customRelations) {
							subEntities.add(customRelation.getTarget());
							populateGraph(entityGraph, List.of(customRelation.getTarget()), subEntities, entitiesToSkip);
							entityGraph.getEntities().stream()
								.filter(e -> e.getName().equals(customRelation.getTarget().getUuid()))
								.findFirst()
								.ifPresentOrElse(e -> {
									var relation = new org.meveo.interfaces.EntityRelation.Builder()
											.name(UUID.randomUUID().toString())
											.source(entity)
											.target(e)
											.type(customRelation.getCrtCode())
											.properties(JacksonUtil.convert(customRelation, GenericTypeReferences.MAP_STRING_OBJECT))
											.build();
									entityGraph.getRelations().add(relation);
								}, () -> { 
									LOG.warn("Target not found for {}", customRelation);
								});
							
						}
					}
				} else if(value.isPresent() && value.get() instanceof CustomEntity) {
					// Case where the field is a custom entity annotated with @Relation
					CustomEntity customEntityValue = (CustomEntity) value.get();
					subEntities.add(customEntityValue);
					
					populateGraph(entityGraph, List.of(customEntityValue), subEntities, entitiesToSkip);
					
					entityGraph.getEntities().stream()
						.filter(e -> e.getName().equals(customEntityValue.getUuid()))
						.findFirst()
						.ifPresentOrElse(e -> {
							var relation = new org.meveo.interfaces.EntityRelation.Builder()
									.name(UUID.randomUUID().toString())
									.source(entity)
									.target(e)
									.type(fieldDef.getAnnotation(Relation.class).value())
									.properties(Map.of())
									.build();
							entityGraph.getRelations().add(relation);
						}, () -> { 
							LOG.warn("Target not found for {} : {}", fieldDef.getAnnotation(Relation.class).value(), customEntityValue);	
						});
					
				} else if(value.isPresent() && value.get() instanceof CustomRelation) {
					// Case where the field is a custom relation
					CustomRelation<?,?> customRelation = (CustomRelation<?,?>) value.get();
					Map<String, Object> customRelationProperties = JacksonUtil.convert(customRelation, GenericTypeReferences.MAP_STRING_OBJECT);
					if(customRelation.getTarget() == null) {
						throw new IllegalArgumentException("Target of relation " + customRelation.getCrtCode() + " (" + customRelation.getUuid() + ") is null");
					}
					
					populateGraph(entityGraph, List.of(customRelation.getTarget()), subEntities, entitiesToSkip);
					entityGraph.getEntities().stream()
						.filter(e -> e.getName().equals(customRelation.getTarget().getUuid()))
						.findFirst()
						.ifPresentOrElse(e -> {
							var relation = new org.meveo.interfaces.EntityRelation.Builder()
									.name(UUID.randomUUID().toString())
									.source(entity)
									.target(e)
									.type(customRelation.getCrtCode())
									.properties(customRelationProperties)
									.build();
							entityGraph.getRelations().add(relation);
						}, () ->  { 
							LOG.warn("Target not found for {}", customRelation);
						});
				}

			}
		}
	}
	
	private static boolean isCollectionOfCustomRelation(Field fieldDef) {
		if(Collection.class.isAssignableFrom(fieldDef.getType())) {
			Type actualType = ((ParameterizedType) fieldDef.getGenericType()).getActualTypeArguments()[0];
			if(actualType instanceof Class) {
				return CustomRelation.class.isAssignableFrom((Class<?>) actualType);
			} else {
				try {
					var actualClass = Class.forName(actualType.getTypeName());
					return CustomRelation.class.isAssignableFrom(actualClass);
				} catch (ClassNotFoundException e) {
					return false;
				}
			}
		}
		return false;
	}

	public static CustomEntityInstance fromMap(Map<String, Object> map, CustomEntityTemplate cet) {
		var cei = pojoToCei(map);
		cei.setCet(cet);
		
		if(cet != null && cet.getSqlStorageConfiguration() != null && !cet.getSqlStorageConfiguration().isStoreAsTable()) {
			cei.setCode((String) map.get("code"));
		}
		
		return cei;
	}

	/**
	 * Converts a POJO to a CEI
	 * 
	 * @param pojo to convert
	 * @return converted CEI
	 */
	public static CustomEntityInstance pojoToCei(Object pojo) {
		Map<String, Object> pojoAsMap;
		if (pojo instanceof Map) {
			pojoAsMap = (Map<String, Object>) pojo;
		} else {
			// Transform POJO into Map
			Map<String, Object> values = new HashMap<>();
			Stream.of(pojo.getClass().getMethods()).filter(m -> m.getName().startsWith("get") | m.getName().startsWith("is")).filter(m -> m.getParameterCount() == 0).forEach(m -> {
				var key = getFieldForGetter(pojo.getClass(), m);
				try {
					if (key != null) {
						var value = m.invoke(pojo);
						values.put(key, value);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			});

			pojoAsMap = new HashMap<>();
			values.entrySet().stream().forEach(e -> {
				if (e.getValue() != null && e.getValue().getClass().getAnnotation(Entity.class) != null) {
					pojoAsMap.put(e.getKey(), getIdValue(e.getValue()));
				} else {
					pojoAsMap.put(e.getKey(), e.getValue());
				}
			});
		}

		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setUuid((String) pojoAsMap.get("uuid"));
		cei.setCetCode((String) pojoAsMap.get("cetCode"));
		if(cei.getCetCode() == null) {
			cei.setCetCode(pojo.getClass().getSimpleName());
		}
		CustomFieldValues customFieldValues = new CustomFieldValues();
		pojoAsMap.forEach(customFieldValues::setValue);
		cei.setCfValues(customFieldValues);
		return cei;
	}

	/**
	 * Converts a CEI to a POJO
	 * 
	 * @param <T>       type of the pojo
	 * @param cei       the cei to convert
	 * @param pojoClass the class of the pojo
	 * @return the instance of the POJO
	 */
	public static <T> T ceiToPojo(CustomEntityInstance cei, Class<T> pojoClass) {
		Map<String, Object> pojoValues = cei.getCfValuesAsValues();
		pojoValues.put("uuid", cei.getUuid());
		pojoValues.put("cetCode", cei.getCetCode());
		return deserialize(pojoValues, pojoClass);
	}

	private static String getFieldForGetter(Class<?> clazz, Method getter) {
		String fieldName;
		if (getter.getName().startsWith("is")) {
			fieldName = getter.getName().substring(2);
		} else {
			fieldName = getter.getName().substring(3);
		}

		return ReflectionUtils.getAllFields(new ArrayList<>(), clazz).stream().filter(f -> f.getName().toUpperCase().equals(fieldName.toUpperCase())).findFirst().map(Field::getName).orElse(null);
	}

	private static void setIdField(Object object, Object id) {
		ReflectionUtils.getAllFields(new ArrayList<>(), object.getClass()).stream().filter(f -> f.getAnnotation(Id.class) != null).findFirst().ifPresent(f -> {
			try {
				f.setAccessible(true);
				f.set(object, id);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private static void setUUIDField(Object object, String value) {
		var setter = findSetter("uuid", object.getClass());
		if (setter != null) {
			try {
				setter.setAccessible(true);
				setter.invoke(object, value);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				LOG.error("Failed to set UUID", e);
			}
		}
	}

	private static Object getIdValue(Object object) {
		return ReflectionUtils.getAllFields(new ArrayList<>(), object.getClass()).stream().filter(f -> f.getAnnotation(Id.class) != null).findFirst().map(f -> {
			try {
				f.setAccessible(true);
				return f.get(object);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}).orElse(null);
	}
	
	public static String serialize(CustomEntityInstance entity) {
		Map<String, Object> values = entity.getCfValuesAsValues();
		if (values == null) {
			values = new HashMap<>();
		}
		
		values.put("uuid", entity.getUuid());
		values.put("cetCode", entity.getCetCode());
		
		// Serialize references
		values.entrySet().forEach(entry -> {
			if (entry.getValue() instanceof EntityReferenceWrapper) {
				entry.setValue(((EntityReferenceWrapper) entry.getValue()).getUuid());
			}
		});
		
		return JacksonUtil.toStringPrettyPrinted(values);
	}

	/**
	 * @param value
	 * @return
	 * @throws RuntimeException
	 */
	public static <T> T deserialize(Map<String, Object> value, Class<T> clazz) throws RuntimeException {
		try {
			T instance = clazz.getDeclaredConstructor().newInstance();
			for (var entry : value.entrySet()) {
				var setter = findSetter(entry.getKey(), clazz);
				if(setter == null) {
					continue;
				}
				
				Object lazyInitInstance = null;

				Class<?> paramType = setter.getParameters()[0].getType();

				// if type extends CustomEntity set the UUID
				if (CustomEntity.class.isAssignableFrom(paramType)) {
					if (entry.getValue() instanceof String) {
						lazyInitInstance = paramType.getDeclaredConstructor().newInstance();
						setUUIDField(lazyInitInstance, (String) entry.getValue());
						setter.invoke(instance, lazyInitInstance);
						
					} else if (entry.getValue() instanceof Map) {
						var customEntity = deserialize((Map<String, Object>) entry.getValue(), paramType);
						setter.invoke(instance, customEntity);

					} else if (entry.getValue() instanceof EntityReferenceWrapper) {
						lazyInitInstance = paramType.getDeclaredConstructor().newInstance();
						setUUIDField(lazyInitInstance, ((EntityReferenceWrapper ) entry.getValue()).getUuid());
						setter.invoke(instance, lazyInitInstance);
						
					} else if(entry.getValue() instanceof CustomEntityInstance) {
						var customEntity = ceiToPojo((CustomEntityInstance) entry.getValue(), paramType);
						setter.invoke(instance, customEntity);
					}

				} else {
					try {
						var type = setter.getParameters()[0].getParameterizedType();
						var jacksonType = TypeFactory.defaultInstance().constructType(type);
						var convertedValue = JacksonUtil.convert(entry.getValue(), jacksonType);
						setter.invoke(instance, convertedValue);

					} catch (IllegalArgumentException e) {
						try {
							lazyInitInstance = paramType.getDeclaredConstructor().newInstance();
							setIdField(lazyInitInstance, entry.getValue());
							setter.invoke(instance, lazyInitInstance);

						} catch (NoSuchMethodException nm) {
							// convert to factory if there are more types in this group
							if (setter.getParameters()[0].getType().isAssignableFrom(Instant.class)) {
								Instant val = ((Timestamp) entry.getValue()).toInstant();
								setter.invoke(instance, val);
							} else {
								LOG.error("Failed to deserialize {}", entry.getValue(), e);
							}
						}
					}
				}
			}

			return instance;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static Method findSetter(String fieldName, Class<?> clazz) {
		return Stream.of(clazz.getMethods()).filter(m -> m.getName().toUpperCase().equals("SET" + fieldName.toUpperCase())).findFirst().orElse(null);
	}

}
